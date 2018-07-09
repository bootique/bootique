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

package io.bootique.help.config;

import io.bootique.help.ConsoleAppender;
import io.bootique.help.ValueObjectDescriptor;
import io.bootique.meta.config.ConfigValueMetadata;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ConfigSectionMapGenerator extends ConfigSectionGenerator {

    private Class<?> keysType;

	/**
	 * @deprecated since 0.26 use {@link #ConfigSectionMapGenerator(Class, ConsoleAppender, Map)} constructor
	 */
    @Deprecated
    public ConfigSectionMapGenerator(Class<?> keysType, ConsoleAppender out) {
        this(keysType, out, Collections.emptyMap());
    }

	public ConfigSectionMapGenerator(Class<?> keysType, ConsoleAppender out, Map<Class<?>, ValueObjectDescriptor> valueObjectsDescriptors) {
		super(out, valueObjectsDescriptors);
		this.keysType = Objects.requireNonNull(keysType);
	}

    @Override
    protected void printNode(ConfigValueMetadata metadata, boolean asValue) {

        if (asValue) {
            String valueLabel = metadata.getType() != null ? sampleValue(metadata.getType()) : "?";
            out.println(sampleValue(keysType), ": ", valueLabel);
        } else {
            out.println(sampleValue(keysType), ":");
        }
    }
}
