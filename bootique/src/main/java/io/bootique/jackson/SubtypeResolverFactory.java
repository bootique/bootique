package io.bootique.jackson;

import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import io.bootique.log.BootLogger;

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
public class SubtypeResolverFactory<T> {

    private BootLogger logger;
    private ClassLoader classLoader;
    private Class<T> serviceType;

    public SubtypeResolverFactory(ClassLoader classLoader, Class<T> serviceType, BootLogger logger) {
        this.logger = logger;
        this.classLoader = classLoader;
        this.serviceType = serviceType;
    }

    public SubtypeResolver createResolver() {

        Collection<Class<? extends T>> subtypes;
        try {
            subtypes = resolveSubclasses();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't load subclasses for type: " + serviceType.getName(), e);
        }

        return new ImmutableSubtypeResolver(subtypes);
    }

    public Collection<Class<? extends T>> resolveSubclasses() throws IOException, ClassNotFoundException {

        // note that unlike java.util.ServiceLoader, SubtypeResolverFactory can work with abstract supertypes
        // as they are not instantiated....

        Collection<Class<? extends T>> subclasses = new ArrayList<>();

        String location = serviceLocation();

        Enumeration<URL> serviceLists = classLoader.getResources(location);
        while (serviceLists.hasMoreElements()) {
            URL url = serviceLists.nextElement();
            appendSubclasses(url, subclasses);
        }

        return subclasses;
    }

    void appendSubclasses(URL url, Collection<Class<? extends T>> subclasses) throws IOException, ClassNotFoundException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {

            String line;
            while ((line = in.readLine()) != null) {
                subclasses.add(loadClass(line.trim()));
            }
        }
    }

    Class<? extends T> loadClass(String className) throws ClassNotFoundException {
        logger.trace(() -> "Loading config subtype: " + className);
        return (Class<? extends T>) classLoader.loadClass(className);
    }

    String serviceLocation() {
        return "META-INF/services/" + serviceType.getName();
    }

}
