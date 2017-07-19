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
package uk.me.sa.android.do_not_disturb.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import uk.me.sa.android.do_not_disturb.R;
import uk.me.sa.android.do_not_disturb.util.TimeUtil;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableBiMap;

public class Rule implements Comparable<Rule>, Cloneable {
	private static ImmutableBiMap<Integer, Integer> CALENDAR_DAYS_TO_BITMAP;
	private static ImmutableBiMap<Integer, Integer> CALENDAR_BITMAP_TO_DAYS;

	static {
		int value = 0;
		ImmutableBiMap.Builder<Integer, Integer> builder = ImmutableBiMap.builder();
		builder.put(Calendar.MONDAY, 1 << (value++));
		builder.put(Calendar.TUESDAY, 1 << (value++));
		builder.put(Calendar.WEDNESDAY, 1 << (value++));
		builder.put(Calendar.THURSDAY, 1 << (value++));
		builder.put(Calendar.FRIDAY, 1 << (value++));
		builder.put(Calendar.SATURDAY, 1 << (value++));
		builder.put(Calendar.SUNDAY, 1 << (value++));
		CALENDAR_DAYS_TO_BITMAP = builder.build();
		CALENDAR_BITMAP_TO_DAYS = CALENDAR_DAYS_TO_BITMAP.inverse();
	}

	private long id = 0;
	private boolean enabled = true;
	private boolean temporarilyDisabled = false;

	private String name;
	private int days;
	private int startHour;
	private int startMinute;
	private int endHour;
	private int endMinute;
	private InterruptionFilter level = InterruptionFilter.PRIORITY;

	public Rule() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isTemporarilyDisabled() {
		return temporarilyDisabled;
	}

	public void setTemporarilyDisabled(boolean temporarilyDisabled) {
		this.temporarilyDisabled = temporarilyDisabled;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer isNameValid(RulesDAO db, String value) {
		if (TextUtils.getTrimmedLength(value) > 0) {
			Rule other = db.getRule(value);

			if (other == null || id == other.id) {
				return null;
			} else {
				return R.string.rule_name_in_use;
			}
		} else {
			return R.string.rule_name_empty;
		}
	}

	int getDays() {
		return this.days;
	}

	void setDays(int days) {
		this.days = days;
	}

	public Set<Integer> getCalendarDays() {
		Set<Integer> calendarDays = new HashSet<Integer>();
		int bitmapDay = 1;

		for (int i = 0; i < Integer.SIZE; i++, bitmapDay <<= 1) {
			if ((days & bitmapDay) != 0) {
				Integer calendarDay = CALENDAR_BITMAP_TO_DAYS.get(bitmapDay);
				if (calendarDay != null) {
					calendarDays.add(calendarDay);
				}
			}
		}

		return calendarDays;
	}

	public void setCalendarDays(Set<Integer> calendarDays) {
		int value = 0;

		for (Integer calendarDay : calendarDays) {
			Integer bitmapDay = CALENDAR_DAYS_TO_BITMAP.get(calendarDay);
			if (bitmapDay != null) {
				value |= bitmapDay;
			}
		}

		days = value;
	}

	public void setCalendarDay(int calendarDay, boolean enabled) {
		if (enabled) {
			days |= CALENDAR_DAYS_TO_BITMAP.get(calendarDay);
		} else {
			days &= ~CALENDAR_DAYS_TO_BITMAP.get(calendarDay);
		}
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public int getEndMinute() {
		return endMinute;
	}

	public void setEndMinute(int endMinute) {
		this.endMinute = endMinute;
	}

	public InterruptionFilter getLevel() {
		return level;
	}

	public void setLevel(InterruptionFilter level) {
		this.level = level;
	}

	public boolean isEndNextDay() {
		return TimeUtil.compareHourMinute(endHour, endMinute, startHour, startMinute) <= 0;
	}

	public boolean isActive(Date when) {
		if (!enabled) {
			return false;
		}

		Calendar c = GregorianCalendar.getInstance();
		c.setTime(when);

		boolean beforeStart = TimeUtil.compareHourMinute(c, startHour, startMinute) < 0;
		boolean beforeEnd = TimeUtil.compareHourMinute(c, endHour, endMinute) < 0;

		if (isEndNextDay()) {
			if (!beforeStart) {
				return (days & CALENDAR_DAYS_TO_BITMAP.get(c.get(Calendar.DAY_OF_WEEK))) != 0;
			}

			if (!beforeEnd) {
				return false;
			}

			c.add(Calendar.DATE, -1);
			return (days & CALENDAR_DAYS_TO_BITMAP.get(c.get(Calendar.DAY_OF_WEEK))) != 0;
		} else {
			if ((days & CALENDAR_DAYS_TO_BITMAP.get(c.get(Calendar.DAY_OF_WEEK))) == 0) {
				return false;
			}

			return !beforeStart && beforeEnd;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Rule) {
			return compareTo((Rule)o) == 0;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	public int compareTo(Rule o) {
		Preconditions.checkNotNull(o);
		return ComparisonChain.start().compare(name, o.name).compare(level, o.level).compare(days, o.days).compare(startHour, o.startHour)
				.compare(startMinute, o.startMinute).compare(endHour, o.endHour).compare(endMinute, o.endMinute).compare(id, o.id)
				.compareTrueFirst(enabled, o.enabled).compareTrueFirst(temporarilyDisabled, o.temporarilyDisabled).result();
	}

	@Override
	public String toString() {
		return "Rule[id=" + id + ",name=" + name + ",level=" + level.name() + ",days=" + days
				+ String.format(",start=%02d:%02d,end=%02d:%02d", startHour, startMinute, endHour, endMinute) + ",enabled=" + enabled + ",temporarilyDisabled="
				+ temporarilyDisabled + "]";
	}

	@Override
	public Rule clone() {
		try {
			return (Rule)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
