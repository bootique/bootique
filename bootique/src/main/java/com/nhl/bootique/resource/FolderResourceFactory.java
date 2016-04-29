package com.nhl.bootique.resource;

import java.util.Objects;

/**
 * A {@link ResourceFactory} that corresponds to a "folder". Cleans up the
 * resource ID to correct missing trailing slashes.
 */
public class FolderResourceFactory extends ResourceFactory {

	static String normalizeResourceId(String resourceId) {

		// folder resources must end with a slash. Otherwise relative URLs won't
		// resolve properly

		// TODO: dealing with empty resourceIds...

		return resourceId.length() > 0 && !resourceId.equals(CLASSPATH_URL_PREFIX) && !resourceId.endsWith("/")
				? resourceId + "/" : resourceId;
	}

	public FolderResourceFactory(String resourceId) {
		super(normalizeResourceId(Objects.requireNonNull(resourceId)));
	}

	@Override
	public String toString() {
		return "FolderResourceFactory:" + resourceId;
	}
}
