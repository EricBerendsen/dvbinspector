/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * 
 *  This file is part of DVB Inspector.
 * 
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 * 
 */

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.*;

/**
 * @author Eric
 *
 */
public class EBUHandlerTest extends PesHandlerSetup{

	@Test
	public void testNPO1Teletext() {

		final PID ebuPid = transportStream.getPID(NPO1_EBU_PID);
		assertNotNull("ebu = null",ebuPid);

		assertEquals("number of TS packets",895, ebuPid.getPackets());
		assertEquals("PID Type",PID.PES, ebuPid.getType());



		final GeneralPesHandler pesHandler = ebuPid.getPesHandler();
		assertEquals(EBUTeletextHandler.class, pesHandler.getClass());

		final EBUTeletextHandler ebuTeletextHandler = (EBUTeletextHandler) pesHandler;

		final List<PesPacketData> pesPackets = ebuTeletextHandler.getPesPackets();
		assertNotNull("pesPackets = null",pesPackets);
		assertEquals("Number of PESPackets", 223, pesPackets.size());
		
		TxtService txtService = ebuTeletextHandler.getTxtService();
		assertNotNull(txtService);
		
		Magazine magazine = txtService.getMagazine(2);
		
		Page page = magazine.getPage(0x51); // page 251
		
		validatePreviewImageSize(page.getJTreeNode(0), 475, 600, "tt page 251");
		
		Map<Integer, SubPage> subPages = page.getSubPages();
		assertEquals(1, subPages.size());
		SubPage sub = subPages.get(0);
		TxtDataField[] lines = sub.getLinesList();
		TxtDataField line = lines[5];
		assertEquals(" 07.05  vroege vogels               VARA",line.getTeletextPlain());
		
	}

	

}
