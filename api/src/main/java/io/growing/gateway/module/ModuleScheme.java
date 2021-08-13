package io.growing.gateway.module;

import java.util.List;

/**
 * @author AI
 */
public interface ModuleScheme {

    String name();

    List<byte[]> graphqlDefinitions();

    List<byte[]> restfulDefinitions();

}
