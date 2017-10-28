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

package nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.*;

/**
 * @author Eric
 *
 */
public class DVBSubtitlingHandlerTest extends PesHandlerSetup{

	@Test
	public void testNPO2SubTitling() {

		final PID subPid = transportStream.getPID(NPO2_SUB_PID);
		assertNotNull("subPid = null",subPid);

		assertEquals("number of TS packets",32, subPid.getPackets());
		assertEquals("number of duplicate TS packets",2, subPid.getDup_packets());
		assertEquals("PID Type",PID.PES, subPid.getType());



		final GeneralPesHandler pesHandler = subPid.getPesHandler();
		assertEquals(DVBSubtitleHandler.class, pesHandler.getClass());

		final DVBSubtitleHandler dvbSubtitleHandler = (DVBSubtitleHandler) pesHandler;

		final List<PesPacketData> pesPackets = dvbSubtitleHandler.getPesPackets();
		assertNotNull("pesPackets = null",pesPackets);
		assertEquals("Number of PESPackets", 3, pesPackets.size());
		
		Titles titles = dvbSubtitleHandler.getTitles();
		assertNotNull(titles);
		
		List<DisplaySet> sets = titles.getDisplaySets();
		assertEquals("Number of display sets",3, sets.size());
		
		DisplaySet set0 = sets.get(0);
		assertNotNull(set0);
		List<Segment> segments0 = set0.getSegments();
		assertEquals(2, segments0.size());
		
		DisplaySet set1 = sets.get(1);
		assertNotNull(set1);
		
		validatePreviewImageSize(set1.getJTreeNode(0),576,720,"display set 1");

		
		List<Segment> segments1 = set1.getSegments();
		assertEquals(7, segments1.size());

		Segment seg0 = segments1.get(0);
		assertEquals(PageCompositionSegment.class, seg0.getClass());
		
		PageCompositionSegment pageCompositionSegment = (PageCompositionSegment)seg0;
		assertEquals(16, pageCompositionSegment.getSegmentType());
		assertEquals(2, pageCompositionSegment.getPageID());
		assertEquals(8, pageCompositionSegment.getSegmentLength());
		assertEquals(1, pageCompositionSegment.getPageState());
		assertEquals(1, pageCompositionSegment.getRegions().size());
		
	
		Segment seg5 = segments1.get(5);
		assertEquals(ObjectDataSegment.class, seg5.getClass());
		
		ObjectDataSegment objectDataSegment = (ObjectDataSegment)seg5;
		
		assertEquals(19, objectDataSegment.getSegmentType());
		assertEquals(2, objectDataSegment.getPageID());
		
		validatePreviewImageSize(objectDataSegment.getJTreeNode(0),58,272,"ObjectDataSegment");

	}
	
	

	

}
