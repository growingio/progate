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
        Assertions.assertTrue(CollectionUtils.isEmpty(null));
        Assertions.assertTrue(CollectionUtils.isEmpty(new HashSet<>()));
        Assertions.assertTrue(CollectionUtils.isNotEmpty(Sets.newHashSet("")));
    }

}
