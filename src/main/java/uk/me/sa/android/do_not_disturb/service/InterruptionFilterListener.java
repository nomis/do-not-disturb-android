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
package uk.me.sa.android.do_not_disturb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import uk.me.sa.android.do_not_disturb.data.InterruptionFilter;

public class InterruptionFilterListener extends NotificationListenerService implements ServiceConnection {
	private static final Logger log = LoggerFactory.getLogger(InterruptionFilterListener.class);

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log.info("Started");
		return START_STICKY;
	}

	@Override
	public void onListenerConnected() {
		log.info("Notification listener connected");
		onInterruptionFilterChanged(getCurrentInterruptionFilter());

		boolean bound = bindService(new Intent(this, DoNotDisturb_.class), this, Context.BIND_AUTO_CREATE);
		log.info("Service bound: {}", bound);
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
		// DoNotDisturb_.intent(this).onInterruptionFilterChanged(interruptionFilterValue).start();
	}

	@Override
	public void onListenerDisconnected() {
		log.info("Notification listener disconnected");
		onInterruptionFilterChanged(INTERRUPTION_FILTER_UNKNOWN);
		unbindService(this);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		log.info("DoNotDisturb service connected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		log.info("DoNotDisturb service disconnected");
	}
}
