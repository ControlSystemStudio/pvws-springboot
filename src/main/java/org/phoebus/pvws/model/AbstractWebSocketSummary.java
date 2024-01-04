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

package org.phoebus.pvws.model;

import org.phoebus.pvws.ws.WebSocket;

public abstract class AbstractWebSocketSummary {
    private String id;
    private Long created;
    private Long last_client_message;
    private Long last_message_sent;
    private Integer queued;

    public AbstractWebSocketSummary(WebSocket webSocket){
        this.id = webSocket.getId();
        this.created = webSocket.getCreateTime();
        this.last_client_message = webSocket.getLastClientMessage();
        this.last_message_sent = webSocket.getLastMessageSent();
        this.queued = webSocket.getQueuedMessageCount();
    }

    public String getId() {
        return id;
    }

    public Long getCreated() {
        return created;
    }

    public Long getLast_client_message() {
        return last_client_message;
    }

    public Long getLast_message_sent() {
        return last_message_sent;
    }

    public Integer getQueued() {
        return queued;
    }
}
