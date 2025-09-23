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
 */

package org.phoebus.pvws;

import org.epics.vtype.VType;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pv.RefCountMap;
import org.phoebus.pvws.ws.Vtype2Json;
import org.phoebus.pvws.ws.WebSocket;
import org.phoebus.pvws.ws.WebSocketPV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@SpringBootApplication
@RestController
public class EpicsWebSocketServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(EpicsWebSocketServerApplication.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            List<WebSocket> sockets = (List<WebSocket>) applicationContext.getBean("sockets");
            sockets.forEach(s -> {
                System.out.println(s.getId());
                s.dispose();
            });
            if (!PVPool.getPVReferences().isEmpty())
                for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences()) {
                    System.out.println("Unreleased PV " + ref.getEntry().getName());
                    PVPool.releasePV(ref.getEntry());
                }
        }));
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(EpicsWebSocketServerApplication.class);
    }

    @GetMapping(value= "/pvget", produces = MediaType.APPLICATION_JSON_VALUE)
    public String pvget(@RequestParam String name) {
        final WebSocketPV pv = new WebSocketPV(name, null);
        String ret;
        try {

            // TODO make these configurable?
            int maxAttempts = 100;
            int retryDelay = 50;

            VType lastValue = null;

            for (int i = 0; i< maxAttempts; i++) {
                Thread.sleep(retryDelay);
                lastValue = pv.get();
                if (lastValue != null) break;
                System.out.println("value is" + lastValue + "attempt no " + i);
            }

            System.out.println("value is" + lastValue);
            ret = Vtype2Json.toJson(name, lastValue, null, true, true);
        } catch (final Exception ex) {
            ret = String.format("Unable to get PV value for %s - exception %s", name, ex);
        }
        pv.dispose();
        return ret;
    }
}
