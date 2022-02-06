package it.oha.chat.test;

import it.oha.chat.Client;
import it.oha.chat.Connection;
import it.oha.chat.Disconnect;
import it.oha.chat.Server;
import it.oha.chat.ipc.Login;
import it.oha.chat.ipc.Message;
import it.oha.chat.ipc.Packet;
import it.oha.chat.ipc.Subscribe;
import it.oha.util.Log;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    @Test
    public void testClient() {
        try {
            var server = new ServerSocket(0);
            BlockingQueue<Packet> recv = new ArrayBlockingQueue<Packet>(10);
            final Connection[] conn = new Connection[1];
            new Thread("mock-server") {
                @Override
                public void run() {
                    try {
                        var sock = server.accept();
                        conn[0] = new Connection(sock);
                        conn[0].startRecvLoop("mock-recv", recv::add);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            var cli = new Client("token1");

            cli.connect("127.0.0.1", server.getLocalPort());
            cli.subscribe("t1");
            cli.message("t1", "echo");

            assertEquals(Login.class, recv.poll(1, TimeUnit.SECONDS).getClass());
            assertEquals(Subscribe.class, recv.poll(1, TimeUnit.SECONDS).getClass());
            assertEquals(Message.class, recv.poll(1, TimeUnit.SECONDS).getClass());
            assertEquals(0, recv.size());

            conn[0].send(new Message("t2", "second"));

            assertFalse(cli.isClosed());
            conn[0].send(new Disconnect());
            Thread.sleep(10);
            assertTrue(cli.isClosed());

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testServer() {
        try (var s = new Server("localhost", 0, new Server.NoStore())
        ) {
            new Thread(s, "server").start();
            Log.notice("server: " + s);

            var sock = new Socket("127.0.0.1", s.getPort());
            var conn = new Connection(sock);
            conn.startRecvLoop("mock-recv", (p) -> {
                Log.info("recv from server: " + p);
            });
            conn.startSendLoop("mock-send");
            Log.notice("client mock connected" + conn);

            conn.emit(new Login("user1"));
            conn.emit(new Subscribe("t1"));
            conn.emit(new Message("t1", "hello!"));
            conn.emit(new Disconnect());

            Log.notice("waitDone");
            s.close();
            s.waitDone();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Log.notice("test done");
    }


}
