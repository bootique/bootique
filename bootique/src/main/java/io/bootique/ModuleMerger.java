package io.bootique;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.bootique.log.BootLogger;

class ModuleMerger {

	private BootLogger bootLogger;

	ModuleMerger(BootLogger bootLogger) {
		this.bootLogger = bootLogger;
	}

	List<Module> getModules(Collection<BQModuleProvider> providers) {
		Map<Class<? extends Module>, ModuleMergeNode> nodes = toNodeMap(providers);

		calcOverrides(nodes);
		calcCycles(nodes);

		return toModules(nodes);
	}

	private Map<Class<? extends Module>, ModuleMergeNode> toNodeMap(Collection<BQModuleProvider> providers) {

		Map<Class<? extends Module>, ModuleMergeNode> nodes = new LinkedHashMap<>();

		providers.forEach(p -> {

			ModuleMergeNode n = new ModuleMergeNode(p.module(), p);
			ModuleMergeNode existing = nodes.putIfAbsent(n.getModuleType(), n);
			if (existing != null) {
				bootLogger.trace(() -> String.format(
						"Skipping module '%s' provided by '%s' (already provided by '%s')...", n.getModuleDescription(),
						n.getProviderDescription(), existing.getProviderDescription()));
			}
		});

		return nodes;
	}

	private void calcOverrides(Map<Class<? extends Module>, ModuleMergeNode> nodes) {
		nodes.values().forEach((n) -> {
			n.getModuleOverrides(nodes).forEach(o -> o.addOverriddenBy(n));
		});
	}

	private void calcCycles(Map<Class<? extends Module>, ModuleMergeNode> nodes) {
		nodes.values().forEach((n) -> {
			n.checkCycles();
		});
	}

	private List<Module> toModules(Map<Class<? extends Module>, ModuleMergeNode> nodes) {

		Predicate<ModuleMergeNode> notOverridden = n -> {
			if (n.getOverriddenBy().isEmpty()) {
				return true;
			}

			if (n.getOverriddenBy().size() == 1) {
				return false;
			}

			String overrideList = n.getOverriddenBy().stream().map(ModuleMergeNode::getModuleDescription)
					.collect(joining(", "));
			String message = String.format(
					"Module %s provided by %s is overridden more then once. Overriding modules: %s",
					n.getModuleDescription(), n.getProviderDescription(), overrideList);
			throw new RuntimeException(message);
		};

		Function<ModuleMergeNode, Module> toModule = (n) -> {

			bootLogger.trace(() -> String.format("Adding module '%s' provided by '%s'...", n.getModuleDescription(),
					n.getProviderDescription()));

			Collection<ModuleMergeNode> overrides = n.getModuleOverrides(nodes);

			if (overrides.isEmpty()) {
				return n.getModule();
			}

			Collection<Module> overrideModules = overrides.stream().map(o -> {
				bootLogger.trace(() -> String.format("Will override %s provided by %s...", o.getModuleDescription(),
						o.getProviderDescription()));
				return o.getModule();
			}).collect(toList());

			return Modules.override(overrideModules).with(n.getModule());

		};

		return nodes.values().stream().filter(notOverridden).map(toModule).collect(toList());
	}
}
