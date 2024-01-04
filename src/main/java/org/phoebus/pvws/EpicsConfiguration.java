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

package org.phoebus.pvws;

import jakarta.annotation.PostConstruct;
import org.phoebus.pv.PVPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

@SuppressWarnings("unused")
@Component
public class EpicsConfiguration {

    private static final Logger logger = Logger.getLogger(EpicsConfiguration.class.getName());

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    @Value("${PV_DEFAULT_TYPE:ca}")
    private String pvDefaultType;

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "===========================================");
        logger.log(Level.INFO, contextPath + " started");
        logger.log(Level.INFO, "Supported PV types: " + PVPool.getSupportedPrefixes());

        // Set default type in preferences before PVPool reads the preferences
        String default_type = System.getenv("PV_DEFAULT_TYPE");
        if (default_type != null  &&  !default_type.isEmpty()){
            Preferences.userRoot().node("/org/phoebus/pv").put("default", default_type);
        }
        // Not set in environment, try application properties or command line
        else if(pvDefaultType != null && !pvDefaultType.isEmpty()){
            Preferences.userRoot().node("/org/phoebus/pv").put("default", pvDefaultType);
        }
        logger.log(Level.INFO, "PV_DEFAULT_TYPE=" + Preferences.userRoot().node("/org/phoebus/pv").get("default", null));





        // Configure JCA/CAJ to use environment vars, not java properties or preferences
        System.setProperty("jca.use_env", "true");

        for (String name : new String[]
                {
                        "PV_DEFAULT_TYPE",
                        "PV_THROTTLE_MS",
                        "PV_ARRAY_THROTTLE_MS",
                        "PV_WRITE_SUPPORT",
                        "EPICS_CA_ADDR_LIST",
                        "EPICS_CA_AUTO_ADDR_LIST",
                        "EPICS_CA_MAX_ARRAY_BYTES",
                        "EPICS_PVA_ADDR_LIST",
                        "EPICS_PVA_AUTO_ADDR_LIST"
                })
            logger.log(Level.INFO, name + " = " + System.getenv(name));


        logger.log(Level.INFO, "===========================================");

    }
}
