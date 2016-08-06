package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.bootique.command.Command;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandMetadata;
import io.bootique.command.DefaultCommandManager;

public class Bootqiue_CommandsIT {

	// "help" command for now
	private static final int STANDARD_COMMANDS_COUNT = 1;

	private Bootique baseChain() {
		return Bootique.app(new String[0]);
	}

	@Test
	public void testCreateInjector_Standard_Plus_Module_Commands() {
		Injector i = baseChain().modules(M0.class, M1.class).createInjector();

		CommandManager inspector = i.getInstance(CommandManager.class);
		assertEquals(2 + STANDARD_COMMANDS_COUNT, inspector.getCommands().size());
		assertTrue(inspector.getCommands().contains(M0.mockCommand));
		assertTrue(inspector.getCommands().contains(M1.mockCommand));
	}

	@Test
	public void testCreateInjector_AppCommands() {
		Injector i = baseChain().modules(M0.class, M1.class).override(BQCoreModule.class).with(M_RemoveCommands.class)
				.createInjector();

		CommandManager inspector = i.getInstance(CommandManager.class);
		assertEquals(0, inspector.getCommands().size());
		assertTrue(inspector.getDefaultCommand() == M_RemoveCommands.mockCommand);
	}

	static class M0 implements Module {

		static final Command mockCommand;

		static {
			mockCommand = mock(Command.class);
			when(mockCommand.getMetadata()).thenReturn(CommandMetadata.builder("m0command").build());
		}

		@Override
		public void configure(Binder binder) {
			BQCoreModule.contributeCommands(binder).addBinding().toInstance(mockCommand);
		}
	}

	static class M1 implements Module {

		static final Command mockCommand;

		static {
			mockCommand = mock(Command.class);
			when(mockCommand.getMetadata()).thenReturn(CommandMetadata.builder("m1command").build());
		}

		@Override
		public void configure(Binder binder) {
			BQCoreModule.contributeCommands(binder).addBinding().toInstance(mockCommand);
		}
	}

	static class M_RemoveCommands implements Module {

		static final Command mockCommand;

		static {
			mockCommand = mock(Command.class);
			when(mockCommand.getMetadata()).thenReturn(CommandMetadata.builder("mo1command").build());
		}

		@Override
		public void configure(Binder binder) {
			binder.bind(CommandManager.class)
					.toInstance(DefaultCommandManager.create(Collections.emptySet(), mockCommand));
		}
	}
}
