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

package io.bootique.value;

/**
 * @since 1.0.RC1
 */
public enum BytesUnit {
    BYTES("Bytes", 1),
    KB("Kilobytes", 1024),
    MB("Megabytes", 1024*1024),
    GB("Gigabytes", 1024*1024*1024);

    private String name;
    private long value;

    BytesUnit(final String name, final long value) {
        this.name = name;
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
