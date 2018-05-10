package com.eventflit.client.example;

import java.util.Set;

import com.eventflit.client.Eventflit;
import com.eventflit.client.EventflitOptions;
import com.eventflit.client.channel.PresenceChannel;
import com.eventflit.client.channel.PresenceChannelEventListener;
import com.eventflit.client.channel.User;
import com.eventflit.client.connection.ConnectionEventListener;
import com.eventflit.client.connection.ConnectionStateChange;
import com.eventflit.client.util.HttpAuthorizer;

public class PresenceChannelExampleApp implements ConnectionEventListener, PresenceChannelEventListener {

    private final Eventflit eventflit;
    private final String channelName;
    private final String eventName;

    private final PresenceChannel channel;

    public static void main(final String[] args) {
        new PresenceChannelExampleApp(args);
    }

    public PresenceChannelExampleApp(final String[] args) {

        final String apiKey = args.length > 0 ? args[0] : "a87fe72c6f36272aa4b1";
        channelName = args.length > 1 ? args[1] : "presence-my-channel";
        eventName = args.length > 2 ? args[2] : "my-event";

        final HttpAuthorizer authorizer = new HttpAuthorizer(
                "http://www.leggetter.co.uk/eventflit/eventflit-examples/php/authentication/src/presence_auth.php");
        final EventflitOptions options = new EventflitOptions().setAuthorizer(authorizer).setEncrypted(true);

        eventflit = new Eventflit(apiKey, options);
        eventflit.connect(this);

        channel = eventflit.subscribePresence(channelName, this, eventName);

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

        System.out.println(String.format("Connection state changed from [%s] to [%s]", change.getPreviousState(),
                change.getCurrentState()));
    }

    @Override
    public void onError(final String message, final String code, final Exception e) {

        System.out.println(String.format("An error was received with message [%s], code [%s], exception [%s]", message,
                code, e));
    }

    /* PresenceChannelEventListener implementation */

    @Override
    public void onUsersInformationReceived(final String channelName, final Set<User> users) {

        System.out.println("Received user information");

        printCurrentlySubscribedUsers();
    }

    @Override
    public void userSubscribed(final String channelName, final User user) {

        System.out.println(String.format("A new user has joined channel [%s]: %s", channelName, user.toString()));

        printCurrentlySubscribedUsers();
    }

    @Override
    public void userUnsubscribed(final String channelName, final User user) {

        System.out.println(String.format("A user has left channel [%s]: %s", channelName, user));

        printCurrentlySubscribedUsers();
    }

    @Override
    public void onEvent(final String channelName, final String eventName, final String data) {

        System.out.println(String.format("Received event [%s] on channel [%s] with data [%s]", eventName, channelName,
                data));
    }

    @Override
    public void onSubscriptionSucceeded(final String channelName) {

        System.out.println(String.format("Subscription to channel [%s] succeeded", channel.getName()));
    }

    @Override
    public void onAuthenticationFailure(final String message, final Exception e) {

        System.out.println(String.format("Authentication failure due to [%s], exception was [%s]", message, e));
    }

    private void printCurrentlySubscribedUsers() {
        final StringBuilder sb = new StringBuilder("Users now subscribed to the channel:");
        for (final User remainingUser : channel.getUsers()) {
            sb.append("\n\t");
            sb.append(remainingUser.toString());

            if (remainingUser.equals(channel.getMe())) {
                sb.append(" (me)");
            }
        }

        System.out.println(sb.toString());
    }
}
