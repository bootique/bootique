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

package io.bootique.jackson.deserializer;

import java.math.BigDecimal;

/**
 * Utilities to aid in the translation of decimal types to/from multiple parts.
 */
final class DecimalUtils {
    private static final BigDecimal ONE_BILLION = new BigDecimal(1_000_000_000L);

    private DecimalUtils() {
        throw new RuntimeException("DecimalUtils cannot be instantiated.");
    }

    public static String toDecimal(long seconds, int nanoseconds) {
        StringBuilder sb = new StringBuilder(20)
                .append(seconds)
                .append('.');
        // 14-Mar-2016, tatu: Although we do not yet (with 2.7) trim trailing zeroes,
        //   for general case, 
        if (nanoseconds == 0L) {
            // !!! TODO: 14-Mar-2016, tatu: as per [datatype-jsr310], should trim
            //     trailing zeroes
            if (seconds == 0L) {
                return "0.0";
            }

//            sb.append('0');
            sb.append("000000000");
        } else {
            StringBuilder nanoSB = new StringBuilder(9);
            nanoSB.append(nanoseconds);
            // May need to both prepend leading nanos (if value less than 0.1)
            final int nanosLen = nanoSB.length();
            int prepZeroes = 9 - nanosLen;
            while (prepZeroes > 0) {
                --prepZeroes;
                sb.append('0');
            }

            // !!! TODO: 14-Mar-2016, tatu: as per [datatype-jsr310], should trim
            //     trailing zeroes
            /*
            // AND possibly trim trailing ones
            int i = nanosLen;
            while ((i > 1) && nanoSB.charAt(i-1) == '0') {
                --i;
            }
            if (i < nanosLen) {
                nanoSB.setLength(i);
            }
            */
            sb.append(nanoSB);
        }
        return sb.toString();
    }

    /**
     * @since 2.7.3
     */
    public static BigDecimal toBigDecimal(long seconds, int nanoseconds) {
        if (nanoseconds == 0L) {
            // 14-Mar-2015, tatu: Let's retain one zero to avoid interpretation
            //    as integral number
            if (seconds == 0L) { // except for "0.0" where it can not be done without scientific notation
                return BigDecimal.ZERO.setScale(1);
            }
            return BigDecimal.valueOf(seconds).setScale(9);
        }
        return new BigDecimal(toDecimal(seconds, nanoseconds));
    }

    public static int extractNanosecondDecimal(BigDecimal value, long integer) {
        // !!! 14-Mar-2016, tatu: Somewhat inefficient; should replace with functionally
        //   equivalent code that just subtracts integral part? (or, measure and show
        //   there's no difference and do nothing... )
        return value.subtract(new BigDecimal(integer)).multiply(ONE_BILLION).intValue();
    }
}