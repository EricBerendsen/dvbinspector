/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
 * @author Eric
 */

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Based on ISO/IEC 13818-1:2013, ch.2.6.58, and ETSI TS 102 323 V1.5.1, ch.5.3.3.2 and DTG D-Book 8.14.7.1
 *
 * @author Eric
 *
 */
public class MetaDataPointerDescriptor extends Descriptor {


	private int metadata_application_format;
	private long metadata_application_format_identifier;
	private int metadata_format;
	private long metadata_format_identifier;
	private int metadata_service_id;
	private int metadata_locator_record_flag;
	private int reserved;
	private byte[] private_data_byte;
	private int MPEG_carriage_flags;
	private int metadata_locator_record_length;
	private byte[] metadata_locator_record_byte;
	private int program_number;
	private int transport_stream_location;
	private int transport_stream_id;

	public MetaDataPointerDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int localOffset = offset+2;
		metadata_application_format = getInt(b, localOffset, 2, MASK_16BITS);
		localOffset+=2;
		if(metadata_application_format==0xFFFF){
			metadata_application_format_identifier = getLong(b, localOffset, 4, MASK_32BITS);
			localOffset+=4;
		}
		metadata_format = getInt(b, localOffset++, 1, MASK_8BITS);
		if(metadata_format==0xFF){
			metadata_format_identifier  = getLong(b, localOffset, 4, MASK_32BITS);
			localOffset+=4;
		}
		metadata_service_id = getInt(b, localOffset++, 1, MASK_8BITS);
		metadata_locator_record_flag = getInt(b, localOffset, 1, 0x80)>>7;
		MPEG_carriage_flags = getInt(b, localOffset, 1, 0x60)>>5;
		reserved = getInt(b, localOffset++, 1, MASK_5BITS);
		if(metadata_locator_record_flag==1){
			metadata_locator_record_length = getInt(b, localOffset++, 1, MASK_8BITS);
			metadata_locator_record_byte = getBytes(b, localOffset, metadata_locator_record_length);
			localOffset += metadata_locator_record_length;
		}
		if(MPEG_carriage_flags!=3){ // 0|1|2
			program_number = getInt(b, localOffset, 2, MASK_16BITS);
			localOffset += 2;
		}
		if (MPEG_carriage_flags == 1) { // '1'
			transport_stream_location = getInt(b, localOffset, 2, MASK_16BITS);
			localOffset += 2;
			transport_stream_id = getInt(b, localOffset, 2, MASK_16BITS);
			localOffset += 2;
		}
		private_data_byte = Utils.copyOfRange(b, localOffset, offset+descriptorLength+2);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("metadata_application_format",metadata_application_format,getMetaDataApplicationFormatString(metadata_application_format))));
		if(metadata_application_format==0xFFFF){
			t.add(new DefaultMutableTreeNode(new KVP("metadata_application_format_identifier",metadata_application_format_identifier,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("metadata_format",metadata_format,getMetaDataFormatString(metadata_format))));
		if(metadata_format==0xFF){
			t.add(new DefaultMutableTreeNode(new KVP("metadata_format_identifier",metadata_format_identifier,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("metadata_service_id",metadata_service_id,metadata_service_id==0xff?"the metadata is carried in a carousel.":null)));
		t.add(new DefaultMutableTreeNode(new KVP("metadata_locator_record_flag",metadata_locator_record_flag,metadata_locator_record_flag==1?"associated metadata is available on a location outside of a Rec. ITU-T H.222.0 | ISO/IEC 13818-1 stream, specified in a metadata_locator_record":null)));
		t.add(new DefaultMutableTreeNode(new KVP("MPEG_carriage_flags",MPEG_carriage_flags,getMPEGCarriageFlagsString(MPEG_carriage_flags))));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));

		if(metadata_locator_record_flag==1){
			t.add(new DefaultMutableTreeNode(new KVP("metadata_locator_record_length",metadata_locator_record_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("metadata_locator_record_byte",metadata_locator_record_byte,null)));
		}
		if(MPEG_carriage_flags!=3){ // 0|1|2
			t.add(new DefaultMutableTreeNode(new KVP("program_number",program_number,getPSI().getSdt().getServiceName(program_number))));
		}
		if (MPEG_carriage_flags == 1) { // '1'
			t.add(new DefaultMutableTreeNode(new KVP("transport_stream_location",transport_stream_location,null)));
			t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transport_stream_id,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",private_data_byte,null)));

		return t;
	}

}
