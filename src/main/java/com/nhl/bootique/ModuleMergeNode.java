package com.nhl.bootique;

import java.util.Optional;

import com.google.inject.Module;

class ModuleMergeNode {

	private Module module;
	private BQModuleProvider providedBy;

	ModuleMergeNode(Module module, BQModuleProvider providedBy) {
		this.module = module;
		this.providedBy = providedBy;
	}

	@Override
	public boolean equals(Object object) {

		if (object == this) {
			return true;
		}

		if (object instanceof ModuleMergeNode) {

			ModuleMergeNode node = (ModuleMergeNode) object;

			// equality by module type...
			return module.getClass().equals(node.module.getClass());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return 37 + module.getClass().hashCode();
	}

	Module getModule() {
		return module;
	}

	String getModuleDescription() {
		return module.getClass().getName();
	}

	String getProviderDescription() {
		return providedBy.getClass().getName();
	}

	Optional<Class<? extends Module>> getReplaces() {
		return providedBy.replaces();
	}

	boolean isReplacement() {
		return getReplaces().isPresent();
	}
}
