package io.growing.gateway.app;

public class GlobalConfig {
    private HashIdConfig hashids;

    public static class HashIdConfig {
        private String salt;
        private int length;

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    public HashIdConfig getHashids() {
        return hashids;
    }

    public void setHashids(HashIdConfig hashids) {
        this.hashids = hashids;
    }
}