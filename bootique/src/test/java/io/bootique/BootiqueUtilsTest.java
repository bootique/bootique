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

package io.bootique;

import org.junit.Test;
import org.mockito.internal.verification.AtLeast;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class BootiqueUtilsTest {

    @Test
    public void testToArray() {
        assertArrayEquals(new String[]{}, BootiqueUtils.toArray(asList()));
        assertArrayEquals(new String[]{"a", "b", "c"}, BootiqueUtils.toArray(asList("a", "b", "c")));
    }

    @Test
    public void testMergeArrays() {
        assertArrayEquals(new String[]{}, BootiqueUtils.mergeArrays(new String[0], new String[0]));
        assertArrayEquals(new String[]{"a"}, BootiqueUtils.mergeArrays(new String[]{"a"}, new String[0]));
        assertArrayEquals(new String[]{"b"}, BootiqueUtils.mergeArrays(new String[0], new String[]{"b"}));
        assertArrayEquals(new String[]{"b", "c", "d"}, BootiqueUtils.mergeArrays(new String[]{"b", "c"}, new String[]{"d"}));
    }

    @Test
    public void moduleProviderDependencies() {
        final BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        final BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        final BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(asList(testModuleProvider2, testModuleProvider3));

        final Collection<BQModuleProvider> bqModuleProviders =
            BootiqueUtils.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertThat(bqModuleProviders, hasItems(testModuleProvider1, testModuleProvider2, testModuleProvider3));
        assertEquals(3, bqModuleProviders.size());

        verify(testModuleProvider1, new AtLeast(1)).dependencies();
        verify(testModuleProvider1, new AtLeast(1)).name();
        verify(testModuleProvider2, new AtLeast(1)).dependencies();
        verify(testModuleProvider2, new AtLeast(1)).name();
        verify(testModuleProvider3, new AtLeast(1)).dependencies();
        verify(testModuleProvider3, new AtLeast(1)).name();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }

    @Test
    public void moduleProviderDependenciesTwoLevels() {
        final BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        final BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        final BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(singletonList(testModuleProvider2));
        when(testModuleProvider2.dependencies()).thenReturn(singletonList(testModuleProvider3));

        final Collection<BQModuleProvider> bqModuleProviders =
            BootiqueUtils.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertThat(bqModuleProviders, hasItems(testModuleProvider1, testModuleProvider2, testModuleProvider3));
        assertEquals(3, bqModuleProviders.size());

        verify(testModuleProvider1, new AtLeast(1)).dependencies();
        verify(testModuleProvider1, new AtLeast(1)).name();
        verify(testModuleProvider2, new AtLeast(1)).dependencies();
        verify(testModuleProvider2, new AtLeast(1)).name();
        verify(testModuleProvider3, new AtLeast(1)).dependencies();
        verify(testModuleProvider3, new AtLeast(1)).name();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }

    @Test
    public void moduleProviderDependenciesCircular() {
        final BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        final BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        final BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(singletonList(testModuleProvider2));
        when(testModuleProvider2.dependencies()).thenReturn(singletonList(testModuleProvider3));
        when(testModuleProvider3.dependencies()).thenReturn(singletonList(testModuleProvider1));

        final Collection<BQModuleProvider> bqModuleProviders =
            BootiqueUtils.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertThat(bqModuleProviders, hasItems(testModuleProvider1, testModuleProvider2, testModuleProvider3));
        assertEquals(3, bqModuleProviders.size());

        verify(testModuleProvider1, new AtLeast(1)).dependencies();
        verify(testModuleProvider1, new AtLeast(1)).name();
        verify(testModuleProvider2, new AtLeast(1)).dependencies();
        verify(testModuleProvider2, new AtLeast(1)).name();
        verify(testModuleProvider3, new AtLeast(1)).dependencies();
        verify(testModuleProvider3, new AtLeast(1)).name();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }
}
