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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.me.sa.android.do_not_disturb.data.InterruptionFilter;
import uk.me.sa.android.do_not_disturb.data.Rule;
import uk.me.sa.android.do_not_disturb.ui.ActiveRuleNotification;
import android.content.Context;
import android.os.Handler;

public class DoNotDisturbManager implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(DoNotDisturbManager.class);

	private InterruptionFilter currentInterruptionFilter = InterruptionFilter.UNKNOWN;
	private InterruptionFilter desiredInterruptionFilter = InterruptionFilter.UNKNOWN;
	private boolean success = true;
	private Collection<Rule> rules = Collections.emptySet();

	private Context context;
	private NotificationListener notificationListener;
	private ActiveRuleNotification activeRuleNotification;
	private Handler handler = new Handler();

	public DoNotDisturbManager(Context context, NotificationListener notificationListener) {
		this.context = context;
		this.notificationListener = notificationListener;

		activeRuleNotification = new ActiveRuleNotification(context);
	}

	public synchronized void setRules(Collection<Rule> rules) {
		this.rules = rules;
		updateState();
	}

	public synchronized void updateInterruptionFilter(InterruptionFilter newInterruptionFilter) {
		log.info("Interruption filter changed from " + currentInterruptionFilter + " to " + newInterruptionFilter);
		currentInterruptionFilter = newInterruptionFilter;

		if (currentInterruptionFilter == desiredInterruptionFilter || /* OnePlus 5 is broken... */(currentInterruptionFilter == InterruptionFilter.SILENT
				&& desiredInterruptionFilter == InterruptionFilter.ALARMS)) {
			log.info("Successfully set interruption filter");
			success = true;
		} else if (desiredInterruptionFilter != InterruptionFilter.UNKNOWN) {
			log.info("Failed to set interruption filter");
			success = (currentInterruptionFilter == InterruptionFilter.NORMAL);
		}

		handler.removeCallbacks(this);
		handler.postDelayed(this, 1000);
	}

	public synchronized void updateDateTime() {
		updateState();
	}

	private synchronized SortedSet<Rule> getActiveRules() {
		Date now = new Date();
		SortedSet<Rule> activeRules = new TreeSet<Rule>();
		for (Rule rule : rules) {
			if (rule.isActive(now)) {
				activeRules.add(rule);
			}
		}

		return activeRules;
	}

	private synchronized void updateState() {
		SortedSet<Rule> activeRules = getActiveRules();

		log.info("Update state");

		desiredInterruptionFilter = InterruptionFilter.NORMAL;

		for (Rule rule : activeRules) {
			if (rule.isTemporarilyDisabled()) {
				log.info("Rule {} is inactive", rule);
			} else {
				log.info("Rule {} is active", rule);
				if (desiredInterruptionFilter.compareTo(rule.level) > 0) {
					desiredInterruptionFilter = rule.level;
				}
			}
		}

		if (currentInterruptionFilter == desiredInterruptionFilter) {
			log.info("Leaving interruption filter at {}", desiredInterruptionFilter.name());
		} else if (success) {
			log.info("Setting interruption filter to {}", desiredInterruptionFilter.name());
			notificationListener.setInterruptionFilter(desiredInterruptionFilter);
		} else {
			log.info("Unable to set interruption filter to {}", desiredInterruptionFilter.name());
		}

		activeRuleNotification.setActiveRules(activeRules);
	}

	@Override
	public synchronized void run() {
		updateState();
	}
}
