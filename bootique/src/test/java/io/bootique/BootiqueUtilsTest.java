package io.bootique;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;

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
        assertArrayEquals(new String[]{"b", "c", "d"},
                BootiqueUtils.mergeArrays(new String[]{"b", "c"}, new String[]{"d"}));
    }
}
