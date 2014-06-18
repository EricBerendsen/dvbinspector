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
import nl.digitalekabeltelevisie.util.LookUpList;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Based on ISO/IEC 13818-1:2013, ch.2.6.60, and ETSI TS 102 323 V1.5.1, ch.5.3.4
 *
 * @author Eric
 *
 */
public class MetaDataDescriptor extends Descriptor {

	private static LookUpList decoder_config_flags_list = new LookUpList.Builder().
			add(0, "No decoder configuration is needed").
			add(1, "The decoder configuration is carried in this descriptor in the decoder_config_byte field").
			add(2,"The decoder configuration is carried in the same metadata service as to which this metadata descriptor applies").
			add(3,"The decoder configuration is carried in a DSM-CC carousel. This value shall only be used if the metadata service to which this descriptor applies is using the same type of DSM-CC carousel").
			add(4,"The decoder configuration is carried in another metadata service within the same program, as identified by the decoder_config_metadata_service_id field in this metadata descriptor").
			add(5,6,"Reserved").
			add(7,"Privately defined").
			build();


	private int metadata_application_format;
	private long metadata_application_format_identifier;
	private int metadata_format;
	private long metadata_format_identifier;
	private int metadata_service_id;
	private int decoder_config_flags;
	private int dsm_cc_flag;
	private int reserved;
	private int service_identification_length = 0;
	private byte[] service_identification_record_byte;
	private int decoder_config_length;
	private byte[] decoder_config_byte;
	private byte[] dec_config_identification_record_byte;
	private int dec_config_identification_record_length;
	private int decoder_config_metadata_service_id;
	private int reserved_data_length;
	private byte[] reserved2;
	private byte[] private_data_byte;


	public MetaDataDescriptor(final byte[] b, final int offset, final TableSection parent) {
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
		decoder_config_flags = getInt(b, localOffset, 1, 0xE0)>>5;
		dsm_cc_flag = getInt(b, localOffset, 1, 0x10)>>4;
		reserved = getInt(b, localOffset++, 1, MASK_4BITS);
		if(dsm_cc_flag==1){
			service_identification_length = getInt(b, localOffset++, 1, MASK_8BITS);
			service_identification_record_byte = getBytes(b, localOffset, service_identification_length);
			localOffset += service_identification_length;
		}
		if(decoder_config_flags==1){ // 001
			decoder_config_length = getInt(b, localOffset++, 1, MASK_8BITS);
			decoder_config_byte = getBytes(b, localOffset, decoder_config_length);
			localOffset += decoder_config_length;
		}
		if (decoder_config_flags == 3) { // '011'
			dec_config_identification_record_length = getInt(b, localOffset++, 1, MASK_8BITS);
			dec_config_identification_record_byte = getBytes(b, localOffset, dec_config_identification_record_length);
			localOffset += dec_config_identification_record_length;
		}
		if (decoder_config_flags == 4) { // '100'
			decoder_config_metadata_service_id = getInt(b, localOffset++, 1, MASK_8BITS);
		}
		if ((decoder_config_flags == 5)|| (decoder_config_flags == 6)){// '101'|'110'
			reserved_data_length = getInt(b, localOffset++, 1, MASK_8BITS);
			reserved2 = getBytes(b, localOffset, dec_config_identification_record_length);
			localOffset += reserved_data_length;
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
		t.add(new DefaultMutableTreeNode(new KVP("metadata_service_id",metadata_service_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("decoder_config_flags",decoder_config_flags,getDecoderConfigFlagsString(decoder_config_flags))));
		t.add(new DefaultMutableTreeNode(new KVP("DSM-CC_flag",dsm_cc_flag,dsm_cc_flag==1?"the stream with which this descriptor is associated is carried in an ISO/IEC 13818-6 data or object carousel":null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));

		if(dsm_cc_flag==1){
			t.add(new DefaultMutableTreeNode(new KVP("service_identification_length",service_identification_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("service_identification_record_byte",service_identification_record_byte,null)));
		}
		if(decoder_config_flags==1){ // 001
			t.add(new DefaultMutableTreeNode(new KVP("decoder_config_length",decoder_config_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("decoder_config_byte",decoder_config_byte,null)));
		}
		if (decoder_config_flags == 3) { // '011'
			t.add(new DefaultMutableTreeNode(new KVP("dec_config_identification_record_length",dec_config_identification_record_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("dec_config_identification_record_byte",dec_config_identification_record_byte,null)));
		}
		if (decoder_config_flags == 4) { // '100'
			t.add(new DefaultMutableTreeNode(new KVP("decoder_config_metadata_service_id",decoder_config_metadata_service_id,null)));
		}
		if ((decoder_config_flags == 5)|| (decoder_config_flags == 6)){// '101'|'110'
			t.add(new DefaultMutableTreeNode(new KVP("reserved_data_length",reserved_data_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",private_data_byte,null)));

		return t;
	}

	public static String getDecoderConfigFlagsString(int decoder_config_flags){
		return decoder_config_flags_list.get(decoder_config_flags);
	}
}
