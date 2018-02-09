package io.bootique;

import com.google.inject.Module;

import java.util.Objects;

/**
 * Represent identifier of {@link BQModuleProvider}.
 *
 * In most cases you should use {@link BQModuleId#of(Class)} method:
 *
 * <pre>
 * new BQModuleProvider() {
 *
 *    {@literal @}Override
 *     public Module module() {
 *         return new ModuleName();
 *     }
 *
 *    {@literal @}Override
 *     public BQModuleId id() {
 *         return BQModuleId.of(ModuleName.class);
 *     }
 * }
 * </pre>
 *
 * But in case when you create {@link BQModuleProvider} instances dynamically,
 * and each {@link BQModuleProvider} provide different instance of same module class
 * you should use {@link BQModuleId#of(Module)} instead.
 *
 * @author Ibragimov Ruslan
 * @since 0.25
 */
public interface BQModuleId {
    static BQModuleId of(Class<? extends Module> module) {
        return new ClassBQModuleId(module);
    }

    static BQModuleId of(Module module) {
        return new InstanceBQModuleId(module);
    }
}

/**
 * Most of time you should this class to represent module id.
 *
 * @author Ibragimov Ruslan
 * @since 0.25
 */
class ClassBQModuleId implements BQModuleId {

    private final Class<? extends Module> clazz;

    ClassBQModuleId(Class<? extends Module> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassBQModuleId that = (ClassBQModuleId) o;
        return Objects.equals(clazz, that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz);
    }
}

/**
 * In case if module can have multiple instances,
 * we can't check it equality but class, so we will use concrete instances as id.
 *
 * @author Ibragimov Ruslan
 * @since 0.25
 */
class InstanceBQModuleId implements BQModuleId {
    private final Module module;

    InstanceBQModuleId(Module module) {
        this.module = module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceBQModuleId that = (InstanceBQModuleId) o;
        return Objects.equals(module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module);
    }
}
