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
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.SupposeUiThread;
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
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import uk.me.sa.android.do_not_disturb.data.RulesDAO;

@EActivity(R.layout.edit_rule)
public class EditRuleActivity extends Activity {
	private static final Logger log = LoggerFactory.getLogger(EditRuleActivity.class);

	public static final String ACTION_EDIT = EditRuleActivity.class.getName() + ".EDIT";
	public static final String EXTRA_RULE_ID = "RULE_ID";

	interface UpdateRule {
		public void apply(Rule rule);
	}

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

	Rule rule;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@AfterExtras
	void onEdit() {
		log.debug("Edit {}", id);

		if (id == null) {
			log.error("Missing rule id");
			finish();
			return;
		}

		synchronized (this) {
			rule = db.getRule(id);
			if (rule == null) {
				log.error("Rule {} does not exist", id);
				finish();
				return;
			}
		}
	}

	@AfterViews
	@SupposeUiThread
	void showRule() {
		log.debug("Show {}", rule);

		getActionBar().setTitle(rule.getName());
		enabled.setText(getResources().getString(rule.isEnabled() ? R.string.on : R.string.off));
		enabled.setChecked(rule.isEnabled());
		name.setText(rule.getName());
		days.setText(ruleText.getDays(rule));
		start.setText(ruleText.getStartTime(rule));
		end.setText(ruleText.getEndTime(rule));
		level.setText(ruleText.getLevel(rule));
	}

	@Click(R.id.name_row)
	synchronized void editName() {
		new TextDialog(this, R.string.rule_name, rule.getName(), R.string.enter_rule_name) {
			@Override
			Integer checkText(String value) {
				synchronized (this) {
					return rule.isNameValid(db, value);
				}
			}

			@Override
			boolean saveText(final String value) {
				synchronized (this) {
					Integer error = rule.isNameValid(db, value);
					if (error == null) {
						rule.setName(value);
						return saveRule();
					} else {
						Toast.makeText(EditRuleActivity.this, getResources().getString(R.string.error_updating_rule, getResources().getString(error)),
								Toast.LENGTH_SHORT).show();
						return false;
					}
				}
			}

			@Override
			void onSuccess() {
				showRule();
			}
		};
	}

	@Click(R.id.days_row)
	synchronized void editDays() {
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
						new AsyncTask<Void, Void, Boolean>() {
							@Override
							protected Boolean doInBackground(Void... params) {
								synchronized (this) {
									rule.setCalendarDay(orderedWeekdays[which], isChecked);
									return saveRule();
								}
							}

							@Override
							protected void onPostExecute(Boolean result) {
								if (result) {
									showRule();
								}
							}
						}.execute();
					}
				}).create().show();
	}

	@Click(R.id.start_time_row)
	synchronized void editStartTime() {
		new TimePickerDialog(this, new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
				new AsyncTask<Void, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Void... params) {
						synchronized (this) {
							rule.setStartHour(hourOfDay);
							rule.setStartMinute(minute);
							return saveRule();
						}
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (result) {
							showRule();
						}
					}
				}.execute();
			}
		}, rule.getStartHour(), rule.getStartMinute(), true).show();
	}

	@Click(R.id.end_time_row)
	synchronized void editEndTime() {
		new TimePickerDialog(this, new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
				new AsyncTask<Void, Void, Boolean>() {
					@Override
					protected Boolean doInBackground(Void... params) {
						synchronized (this) {
							rule.setEndHour(hourOfDay);
							rule.setEndMinute(minute);
							return saveRule();
						}
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (result) {
							showRule();
						}
					}
				}.execute();
			}
		}, rule.getEndHour(), rule.getEndMinute(), true).show();
	}

	@SupposeBackground
	boolean saveRule() {
		if (db.updateRule(rule)) {
			rule = db.getRule(rule.getId());
			return true;
		} else {
			Toast.makeText(this, getResources().getString(R.string.error_updating_rule, getResources().getString(R.string.database_write_failed)),
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}
}
