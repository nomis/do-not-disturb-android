/*
	do-not-disturb-android - Android Do not Disturb Service

	Copyright 2017  Simon Arlott

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.me.sa.android.do_not_disturb;

import java.util.HashMap;
import java.util.Map;

import org.androidannotations.annotations.EService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import uk.me.sa.android.do_not_disturb.data.InterruptionFilter;
import uk.me.sa.android.do_not_disturb.data.Rule;

@EService
public class NotificationListener extends NotificationListenerService {
	private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

	public static final String ACTION_ENABLE = NotificationListener.class.getName() + ".ENABLE";
	public static final String ACTION_DISABLE = NotificationListener.class.getName() + ".DISABLE";
	public static final String ACTION_EDIT = NotificationListener.class.getName() + ".EDIT";
	public static final String EXTRA_RULE_ID = NotificationListener.class.getName() + ".RULE_ID";

	DoNotDisturbManager dndManager;
	@SuppressLint("UseSparseArrays")
	Map<Integer, Rule> rules = new HashMap<Integer, Rule>();

	@Override
	public void onCreate() {
		dndManager = new DoNotDisturbManager(getApplicationContext(), this);
	}

	@Override
	public void onListenerConnected() {
		log.info("Notification listener connected");
		onInterruptionFilterChanged(getCurrentInterruptionFilter());
	}

	@Override
	public synchronized int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_STICKY;
		}

		if (intent.getAction().equals(ACTION_ENABLE) || intent.getAction().equals(ACTION_DISABLE)) {
			int id = intent.getExtras().getInt(EXTRA_RULE_ID);

			if (rules.containsKey(id)) {
				rules.get(id).setTemporarilyDisabled(intent.getAction().equals(ACTION_DISABLE));
				dndManager.setRules(rules.values());
			}
		}
		return START_STICKY;
	}

	@Override
	public void onInterruptionFilterChanged(int interruptionFilter) {
		String interruptionFilterName = Integer.toString(interruptionFilter);
		InterruptionFilter interruptionFilterValue = InterruptionFilter.UNKNOWN;

		switch (interruptionFilter) {
		case INTERRUPTION_FILTER_NONE:
			interruptionFilterName = "NONE (silent)";
			interruptionFilterValue = InterruptionFilter.SILENT;
			break;

		case INTERRUPTION_FILTER_ALARMS:
			interruptionFilterName = "ALARMS only";
			interruptionFilterValue = InterruptionFilter.ALARMS;
			break;

		case INTERRUPTION_FILTER_PRIORITY:
			interruptionFilterName = "PRIORITY only";
			interruptionFilterValue = InterruptionFilter.PRIORITY;
			break;

		case INTERRUPTION_FILTER_ALL:
			interruptionFilterName = "ALL (normal)";
			interruptionFilterValue = InterruptionFilter.NORMAL;
			break;

		case INTERRUPTION_FILTER_UNKNOWN:
			interruptionFilterName = "UNKNOWN";
			break;
		}

		log.debug("Interruption filter changed to {}", interruptionFilterName);
		dndManager.updateInterruptionFilter(interruptionFilterValue);
	}

	void setInterruptionFilter(InterruptionFilter interruptionFilter) {
		switch (interruptionFilter) {
		case NORMAL:
			requestInterruptionFilter(NotificationListener.INTERRUPTION_FILTER_ALL);
			break;

		case PRIORITY:
			requestInterruptionFilter(NotificationListener.INTERRUPTION_FILTER_PRIORITY);
			break;

		case ALARMS:
			requestInterruptionFilter(NotificationListener.INTERRUPTION_FILTER_ALARMS);
			break;

		case SILENT:
			requestInterruptionFilter(NotificationListener.INTERRUPTION_FILTER_NONE);
			break;

		case UNKNOWN:
			break;
		}
	}
}
