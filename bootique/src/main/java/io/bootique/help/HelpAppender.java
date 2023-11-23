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

package io.bootique.help;

import java.util.Collection;

import static java.util.Arrays.asList;

/*
 * A helper for building a text help document with consistent formatting. It will include line breaks to separate
 * sections, will provide consistent offsets, text wrapping, etc.
 */
public class HelpAppender {

    private final ConsoleAppender rootAppender;
    private ConsoleAppender offsetAppender;
    private ConsoleAppender doubleOffsetAppender;

    private int sectionCount;
    private int subsectionCount;

    public HelpAppender(ConsoleAppender appender) {
        this.rootAppender = appender;
    }

    public ConsoleAppender getAppender() {
        return rootAppender;
    }

    public void printSectionName(String name) {

        // line break between sections
        if (sectionCount++ > 0) {
            subsectionCount = 0;
            rootAppender.println();
        }

        rootAppender.println(name);
    }

    public void printSubsectionHeader(String... parts) {
        printSubsectionHeader(asList(parts));
    }

    public void printSubsectionHeader(Collection<String> parts) {

        // line break between subsections
        if (subsectionCount++ > 0) {
            rootAppender.println();
        }

        getOrCreateOffsetAppender().foldPrintln(parts);
    }

    public void printText(String... parts) {
        // TODO: should this be 'foldPrintln(..)'?
        getOrCreateOffsetAppender().println(parts);
    }

    public void printDescription(String... parts) {
        getOrCreateDoubleOffsetAppender().foldPrintln(parts);
    }

    public void println() {
        rootAppender.println();
    }

    private ConsoleAppender getOrCreateOffsetAppender() {
        if (this.offsetAppender == null) {
            this.offsetAppender = rootAppender.withOffset(6);
        }

        return offsetAppender;
    }

    private ConsoleAppender getOrCreateDoubleOffsetAppender() {
        if (this.doubleOffsetAppender == null) {
            // "5" is the magic number used for decsription offset. It seems to look best when description is printed
            // under the options
            this.doubleOffsetAppender = getOrCreateOffsetAppender().withOffset(5);
        }

        return doubleOffsetAppender;
    }
}
