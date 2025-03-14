/*******************************************************************************
 * Copyright (c) 2019-2022 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the LICENSE
 * which accompanies this distribution
 ******************************************************************************/
package org.phoebus.pvws.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.epics.vtype.VType;
import org.phoebus.pvws.model.ErrorMessage;
import org.phoebus.pvws.model.PvList;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web socket, handles {@link WebSocketPV}s for one web client
 *
 * @author Kay Kasemir
 */
@SuppressWarnings("nls")
public class WebSocket {
    /**
     * Time when web socket was created
     */
    private final long created = System.currentTimeMillis();

    /**
     * Track when the last message was received by web client
     */
    private volatile long last_client_message = 0;

    /**
     * Track when the last message was sent to web client
     */
    private volatile long last_message_sent = 0;

    /**
     * Is the queue full?
     */
    private final AtomicBoolean stuffed = new AtomicBoolean();

    /**
     * Queue of messages for the client.
     *
     * <p>Multiple threads concurrently writing to the socket results in
     * IllegalStateException "remote endpoint was in state [TEXT_FULL_WRITING]"
     * All writes are thus performed by just one thread off this queue.
     */
    private final ArrayBlockingQueue<String> write_queue = new ArrayBlockingQueue<>(2048);

    private static final String EXIT_MESSAGE = "EXIT";

    private volatile WebSocketSession session;
    private volatile String id;

    /**
     * Map of PV name to PV
     */
    private final ConcurrentHashMap<String, WebSocketPV> pvs = new ConcurrentHashMap<>();

    private final Logger logger = Logger.getLogger(WebSocket.class.getName());

    private final ObjectMapper objectMapper;

    /**
     * Constructor
     */
    public WebSocket(ObjectMapper objectMapper, WebSocketSession webSocketSession) {
        this.session = webSocketSession;
        logger.log(Level.FINE, () -> "Opening web socket " + session.getUri() + " ID " + session.getId());
        this.objectMapper = objectMapper;
        this.id = webSocketSession.getId();
        Thread write_thread = new Thread(this::writeQueuedMessages, "PVWS Write Thread");
        write_thread.setName("PVWS Write Thread " + this.id);
        write_thread.setDaemon(true);
        write_thread.start();
        trackClientUpdate();
    }

    /**
     * @return Session ID
     */
    public String getId() {
        if (session == null)
            return "(" + id + ")";
        else
            return id;
    }

    /**
     * @return Timestamp (ms since epoch) when socket was created
     */
    public long getCreateTime() {
        return created;
    }

    /**
     * @return Timestamp (ms since epoch) of last client message
     */
    public long getLastClientMessage() {
        return last_client_message;
    }

    /**
     * @return Timestamp (ms since epoch) of last message sent to client
     */
    public long getLastMessageSent() {
        return last_message_sent;
    }

    /**
     * @return {@link WebSocketPV}s
     */
    public Collection<WebSocketPV> getPVs() {
        return Collections.unmodifiableCollection(pvs.values());
    }

    /**
     * @return Number of queued messages
     */
    public int getQueuedMessageCount() {
        return write_queue.size();
    }

    /**
     * @param message Potentially long message
     * @return Message shorted to 200 chars
     */
    private String shorten(final String message) {
        if (message == null || message.length() < 200)
            return message;
        return message.substring(0, 200) + " ...";
    }

    private void queueMessage(final String message) {
        // Ignore messages after 'dispose'
        if (session == null)
            return;

        if (write_queue.offer(message)) {   // Queued OK. Is this a recovery from stuffed queue?
            if (stuffed.getAndSet(false))
                logger.log(Level.WARNING, () -> "Un-stuffed message queue for " + id);
        } else {   // Log, but only for the first message to prevent flooding the log
            if (!stuffed.getAndSet(true))
                logger.log(Level.WARNING, () -> "Cannot queue message '" + shorten(message) + "' for " + id);
        }
    }

    private void writeQueuedMessages() {
        try {
            while (true) {
                final String message;
                try {
                    message = write_queue.take();
                } catch (final InterruptedException ex) {
                    return;
                }

                // Check if we should exit the thread
                if (message.equals(EXIT_MESSAGE)) {
                    logger.log(Level.FINE, () -> "Exiting write thread " + id);
                    return;
                }

                final WebSocketSession safe_session = session;
                try {
                    if (safe_session == null)
                        throw new Exception("No session");
                    if (!safe_session.isOpen())
                        throw new Exception("Session closed");
                    safe_session.sendMessage(new TextMessage(message));
                    last_message_sent = System.currentTimeMillis();
                } catch (final Exception ex) {
                    logger.log(Level.WARNING, ex, () -> "Cannot write '" + shorten(message) + "' for " + id);

                    // Clear queue
                    String drop = write_queue.take();
                    while (drop != null) {
                        if (drop.equals(EXIT_MESSAGE)) {
                            logger.log(Level.FINE, () -> "Exiting write thread " + id);
                            return;
                        }
                        drop = write_queue.take();
                    }
                }
            }
        } catch (Throwable ex) {
            logger.log(Level.WARNING, "Write thread error for " + id, ex);
        }
    }

    public void trackClientUpdate() {
        last_client_message = System.currentTimeMillis();
    }

    private List<String> getPVs(final String message, final JsonNode json) throws Exception {
        final JsonNode node = json.path("pvs");
        if (node.isMissingNode())
            throw new Exception("Missing 'pvs' in " + shorten(message));
        final Iterator<JsonNode> nodes = node.elements();
        final List<String> pvs = new ArrayList<>();
        while (nodes.hasNext())
            pvs.add(nodes.next().asText());
        return pvs;
    }

    /**
     * Called when client sends a general message
     *
     * @param message {@link TextMessage}, its payload is expected to be JSON.
     */
    public void handleTextMessage(TextMessage message) throws Exception {
        final JsonNode json = objectMapper.readTree(message.getPayload());
        final JsonNode node = json.path("type");
        if (node.isMissingNode())
            throw new Exception("Missing 'type' in " + shorten(message.getPayload()));
        final String type = node.asText();

        switch (type) {
            case "monitor":
            case "subscribe":
                subscribe(getPVs(message.getPayload(), json));
                break;
            case "clear":
                unsubscribe(getPVs(message.getPayload(), json));
                break;
            case "write":
                write(message.getPayload(), json);
                break;
            case "list":
                list();
                break;
            case "ping":
                logger.log(Level.FINER, "Sending ping...");
                session.sendMessage(new PingMessage());
                break;
            case "echo":
                queueMessage(message.getPayload());
                break;
            default:
                throw new Exception("Unknown message type: " + shorten(message.getPayload()));
        }
    }

    /**
     * @param name          PV name for which to send an update
     * @param value         Current value
     * @param last_value    Previous value
     * @param last_readonly Was the PV read-only?
     * @param readonly      Is the PV read-only?
     */
    public void sendUpdate(final String name, final VType value, final VType last_value, final boolean last_readonly, final boolean readonly) {
        try {
            queueMessage(Vtype2Json.toJson(name, value, last_value, last_readonly, readonly));
        } catch (final Exception ex) {
            logger.log(Level.WARNING, "Cannot send " + name + " = " + shorten(Objects.toString(value)), ex);
        }
    }

    /**
     * Clears all PVs
     *
     * <p>Web socket calls this onClose(),
     * but context may also call this again just in case
     */
    public void dispose() {
        // Exit write thread
        try {
            // Drop queued messages (which might be stuffed):
            // We're closing and just need the EXIT_MESSAGE
            write_queue.clear();
            queueMessage(EXIT_MESSAGE);
            if (!pvs.isEmpty()) {
                logger.log(Level.FINE, "Disposing web socket PVs:");
                for (final WebSocketPV pv : pvs.values()) {
                    logger.log(Level.FINE, () -> "Closing " + pv);
                    //pv.dispose();
                }
                pvs.clear();
            }
            // TODO: is this needed?
            session.close();
        } catch (Throwable ex) {
            logger.log(Level.WARNING, "Error disposing " + getId(), ex);
        }
        logger.log(Level.FINE, () -> "Web socket " + session.getId() + " closed");
        last_client_message = 0;
    }

    private void subscribe(List<String> pvNames) {
        for (final String name : pvNames) {
            pvs.computeIfAbsent(name, n ->
            {
                logger.log(Level.FINER, () -> "Subscribe to " + name);
                final WebSocketPV pv = new WebSocketPV(name, this);
                try {
                    pv.start();
                } catch (final Exception ex) {
                    logger.log(Level.WARNING, "Cannot start PV " + name, ex);
                }
                return pv;
            });
        }
    }

    private void unsubscribe(List<String> pvNames) {
        for (final String name : pvNames) {
            final WebSocketPV pv = pvs.remove(name);
            if (pv != null) {
                logger.log(Level.FINER, () -> "Clear " + name);
                pv.dispose();
            }
        }
    }

    /**
     * @param message Error message
     */
    public void sendError(final String message) {
        try {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setMessage(message);
            queueMessage(objectMapper.writeValueAsString(errorMessage));
        } catch (final Exception ex) {
            logger.log(Level.WARNING, "Cannot send error " + shorten(message), ex);
        }
    }

    private void write(String message, JsonNode json) throws Exception {
        JsonNode n = json.path("pv");
        if (n.isMissingNode())
            throw new Exception("Missing 'pv' in " + shorten(message));
        final String pv_name = n.asText();

        n = json.path("value");
        if (n.isMissingNode())
            throw new Exception("Missing 'value' in " + shorten(message));
        final Object value;
        if (n.getNodeType() == JsonNodeType.NUMBER)
            value = n.asDouble();
        else
            value = n.asText();

        try {
            final WebSocketPV pv = pvs.get(pv_name);
            if (pv == null)
                throw new Exception("Cannot write unknown PV " + pv_name);
            pv.write(value);
        } catch (final Exception ex) {
            sendError(ex.getMessage());
        }
    }

    private void list() throws Exception {
        List<String> pvNames = new ArrayList<>();
        for (final WebSocketPV pv : pvs.values()) {
            pvNames.add(pv.getName());
        }
        PvList pvList = new PvList();
        pvList.setPvs(pvNames);
        queueMessage(objectMapper.writeValueAsString(pvList));
    }
}
