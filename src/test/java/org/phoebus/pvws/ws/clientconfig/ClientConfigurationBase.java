/*
 * Copyright (C) 2024 European Spallation Source ERIC.
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

package org.phoebus.pvws.ws.clientconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Configuration(proxyBeanMethods = false)
public abstract class ClientConfigurationBase implements CommandLineRunner {

    public CountDownLatch latch = new CountDownLatch(1);

    @Value("${websocket.uri}")
    private String webSocketUri;

    @Override
    public void run(String... args) throws Exception {
        if (!this.latch.await(3, TimeUnit.SECONDS)) {
            System.out.println("Response not received");
        }
    }

    @Bean
    WebSocketConnectionManager wsConnectionManager() {
        WebSocketConnectionManager manager = new WebSocketConnectionManager(client(), handler(), this.webSocketUri);
        manager.setAutoStartup(true);
        return manager;
    }

    @Bean
    StandardWebSocketClient client() {
        return new StandardWebSocketClient();
    }

    @Bean
    TextWebSocketHandler handler() {
        return new TextWebSocketHandler();
    }
}
