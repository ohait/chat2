# chat2

experiments with chat client/server

## server

    it.oha.chat.Server host:port path/

starts a server listening to the given host/port

use `0.0.0.0` to bind to all interfaces

use `:0` port to get a random port

## client

    it.oha.chat.Client name host:port

specify the name you want to use in the chat system, and where to connect for a server

after than you will be able to send messages to people using the terminal:

    @foo: message

or to everyone

    @all: hello!

you will also receive any message for yout `@name` or `@all`

