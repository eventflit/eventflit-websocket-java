package com.eventflit.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.eventflit.client.channel.ChannelEventListener;
import com.eventflit.client.channel.PresenceChannelEventListener;
import com.eventflit.client.channel.PrivateChannelEventListener;
import com.eventflit.client.channel.impl.ChannelImpl;
import com.eventflit.client.channel.impl.ChannelManager;
import com.eventflit.client.channel.impl.PresenceChannelImpl;
import com.eventflit.client.channel.impl.PrivateChannelImpl;
import com.eventflit.client.connection.ConnectionEventListener;
import com.eventflit.client.connection.ConnectionState;
import com.eventflit.client.connection.impl.InternalConnection;
import com.eventflit.client.util.Factory;
import com.eventflit.client.util.HttpAuthorizer;

@RunWith(MockitoJUnitRunner.class)
public class EventflitTest {

    private static final String API_KEY = "123456";
    private static final String PUBLIC_CHANNEL_NAME = "my-channel";
    private static final String PRIVATE_CHANNEL_NAME = "private-my-channel";
    private static final String PRESENCE_CHANNEL_NAME = "presence-my-channel";

    private Eventflit eventflit;
    private EventflitOptions options;
    private Authorizer authorizer;
    private @Mock InternalConnection mockConnection;
    private @Mock ChannelManager mockChannelManager;
    private @Mock ConnectionEventListener mockConnectionEventListener;
    private @Mock ChannelImpl mockPublicChannel;
    private @Mock PrivateChannelImpl mockPrivateChannel;
    private @Mock PresenceChannelImpl mockPresenceChannel;
    private @Mock ChannelEventListener mockChannelEventListener;
    private @Mock PrivateChannelEventListener mockPrivateChannelEventListener;
    private @Mock PresenceChannelEventListener mockPresenceChannelEventListener;
    private @Mock Factory factory;

    @Before
    public void setUp() {
        authorizer = new HttpAuthorizer("http://www.example.com");
        options = new EventflitOptions().setAuthorizer(authorizer);

        when(factory.getConnection(eq(API_KEY), any(EventflitOptions.class))).thenReturn(mockConnection);
        when(factory.getChannelManager()).thenReturn(mockChannelManager);
        when(factory.newPublicChannel(PUBLIC_CHANNEL_NAME)).thenReturn(mockPublicChannel);
        when(factory.newPrivateChannel(mockConnection, PRIVATE_CHANNEL_NAME, authorizer))
                .thenReturn(mockPrivateChannel);
        when(factory.newPresenceChannel(mockConnection, PRESENCE_CHANNEL_NAME, authorizer)).thenReturn(
                mockPresenceChannel);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }
        }).when(factory).queueOnEventThread(any(Runnable.class));
        eventflit = new Eventflit(API_KEY, options, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullAPIKeyThrowsIllegalArgumentException() {
        new Eventflit(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyAPIKeyThrowsIllegalArgumentException() {
        new Eventflit("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEventflitOptionsThrowsIllegalArgumentException() {
        new Eventflit(API_KEY, null);
    }

    @Test
    public void testCreatesConnectionObjectWhenConstructed() {
        assertNotNull(eventflit.getConnection());
        assertSame(mockConnection, eventflit.getConnection());
    }

    @Test
    public void testConnectCallWithNoListenerIsDelegatedToUnderlyingConnection() {
        eventflit.connect();
        verify(mockConnection).connect();
    }

    @Test
    public void testDisconnectCallIsDelegatedToUnderlyingConnection() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);

        eventflit.disconnect();
        verify(mockConnection).disconnect();
    }

    @Test
    public void testDisconnectCallDoesNothingIfStateIsDisconnected() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        eventflit.disconnect();
        verify(mockConnection, never()).disconnect();
    }

    @Test
    public void testDisconnectCallDoesNothingIfStateIsConnecting() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);

        eventflit.disconnect();
        verify(mockConnection, never()).disconnect();
    }

    @Test
    public void testDisconnectCallDoesNothingIfStateIsDisconnecting() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTING);

        eventflit.disconnect();
        verify(mockConnection, never()).disconnect();
    }

    @Test
    public void testConnectCallWithListenerAndEventsBindsListenerToEventsBeforeConnecting() {
        eventflit.connect(mockConnectionEventListener, ConnectionState.CONNECTED, ConnectionState.DISCONNECTED);

        verify(mockConnection).bind(ConnectionState.CONNECTED, mockConnectionEventListener);
        verify(mockConnection).bind(ConnectionState.DISCONNECTED, mockConnectionEventListener);
        verify(mockConnection).connect();
    }

    @Test
    public void testConnectCallWithListenerAndNoEventsBindsListenerToAllEventsBeforeConnecting() {
        eventflit.connect(mockConnectionEventListener);

        verify(mockConnection).bind(ConnectionState.ALL, mockConnectionEventListener);
        verify(mockConnection).connect();
    }

    @Test
    public void testConnectCallWithNullListenerAndNoEventsJustConnectsWithoutBinding() {
        eventflit.connect(null);

        verify(mockConnection, never()).bind(any(ConnectionState.class), any(ConnectionEventListener.class));
        verify(mockConnection).connect();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectCallWithNullListenerAndEventsThrowsException() {
        eventflit.connect(null, ConnectionState.CONNECTED);
    }

    @Test
    public void testSubscribeWithoutListenerCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.subscribe(PUBLIC_CHANNEL_NAME);

        verify(mockChannelManager).subscribeTo(mockPublicChannel, null);
    }

    @Test
    public void testSubscribeCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPublicChannel, mockChannelEventListener);
    }

    @Test
    public void testSubscribeWithEventNamesCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener, "event1", "event2");

        verify(mockChannelManager).subscribeTo(mockPublicChannel, mockChannelEventListener, "event1", "event2");
    }

    @Test
    public void testSubscribeWhenConnectingCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);

        eventflit.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPublicChannel, mockChannelEventListener);
    }

    @Test
    public void testSubscribeWhenDisconnectedCreatesPublicChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        eventflit.subscribe(PUBLIC_CHANNEL_NAME, mockChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPublicChannel, mockChannelEventListener);
    }

    @Test
    public void testSubscribePresenceCreatesPresenceChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.subscribePresence(PRESENCE_CHANNEL_NAME, mockPresenceChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPresenceChannel, mockPresenceChannelEventListener);
    }

    @Test
    public void testSubscribePresenceWithEventNamesCreatesPresenceChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.subscribePresence(PRESENCE_CHANNEL_NAME, mockPresenceChannelEventListener, "event1", "event2");

        verify(mockChannelManager).subscribeTo(mockPresenceChannel, mockPresenceChannelEventListener, "event1",
                "event2");
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePresenceIfNoEventflitOptionsHaveBeenPassedThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit = new Eventflit(API_KEY);

        eventflit.subscribePresence(PRESENCE_CHANNEL_NAME, mockPresenceChannelEventListener);
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePresenceIfEventflitOptionsHaveBeenPassedButNoAuthorizerHasBeenSetThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        options.setAuthorizer(null);
        eventflit = new Eventflit(API_KEY, options);

        eventflit.subscribePresence(PRESENCE_CHANNEL_NAME, mockPresenceChannelEventListener);
    }

    @Test
    public void testSubscribePresenceWhenConnectingCreatesPresenceChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);

        eventflit.subscribePresence(PRESENCE_CHANNEL_NAME, mockPresenceChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPresenceChannel, mockPresenceChannelEventListener);
    }

    @Test
    public void testSubscribePresenceWhenDisconnectedCreatesPresenceChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        eventflit.subscribePresence(PRESENCE_CHANNEL_NAME, mockPresenceChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPresenceChannel, mockPresenceChannelEventListener);
    }

    @Test
    public void testSubscribePrivateCreatesPrivateChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.subscribePrivate(PRIVATE_CHANNEL_NAME, mockPrivateChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
    }

    @Test
    public void testSubscribePrivateWithEventNamesCreatesPrivateChannelAndDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.subscribePrivate(PRIVATE_CHANNEL_NAME, mockPrivateChannelEventListener, "event1", "event2");

        verify(mockChannelManager).subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener, "event1", "event2");
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePrivateIfNoEventflitOptionsHaveBeenPassedThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit = new Eventflit(API_KEY);

        eventflit.subscribePrivate(PRIVATE_CHANNEL_NAME, mockPrivateChannelEventListener);
    }

    @Test(expected = IllegalStateException.class)
    public void testSubscribePrivateIfEventflitOptionsHaveBeenPassedButNoAuthorizerHasBeenSetThrowsException() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        options.setAuthorizer(null);
        eventflit = new Eventflit(API_KEY, options);

        eventflit.subscribePrivate(PRIVATE_CHANNEL_NAME, mockPrivateChannelEventListener);
    }

    @Test
    public void testSubscribePrivateWhenConnectingCreatesPrivateChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTING);

        eventflit.subscribePrivate(PRIVATE_CHANNEL_NAME, mockPrivateChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
    }

    @Test
    public void testSubscribePrivateWhenDisconnectedCreatesPrivateChannelAndDelegatesCallToChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.DISCONNECTED);

        eventflit.subscribePrivate(PRIVATE_CHANNEL_NAME, mockPrivateChannelEventListener);

        verify(mockChannelManager).subscribeTo(mockPrivateChannel, mockPrivateChannelEventListener);
    }

    @Test
    public void testUnsubscribeDelegatesCallToTheChannelManager() {
        when(mockConnection.getState()).thenReturn(ConnectionState.CONNECTED);
        eventflit.unsubscribe(PUBLIC_CHANNEL_NAME);
        verify(mockChannelManager).unsubscribeFrom(PUBLIC_CHANNEL_NAME);
    }

}
