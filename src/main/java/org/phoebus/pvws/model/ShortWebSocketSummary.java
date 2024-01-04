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

import org.phoebus.pvws.ws.WebSocket;

public class ShortWebSocketSummary extends AbstractWebSocketSummary {

    private Integer pvs;
    private Integer arrays;
    private Integer max_size;

    public ShortWebSocketSummary(WebSocket webSocket){
        super(webSocket);
    }

    public Integer getPvs() {
        return pvs;
    }

    public void setPvs(Integer pvs) {
        this.pvs = pvs;
    }

    public Integer getArrays() {
        return arrays;
    }

    public void setArrays(Integer arrays) {
        this.arrays = arrays;
    }

    public Integer getMax_size() {
        return max_size;
    }

    public void setMax_size(Integer max_size) {
        this.max_size = max_size;
    }
}
