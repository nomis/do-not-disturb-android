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
package uk.me.sa.android.do_not_disturb.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ReflectionHelpers;
import org.robolectric.shadows.ShadowPowerManager;
import org.robolectric.shadows.ShadowPreferenceManager;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ServiceController;

import android.content.SharedPreferences;
import android.os.Build;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
@PrepareForTest(fullyQualifiedNames = { "uk.me.sa.android.do_not_disturb.service.InterruptionFilterListener",
		"uk.me.sa.android.do_not_disturb.service.InterruptionFilterListener_" })
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
public class TestInterruptionFilterListener {
	SharedPreferences sharedPreferences;
	ServiceController<InterruptionFilterListener> controller;
	InterruptionFilterListener service;

	@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	@Rule
	public PowerMockRule rule = new PowerMockRule();

	@Before
	public void create() throws Exception {
		MockitoAnnotations.initMocks(this);

		ReflectionHelpers.setStaticFieldReflectively(Build.VERSION.class, "SDK_INT", 18);

		ShadowToast.reset();
		ShadowPowerManager.reset();
		sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
		controller = Robolectric.buildService(InterruptionFilterListener.class);
		service = PowerMockito.spy(controller.create().bind().get());
	}

	@Test
	public void placeholder() {

	}

	@After
	public void destroy() {
		if (controller != null)
			controller.unbind().destroy();
	}
}
