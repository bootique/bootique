package com.nhl.bootique;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.inject.Module;
import com.nhl.bootique.log.BootLogger;

class ModuleMerger {

	private Function<Collection<BQModuleProvider>, Collection<ModuleMergeNode>> nodeMapper;
	private Predicate<ModuleMergeNode> skippedNodesFilter;
	private Function<ModuleMergeNode, Module> moduleMapper;

	ModuleMerger(BootLogger bootLogger) {

		this.nodeMapper = (providers) -> {
			Collection<ModuleMergeNode> nodes = providers.stream().map(p -> new ModuleMergeNode(p.module(), p))
					.collect(toList());

			// de-dupe modules and index by module type

			// need a map sorted in the order of node iteration
			Map<Class<? extends Module>, ModuleMergeNode> dedupedNodesByModuleType = nodes.stream()
					.collect(toMap(ModuleMergeNode::getModuleType, n -> n, (u, v) -> {
				v.setDuplicateOf(u);
				return u;
			} , () -> new LinkedHashMap<>()));

			// see who replaces who, and who replaces already replaced modules
			dedupedNodesByModuleType.forEach((k, v) -> {
				v.getReplaces().ifPresent(rt -> {
					ModuleMergeNode replaced = dedupedNodesByModuleType.get(rt);
					if (replaced != null) {

						if (replaced.getReplacedBy() != null) {
							v.setDuplicateReplacementOf(replaced.getReplacedBy());
						} else {
							replaced.setReplacedBy(v);
						}
					}
				});
			});

			return nodes;
		};

		this.skippedNodesFilter = (n) -> {

			if (n.getDuplicateOf() != null) {
				bootLogger.trace(() -> String.format(
						"Skipping module '%s' provided by '%s' (already provided by '%s')...", n.getModuleDescription(),
						n.getProviderDescription(), n.getDuplicateOf().getProviderDescription()));
				return false;
			}

			if (n.getReplacedBy() != null) {

				n.checkReplacementCycles();

				bootLogger.trace(() -> String.format(
						"Skipping module '%s' provided by '%s' (replaced by '%s' provided by '%s')...",
						n.getModuleDescription(), n.getProviderDescription(), n.getReplacedBy().getModuleDescription(),
						n.getReplacedBy().getProviderDescription()));
				return false;
			}

			if (n.getDuplicateReplacementOf() != null) {
				bootLogger.trace(() -> String.format(
						"Skipping module '%s' provided by '%s' (module it replaces is already replaced by '%s' provided by '%s')...",
						n.getModuleDescription(), n.getProviderDescription(),
						n.getDuplicateReplacementOf().getModuleDescription(),
						n.getDuplicateReplacementOf().getProviderDescription()));
				return false;
			}

			return true;
		};

		this.moduleMapper = (n) -> {
			bootLogger.trace(() -> String.format("Adding module '%s' provided by '%s'...", n.getModuleDescription(),
					n.getProviderDescription()));
			return n.getModule();
		};
	}

	Collection<Module> getModules(Collection<BQModuleProvider> providers) {
		return nodeMapper.apply(providers).stream().filter(skippedNodesFilter).map(moduleMapper).collect(toList());
	}
}
