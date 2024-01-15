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

import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pv.RefCountMap;
import org.phoebus.pvws.ws.WebSocket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
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
}
