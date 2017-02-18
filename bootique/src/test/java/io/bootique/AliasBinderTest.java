package io.bootique;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

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
        AliasBinder ab = new AliasBinder(mapBinder, "BQ_NAME1");
        ab.as("MY_NAME");

        verify(mapBinder).addBinding("BQ_NAME1");
        verify(aliasBinder).toInstance("MY_NAME");
    }

    @Test
    public void testAsIs() {
        AliasBinder ab = new AliasBinder(mapBinder, "BQ_NAME2");
        ab.asIs();

        verify(mapBinder).addBinding("BQ_NAME2");
        verify(aliasBinder).toInstance("BQ_NAME2");
    }
}
