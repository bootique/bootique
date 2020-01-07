package io.bootique.test.junit5;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class BQRuntimeCheckerTest {

    @RegisterExtension
    public static BQTestExtension testExtension = new BQTestExtension();

    @Test
    public void testTestModulesLoaded() {
        final BQRuntime runtime = testExtension.app().createRuntime();
        BQRuntimeChecker.testModulesLoaded(runtime, BQCoreModule.class);
    }

    @Test
    public void testTestModulesNotLoaded() {
        final BQRuntime runtime = testExtension.app().createRuntime();
        Assertions.assertThrows(AssertionError.class,
                () -> BQRuntimeChecker.testModulesLoaded(runtime, NonLoadedModule.class));
    }

    static class NonLoadedModule implements Module {

        @Override
        public void configure(Binder binder) {
        }
    }
}
