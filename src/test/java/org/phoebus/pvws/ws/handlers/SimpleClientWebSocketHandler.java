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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleClientWebSocketHandler extends TextWebSocketHandler {

    protected Log logger = LogFactory.getLog(SimpleClientWebSocketHandler.class);

    private final CountDownLatch latch;

    private final AtomicReference<String> messagePayload;

    public SimpleClientWebSocketHandler(CountDownLatch latch, AtomicReference<String> message) {
        this.latch = latch;
        this.messagePayload = message;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        this.logger.info("Received: " + message + " (" + this.latch.getCount() + ")");
        session.close();
        this.messagePayload.set(message.getPayload());
        this.latch.countDown();
    }
}
