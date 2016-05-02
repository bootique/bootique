package com.nhl.bootique.config.jackson;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A helper class to navigate {@link JsonNode} objects.
 */
class PathSegment implements Iterable<PathSegment> {

	private String remainingPath;
	private String incomingPath;
	private JsonNode node;
	private PathSegment parent;

	PathSegment(JsonNode node, String remainingPath) {
		this(node, null, null, remainingPath);
	}

	PathSegment(JsonNode node, PathSegment parent, String incomingPath, String remainingPath) {
		this.node = node;
		this.parent = parent;
		this.incomingPath = incomingPath;

		if (remainingPath != null && remainingPath.endsWith(".")) {
			remainingPath = remainingPath.substring(0, remainingPath.length() - 1);
		}
		this.remainingPath = remainingPath;
	}

	public Optional<PathSegment> lastPathComponent() {
		return StreamSupport.stream(spliterator(), false).reduce((a, b) -> b);
	}

	public JsonNode getNode() {
		return node;
	}
	
	public JsonNode getParentNode() {
		return parent.getNode();
	}
	
	public String getIncomingPath() {
		return incomingPath;
	}

	private PathSegment createNext() {
		if (remainingPath == null || remainingPath.length() == 0) {
			return null;
		}

		int dot = remainingPath.indexOf('.');
		String pre = dot > 0 ? remainingPath.substring(0, dot) : remainingPath;
		String post = dot > 0 ? remainingPath.substring(dot + 1) : "";

		JsonNode child = node != null ? node.get(pre) : null;
		return new PathSegment(child, this, pre, post);
	}

	void fillMissingParents() {
		parent.fillMissingNodes(incomingPath, node, new JsonNodeFactory(true));
	}

	void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory) {

		if (node == null) {
			node = new ObjectNode(nodeFactory);
			parent.fillMissingNodes(incomingPath, node, nodeFactory);
		}

		if (child != null) {
			if (node instanceof ObjectNode) {
				((ObjectNode) node).set(field, child);
			} else {
				throw new IllegalArgumentException(
						"Node '" + incomingPath + "' is unexpected in the middle of the path");
			}
		}
	}

	@Override
	public Iterator<PathSegment> iterator() {
		return new Iterator<PathSegment>() {

			private PathSegment current = PathSegment.this;
			private PathSegment next = current.createNext();

			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public PathSegment next() {

				if (!hasNext()) {
					throw new NoSuchElementException("Past iterator end");
				}

				PathSegment r = current;
				current = next;
				next = current != null ? current.createNext() : null;
				return r;
			}
		};
	}
}