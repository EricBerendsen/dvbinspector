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


import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.*;

public class H265NALUnit extends AbstractNALUnit implements TreeNode {

	private final int forbidden_zero_bit;
	private final NALUnitType nal_unit_type;
	private final int nuh_layer_id;
	private final int nuh_temporal_id_plus1;


	/**
	 * @param bytes
	 * @param offset
	 * @param len
	 */
	public H265NALUnit(final byte[] bytes, final int offset, final int len) {
		super(bytes, offset, len);

		this.forbidden_zero_bit = bs.readBits(1);
		this.nal_unit_type = NALUnitType.getByType(bs.readBits(6));
		this.nuh_layer_id = bs.readBits(6);
		this.nuh_temporal_id_plus1 = bs.readBits(3);

		readRBSPBytes();
		createRBSP();
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("NALUnit ("+nal_unit_type.name()+" "+ nal_unit_type.getDescription()+")"));
		t.add(new DefaultMutableTreeNode(new KVP("bytes",bytes,offset,numBytesInNALunit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("numBytesInNALunit",numBytesInNALunit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("forbidden_zero_bit",forbidden_zero_bit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("nal_unit_type",nal_unit_type.getType(),nal_unit_type.getDescription())));
		t.add(new DefaultMutableTreeNode(new KVP("nuh_layer_id",nuh_layer_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("nuh_temporal_id_plus1",nuh_temporal_id_plus1,null)));

		t.add(new DefaultMutableTreeNode(new KVP("rbsp_byte",rbsp_byte,0,numBytesInRBSP,null)));
		t.add(new DefaultMutableTreeNode(new KVP("NumBytesInRBSP",numBytesInRBSP,null)));
		if(rbsp!=null){
			t.add(rbsp.getJTreeNode(modus));
		}
		return t;
	}

	public String getNALUnitTypeString(final int nal_unit_type) {
		return NALUnitType.getDescription(nal_unit_type);
	}

	//	public String getNALUnitTypeString(final int nal_unit_type) {
	//
	//		switch (nal_unit_type) {
	//		case 0: return "TRAIL_N Coded slice segment of a non-TSA, non-STSA trailing picture";
	//		case 1: return "TRAIL_R Coded slice segment of a non-TSA, non-STSA trailing picture";
	//		case 2 : return "TSA_N Coded slice segment of a TSA picture";
	//		case 3 : return "TSA_R Coded slice segment of a TSA picture";
	//		case 4 : return "STSA_N Coded slice segment of an STSA picture";
	//		case 5 : return "STSA_R Coded slice segment of an STSA picture";
	//		case 6 : return "RADL_N Coded slice segment of a RADL picture";
	//		case 7 : return "RADL_R Coded slice segment of a RADL picture";
	//		case 8 : return "RASL_N Coded slice segment of a RASL picture";
	//		case 9 : return "RASL_R Coded slice segment of a RASL picture";
	//		case 10 : return "RSV_VCL_N10 Reserved non-IRAP SLNR VCL NAL unit types";
	//		case 11 : return "RSV_VCL_R11 Reserved non-IRAP sub-layer reference VCL NAL unit types";
	//		case 12 : return "RSV_VCL_N12 Reserved non-IRAP SLNR VCL NAL unit types";
	//		case 13 : return "RSV_VCL_R13 Reserved non-IRAP sub-layer reference VCL NAL unit types";
	//		case 14 : return "RSV_VCL_N14 Reserved non-IRAP SLNR VCL NAL unit types";
	//		case 15 : return "RSV_VCL_R15 Reserved non-IRAP sub-layer reference VCL NAL unit types";
	//		case 16 : return "BLA_W_LP Coded slice segment of a BLA picture";
	//		case 17 : return "BLA_W_RADL Coded slice segment of a BLA picture";
	//		case 18 : return "BLA_N_LP Coded slice segment of a BLA picture";
	//		case 19 : return "IDR_W_RADL Coded slice segment of an IDR picture";
	//		case 20 : return "IDR_N_LP Coded slice segment of an IDR picture";
	//		case 21 : return "CRA_NUT Coded slice segment of a CRA picture";
	//		case 22 : return "RSV_IRAP_VCL22 Reserved IRAP VCL NAL unit types";
	//		case 23 : return "RSV_IRAP_VCL23 Reserved IRAP VCL NAL unit types";
	//		case 32 : return "VPS_NUT Video parameter set";
	//		case 33 : return "SPS_NUT Sequence parameter set";
	//
	//		case 34 : return "PPS_NUT Picture parameter set";
	//		case 35 : return "AUD_NUT Access unit delimiter";
	//
	//		case 36 : return "EOS_NUT End of sequence";
	//		case 37 : return "EOB_NUT End of bitstream";
	//		case 38 : return "FD_NUT Filler data";
	//		case 39 : return "PREFIX_SEI_NUT Supplemental enhancement information";
	//		case 40 : return "SUFFIX_SEI_NUT Supplemental enhancement information";
	//
	//		default:
	//			return "reserved";
	//		}
	//	}
	//

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.video26x.AbstractNALUnit#createRBSP()
	 */
	@Override
	protected void createRBSP() {
		switch(nal_unit_type){
		case VPS_NUT:
			rbsp=new Video_parameter_set_rbsp(rbsp_byte, numBytesInRBSP);
			break;
		case SPS_NUT:
			rbsp=new Seq_parameter_set_rbsp(rbsp_byte, numBytesInRBSP);
			break;
		case PPS_NUT:
			rbsp=new Pic_parameter_set_rbsp(rbsp_byte, numBytesInRBSP);
			break;
		case AUD_NUT:
			rbsp=new Access_unit_delimiter_rbsp(rbsp_byte, numBytesInRBSP);
			break;
		case PREFIX_SEI_NUT:
		case SUFFIX_SEI_NUT:
			rbsp=new Sei_rbsp(rbsp_byte, numBytesInRBSP);
			break;
		default:
			logger.info("unimplemented nal_unit_type:" +nal_unit_type.getType() + ", " + nal_unit_type.getDescription());
		}
	}

}
