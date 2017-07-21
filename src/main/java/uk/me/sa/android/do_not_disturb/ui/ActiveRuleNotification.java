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

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import uk.me.sa.android.do_not_disturb.service.DoNotDisturb_;

@EBean
public class ActiveRuleNotification {
	private static final Logger log = LoggerFactory.getLogger(ActiveRuleNotification.class);

	@RootContext
	Context context;

	@SystemService
	NotificationManager notificationManager;

	@Bean
	RuleText ruleText;

	@SuppressLint("UseSparseArrays")
	Map<Long, Rule> rules = new HashMap<Long, Rule>();

	@AfterInject
	public void onStart() {
		log.debug("Clearing all notifications");
		notificationManager.cancelAll();
	}

	public synchronized void setActiveRules(Set<Rule> newRules) {
		Set<Long> newRuleIds = new HashSet<Long>();

		for (Rule rule : newRules) {
			newRuleIds.add(rule.getId());

			if (Objects.equal(rules.get(rule.getId()), rule)) {
				continue;
			}

			log.debug("Adding notification for {}", rule);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentTitle(rule.getName())
					.setContentText(ruleText.getNotification(rule)).setOngoing(true).setPriority(Notification.PRIORITY_HIGH).setWhen(0);

			builder.addAction(android.R.drawable.ic_menu_edit, context.getString(R.string.notification_edit),
					PendingIntent.getActivity(context, (int)rule.getId(),
							EditRuleActivity_.intent(context).ruleId(rule.getId())
									.flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP).get(),
							PendingIntent.FLAG_UPDATE_CURRENT));

			if (rule.isTemporarilyDisabled()) {
				builder.setSmallIcon(R.drawable.ic_do_not_disturb_off);
				builder.addAction(android.R.drawable.button_onoff_indicator_off, context.getString(R.string.notification_enable), PendingIntent.getService(
						context, (int)rule.getId(), DoNotDisturb_.intent(context).enableRule(rule.getId()).get(), PendingIntent.FLAG_UPDATE_CURRENT));
			} else {
				builder.setSmallIcon(R.drawable.ic_do_not_disturb_on);
				builder.addAction(android.R.drawable.button_onoff_indicator_on, context.getString(R.string.notification_disable), PendingIntent.getService(
						context, (int)rule.getId(), DoNotDisturb_.intent(context).disableRule(rule.getId()).get(), PendingIntent.FLAG_UPDATE_CURRENT));
			}

			Notification notification = builder.build();
			notification.visibility = Notification.VISIBILITY_PUBLIC;
			notificationManager.notify((int)rule.getId(), notification);

			rules.put(rule.getId(), rule.clone());
		}

		for (Iterator<Long> it = rules.keySet().iterator(); it.hasNext();) {
			Long id = it.next();

			if (!newRuleIds.contains(id)) {
				log.debug("Removing notification for {}", rules.get(id));
				notificationManager.cancel(id.intValue());

				it.remove();
			}
		}
	}
}
