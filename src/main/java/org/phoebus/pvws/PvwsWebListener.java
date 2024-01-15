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

package org.phoebus.pvws;

import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pv.RefCountMap;
import org.phoebus.pvws.ws.WebSocket;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.List;

@WebListener
public class PvwsWebListener implements ServletContextListener {

    private List<WebSocket> sockets;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        System.out.println("sdjhgfdfugh");
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        sockets = (List<WebSocket>) context.getBean("sockets");
        if (sockets == null) {
            System.out.println("null");
        } else {
            System.out.println("sockets size " + sockets.size());
            sockets.forEach(s -> {
                System.out.println(s.getId());
                s.dispose();
            });
        }
        System.out.println("check pv pool");
        if (!PVPool.getPVReferences().isEmpty())
            for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences()) {
                System.out.println("Unreleased PV " + ref.getEntry().getName());
                PVPool.releasePV(ref.getEntry());
            }
    }
}
