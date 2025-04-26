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

package io.bootique.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A class intended for subclassing to obtain a {@link Type} instance describing a specific generic type. Intended to
 * work around Java generics inference limitations.
 */
public abstract class TypeRef<T> {

    protected final Type type;

    protected TypeRef() {
        if (getClass().getGenericSuperclass() instanceof ParameterizedType pt) {
            this.type = pt.getActualTypeArguments()[0];
        } else {
            // should not happen
            throw new IllegalArgumentException("TypeRef created without type information.");
        }
    }

    public Type getType() {
        return type;
    }
}
