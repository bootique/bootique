package io.bootique.resource;

import io.bootique.BootiqueException;
import io.bootique.command.CommandOutcome;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * A value object representing a resource URL. Supports 3 common resource
 * representations:
 * <p>
 * <ul>
 * <li>resource as a URL using protocols recognized by Java (http:, https:,
 * jar:, file:, etc).</li>
 * <li>resource as URL with "classpath:" protocol that allows to identify
 * resources on classpath in a portable manner. E.g. the same URL would identify
 * the resource regardless of whether it is packaged in a jar or resides in a
 * source folder in an IDE.</li>
 * <li>resource as absolute or relative file path.</li>
 * </ul>
 *
 * @since 0.15
 */
public class ResourceFactory {

    protected static final String CLASSPATH_URL_PREFIX = "classpath:";

    protected String resourceId;

    /**
     * Creates a ResourceFactory passing it a String resource identifier. It can
     * be one of
     * <ul>
     * <li>resource as a URL using protocols recognized by Java (http:, https:,
     * jar:, file:, etc).</li>
     * <li>resource as URL with "classpath:" protocol that allows to identify
     * resources on classpath in a portable manner. E.g. the same URL would
     * identify the resource regardless of whether it is packaged in a jar or
     * resides in a source folder in an IDE.</li>
     * <li>resource as absolute or relative file path.</li>
     * </ul>
     *
     * @param resourceId a String identifier of the resource.
     */
    public ResourceFactory(String resourceId) {
        this.resourceId = Objects.requireNonNull(resourceId);
    }

    /**
     * Returns a URL to access resource contents.
     *
     * @return a URL to access resource contents.
     */
    public URL getUrl() {
        return resolveUrl(this.resourceId);
    }

    /**
     * Returns resource ID string used to initialize this ResourceFactory.
     *
     * @return resource ID string used to initialize this ResourceFactory.
     * @since 0.21
     */
    public String getResourceId() {
        return resourceId;
    }

    protected URL resolveUrl(String resourceId) {

        // resourceId can be either a file path or a URL or a classpath: URL

        if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {

            String path = resourceId.substring(CLASSPATH_URL_PREFIX.length());

            // classpath URLs must not start with a slash. This does not work
            // with ClassLoader.
            if (path.length() > 0 && path.charAt(0) == '/') {
                throw new RuntimeException(CLASSPATH_URL_PREFIX + " URLs must not start with a slash: " + resourceId);
            }

            URL cpUrl = ResourceFactory.class.getClassLoader().getResource(path);

            if (cpUrl == null) {
                throw new IllegalArgumentException("Classpath URL not found: " + resourceId);
            }

            return cpUrl;
        }

        URI uri;
        try {
            uri = URI.create(resourceId);
        } catch (IllegalArgumentException e) {
            throw new BootiqueException(CommandOutcome.failed(1, "Invalid config resource url: " + resourceId, e));
        }
        try {
            return uri.isAbsolute() ? uri.toURL() : getCanonicalFile(resourceId).toURI().toURL();
        } catch (IOException e) {
            throw new BootiqueException(CommandOutcome.failed(1, "Invalid config resource url: " + resourceId, e));
        }
    }

    /**
     * Converts resource ID to a canonical file.
     *
     * @return canonical file produced from resource id.
     * @throws IOException
     */
    // using canonical file avoids downstream bugs like this:
    // https://github.com/bootique/bootique-jetty/issues/29
    protected File getCanonicalFile(String resourceId) throws IOException {
        return new File(resourceId).getCanonicalFile();
    }

    @Override
    public String toString() {
        return "ResourceFactory:" + resourceId;
    }

}
