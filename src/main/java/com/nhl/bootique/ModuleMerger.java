package com.nhl.bootique;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Module;
import com.nhl.bootique.log.BootLogger;

class ModuleMerger {

	private Collection<Module> modules;

	ModuleMerger(Collection<BQModuleProvider> providers, BootLogger bootLogger) {
		Set<ModuleMergeNode> nodes = new HashSet<>();

		// immediately get rid of dupes...
		providers.forEach(p -> {

			ModuleMergeNode n = new ModuleMergeNode(p.module(), p);
			if (!nodes.add(n)) {
				bootLogger.trace(() -> String.format("Skipping module '%s' provided by '%s' - already present...",
						n.getModuleDescription(), n.getProviderDescription()));
			}
		});

		// find modules replaced by other modules...
		Map<Class<? extends Module>, ModuleMergeNode> replacements = nodes.stream()
				.filter(ModuleMergeNode::isReplacement).collect(toMap(n -> n.getReplaces().get(), n -> n));

		// skip any replaced modules...
		this.modules = nodes.stream().filter(n -> {
			ModuleMergeNode replacedBy = replacements.get(n.getModule().getClass());

			if (replacedBy == null) {
				bootLogger.trace(() -> String.format("Adding module '%s' provided by '%s'...", n.getModuleDescription(),
						n.getProviderDescription()));

				return true;
			} else {
				bootLogger.trace(() -> String.format(
						"Skipping module '%s' provided by '%s' - replaced by '%s' provided by '%s'...",
						n.getModuleDescription(), n.getProviderDescription(), replacedBy.getModuleDescription(),
						replacedBy.getProviderDescription()));
				return false;
			}
		}).map(ModuleMergeNode::getModule).collect(toList());

		// TODO: detect replacement graph cycles
		// TDOO: detect two modules replacing the same module..
	}

	Collection<Module> getModules() {
		return modules;
	}

}
