package io.growing.gateway.utilities;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

/**
 * @author AI
 */
public class CollectionUtilsTests {

    @Test
    public void test() {
        Assertions.assertTrue(CollectionUtilities.isEmpty(null));
        Assertions.assertTrue(CollectionUtilities.isEmpty(new HashSet<>()));
        Assertions.assertFalse(CollectionUtilities.isNotEmpty(null));
        Assertions.assertTrue(CollectionUtilities.isNotEmpty(Sets.newHashSet("")));
    }

}
