package org.phoebus.pvws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.phoebus.pvws.ws.PvSocketHandler;
import org.phoebus.pvws.ws.Vtype2Json;
import org.phoebus.pvws.ws.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("unused")
@SpringBootTest
class EpicsWebSocketServerApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Vtype2Json vtype2Json;

    @Autowired
    private PvSocketHandler pvSocketHandler;

    @Autowired
    private Instant start_time;

    @Autowired
    private List<WebSocket> sockets;

    @Test
    void contextLoads() {
        assertNotNull(objectMapper);
        assertNotNull(vtype2Json);
        assertNotNull(start_time);
        assertNotNull(pvSocketHandler);
        assertNotNull(sockets);
    }
}
