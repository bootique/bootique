package com.nhl.bootique;

import java.util.Optional;

import com.google.inject.Module;

class ModuleMergeNode {

	private Module module;
	private BQModuleProvider providedBy;

	private ModuleMergeNode duplicateOf;
	private ModuleMergeNode replacedBy;
	private ModuleMergeNode duplicateReplacementOf;

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

	Class<? extends Module> getModuleType() {
		return module.getClass();
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

	public ModuleMergeNode getDuplicateOf() {
		return duplicateOf;
	}

	public void setDuplicateOf(ModuleMergeNode duplicateOf) {
		this.duplicateOf = duplicateOf;
	}

	public ModuleMergeNode getReplacedBy() {
		return replacedBy;
	}

	public void setReplacedBy(ModuleMergeNode replacedBy) {
		this.replacedBy = replacedBy;
	}

	public ModuleMergeNode getDuplicateReplacementOf() {
		return duplicateReplacementOf;
	}

	public void setDuplicateReplacementOf(ModuleMergeNode duplicateReplacementOf) {
		this.duplicateReplacementOf = duplicateReplacementOf;
	}

}
