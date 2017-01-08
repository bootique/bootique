package io.bootique.help.config;

import io.bootique.Bootique;
import io.bootique.help.ConsoleAppender;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ConfigSectionGeneratorTest {

    private static void assertLines(ConfigMetadataNode node, String... expectedLines) {

        StringBuilder expected = new StringBuilder();
        for (String s : expectedLines) {
            expected.append(s).append(DefaultConfigHelpGeneratorTest.NEWLINE);
        }

        StringBuilder buffer = new StringBuilder();
        ConsoleAppender out = new ConsoleAppender(buffer, 80);
        node.accept(new ConfigSectionGenerator(out));
        String help = buffer.toString();
        assertNotNull(help);
        assertEquals(expected.toString(), help);
    }

    @Test
    public void testVisitObjectConfig() {

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigValueMetadata.builder("p2").type(Integer.TYPE).description("Designates an integer value").build())
                .addProperty(ConfigValueMetadata.builder("p1").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p0").type(Boolean.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Bootique.class).build())
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      p0: <true|false>",
                "",
                "      p1: <string>",
                "",
                "      # Designates an integer value",
                "      p2: <int>",
                "",
                "      # Type: io.bootique.Bootique",
                "      p3: <value>"
        );
    }

    @Test
    public void testVisitListOfValues() {

        ConfigValueMetadata listMd1 = ConfigValueMetadata.builder().type(Integer.TYPE).build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigListMetadata.builder("p1").elementType(listMd1).build())
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: List",
                "      p1:",
                "            - <int>"
        );
    }

    @Test
    public void testVisitListOfObjects() {

        ConfigObjectMetadata listMd2 = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigListMetadata.builder("p2").elementType(listMd2).description("I am a list").build())
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: List",
                "      # I am a list",
                "      p2:",
                "            - # Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "                  p3: <true|false>",
                "",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitMapOfValues() throws NoSuchFieldException {

        Type genericMapType = ConfigRoot2.class.getField("map").getGenericType();

        ConfigValueMetadata mapValueMd = ConfigValueMetadata.builder().type(String.class).build();
        ConfigMapMetadata mapMd = ConfigMapMetadata.builder("p1")
                .type(genericMapType)
                .keysType(Integer.class)
                .valuesType(mapValueMd).build();

        ConfigObjectMetadata rootMd = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(mapMd)
                .build();

        assertLines(rootMd,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: Map<int, String>",
                "      p1:",
                "            <int>: <string>"
        );
    }

    @Test
    public void testVisitObjectWithMapOfObjects() {

        ConfigObjectMetadata mapMd = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigObjectMetadata rootMd = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigMapMetadata.builder("p1").keysType(String.class).valuesType(mapMd).build())
                .build();

        assertLines(rootMd,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: Map",
                "      p1:",
                "            # Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "            <string>:",
                "                  p3: <true|false>",
                "",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitMapOfMapsOfObjects() {

        ConfigObjectMetadata objectMd = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigMapMetadata subMapMd = ConfigMapMetadata
                .builder()
                .description("Submap description")
                .keysType(String.class)
                .valuesType(objectMd)
                .build();

        ConfigMapMetadata rootMapMd = ConfigMapMetadata
                .builder("root")
                .description("Root map")
                .keysType(String.class)
                .valuesType(subMapMd)
                .build();

        assertLines(rootMapMd,
                "# Type: Map",
                "# Root map",
                "root:",
                "      # Type: Map",
                "      # Submap description",
                "      <string>:",
                "            # Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "            <string>:",
                "                  p3: <true|false>",
                "",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitMapOfListsOfObjects() {

        ConfigObjectMetadata objectMd = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigListMetadata subListMd = ConfigListMetadata
                .builder()
                .description("Sublist description")
                .elementType(objectMd)
                .build();

        ConfigMapMetadata rootMapMd = ConfigMapMetadata
                .builder("root")
                .description("Root map")
                .keysType(String.class)
                .valuesType(subListMd)
                .build();

        assertLines(rootMapMd,
                "# Type: Map",
                "# Root map",
                "root:",
                "      # Type: List",
                "      # Sublist description",
                "      <string>:",
                "            - # Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "                  p3: <true|false>",
                "",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitObjectConfig_Inheritance() {

        ConfigObjectMetadata sub1 = ConfigObjectMetadata.builder()
                .type(Config3.class)
                .typeLabel("c3")
                .description("Subtype desc")
                .addProperty(ConfigValueMetadata.builder("p0").type(Boolean.class).build())
                .addProperty(ConfigValueMetadata.builder("p1").type(String.class).build())
                .build();

        ConfigObjectMetadata sub2 = ConfigObjectMetadata.builder()
                .type(Config4.class)
                .typeLabel("c4")
                .addProperty(ConfigValueMetadata.builder("p2").type(Integer.TYPE).description("Designates an integer value").build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Bootique.class).build())
                .build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .abstractType(true)
                .addProperty(ConfigValueMetadata.builder("pa1").type(Integer.TYPE).build())
                .addSubConfig(sub1)
                .addSubConfig(sub2)
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      #",
                "      # Subtype: io.bootique.help.config.ConfigSectionGeneratorTest$Config3",
                "      # Subtype desc",
                "      #",
                "",
                "      # Subtype identifier.",
                "      type: 'c3'",
                "",
                "      p0: <true|false>",
                "",
                "      p1: <string>",
                "",
                "      #",
                "      # Subtype: io.bootique.help.config.ConfigSectionGeneratorTest$Config4",
                "      #",
                "",
                "      # Subtype identifier.",
                "      type: 'c4'",
                "",
                "      # Designates an integer value",
                "      p2: <int>",
                "",
                "      # Type: io.bootique.Bootique",
                "      p3: <value>"
        );
    }

    @Test
    public void testVisitMapConfig_ValueInheritance() throws NoSuchFieldException {

        Type genericMapType = ConfigRoot2.class.getField("mapOfRoot1").getGenericType();

        ConfigObjectMetadata sub1 = ConfigObjectMetadata.builder()
                .type(Config3.class)
                .typeLabel("c3")
                .addProperty(ConfigValueMetadata.builder("p0").type(Boolean.class).build())
                .addProperty(ConfigValueMetadata.builder("p1").type(String.class).build())
                .build();

        ConfigObjectMetadata sub2 = ConfigObjectMetadata.builder()
                .type(Config4.class)
                .typeLabel("c4")
                .addProperty(ConfigValueMetadata.builder("p2").type(Integer.TYPE).description("Designates an integer value").build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Bootique.class).build())
                .build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder()
                .description("One config")
                .type(ConfigRoot1.class)
                .abstractType(true)
                .addProperty(ConfigValueMetadata.builder("pa1").type(Integer.TYPE).build())
                .addSubConfig(sub1)
                .addSubConfig(sub2)
                .build();

        ConfigMapMetadata mapMd = ConfigMapMetadata.builder("root")
                .description("Map root")
                .type(genericMapType)
                .keysType(String.class)
                .valuesType(m1Config).build();

        assertLines(mapMd,
                "# Type: Map<String, io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1>",
                "# Map root",
                "root:",
                "      # Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "      # One config",
                "      <string>:",
                "            #",
                "            # Subtype: io.bootique.help.config.ConfigSectionGeneratorTest$Config3",
                "            #",
                "",
                "            # Subtype identifier.",
                "            type: 'c3'",
                "",
                "            p0: <true|false>",
                "",
                "            p1: <string>",
                "",
                "            #",
                "            # Subtype: io.bootique.help.config.ConfigSectionGeneratorTest$Config4",
                "            #",
                "",
                "            # Subtype identifier.",
                "            type: 'c4'",
                "",
                "            # Designates an integer value",
                "            p2: <int>",
                "",
                "            # Type: io.bootique.Bootique",
                "            p3: <value>"
        );
    }

    @Test
    public void testTypeLabel() throws NoSuchFieldException {
        ConfigSectionGenerator generator = new ConfigSectionGenerator(mock(ConsoleAppender.class));
        assertEquals("int", generator.typeLabel(Integer.class));
        assertEquals("int", generator.typeLabel(Integer.TYPE));
        assertEquals("boolean", generator.typeLabel(Boolean.class));
        assertEquals("boolean", generator.typeLabel(Boolean.TYPE));
        assertEquals("String", generator.typeLabel(String.class));
        assertEquals("io.bootique.Bootique", generator.typeLabel(Bootique.class));
        assertEquals("Map", generator.typeLabel(HashMap.class));
        assertEquals("List", generator.typeLabel(ArrayList.class));

        Type genericMapType = ConfigRoot2.class.getField("map").getGenericType();
        assertEquals("Map<int, String>", generator.typeLabel(genericMapType));

        Type genericListType = ConfigRoot2.class.getField("list").getGenericType();
        assertEquals("List<String>", generator.typeLabel(genericListType));
    }

    @Test
    public void testSampleValue() throws NoSuchFieldException {
        ConfigSectionGenerator generator = new ConfigSectionGenerator(mock(ConsoleAppender.class));
        assertEquals("<int>", generator.sampleValue(Integer.class));
        assertEquals("<int>", generator.sampleValue(Integer.TYPE));
        assertEquals("<true|false>", generator.sampleValue(Boolean.class));
        assertEquals("<true|false>", generator.sampleValue(Boolean.TYPE));
        assertEquals("<string>", generator.sampleValue(String.class));
        assertEquals("<value>", generator.sampleValue(Bootique.class));
        assertEquals("<value>", generator.sampleValue(HashMap.class));
        assertEquals("<value>", generator.sampleValue(ArrayList.class));
        assertEquals("<a|B|Cd>", generator.sampleValue(E.class));
    }

    public static enum E {
        a, B, Cd
    }

    public static class ConfigRoot1 {

    }

    public static class ConfigRoot2 {

        public Map<Integer, String> map;
        public Map<String, ConfigRoot1> mapOfRoot1;
        public List<String> list;

    }

    public static class Config3 {

    }

    public static class Config4 {

    }
}
