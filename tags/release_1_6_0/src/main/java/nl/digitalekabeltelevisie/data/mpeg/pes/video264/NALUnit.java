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
package nl.digitalekabeltelevisie.data.mpeg.pes.video264;


import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.*;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

public class NALUnit extends AbstractNALUnit implements TreeNode {

	private final int forbidden_zero_bit;
	private final int nal_ref_idc;
	private final int nal_unit_type;
	
	private int svc_extension_flag;
	private int avc_3d_extension_flag;
	
	NalUnitHeaderMvcExtension nalUnitHeaderMvcExtension;
	/**
	 * @param bytes
	 * @param offset
	 * @param len
	 */
	public NALUnit(final byte[] bytes, final int offset, final int len) {
		super(bytes, offset, len);

		this.forbidden_zero_bit = bs.readBits(1);

		this.nal_ref_idc = bs.readBits(2);
		this.nal_unit_type = bs.readBits(5);

		if( (nal_unit_type == 14) || (nal_unit_type == 20) || (nal_unit_type == 21) ) {
			if(nal_unit_type != 21 ){
				svc_extension_flag = bs.u(1);
			}else{
				avc_3d_extension_flag = bs.u(1);
			}
			if( svc_extension_flag==1 ) {
				// nal_unit_header_svc_extension()
				logger.warning("Not implemented nal_unit_header_svc_extension");
				bs.readBits(24);
			} else if( avc_3d_extension_flag==1 ) {
				//al_unit_header_3davc_extension()
				logger.warning("Not implemented al_unit_header_3davc_extension");
				bs.readBits(16);
			} else {
				//nal_unit_header_mvc_extension( )
				nalUnitHeaderMvcExtension = new NalUnitHeaderMvcExtension(bs);
			}
		}
		
		readRBSPBytes();

		createRBSP();



	}


	/**
	 * 
	 */
	@Override
	protected void createRBSP() {
		if(nal_unit_type==1){
			rbsp=new Slice_layer_without_partitioning_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==5){
			rbsp=new Slice_layer_without_partitioning_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==6){
			rbsp=new Sei_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==7){
			rbsp=new Seq_parameter_set_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==8){
			rbsp=new Pic_parameter_set_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==9){
			rbsp=new Access_unit_delimiter_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==12){
			rbsp=new Filler_data_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==15){
			rbsp=new Subset_seq_parameter_set_rbsp(rbsp_byte, numBytesInRBSP);
		}else if(nal_unit_type==20){
			rbsp=new Slice_layer_extension_rbsp(rbsp_byte, numBytesInRBSP, svc_extension_flag, avc_3d_extension_flag);
		}else{
			logger.warning("not implemented nal_unit_type: "+nal_unit_type+" ("+getNALUnitTypeString(nal_unit_type)+")");
			
		}
	}


	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("NALUnit ("+getNALUnitTypeString(nal_unit_type)+")"));
		t.add(new DefaultMutableTreeNode(new KVP("bytes",bytes,offset,numBytesInNALunit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("numBytesInNALunit",numBytesInNALunit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("forbidden_zero_bit",forbidden_zero_bit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("nal_ref_idc",nal_ref_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("nal_unit_type",nal_unit_type,getNALUnitTypeString(nal_unit_type))));

		if( (nal_unit_type == 14) || (nal_unit_type == 20) || (nal_unit_type == 21) ) {
			if(nal_unit_type != 21 ){
				t.add(new DefaultMutableTreeNode(new KVP("svc_extension_flag",svc_extension_flag,null)));
			}else{
				t.add(new DefaultMutableTreeNode(new KVP("avc_3d_extension_flag",avc_3d_extension_flag,null)));
			}
			if( svc_extension_flag==1 ) {
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("nal_unit_header_svc_extension()")));
			} else if( avc_3d_extension_flag==1 ) {
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("nal_unit_header_3davc_extension()")));
			} else {
				t.add(nalUnitHeaderMvcExtension.getJTreeNode(modus));
			}
		}

		t.add(new DefaultMutableTreeNode(new KVP("rbsp_byte",rbsp_byte,0,numBytesInRBSP,null)));
		t.add(new DefaultMutableTreeNode(new KVP("NumBytesInRBSP",numBytesInRBSP,null)));
		if(rbsp!=null){
			t.add(rbsp.getJTreeNode(modus));
		}
		return t;
	}


	public String getNALUnitTypeString(final int nal_unit_type) {

		switch (nal_unit_type) {
		case 0: return "Unspecified";
		case 1: return "Coded slice of a non-IDR picture";
		case 2 : return "Coded slice data partition A";
		case 3 : return "Coded slice data partition B";
		case 4 : return "Coded slice data partition C";
		case 5 : return "Coded slice of an IDR picture";
		case 6 : return "Supplemental enhancement information (SEI)";
		case 7 : return "Sequence parameter set";
		case 8 : return "Picture parameter set";
		case 9 : return "Access unit delimiter";
		case 10 : return "End of sequence";
		case 11 : return "End of stream";
		case 12 : return "Filler data";
		case 13 : return "Sequence parameter set extension";
		case 14 : return "Prefix NAL unit";
		case 15 : return "Subset sequence parameter set";
		case 16 : return "Depth parameter set";
		case 19 : return "Coded slice of an auxiliary coded picture without partitioning";
		case 20 : return "Coded slice extension";
		case 21 : return "Coded slice extension for a depth view component or a 3D-AVC texture view component";

		// RFC 6184 RTP Payload Format for H.264 Video
		// https://tools.ietf.org/html/rfc6184#section-5.2
		case 24 : return "Single-time aggregation packet without DON (STAP-A) RFC 6184";
		case 25 : return "Single-time aggregation packet including DON (STAP-B) RFC 6184";
		case 26 : return "Multi-time aggregation packet (MTAP16) RFC 6184";
		case 27 : return "Multi-time aggregation packet (MTAP24) RFC 6184";
		case 28 : return "Fragmentation unit (FU-A) RFC 6184";
		case 29 : return "Fragmentation unit (FU-B) RFC 6184";
		default:
			return "reserved";
		}
	}

	public int getForbidden_zero_bit() {
		return forbidden_zero_bit;
	}

	public int getNal_ref_idc() {
		return nal_ref_idc;
	}

	public int getNal_unit_type() {
		return nal_unit_type;
	}

}
