package io.bootique;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AliasBinderTest {

    private MapBinder<String, String> mapBinder;
    private LinkedBindingBuilder<String> aliasBinder;

    @Before
    public void before() {

        aliasBinder = mock(LinkedBindingBuilder.class);
        mapBinder = mock(MapBinder.class);

        when(mapBinder.addBinding(Matchers.any(String.class))).thenReturn(aliasBinder);
    }

    @Test
    public void testAs() {
        AliasBinder ab = new AliasBinder(mapBinder, "name1");
        ab.as("MY_NAME");

        verify(mapBinder).addBinding("MY_NAME");
        verify(aliasBinder).toInstance("name1");
    }

    @Test
    public void testAsIs() {
        AliasBinder ab = new AliasBinder(mapBinder, "naMe2");
        ab.asIs();

        verify(mapBinder).addBinding("BQ_NAME2");
        verify(aliasBinder).toInstance("naMe2");
    }

    @Test
    public void testGetCanonicalVariableName() {
        assertEquals("BQ_NAME", new AliasBinder(mapBinder, "name").getCanonicalVariableName());
        assertEquals("BQ_NAME_NAME2", new AliasBinder(mapBinder, "name.name2").getCanonicalVariableName());
        assertEquals("BQ_NAME_NAME2_", new AliasBinder(mapBinder, "name.name2.").getCanonicalVariableName());
        assertEquals("BQ_NAME__NAME2", new AliasBinder(mapBinder, "name..name2").getCanonicalVariableName());
        assertEquals("BQ__NAME", new AliasBinder(mapBinder, ".name").getCanonicalVariableName());
    }
}
