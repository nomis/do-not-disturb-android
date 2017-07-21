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

import java.util.Map;
import java.util.Set;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.Receiver.RegisterAt;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.UiThread.Propagation;
import org.androidannotations.annotations.ViewById;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.primitives.Booleans;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import uk.me.sa.android.do_not_disturb.data.RulesDAO;

@EActivity(R.layout.edit_rule)
@OptionsMenu(R.menu.edit_rule_actions)
public class EditRuleActivity extends Activity {
	private static final Logger log = LoggerFactory.getLogger(EditRuleActivity.class);

	public static final String ACTION_EDIT = EditRuleActivity.class.getName() + ".EDIT";
	public static final String EXTRA_RULE_ID = "RULE_ID";

	@Bean
	RuleText ruleText;

	@ViewById
	Switch enabled;

	@ViewById
	TextView name;

	@ViewById
	TextView days;

	@ViewById
	TextView start;

	@ViewById
	TextView end;

	@ViewById
	TextView level;

	@Bean
	RulesDAO db;

	@Extra(value = EXTRA_RULE_ID)
	Long id;

	volatile Rule rule;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadRule();
	}

	@AfterExtras
	void onEdit() {
		log.debug("Edit {}", id);

		if (id != null) {
			loadRule();
		} else {
			log.error("Missing rule id");
			finish();
		}
	}

	@Background
	void loadRule() {
		Rule load = db.getRule(id);

		if (load != null) {
			rule = load;
			showRule();
		} else {
			log.error("Rule {} does not exist", id);
			runOnUiThread(new Runnable() {
				public void run() {
					finish();
				}
			});
		}
	}

	@AfterViews
	@UiThread(propagation = Propagation.REUSE)
	void showRule() {
		log.debug("Show {}", rule);

		if (rule != null) {
			Rule show = rule.clone();

			getActionBar().setTitle(show.getName());
			enabled.setText(getResources().getString(show.isEnabled() ? R.string.on : R.string.off));
			enabled.setChecked(show.isEnabled());
			name.setText(show.getName());
			days.setText(ruleText.getDays(show));
			start.setText(ruleText.getStartTime(show));
			end.setText(ruleText.getEndTime(show));
			level.setText(ruleText.getLevel(show));
		}
	}

	@Receiver(registerAt = RegisterAt.OnStartOnStop, local = true, actions = RulesDAO.BROADCAST_UPDATE)
	void onDatabaseUpdate(@Receiver.Extra(RulesDAO.EXTRA_RULE_ID) Long id) {
		if (id == rule.getId()) {
			log.info("Update {}", rule);
			loadRule();
		}
	}

	@Receiver(registerAt = RegisterAt.OnStartOnStop, local = true, actions = RulesDAO.BROADCAST_DELETE)
	void onDatabaseDelete(@Receiver.Extra(RulesDAO.EXTRA_RULE_ID) Long id) {
		if (id == rule.getId()) {
			log.info("Delete {}", rule);
			finish();
		}
	}

	@Click(R.id.enabled)
	void toggleEnabled() {
		toggleEnabled(enabled.isChecked());
	}

	@Background(serial = "enabled")
	void toggleEnabled(boolean value) {
		Rule save = rule.clone();
		save.setEnabled(value);
		savedRule(db.updateRuleEnabled(save));
	}

	@Click(R.id.name_row)
	void editName() {
		new TextDialog(this, R.string.rule_name, rule.getName(), R.string.enter_rule_name) {
			@Override
			Integer checkText(String value) {
				return rule.isNameValid(db, value);
			}

			@Override
			void saveText(final String value) {
				Integer error = rule.isNameValid(db, value);
				if (error == null) {
					Rule save = rule.clone();
					save.setName(value);
					savedRule(db.updateRuleName(save));
				} else {
					Toast.makeText(EditRuleActivity.this, getResources().getString(R.string.error_updating_rule, getResources().getString(error)),
							Toast.LENGTH_SHORT).show();
				}
			}
		};
	}

	@Click(R.id.days_row)
	void editDays() {
		Map<String, Integer> weekdays = ruleText.getLongWeekdays();
		final Integer[] orderedWeekdays = weekdays.values().toArray(new Integer[weekdays.size()]);
		final Set<Integer> ruleDays = rule.getCalendarDays();
		boolean[] checkedDays = Booleans.toArray(Collections2.transform(weekdays.values(), new Function<Integer, Boolean>() {
			@Override
			public Boolean apply(Integer input) {
				return ruleDays.contains(input);
			}
		}));

		new AlertDialog.Builder(this).setTitle(R.string.days).setPositiveButton(R.string.done, null)
				.setMultiChoiceItems(weekdays.keySet().toArray(new String[weekdays.size()]), checkedDays, new OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which, final boolean isChecked) {
						new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								Rule save = rule.clone();
								save.setCalendarDay(orderedWeekdays[which], isChecked);
								savedRule(db.updateRuleDays(save));
								return null;
							}
						}.execute();
					}
				}).create().show();
	}

	@Click(R.id.start_time_row)
	void editStartTime() {
		Rule edit = rule.clone();
		new TimePickerDialog(this, new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						Rule save = rule.clone();
						save.setStartHour(hourOfDay);
						save.setStartMinute(minute);
						savedRule(db.updateRuleStartTime(save));
						return null;
					}
				}.execute();
			}
		}, edit.getStartHour(), edit.getStartMinute(), DateFormat.is24HourFormat(this)).show();
	}

	@Click(R.id.end_time_row)
	void editEndTime() {
		Rule edit = rule.clone();
		new TimePickerDialog(this, new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						Rule save = rule.clone();
						save.setEndHour(hourOfDay);
						save.setEndMinute(minute);
						savedRule(db.updateRuleEndTime(save));
						return null;
					}
				}.execute();
			}
		}, edit.getEndHour(), edit.getEndMinute(), DateFormat.is24HourFormat(this)).show();
	}

	void savedRule(boolean success) {
		if (!success) {
			Toast.makeText(this, getResources().getString(R.string.error_updating_rule, getResources().getString(R.string.database_write_failed)),
					Toast.LENGTH_SHORT).show();
		}
	}

	@OptionsItem(R.id.delete_rule)
	void deleteRule() {
		if (rule != null) {
			new AlertDialog.Builder(this).setMessage(getResources().getString(R.string.confirm_delete_rule, rule.getName()))
					.setPositiveButton(R.string.delete_rule, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new AsyncTask<Void, Void, Void>() {
								@Override
								protected Void doInBackground(Void... params) {
									savedRule(db.deleteRule(rule));
									return null;
								}
							}.execute();
						}
					}).setNegativeButton(android.R.string.cancel, null).create().show();
		}
	}
}
