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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.phoebus.pvws.model.InfoData;
import org.phoebus.pvws.model.PvPoolData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PvwsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testInfo() throws Exception {
        MockHttpServletRequestBuilder request = get("/info").contentType("application/json");

        MvcResult result = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn();
        String s = result.getResponse().getContentAsString();
        // Make sure response contains expected data
        objectMapper.readValue(s, InfoData.class);

        request = get("/info").contentType("application/json?env=true");

        result = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn();
        s = result.getResponse().getContentAsString();
        // Make sure response contains expected data
        objectMapper.readValue(s, InfoData.class);
    }

    @Test
    public void testPool() throws Exception {
        MockHttpServletRequestBuilder request = get("/pool").contentType("application/json");

        MvcResult result = mockMvc.perform(request).andExpect(status().isOk())
                .andReturn();
        String s = result.getResponse().getContentAsString();
        // Make sure response contains expected data
        objectMapper.readValue(s, new TypeReference<List<PvPoolData>>() {
        });
    }


}
