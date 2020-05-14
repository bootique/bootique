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

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BootiqueUtilsTest {

    @Test
    public void testToArray() {
        assertArrayEquals(new String[]{}, BootiqueUtils.toArray(Collections.emptyList()));
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
        BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(asList(testModuleProvider2, testModuleProvider3));

        BQModuleMetadata.Builder builder1 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata1 = mock(BQModuleMetadata.class);
        when(builder1.build()).thenReturn(metadata1);
        when(testModuleProvider1.moduleBuilder()).thenReturn(builder1);

        BQModuleMetadata.Builder builder2 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata2 = mock(BQModuleMetadata.class);
        when(builder2.build()).thenReturn(metadata2);
        when(testModuleProvider2.moduleBuilder()).thenReturn(builder2);

        BQModuleMetadata.Builder builder3 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata3 = mock(BQModuleMetadata.class);
        when(builder3.build()).thenReturn(metadata3);
        when(testModuleProvider3.moduleBuilder()).thenReturn(builder3);

        Collection<BQModuleMetadata> bqModuleMetadata =
            BootiqueUtils.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertEquals(3, bqModuleMetadata.size());
        assertTrue(bqModuleMetadata.contains(metadata1));
        assertTrue(bqModuleMetadata.contains(metadata2));
        assertTrue(bqModuleMetadata.contains(metadata3));

        verify(testModuleProvider1, times(1)).dependencies();
        verify(testModuleProvider1, times(1)).moduleBuilder();
        verify(testModuleProvider2, times(1)).dependencies();
        verify(testModuleProvider2, times(1)).moduleBuilder();
        verify(testModuleProvider3, times(1)).dependencies();
        verify(testModuleProvider3, times(1)).moduleBuilder();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }

    @Test
    public void moduleProviderDependenciesTwoLevels() {
        BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(asList(testModuleProvider2, testModuleProvider3));

        BQModuleMetadata.Builder builder1 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata1 = mock(BQModuleMetadata.class);
        when(builder1.build()).thenReturn(metadata1);
        when(testModuleProvider1.moduleBuilder()).thenReturn(builder1);

        BQModuleMetadata.Builder builder2 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata2 = mock(BQModuleMetadata.class);
        when(builder2.build()).thenReturn(metadata2);
        when(testModuleProvider2.moduleBuilder()).thenReturn(builder2);

        BQModuleMetadata.Builder builder3 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata3 = mock(BQModuleMetadata.class);
        when(builder3.build()).thenReturn(metadata3);
        when(testModuleProvider3.moduleBuilder()).thenReturn(builder3);

        Collection<BQModuleMetadata> bqModuleMetadata =
                BootiqueUtils.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertEquals(3, bqModuleMetadata.size());
        assertTrue(bqModuleMetadata.contains(metadata1));
        assertTrue(bqModuleMetadata.contains(metadata2));
        assertTrue(bqModuleMetadata.contains(metadata3));

        verify(testModuleProvider1, times(1)).dependencies();
        verify(testModuleProvider1, times(1)).moduleBuilder();
        verify(testModuleProvider2, times(1)).dependencies();
        verify(testModuleProvider2, times(1)).moduleBuilder();
        verify(testModuleProvider3, times(1)).dependencies();
        verify(testModuleProvider3, times(1)).moduleBuilder();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }

    @Test
    public void moduleProviderDependenciesCircular() {
        BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(asList(testModuleProvider2, testModuleProvider3));

        BQModuleMetadata.Builder builder1 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata1 = mock(BQModuleMetadata.class);
        when(builder1.build()).thenReturn(metadata1);
        when(testModuleProvider1.moduleBuilder()).thenReturn(builder1);

        BQModuleMetadata.Builder builder2 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata2 = mock(BQModuleMetadata.class);
        when(builder2.build()).thenReturn(metadata2);
        when(testModuleProvider2.moduleBuilder()).thenReturn(builder2);

        BQModuleMetadata.Builder builder3 = mock(BQModuleMetadata.Builder.class);
        BQModuleMetadata metadata3 = mock(BQModuleMetadata.class);
        when(builder3.build()).thenReturn(metadata3);
        when(testModuleProvider3.moduleBuilder()).thenReturn(builder3);

        Collection<BQModuleMetadata> bqModuleMetadata =
                BootiqueUtils.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertEquals(3, bqModuleMetadata.size());
        assertTrue(bqModuleMetadata.contains(metadata1));
        assertTrue(bqModuleMetadata.contains(metadata2));
        assertTrue(bqModuleMetadata.contains(metadata3));

        verify(testModuleProvider1, times(1)).dependencies();
        verify(testModuleProvider1, times(1)).moduleBuilder();
        verify(testModuleProvider2, times(1)).dependencies();
        verify(testModuleProvider2, times(1)).moduleBuilder();
        verify(testModuleProvider3, times(1)).dependencies();
        verify(testModuleProvider3, times(1)).moduleBuilder();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }
}
