/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;

import java.util.*;

public enum NALUnitType {

	TRAIL_N(0,"Coded slice segment of a non-TSA, non-STSA trailing picture"),
	TRAIL_R(1,"Coded slice segment of a non-TSA, non-STSA trailing picture"),
	TSA_N(2,"Coded slice segment of a TSA picture"),
	TSA_R(3,"Coded slice segment of a TSA picture"),
	STSA_N(4,"Coded slice segment of an STSA picture"),
	STSA_R(5,"Coded slice segment of an STSA picture"),
	RADL_N(6,"Coded slice segment of a RADL picture"),
	RADL_R(7,"Coded slice segment of a RADL picture"),
	RASL_N(8,"Coded slice segment of a RASL picture"),
	RASL_R(9,"Coded slice segment of a RASL picture"),
	RSV_VCL_N10(10,"Reserved non-IRAP SLNR VCL NAL unit types"),
	RSV_VCL_R11(11,"Reserved non-IRAP sub-layer reference VCL NAL unit types"),
	RSV_VCL_N12(12,"Reserved non-IRAP SLNR VCL NAL unit types"),
	RSV_VCL_R13(13,"Reserved non-IRAP sub-layer reference VCL NAL unit types"),
	RSV_VCL_N14(14,"Reserved non-IRAP SLNR VCL NAL unit types"),
	RSV_VCL_R15(15,"Reserved non-IRAP sub-layer reference VCL NAL unit types"),
	BLA_W_LP(16,"Coded slice segment of a BLA picture"),
	BLA_W_RADL(17,"Coded slice segment of a BLA picture"),
	BLA_N_LP(18,"Coded slice segment of a BLA picture"),
	IDR_W_RADL(19,"Coded slice segment of an IDR picture"),
	IDR_N_LP(20,"Coded slice segment of an IDR picture"),
	CRA_NUT(21,"Coded slice segment of a CRA picture"),
	RSV_IRAP_VCL22(22,"Reserved IRAP VCL NAL unit types"),
	RSV_IRAP_VCL23(23,"Reserved IRAP VCL NAL unit types"),
	VPS_NUT(32,"Video parameter set"),
	SPS_NUT(33,"Sequence parameter set"),
	PPS_NUT(34,"Picture parameter set"),
	AUD_NUT(35,"Access unit delimiter"),
	EOS_NUT(36,"End of sequence"),
	EOB_NUT(37,"End of bitstream"),
	FD_NUT(38,"Filler data"),
	PREFIX_SEI_NUT(39,"Supplemental enhancement information"),
	SUFFIX_SEI_NUT(40,"Supplemental enhancement information");


	private NALUnitType(final int type,final String description){
		this.type = type;
		this.description = description;

	}

	private static Map<Integer, NALUnitType> enumMap = new HashMap<Integer, NALUnitType>();

	static {
		for (final NALUnitType e : NALUnitType.values()) {
			if (enumMap.put(e.getType(), e) != null) {
				throw new IllegalArgumentException("duplicate id: " + e.getType());
			}
		}
	}

	public static String getDescription(final int type) {
		final NALUnitType byType = getByType(type);
		if(byType!=null){
			return byType.getDescription();
		}else{
			return "unknown";
		}
	}

	public static NALUnitType getByType(final int type) {
		return enumMap.get(type);
	}
	private int type;
	private String description;

	public int getType() {
		return type;
	}
	public String getDescription() {
		return description;
	}



}
