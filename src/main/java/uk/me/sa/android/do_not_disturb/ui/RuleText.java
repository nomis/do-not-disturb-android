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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import android.content.Context;
import android.text.format.DateFormat;
import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.data.Rule;

@EBean
public class RuleText {
	@RootContext
	Context context;

	Map<String, Integer> shortWeekdays;
	Map<String, Integer> shortWeekdaysReverse;
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
		shortWeekdaysReverse = ImmutableMap.copyOf(ImmutableList.copyOf(shortWeekdays.entrySet()).reverse());
		longWeekdays = buildLongWeekdays.build();
	}

	public String getDaysSummary(Rule rule) {
		Set<Integer> days = rule.getCalendarDays();

		if (days.isEmpty()) {
			return context.getResources().getString(R.string.no_days);
		} else if (days.size() == shortWeekdays.size()) {
			return context.getResources().getString(R.string.all_days);
		}

		StringBuilder sb = new StringBuilder();
		String first = null;
		String last = null;
		boolean fromStart = true;
		String truncate = null;
		for (Iterator<Entry<String, Integer>> it = shortWeekdays.entrySet().iterator(); it.hasNext();) {
			Entry<String, Integer> weekday = it.next();

			if (weekday.getKey().equals(truncate)) {
				// Stop early if the days were checked backwards
				break;
			}

			if (days.contains(weekday.getValue())) {
				if (first == null) {
					first = weekday.getKey();
				}
				last = weekday.getKey();

				if (it.hasNext()) {
					// Check next day, except on the last day which must output any residual days
					continue;
				}
			} else if (fromStart && first != null) {
				// Check backwards from the end
				for (Iterator<Entry<String, Integer>> itReverse = shortWeekdaysReverse.entrySet().iterator(); itReverse.hasNext();) {
					Entry<String, Integer> weekdayReverse = itReverse.next();

					if (weekdayReverse.getKey().equals(weekday.getKey())) {
						break;
					}

					if (days.contains(weekdayReverse.getValue())) {
						first = truncate = weekdayReverse.getKey();
					} else {
						break;
					}
				}

				fromStart = false;
			} else {
				fromStart = false;
			}

			if (first != null) {
				if (sb.length() > 0) {
					sb.append(context.getResources().getString(R.string.days_separator));
				}

				if (last == null || first.equals(last)) {
					sb.append(first);
				} else {
					sb.append(context.getResources().getString(R.string.fmt_day_range, first, last));
				}

				first = last = null;
			}
		}

		return sb.toString();
	}

	public String getDays(Rule rule) {
		Set<Integer> days = rule.getCalendarDays();

		if (days.isEmpty()) {
			return context.getResources().getString(R.string.no_days);
		}

		StringBuilder sb = new StringBuilder();
		for (Iterator<Entry<String, Integer>> it = shortWeekdays.entrySet().iterator(); it.hasNext();) {
			Entry<String, Integer> weekday = it.next();

			if (days.contains(weekday.getValue())) {
				if (sb.length() > 0) {
					sb.append(context.getResources().getString(R.string.days_separator));
				}

				sb.append(weekday.getKey());
			}
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
		Calendar c = GregorianCalendar.getInstance();
		c.clear();
		c.set(Calendar.HOUR_OF_DAY, rule.getStartHour());
		c.set(Calendar.MINUTE, rule.getStartMinute());
		return DateFormat.getTimeFormat(context).format(c.getTime());
	}

	public String getEndTime(Rule rule) {
		Calendar c = GregorianCalendar.getInstance();
		c.clear();
		c.set(Calendar.HOUR_OF_DAY, rule.getEndHour());
		c.set(Calendar.MINUTE, rule.getEndMinute());
		String time = DateFormat.getTimeFormat(context).format(c.getTime());
		if (rule.isEndNextDay()) {
			return String.format(context.getResources().getString(R.string.fmt_next_day, time));
		} else {
			return time;
		}
	}

	public String getLevel(Rule rule) {
		return context.getResources().getString(rule.getLevel().getDescription());
	}

	public String getDescription(Rule rule) {
		if (rule.isEnabled()) {
			return String.format(
					context.getResources().getString(R.string.fmt_description, getDaysSummary(rule), getStartTime(rule), getEndTime(rule), getLevel(rule)));
		} else {
			return context.getResources().getString(R.string.off);
		}
	}

	public String getNotification(Rule rule) {
		return context.getResources().getString(rule.getLevel().getDescription());
	}
}
