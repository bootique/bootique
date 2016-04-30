package com.nhl.bootique.config;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.log.BootLogger;

public class CliConfigurationSourceTest {

	static Cli createCli(String... configOptions) {
		Cli cli = mock(Cli.class);

		switch (configOptions.length) {
		case 0:
			when(cli.optionString(CliConfigurationSource.CONFIG_OPTION)).thenReturn(null);
			when(cli.optionStrings(CliConfigurationSource.CONFIG_OPTION)).thenReturn(Collections.emptyList());
			break;
		case 1:
			when(cli.optionString(CliConfigurationSource.CONFIG_OPTION)).thenReturn(configOptions[0]);
			when(cli.optionStrings(CliConfigurationSource.CONFIG_OPTION))
					.thenReturn(Collections.singletonList(configOptions[0]));
			break;
		default:
			when(cli.optionString(CliConfigurationSource.CONFIG_OPTION)).thenThrow(new RuntimeException());
			when(cli.optionStrings(CliConfigurationSource.CONFIG_OPTION)).thenReturn(Arrays.asList(configOptions));
			break;
		}

		return cli;
	}

	static Function<InputStream, String> createConfigReader() {
		return (in) -> {

			StringBuilder string = new StringBuilder();
			byte[] buffer = new byte[1024];
			int read;
			Charset utf8 = Charset.forName("UTF8");

			try {
				while ((read = in.read(buffer, 0, buffer.length)) > 0) {
					string.append(new String(buffer, 0, read, utf8));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return string.toString();
		};
	}

	private BootLogger mockBootLogger;
	private Function<InputStream, String> configReader;

	@Before
	public void before() {
		this.mockBootLogger = mock(BootLogger.class);
		this.configReader = createConfigReader();
	}

	private String fileUrl(String path) {
		try {
			return new File(path).toURI().toURL().toExternalForm();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private String jarEntryUrl(String jarPath, String entryPath) {
		return String.format("jar:%s!/%s", fileUrl(jarPath), entryPath);
	}

	@Test
	public void testGet_File() {
		Cli cli = createCli("src/test/resources/com/nhl/bootique/config/test1.yml");
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(","));
		assertEquals("a: b", config);
	}

	@Test
	public void testGet_FileUrl() {
		String url = fileUrl("src/test/resources/com/nhl/bootique/config/test2.yml");
		Cli cli = createCli(url);
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(","));
		assertEquals("c: d", config);
	}

	@Test
	public void testGet_MultipleFiles1() {
		Cli cli = createCli("src/test/resources/com/nhl/bootique/config/test2.yml",
				"src/test/resources/com/nhl/bootique/config/test1.yml");
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(","));
		assertEquals("c: d,a: b", config);
	}

	@Test
	public void testGet_MultipleFiles2() {

		// change file order compared to testGet_MultipleFiles1
		Cli cli = createCli("src/test/resources/com/nhl/bootique/config/test1.yml",
				"src/test/resources/com/nhl/bootique/config/test2.yml");
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(","));
		assertEquals("a: b,c: d", config);
	}

	@Test
	public void testGet_JarUrl() {
		String url = jarEntryUrl("src/test/resources/com/nhl/bootique/config/test3.jar", "com/foo/test3.yml");
		Cli cli = createCli(url);
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(","));
		assertEquals("e: f", config);
	}

	@Test
	public void testGet_ClasspathUrl() {
		String url = "classpath:com/nhl/bootique/config/test2.yml";
		Cli cli = createCli(url);
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(","));
		assertEquals("c: d", config);
	}

}
