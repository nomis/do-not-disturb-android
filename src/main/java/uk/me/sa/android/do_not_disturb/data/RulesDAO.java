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
import java.util.Set;

import org.androidannotations.annotations.EBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;

@EBean
public class RulesDAO extends SQLiteOpenHelper {
	private static final Logger log = LoggerFactory.getLogger(RulesDAO.class);

	private static final String DATABASE_NAME = "rules";
	private static final int DATABASE_VERSION = 2;

	public static final String BROADCAST_INSERT = "uk.me.sa.android.do_not_disturb.data.Rule.INSERT";
	public static final String BROADCAST_UPDATE = "uk.me.sa.android.do_not_disturb.data.Rule.UPDATE";
	public static final String BROADCAST_DELETE = "uk.me.sa.android.do_not_disturb.data.Rule.DELETE";
	public static final String EXTRA_RULE_ID = "RULE_ID";

	private static final String RULES_COLUMN_ID = "id";
	private static final String RULES_COLUMN_ENABLED = "enabled";
	private static final String RULES_COLUMN_TEMPORARILY_DISABLED = "temporarily_disabled";
	private static final String RULES_COLUMN_NAME = "name";
	private static final String RULES_COLUMN_DAYS = "days";
	private static final String RULES_COLUMN_START_HOUR = "start_hour";
	private static final String RULES_COLUMN_START_MINUTE = "start_minute";
	private static final String RULES_COLUMN_END_HOUR = "end_hour";
	private static final String RULES_COLUMN_END_MINUTE = "end_minute";
	private static final String RULES_COLUMN_LEVEL = "interruption_filter";

	private static final String RULES_TABLE_NAME = "rules";
	private static final String RULES_TABLE_CREATE = "CREATE TABLE " + RULES_TABLE_NAME + " (" + RULES_COLUMN_ID + " INTEGER PRIMARY KEY, "
			+ RULES_COLUMN_ENABLED + " INTEGER NOT NULL, " + RULES_COLUMN_TEMPORARILY_DISABLED + " INTEGER NOT NULL, " + RULES_COLUMN_NAME + " TEXT NOT NULL, "
			+ RULES_COLUMN_DAYS + " INTEGER NOT NULL, " + RULES_COLUMN_START_HOUR + " INTEGER NOT NULL, " + RULES_COLUMN_START_MINUTE + " INTEGER NOT NULL, "
			+ RULES_COLUMN_END_HOUR + " INTEGER NOT NULL, " + RULES_COLUMN_END_MINUTE + " INTEGER NOT NULL, " + RULES_COLUMN_LEVEL + " TEXT NOT NULL);";
	private static final String RULES_INDEX_NAME_CREATE = "CREATE UNIQUE INDEX " + RULES_TABLE_NAME + "_" + RULES_COLUMN_NAME + " ON " + RULES_TABLE_NAME + " ("
			+ RULES_COLUMN_NAME + ");";

	private Context context;

	public RulesDAO(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
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

	private ContentValues toContentValues(Rule rule, Set<String> columns) {
		ContentValues values = new ContentValues(columns == null ? 16 : columns.size());

		if (columns == null || columns.contains(RULES_COLUMN_ENABLED)) {
			values.put(RULES_COLUMN_ENABLED, rule.isEnabled() ? 1 : 0);
		}

		if (columns == null || columns.contains(RULES_COLUMN_TEMPORARILY_DISABLED)) {
			values.put(RULES_COLUMN_TEMPORARILY_DISABLED, rule.isTemporarilyDisabled() ? 1 : 0);
		}

		if (columns == null || columns.contains(RULES_COLUMN_NAME)) {
			values.put(RULES_COLUMN_NAME, rule.getName());
		}

		if (columns == null || columns.contains(RULES_COLUMN_DAYS)) {
			values.put(RULES_COLUMN_DAYS, rule.getDays());
		}

		if (columns == null || columns.contains(RULES_COLUMN_START_HOUR)) {
			values.put(RULES_COLUMN_START_HOUR, rule.getStartHour());
		}

		if (columns == null || columns.contains(RULES_COLUMN_START_MINUTE)) {
			values.put(RULES_COLUMN_START_MINUTE, rule.getStartMinute());
		}

		if (columns == null || columns.contains(RULES_COLUMN_END_HOUR)) {
			values.put(RULES_COLUMN_END_HOUR, rule.getEndHour());
		}

		if (columns == null || columns.contains(RULES_COLUMN_END_MINUTE)) {
			values.put(RULES_COLUMN_END_MINUTE, rule.getEndMinute());
		}

		if (columns == null || columns.contains(RULES_COLUMN_LEVEL)) {
			values.put(RULES_COLUMN_LEVEL, rule.getLevel().name());
		}

		return values;
	}

	public boolean addRule(Rule rule) {
		log.debug("Add {}", rule);
		Preconditions.checkArgument(rule.getId() == 0, "Rule id != 0");

		long id;
		ContentValues values = toContentValues(rule, null);

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

		if (id != -1) {
			Intent intent = new Intent(BROADCAST_INSERT);
			intent.putExtra(EXTRA_RULE_ID, rule.getId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		}

		return id != -1;
	}

	private boolean updateRule(Rule rule, Set<String> columns) {
		log.debug("Update {}", rule);

		int rows;
		ContentValues values = toContentValues(rule, columns);

		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			rows = db.update(RULES_TABLE_NAME, values, RULES_COLUMN_ID + " = ?", new String[] { String.valueOf(rule.getId()) });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		if (rows > 0) {
			Intent intent = new Intent(BROADCAST_UPDATE);
			intent.putExtra(EXTRA_RULE_ID, rule.getId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		}

		return rows > 0;
	}

	public boolean updateRuleEnabled(Rule rule) {
		rule.setTemporarilyDisabled(false);
		log.debug("Update rule enabled {}", rule);
		return updateRule(rule, ImmutableSet.of(RULES_COLUMN_ENABLED, RULES_COLUMN_TEMPORARILY_DISABLED));
	}

	public boolean updateRuleTemporarilyDisabled(Rule rule) {
		log.debug("Update rule temporarily disabled {}", rule);
		return updateRule(rule, ImmutableSet.of(RULES_COLUMN_TEMPORARILY_DISABLED));
	}

	public boolean updateRuleName(Rule rule) {
		log.debug("Update rule name {}", rule);
		return updateRule(rule, ImmutableSet.of(RULES_COLUMN_NAME));
	}

	public boolean updateRuleDays(Rule rule) {
		log.debug("Update rule days {}", rule);
		return updateRule(rule, ImmutableSet.of(RULES_COLUMN_DAYS));
	}

	public boolean updateRuleStartTime(Rule rule) {
		log.debug("Update rule start time {}", rule);
		return updateRule(rule, ImmutableSet.of(RULES_COLUMN_START_HOUR, RULES_COLUMN_START_MINUTE));
	}

	public boolean updateRuleEndTime(Rule rule) {
		log.debug("Update rule end time {}", rule);
		return updateRule(rule, ImmutableSet.of(RULES_COLUMN_END_HOUR, RULES_COLUMN_END_MINUTE));
	}

	public boolean updateRuleLevel(Rule rule) {
		log.debug("Update rule level {}", rule);
		return updateRule(rule, ImmutableSet.of(RULES_COLUMN_LEVEL));
	}

	public boolean deleteRule(Rule rule) {
		log.debug("Delete {}", rule);

		int rows;
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			rows = db.delete(RULES_TABLE_NAME, RULES_COLUMN_ID + " = ?", new String[] { String.valueOf(rule.getId()) });
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		if (rows > 0) {
			Intent intent = new Intent(BROADCAST_DELETE);
			intent.putExtra(EXTRA_RULE_ID, rule.getId());
			LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		}

		return rows > 0;
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

		log.debug("Get rule: {}", rule);
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

		log.debug("Get rule: {}", rule);
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

		log.debug("Get rules: {}", rules.size());
		return rules;
	}
}
