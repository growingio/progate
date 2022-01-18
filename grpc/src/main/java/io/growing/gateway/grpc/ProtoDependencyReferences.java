package io.growing.gateway.grpc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ProtoDependencyReferences {
    private ProtoDependencyReferences() {
    }

    private static final List<ProtoDependencyReference> REFERENCES = new CopyOnWriteArrayList<>();

    public static void register(ProtoDependencyReference reference) {
        REFERENCES.add(reference);
    }

    public static Optional<String> find(final String packageName, final String protoFilename) {
        for (ProtoDependencyReference reference : REFERENCES) {
            if (("*".equals(reference.getIn()) || packageName.equals(reference.getIn())) && protoFilename.equals(reference.getRef())) {
                return Optional.ofNullable(reference.getTo());
            }
        }
        return Optional.empty();
    }

}
