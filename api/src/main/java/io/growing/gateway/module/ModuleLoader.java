package io.growing.gateway.module;

import io.growing.gateway.api.Upstream;

/**
 * @author AI
 */
public interface ModuleLoader {

    ModuleScheme load(Upstream upstream);

}
