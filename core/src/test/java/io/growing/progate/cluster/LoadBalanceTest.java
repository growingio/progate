package io.growing.progate.cluster;

import io.growing.gateway.cluster.LoadBalance;
import io.growing.gateway.meta.ServerNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

class LoadBalanceTest {

    @Test
    void testRoundRobin() {
        final LoadBalance lb = new RoundRobin();
        final Object context = new Object();
        Assertions.assertNull(lb.select(Collections.emptyList(), context));

        final ServerNode node1 = Mockito.mock(ServerNode.class);
        Assertions.assertEquals(node1, lb.select(List.of(node1), context));

        final ServerNode node2 = Mockito.mock(ServerNode.class);
        final List<ServerNode> nodes = List.of(node1, node2);

        final int count = 100;
        int agg = 0;
        for (int i = 0; i < count; i++) {
            final ServerNode node = lb.select(nodes, context);
            if (node == node1) {
                agg++;
            }
        }
        Assertions.assertTrue(agg >= 45);
        Assertions.assertTrue(agg <= 55);
    }

}
