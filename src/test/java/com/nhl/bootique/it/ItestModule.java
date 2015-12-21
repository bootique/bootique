package com.nhl.bootique.it;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ItestModule implements Module {

	public static Module MOCK_DELEGATE;

	@Override
	public void configure(Binder binder) {
		if (MOCK_DELEGATE != null) {
			MOCK_DELEGATE.configure(binder);
		}
	}

}
