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

import java.util.List;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import uk.me.sa.android.do_not_disturb.data.Rule;
import uk.me.sa.android.do_not_disturb.data.RulesDAO;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.common.collect.ImmutableList;

@EBean
public class RuleListAdapter extends BaseAdapter {
	List<Rule> rules = ImmutableList.of();

	@RootContext
	Context context;

	@Bean
	RulesDAO db;

	@AfterInject
	void initAdapter() {
		loadRules();
	}

	@Background
	void loadRules() {
		updateRules(ImmutableList.sortedCopyOf(db.getRules()));
	}

	@UiThread
	void updateRules(List<Rule> rules) {
		this.rules = rules;
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == rules.size()) {
			AddRuleLine addRuleLine;

			if (convertView == null) {
				addRuleLine = AddRuleLine_.build(context);
			} else {
				addRuleLine = (AddRuleLine)convertView;
			}

			return addRuleLine;
		} else {
			RuleView ruleView;

			if (convertView == null) {
				ruleView = RuleView_.build(context);
			} else {
				ruleView = (RuleView)convertView;
			}

			ruleView.bind(getItem(position));
			return ruleView;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return position == rules.size() ? 1 : 0;
	}

	@Override
	public int getCount() {
		return rules.size() + 1;
	}

	@Override
	public Rule getItem(int position) {
		if (position == rules.size())
			return null;
		return rules.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
