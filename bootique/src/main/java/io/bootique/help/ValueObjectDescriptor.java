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

/**
 * Descriptor of value object type.
 * For now (as of version 1.0.RC1) it only contains description text that will be shown in app help.
 *
 * @since 1.0.RC1
 */
public class ValueObjectDescriptor {

	private final String description;

	public ValueObjectDescriptor(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
