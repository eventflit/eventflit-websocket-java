package com.eventflit.client.channel;

import java.util.Set;

/**
 * An object that represents a Eventflit presence channel. An implementation of
 * this interface is returned when you call
 * {@link com.eventflit.client.Eventflit#subscribePresence(String)} or
 * {@link com.eventflit.client.Eventflit#subscribePresence(String, PresenceChannelEventListener, String...)}
 * .
 */
public interface PresenceChannel extends PrivateChannel {

    /**
     * Gets a set of users currently subscribed to the channel.
     *
     * @return The users.
     */
    Set<User> getUsers();

    /**
     * Gets the user that represents the currently connected client.
     *
     * @return A user.
     */
    User getMe();
}
