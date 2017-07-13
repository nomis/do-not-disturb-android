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

import java.util.Calendar;

public class TimeUtil {
	private TimeUtil() {
	}

	public static int compareHourMinute(int hour1, int minute1, int hour2, int minute2) {
		if (hour1 < hour2) {
			return -1;
		} else if (hour1 == hour2) {
			if (minute1 < minute2) {
				return -1;
			} else if (minute1 == minute2) {
				return 0;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	public static int compareHourMinute(Calendar calendar1, int hour2, int minute2) {
		return compareHourMinute(calendar1.get(Calendar.HOUR_OF_DAY), calendar1.get(Calendar.MINUTE), hour2, minute2);
	}

	public static int compareHourMinute(int hour1, int minute1, Calendar calendar2) {
		return compareHourMinute(hour1, minute1, calendar2.get(Calendar.HOUR_OF_DAY), calendar2.get(Calendar.MINUTE));
	}
}
