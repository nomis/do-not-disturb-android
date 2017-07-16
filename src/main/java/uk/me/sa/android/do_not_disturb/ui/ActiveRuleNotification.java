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
package uk.me.sa.android.do_not_disturb.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.me.sa.android.do_not_disturb.NotificationListener;
import uk.me.sa.android.do_not_disturb.NotificationListener_;
import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.common.base.Objects;

public final class ActiveRuleNotification {
	private static final Logger log = LoggerFactory.getLogger(ActiveRuleNotification.class);

	private Context context;
	private NotificationManager notificationManager;
	@SuppressLint("UseSparseArrays")
	private Map<Long, Rule> rules = new HashMap<Long, Rule>();

	public ActiveRuleNotification(Context context) {
		this.context = context;
		notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	public synchronized void setActiveRules(Set<Rule> newRules) {
		Set<Long> newRuleIds = new HashSet<Long>();

		for (Rule newRule : newRules) {
			newRuleIds.add(newRule.getId());

			if (Objects.equal(rules.get(newRule.getId()), newRule)) {
				continue;
			}

			log.info("Adding notification for {}", newRule);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentTitle(newRule.getName())
					.setContentText(newRule.getLevel().toString()).setOngoing(true).setPriority(Notification.PRIORITY_HIGH);

			Intent editIntent = new Intent(EditRuleActivity.ACTION_EDIT, null, context, EditRuleActivity_.class);
			editIntent.putExtra(EditRuleActivity.EXTRA_RULE_ID, newRule.getId());
			builder.addAction(android.R.drawable.ic_menu_edit, context.getString(R.string.notification_edit),
					PendingIntent.getService(context, 0, editIntent, 0));

			if (newRule.isTemporarilyDisabled()) {
				Intent enableIntent = new Intent(NotificationListener.ACTION_TEMPORARY_ENABLE, null, context, NotificationListener_.class);
				enableIntent.putExtra(EditRuleActivity.EXTRA_RULE_ID, newRule.getId());

				builder.setSmallIcon(R.drawable.ic_do_not_disturb_off);
				builder.addAction(android.R.drawable.button_onoff_indicator_off, context.getString(R.string.notification_enable),
						PendingIntent.getService(context, 0, enableIntent, 0));
			} else {
				Intent disableIntent = new Intent(NotificationListener.ACTION_TEMPORARY_DISABLE, null, context, NotificationListener_.class);
				disableIntent.putExtra(EditRuleActivity.EXTRA_RULE_ID, newRule.getId());

				builder.setSmallIcon(R.drawable.ic_do_not_disturb_on);
				builder.addAction(android.R.drawable.button_onoff_indicator_on, context.getString(R.string.notification_disable),
						PendingIntent.getService(context, 0, disableIntent, 0));
			}

			Notification notification = builder.build();
			notification.visibility = Notification.VISIBILITY_PUBLIC;
			notificationManager.notify((int)newRule.getId(), notification);

			rules.put(newRule.getId(), newRule.clone());
		}

		for (Iterator<Long> it = rules.keySet().iterator(); it.hasNext();) {
			Long id = it.next();

			if (!newRuleIds.contains(id)) {
				log.info("Removing notification for {}", rules.get(id));
				notificationManager.cancel(id.intValue());

				it.remove();
			}
		}
	}
}
