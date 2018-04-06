package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.junit.Test;

import java.util.HashSet;

import static com.google.common.collect.ImmutableList.of;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Ibragimov Ruslan
 * @since 0.25
 */
public class BQModuleIdTest {

    @Test
    public void sameModuleClassEquals() {
        final HashSet<BQModuleId> moduleIds = new HashSet<>(of(BQModuleId.of(TM1.class), BQModuleId.of(TM1.class)));

        assertThat(moduleIds, hasItem(BQModuleId.of(TM1.class)));
        assertEquals(1, moduleIds.size());
    }

    @Test
    public void sameModuleInstanceEquals() {
        Module module = new TM1();

        final HashSet<BQModuleId> moduleIds = new HashSet<>(of(BQModuleId.of(module), BQModuleId.of(module)));

        assertThat(moduleIds, hasItem(BQModuleId.of(module)));
        assertEquals(1, moduleIds.size());
    }

    @Test
    public void sameModuleClassAndInstanceNotEquals() {
        Module module = new TM1();

        final HashSet<BQModuleId> moduleIds = new HashSet<>(of(BQModuleId.of(TM1.class), BQModuleId.of(module)));

        assertThat(moduleIds, hasItem(BQModuleId.of(module)));
        assertThat(moduleIds, hasItem(BQModuleId.of(TM1.class)));
        assertEquals(2, moduleIds.size());
    }

    @Test
    public void sameDifferentModuleClassesNotEquals() {
        final HashSet<BQModuleId> moduleIds = new HashSet<>(of(BQModuleId.of(TM1.class), BQModuleId.of(TM2.class)));

        assertThat(moduleIds, hasItem(BQModuleId.of(TM2.class)));
        assertThat(moduleIds, hasItem(BQModuleId.of(TM1.class)));
        assertEquals(2, moduleIds.size());
    }
    @Test
    public void sameDifferentInstancesNotEquals() {
        Module module1 = new TM1();
        Module module2 = new TM1();

        final HashSet<BQModuleId> moduleIds = new HashSet<>(of(BQModuleId.of(module1), BQModuleId.of(module2)));

        assertThat(moduleIds, hasItem(BQModuleId.of(module1)));
        assertThat(moduleIds, hasItem(BQModuleId.of(module2)));
        assertEquals(2, moduleIds.size());
    }

    static class TM1 implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }

    static class TM2 implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }



}
