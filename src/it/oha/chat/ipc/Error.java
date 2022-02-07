package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;

public class Error extends Packet {
    public Error(String fmt, Object... args) {
        this.message = String.format(fmt, args);
    }

    public String message;

    @Override
    public Error onServer(Server ser, Connection c) {
        return null;
    }

    @Override
    public void onClient(Client cli, Connection c) {
        cli.onError(this);
    }
}
