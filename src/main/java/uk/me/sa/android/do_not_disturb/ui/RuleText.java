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

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;
import android.content.Context;

import com.google.common.collect.ImmutableMap;

@EBean
public class RuleText {
	@RootContext
	Context context;

	Map<String, Integer> shortWeekdays;
	Map<String, Integer> longWeekdays;

	public RuleText(Context context) {
		ImmutableMap.Builder<String, Integer> buildShortWeekdays = ImmutableMap.<String, Integer>builder();
		ImmutableMap.Builder<String, Integer> buildLongWeekdays = ImmutableMap.<String, Integer>builder();
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
		String[] shortWeekdayStrings = dateFormatSymbols.getShortWeekdays();
		String[] longWeekdayStrings = dateFormatSymbols.getWeekdays();

		Calendar c = GregorianCalendar.getInstance();
		c.clear();
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());

		for (int i = 0; i < 7; i++, c.add(Calendar.DAY_OF_WEEK, 1)) {
			buildShortWeekdays.put(shortWeekdayStrings[c.get(Calendar.DAY_OF_WEEK)], c.get(Calendar.DAY_OF_WEEK));
			buildLongWeekdays.put(longWeekdayStrings[c.get(Calendar.DAY_OF_WEEK)], c.get(Calendar.DAY_OF_WEEK));
		}

		shortWeekdays = buildShortWeekdays.build();
		longWeekdays = buildLongWeekdays.build();
	}

	public String getDays(Rule rule) {
		StringBuilder sb = new StringBuilder();
		Set<Integer> days = rule.getCalendarDays();

		String first = null;
		String last = null;
		for (Iterator<Entry<String, Integer>> it = shortWeekdays.entrySet().iterator(); it.hasNext();) {
			Entry<String, Integer> weekday = it.next();

			if (days.contains(weekday.getValue())) {
				if (first == null) {
					first = weekday.getKey();
				}
				last = weekday.getKey();

				if (it.hasNext()) {
					// Check next day, except on the last day which must output any residual days
					continue;
				}
			}

			if (first != null) {
				if (sb.length() > 0) {
					sb.append(context.getResources().getString(R.string.days_separator));
				}

				if (last == null || first == last) {
					sb.append(first);
				} else {
					sb.append(context.getResources().getString(R.string.fmt_day_range, first, last));
				}

				first = last = null;
			}
		}

		if (sb.length() == 0) {
			sb.append(context.getResources().getString(R.string.no_days));
		}

		return sb.toString();
	}

	public Map<String, Integer> getShortWeekdays() {
		return shortWeekdays;
	}

	public Map<String, Integer> getLongWeekdays() {
		return longWeekdays;
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
