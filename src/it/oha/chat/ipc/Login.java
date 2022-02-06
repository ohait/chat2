package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;

public class Login extends Packet {
    public String token;

    public Login(String token) {
        this.token = token;
    }

    @Override
    public void onServer(Server ser, Connection c) {
        c.name = token; // TODO: parse token for authentication instead
    }

    @Override
    public void onClient(Client cli, Connection c) {
        // unexpected
    }
}
