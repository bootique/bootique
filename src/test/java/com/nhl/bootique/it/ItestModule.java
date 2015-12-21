package com.nhl.bootique.it;

import static org.mockito.Mockito.mock;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ItestModule implements Module {

	public static Module MOCK_DELEGATE = mock(Module.class);

	@Override
	public void configure(Binder binder) {
		MOCK_DELEGATE.configure(binder);
	}

}
