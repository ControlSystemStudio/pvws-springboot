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
import org.phoebus.pvws.model.ApplicationClientPvsMessage;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SubscribeUnsubscribeWebSocketHandler extends TextWebSocketHandler {

    private final CountDownLatch latch;

    private final AtomicReference<String> message1;

    private final AtomicReference<String> message2;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean firstValueMessageReceived = false;

    private final ApplicationClientMessage listMessage = new ApplicationClientMessage("list");

    public SubscribeUnsubscribeWebSocketHandler(AtomicReference<String> message1,
                                                AtomicReference<String> message2,
                                                CountDownLatch latch){
        this.message1 = message1;
        this.message2 = message2;
        this.latch = latch;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        ApplicationClientMessage applicationClientMessage =
                new ApplicationClientPvsMessage("subscribe", List.of("sim://sine"));
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(applicationClientMessage)));
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        // Send message only if it contains a value. That way we can detect if the message
        // originates from an actual PV (e.g. sim://sine).
        if(message.getPayload().contains("value") && !firstValueMessageReceived){
            firstValueMessageReceived = true; // Send list message only once
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(listMessage)));
        }
        else if(message.getPayload().contains("list")){
            if(message.getPayload().contains("sim://sine")){
                message1.set(message.getPayload());
                ApplicationClientMessage unsubscribeMessage =
                        new ApplicationClientPvsMessage("clear", List.of("sim://sine"));
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(unsubscribeMessage)));
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(listMessage)));
            }
            else{
                message2.set(message.getPayload());
                latch.countDown();
            }
        }
    }
}
