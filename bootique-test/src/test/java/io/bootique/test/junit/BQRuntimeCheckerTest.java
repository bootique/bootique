package io.bootique.test.junit;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import org.junit.Rule;
import org.junit.Test;

public class BQRuntimeCheckerTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testTestModulesLoaded() {
        final BQRuntime runtime = testFactory.app().createRuntime();
        BQRuntimeChecker.testModulesLoaded(runtime, BQCoreModule.class);
    }

    @Test(expected = AssertionError.class)
    public void testTestModulesNotLoaded() {
        final BQRuntime runtime = testFactory.app().createRuntime();
        BQRuntimeChecker.testModulesLoaded(runtime, NonLoadedModule.class);
    }
}


class NonLoadedModule implements Module {

    @Override
    public void configure(Binder binder) {
    }
}
