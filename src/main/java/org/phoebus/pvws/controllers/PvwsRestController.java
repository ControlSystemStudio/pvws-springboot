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

package org.phoebus.pvws.controllers;

import org.epics.util.array.ListInteger;
import org.epics.vtype.Array;
import org.epics.vtype.VType;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pv.RefCountMap;
import org.phoebus.pvws.model.*;
import org.phoebus.pvws.ws.WebSocket;
import org.phoebus.pvws.ws.WebSocketPV;
import org.phoebus.util.time.TimestampFormats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class replaces several servlets from the original pvws implementation,
 * which are published under the following copyright:
 * <p>
 * ******************************************************************************
 * Copyright (c) 2019-2022 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the LICENSE
 * which accompanies this distribution
 * ******************************************************************************
 * </p>
 */
@SuppressWarnings("unused")
@RestController
public class PvwsRestController {

    @Autowired
    private List<WebSocket> sockets;

    @Autowired
    private Instant startTime;

    private final Logger logger = Logger.getLogger(PvwsRestController.class.getName());

    @GetMapping("/summary")
    public ServerSummary summary() {
        ServerSummary summary = new ServerSummary();

        if (sockets != null && !sockets.isEmpty()) {
            List<AbstractWebSocketSummary> webSocketSummaries = new ArrayList<>();
            sockets.forEach(s -> {
                ShortWebSocketSummary webSocketSummary = new ShortWebSocketSummary(s);
                webSocketSummary.setPvs(s.getPVs().size());
                int arrays = 0;
                int max_size = 0;
                for (final WebSocketPV pv : s.getPVs()) {
                    final VType value = pv.getLastValue();
                    if (value instanceof Array) {
                        ++arrays;
                        final ListInteger sizes = ((Array) value).getSizes();
                        for (int i = 0; i < sizes.size(); ++i)
                            max_size = Math.max(max_size, sizes.getInt(i));
                    }
                }
                webSocketSummary.setArrays(arrays);
                webSocketSummary.setMax_size(max_size);
                webSocketSummaries.add(webSocketSummary);
            });
            summary.setSockets(webSocketSummaries);
        }
        return summary;
    }

    @GetMapping(value = {"/socket", "/socket/{id}"})
    public ServerSummary socketSummary(@PathVariable(name = "id", required = false) String id) {
        ServerSummary summary = new ServerSummary();

        if (sockets != null && !sockets.isEmpty()) {
            List<AbstractWebSocketSummary> webSocketSummaries = new ArrayList<>();
            for (final WebSocket webSocket : sockets) {
                if (id != null && !webSocket.getId().equals(id)) {
                    continue;
                }
                FullSocketSummary fullSocketSummary = new FullSocketSummary(webSocket);
                List<PvSummary> pvSummaries = new ArrayList<>();
                webSocket.getPVs().forEach(pv -> pvSummaries.add(new PvSummary(pv)));
                fullSocketSummary.setPvs(pvSummaries);
                webSocketSummaries.add(fullSocketSummary);
            }
            summary.setSockets(webSocketSummaries);
        }

        return summary;
    }

    @DeleteMapping("/socket/{id}")
    public void deleteSocket(@PathVariable String id) {
        for (final WebSocket socket : sockets)
            if (id.equals(socket.getId())) {
                logger.log(Level.INFO, "DELETE socket '" + id + "'");
                socket.dispose();
                return;
            }
        logger.log(Level.WARNING, "Cannot DELETE socket '" + id + "'");
    }

    @GetMapping("/pool")
    public List<PvPoolData> pool() {
        List<PvPoolData> data = new ArrayList<>();
        for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences()) {
            PvPoolData pvPoolData = new PvPoolData();
            pvPoolData.setName(ref.getEntry().getName());
            pvPoolData.setRefs(ref.getReferences());
            data.add(pvPoolData);
        }
        return data;
    }

    @GetMapping("/info")
    public InfoData info(@RequestParam(name = "env", defaultValue = "false") boolean env) {
        InfoData infoData = new InfoData();
        infoData.setJre(System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
        infoData.setStart_time(TimestampFormats.SECONDS_FORMAT.format(startTime));
        if (env) {
            infoData.setEnv(System.getenv().entrySet());
        }
        return infoData;
    }
}
