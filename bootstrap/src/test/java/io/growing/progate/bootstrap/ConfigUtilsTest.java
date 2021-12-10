package io.growing.progate.bootstrap;

import io.growing.progate.bootstrap.utils.ConfigUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigUtilsTest {

    @Test
    void testGetConfigPathFromArgs() {
        final String etc = "/etc/conf/gateway.conf";
        final String[] args = new String[]{"path", "config.path=" + etc};
        final String path = ConfigUtils.getApplicationConfigFile(args);
        Assertions.assertEquals(etc, path);
    }

    @Test
    void testGetConfigPathFromEnv() {
        final String etc = "/var/conf/gateway.conf";
        System.setProperty("config.path", etc);
        final String path = ConfigUtils.getApplicationConfigFile(new String[]{"path"});
        Assertions.assertEquals(etc, path);
    }

    @Test
    void testGetConfigPathDefault() {
        final String path = ConfigUtils.getApplicationConfigFile(new String[]{"env"});
        final String defaultPath = ConfigUtils.getDefaultConfigPath();
        Assertions.assertEquals(defaultPath, path);
    }


}
