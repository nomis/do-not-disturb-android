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

import uk.me.sa.android.do_not_disturb.R;

public enum InterruptionFilter {
	SILENT (R.string.if_silent), ALARMS (R.string.if_alarms), PRIORITY (R.string.if_priority), NORMAL (R.string.if_unknown), UNKNOWN (R.string.if_unknown);

	private int desc;

	private InterruptionFilter(int desc) {
		this.desc = desc;
	}

	public int getDescription() {
		return desc;
	}

	public static InterruptionFilter safeValueOf(String value) {
		try {
			return valueOf(value);
		} catch (IllegalArgumentException e) {
			return InterruptionFilter.UNKNOWN;
		}
	}
}
