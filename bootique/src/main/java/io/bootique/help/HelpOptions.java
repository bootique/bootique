package io.bootique.help;

import io.bootique.cli.meta.CliOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class to collect CLI options from various models, sort them and resolve conflicts before inlcuding them in help.
 *
 * @since 0.20
 */
public class HelpOptions {

    private Map<String, List<HelpOption>> byShortName;

    public HelpOptions() {
        this.byShortName = new HashMap<>();
    }

    public void add(CliOption option) {
        HelpOption ho = new HelpOption(option);
        byShortName.computeIfAbsent(ho.getShortName(), sn -> new ArrayList<HelpOption>()).add(ho);
    }

    /**
     * Returns sorted options with resolved conflicting names.
     *
     * @return sorted options with resolved conflicting names.
     */
    public List<HelpOption> getOptions() {

        return byShortName.values().stream().flatMap(list -> {

            Stream<HelpOption> options = list.stream();

            // suppress short options if 2 or more long options resolve into a single short opt=
            if (list.size() > 1) {

                options = options.map(o -> {
                    o.setShortNameAllowed(false);
                    return o;
                });
            }

            return options;
        }).sorted().collect(Collectors.toList());
    }
}
