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
import org.phoebus.pvws.model.ApplicationClientWriteMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WriteWebSocketHandler extends TextWebSocketHandler {

    private final CountDownLatch latch;

    private final AtomicReference<String> valueMessage1;

    private final AtomicReference<String> valueMessage2;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicInteger counter = new AtomicInteger();

    public WriteWebSocketHandler(AtomicReference valueMessage1,
                                 AtomicReference valueMessage2,
                                 CountDownLatch latch) {
        this.valueMessage1 = valueMessage1;
        this.valueMessage2 = valueMessage2;
        this.latch = latch;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        ApplicationClientMessage applicationClientMessage =
                new ApplicationClientPvsMessage("subscribe", List.of("loc://x"));
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(applicationClientMessage)));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Send message only if it contains a value. That way we can detect if the message
        // originates from an actual PV (e.g. sim://sine).
        if (message.getPayload().contains("value")) {
            if (counter.getAndIncrement() < 2) {
                valueMessage1.set(message.getPayload());
                ApplicationClientWriteMessage applicationClientWriteMessage =
                        new ApplicationClientWriteMessage();
                applicationClientWriteMessage.setPv("loc://x");
                applicationClientWriteMessage.setValue(7);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(applicationClientWriteMessage)));
            } else {
                valueMessage2.set(message.getPayload());
                latch.countDown();
            }
        }
    }
}
