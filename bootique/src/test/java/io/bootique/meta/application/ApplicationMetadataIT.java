package io.bootique.meta.application;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApplicationMetadataIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testDefault() {
        BQRuntime runtime = runtimeFactory.app().createRuntime();

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        // we really don't know what the generated name is. It varies depending on the unit test execution environment
        assertNotNull(md.getName());
        assertNull(md.getDescription());
        assertEquals(2, md.getCommands().size());
        assertEquals(1, md.getOptions().size());
    }

    @Test
    public void testCustomDescription() {
        BQRuntime runtime = runtimeFactory.app()
                .module(b -> BQCoreModule.contribute(b).setApplicationDescription("app desc"))
                .createRuntime();

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        // we really don't know what the generated name is. It varies depending on the unit test execution environment
        assertNotNull(md.getName());
        assertEquals("app desc", md.getDescription());
        assertEquals(2, md.getCommands().size());
        assertEquals(1, md.getOptions().size());
    }
}
