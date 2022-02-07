package it.oha.chat.ipc;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Server;
import it.oha.util.Log;

import java.util.regex.Pattern;

public class Login extends Packet {
    public String token;

    public Login(String token) {
        this.token = token;
    }

    static Pattern reValidName = Pattern.compile("""
                    ^
                    (?!all) # not all
                    \\w+ # at least 1 letter/underscore
                    $
            """, Pattern.COMMENTS);

    @Override
    public Error onServer(Server ser, Connection c) {
        c.name = token; // TODO: parse token for authentication instead
        if (!reValidName.matcher(c.name).matches()) {
            return new Error("invalid name: %s", c.name);
        }

        Log.debug("login %s", c.name);

        ser.topic(c.name).subscribe(c::emit);
        ser.topic("all").subscribe(c::emit);

        ser.topic("all").broadcast(this); // tell everyone who joined
        return null;
    }

    @Override
    public void onClient(Client cli, Connection c) {
        // ignore
    }
}
