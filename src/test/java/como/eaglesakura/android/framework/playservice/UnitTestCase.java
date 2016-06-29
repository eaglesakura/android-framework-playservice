package como.eaglesakura.android.framework.playservice;


import com.eaglesakura.android.AndroidSupportTestCase;
import com.eaglesakura.android.framework.BuildConfig;

import org.junit.runner.RunWith;

import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, packageName = BuildConfig.APPLICATION_ID, sdk = 23)
public abstract class UnitTestCase extends AndroidSupportTestCase {
}
