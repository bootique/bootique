package io.bootique.it;

import com.google.inject.Module;
import io.bootique.BQModuleId;
import io.bootique.BQModuleProvider;

public class ItestModuleProvider implements BQModuleProvider {

    @Override
    public Module module() {
        return new ItestModule();
    }

    @Override
    public BQModuleId id() {
        return BQModuleId.of(ItestModule.class);
    }
}
