/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import static org.junit.Assert.*;

import java.util.List;

//import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.*;

/**
 * @author Eric
 *
 */
public class Video14496HandlerTest extends PesHandlerSetup{

	@Test
	public void testNPO1Video() {

		assertNotNull("transportStream = null",transportStream);
		final PID npo1 = transportStream.getPID(NPO1_H264_PID);
		assertNotNull("npo1 = null",npo1);

		assertEquals("number of TS packets",36538, npo1.getPackets());
		assertEquals("PID Type",PID.PES, npo1.getType());


		assertEquals("PCR Count",125, npo1.getPcr_count());

		final GeneralPidHandler pidHandler = npo1.getPidHandler();
		assertEquals(Video14496Handler.class, pidHandler.getClass());

		final H26xHandler<?, ?> video14496Handler = (H26xHandler<?, ?>) pidHandler;

		final List<PesPacketData> pesPackets = video14496Handler.getPesPackets();
		assertNotNull("pesPackets = null",pesPackets);
		assertEquals("Number of PESPackets", 115, pesPackets.size());

		testFrame0(pesPackets.get(0));
		testFrame2(pesPackets.get(2));
		testFrame19(pesPackets.get(19));
	}

	
	@Test
	public void testNPO2Video() {

		assertNotNull("transportStream = null",transportStream);
		final PID npo2 = transportStream.getPID(NPO2_H264_PID);
		assertNotNull("npo2 = null",npo2);

		assertEquals("number of TS packets",36545, npo2.getPackets());
		assertEquals("PID Type",PID.PES, npo2.getType());


		assertEquals("PCR Count",125, npo2.getPcr_count());

		final GeneralPidHandler pidHandler = npo2.getPidHandler();
		assertEquals(Video14496Handler.class, pidHandler.getClass());

		final H26xHandler<?, ?> video14496Handler = (H26xHandler<?, ?>) pidHandler;

		final List<PesPacketData> pesPackets = video14496Handler.getPesPackets();
		assertNotNull("pesPackets = null",pesPackets);
		assertEquals("Number of PESPackets", 110, pesPackets.size());
	}

	/**
	 * @param bFrame
	 */
	private static void testFrame19(final PesPacketData iFrame) {
		assertNotNull("iFrame = null",iFrame);

		final List<NALUnit> nalUnitsIFrame = getNALUnits(iFrame);
		assertEquals("Number of nalUnits", 6, nalUnitsIFrame.size());
		final NALUnit unit0 = nalUnitsIFrame.get(0);
		testNALUnit(unit0, Access_unit_delimiter_rbsp.class, 9);
		final NALUnit unit1 = nalUnitsIFrame.get(1);
		testNALUnit(unit1, Seq_parameter_set_rbsp.class, 7);
		final Seq_parameter_set_rbsp seq_parameter_set_rbsp = (Seq_parameter_set_rbsp) unit1.getRbsp();

		assertEquals("Profile_idc",77,seq_parameter_set_rbsp.getSeqParameterSetData().getProfile_idc());
		assertEquals("Pic_width_in_mbs_minus1",119,seq_parameter_set_rbsp.getSeqParameterSetData().getPic_width_in_mbs_minus1());
		assertEquals("Pic_height_in_map_units_minus1",33,seq_parameter_set_rbsp.getSeqParameterSetData().getPic_height_in_map_units_minus1());

		final NALUnit unit2 = nalUnitsIFrame.get(2);
		testNALUnit(unit2, Pic_parameter_set_rbsp.class, 8);

		final NALUnit unit3 = nalUnitsIFrame.get(3);
		testNALUnit(unit3, Sei_rbsp.class, 6);

		final Sei_rbsp sei3 = (Sei_rbsp) unit3.getRbsp();

		final List<Sei_message> sei3Message = sei3.getSei_messages();
		assertNotNull("Sei_message IFrame is null",sei3Message);
		assertEquals(4, sei3Message.size());


		final NALUnit unit4 = nalUnitsIFrame.get(4);
		testNALUnit(unit4, Sei_rbsp.class, 6);

		final NALUnit unit5 = nalUnitsIFrame.get(5);
		testNALUnit(unit5, Slice_layer_without_partitioning_rbsp.class, 1);

		assertEquals("NumBytesInRBSP",201000,unit5.getNumBytesInRBSP());
		final Slice_layer_without_partitioning_rbsp slice_layer_without_partitioning_rbsp = (Slice_layer_without_partitioning_rbsp) unit5.getRbsp();

		final Slice_header sliceHeader = slice_layer_without_partitioning_rbsp.getSlice_header();
		assertNotNull("sliceHeader",sliceHeader);
		assertEquals("slice_type",7,sliceHeader.getSlice_type()); // I slice

	}


	/**
	 * @param bFrame
	 */

	private static void testFrame2(final PesPacketData pFrame) {
		assertNotNull("bFrame = null",pFrame);

		final List<NALUnit> nalUnitsBFrame = getNALUnits(pFrame);
		assertEquals("Number of nalUnits", 4, nalUnitsBFrame.size());
		final NALUnit unit0 = nalUnitsBFrame.get(0);
		testNALUnit(unit0, Access_unit_delimiter_rbsp.class, 9);
		final NALUnit unit3 = nalUnitsBFrame.get(3);

		testNALUnit(unit3, Slice_layer_without_partitioning_rbsp.class, 1);

		assertEquals("NumBytesInRBSP",109180,unit3.getNumBytesInRBSP());
		final Slice_layer_without_partitioning_rbsp slice_layer_without_partitioning_rbsp = (Slice_layer_without_partitioning_rbsp) unit3.getRbsp();

		final Slice_header sliceHeader = slice_layer_without_partitioning_rbsp.getSlice_header();
		assertNotNull("sliceHeader",sliceHeader);
		assertEquals("slice_type",5,sliceHeader.getSlice_type()); // P slice

	}

	/**
	 * @param bFrame
	 */

	private static void testFrame0(final PesPacketData bFrame) {
		assertNotNull("bFrame = null",bFrame);

		final PesHeader pesHeader = bFrame.getPesHeader();
		assertNotNull("pesHeader = null",pesHeader);
		assertEquals("pesHeader.getStreamID()",224,pesHeader.getStreamID());
		final List<NALUnit> nalUnitsBFrame = getNALUnits(bFrame);
		assertEquals("Number of nalUnits", 4, nalUnitsBFrame.size());

		final NALUnit unit0 = nalUnitsBFrame.get(0);
		testNALUnit(unit0, Access_unit_delimiter_rbsp.class, 9);
		final Access_unit_delimiter_rbsp access = (Access_unit_delimiter_rbsp) unit0.getRbsp();
		assertEquals("access.getPrimary_pic_type()",2,access.getPrimary_pic_type());

		final NALUnit unit1 = nalUnitsBFrame.get(1);
		testNALUnit(unit1, Sei_rbsp.class, 6);
		final Sei_rbsp sei = (Sei_rbsp) unit1.getRbsp();
		final List<Sei_message> seiMessage = sei.getSei_messages();
		assertNotNull("Sei_message BFrame is null",seiMessage);
		assertEquals(2, seiMessage.size());

		final Sei_message mes0 = seiMessage.get(0);
		assertEquals(1,mes0.getPayloadType());

		final Sei_message mes1 = seiMessage.get(1);
		assertEquals("PayloadType",4,mes1.getPayloadType());
		assertEquals(UserDataRegisteredItuT35Sei_message.class, mes1.getClass());
		final UserDataRegisteredItuT35Sei_message userDataRegisteredItuT35Sei_message = (UserDataRegisteredItuT35Sei_message) mes1;

		assertEquals("Itu_t_t35_country_code",181,userDataRegisteredItuT35Sei_message.getItu_t_t35_country_code());
		assertEquals("Itu_t_t35_provider_code",49,userDataRegisteredItuT35Sei_message.getItu_t_t35_provider_code());

		final AuxiliaryData auxData = userDataRegisteredItuT35Sei_message.getAuxData();
		assertNotNull("auxData is null",auxData);
		final byte[] userIdentifier = auxData.getUser_identifier();
		assertArrayEquals("User_identifier",new byte[] {0x44,0x54,0x47,0x31}, userIdentifier); // DTG1

		assertEquals("Active_format_flag",1,auxData.getActive_format_flag());
		assertEquals("Active_format",8,auxData.getActive_format());

		final NALUnit unit2 = nalUnitsBFrame.get(2);
		testNALUnit(unit2, Sei_rbsp.class, 6);
		final Sei_rbsp sei2 = (Sei_rbsp) unit2.getRbsp();
		final List<Sei_message> sei2Message = sei2.getSei_messages();
		assertNotNull("Sei_message BFrame is null",sei2Message);
		assertEquals(1, sei2Message.size());

		final Sei_message mes21 = sei2Message.get(0);
		assertEquals(4,mes21.getPayloadType());
		assertEquals(UserDataRegisteredItuT35Sei_message.class, mes21.getClass());
		final UserDataRegisteredItuT35Sei_message userDataRegisteredItuT35Sei_message2 = (UserDataRegisteredItuT35Sei_message) mes21;
		assertEquals("Itu_t_t35_country_code",181,userDataRegisteredItuT35Sei_message2.getItu_t_t35_country_code());
		assertEquals("Itu_t_t35_provider_code",49,userDataRegisteredItuT35Sei_message2.getItu_t_t35_provider_code());

		final AuxiliaryData auxData2 = userDataRegisteredItuT35Sei_message2.getAuxData();
		assertNotNull("auxData2 is null",auxData2);

		assertArrayEquals("User_identifier",new byte[] {0x47,0x41,0x39,0x34}, auxData2.getUser_identifier()); // GA94

		assertEquals("User_data_type_code",3, auxData2.getUser_data_type_code());

		final NALUnit unit3 = nalUnitsBFrame.get(3);
		testNALUnit(unit3, Slice_layer_without_partitioning_rbsp.class, 1);
		final Slice_layer_without_partitioning_rbsp slice_layer_without_partitioning_rbsp = (Slice_layer_without_partitioning_rbsp) unit3.getRbsp();

		final Slice_header sliceHeader = slice_layer_without_partitioning_rbsp.getSlice_header();
		assertNotNull("sliceHeader",sliceHeader);
		assertEquals("slice_type",6,sliceHeader.getSlice_type()); // B slice
	}


	/**
	 * @param bFrame
	 * @return
	 */
	private static List<NALUnit> getNALUnits(final PesPacketData bFrame) {
		assertEquals(Video14496PESDataField.class, bFrame.getClass());
		final Video14496PESDataField bFrame264 = (Video14496PESDataField) bFrame;

		final List<NALUnit> nalUnitsBFrame = bFrame264.getNalUnits();
		assertNotNull("nalUnits = null",nalUnitsBFrame);
		return nalUnitsBFrame;
	}


	/**
	 * @param unit0
	 * @param expectedClass
	 * @param expectedType
	 */

	private static void testNALUnit(final NALUnit unit, final Class<? extends RBSP> expectedClass,
			final int expectedType) {
		assertNotNull("unit = null",unit);
		assertEquals("unit.getNal_unit_type",expectedType,unit.getNal_unit_type());
		final RBSP rbsp = unit.getRbsp();
		assertNotNull("rbsp = null",rbsp);

		assertEquals(expectedClass, rbsp.getClass());
	}


}
