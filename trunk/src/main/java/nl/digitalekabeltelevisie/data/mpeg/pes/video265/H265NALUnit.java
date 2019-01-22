/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

	@Override
	public String getNALUnitTypeString(final int nal_unit_type) {
		return NALUnitType.getDescription(nal_unit_type);
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.video26x.AbstractNALUnit#createRBSP()
	 */
	@Override
	protected void createRBSP() {
		try {
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
			case TRAIL_N:
			case TRAIL_R:
			case TSA_N:
			case TSA_R:
			case STSA_N:
			case STSA_R:
			case RADL_N:
			case RADL_R:
			case RASL_N:
			case RASL_R:
			case BLA_W_LP:
			case BLA_W_RADL:
			case BLA_N_LP:
			case IDR_W_RADL:
			case IDR_N_LP:
			case CRA_NUT:
				rbsp = new Slice_segment_layer_rbsp(rbsp_byte, numBytesInRBSP, nal_unit_type);
				break;
				
			case FD_NUT:
				rbsp = new Filler_data_rbsp(rbsp_byte, forbidden_zero_bit);
				break; 
			default:
				logger.info("unimplemented nal_unit_type:" +nal_unit_type.getType() + ", " + nal_unit_type.getDescription());
			}
		} catch (Exception e) {
			logger.warning("H265: Create NALUnit failed:  nal_unit_type:"+nal_unit_type+",exception="+ e +", msg: "+e.getMessage());
		}
	}

	public int getForbidden_zero_bit() {
		return forbidden_zero_bit;
	}

	public NALUnitType getNal_unit_type() {
		return nal_unit_type;
	}

	public int getNuh_layer_id() {
		return nuh_layer_id;
	}

	public int getNuh_temporal_id_plus1() {
		return nuh_temporal_id_plus1;
	}

}
