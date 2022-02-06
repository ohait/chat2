package it.oha.chat;

import it.oha.chat.ipc.Message;
import it.oha.util.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoreTest {
    File tmpDir() {
        String strTmp = System.getProperty("java.io.tmpdir");
        return new File(strTmp);
    }

    @Test
    void storeAndFetch() {
        var tmp = tmpDir();
        Log.notice("storeAndFetch() using " + tmp);
        tmp.deleteOnExit();
        var s = new FileStore(tmp);
        var m1 = new Message("t1", "hello");
        m1.sender = "u1";
        m1.time = new Date();
        s.file("t1").deleteOnExit();
        try {
            s.store(m1);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.assertNull(e);
        }
        try {
            var list = s.fetch("t1");
            assertEquals(1, list.size());
            assertEquals(m1.getClass(), list.get(0).getClass());


        } catch (IOException e) {
            assertEquals(null, e); // there must be a better way
        }
    }
}