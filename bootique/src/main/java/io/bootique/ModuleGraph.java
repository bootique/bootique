/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class ModuleGraph {

    /**
     * {@link LinkedHashMap} is used for supporting insertion order.
     */
    private final Map<BQModuleMetadata, List<BQModuleMetadata>> neighbors;

    ModuleGraph(int size) {
        neighbors = new LinkedHashMap<>(size);
    }

    /**
     * Add a vertex to the graph. Nothing happens if vertex is already in graph.
     */
    void add(BQModuleMetadata vertex) {
        neighbors.putIfAbsent(vertex, new ArrayList<>(0));
    }

    /**
     * Add an edge to the graph; if either vertex does not exist, it's added.
     * This implementation allows the creation of multi-edges and self-loops.
     */
    void add(BQModuleMetadata from, BQModuleMetadata to) {
        neighbors.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        this.add(to);
    }

    /**
     * Return (as a Map) the in-degree of each vertex.
     */
    private Map<BQModuleMetadata, Integer> inDegree() {
        Map<BQModuleMetadata, Integer> result = new LinkedHashMap<>(neighbors.size());

        neighbors.forEach((from, neighbors) -> {
            neighbors.forEach(to -> result.compute(to, (k, old) -> {
                if(old == null) {
                    return 1;
                }
                if(old > 0) {
                    throw new BootiqueException(1, "Module " + to.getName()
                            + " provided by " + to.getProviderName()
                            + " is overridden twice by " + from.getName());
                }
                return ++old;
            }));
            result.putIfAbsent(from, 0);
        });

        return result;
    }

    /**
     * Return (as a List) the topological sort of the vertices. Throws an exception if cycles are detected.
     */
    List<BQModuleMetadata> topSort() {
        Map<BQModuleMetadata, Integer> degree = inDegree();
        Deque<BQModuleMetadata> zeroDegree = new ArrayDeque<>(neighbors.size());
        List<BQModuleMetadata> result = new ArrayList<>(neighbors.size());

        degree.forEach((k, v) -> {
            if(v == 0) {
                zeroDegree.push(k);
            }
        });

        while (!zeroDegree.isEmpty()) {
            BQModuleMetadata v = zeroDegree.pop();
            result.add(v);

            neighbors.get(v).forEach(neighbor ->
                    degree.computeIfPresent(neighbor, (k, oldValue) -> {
                        int newValue = --oldValue;
                        if(newValue == 0) {
                            zeroDegree.push(k);
                        }
                        return newValue;
                    })
            );
        }

        // Check that we have used the entire graph (if not, there was a cycle)
        if (result.size() != neighbors.size()) {
            Set<BQModuleMetadata> remainingKeys = new HashSet<>(neighbors.keySet());
            String cycleString = remainingKeys.stream()
                    .filter(o -> !result.contains(o))
                    .map(BQModuleMetadata::getName)
                    .collect(Collectors.joining(" -> "));
            throw new BootiqueException(1, "Circular override dependency between DI modules: " + cycleString);
        }

        Collections.reverse(result);
        return result;
    }

    public String traceModuleMessage(BQModuleMetadata module, BQModuleMetadata overriddes) {
        StringBuilder message = new StringBuilder("Loading module '")
                .append(module.getName())
                .append("'");

        String providerName = module.getProviderName();
        boolean hasProvider = providerName != null && providerName.length() > 0;
        if (hasProvider) {
            message.append(" provided by '").append(providerName).append("'");
        }

        if (overriddes != null) {
            if (hasProvider) {
                message.append(",");
            }

            message.append(" overrides '").append(overriddes.getName()).append("'");
        }

        return message.toString();
    }
}
