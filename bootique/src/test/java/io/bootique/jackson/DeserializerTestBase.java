/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.jackson;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.config.TypesFactory;
import io.bootique.log.DefaultBootLogger;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public abstract class DeserializerTestBase {

    private TypesFactory<PolymorphicConfiguration> typesFactory = new TypesFactory<>(
            getClass().getClassLoader(),
            PolymorphicConfiguration.class,
            new DefaultBootLogger(true));

    private JacksonService jacksonService = new DefaultJacksonService(typesFactory.getTypes());

    protected <T> T deserialize(Class<T> type, String yml) throws IOException {
        YAMLParser parser = new YAMLFactory().createParser(yml);
        return jacksonService.newObjectMapper().readValue(parser, type);
    }

    protected ObjectMapper createMapper() {
        return jacksonService.newObjectMapper();
    }

    protected static class Bean1 {
        protected String a;
        protected Bean2 c;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public Bean2 getC() {
            return c;
        }

        public void setC(Bean2 c) {
            this.c = c;
        }
    }

    protected static class Bean2 {

        protected OffsetTime offsetTime;
        protected OffsetDateTime offsetDateTime;
        protected ZonedDateTime zonedDateTime;
        protected ZoneOffset zoneOffset;

        public OffsetTime getOffsetTime() {
            return offsetTime;
        }

        public void setOffsetTime(OffsetTime offsetTime) {
            this.offsetTime = offsetTime;
        }

        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }

        public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
            this.offsetDateTime = offsetDateTime;
        }

        public ZonedDateTime getZonedDateTime() {
            return zonedDateTime;
        }

        public void setZonedDateTime(ZonedDateTime zonedDateTime) {
            this.zonedDateTime = zonedDateTime;
        }

        public ZoneOffset getZoneOffset() {
            return zoneOffset;
        }

        public void setZoneOffset(ZoneOffset zoneOffset) {
            this.zoneOffset = zoneOffset;
        }

    }
}
