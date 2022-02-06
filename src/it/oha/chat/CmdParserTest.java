package it.oha.chat;

import it.oha.chat.ipc.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CmdParserTest {

    @Test
    void testParse() {
        var parser = new CmdParser();
        { // message
            var p = (Message) parser.parse("@john: hello");
            assertEquals("hello", p.message);
            assertEquals("john", p.topic);
        }
        { // follow up
            var p = (Message) parser.parse("again");
            assertEquals("again", p.message);
            assertEquals("john", p.topic);
        }
        { // message with whitespaces
            var p = (Message) parser.parse("\t@all: hello  ");
            assertEquals("hello", p.message);
            assertEquals("all", p.topic);
        }
        { // exit
            var p = (Disconnect) parser.parse("     exit  ");
        }
    }
}