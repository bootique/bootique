package com.nhl.bootique.config;

/**
 * A tag interface that allows Bootique to resolve configuration subclasses of
 * the implementing superclass or interface. The actual resolution mechanism is
 * based on java ServiceLoader mechanism. Subclasses should be declared in
 * {@code META-INF/services/com.nhl.bootique.config.PolymorphicConfiguration}.
 * 
 * @since 0.13
 */
public interface PolymorphicConfiguration {

}
