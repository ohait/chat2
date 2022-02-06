package it.oha.util;

public class Addr {
    public String host;
    public int port;

    public Addr(String hostport) {
        var parts = hostport.split(":");
        this.host = parts[0];
        switch (parts.length) {
            case 1:
                break;
            case 2:
                try {
                    this.port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("invalid port: " + hostport);
                }
                break;
            default:
                throw new IllegalArgumentException("invalid host: " + hostport);
        }
    }
}
