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

import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a percent value. Used as a value object to deserialize percentages in application configurations.
 */
public class Percent implements Comparable<Percent> {

    public static final Percent ZERO = new Percent(0.);
    public static final Percent HUNDRED = new Percent(1.);

    private double percent;

    /**
     * Creates a Percent instance from a String representation. The String can be either a double, in which case it
     * must represent a fraction of 1.0 (e.g. "0.5"), or a percent, in which case it must be followed by percentage sign
     * (e.g. "50.1%").
     *
     * @param value a String value representing percentage. Optionally followed by the percent sign. E.g. "0.5%",
     *              "100%", "-7.9%".
     */
    public Percent(String value) {
        this.percent = parse(value);
    }

    /**
     * Creates a percent instance from an int that represents a fraction of 1.0. I.e. 1 is "100%".
     *
     * @param value int representing a fraction of 1.0. I.e. 1 is "100%".
     */
    public Percent(int value) {
        this.percent = value * 100.;
    }

    /**
     * Creates a percent instance from an double that represents a fraction of 1.0. I.e. 0.5 is "50%".
     *
     * @param value double representing a fraction of 1.0. I.e. I.e. 0.5 is "50%".
     */
    public Percent(double value) {
        this.percent = value * 100.;
    }

    static double parse(String percent) {
        Objects.requireNonNull(percent, "Null 'percent' argument");

        if (percent.length() == 0) {
            throw new IllegalArgumentException("Empty 'percent' argument");
        }

        if (percent.charAt(percent.length() - 1) == '%') {
            percent = percent.substring(0, percent.length() - 1);
        }

        if (percent.length() == 0) {
            throw new IllegalArgumentException("Non-numeric 'percent' argument: '%'");
        }

        return Double.parseDouble(percent);
    }

    /**
     * Returns a double value for this percentage that is a fraction of 1. I.e. will return 0.05 for 5%.
     *
     * @return a double value for this percentage that is a fraction of 1.
     */
    public double getValue() {
        return percent / 100.;
    }

    public double getPercent() {
        return percent;
    }

    @Override
    @JsonValue
    public String toString() {
        return percent + "%";
    }

    /**
     * A {@link #toString()} variation that returns percent string rounded to the specified precision.
     *
     * @param precision precision for the returned String.
     * @return  percent string rounded to the specified precision.
     */
    public String toString(int precision) {
        return new BigDecimal(percent, new MathContext(precision, RoundingMode.HALF_UP)) + "%";
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Percent) {
            return percent == ((Percent) obj).percent;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(percent);
    }

    @Override
    public int compareTo(Percent o) {
        return Double.compare(percent, o.percent);
    }
}
