package com.nhl.bootique;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import com.google.inject.Module;

class ModuleMergeNode {

	private Module module;
	private BQModuleProvider providedBy;
	private Collection<ModuleMergeNode> overriddenBy;

	ModuleMergeNode(Module module, BQModuleProvider providedBy) {
		this.module = module;
		this.providedBy = providedBy;
		this.overriddenBy = new LinkedHashSet<>();
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

	Collection<ModuleMergeNode> getModuleOverrides(Map<Class<? extends Module>, ModuleMergeNode> nodes) {
		return providedBy.overrides().stream().map(t -> nodes.get(t)).filter(n -> n != null).collect(toList());
	}

	void checkCycles() {
		overriddenBy.forEach(n -> n.checkCycles(this));
	}

	void checkCycles(ModuleMergeNode root) {
		if (root == this) {
			throw new RuntimeException("Circular override dependency: " + getModuleDescription());
		}
		
		overriddenBy.forEach(n -> n.checkCycles(root));
	}

	Class<? extends Module> getModuleType() {
		return module.getClass();
	}

	String getModuleDescription() {
		return module.getClass().getSimpleName();
	}

	String getProviderDescription() {
		return providedBy.name();
	}

	Collection<ModuleMergeNode> getOverriddenBy() {
		return overriddenBy;
	}

	void addOverriddenBy(ModuleMergeNode node) {
		overriddenBy.add(node);
	}
}
