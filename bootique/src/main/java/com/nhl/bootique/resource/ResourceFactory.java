package com.nhl.bootique.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * A value object representing a resource URL. Supports 3 common resource
 * representations:
 * 
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

	private static final String CLASSPATH_URL_PREFIX = "classpath:";

	private String resourceId;

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
	 * @param resourceId
	 *            a String identifier of the resource.
	 */
	public ResourceFactory(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Creates a new input stream to read the resource contents. It is caller's
	 * responsibility to close the obtained stream.
	 * 
	 * @return a new input stream to read the resource contents.
	 */
	public InputStream openStream() {
		try {
			return resourceAsUrl().openStream();
		} catch (IOException e) {
			throw new RuntimeException("Error opening stream for resource: " + resourceId, e);
		}
	}

	protected URL resourceAsUrl() throws MalformedURLException {

		// resourceId can be either a file path or a URL or a classpath: URL

		if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {

			String path = resourceId.substring(CLASSPATH_URL_PREFIX.length());

			// classpath URLs must not start with a slash. This does not work
			// with ClassLoader.
			if (path.length() > 0 && path.charAt(0) == '/') {
				throw new MalformedURLException(
						CLASSPATH_URL_PREFIX + " URLs must not start with a slash: " + resourceId);
			}

			URL cpUrl = ResourceFactory.class.getClassLoader().getResource(path);

			if (cpUrl == null) {
				throw new NullPointerException("Classpath URL not found: " + resourceId);
			}

			return cpUrl;
		}

		URI uri = URI.create(resourceId);
		return uri.isAbsolute() ? uri.toURL() : new File(resourceId).toURI().toURL();
	}

}
