package io.bootique;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.ConfigurationSource;
import io.bootique.config.jackson.JsonNodeConfigurationFactory;
import io.bootique.env.Environment;
import io.bootique.jackson.JacksonService;
import io.bootique.log.DefaultBootLogger;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonNodeConfigurationFactoryProviderTest {

    private static ConfigurationFactory factory(String... yaml) {
        return factory(Collections.emptyMap(), yaml);
    }

    private static ConfigurationFactory factory(Map<String, String> propertyOverrides, String... yaml) {
        return factory(propertyOverrides, Collections.emptyMap(), yaml);
    }

    private static ConfigurationFactory factory(Map<String, String> propertyOverrides, Map<String, String> varOverrides,
                                                String... yaml) {
        ConfigurationSource mockSource = mock(ConfigurationSource.class);

        when(mockSource.get()).thenReturn(asList(yaml).stream().map(s -> {

            // need to store YAML on disk to have a URL for it

            try {
                Path dir = Paths.get("target", "junit", "tmp");
                Files.createDirectories(dir);
                Path tmp = Files.createTempFile(dir, "BQConfigurationFactoryProviderTest", ".yml");
                Files.write(tmp, s.getBytes());
                return tmp.toUri().toURL();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        Environment mockEnvironment = mock(Environment.class);
        when(mockEnvironment.frameworkProperties()).thenReturn(propertyOverrides);
        when(mockEnvironment.frameworkVariables()).thenReturn(varOverrides);

        return factory(mockSource, mockEnvironment);
    }

    private static ConfigurationFactory factory(ConfigurationSource mockSource, Environment mockEnvironment) {

        JacksonService mockJackson = mock(JacksonService.class);
        when(mockJackson.newObjectMapper()).thenReturn(new ObjectMapper());

        ConfigurationFactory factory = new JsonNodeConfigurationFactoryProvider(mockSource, mockEnvironment, mockJackson,
                new DefaultBootLogger(true), Collections.emptySet(), Collections.emptySet(), null).get();

        assertNotNull(factory);
        assertTrue(factory instanceof JsonNodeConfigurationFactory);

        return factory;
    }

    @Test
    public void testGet_SingleConfig() {

        Bean1 b1 = factory("s: SS\ni: 55").config(Bean1.class, "");
        assertNotNull(b1);
        assertEquals("SS", b1.s);
        assertEquals(55, b1.i);
    }

    @Test
    public void testGet_MultiConfig() {

        Bean1 b1 = factory("s: SS\ni: 55", "l: 12345\ni: 56").config(Bean1.class, "");
        assertNotNull(b1);
        assertEquals("SS", b1.s);
        assertEquals(56, b1.i);
        assertEquals(12345l, b1.l);
    }

    @Test
    public void testGet_SingleConfig_PropOverride() {

        Map<String, String> overrides = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("s", "SS");
                put("i", "55");
            }
        };

        Bean1 b1 = factory(overrides, "s: replace_me\ni: 5").config(Bean1.class, "");
        assertNotNull(b1);
        assertEquals("SS", b1.s);
        assertEquals(55, b1.i);
    }

    @Test
    public void testGet_SingleConfig_PropOverride_Nested() {

        Map<String, String> overrides = new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;

            {
                put("b1.s", "SS");
                put("b1.i", "55");
            }
        };

        Bean1 b1 = factory(overrides, "b1:\n  s: replace_me\n  i: 6").config(Bean1.class, "b1");
        assertNotNull(b1);
        assertEquals("SS", b1.s);
        assertEquals(55, b1.i);
    }

    @Test
    public void testGet_SingleConfig_PropOverride_VarOverride_Nested() {

        Map<String, String> propOverrides = new HashMap<String, String>() {

            {
                put("b1.s", "SS");
                put("b1.i", "55");
            }
        };

        Map<String, String> varOverrides = new HashMap<String, String>() {

            {
                put("B1_I", "58");
            }
        };

        Bean1 b1 = factory(propOverrides, varOverrides, "b1:\n  s: replace_me\n  i: 6").config(Bean1.class, "b1");
        assertNotNull(b1);
        assertEquals("SS", b1.s);
        assertEquals(58, b1.i);
    }

    public static class Bean1 {

        private String s;
        private int i;
        private long l;

        public String getS() {
            return s;
        }

        public int getI() {
            return i;
        }

        public long getL() {
            return l;
        }

    }
}
