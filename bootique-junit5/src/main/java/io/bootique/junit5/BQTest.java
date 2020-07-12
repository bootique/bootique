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
package io.bootique.junit5;

import io.bootique.junit5.handler.app.BQAppHandler;
import io.bootique.junit5.handler.testtool.BQTestToolHandler;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

/**
 * Registers Bootique Junit 5 extension that will manage emulated test apps in the annotated test class. Used in
 * conjunction with fields annotated with @{@link BQApp} and @{@link BQTestTool}.
 *
 * @since 2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
// TODO: need TestToolHandler to be run before the AppHandler.. Is there a guarantee that the order here is followed?
@ExtendWith({BQTestToolHandler.class, BQAppHandler.class})
@Inherited
public @interface BQTest {
}
