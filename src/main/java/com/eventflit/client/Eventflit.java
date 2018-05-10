package com.eventflit.client;

import com.eventflit.client.channel.Channel;
import com.eventflit.client.channel.ChannelEventListener;
import com.eventflit.client.channel.PresenceChannel;
import com.eventflit.client.channel.PresenceChannelEventListener;
import com.eventflit.client.channel.PrivateChannel;
import com.eventflit.client.channel.PrivateChannelEventListener;
import com.eventflit.client.channel.SubscriptionEventListener;
import com.eventflit.client.channel.impl.ChannelManager;
import com.eventflit.client.channel.impl.InternalChannel;
import com.eventflit.client.channel.impl.PresenceChannelImpl;
import com.eventflit.client.channel.impl.PrivateChannelImpl;
import com.eventflit.client.connection.Connection;
import com.eventflit.client.connection.ConnectionEventListener;
import com.eventflit.client.connection.ConnectionState;
import com.eventflit.client.connection.impl.InternalConnection;
import com.eventflit.client.util.Factory;

/**
 * This class is the main entry point for accessing Eventflit.
 *
 * <p>
 * By creating a new {@link Eventflit} instance and calling {@link
 * Eventflit#connect()} a connection to Eventflit is established.
 * </p>
 *
 * <p>
 * Subscriptions for data are represented by
 * {@link com.eventflit.client.channel.Channel} objects, or subclasses thereof.
 * Subscriptions are created by calling {@link Eventflit#subscribe(String)},
 * {@link Eventflit#subscribePrivate(String)},
 * {@link Eventflit#subscribePresence(String)} or one of the overloads.
 * </p>
 */
public class Eventflit implements Client {

    private final EventflitOptions eventflitOptions;
    private final InternalConnection connection;
    private final ChannelManager channelManager;
    private final Factory factory;

    /**
     * Creates a new instance of Eventflit.
     *
     * <p>
     * Note that if you use this constructor you will not be able to subscribe
     * to private or presence channels because no {@link Authorizer} has been
     * set. If you want to use private or presence channels:
     * <ul>
     * <li>Create an implementation of the {@link Authorizer} interface, or use
     * the {@link com.eventflit.client.util.HttpAuthorizer} provided.</li>
     * <li>Create an instance of {@link EventflitOptions} and set the authorizer on
     * it by calling {@link EventflitOptions#setAuthorizer(Authorizer)}.</li>
     * <li>Use the {@link #Eventflit(String, EventflitOptions)} constructor to create
     * an instance of Eventflit.</li>
     * </ul>
     *
     * <p>
     * The {@link com.eventflit.client.example.PrivateChannelExampleApp} and
     * {@link com.eventflit.client.example.PresenceChannelExampleApp} example
     * applications show how to do this.
     * </p>
     *
     * @param apiKey
     *            Your Eventflit API key.
     */
    public Eventflit(final String apiKey) {

        this(apiKey, new EventflitOptions());
    }

    /**
     * Creates a new instance of Eventflit.
     *
     * @param apiKey
     *            Your Eventflit API key.
     * @param eventflitOptions
     *            Options for the Eventflit client library to use.
     */
    public Eventflit(final String apiKey, final EventflitOptions eventflitOptions) {

        this(apiKey, eventflitOptions, new Factory());
    }

    /**
     * Creates a new Eventflit instance using the provided Factory, package level
     * access for unit tests only.
     */
    Eventflit(final String apiKey, final EventflitOptions eventflitOptions, final Factory factory) {

        if (apiKey == null || apiKey.length() == 0) {
            throw new IllegalArgumentException("API Key cannot be null or empty");
        }

        if (eventflitOptions == null) {
            throw new IllegalArgumentException("EventflitOptions cannot be null");
        }

        this.eventflitOptions = eventflitOptions;
        this.factory = factory;
        connection = factory.getConnection(apiKey, this.eventflitOptions);
        channelManager = factory.getChannelManager();
        channelManager.setConnection(connection);
    }

    /* Connection methods */

    /**
     * Gets the underlying {@link Connection} object that is being used by this
     * instance of {@linkplain Eventflit}.
     *
     * @return The {@link Connection} object.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Connects to Eventflit. Any {@link ConnectionEventListener}s that have
     * already been registered using the
     * {@link Connection#bind(ConnectionState, ConnectionEventListener)} method
     * will receive connection events.
     *
     * <p>Calls are ignored (a connection is not attempted) if the {@link Connection#getState()} is not {@link com.eventflit.client.connection.ConnectionState#DISCONNECTED}.</p>
     */
    public void connect() {
        connect(null);
    }

    /**
     * Binds a {@link ConnectionEventListener} to the specified events and then
     * connects to Eventflit. This is equivalent to binding a
     * {@link ConnectionEventListener} using the
     * {@link Connection#bind(ConnectionState, ConnectionEventListener)} method
     * before connecting.
     *
     <p>Calls are ignored (a connection is not attempted) if the {@link Connection#getState()} is not {@link com.eventflit.client.connection.ConnectionState#DISCONNECTED}.</p>
     *
     * @param eventListener
     *            A {@link ConnectionEventListener} that will receive connection
     *            events. This can be null if you are not interested in
     *            receiving connection events, in which case you should call
     *            {@link #connect()} instead of this method.
     * @param connectionStates
     *            An optional list of {@link ConnectionState}s to bind your
     *            {@link ConnectionEventListener} to before connecting to
     *            Eventflit. If you do not specify any {@link ConnectionState}s
     *            then your {@link ConnectionEventListener} will be bound to all
     *            connection events. This is equivalent to calling
     *            {@link #connect(ConnectionEventListener, ConnectionState...)}
     *            with {@link ConnectionState#ALL}.
     * @throws IllegalArgumentException
     *             If the {@link ConnectionEventListener} is null and at least
     *             one connection state has been specified.
     */
    public void connect(final ConnectionEventListener eventListener, ConnectionState... connectionStates) {

        if (eventListener != null) {
            if (connectionStates.length == 0) {
                connectionStates = new ConnectionState[] { ConnectionState.ALL };
            }

            for (final ConnectionState state : connectionStates) {
                connection.bind(state, eventListener);
            }
        }
        else {
            if (connectionStates.length > 0) {
                throw new IllegalArgumentException(
                        "Cannot bind to connection states with a null connection event listener");
            }
        }

        connection.connect();
    }

    /**
     * Disconnect from Eventflit.
     *
     * <p>
     * Calls are ignored if the {@link Connection#getState()}, retrieved from {@link Eventflit#getConnection}, is not
     * {@link com.eventflit.client.connection.ConnectionState#CONNECTED}.
     * </p>
     */
    public void disconnect() {
        if (connection.getState() == ConnectionState.CONNECTED) {
            connection.disconnect();
        }
    }

    /* Subscription methods */

    /**
     * Subscribes to a public {@link Channel}.
     *
     * Note that subscriptions should be registered only once with a Eventflit
     * instance. Subscriptions are persisted over disconnection and
     * re-registered with the server automatically on reconnection. This means
     * that subscriptions may also be registered before connect() is called,
     * they will be initiated on connection.
     *
     * @param channelName
     *            The name of the {@link Channel} to subscribe to.
     * @return The {@link Channel} object representing your subscription.
     */
    public Channel subscribe(final String channelName) {
        return subscribe(channelName, null);
    }

    /**
     * Binds a {@link ChannelEventListener} to the specified events and then
     * subscribes to a public {@link Channel}.
     *
     * @param channelName
     *            The name of the {@link Channel} to subscribe to.
     * @param listener
     *            A {@link ChannelEventListener} to receive events. This can be
     *            null if you don't want to bind a listener at subscription
     *            time, in which case you should call {@link #subscribe(String)}
     *            instead of this method.
     * @param eventNames
     *            An optional list of event names to bind your
     *            {@link ChannelEventListener} to before subscribing.
     * @return The {@link Channel} object representing your subscription.
     * @throws IllegalArgumentException
     *             If any of the following are true:
     *             <ul>
     *             <li>The channel name is null.</li>
     *             <li>You are already subscribed to this channel.</li>
     *             <li>The channel name starts with "private-". If you want to
     *             subscribe to a private channel, call
     *             {@link #subscribePrivate(String, PrivateChannelEventListener, String...)}
     *             instead of this method.</li>
     *             <li>At least one of the specified event names is null.</li>
     *             <li>You have specified at least one event name and your
     *             {@link ChannelEventListener} is null.</li>
     *             </ul>
     */
    public Channel subscribe(final String channelName, final ChannelEventListener listener, final String... eventNames) {

        final InternalChannel channel = factory.newPublicChannel(channelName);
        channelManager.subscribeTo(channel, listener, eventNames);

        return channel;
    }

    /**
     * Subscribes to a {@link com.eventflit.client.channel.PrivateChannel} which
     * requires authentication.
     *
     * @param channelName
     *            The name of the channel to subscribe to.
     * @return A new {@link com.eventflit.client.channel.PrivateChannel}
     *         representing the subscription.
     * @throws IllegalStateException
     *             if a {@link com.eventflit.client.Authorizer} has not been set
     *             for the {@link Eventflit} instance via
     *             {@link #Eventflit(String, EventflitOptions)}.
     */
    public PrivateChannel subscribePrivate(final String channelName) {
        return subscribePrivate(channelName, null);
    }

    /**
     * Subscribes to a {@link com.eventflit.client.channel.PrivateChannel} which
     * requires authentication.
     *
     * @param channelName The name of the channel to subscribe to.
     * @param listener A listener to be informed of both Eventflit channel protocol events and subscription data events.
     * @param eventNames An optional list of names of events to be bound to on the channel. The equivalent of calling {@link com.eventflit.client.channel.Channel#bind(String, SubscriptionEventListener)} one or more times.
     * @return A new {@link com.eventflit.client.channel.PrivateChannel} representing the subscription.
     * @throws IllegalStateException if a {@link com.eventflit.client.Authorizer} has not been set for the {@link Eventflit} instance via {@link #Eventflit(String, EventflitOptions)}.
     */
    public PrivateChannel subscribePrivate(final String channelName, final PrivateChannelEventListener listener,
            final String... eventNames) {

        throwExceptionIfNoAuthorizerHasBeenSet();

        final PrivateChannelImpl channel = factory.newPrivateChannel(connection, channelName,
                eventflitOptions.getAuthorizer());
        channelManager.subscribeTo(channel, listener, eventNames);

        return channel;
    }

    /**
     * Subscribes to a {@link com.eventflit.client.channel.PresenceChannel} which
     * requires authentication.
     *
     * @param channelName
     *            The name of the channel to subscribe to.
     * @return A new {@link com.eventflit.client.channel.PresenceChannel}
     *         representing the subscription.
     * @throws IllegalStateException
     *             if a {@link com.eventflit.client.Authorizer} has not been set
     *             for the {@link Eventflit} instance via
     *             {@link #Eventflit(String, EventflitOptions)}.
     */
    public PresenceChannel subscribePresence(final String channelName) {
        return subscribePresence(channelName, null);
    }

    /**
     * Subscribes to a {@link com.eventflit.client.channel.PresenceChannel} which
     * requires authentication.
     *
     * @param channelName The name of the channel to subscribe to.
     * @param listener A listener to be informed of Eventflit channel protocol, including presence-specific events, and subscription data events.
     * @param eventNames An optional list of names of events to be bound to on the channel. The equivalent of calling {@link com.eventflit.client.channel.Channel#bind(String, SubscriptionEventListener)} one or more times.
     * @return A new {@link com.eventflit.client.channel.PresenceChannel} representing the subscription.
     * @throws IllegalStateException if a {@link com.eventflit.client.Authorizer} has not been set for the {@link Eventflit} instance via {@link #Eventflit(String, EventflitOptions)}.
     */
    public PresenceChannel subscribePresence(final String channelName, final PresenceChannelEventListener listener,
            final String... eventNames) {

        throwExceptionIfNoAuthorizerHasBeenSet();

        final PresenceChannelImpl channel = factory.newPresenceChannel(connection, channelName,
                eventflitOptions.getAuthorizer());
        channelManager.subscribeTo(channel, listener, eventNames);

        return channel;
    }

    /**
     * Unsubscribes from a channel using via the name of the channel.
     *
     * @param channelName
     *            the name of the channel to be unsubscribed from.
     */
    public void unsubscribe(final String channelName) {

        channelManager.unsubscribeFrom(channelName);
    }

    /* implementation detail */

    private void throwExceptionIfNoAuthorizerHasBeenSet() {
        if (eventflitOptions.getAuthorizer() == null) {
            throw new IllegalStateException(
                    "Cannot subscribe to a private or presence channel because no Authorizer has been set. Call EventflitOptions.setAuthorizer() before connecting to Eventflit");
        }
    }

    /**
     *
     * @param channelName The name of the public channel to be retrieved
     * @return A public channel, or null if it could not be found
     * @throws IllegalArgumentException if you try to retrieve a private or presence channel.
     */
    public Channel getChannel(String channelName){
        return channelManager.getChannel(channelName);
    }

    /**
     *
     * @param channelName The name of the private channel to be retrieved
     * @return A private channel, or null if it could not be found
     * @throws IllegalArgumentException if you try to retrieve a public or presence channel.
     */
    public PrivateChannel getPrivateChannel(String channelName){
        return channelManager.getPrivateChannel(channelName);
    }

    /**
     *
     * @param channelName The name of the presence channel to be retrieved
     * @return A presence channel, or null if it could not be found
     * @throws IllegalArgumentException if you try to retrieve a public or private channel.
     */
    public PresenceChannel getPresenceChannel(String channelName){
        return channelManager.getPresenceChannel(channelName);
    }

}
