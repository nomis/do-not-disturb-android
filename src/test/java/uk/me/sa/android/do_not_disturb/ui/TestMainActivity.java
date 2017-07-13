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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.provider.Settings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
@PrepareForTest(fullyQualifiedNames = { "uk.me.sa.android.do_not_disturb.ui.MainActivity" })
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@SuppressFBWarnings({ "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", "UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR" })
public class TestMainActivity {
	ActivityController<MainActivity_> controller;
	MainActivity_ activity;

	@Rule
	public PowerMockRule rule = new PowerMockRule();

	@Before
	public void create() throws Exception {
		MockitoAnnotations.initMocks(this);
		ShadowToast.reset();
		controller = Robolectric.buildActivity(MainActivity_.class);
		activity = controller.create().start().resume().visible().get();
	}

	@After
	public void destroy() {
		if (controller != null)
			controller.pause().stop().destroy();
	}

	@Test
	public void openNotificationAccess() throws Exception {
		activity.openNotificationAccess();

		ShadowActivity shadowActivity = Robolectric.shadowOf_(activity);
		Intent startedIntent = shadowActivity.getNextStartedActivity();
		ShadowIntent shadowIntent = Robolectric.shadowOf_(startedIntent);
		assertEquals(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, shadowIntent.getAction());
		assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, shadowIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK);
	}
}
