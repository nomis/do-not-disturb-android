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

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import uk.me.sa.android.do_not_disturb.data.Rule;
import android.content.Context;
import android.icu.util.Calendar;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
@PrepareForTest(fullyQualifiedNames = { "uk.me.sa.android.do_not_disturb.ui.RuleText" })
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@SuppressFBWarnings({ "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" })
public class TestRuleText {
	RuleText_ ruleText;

	@org.junit.Rule
	public PowerMockRule rule = new PowerMockRule();

	@Before
	public void create() throws Exception {
		MockitoAnnotations.initMocks(this);
		ShadowToast.reset();

		Context context = Robolectric.application.getApplicationContext();
		Robolectric.shadowOf(context.getResources().getConfiguration()).setLocale(Locale.UK);

		ruleText = RuleText_.getInstance_(context);
	}

	@Test
	public void none() throws Exception {
		Rule rule = new Rule();
		Assert.assertEquals("None", ruleText.getDaysSummary(rule));
		Assert.assertEquals("None", ruleText.getDays(rule));
	}

	@Test
	public void monday() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		Assert.assertEquals("Mon", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon", ruleText.getDays(rule));
	}

	@Test
	public void tuesday() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.TUESDAY, true);
		Assert.assertEquals("Tue", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Tue", ruleText.getDays(rule));
	}

	@Test
	public void wednesday() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.WEDNESDAY, true);
		Assert.assertEquals("Wed", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Wed", ruleText.getDays(rule));
	}

	@Test
	public void thursday() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.THURSDAY, true);
		Assert.assertEquals("Thu", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Thu", ruleText.getDays(rule));
	}

	@Test
	public void friday() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.FRIDAY, true);
		Assert.assertEquals("Fri", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Fri", ruleText.getDays(rule));
	}

	@Test
	public void saturday() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.SATURDAY, true);
		Assert.assertEquals("Sat", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Sat", ruleText.getDays(rule));
	}

	@Test
	public void sunday() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Sun", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Sun", ruleText.getDays(rule));
	}

	@Test
	public void notMonday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.MONDAY, false);
		Assert.assertEquals("Tue - Sun", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Tue, Wed, Thu, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notMondayOrWednesday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.MONDAY, false);
		rule.setCalendarDay(Calendar.WEDNESDAY, false);
		Assert.assertEquals("Tue, Thu - Sun", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Tue, Thu, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notTuesday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.TUESDAY, false);
		Assert.assertEquals("Wed - Mon", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Wed, Thu, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notTuesdayOrThursday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.TUESDAY, false);
		rule.setCalendarDay(Calendar.THURSDAY, false);
		Assert.assertEquals("Fri - Mon, Wed", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Wed, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notWednesday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.WEDNESDAY, false);
		Assert.assertEquals("Thu - Tue", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Thu, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notWednesdayOrFriday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.WEDNESDAY, false);
		rule.setCalendarDay(Calendar.FRIDAY, false);
		Assert.assertEquals("Sat - Tue, Thu", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Thu, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notThursday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.THURSDAY, false);
		Assert.assertEquals("Fri - Wed", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notThursdayOrSaturday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.THURSDAY, false);
		rule.setCalendarDay(Calendar.SATURDAY, false);
		Assert.assertEquals("Sun - Wed, Fri", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Fri, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notFriday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.FRIDAY, false);
		Assert.assertEquals("Sat - Thu", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Thu, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notFridayOrSunday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.FRIDAY, false);
		rule.setCalendarDay(Calendar.SUNDAY, false);
		Assert.assertEquals("Mon - Thu, Sat", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Thu, Sat", ruleText.getDays(rule));
	}

	@Test
	public void notSaturday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.SATURDAY, false);
		Assert.assertEquals("Sun - Fri", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Thu, Fri, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notSaturdayOrMonday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.SATURDAY, false);
		rule.setCalendarDay(Calendar.MONDAY, false);
		Assert.assertEquals("Tue - Fri, Sun", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Tue, Wed, Thu, Fri, Sun", ruleText.getDays(rule));
	}

	@Test
	public void notSunday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.SUNDAY, false);
		Assert.assertEquals("Mon - Sat", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Thu, Fri, Sat", ruleText.getDays(rule));
	}

	@Test
	public void notSundayOrTuesday() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		rule.setCalendarDay(Calendar.SUNDAY, false);
		rule.setCalendarDay(Calendar.TUESDAY, false);
		Assert.assertEquals("Mon, Wed - Sat", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Wed, Thu, Fri, Sat", ruleText.getDays(rule));
	}

	@Test
	public void all1() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		rule.setCalendarDay(Calendar.TUESDAY, true);
		rule.setCalendarDay(Calendar.WEDNESDAY, true);
		rule.setCalendarDay(Calendar.THURSDAY, true);
		rule.setCalendarDay(Calendar.FRIDAY, true);
		rule.setCalendarDay(Calendar.SATURDAY, true);
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Every day", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Thu, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void all2() throws Exception {
		Rule rule = new Rule();
		rule.setAllCalendarDays();
		Assert.assertEquals("Every day", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Thu, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void weekdays() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		rule.setCalendarDay(Calendar.TUESDAY, true);
		rule.setCalendarDay(Calendar.WEDNESDAY, true);
		rule.setCalendarDay(Calendar.THURSDAY, true);
		rule.setCalendarDay(Calendar.FRIDAY, true);
		Assert.assertEquals("Mon - Fri", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Thu, Fri", ruleText.getDays(rule));
	}

	@Test
	public void weekend() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.SATURDAY, true);
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Sat - Sun", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void alternateSingles1() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		rule.setCalendarDay(Calendar.WEDNESDAY, true);
		rule.setCalendarDay(Calendar.FRIDAY, true);
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Sun - Mon, Wed, Fri", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Wed, Fri, Sun", ruleText.getDays(rule));
	}

	@Test
	public void alternateSingles2() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.TUESDAY, true);
		rule.setCalendarDay(Calendar.THURSDAY, true);
		rule.setCalendarDay(Calendar.SATURDAY, true);
		Assert.assertEquals("Tue, Thu, Sat", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Tue, Thu, Sat", ruleText.getDays(rule));
	}

	@Test
	public void alternatePairs1() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		rule.setCalendarDay(Calendar.TUESDAY, true);
		rule.setCalendarDay(Calendar.THURSDAY, true);
		rule.setCalendarDay(Calendar.FRIDAY, true);
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Sun - Tue, Thu - Fri", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Thu, Fri, Sun", ruleText.getDays(rule));
	}

	@Test
	public void alternatePairs2() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		rule.setCalendarDay(Calendar.WEDNESDAY, true);
		rule.setCalendarDay(Calendar.THURSDAY, true);
		rule.setCalendarDay(Calendar.SATURDAY, true);
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Sat - Mon, Wed - Thu", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Wed, Thu, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void alternateTriples1() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		rule.setCalendarDay(Calendar.TUESDAY, true);
		rule.setCalendarDay(Calendar.WEDNESDAY, true);
		rule.setCalendarDay(Calendar.FRIDAY, true);
		rule.setCalendarDay(Calendar.SATURDAY, true);
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Fri - Wed", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Tue, Wed, Fri, Sat, Sun", ruleText.getDays(rule));
	}

	@Test
	public void alternateTriples2() throws Exception {
		Rule rule = new Rule();
		rule.setCalendarDay(Calendar.MONDAY, true);
		rule.setCalendarDay(Calendar.WEDNESDAY, true);
		rule.setCalendarDay(Calendar.THURSDAY, true);
		rule.setCalendarDay(Calendar.FRIDAY, true);
		rule.setCalendarDay(Calendar.SUNDAY, true);
		Assert.assertEquals("Sun - Mon, Wed - Fri", ruleText.getDaysSummary(rule));
		Assert.assertEquals("Mon, Wed, Thu, Fri, Sun", ruleText.getDays(rule));
	}
}
