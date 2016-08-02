package com.nhl.bootique.jackson;

import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.nhl.bootique.log.BootLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * @since 0.13
 */
class SubtypeResolverFactory {

    private BootLogger logger;
    private ClassLoader classLoader;
    private Class<?> serviceType;

    SubtypeResolverFactory(ClassLoader classLoader, Class<?> serviceType, BootLogger logger) {
        this.logger = logger;
        this.classLoader = classLoader;
        this.serviceType = serviceType;
    }

    SubtypeResolver createResolver() {

        Collection<Class<?>> subtypes;
        try {
            subtypes = resolveSubclasses();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't load subclasses for type: " + serviceType.getName(), e);
        }

        return new ImmutableSubtypeResolver(subtypes);
    }

    Collection<Class<?>> resolveSubclasses() throws IOException, ClassNotFoundException {

        Collection<Class<?>> subclasses = new ArrayList<>();

        String location = serviceLocation();

        Enumeration<URL> serviceLists = classLoader.getResources(location);
        while (serviceLists.hasMoreElements()) {
            URL url = serviceLists.nextElement();
            appendSubclasses(url, subclasses);
        }

        return subclasses;
    }

    void appendSubclasses(URL url, Collection<Class<?>> subclasses) throws IOException, ClassNotFoundException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {

            String line;
            while ((line = in.readLine()) != null) {
                subclasses.add(loadClass(line.trim()));
            }
        }
    }

    Class<?> loadClass(String className) throws ClassNotFoundException {
        logger.trace(() -> "Loading config subtype: " + className);
        return classLoader.loadClass(className);
    }

    String serviceLocation() {
        return "META-INF/services/" + serviceType.getName();
    }

}
