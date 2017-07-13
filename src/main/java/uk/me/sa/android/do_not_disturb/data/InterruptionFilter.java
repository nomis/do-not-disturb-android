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

public enum InterruptionFilter {
	SILENT ("Total silence"), ALARMS ("Alarms only"), PRIORITY ("Priority only"), NORMAL (null), UNKNOWN (null);

	private String desc;

	private InterruptionFilter(String desc) {
		this.desc = desc;
	}

	public String toString() {
		return desc;
	}
}
