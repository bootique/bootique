package io.bootique.help;

import io.bootique.application.OptionMetadata;

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

    public void add(OptionMetadata option) {
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

            // suppress short options if 2 or more long options resolve into a single short opt.
            // although if one of those options is 1 char long, it can be exposed as a short option...
            if (list.size() > 1) {

                boolean[] shortCounter = new boolean[1];

                options = options.map(o -> {

                    if (o.isLongNameAllowed()) {
                        o.setShortNameAllowed(false);
                    } else if (shortCounter[0]) {
                        throw new IllegalStateException("Conflicting short option name: " + o.getShortName());
                    }
                    else {
                        shortCounter[0] = true;
                    }

                    return o;
                });
            }

            return options;
        }).sorted().collect(Collectors.toList());
    }
}
