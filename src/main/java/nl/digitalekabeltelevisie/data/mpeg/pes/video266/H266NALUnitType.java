/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video266;

import java.util.HashMap;
import java.util.Map;

import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.Sei_rbsp;

/**
 * @author Eric
 *
 */
public enum H266NALUnitType {

	TRAIL_NUT(0,"Coded slice of a trailing picture or subpicture", null),
	STSA_NUT(1,"Coded slice of an STSA picture or subpicture",null),
	RADL_NUT(2,"Coded slice of a RADL picture or subpicture",null),
	RASL_NUT(3,"Coded slice of a RASL picture or subpicture",null),
	RSV_VCL_4(4,"Reserved non-IRAP VCL NAL unit types",null),
	RSV_VCL_5(5,"Reserved non-IRAP VCL NAL unit types",null),
	RSV_VCL_6(6,"Reserved non-IRAP VCL NAL unit types",null),
	IDR_W_RADL(7,"Coded slice of an IDR picture or subpicture",null),
	IDR_N_LP(8,"Coded slice of an IDR picture or subpicture",null),
	CRA_NUT(9,"Coded slice of a CRA picture or subpicture",null),
	GDR_NUT(10,"Coded slice of a GDR picture or subpicture",null),
	RSV_IRAP_11(11,"Reserved IRAP VCL NAL unit type",null),
	OPI_NUT(12,"Operating point information",null),
	DCI_NUT(13,"Decoding capability information",null),
	VPS_NUT(14,"Video parameter set",null),
	SPS_NUT(15,"Sequence parameter set",Seq_parameter_set_rbsp.class),
	PPS_NUT(16,"Picture parameter set",Pic_parameter_set_rbsp.class),
	PREFIX_APS_NUT(17,"Adaptation parameter set",null),
	SUFFIX_APS_NUT(18,"Adaptation parameter set",null),
	PH_NUT(19,"Picture header",null),
	AUD_NUT(20,"AU delimiter",null),
	EOS_NUT(21,"End of sequence",null),
	EOB_NUT(22,"End of bitstream",null),
	PREFIX_SEI_NUT(23,"Supplemental enhancement information",Sei_rbsp.class),
	SUFFIX_SEI_NUT(24,"Supplemental enhancement information",Sei_rbsp.class),
	FD_NUT(25,"Filler data",null),
	RSV_NVCL_26(26,"Reserved non-VCL NAL unit types",null),
	RSV_NVCL_27(27,"Reserved non-VCL NAL unit types",null),

	UNSPEC_28(28,"Unspecified non-VCL NAL unit types",null),
	UNSPEC_29(29,"Unspecified non-VCL NAL unit types",null),
	UNSPEC_30(30,"Unspecified non-VCL NAL unit types",null),
	UNSPEC_31(31,"Unspecified non-VCL NAL unit types",null);
	
	private int type;
	private String description;
	Class<? extends RBSP> clazz;

	private H266NALUnitType(final int type,final String description, Class<? extends RBSP> class1){
		this.type = type;
		this.description = description;
		this.clazz = class1;

	}

	

	private static Map<Integer, H266NALUnitType> enumMap = new HashMap<>();

	static {
		for (final H266NALUnitType e : H266NALUnitType.values()) {
			if (enumMap.put(e.getType(), e) != null) {
				throw new IllegalArgumentException("duplicate id: " + e.getType());
			}
		}
	}

	public static String getDescription(final int type) {
		final H266NALUnitType byType = getByType(type);
		if(byType!=null){
			return byType.getDescription();
		}
		return "unknown";
	}

	public static H266NALUnitType getByType(final int type) {
		return enumMap.get(type);
	}
	

	public int getType() {
		return type;
	}
	public String getDescription() {
		return description;
	}

	public Class<? extends RBSP> getClazz() {
		return clazz;
	}



}
