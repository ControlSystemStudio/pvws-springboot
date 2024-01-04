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

import org.phoebus.pvws.model.ApplicationClientMessage;
import org.phoebus.pvws.model.ApplicationClientPvsMessage;
import org.phoebus.pvws.ws.handlers.SubscribeWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public class SubscribeClientConfiguration extends ClientConfigurationBase{

    @Value("${type}")
    private String type;

    @Value("${pvs}")
    private List<String> pvs;

    public final AtomicReference<String> messagePayload = new AtomicReference<>();

    @Override
    @Bean
    TextWebSocketHandler handler() {
        ApplicationClientMessage applicationClientMessage =
                new ApplicationClientPvsMessage(type, pvs);
        return new SubscribeWebSocketHandler(applicationClientMessage, messagePayload, latch);
    }
}
