/*
 * Copyright (C) 2023 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.phoebus.pvws.ws;

import org.junit.jupiter.api.Test;
import org.phoebus.pvws.EpicsWebSocketServerApplication;
import org.phoebus.pvws.ws.clientconfig.*;
import org.phoebus.pvws.ws.handlers.SubscribeUnsubscribeWebSocketHandler;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing websockets in Spring Boot is a bit painful. Unlike Spring MVC tests - where the Spring test framework
 * &quot;emulates&quot; the server - the tests in this class need to actually start the service and then launch a websocket
 * client to send messages.
 * <p>
 * The pattern used in these test cases is stolen from
 * <a href="https://github.com/spring-projects/spring-boot/tree/main/spring-boot-tests/spring-boot-smoke-tests/spring-boot-smoke-test-websocket-tomcat">Spring Boot Smoke Tests</a>.
 * </p>
 *
 * <p>The websocket clients participating in the test cases are implemented in separate classes, see
 * for instance {@link SubscribeUnsubscribeWebSocketHandler}.</p>
 */
@SpringBootTest(classes = EpicsWebSocketServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTest {

    @LocalServerPort
    private int port = 1234;

    /**
     * Tests only connection, i.e. no payload
     */
    @Test
    void testConnectOnly() {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ConnectionOnlyClientConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + port + "/pvws/pv")
                .run("--spring.main.web-application-type=none");
        long count = context.getBean(ConnectionOnlyClientConfiguration.class).latch.getCount();
        context.close();
        assertThat(count).isZero();
    }

    @Test
    public void testSubscribeInvalidPV() {
        Map<String, Object> clientMessageParams = new HashMap<>();
        clientMessageParams.put("type", "subscribe");
        clientMessageParams.put("pvs", List.of("sim://invalid"));
        ConfigurableApplicationContext context = new SpringApplicationBuilder(SubscribeClientConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + port + "/pvws/pv")
                .properties(clientMessageParams)
                .run("--spring.main.web-application-type=none");
        long count = context.getBean(SubscribeClientConfiguration.class).latch.getCount();
        AtomicReference<String> messagePayloadReference = context.getBean(SubscribeClientConfiguration.class).messagePayload;
        context.close();
        assertThat(count).isEqualTo(1);
        // Make sure the service response is empty
        assertNull(messagePayloadReference.get());
    }

    @Test
    public void testInvalidMessageType() {
        Map<String, Object> clientMessageParams = new HashMap<>();
        clientMessageParams.put("type", "invalid");
        clientMessageParams.put("pvs", List.of("sim://invalid"));
        ConfigurableApplicationContext context = new SpringApplicationBuilder(SubscribeClientConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + port + "/pvws/pv")
                .properties(clientMessageParams)
                .run("--spring.main.web-application-type=none");
        long count = context.getBean(SubscribeClientConfiguration.class).latch.getCount();
        AtomicReference<String> messagePayloadReference = context.getBean(SubscribeClientConfiguration.class).messagePayload;
        context.close();
        assertThat(count).isEqualTo(1);
        // Make sure the service response is empty
        assertNull(messagePayloadReference.get());
    }

    @Test
    public void testSubscribeUnsubscribe() {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(SubscribeUnsubscribeClientConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + port + "/pvws/pv")
                .run("--spring.main.web-application-type=none");
        long count = context.getBean(SubscribeUnsubscribeClientConfiguration.class).latch.getCount();
        AtomicReference<String> messagePayloadReference1 = context.getBean(SubscribeUnsubscribeClientConfiguration.class).message1;
        AtomicReference<String> messagePayloadReference2 = context.getBean(SubscribeUnsubscribeClientConfiguration.class).message2;
        context.close();
        assertThat(count).isEqualTo(0);
        // First list response shall list only sim://sine
        assertEquals("{\"type\":\"list\",\"pvs\":[\"sim://sine\"]}", messagePayloadReference1.get());
        // Second list response shall list empty array since the client has sent an unsubscribe message
        assertEquals("{\"type\":\"list\",\"pvs\":[]}", messagePayloadReference2.get());
    }

    @Test
    public void testWrite() {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(WriteClientConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + port + "/pvws/pv")
                .run("--spring.main.web-application-type=none");
        long count = context.getBean(WriteClientConfiguration.class).latch.getCount();
        AtomicReference<String> messagePayloadReference1 = context.getBean(WriteClientConfiguration.class).message1;
        AtomicReference<String> messagePayloadReference2 = context.getBean(WriteClientConfiguration.class).message2;
        context.close();
        assertThat(count).isEqualTo(0);
        // First list response shall list only sim://sine
        assertTrue(messagePayloadReference1.get().contains("\"value\":0.0"));
        // Second list response shall list empty array since the client has sent an unsubscribe message
        assertTrue(messagePayloadReference2.get().contains("\"value\":7.0"));
    }

    @Test
    public void testPing() {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(PingClientConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + port + "/pvws/pv")
                .run("--spring.main.web-application-type=none");
        long count = context.getBean(PingClientConfiguration.class).latch.getCount();
        context.close();
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testEcho() {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(EchoClientConfiguration.class,
                PropertyPlaceholderAutoConfiguration.class)
                .properties("websocket.uri:ws://localhost:" + port + "/pvws/pv")
                .run("--spring.main.web-application-type=none");
        AtomicReference<String> messagePayloadReference = context.getBean(EchoClientConfiguration.class).messagePayload;
        long count = context.getBean(EchoClientConfiguration.class).latch.getCount();
        context.close();
        assertThat(count).isEqualTo(0);
        assertEquals("{\"type\":\"echo\",\"body\":\"Hello, world!\",\"foo\":\"bar\"}", messagePayloadReference.get());
    }
}
