package io.growing.gateway.graphql.transcode;

public class TranscodeMapping {
    private String source;
    private String target;
    private String extension;

    public TranscodeMapping(String source, String target, String extension) {
        this.source = source;
        this.target = target;
        this.extension = extension;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getExtension() {
        return extension;
    }
}
