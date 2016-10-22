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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.*;

/**
 * @author Eric
 *
 */
public class AudioHandlerTest extends PesHandlerSetup{

	@Test
	public void testNPO1Audio() {

		assertNotNull("transportStream = null",transportStream);
		final PID audPid = transportStream.getPID(NPO1_AUD_PID);
		assertNotNull("aud = null",audPid);

		assertEquals("number of TS packets",783, audPid.getPackets());
		assertEquals("PID Type",PID.PES, audPid.getType());



		final GeneralPesHandler pesHandler = audPid.getPesHandler();
		assertEquals(Audio138183Handler.class, pesHandler.getClass());

		final Audio138183Handler audio138182Handler = (Audio138183Handler) pesHandler;
		
		validatePreviewImageSize(audio138182Handler.getJTreeNode(0),840, 2208, "audio graph");

		final List<PesPacketData> pesPackets = audio138182Handler.getPesPackets();
		assertNotNull("pesPackets = null",pesPackets);
		assertEquals("Number of PESPackets", 36, pesPackets.size());
		
		List<AudioAccessUnit> audioAccessUnits = audio138182Handler.getAudioAccessUnits();
		assertEquals(179, audioAccessUnits.size());
		
		AudioAccessUnit unit0 = audioAccessUnits.get(0);
		assertEquals(1, unit0.getId());
		assertEquals(2, unit0.getLayer());
		assertEquals(12, unit0.getBit_rate_index());
		assertEquals(1, unit0.getSampling_frequency_index());
		assertEquals(0, unit0.getMode());
		
		
	}

	

}
