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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.*;

import nl.digitalekabeltelevisie.controller.KVP;

/**
 *
 * Data field for insertion into VBI, for use with VPS/PDC.
 *
 * @see "ETSI EN 300 231 V1.3.1 (2003-04) Specification of the domestic video Programme Delivery Control system (PDC)"
 */
public class VPSDataField extends EBUDataField {

	/**
	 * @param data
	 * @param offset
	 * @param len
	 */
	public VPSDataField(byte[] data, int offset, int len, long pts) {
		super(data, offset, len, pts);
		// do not instantiatie fields, will be calculated as needed.
	}
	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = super.getJTreeNode(modus);
		s.add(new KVP("vps_data_block",data_block,offset+3,13));
		// offset happens to be same as in  Figure 9: Data format of the programme delivery data in the dedicated TV line ETSI EN 300 231 V1.3.1
		// (starting from byte 3, 0 - 2 are dataUnitId, dataUnitLength ,[reserved_future_use ,field_parity ,line_offset]
		s.add(new KVP("not Relevant byte 3 to 4",getNotRelevant3_4()));
		s.add(new KVP("PCS/Audio",getPCSAudio(),getPCSAudioString(getPCSAudio())));
		s.add(new KVP("Reserved for enhancement of VPS",getReservedEnhancement(),getReservedEnhancement()==0xF?"Unenhanced VPS":""));
		s.add(new KVP("not Relevant byte 6 to 10",data_block,offset+6,5));
		if((getDay()==0)&&(getMonth()==0xF)&&(getMinute()==0x3F)){ //Reserved code values for receiver control
			// TODO ETSI EN 300 231 V1.3.1 (2003-04) p14  PIL
			s.add(new KVP("Service Code",getHour(),getServiceCodeString(getHour())));

		}else{
			s.add(new KVP("day",getDay()));
			s.add(new KVP("month",getMonth()));
			s.add(new KVP("hour",getHour()));
			s.add(new KVP("minute",getMinute()));
		}
		s.add(new KVP("country",getCountry()));
		s.add(new KVP("network",getNetwork()));
		s.add(new KVP("PTY",getPTY()));
		return s;
	}

	private static String getServiceCodeString(int b) {
		return switch (b) {
		case 31 -> "Timer-control Code (TC), indicating that the programme identification information is to be ignored";
		case 30 -> "Recording Inhibit/Terminate code (RI/T), indicating that the transmission has no label and is for example, not intended to be recorded";
		case 29 -> "Interruption code (INT), indicating a break in the programme, which will continue after a short interval";
		case 28 -> "Continuation code, indicating possibly an erroneous transmission state. No action required";
		default -> "reserved for future use";
		};
	}

	/**
	 * @return
	 */
	public int getCountry() {
		return getInt(data_block, offset+13, 2, 0x03C0)>>6;
	}
	/**
	 * @return
	 */
	public int getMinute() {
		return getInt(data_block, offset+13, 1, 0xFC)>>2;
	}
	/**
	 * @return
	 */
	protected int getHour() {
		return getInt(data_block, offset+12, 1, MASK_5BITS);
	}
	/**
	 * @return
	 */
	public int getMonth() {
		return getInt(data_block, offset+11, 2, 0x01E0)>>5;
	}
	/**
	 * @return
	 */
	public int getDay() {
		return getInt(data_block, offset+11, 1, 0x3E)>>1;
	}

	public int getNotRelevant3_4() {
		return getInt(data_block, offset+3, 2, MASK_16BITS);
	}

	public int getReservedEnhancement() {
		return getInt(data_block, offset+5, 1, MASK_4BITS);
	}

	public int getPCSAudio() {
		return getInt(data_block, offset+5, 1, 0xC0)>>6;
	}

	public int getPTY() {
		return getInt(data_block, offset+15, 1, MASK_8BITS);
	}


	public static String getPCSAudioString(int aud) {
        return switch (aud) {
            case 0x0 -> "don't know";
            case 0x1 -> "mono";
            case 0x2 -> "Stereo";
            case 0x3 -> "dual sound";
            default -> "Illegal Value";
        };
	}


	public int getNetwork(){
		return getInt(data_block, offset+11, 1, 0xC0) + getInt(data_block, offset+14, 1, MASK_6BITS);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = (PRIME * result) + dataUnitId;
		result = (PRIME * result) + dataUnitLength;
		result = (PRIME * result) + field_parity;
		result = (PRIME * result) + line_offset;
		result = (PRIME * result) + offset;
		result = (PRIME * result) + reserved_future_use;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VPSDataField other = (VPSDataField) obj;
		if (dataUnitId != other.dataUnitId) {
			return false;
		}
		if (dataUnitLength != other.dataUnitLength) {
			return false;
		}
		if (field_parity != other.field_parity) {
			return false;
		}
		if (getCountry() != other.getCountry()) {
			return false;
		}
		if (getDay() != other.getDay()) {
			return false;
		}
		if (getHour() != other.getHour()) {
			return false;
		}
		if (getMinute() != other.getMinute()) {
			return false;
		}
		if (getMonth() != other.getMonth()) {
			return false;
		}
		if (getNetwork() != other.getNetwork()) {
			return false;
		}
		if (getPCSAudio() != other.getPCSAudio()) {
			return false;
		}
		if (getPTY() != other.getPTY()) {
			return false;
		}
		if (getReservedEnhancement() != other.getReservedEnhancement()) {
			return false;
		}
		if (line_offset != other.line_offset) {
			return false;
		}
		if (offset != other.offset) {
			return false;
		}
        return reserved_future_use == other.reserved_future_use;
    }

}

