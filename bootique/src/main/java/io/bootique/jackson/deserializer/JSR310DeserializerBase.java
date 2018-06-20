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

package io.bootique.jackson.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.io.IOException;
import java.time.DateTimeException;

/**
 * Base class that indicates that all JSR310 datatypes are deserialized from scalar JSON types.
 */
abstract class JSR310DeserializerBase<T> extends StdScalarDeserializer<T> {

    protected JSR310DeserializerBase(Class<T> supportedType) {
        super(supportedType);
    }

    @Override
    public Object deserializeWithType(JsonParser parser, DeserializationContext context, TypeDeserializer deserializer)
            throws IOException {
        return deserializer.deserializeTypedFromAny(parser, context);
    }

    protected void reportWrongToken(JsonParser parser, DeserializationContext context,
                                    JsonToken exp, String unit) throws IOException {
        throw context.wrongTokenException(parser, JsonToken.VALUE_NUMBER_INT,
                "Expected " + exp.name() + " for '" + unit + "' of " + handledType().getName() + " value");
    }

    /**
     * Helper method used to peel off spurious wrappings of DateTimeException
     */
    protected DateTimeException _peelDTE(DateTimeException e) {
        while (true) {
            Throwable t = e.getCause();
            if (t != null && t instanceof DateTimeException) {
                e = (DateTimeException) t;
                continue;
            }
            break;
        }
        return e;
    }
}
