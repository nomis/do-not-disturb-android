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

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

@EViewGroup(R.layout.list_rule)
public class RuleView extends LinearLayout {
	@Bean
	RuleText ruleText;

	@ViewById
	TextView name;

	@ViewById
	TextView description;

	public RuleView(Context context) {
		super(context);
	}

	public void bind(Rule rule) {
		name.setText(rule.getName());
		description.setText(ruleText.getDescription(rule));
	}
}
