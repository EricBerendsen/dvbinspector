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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;

import static org.junit.Assert.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

//import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.*;

/**
 * @author Eric
 *
 */
public class VideoHandlerTest extends PesHandlerSetup{

	@Test
	public void testTVEVideo() {

		assertNotNull("transportStream = null",transportStream);
		final PID tvePid = transportStream.getPID(TVE_H222_PID);
		assertNotNull("tve = null",tvePid);

		assertEquals("number of TS packets",8675, tvePid.getPackets());
		assertEquals("PID Type",PID.PES, tvePid.getType());


		assertEquals("PCR Count",121, tvePid.getPcr_count());

		final GeneralPesHandler pesHandler = tvePid.getPesHandler();
		assertEquals(Video138182Handler.class, pesHandler.getClass());

		final Video138182Handler video138182Handler = (Video138182Handler) pesHandler;
		
		validatePreviewImageSize(video138182Handler.getJTreeNode(0),640, 2098, "video bar chart");

		final List<PesPacketData> pesPackets = video138182Handler.getPesPackets();
		assertNotNull("pesPackets = null",pesPackets);
		assertEquals("Number of PESPackets", 111, pesPackets.size());

		testFrame0(pesPackets.get(0));
		testFrame7(pesPackets.get(7));
	}

	private static void testFrame0(PesPacketData pesPacketData) {
		assertNotNull("Frame 0 ", pesPacketData);
		assertEquals(VideoPESDataField.class, pesPacketData.getClass());
		VideoPESDataField videoPESDataField = (VideoPESDataField)pesPacketData;
		List<VideoMPEG2Section> sections = videoPESDataField.getSections();
		assertEquals(39, sections.size());
		
		VideoMPEG2Section section0 = sections.get(0);
		assertEquals(PictureHeader.class, section0.getClass());
		PictureHeader pictureHeader = (PictureHeader)section0;
		assertEquals(3,pictureHeader.getPicture_coding_type());  // B Frame
	}

	private static void testFrame7(PesPacketData pesPacketData) {
		assertNotNull("Frame 7 ", pesPacketData);
		assertEquals(VideoPESDataField.class, pesPacketData.getClass());
		VideoPESDataField videoPESDataField = (VideoPESDataField)pesPacketData;
		List<VideoMPEG2Section> sections = videoPESDataField.getSections();
		assertEquals(42, sections.size());
		
		VideoMPEG2Section section0 = sections.get(0);
		assertEquals(SequenceHeader.class, section0.getClass());
		SequenceHeader sequenceHeader = (SequenceHeader)section0;
		assertEquals(720,sequenceHeader.getHorizontal_size_value());
		assertEquals(576,sequenceHeader.getVertical_size_value());
		
		VideoMPEG2Section section3 = sections.get(3);
		assertEquals(PictureHeader.class, section3.getClass());
		PictureHeader pictureHeader = (PictureHeader)section3;
		assertEquals(1,pictureHeader.getPicture_coding_type()); // I frame
		
		VideoMPEG2Section section5 = sections.get(5);
		assertEquals(UserData.class, section5.getClass());
		UserData userData = (UserData)section5;

		AuxiliaryData auxData = userData.getAuxData();
		assertNotNull(auxData);
		assertTrue("Expecting DTG1",Arrays.equals(new byte[]{0x44,0x54,0x47,0x31}, auxData.getUser_identifier()));
		assertEquals(1,auxData.getActive_format_flag()); 
		assertEquals(0,auxData.getActive_format()); // reserved

		
		DefaultMutableTreeNode tree = pesPacketData.getJTreeNode(0);
		
		assertEquals("child count of tree, very likely to change when changing presentation",22,tree.getChildCount());
		
		validatePreviewImageSize(tree, 576, 720, "mpeg2 video preview");

		
	}

	

}
