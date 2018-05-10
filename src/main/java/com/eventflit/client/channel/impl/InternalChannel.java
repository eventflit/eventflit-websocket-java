package com.eventflit.client.channel.impl;

import com.eventflit.client.channel.Channel;
import com.eventflit.client.channel.ChannelEventListener;
import com.eventflit.client.channel.ChannelState;

public interface InternalChannel extends Channel, Comparable<InternalChannel> {

    String toSubscribeMessage();

    String toUnsubscribeMessage();

    void onMessage(String event, String message);

    void updateState(ChannelState state);

    void setEventListener(ChannelEventListener listener);

    ChannelEventListener getEventListener();
}
