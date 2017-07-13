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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import uk.me.sa.android.do_not_disturb.util.TimeUtil;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

public class Rule implements Comparable<Rule> {
	public final int id;
	private boolean enabled = true;
	private boolean temporarilyDisabled = false;

	public String name;
	public final Set<Integer> days = new HashSet<Integer>();
	public int startHour;
	public int startMinute;
	public int endHour;
	public int endMinute;
	public InterruptionFilter level = InterruptionFilter.PRIORITY;

	public Rule(int id) {
		this.id = id;
	}

	public Rule(Rule rule) {
		this(rule.id);
		this.enabled = rule.enabled;
		this.temporarilyDisabled = rule.temporarilyDisabled;
		this.name = rule.name;
		this.days.addAll(rule.days);
		this.startHour = rule.startHour;
		this.startMinute = rule.startMinute;
		this.endHour = rule.endHour;
		this.endMinute = rule.endMinute;
		this.level = rule.level;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		temporarilyDisabled = false;
	}

	public boolean isTemporarilyDisabled() {
		return temporarilyDisabled;
	}

	public void setTemporarilyDisabled(boolean temporarilyDisabled) {
		this.temporarilyDisabled = temporarilyDisabled;
	}

	public boolean isActive(Date when) {
		if (!enabled) {
			return false;
		}

		Calendar c = GregorianCalendar.getInstance();
		c.setTime(when);

		boolean beforeStart = TimeUtil.compareHourMinute(c, startHour, startMinute) < 0;
		boolean beforeEnd = TimeUtil.compareHourMinute(c, endHour, endMinute) < 0;
		boolean endNextDay = TimeUtil.compareHourMinute(endHour, endMinute, startHour, startMinute) <= 0;

		if (endNextDay) {
			if (!beforeStart) {
				return days.contains(c.get(Calendar.DAY_OF_WEEK));
			}

			if (!beforeEnd) {
				return false;
			}

			c.add(Calendar.DATE, -1);
			return days.contains(c.get(Calendar.DAY_OF_WEEK));
		} else {
			if (!days.contains(c.get(Calendar.DAY_OF_WEEK))) {
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
		return Integer.hashCode(id);
	}

	private long daysBitmap() {
		long bitmap = 0;

		for (int value : days) {
			bitmap |= 1 << value;
		}

		return bitmap;
	}

	@Override
	public int compareTo(Rule o) {
		Preconditions.checkNotNull(o);
		return ComparisonChain.start().compare(name, o.name).compare(level, o.level).compare(daysBitmap(), o.daysBitmap()).compare(startHour, o.startHour)
				.compare(startMinute, o.startMinute).compare(endHour, o.endHour).compare(endMinute, o.endMinute).compare(id, o.id)
				.compareTrueFirst(enabled, o.enabled).compareTrueFirst(temporarilyDisabled, o.temporarilyDisabled).result();
	}

	@Override
	public String toString() {
		return "Rule[name=" + name + ",level=" + level + ",days=" + Arrays.toString(days.toArray())
				+ String.format(",start=%02d:%02d,end=%02d:%02d", startHour, startMinute, endHour, endMinute) + ",enabled=" + enabled + ",temporarilyDisabled="
				+ temporarilyDisabled + "]";
	}
}
