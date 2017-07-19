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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.androidannotations.annotations.EBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

@EBean
public class RulesDAO extends SQLiteOpenHelper {
	private static final Logger log = LoggerFactory.getLogger(RulesDAO.class);

	private static final String DATABASE_NAME = "rules";
	private static final int DATABASE_VERSION = 2;

	static final String RULES_COLUMN_ID = "id";
	static final String RULES_COLUMN_ENABLED = "enabled";
	static final String RULES_COLUMN_TEMPORARILY_DISABLED = "temporarily_disabled";
	static final String RULES_COLUMN_NAME = "name";
	static final String RULES_COLUMN_DAYS = "days";
	static final String RULES_COLUMN_START_HOUR = "start_hour";
	static final String RULES_COLUMN_START_MINUTE = "start_minute";
	static final String RULES_COLUMN_END_HOUR = "end_hour";
	static final String RULES_COLUMN_END_MINUTE = "end_minute";
	static final String RULES_COLUMN_LEVEL = "interruption_filter";

	private static final String RULES_TABLE_NAME = "rules";
	private static final String RULES_TABLE_CREATE = "CREATE TABLE " + RULES_TABLE_NAME + " (" + RULES_COLUMN_ID + " INTEGER PRIMARY KEY, "
			+ RULES_COLUMN_ENABLED + " INTEGER NOT NULL, " + RULES_COLUMN_TEMPORARILY_DISABLED + " INTEGER NOT NULL, " + RULES_COLUMN_NAME + " TEXT NOT NULL, "
			+ RULES_COLUMN_DAYS + " INTEGER NOT NULL, " + RULES_COLUMN_START_HOUR + " INTEGER NOT NULL, " + RULES_COLUMN_START_MINUTE + " INTEGER NOT NULL, "
			+ RULES_COLUMN_END_HOUR + " INTEGER NOT NULL, " + RULES_COLUMN_END_MINUTE + " INTEGER NOT NULL, " + RULES_COLUMN_LEVEL + " TEXT NOT NULL);";
	private static final String RULES_INDEX_NAME_CREATE = "CREATE UNIQUE INDEX " + RULES_TABLE_NAME + "_" + RULES_COLUMN_NAME + " ON " + RULES_TABLE_NAME
			+ " (" + RULES_COLUMN_NAME + ");";

	public RulesDAO(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		log.info("Create database version {}", DATABASE_VERSION);
		execCreateCommands(db, 0);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		log.info("Upgrade database version from {} to {}", oldVersion, newVersion);
		execCreateCommands(db, oldVersion);
	}

	private void execCreateCommands(SQLiteDatabase db, int oldVersion) {
		switch (oldVersion) {
		case 0:
			db.execSQL(RULES_TABLE_CREATE);
		case 1:
			db.execSQL(RULES_INDEX_NAME_CREATE);
		case 2:
			break;
		}
	}

	private ContentValues toContentValues(Rule rule) {
		ContentValues values = new ContentValues(16);
		values.put(RULES_COLUMN_ENABLED, rule.isEnabled() ? 1 : 0);
		values.put(RULES_COLUMN_TEMPORARILY_DISABLED, rule.isTemporarilyDisabled() ? 1 : 0);
		values.put(RULES_COLUMN_NAME, rule.getName());
		values.put(RULES_COLUMN_DAYS, rule.getDays());
		values.put(RULES_COLUMN_START_HOUR, rule.getStartHour());
		values.put(RULES_COLUMN_START_MINUTE, rule.getStartMinute());
		values.put(RULES_COLUMN_END_HOUR, rule.getEndHour());
		values.put(RULES_COLUMN_END_MINUTE, rule.getEndMinute());
		values.put(RULES_COLUMN_LEVEL, rule.getLevel().name());
		return values;
	}

	public boolean addRule(Rule rule) {
		log.info("Add {}", rule);

		long id;
		ContentValues values = toContentValues(rule);

		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			id = db.insert(RULES_TABLE_NAME, null, values);
			if (id != -1) {
				log.debug("New rule id: {}", id);
				rule.setId(id);
				db.setTransactionSuccessful();
			}
		} finally {
			db.endTransaction();
		}

		db.close();
		return id != -1;
	}

	public boolean updateRule(Rule rule) {
		log.info("Update {}", rule);

		int rows;
		ContentValues values = toContentValues(rule);

		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			rows = db.update(RULES_TABLE_NAME, values, RULES_COLUMN_ID + " = ?", new String[] { String.valueOf(rule.getId()) });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();
		return rows > 0;
	}

	public void deleteRule(Rule rule) {
		log.info("Delete {}", rule);

		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			db.delete(RULES_TABLE_NAME, RULES_COLUMN_ID + " = ?", new String[] { String.valueOf(rule.getId()) });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();
	}

	private Rule loadRuleFromCursor(Cursor c) {
		Rule rule = new Rule();
		rule.setId(c.getLong(c.getColumnIndexOrThrow(RULES_COLUMN_ID)));
		rule.setEnabled(c.getInt(c.getColumnIndexOrThrow(RULES_COLUMN_ENABLED)) != 0);
		rule.setTemporarilyDisabled(c.getInt(c.getColumnIndexOrThrow(RULES_COLUMN_TEMPORARILY_DISABLED)) != 0);
		rule.setName(c.getString(c.getColumnIndexOrThrow(RULES_COLUMN_NAME)));
		rule.setDays(c.getInt(c.getColumnIndexOrThrow(RULES_COLUMN_DAYS)));
		rule.setStartHour(c.getInt(c.getColumnIndexOrThrow(RULES_COLUMN_START_HOUR)));
		rule.setStartMinute(c.getInt(c.getColumnIndexOrThrow(RULES_COLUMN_START_MINUTE)));
		rule.setEndHour(c.getInt(c.getColumnIndexOrThrow(RULES_COLUMN_END_HOUR)));
		rule.setEndMinute(c.getInt(c.getColumnIndexOrThrow(RULES_COLUMN_END_MINUTE)));
		rule.setLevel(InterruptionFilter.safeValueOf(c.getString(c.getColumnIndexOrThrow(RULES_COLUMN_LEVEL))));
		return rule;
	}

	public Rule getRule(long id) {
		Rule rule = null;

		SQLiteDatabase db = getReadableDatabase();
		db.beginTransaction();
		try {
			Cursor c = db.query(RULES_TABLE_NAME, null, RULES_COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null);
			if (c.moveToNext()) {
				rule = loadRuleFromCursor(c);
			}
			c.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();

		log.info("Get rule: {}", rule);
		return rule;
	}

	public Rule getRule(String name) {
		Rule rule = null;

		SQLiteDatabase db = getReadableDatabase();
		db.beginTransaction();
		try {
			Cursor c = db.query(RULES_TABLE_NAME, null, RULES_COLUMN_NAME + " = ?", new String[] { name }, null, null, null);
			if (c.moveToNext()) {
				rule = loadRuleFromCursor(c);
			}
			c.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();

		log.info("Get rule: {}", rule);
		return rule;
	}

	public Collection<Rule> getRules() {
		List<Rule> rules = new ArrayList<Rule>();

		SQLiteDatabase db = getReadableDatabase();
		db.beginTransaction();
		try {
			Cursor c = db.query(RULES_TABLE_NAME, null, null, null, null, null, null);
			while (c.moveToNext()) {
				rules.add(loadRuleFromCursor(c));
			}
			c.close();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		db.close();

		log.info("Get rules: {}", rules.size());
		return rules;
	}
}
