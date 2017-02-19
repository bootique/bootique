package io.bootique;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.bootique.annotation.EnvironmentProperties;
import io.bootique.meta.application.OptionMetadata;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BQCoreModuleExtenderTest {

    @Test
    public void testSetProperties() {

        Injector i = Guice.createInjector(b -> {

            BQCoreModule.extend(b).setProperty("a", "b").setProperty("c", "d");
            b.bind(MapInspector.class);
        });

        MapInspector inspector = i.getInstance(MapInspector.class);

        assertEquals("b", inspector.map.get("a"));
        assertEquals("d", inspector.map.get("c"));
    }

    @Test
    public void testSetOptions() {
        OptionMetadata o1 = OptionMetadata.builder("o1").build();
        OptionMetadata o2 = OptionMetadata.builder("o2").build();

        Injector i = Guice.createInjector(b -> {
            BQCoreModule.extend(b).addOptions(o1, o2);

            b.bind(OptionsInspector.class);
        });

        OptionsInspector inspector = i.getInstance(OptionsInspector.class);
        assertEquals(2, inspector.options.size());
        assertTrue(inspector.options.contains(o1));
        assertTrue(inspector.options.contains(o2));
    }

    static class MapInspector {

        Map<String, String> map;

        @Inject
        public MapInspector(@EnvironmentProperties Map<String, String> map) {
            this.map = map;
        }
    }

    static class OptionsInspector {
        Set<OptionMetadata> options;

        @Inject
        public OptionsInspector(Set<OptionMetadata> options) {
            this.options = options;
        }
    }
}
