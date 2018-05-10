package com.eventflit.client;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;

@RunWith(MockitoJUnitRunner.class)
public class EventflitOptionsTest {

    private static final String API_KEY = "4PI_K3Y";

    private EventflitOptions eventflitOptions;
    private @Mock Authorizer mockAuthorizer;

    @Before
    public void setUp() {
        eventflitOptions = new EventflitOptions();
    }

    @Test
    public void testEncryptedInitializedAsTrue() {
        assert eventflitOptions.isEncrypted();
    }

    @Test
    public void testAuthorizerIsInitiallyNull() {
        assertNull(eventflitOptions.getAuthorizer());
    }

    @Test
    public void testAuthorizerCanBeSet() {
        eventflitOptions.setAuthorizer(mockAuthorizer);
        assertSame(mockAuthorizer, eventflitOptions.getAuthorizer());
    }

    @Test
    public void testEncryptedCanBeSetToTrue() {
        eventflitOptions.setEncrypted(true);
        assertSame(true, eventflitOptions.isEncrypted());
    }

    @Test
    public void testSetAuthorizerReturnsSelf() {
        assertSame(eventflitOptions, eventflitOptions.setAuthorizer(mockAuthorizer));
    }

    @Test
    public void testSetEncryptedReturnsSelf() {
        assertSame(eventflitOptions, eventflitOptions.setEncrypted(true));
    }

    @Test
    public void testDefaultURL() {
        assertEquals(eventflitOptions.buildUrl(API_KEY), "wss://service.eventflit.com:443/app/" + API_KEY
                + "?client=java-client&protocol=5&version=" + EventflitOptions.LIB_VERSION);
    }

    @Test
    public void testNonSSLURLIsCorrect() {
        eventflitOptions.setEncrypted(false);
        assertEquals(eventflitOptions.buildUrl(API_KEY), "ws://service.eventflit.com:80/app/" + API_KEY
                + "?client=java-client&protocol=5&version=" + EventflitOptions.LIB_VERSION);
    }

    @Test
    public void testClusterSetURLIsCorrect() {
        eventflitOptions.setCluster("eu");
        assertEquals(eventflitOptions.buildUrl(API_KEY), "wss://ws-eu.eventflit.com:443/app/" + API_KEY
                + "?client=java-client&protocol=5&version=" + EventflitOptions.LIB_VERSION);
    }

    @Test
    public void testClusterSetNonSSLURLIsCorrect() {
        eventflitOptions.setCluster("eu").setEncrypted(false);
        assertEquals(eventflitOptions.buildUrl(API_KEY), "ws://ws-eu.eventflit.com:80/app/" + API_KEY
                + "?client=java-client&protocol=5&version=" + EventflitOptions.LIB_VERSION);
    }

    @Test
    public void testCustomHostAndPortURLIsCorrect() {
        eventflitOptions.setHost("subdomain.example.com").setWsPort(8080).setWssPort(8181);
        assertEquals(eventflitOptions.buildUrl(API_KEY), "wss://subdomain.example.com:8181/app/" + API_KEY
                + "?client=java-client&protocol=5&version=" + EventflitOptions.LIB_VERSION);
    }

    @Test
    public void testCustomHostAndPortNonSSLURLIsCorrect() {
        eventflitOptions.setHost("subdomain.example.com").setWsPort(8080).setWssPort(8181).setEncrypted(false);
        assertEquals(eventflitOptions.buildUrl(API_KEY), "ws://subdomain.example.com:8080/app/" + API_KEY
                + "?client=java-client&protocol=5&version=" + EventflitOptions.LIB_VERSION);
    }

    @Test
    public void testSetProxy(){
        Proxy newProxy = new Proxy( Proxy.Type.HTTP, new InetSocketAddress( "proxyaddress", 80 ) );
        eventflitOptions.setProxy(newProxy);
        assertEquals(eventflitOptions.getProxy(), newProxy);
    }

    @Test
    public void testGetProxyReturnDefaultProxy(){
        assertEquals(eventflitOptions.getProxy(), Proxy.NO_PROXY);
    }

}
