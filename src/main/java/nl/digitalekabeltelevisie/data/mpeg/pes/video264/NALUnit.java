/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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


import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.AbstractNALUnit;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.Filler_data_rbsp;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.Sei_rbsp;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

public class NALUnit extends AbstractNALUnit {

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
		}else if(nal_unit_type==10){
			rbsp=new End_of_sequence_rbsp(rbsp_byte, numBytesInRBSP);
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


	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("NALUnit ("+getNALUnitTypeString(nal_unit_type)+")");
		t.add(new KVP("bytes",bytes,offset,numBytesInNALunit));
		t.add(new KVP("numBytesInNALunit",numBytesInNALunit));
		t.add(new KVP("forbidden_zero_bit",forbidden_zero_bit));
		t.add(new KVP("nal_ref_idc",nal_ref_idc));
		t.add(new KVP("nal_unit_type",nal_unit_type,getNALUnitTypeString(nal_unit_type)));

		if( (nal_unit_type == 14) || (nal_unit_type == 20) || (nal_unit_type == 21) ) {
			if(nal_unit_type != 21 ){
				t.add(new KVP("svc_extension_flag",svc_extension_flag));
			}else{
				t.add(new KVP("avc_3d_extension_flag",avc_3d_extension_flag));
			}
			if( svc_extension_flag==1 ) {
				t.add(GuiUtils.getNotImplementedKVP("nal_unit_header_svc_extension()"));
			} else if( avc_3d_extension_flag==1 ) {
				t.add(GuiUtils.getNotImplementedKVP("nal_unit_header_3davc_extension()"));
			} else {
				t.add(nalUnitHeaderMvcExtension.getJTreeNode(modus));
			}
		}

		t.add(new KVP("rbsp_byte",rbsp_byte,0,numBytesInRBSP));
		t.add(new KVP("NumBytesInRBSP",numBytesInRBSP));
		if(rbsp!=null){
			t.add(rbsp.getJTreeNode(modus));
		}
		return t;
	}


	public String getNALUnitTypeString(final int nal_unit_type) {

		return switch (nal_unit_type) {
		case 0 -> "Unspecified";
		case 1 -> "Coded slice of a non-IDR picture";
		case 2 -> "Coded slice data partition A";
		case 3 -> "Coded slice data partition B";
		case 4 -> "Coded slice data partition C";
		case 5 -> "Coded slice of an IDR picture";
		case 6 -> "Supplemental enhancement information (SEI)";
		case 7 -> "Sequence parameter set";
		case 8 -> "Picture parameter set";
		case 9 -> "Access unit delimiter";
		case 10 -> "End of sequence";
		case 11 -> "End of stream";
		case 12 -> "Filler data";
		case 13 -> "Sequence parameter set extension";
		case 14 -> "Prefix NAL unit";
		case 15 -> "Subset sequence parameter set";
		case 16 -> "Depth parameter set";
		case 19 -> "Coded slice of an auxiliary coded picture without partitioning";
		case 20 -> "Coded slice extension";
		case 21 -> "Coded slice extension for a depth view component or a 3D-AVC texture view component";
		case 24 -> "Single-time aggregation packet without DON (STAP-A) RFC 6184";
		case 25 -> "Single-time aggregation packet including DON (STAP-B) RFC 6184";
		case 26 -> "Multi-time aggregation packet (MTAP16) RFC 6184";
		case 27 -> "Multi-time aggregation packet (MTAP24) RFC 6184";
		case 28 -> "Fragmentation unit (FU-A) RFC 6184";
		case 29 -> "Fragmentation unit (FU-B) RFC 6184";
		default -> "reserved";
		};
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
