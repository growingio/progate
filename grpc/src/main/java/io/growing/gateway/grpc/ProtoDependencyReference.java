package io.growing.gateway.grpc;

public class ProtoDependencyReference {
    private String in;
    private String ref;
    private String to;

    public ProtoDependencyReference(String in, String ref, String to) {
        this.in = in;
        this.ref = ref;
        this.to = to;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
