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

import nl.digitalekabeltelevisie.util.Utils;

public class AdaptationField {

	private int adaptation_field_length = -1;
	private boolean discontinuity_indicator = false;
	private boolean random_access_indicator = false;
	private boolean elementary_stream_priority_indicator = false;
	private boolean PCR_flag = false;
	private boolean OPCR_flag = false;
	private boolean splicing_point_flag = false;
	private boolean transport_private_data_flag = false;
	private boolean adaptation_field_extension_flag = false;
	private PCR program_clock_reference_base = null;
	private PCR original_program_clock_reference_base = null;

	private int offset = 2;

	public AdaptationField(final byte[] data) {
		adaptation_field_length =   Utils.getInt(data,0,1,Utils.MASK_8BITS);
		if(adaptation_field_length >0) {
			discontinuity_indicator = getBit(data[1],1);
			random_access_indicator  = getBit(data[1],2);
			elementary_stream_priority_indicator = getBit(data[1],3);
			PCR_flag = getBit(data[1],4);
			OPCR_flag = getBit(data[1],5);
			splicing_point_flag = getBit(data[1],6);
			transport_private_data_flag = getBit(data[1],7);
			adaptation_field_extension_flag = getBit(data[1],8);
			if(PCR_flag){
				program_clock_reference_base = getPCRfromBytes(data, offset);
				offset+=6; //33+6+9 bits = 6 bytes
			}
			if(OPCR_flag){
				original_program_clock_reference_base = getPCRfromBytes(data, offset);
				offset+=6; //33+6+9 bits = 6 bytes
			}
		}
	}


	/**
	 * Get single bit as boolean from byte
	 * numberings starts from high order bit, starts at 1.
	 * 
	 * @param b singel byte
	 * @param i position of bit in byte, start from 1 up to 8
	 * @return boolen true if bit is set
	 */
	public static boolean getBit(final byte b, final int i) {
		return (( b & (0x80 >> (i-1))) != 0);
	}

	private static PCR getPCRfromBytes(final byte[] array, final int offset)
	{
		long pcr = ((long)Utils.getUnsignedByte(array[offset]))<<25;
		pcr |= ((long)Utils.getUnsignedByte(array[offset + 1]))<<17;
		pcr |= ((long)Utils.getUnsignedByte(array[offset + 2]))<<9 ;
		pcr |=((long)Utils.getUnsignedByte(array[offset + 3]))<<1 ;
		pcr |= ((long)(0x80 & Utils.getUnsignedByte(array[offset + 4])))>>>7;
		final long reserved =(0x7E & Utils.getUnsignedByte(array[offset + 4]))>>>1;

		long pcr_extension = (0x01 & (Utils.getUnsignedByte(array[offset+4])))<<8;
		pcr_extension |= Utils.getUnsignedByte(array[offset+5]);

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


	public PCR getOriginal_program_clock_reference_base() {
		return original_program_clock_reference_base;
	}


	public void setOriginal_program_clock_reference_base(
			final PCR original_program_clock_reference_base) {
		this.original_program_clock_reference_base = original_program_clock_reference_base;
	}


	public boolean isPCR_flag() {
		return PCR_flag;
	}


	public void setPCR_flag(final boolean pcr_flag) {
		PCR_flag = pcr_flag;
	}


	public PCR getProgram_clock_reference_base() {
		return program_clock_reference_base;
	}


	public void setProgram_clock_reference_base(final PCR program_clock_reference_base) {
		this.program_clock_reference_base = program_clock_reference_base;
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
}
