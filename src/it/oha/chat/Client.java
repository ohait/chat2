package it.oha.chat;

import it.oha.chat.ipc.Error;
import it.oha.chat.ipc.Login;
import it.oha.chat.ipc.Message;
import it.oha.chat.ipc.Subscribe;
import it.oha.util.Addr;
import it.oha.util.Log;

import java.io.*;
import java.net.Socket;


public class Client implements AutoCloseable {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: Client <name> <host:port>");
            System.exit(-1);
        }
        try {
            var name = args[0];
            var addr = new Addr(args[1]);
            var cli = new Client(name);
            cli.connect(addr.host, addr.port);

            new Thread(() -> cli.readTerminal(System.in), "term").start();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void readTerminal(InputStream in) {
        try (
                BufferedReader obj = new BufferedReader(new InputStreamReader(in));
        ) {
            var parser = new CmdParser();
            while (true) {
                String cmd = obj.readLine();
                var p = parser.parse(cmd);
                if (p != null) conn.emit(p);
            }
        } catch (EOFException e) {
            // exit quietly
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Connection conn;
    private String token;

    public Client(String token) {
        this.token = token;
    }

    public void connect(String host, int port) throws IOException {
        Log.info("connecting to %s:%d", host, port);
        var sock = new Socket(host, port);
        conn = new Connection(sock);

        conn.startRecvLoop("recv", (p) -> {
            p.onClient(Client.this, conn);
        });
        conn.startSendLoop("send");
        conn.emit(new Login(token));
    }

    public void onMessage(Message m) {
        System.out.println(m.sender + "@" + m.topic + ": " + m.message);
    }

    public void onError(Error e) {
        System.err.println(e.message);
    }

    public void close() throws IOException {
        conn.close();
    }

    public boolean isClosed() {
        return conn.sock.isClosed();
    }

    public void subscribe(String topic) {
        conn.emit(new Subscribe(topic));
    }

    public void message(String topic, String message) {
        conn.emit(new Message(topic, message));
    }
}
