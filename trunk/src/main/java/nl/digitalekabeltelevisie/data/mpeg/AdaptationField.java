/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.gui.HTMLSource;


public class AdaptationField implements HTMLSource, nl.digitalekabeltelevisie.controller.TreeNode{

	private static Logger	logger	= Logger.getLogger(AdaptationField.class.getName());

	private int adaptation_field_length = -1;
	private boolean discontinuity_indicator = false;
	private boolean random_access_indicator = false;
	private boolean elementary_stream_priority_indicator = false;
	private boolean PCR_flag = false;
	private boolean OPCR_flag = false;
	private boolean splicing_point_flag = false;
	private boolean transport_private_data_flag = false;
	private boolean adaptation_field_extension_flag = false;
	private PCR program_clock_reference = null;
	private PCR original_program_clock_reference = null;
	private int splice_countdown = 0;

	// TODO (splicing_point_flag = = '1', transport_private_data_flag = = '1', adaptation_field_extension_flag = = '1'..

	private int offset = 2;

	private int transport_private_data_length = 0;

	private int adaptation_field_extension_length;

	private boolean ltw_flag;

	private boolean piecewise_rate_flag;

	private boolean seamless_splice_flag;

	private byte[] private_data_byte;

	private boolean ltw_valid_flag;

	private int ltw_offset;


	public AdaptationField(final byte[] data) {
		adaptation_field_length =   getInt(data,0,1,MASK_8BITS);
		if(adaptation_field_length >0) {
			discontinuity_indicator = getBitAsBoolean(data[1],1);
			random_access_indicator  = getBitAsBoolean(data[1],2);
			elementary_stream_priority_indicator = getBitAsBoolean(data[1],3);
			PCR_flag = getBitAsBoolean(data[1],4);
			OPCR_flag = getBitAsBoolean(data[1],5);
			splicing_point_flag = getBitAsBoolean(data[1],6);
			transport_private_data_flag = getBitAsBoolean(data[1],7);
			adaptation_field_extension_flag = getBitAsBoolean(data[1],8);
			if(PCR_flag){
				program_clock_reference = getPCRfromBytes(data, offset);
				offset+=6; //33+6+9 bits = 6 bytes
			}
			if(OPCR_flag){
				original_program_clock_reference = getPCRfromBytes(data, offset);
				offset+=6; //33+6+9 bits = 6 bytes
			}
			if(splicing_point_flag){
				splice_countdown =  getInt(data,offset,1,MASK_8BITS);
				offset+=1;
			}
			if(transport_private_data_flag){
				transport_private_data_length = getInt(data,offset,1,MASK_8BITS);
				offset+= 1;
				private_data_byte = getBytes(data, offset, adaptation_field_extension_length);
				offset+= transport_private_data_length ;
			}
			if(adaptation_field_extension_flag){
				adaptation_field_extension_length =  getInt(data,offset,1,MASK_8BITS);
				offset+=1;
				ltw_flag = getBitAsBoolean(data[offset],1);
				piecewise_rate_flag =  getBitAsBoolean(data[offset],2);
				seamless_splice_flag =  getBitAsBoolean(data[offset],3);
				offset+=1;
				if(ltw_flag){
					ltw_valid_flag = getBitAsBoolean(data[offset],1);
					ltw_offset = getInt(data,offset,2,MASK_15BITS);

				}

			}
		}
	}


	private static PCR getPCRfromBytes(final byte[] array, final int offset)
	{
		long pcr = ((long)getUnsignedByte(array[offset]))<<25;
		pcr |= ((long)getUnsignedByte(array[offset + 1]))<<17;
		pcr |= ((long)getUnsignedByte(array[offset + 2]))<<9 ;
		pcr |=((long)getUnsignedByte(array[offset + 3]))<<1 ;
		pcr |= ((long)(0x80 & getUnsignedByte(array[offset + 4])))>>>7;
		final long reserved =(0x7E & getUnsignedByte(array[offset + 4]))>>>1;

		long pcr_extension = (0x01 & (getUnsignedByte(array[offset+4])))<<8;
		pcr_extension |= getUnsignedByte(array[offset+5]);

		return new PCR(pcr, reserved, pcr_extension);
	}


	public boolean isAdaptation_field_extension_flag() {
		return adaptation_field_extension_flag;
	}


	public void setAdaptation_field_extension_flag(
			final boolean adaptation_field_extension_flag) {
		this.adaptation_field_extension_flag = adaptation_field_extension_flag;
	}


	public int getAdaptation_field_length() {
		return adaptation_field_length;
	}


	public void setAdaptation_field_length(final int adaptation_field_length) {
		this.adaptation_field_length = adaptation_field_length;
	}


	public boolean isDiscontinuity_indicator() {
		return discontinuity_indicator;
	}


	public void setDiscontinuity_indicator(final boolean discontinuity_indicator) {
		this.discontinuity_indicator = discontinuity_indicator;
	}


	public boolean isElementary_stream_priority_indicator() {
		return elementary_stream_priority_indicator;
	}


	public void setElementary_stream_priority_indicator(
			final boolean elementary_stream_priority_indicator) {
		this.elementary_stream_priority_indicator = elementary_stream_priority_indicator;
	}


	public boolean isOPCR_flag() {
		return OPCR_flag;
	}


	public void setOPCR_flag(final boolean opcr_flag) {
		OPCR_flag = opcr_flag;
	}


	public PCR getOriginal_program_clock_reference() {
		return original_program_clock_reference;
	}


	public void setOriginal_program_clock_reference(
			final PCR original_program_clock_reference_base) {
		this.original_program_clock_reference = original_program_clock_reference_base;
	}


	public boolean isPCR_flag() {
		return PCR_flag;
	}


	public void setPCR_flag(final boolean pcr_flag) {
		PCR_flag = pcr_flag;
	}


	public PCR getProgram_clock_reference() {
		return program_clock_reference;
	}


	public void setProgram_clock_reference(final PCR program_clock_reference_base) {
		this.program_clock_reference = program_clock_reference_base;
	}


	public boolean isRandom_access_indicator() {
		return random_access_indicator;
	}


	public void setRandom_access_indicator(final boolean random_access_indicator) {
		this.random_access_indicator = random_access_indicator;
	}


	public boolean isSplicing_point_flag() {
		return splicing_point_flag;
	}


	public void setSplicing_point_flag(final boolean splicing_point_flag) {
		this.splicing_point_flag = splicing_point_flag;
	}


	public boolean isTransport_private_data_flag() {
		return transport_private_data_flag;
	}


	public void setTransport_private_data_flag(final boolean transport_private_data_flag) {
		this.transport_private_data_flag = transport_private_data_flag;
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
	 */
	@Override
	public String getHTML() {
		StringBuilder s = new StringBuilder();

		s.append("<br>adaptation_field_length: ").append(getHexAndDecimalFormattedString(getAdaptation_field_length()));
		s.append("<br>discontinuity_indicator: ").append(getBooleanAsInt(discontinuity_indicator));
		s.append("<br>random_access_indicator: ").append(getBooleanAsInt(random_access_indicator));
		s.append("<br>elementary_stream_priority_indicator: ").append(getBooleanAsInt(elementary_stream_priority_indicator));
		s.append("<br>PCR_flag: ").append(getBooleanAsInt(PCR_flag));
		s.append("<br>OPCR_flag: ").append(getBooleanAsInt(OPCR_flag));
		s.append("<br>splicing_point_flag: ").append(getBooleanAsInt(splicing_point_flag));
		s.append("<br>transport_private_data_flag: ").append(getBooleanAsInt(transport_private_data_flag));
		s.append("<br>adaptation_field_extension_flag: ").append(getBooleanAsInt(adaptation_field_extension_flag));
		if(PCR_flag){
			s.append("<br>PCR: ").append(getHexAndDecimalFormattedString(program_clock_reference.getProgram_clock_reference())).append(" (").append(printPCRTime(program_clock_reference.getProgram_clock_reference())).append(")");
		}
		if(OPCR_flag){
			s.append("<br>OPCR: ").append(getHexAndDecimalFormattedString(original_program_clock_reference.getProgram_clock_reference())).append(" (").append(printPCRTime(original_program_clock_reference.getProgram_clock_reference())).append(")");
		}
		if(splicing_point_flag){
			s.append("<br>splice_countdown: ").append(getHexAndDecimalFormattedString(splice_countdown));
		}
		if(transport_private_data_flag){
			s.append("<br>transport_private_data_length: ").append(getHexAndDecimalFormattedString(transport_private_data_length));
			s.append("<br>private_data_byte: ").append("0x").append(toHexString(private_data_byte, 0)).append(" \"").append(
					toSafeString(private_data_byte)).append("\"");
		}
		if(adaptation_field_extension_flag){
			s.append("<br>adaptation_field_extension_length: ").append(getHexAndDecimalFormattedString(adaptation_field_extension_length));
			s.append("<br>ltw_flag: ").append(getBooleanAsInt(ltw_flag));
			s.append("<br>piecewise_rate_flag: ").append(getBooleanAsInt(piecewise_rate_flag));
			s.append("<br>seamless_splice_flag: ").append(getBooleanAsInt(seamless_splice_flag));
			if(ltw_flag){
				s.append("<br>ltw_valid_flag: ").append(getBooleanAsInt(ltw_valid_flag));
				s.append("<br>ltw_offset: ").append(getHexAndDecimalFormattedString(ltw_offset));

			}
			if(piecewise_rate_flag){
				s.append("<br>piecewise_rate_flag: <span style=\"color:red\">Not implemented, please report!</span>");
				logger.info("piecewise_rate_flag: Not implemented, please report!");
			}
			if(seamless_splice_flag){
				s.append("<br>seamless_splice_flag: <span style=\"color:red\">Not implemented, please report!</span>");
				logger.info("seamless_splice_flag: Not implemented, please report!");
			}
		}

		return s.toString();
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Adaptation Field"));
		t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_length",getAdaptation_field_length() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("discontinuity_indicator",getBooleanAsInt(discontinuity_indicator) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("random_access_indicator",getBooleanAsInt(random_access_indicator) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("elementary_stream_priority_indicator",getBooleanAsInt(elementary_stream_priority_indicator) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("PCR_flag",getBooleanAsInt(PCR_flag) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("OPCR_flag",getBooleanAsInt(OPCR_flag) ,null)));

		t.add(new DefaultMutableTreeNode(new KVP("splicing_point_flag",getBooleanAsInt(splicing_point_flag) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("transport_private_data_flag",getBooleanAsInt(transport_private_data_flag) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_extension_flag",getBooleanAsInt(adaptation_field_extension_flag) ,null)));

//		t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_length",getAdaptation_field_length() ,null)));

		if(PCR_flag){
			t.add(program_clock_reference.getJTreeNode(modus, "PCR"));
		}

		if(OPCR_flag){
			t.add(original_program_clock_reference.getJTreeNode(modus, "OPCR"));
		}


		if(splicing_point_flag){
			t.add(new DefaultMutableTreeNode(new KVP("splice_countdown",splice_countdown ,null)));
		}
		if(transport_private_data_flag){
			t.add(new DefaultMutableTreeNode(new KVP("transport_private_data_length",transport_private_data_length ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",private_data_byte ,null)));

		}

		if(adaptation_field_extension_flag){
			t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_extension_length",adaptation_field_extension_length ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("ltw_flag",getBooleanAsInt(ltw_flag) ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("piecewise_rate_flag",getBooleanAsInt(piecewise_rate_flag) ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("seamless_splice_flag",getBooleanAsInt(seamless_splice_flag) ,null)));

			if(ltw_flag){
				t.add(new DefaultMutableTreeNode(new KVP("ltw_valid_flag",getBooleanAsInt(ltw_valid_flag) ,null)));
				t.add(new DefaultMutableTreeNode(new KVP("ltw_offset",ltw_offset ,null)));
			}
			if(piecewise_rate_flag){
				t.add(new DefaultMutableTreeNode(new KVP("piecewise_rate NOT implemented in DVB Inspector. Please report")));

			}
			if(seamless_splice_flag){
				t.add(new DefaultMutableTreeNode(new KVP("seamless_splice_flag NOT implemented in DVB Inspector. Please report")));

			}
		}

		return t;

	}
}
