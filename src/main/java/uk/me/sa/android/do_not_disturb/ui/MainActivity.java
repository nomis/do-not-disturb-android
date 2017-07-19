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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import uk.me.sa.android.do_not_disturb.data.RulesDAO;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.widget.ListView;
import android.widget.Toast;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main_activity_actions)
public class MainActivity extends Activity {
	@ViewById
	ListView rules;

	@Bean
	RuleListAdapter adapter;

	@Bean
	RulesDAO db;

	@AfterViews
	void bindAdapter() {
		rules.setAdapter(adapter);
	}

	@ItemClick(R.id.rules)
	void ruleClicked(Rule rule) {
		if (rule == null) {
			addRule();
		} else {
			editRule(rule);
		}
	}

	void editRule(Rule rule) {
		Intent intent = new Intent(EditRuleActivity.ACTION_EDIT, null, this, EditRuleActivity_.class);
		intent.putExtra(EditRuleActivity.EXTRA_RULE_ID, rule.getId());
		startActivity(intent);
	}

	void addRule() {
		new TextDialog(this, R.string.add_rule, null, R.string.enter_rule_name) {
			void onTextChanged(String value) {
				Integer error = new Rule().isNameValid(db, value);
				if (error == null) {
					setValid();
				} else {
					setInvalid(error);
				}
			}

			void onSuccess(final String value) {
				Integer error = new Rule().isNameValid(db, value);
				if (error == null) {
					addRule(value);
				} else {
					Toast.makeText(MainActivity.this, getResources().getString(R.string.error_adding_rule, getResources().getString(error)), Toast.LENGTH_SHORT)
							.show();
				}
			}
		};
	}

	@Background
	void addRule(String name) {
		Rule rule = new Rule();

		rule.setName(name);
		if (db.addRule(rule)) {
			adapter.loadRules(); // TODO use broadcast

			editRule(rule);
		} else {
			Toast.makeText(this, R.string.error_adding_rule, Toast.LENGTH_SHORT).show();
		}
	}

	@OptionsItem(R.id.menu_access)
	void openNotificationAccess() {
		startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}
