package io.bootique;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import io.bootique.env.DeclaredVariable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DeclaredVariableBinderTest {

    private Multibinder<DeclaredVariable> varBinder1;
    private LinkedBindingBuilder<DeclaredVariable> varBinder2;
    private ArgumentCaptor<DeclaredVariable> varCaptor;

    @Before
    public void before() {

        varCaptor = ArgumentCaptor.forClass(DeclaredVariable.class);
        varBinder1 = mock(Multibinder.class);
        varBinder2 = mock(LinkedBindingBuilder.class);

        when(varBinder1.addBinding()).thenReturn(varBinder2);
    }

    @Test
    public void testWithName() {
        DeclaredVariableBinder ab = new DeclaredVariableBinder(varBinder1, "name1");
        ab.withName("MY_NAME");

        verify(varBinder1).addBinding();
        verify(varBinder2).toInstance(varCaptor.capture());

        DeclaredVariable v = varCaptor.getValue();
        assertEquals("MY_NAME", v.getName());
        assertEquals("BQ_NAME1", v.getCanonicalName());
        assertEquals("name1", v.getConfigPath());
    }

    @Test
    public void testWithCanonicalName() {
        DeclaredVariableBinder ab = new DeclaredVariableBinder(varBinder1, "naMe2");
        ab.withCanonicalName();

        verify(varBinder1).addBinding();
        verify(varBinder2).toInstance(varCaptor.capture());

        DeclaredVariable v = varCaptor.getValue();
        assertEquals("BQ_NAME2", v.getName());
        assertEquals("BQ_NAME2", v.getCanonicalName());
        assertEquals("naMe2", v.getConfigPath());
    }

    @Test
    public void testGetCanonicalVariableName() {
        assertEquals("BQ_NAME", new DeclaredVariableBinder(varBinder1, "name").getCanonicalVariableName());
        assertEquals("BQ_NAME_NAME2", new DeclaredVariableBinder(varBinder1, "name.name2").getCanonicalVariableName());
        assertEquals("BQ_NAME_NAME2_", new DeclaredVariableBinder(varBinder1, "name.name2.").getCanonicalVariableName());
        assertEquals("BQ_NAME__NAME2", new DeclaredVariableBinder(varBinder1, "name..name2").getCanonicalVariableName());
        assertEquals("BQ__NAME", new DeclaredVariableBinder(varBinder1, ".name").getCanonicalVariableName());
    }
}
