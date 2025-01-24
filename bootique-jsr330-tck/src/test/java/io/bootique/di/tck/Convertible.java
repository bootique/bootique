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

package io.bootique.di.tck;

import io.bootique.di.tck.accessories.Cupholder;
import io.bootique.di.tck.accessories.SpareTire;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

public class Convertible implements Car {

    @Inject
    @Drivers
    Seat driversSeatA;
    @Inject
    @Drivers
    Seat driversSeatB;
    @Inject
    SpareTire spareTire;
    @Inject
    Cupholder cupholder;
    @Inject
    Provider<Engine> engineProvider;

    private boolean methodWithZeroParamsInjected;
    private boolean methodWithMultipleParamsInjected;
    private boolean methodWithNonVoidReturnInjected;

    private Seat constructorPlainSeat;
    private Seat constructorDriversSeat;
    private Tire constructorPlainTire;
    private Tire constructorSpareTire;
    private Provider<Seat> constructorPlainSeatProvider = nullProvider();
    private Provider<Seat> constructorDriversSeatProvider = nullProvider();
    private Provider<Tire> constructorPlainTireProvider = nullProvider();
    private Provider<Tire> constructorSpareTireProvider = nullProvider();

    @Inject
    Seat fieldPlainSeat;
    @Inject
    @Drivers
    Seat fieldDriversSeat;
    @Inject
    Tire fieldPlainTire;
    @Inject
    @Named("spare")
    Tire fieldSpareTire;
    @Inject
    Provider<Seat> fieldPlainSeatProvider = nullProvider();
    @Inject
    @Drivers
    Provider<Seat> fieldDriversSeatProvider = nullProvider();
    @Inject
    Provider<Tire> fieldPlainTireProvider = nullProvider();
    @Inject
    @Named("spare")
    Provider<Tire> fieldSpareTireProvider = nullProvider();

    private Seat methodPlainSeat;
    private Seat methodDriversSeat;
    private Tire methodPlainTire;
    private Tire methodSpareTire;
    private Provider<Seat> methodPlainSeatProvider = nullProvider();
    private Provider<Seat> methodDriversSeatProvider = nullProvider();
    private Provider<Tire> methodPlainTireProvider = nullProvider();
    private Provider<Tire> methodSpareTireProvider = nullProvider();

    @Inject
    static Seat staticFieldPlainSeat;
    @Inject
    @Drivers
    static Seat staticFieldDriversSeat;
    @Inject
    static Tire staticFieldPlainTire;
    @Inject
    @Named("spare")
    static Tire staticFieldSpareTire;
    @Inject
    static Provider<Seat> staticFieldPlainSeatProvider = nullProvider();
    @Inject
    @Drivers
    static Provider<Seat> staticFieldDriversSeatProvider = nullProvider();
    @Inject
    static Provider<Tire> staticFieldPlainTireProvider = nullProvider();
    @Inject
    @Named("spare")
    static Provider<Tire> staticFieldSpareTireProvider = nullProvider();

    private static Seat staticMethodPlainSeat;
    private static Seat staticMethodDriversSeat;
    private static Tire staticMethodPlainTire;
    private static Tire staticMethodSpareTire;
    private static Provider<Seat> staticMethodPlainSeatProvider = nullProvider();
    private static Provider<Seat> staticMethodDriversSeatProvider = nullProvider();
    private static Provider<Tire> staticMethodPlainTireProvider = nullProvider();
    private static Provider<Tire> staticMethodSpareTireProvider = nullProvider();

    @Inject
    Convertible(
            Seat plainSeat,
            @Drivers Seat driversSeat,
            Tire plainTire,
            @Named("spare") Tire spareTire,
            Provider<Seat> plainSeatProvider,
            @Drivers Provider<Seat> driversSeatProvider,
            Provider<Tire> plainTireProvider,
            @Named("spare") Provider<Tire> spareTireProvider) {
        constructorPlainSeat = plainSeat;
        constructorDriversSeat = driversSeat;
        constructorPlainTire = plainTire;
        constructorSpareTire = spareTire;
        constructorPlainSeatProvider = plainSeatProvider;
        constructorDriversSeatProvider = driversSeatProvider;
        constructorPlainTireProvider = plainTireProvider;
        constructorSpareTireProvider = spareTireProvider;
    }

    Convertible() {
        throw new AssertionError("Unexpected call to non-injectable constructor");
    }

    void setSeat(Seat unused) {
        throw new AssertionError("Unexpected call to non-injectable method");
    }

    @Inject
    void injectMethodWithZeroArgs() {
        methodWithZeroParamsInjected = true;
    }

    @Inject
    String injectMethodWithNonVoidReturn() {
        methodWithNonVoidReturnInjected = true;
        return "unused";
    }

    @Inject
    void injectInstanceMethodWithManyArgs(
            Seat plainSeat,
            @Drivers Seat driversSeat,
            Tire plainTire,
            @Named("spare") Tire spareTire,
            Provider<Seat> plainSeatProvider,
            @Drivers Provider<Seat> driversSeatProvider,
            Provider<Tire> plainTireProvider,
            @Named("spare") Provider<Tire> spareTireProvider) {
        methodWithMultipleParamsInjected = true;

        methodPlainSeat = plainSeat;
        methodDriversSeat = driversSeat;
        methodPlainTire = plainTire;
        methodSpareTire = spareTire;
        methodPlainSeatProvider = plainSeatProvider;
        methodDriversSeatProvider = driversSeatProvider;
        methodPlainTireProvider = plainTireProvider;
        methodSpareTireProvider = spareTireProvider;
    }

    @Inject
    static void injectStaticMethodWithManyArgs(
            Seat plainSeat,
            @Drivers Seat driversSeat,
            Tire plainTire,
            @Named("spare") Tire spareTire,
            Provider<Seat> plainSeatProvider,
            @Drivers Provider<Seat> driversSeatProvider,
            Provider<Tire> plainTireProvider,
            @Named("spare") Provider<Tire> spareTireProvider) {
        staticMethodPlainSeat = plainSeat;
        staticMethodDriversSeat = driversSeat;
        staticMethodPlainTire = plainTire;
        staticMethodSpareTire = spareTire;
        staticMethodPlainSeatProvider = plainSeatProvider;
        staticMethodDriversSeatProvider = driversSeatProvider;
        staticMethodPlainTireProvider = plainTireProvider;
        staticMethodSpareTireProvider = spareTireProvider;
    }

    /**
     * Returns a provider that always returns null. This is used as a default
     * value to avoid null checks for omitted provider injections.
     */
    private static <T> Provider<T> nullProvider() {
        return new Provider<T>() {
            public T get() {
                return null;
            }
        };
    }

    // name accessors in a random way so that they do not resemble properties and do not create potential side effects
    // for the injector

    boolean fotTest_methodWithZeroParamsInjected() {
        return methodWithZeroParamsInjected;
    }

    boolean forTest_methodWithMultipleParamsInjected() {
        return methodWithMultipleParamsInjected;
    }

    boolean forTest_methodWithNonVoidReturnInjected() {
        return methodWithNonVoidReturnInjected;
    }

    Seat forTest_constructorPlainSeat() {
        return constructorPlainSeat;
    }

    Seat forTest_constructorDriversSeat() {
        return constructorDriversSeat;
    }

    Tire forTest_constructorPlainTire() {
        return constructorPlainTire;
    }

    Tire forTest_constructorSpareTire() {
        return constructorSpareTire;
    }

    Provider<Seat> forTest_constructorPlainSeatProvider() {
        return constructorPlainSeatProvider;
    }

    Provider<Seat> forTest_constructorDriversSeatProvider() {
        return constructorDriversSeatProvider;
    }

    Provider<Tire> forTest_constructorPlainTireProvider() {
        return constructorPlainTireProvider;
    }

    Provider<Tire> forTest_constructorSpareTireProvider() {
        return constructorSpareTireProvider;
    }

    Seat forTest_methodPlainSeat() {
        return methodPlainSeat;
    }

    Seat forTest_methodDriversSeat() {
        return methodDriversSeat;
    }

    Tire forTest_methodPlainTire() {
        return methodPlainTire;
    }

    Tire forTest_methodSpareTire() {
        return methodSpareTire;
    }

    Provider<Seat> forTest_methodPlainSeatProvider() {
        return methodPlainSeatProvider;
    }

    Provider<Seat> forTest_methodDriversSeatProvider() {
        return methodDriversSeatProvider;
    }

    Provider<Tire> forTest_methodPlainTireProvider() {
        return methodPlainTireProvider;
    }

    Provider<Tire> forTest_methodSpareTireProvider() {
        return methodSpareTireProvider;
    }
}
