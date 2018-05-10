package com.eventflit.client.example;

import java.util.Map;

import com.google.gson.Gson;

import com.eventflit.client.Eventflit;
import com.eventflit.client.EventflitOptions;
import com.eventflit.client.channel.ChannelEventListener;
import com.eventflit.client.connection.ConnectionEventListener;
import com.eventflit.client.connection.ConnectionStateChange;

public class ExampleApp implements ConnectionEventListener, ChannelEventListener {

    private final Eventflit eventflit;
    private final String channelName;
    private final String eventName;
    private final long startTime = System.currentTimeMillis();

    public static void main(final String[] args) {
        new ExampleApp(args);
    }

    public ExampleApp(final String[] args) {

        final String apiKey = args.length > 0 ? args[0] : "161717a55e65825bacf1";
        channelName = args.length > 1 ? args[1] : "my-channel";
        eventName = args.length > 2 ? args[2] : "my-event";

        final EventflitOptions options = new EventflitOptions().setEncrypted(true);
        eventflit = new Eventflit(apiKey, options);
        eventflit.connect(this);

        eventflit.subscribe(channelName, this, eventName);

        // Keep main thread asleep while we watch for events or application will
        // terminate
        while (true) {
            try {
                Thread.sleep(1000);
            }
            catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /* ConnectionEventListener implementation */

    @Override
    public void onConnectionStateChange(final ConnectionStateChange change) {

        System.out.println(String.format("[%d] Connection state changed from [%s] to [%s]", timestamp(),
                change.getPreviousState(), change.getCurrentState()));
    }

    @Override
    public void onError(final String message, final String code, final Exception e) {

        System.out.println(String.format("[%d] An error was received with message [%s], code [%s], exception [%s]",
                timestamp(), message, code, e));
    }

    /* ChannelEventListener implementation */

    @Override
    public void onEvent(final String channelName, final String eventName, final String data) {

        System.out.println(String.format("[%d] Received event [%s] on channel [%s] with data [%s]", timestamp(),
                eventName, channelName, data));

        final Gson gson = new Gson();
        @SuppressWarnings("unchecked")
        final Map<String, String> jsonObject = gson.fromJson(data, Map.class);
        System.out.println(jsonObject);
    }

    @Override
    public void onSubscriptionSucceeded(final String channelName) {

        System.out.println(String.format("[%d] Subscription to channel [%s] succeeded", timestamp(), channelName));
    }

    private long timestamp() {
        return System.currentTimeMillis() - startTime;
    }
}
