package com.eventflit.client;

import com.eventflit.client.channel.Channel;
import com.eventflit.client.channel.ChannelEventListener;
import com.eventflit.client.channel.PresenceChannel;
import com.eventflit.client.channel.PresenceChannelEventListener;
import com.eventflit.client.channel.PrivateChannel;
import com.eventflit.client.channel.PrivateChannelEventListener;
import com.eventflit.client.connection.Connection;
import com.eventflit.client.connection.ConnectionEventListener;
import com.eventflit.client.connection.ConnectionState;

/**
 * Created by jamiepatel on 09/06/2016.
 */
public interface Client {
    Connection getConnection();
    void connect();
    void connect(final ConnectionEventListener eventListener, ConnectionState... connectionStates);
    void disconnect();
    Channel subscribe(final String channelName);
    Channel subscribe(final String channelName, final ChannelEventListener listener, final String... eventNames);
    PrivateChannel subscribePrivate(final String channelName);
    PrivateChannel subscribePrivate(final String channelName, final PrivateChannelEventListener listener,
                                    final String... eventNames);
    PresenceChannel subscribePresence(final String channelName);
    PresenceChannel subscribePresence(final String channelName, final PresenceChannelEventListener listener,
                                      final String... eventNames);
    void unsubscribe(final String channelName);
    Channel getChannel(String channelName);
    PrivateChannel getPrivateChannel(String channelName);
    PresenceChannel getPresenceChannel(String channelName);
}
