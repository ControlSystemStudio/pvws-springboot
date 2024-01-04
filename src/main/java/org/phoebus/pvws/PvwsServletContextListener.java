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

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.phoebus.pv.PV;
import org.phoebus.pv.PVPool;
import org.phoebus.pv.RefCountMap;
import org.phoebus.pvws.ws.WebSocket;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class PvwsServletContextListener implements ServletContextListener {

    private final List<WebSocket> sockets;

    private final Logger logger = Logger.getLogger(PvwsServletContextListener.class.getName());

    public PvwsServletContextListener(List<WebSocket> sockets) {
        this.sockets = sockets;
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.log(Level.WARNING, "===========================================");
        logger.log(Level.INFO, "Application context shut down");

        sockets.forEach(s -> {
            logger.log(Level.INFO, "Web sockets that did not close/unregister:");
            s.dispose();
        });

        if (!PVPool.getPVReferences().isEmpty()) {
            for (final RefCountMap.ReferencedEntry<PV> ref : PVPool.getPVReferences()) {
                logger.log(Level.WARNING, "Unreleased PV " + ref.getEntry().getName());
                PVPool.releasePV(ref.getEntry());
            }
        }
        logger.log(Level.INFO, "===========================================");
    }
}
