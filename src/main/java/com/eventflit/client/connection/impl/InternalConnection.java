package com.eventflit.client.connection.impl;

import com.eventflit.client.connection.Connection;

public interface InternalConnection extends Connection {

    void sendMessage(String message);

    void disconnect();
}
