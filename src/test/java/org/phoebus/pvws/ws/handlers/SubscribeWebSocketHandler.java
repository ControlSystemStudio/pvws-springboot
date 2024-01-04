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

package org.phoebus.pvws.ws.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.phoebus.pvws.model.ApplicationClientMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SubscribeWebSocketHandler extends TextWebSocketHandler {


    private final CountDownLatch latch;

    private final AtomicReference<String> messagePayload;

    private final ApplicationClientMessage applicationClientMessage;

    public SubscribeWebSocketHandler(ApplicationClientMessage applicationClientMessage, AtomicReference<String> messagePayload, CountDownLatch latch) {
        this.applicationClientMessage = applicationClientMessage;
        this.latch = latch;
        this.messagePayload = messagePayload;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(applicationClientMessage)));

    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Send message only if it contains a value. That way we can detect if the message
        // originates from an actual PV (e.g. sim://sine).
        if(message.getPayload().contains("value")){
            messagePayload.set(message.getPayload());
            latch.countDown();
            session.close();
        }
    }

}
