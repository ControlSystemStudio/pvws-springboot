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

import org.epics.vtype.Array;
import org.epics.vtype.VType;
import org.phoebus.pvws.ws.WebSocketPV;

import java.util.Objects;

public class PvSummary {

    private String name;
    private String value;

    public PvSummary(WebSocketPV webSocketPV){
        this.name = webSocketPV.getName();
        VType value = webSocketPV.getLastValue();
        if (value instanceof Array)
        {   // For arrays, show size, not actual elements
            this.value = value.getClass().getName() + ", size " + ((Array) value).getSizes();
        }
        else{
            this.value = Objects.toString(webSocketPV.getLastValue());
        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
