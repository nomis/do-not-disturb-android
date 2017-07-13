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
package uk.me.sa.android.do_not_disturb.util;

import org.junit.Assert;
import org.junit.Test;

public class TestTimeUtil {
	@Test
	public void testBefore() {
		Assert.assertEquals(TimeUtil.compareHourMinute(0, 0, 0, 1), -1);
		Assert.assertEquals(TimeUtil.compareHourMinute(0, 0, 1, 0), -1);
		Assert.assertEquals(TimeUtil.compareHourMinute(0, 0, 1, 59), -1);
		Assert.assertEquals(TimeUtil.compareHourMinute(0, 0, 2, 0), -1);
		Assert.assertEquals(TimeUtil.compareHourMinute(22, 59, 23, 0), -1);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 0, 23, 59), -1);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 58, 23, 59), -1);
	}

	@Test
	public void testEquals() {
		Assert.assertEquals(TimeUtil.compareHourMinute(0, 0, 0, 0), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(0, 1, 0, 1), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(1, 0, 1, 0), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(1, 59, 1, 59), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(2, 0, 2, 0), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(22, 59, 22, 59), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 0, 23, 0), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 58, 23, 58), 0);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 59, 23, 59), 0);
	}

	@Test
	public void testAfter() {
		Assert.assertEquals(TimeUtil.compareHourMinute(0, 1, 0, 0), 1);
		Assert.assertEquals(TimeUtil.compareHourMinute(1, 0, 0, 0), 1);
		Assert.assertEquals(TimeUtil.compareHourMinute(1, 59, 1, 0), 1);
		Assert.assertEquals(TimeUtil.compareHourMinute(2, 0, 0, 0), 1);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 0, 22, 59), 1);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 59, 23, 0), 1);
		Assert.assertEquals(TimeUtil.compareHourMinute(23, 59, 23, 58), 1);
	}
}
