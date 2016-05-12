package com.nhl.bootique.config.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.bootique.log.BootLogger;

public class MultiFormatJsonNodeParser implements Function<URL, JsonNode> {

	public static enum ParserType {
		YAML, JSON, XML
	}

	private Map<ParserType, Function<InputStream, JsonNode>> parsers;
	private BootLogger bootLogger;

	public MultiFormatJsonNodeParser(Map<ParserType, Function<InputStream, JsonNode>> parsers, BootLogger bootLogger) {
		this.parsers = parsers;
		this.bootLogger = bootLogger;
	}

	@Override
	public JsonNode apply(URL url) {

		URLConnection connection;
		try {
			connection = url.openConnection();
		} catch (IOException e) {
			throw new RuntimeException("Error opening connection to " + url, e);
		}

		ParserType type = parserTypeFromHeaders(connection);

		if (type == null) {
			type = parserTypeFromExtension(url);
		}

		if (type == null) {
			type = ParserType.YAML;
		}

		Function<InputStream, JsonNode> parser = parser(type);

		try (InputStream in = connection.getInputStream();) {
			return parser.apply(in);
		} catch (IOException e) {
			throw new RuntimeException("Error reading URL data " + url, e);
		}
	}

	Function<InputStream, JsonNode> parser(ParserType type) {

		Function<InputStream, JsonNode> parser = parsers.get(type);

		if (parser == null) {
			bootLogger.trace(() -> "No parser for type: " + type);
			throw new IllegalStateException("Can't find configuration parser for the format: " + type);
		}

		return parser;
	}

	ParserType parserTypeFromHeaders(URLConnection connection) {
		String contentType = connection.getHeaderField("Content-Type");
		if (contentType == null) {
			return null;
		}

		switch (contentType) {
		case "application/json":
			bootLogger.trace(() -> "Configuration is in JSON format (based on HTTP content-type)");
			return ParserType.JSON;
		case "application/xml":
			bootLogger.trace(() -> "Configuration is in XML format (based on HTTP content-type)");
			return ParserType.XML;
		default:
			return null;
		}
	}

	ParserType parserTypeFromExtension(URL url) {

		String path = url.getPath();
		if (path == null) {
			return null;
		}

		int dot = path.lastIndexOf('.');
		if (dot < 0 || dot == path.length() - 1) {
			return null;
		}

		switch (path.substring(dot + 1)) {
		case "yml":
		case "yaml":
			bootLogger.trace(() -> "Configuration is in YAML format (based on URL extension)");
			return ParserType.YAML;
		case "json":
			bootLogger.trace(() -> "Configuration is in JSON format (based on URL extension)");
			return ParserType.JSON;
		case "xml":
			bootLogger.trace(() -> "Configuration is in XML format (based on URL extension)");
			return ParserType.XML;
		default:
			return null;
		}
	}
}
