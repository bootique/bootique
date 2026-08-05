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

package io.bootique.config.jackson.merger;

import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.config.jackson.path.IndexPathSegment;
import io.bootique.config.jackson.path.PathSegment;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

/**
 * Overrides JsonNode object values from a map of properties.
 */
public class InPlacePropertiesMerger implements Function<JsonNode, JsonNode> {

    // comparator that takes into account numeric array indices (e.g. "a[100]" should go after "a[2]")
    static final Comparator<String> PATH_ORDER = InPlacePropertiesMerger::comparePaths;

    private final Map<String, String> properties;

    public InPlacePropertiesMerger(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public JsonNode apply(JsonNode t) {

        properties.entrySet().stream().sorted(Map.Entry.comparingByKey(PATH_ORDER)).forEach(e -> {

            PathSegment<?> target = lastPathComponent(t, e.getKey());
            target.fillMissingParents();

            if (target.getParent() == null) {
                throw new IllegalArgumentException("No parent node");
            }

            target.getParent().writeChildValue(target.getIncomingPath(), e.getValue());
        });

        return t;
    }

    protected PathSegment<?> lastPathComponent(JsonNode t, String path) {
        return PathSegment.create(t, path).lastPathComponent().get();
    }

    /**
     * Compares two property paths lexicographically, except for the array indices that are compared as numbers, so
     * that "a[2]" comes before "a[10]", and the symbolic "a[.length]" index comes after either of them. Digits outside
     * of the "[...]" brackets are just characters, as the relative order of object properties is of no consequence.
     */
    static int comparePaths(String p1, String p2) {

        int len1 = p1.length();
        int len2 = p2.length();
        int i1 = 0;
        int i2 = 0;

        // the paths are traversed in lockstep, and only advance past matching characters, so a single flag describes
        // the position in both of them
        boolean insideIndex = false;

        while (i1 < len1 && i2 < len2) {

            char c1 = p1.charAt(i1);
            char c2 = p2.charAt(i2);

            // "[.length]" appends to the end of an array, so it must be applied after all the explicit indices of
            // that array. Otherwise the appended element would be overwritten by an explicit index
            if (insideIndex) {
                boolean pastEnd1 = isPastEndIndex(p1, i1);
                boolean pastEnd2 = isPastEndIndex(p2, i2);
                if (pastEnd1 != pastEnd2) {
                    return pastEnd1 ? 1 : -1;
                }
            }

            if (insideIndex && isDigit(c1) && isDigit(c2)) {

                int end1 = digitsEnd(p1, i1);
                int end2 = digitsEnd(p2, i2);

                // ignore leading zeros, so that "01" and "1" are treated as the same number
                int start1 = significantDigitsStart(p1, i1, end1);
                int start2 = significantDigitsStart(p2, i2, end2);

                // longer number is bigger
                int cmp = (end1 - start1) - (end2 - start2);
                if (cmp != 0) {
                    return cmp;
                }

                // same number of digits, so a char-by-char comparison yields the numeric order
                for (int i = 0; i < end1 - start1; i++) {
                    cmp = p1.charAt(start1 + i) - p2.charAt(start2 + i);
                    if (cmp != 0) {
                        return cmp;
                    }
                }

                // the index is left unclosed for now. The trailing "]" is consumed on the next iteration
                i1 = end1;
                i2 = end2;
            } else if (c1 != c2) {
                return c1 - c2;
            } else {
                if (c1 == '[') {
                    insideIndex = true;
                } else if (c1 == ']') {
                    insideIndex = false;
                }

                i1++;
                i2++;
            }
        }

        // one path is a prefix of the other (or they only differ in leading zeros). Fall back to a plain comparison
        // to keep the comparator consistent with "equals"
        int cmp = (len1 - i1) - (len2 - i2);
        return cmp != 0 ? cmp : p1.compareTo(p2);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isPastEndIndex(String path, int from) {
        // as PAST_END_INDEX has no internal '.', this can only match at the start of an index
        return path.startsWith(IndexPathSegment.PAST_END_INDEX, from)
                && from + IndexPathSegment.PAST_END_INDEX.length() < path.length()
                && path.charAt(from + IndexPathSegment.PAST_END_INDEX.length()) == ']';
    }

    private static int digitsEnd(String path, int from) {
        int i = from;
        while (i < path.length() && isDigit(path.charAt(i))) {
            i++;
        }
        return i;
    }

    private static int significantDigitsStart(String path, int from, int end) {
        int i = from;
        // stop at "end - 1", so that a run of all zeros is treated as a single "0" digit
        while (i < end - 1 && path.charAt(i) == '0') {
            i++;
        }
        return i;
    }
}
