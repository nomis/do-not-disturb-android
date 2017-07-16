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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import android.content.Context;

@EBean
public class RuleText {
	@RootContext
	Context context;

	public String getDays(Rule rule) {
		StringBuilder sb = new StringBuilder();
		Set<Integer> days = rule.getCalendarDays();
		SimpleDateFormat dayOfWeek = new SimpleDateFormat("EEE");
		Calendar c = GregorianCalendar.getInstance();
		c.clear();
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());

		Integer first = null;
		Integer last = null;
		for (int i = 0; i < 7; i++) {
			if (days.contains(c.get(Calendar.DAY_OF_WEEK))) {
				if (first != null) {
					first = c.get(Calendar.DAY_OF_WEEK);
				}
				last = c.get(Calendar.DAY_OF_WEEK);

				if (i < 6) {
					// Check next day, except on the last day which must output any residual days
					continue;
				}
			}

			if (first != null) {
				if (sb.length() > 0) {
					sb.append(context.getResources().getString(R.string.days_separator));
				}

				if (first == last) {
					sb.append(dayOfWeek.format(first));
				} else {
					sb.append(context.getResources().getString(R.string.fmt_day_range, dayOfWeek.format(first), dayOfWeek.format(last)));
				}

				first = last = null;
			}
		}

		if (sb.length() == 0) {
			sb.append(context.getResources().getString(R.string.no_days));
		}

		return sb.toString();
	}

	public String getStartTime(Rule rule) {
		return String.format(context.getResources().getString(R.string.fmt_start_time, rule.getStartHour(), rule.getStartMinute()));
	}

	public String getEndTime(Rule rule) {
		return String.format(context.getResources().getString(rule.isEndNextDay() ? R.string.fmt_end_time_next_day : R.string.fmt_end_time_same_day,
				rule.getEndHour(), rule.getEndMinute()));
	}

	public String getLevel(Rule rule) {
		return context.getResources().getString(rule.getLevel().getDescription());
	}

	public String getDescription(Rule rule) {
		return String.format(context.getResources().getString(R.string.fmt_description, getDays(rule), getStartTime(rule), getEndTime(rule), getLevel(rule)));
	}
}
