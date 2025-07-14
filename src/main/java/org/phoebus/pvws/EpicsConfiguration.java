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

import org.phoebus.pv.PVPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    @Value("${EPICS_PVA_ADDR_LIST}")
    private String epicsPvaAddrList;

    @Value("${EPICS_PVA_AUTO_ADDR_LIST:YES}")
    private String epicsPvaAutoAddrList;

    @Value("${EPICS_PVA_BROADCAST_PORT:5076}")
    private String epicsPvaBroadcastPort;

    @Value("${EPICS_PVA_NAME_SERVERS}")
    private String epicsPvaNameServers;

    @Value("${PV_THROTTLE_MS:1000}")
    private String pvThrottleMs;

    @Value("${PV_ARRAY_THROTTLE_MS:10000}")
    private String pvArrayThrottleMs;

    @Value("${PV_WRITE_SUPPORT:true}")
    private String pvWriteSupport;

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "===========================================");
        logger.log(Level.INFO, contextPath + " started");
        // Set default type in preferences before PVPool reads the preferences
        String default_type = System.getenv("PV_DEFAULT_TYPE");
        if (default_type != null && !default_type.isEmpty()) {
            Preferences.userRoot().node("/org/phoebus/pv").put("default", default_type);
        }
        // Not set in environment, try application properties or command line
        else if (pvDefaultType != null && !pvDefaultType.isEmpty()) {
            Preferences.userRoot().node("/org/phoebus/pv").put("default", pvDefaultType);
        }
        logger.log(Level.INFO, "PV_DEFAULT_TYPE=" + Preferences.userRoot().node("/org/phoebus/pv").get("default", null));
        logger.log(Level.INFO, "Supported PV types: " + PVPool.getSupportedPrefixes());

        Preferences.userRoot().node("/org/phoebus/pv/pva").put("epics_pva_addr_list", epicsPvaAddrList);

        if(!epicsPvaAutoAddrList.isEmpty()){
            Preferences.userRoot().node("/org/phoebus/pv/pva").put("epics_pva_auto_addr_list", epicsPvaAutoAddrList);
        }

        Preferences.userRoot().node("/org/phoebus/pv/pva").put("epics_pva_broadcast_port", epicsPvaBroadcastPort);
        Preferences.userRoot().node("/org/phoebus/pv/pva").put("epics_pva_name_servers", epicsPvaNameServers);

        System.setProperty("PV_THROTTLE_MS", pvThrottleMs);
        System.setProperty("PV_ARRAY_THROTTLE_MS", pvArrayThrottleMs);
        System.setProperty("PV_WRITE_SUPPORT", pvWriteSupport);

        // Configure JCA/CAJ to use environment vars, not java properties or preferences
        System.setProperty("jca.use_env", "true");

        logger.log(Level.INFO, "EPICS_CA_ADDR_LIST=" + System.getenv("EPICS_CA_ADDR_LIST"));
        logger.log(Level.INFO, "EPICS_CA_AUTO_ADDR_LIST=" + System.getenv("EPICS_CA_AUTO_ADDR_LIST"));
        logger.log(Level.INFO, "EPICS_CA_MAX_ARRAY_BYTES=" + System.getenv("EPICS_CA_MAX_ARRAY_BYTES"));
        logger.log(Level.INFO, "EPICS_PVA_ADDR_LIST=" + epicsPvaAddrList);
        logger.log(Level.INFO, "EPICS_PVA_AUTO_ADDR_LIST=" + epicsPvaAutoAddrList);

        logger.log(Level.INFO, "===========================================");

    }
}
