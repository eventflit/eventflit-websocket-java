package com.eventflit.client.channel;

/**
 * Represents a subscription to a private channel.
 */
public interface PrivateChannel extends Channel {

    /**
     * Once subscribed it is possible to trigger client events on a private
     * channel as long as client events have been activated for the a Eventflit
     * application. There are a number of restrictions enforced with client
     * events. For full details see the <a
     * href="http://eventflit.com/docs/client_events">client events
     * documentation</a>.
     *
     * @param eventName
     *            The name of the event to trigger. It must have a
     *            <code>client-</code> prefix.
     * @param data
     *            The data to be triggered with the event.
     */
    void trigger(String eventName, String data);
}
