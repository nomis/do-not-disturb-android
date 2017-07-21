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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.Receiver.RegisterAt;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.WakeLock;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.api.support.app.AbstractIntentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Intent;
import uk.me.sa.android.do_not_disturb.data.InterruptionFilter;
import uk.me.sa.android.do_not_disturb.data.Rule;
import uk.me.sa.android.do_not_disturb.data.RulesDAO;
import uk.me.sa.android.do_not_disturb.ui.ActiveRuleNotification;

@EIntentService
public class DoNotDisturb extends AbstractIntentService {
	private static final Logger log = LoggerFactory.getLogger(DoNotDisturb.class);

	public static final String ACTION_INTERUPPTION_FILTER_CHANGED = "uk.me.sa.android.do_not_disturb.service.DoNotDisturb.UPDATE";
	public static final String ACTION_TEMPORARY_ENABLE = "uk.me.sa.android.do_not_disturb.service.DoNotDisturb.ENABLE";
	public static final String ACTION_TEMPORARY_DISABLE = "uk.me.sa.android.do_not_disturb.service.DoNotDisturb.DISABLE";

	InterruptionFilter currentInterruptionFilter = InterruptionFilter.UNKNOWN;
	InterruptionFilter desiredInterruptionFilter = InterruptionFilter.UNKNOWN;
	boolean success = true;

	@SuppressLint("UseSparseArrays")
	Map<Long, Rule> rules = new HashMap<Long, Rule>();

	@Bean
	RulesDAO db;

	@SystemService
	NotificationManager notificationManager;

	@Bean
	ActiveRuleNotification activeRuleNotification;

	public DoNotDisturb() {
		super(DoNotDisturb.class.getSimpleName());
	}

	@Override
	public void onCreate() {
		log.debug("Created");
		super.onCreate();
		loadRules();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		log.debug("Destroyed");
	}

	@ServiceAction(ACTION_TEMPORARY_ENABLE)
	void enableRule(Long ruleId) {
		log.info("Enable {}", ruleId);
		setRuleTemporarilyDisabled(ruleId, false);
	}

	@ServiceAction(ACTION_TEMPORARY_DISABLE)
	void disableRule(Long ruleId) {
		log.info("Disable {}", ruleId);
		setRuleTemporarilyDisabled(ruleId, true);
	}

	@WakeLock(tag = "intent")
	void setRuleTemporarilyDisabled(Long id, boolean value) {
		Rule rule = db.getRule(id);
		if (rule != null) {
			rule.setTemporarilyDisabled(value);
			db.updateRuleTemporarilyDisabled(rule);
		} else {
			log.error("Rule {} does not exist", id);
		}
	}

	@ServiceAction(ACTION_INTERUPPTION_FILTER_CHANGED)
	@WakeLock(tag = "intent")
	void onInterruptionFilterChanged(InterruptionFilter interruptionFilter) {
		log.info("Interruption filter changed from {} to {}", currentInterruptionFilter, interruptionFilter);
		currentInterruptionFilter = interruptionFilter;

		if (currentInterruptionFilter == desiredInterruptionFilter || /* OnePlus 5 is broken... */(currentInterruptionFilter == InterruptionFilter.SILENT
				&& desiredInterruptionFilter == InterruptionFilter.ALARMS)) {
			log.info("Successfully set interruption filter");
			success = true;
		} else if (desiredInterruptionFilter != InterruptionFilter.UNKNOWN) {
			log.info("Failed to set interruption filter");
			success = (currentInterruptionFilter == InterruptionFilter.NORMAL);
		}

		BackgroundExecutor.cancelAll("delayed", false);
		updateStateDelayed();
	}

	@Background(serial = "update")
	@WakeLock(tag = "load")
	void loadRules() {
		log.debug("Load rules");
		rules.clear();
		for (Rule rule : db.getRules()) {
			if (rule.isEnabled()) {
				rules.put(rule.getId(), rule);
			}
		}

		updateState();
	}

	@Background(serial = "database")
	@WakeLock(tag = "database_changed")
	void updateRule(long id) {
		Rule rule = db.getRule(id);

		if (rule != null && rule.isEnabled()) {
			rules.put(rule.getId(), rule);
		} else {
			if (rules.remove(id) == null) {
				return;
			}
		}

		updateState();
	}

	@Receiver(registerAt = RegisterAt.OnCreateOnDestroy, local = true, actions = RulesDAO.BROADCAST_INSERT)
	void onDatabaseInsert(@Receiver.Extra(RulesDAO.EXTRA_RULE_ID) Long id) {
		log.debug("Insert {}", id);
		updateRule(id);
	}

	@Receiver(registerAt = RegisterAt.OnCreateOnDestroy, local = true, actions = RulesDAO.BROADCAST_UPDATE)
	void onDatabaseUpdate(@Receiver.Extra(RulesDAO.EXTRA_RULE_ID) Long id) {
		log.debug("Update {}", id);
		updateRule(id);
	}

	@Receiver(registerAt = RegisterAt.OnCreateOnDestroy, local = true, actions = RulesDAO.BROADCAST_DELETE)
	void onDatabaseDelete(@Receiver.Extra(RulesDAO.EXTRA_RULE_ID) Long id) {
		log.debug("Delete {}", id);
		updateRule(id);
	}

	@Receiver(actions = Intent.ACTION_TIME_TICK, registerAt = RegisterAt.OnCreateOnDestroy)
	void onTimeTick() {
		log.debug("Time tick");
		updateState();
	}

	@Receiver(actions = Intent.ACTION_TIME_CHANGED, registerAt = RegisterAt.OnCreateOnDestroy)
	void onTimeChanged() {
		log.debug("Time changed");
		updateState();
	}

	@Receiver(actions = Intent.ACTION_TIMEZONE_CHANGED, registerAt = RegisterAt.OnCreateOnDestroy)
	void onTimeZoneChanged() {
		log.debug("Time zone changed");
		updateState();
	}

	@Receiver(actions = NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED, registerAt = RegisterAt.OnCreateOnDestroy)
	void onInterruptionFilterChanged() {
		int interruptionFilter = notificationManager.getCurrentInterruptionFilter();
		InterruptionFilter interruptionFilterValue = InterruptionFilter.UNKNOWN;

		switch (interruptionFilter) {
		case NotificationManager.INTERRUPTION_FILTER_NONE:
			interruptionFilterValue = InterruptionFilter.SILENT;
			break;

		case NotificationManager.INTERRUPTION_FILTER_ALARMS:
			interruptionFilterValue = InterruptionFilter.ALARMS;
			break;

		case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
			interruptionFilterValue = InterruptionFilter.PRIORITY;
			break;

		case NotificationManager.INTERRUPTION_FILTER_ALL:
			interruptionFilterValue = InterruptionFilter.NORMAL;
			break;

		case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
			break;
		}

		log.info("Interruption filter changed from {} to {}", currentInterruptionFilter, interruptionFilterValue);
		currentInterruptionFilter = interruptionFilterValue;

		if (currentInterruptionFilter == desiredInterruptionFilter || /* OnePlus 5 is broken... */(currentInterruptionFilter == InterruptionFilter.SILENT
				&& desiredInterruptionFilter == InterruptionFilter.ALARMS)) {
			log.info("Successfully set interruption filter");
			success = true;
		} else if (desiredInterruptionFilter != InterruptionFilter.UNKNOWN) {
			log.info("Failed to set interruption filter");
			success = (currentInterruptionFilter == InterruptionFilter.NORMAL);
		}

		BackgroundExecutor.cancelAll("delayed", false);
		updateStateDelayed();
	}

	@SupposeBackground
	SortedSet<Rule> getActiveRules() {
		Date now = new Date();
		SortedSet<Rule> activeRules = new TreeSet<Rule>();

		for (Rule rule : rules.values()) {
			if (rule.isActive(now)) {
				activeRules.add(rule);
			} else if (rule.isTemporarilyDisabled()) {
				log.info("Rule {} no longer temporarily disabled", rule);
				rule.setTemporarilyDisabled(false);
				db.updateRuleTemporarilyDisabled(rule);
			}
		}

		return activeRules;
	}

	@Background(serial = "update")
	@WakeLock(tag = "update")
	void updateState() {
		SortedSet<Rule> activeRules = getActiveRules();

		log.debug("Update state");

		desiredInterruptionFilter = InterruptionFilter.NORMAL;

		for (Rule rule : activeRules) {
			if (rule.isTemporarilyDisabled()) {
				log.debug("Rule {} is inactive", rule);
			} else {
				log.debug("Rule {} is active", rule);
				if (desiredInterruptionFilter.compareTo(rule.getLevel()) > 0) {
					desiredInterruptionFilter = rule.getLevel();
				}
			}
		}

		if (currentInterruptionFilter == desiredInterruptionFilter) {
			log.info("Leaving interruption filter at {}", desiredInterruptionFilter.name());
		} else if (success) {
			log.info("Setting interruption filter to {}", desiredInterruptionFilter.name());
			setInterruptionFilter(desiredInterruptionFilter);
		} else {
			log.info("Unable to set interruption filter to {}", desiredInterruptionFilter.name());
		}

		activeRuleNotification.setActiveRules(activeRules);
	}

	@Background(delay = 1000, id = "delayed", serial = "delayed")
	@WakeLock(tag = "delayed")
	void updateStateDelayed() {
		updateState();
	}

	void setInterruptionFilter(InterruptionFilter interruptionFilter) {
		switch (interruptionFilter) {
		case NORMAL:
			notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
			break;

		case PRIORITY:
			notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
			break;

		case ALARMS:
			notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
			break;

		case SILENT:
			notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
			break;

		case UNKNOWN:
			break;
		}
	}
}
