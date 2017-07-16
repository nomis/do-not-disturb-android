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
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

public class TestRule {
	@Test
	public void isActiveSameDay() {
		Rule rule = new Rule();
		Set<Integer> days = rule.getCalendarDays();
		days.add(Calendar.SUNDAY);
		rule.setCalendarDays(days);
		rule.setStartHour(15);
		rule.setEndHour(23);

		Calendar c = GregorianCalendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("Europe/London"));
		c.clear();

		c.set(2017, 6, 9, 19, 26, 36);
		Assert.assertTrue(rule.isActive(c.getTime()));

		c.set(2017, 6, 9, 14, 59, 59);
		Assert.assertFalse(rule.isActive(c.getTime()));

		c.set(2017, 6, 9, 15, 00, 00);
		Assert.assertTrue(rule.isActive(c.getTime()));

		c.set(2017, 6, 9, 15, 01, 00);
		Assert.assertTrue(rule.isActive(c.getTime()));

		c.set(2017, 6, 9, 22, 58, 0);
		Assert.assertTrue(rule.isActive(c.getTime()));

		c.set(2017, 6, 9, 22, 59, 0);
		Assert.assertTrue(rule.isActive(c.getTime()));

		c.set(2017, 6, 9, 22, 59, 59);
		Assert.assertTrue(rule.isActive(c.getTime()));

		c.set(2017, 6, 9, 23, 00, 00);
		Assert.assertFalse(rule.isActive(c.getTime()));
	}
}
