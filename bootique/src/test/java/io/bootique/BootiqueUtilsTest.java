package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BootiqueUtilsTest {

    @Test
    public void testToArray() {
        assertArrayEquals(new String[]{}, BootiqueUtils.toArray(asList()));
        assertArrayEquals(new String[]{"a", "b", "c"}, BootiqueUtils.toArray(asList("a", "b", "c")));
    }

    @Test
    public void testMergeArrays() {
        assertArrayEquals(new String[]{}, BootiqueUtils.mergeArrays(new String[0], new String[0]));
        assertArrayEquals(new String[]{"a"}, BootiqueUtils.mergeArrays(new String[]{"a"}, new String[0]));
        assertArrayEquals(new String[]{"b"}, BootiqueUtils.mergeArrays(new String[0], new String[]{"b"}));
        assertArrayEquals(new String[]{"b", "c", "d"}, BootiqueUtils.mergeArrays(new String[]{"b", "c"}, new String[]{"d"}));
    }

    @Test
    public void moduleProviderDependencies() {
        final TestModuleProvider1 moduleProvider1 = new TestModuleProvider1();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(moduleProvider1));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider1.class, TestModuleProvider2.class, TestModuleProvider3.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void moduleProviderDependenciesDuplicateProvider() {
        final TestModuleProvider1 moduleProvider1 = new TestModuleProvider1();
        final TestModuleProvider2 moduleProvider2 = new TestModuleProvider2();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(
                moduleProvider1,
                moduleProvider2
        ));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider1.class, TestModuleProvider2.class, TestModuleProvider3.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void moduleProviderDependenciesDuplicateProviders() {
        final TestModuleProvider1 moduleProvider1 = new TestModuleProvider1();
        final TestModuleProvider2 moduleProvider2 = new TestModuleProvider2();
        final TestModuleProvider3 moduleProvider3 = new TestModuleProvider3();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(
                moduleProvider1,
                moduleProvider1,
                moduleProvider2,
                moduleProvider2,
                moduleProvider3,
                moduleProvider3
        ));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider1.class, TestModuleProvider2.class, TestModuleProvider3.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void moduleProviderDependenciesTwoLevels() {
        final TestModuleProvider11 moduleProvider1 = new TestModuleProvider11();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(moduleProvider1));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider11.class, TestModuleProvider22.class, TestModuleProvider33.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void moduleProviderDependenciesTwoLevelsDuplicate() {
        final TestModuleProvider11 moduleProvider1 = new TestModuleProvider11();
        final TestModuleProvider22 moduleProvider2 = new TestModuleProvider22();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(
                moduleProvider1,
                moduleProvider2
        ));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider11.class, TestModuleProvider22.class, TestModuleProvider33.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void moduleProviderDependenciesTwoLevelsDuplicates() {
        final TestModuleProvider11 moduleProvider1 = new TestModuleProvider11();
        final TestModuleProvider22 moduleProvider2 = new TestModuleProvider22();
        final TestModuleProvider33 moduleProvider3 = new TestModuleProvider33();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(
                moduleProvider1,
                moduleProvider1,
                moduleProvider2,
                moduleProvider2,
                moduleProvider3,
                moduleProvider3
        ));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider11.class, TestModuleProvider22.class, TestModuleProvider33.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void moduleProviderDependenciesCircular() {
        final TestModuleProvider111 moduleProvider1 = new TestModuleProvider111();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(moduleProvider1));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider111.class, TestModuleProvider222.class, TestModuleProvider333.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void moduleProviderDependenciesCircularDuplicate() {
        final TestModuleProvider111 moduleProvider1 = new TestModuleProvider111();
        final TestModuleProvider222 moduleProvider2 = new TestModuleProvider222();

        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(
                moduleProvider1,
                moduleProvider2
        ));

        final List<Class<? extends BQModuleProvider>> moduleProviderClasses = bqModuleProviders
                .stream()
                .map(BQModuleProvider::getClass)
                .collect(toList());

        assertThat(moduleProviderClasses, hasItems(TestModuleProvider111.class, TestModuleProvider222.class, TestModuleProvider333.class));
        assertEquals(3, bqModuleProviders.size());
    }

    @Test
    public void anonymousModuleProvider() {
        final Collection<? extends BQModuleProvider> bqModuleProviders = BootiqueUtils.moduleProviderDependencies(of(
                new BQModuleProvider() {
                    @Override
                    public Module module() {
                        return new TestModule1();
                    }

                    @Override
                    public BQModuleId id() {
                        return BQModuleId.of(TestModule1.class);
                    }
                },
                new BQModuleProvider() {
                    @Override
                    public Module module() {
                        return new TestModule2();
                    }

                    @Override
                    public BQModuleId id() {
                        return BQModuleId.of(TestModule2.class);
                    }
                },
                new BQModuleProvider() {
                    @Override
                    public Module module() {
                        return new TestModule3();
                    }

                    @Override
                    public BQModuleId id() {
                        return BQModuleId.of(TestModule3.class);
                    }
                }
        ));

        assertEquals(3, bqModuleProviders.size());
    }

    static class TestModule1 implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }

    static class TestModule2 implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }

    static class TestModule3 implements Module {
        @Override
        public void configure(Binder binder) {
        }
    }

    static class TestModuleProvider1 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule1();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule1.class);
        }

        @Override
        public Collection<Class<? extends BQModuleProvider>> dependencies() {
            return of(TestModuleProvider2.class, TestModuleProvider3.class);
        }
    }

    static class TestModuleProvider2 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule2();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule2.class);
        }
    }

    static class TestModuleProvider3 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule3();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule3.class);
        }
    }

    static class TestModuleProvider11 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule1();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule1.class);
        }

        @Override
        public Collection<Class<? extends BQModuleProvider>> dependencies() {
            return of(TestModuleProvider22.class);
        }
    }

    static class TestModuleProvider22 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule2();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule2.class);
        }

        @Override
        public Collection<Class<? extends BQModuleProvider>> dependencies() {
            return of(TestModuleProvider33.class);
        }
    }

    static class TestModuleProvider33 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule3();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule3.class);
        }
    }

    static class TestModuleProvider111 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule1();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule1.class);
        }

        @Override
        public Collection<Class<? extends BQModuleProvider>> dependencies() {
            return of(TestModuleProvider222.class);
        }
    }

    static class TestModuleProvider222 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule2();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule2.class);
        }

        @Override
        public Collection<Class<? extends BQModuleProvider>> dependencies() {
            return of(TestModuleProvider333.class);
        }
    }

    static class TestModuleProvider333 implements BQModuleProvider {
        @Override
        public Module module() {
            return new TestModule3();
        }

        @Override
        public BQModuleId id() {
            return BQModuleId.of(TestModule3.class);
        }


        @Override
        public Collection<Class<? extends BQModuleProvider>> dependencies() {
            return of(TestModuleProvider111.class);
        }
    }
}




