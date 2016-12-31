package io.bootique.jackson;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.log.DefaultBootLogger;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultJacksonServiceIT {

    private TypesFactory<PolymorphicConfiguration> typesFactory;

    @Before
    public void before() {
        typesFactory = new TypesFactory<>(
                getClass().getClassLoader(),
                PolymorphicConfiguration.class,
                new DefaultBootLogger(true));
    }

    protected <T> T readValue(Class<T> type, ObjectMapper mapper, String json) throws IOException {
        JsonParser parser = new JsonFactory().createParser(json);
        JsonNode node = mapper.readTree(parser);
        assertNotNull(node);

        return mapper.readValue(new TreeTraversingParser(node), type);
    }

    @Test
    public void testNewObjectMapper_Inheritance() throws IOException {
        ObjectMapper mapper = new DefaultJacksonService(typesFactory.getTypes()).newObjectMapper();

        Sup1 su1 = readValue(Sup1.class, mapper, "{\"type\":\"sub1\",\"p1\":\"p1111\"}");
        assertTrue(su1 instanceof Sub1);
        assertEquals("p1111", ((Sub1) su1).getP1());

        Sup1 su2 = readValue(Sup1.class, mapper, "{\"type\":\"sub2\",\"p2\":15}");
        assertTrue(su2 instanceof Sub2);
        assertEquals(15, ((Sub2) su2).getP2());

        Sup1 su22 = readValue(Sup1.class, mapper, "{\"p2\":18}");
        assertTrue(su22 instanceof Sub2);
        assertEquals(18, ((Sub2) su22).getP2());


        Sup2 su3 = readValue(Sup2.class, mapper, "{\"type\":\"sub3\",\"p3\":\"pxxxx\"}");
        assertTrue(su3 instanceof Sub3);
        assertEquals("pxxxx", ((Sub3) su3).getP3());

        Sup2 su4 = readValue(Sup2.class, mapper, "{\"type\":\"sub4\",\"p4\":150}");
        assertTrue(su4 instanceof Sub4);
        assertEquals(150, ((Sub4) su4).getP4());
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Sub2.class)
    public static interface Sup1 extends PolymorphicConfiguration {

    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    public static interface Sup2 extends PolymorphicConfiguration {

    }

    @JsonTypeName("sub1")
    public static class Sub1 implements Sup1 {
        private String p1;

        public String getP1() {
            return p1;
        }
    }

    @JsonTypeName("sub2")
    public static class Sub2 implements Sup1 {
        private int p2;

        public int getP2() {
            return p2;
        }
    }

    @JsonTypeName("sub3")
    public static class Sub3 implements Sup2 {
        private String p3;

        public String getP3() {
            return p3;
        }
    }

    @JsonTypeName("sub4")
    public static class Sub4 implements Sup2 {
        private int p4;

        public int getP4() {
            return p4;
        }
    }
}
