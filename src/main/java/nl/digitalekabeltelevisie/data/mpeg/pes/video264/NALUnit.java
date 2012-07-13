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

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

public class NALUnit implements TreeNode {

	private static final Logger	logger	= Logger.getLogger(NALUnit.class.getName());

	/**
	 * @param bytes
	 * @param offset
	 * @param len
	 */
	public NALUnit(byte[] bytes, int offset, int len) {
		super();
		this.bytes = bytes;
		this.offset = offset;
		this.numBytesInNALunit = len;
		rbsp_byte = new byte[len]; // max len, maybe a bit shorter
		
		
		bs = new BitSource(bytes, offset);
		this.forbidden_zero_bit = bs.readBits(1);
		
		this.nal_ref_idc = bs.readBits(2);
		this.nal_unit_type = bs.readBits(5);
		
		
		numBytesInRBSP = 0;
		for(int i = 1; i < numBytesInNALunit; i++ ) {
			if( i + 2 < numBytesInNALunit && bs.nextBits(24) == 0x000003 ) {
				rbsp_byte[ numBytesInRBSP++ ]=bs.readSignedByte(8); // All b(8)
				rbsp_byte[ numBytesInRBSP++ ]=bs.readSignedByte(8);// All b(8)
				i += 2;
				byte  emulation_prevention_three_byte =bs.readSignedByte(8); // equal to 0x03 */ All f(8) ignore result
			} else{
				rbsp_byte[ numBytesInRBSP++ ]= bs.readSignedByte(8);
			}
		}
		
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
		}else{
			logger.warning("not implemented nal_unit_type: "+nal_unit_type);
		}
		
		
	}


	private final byte[] bytes;
	private final int offset;
	private final int numBytesInNALunit;
	
	private final int forbidden_zero_bit;
	private final int nal_ref_idc;
	private final int nal_unit_type;
	protected BitSource bs;
	
	private final byte[]rbsp_byte;
	private int numBytesInRBSP=0;
	
	private RBSP rbsp = null;

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("NALUnit ("+getNALUnitTypeString(nal_unit_type)+")"));
		t.add(new DefaultMutableTreeNode(new KVP("bytes",bytes,offset,numBytesInNALunit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("numBytesInNALunit",numBytesInNALunit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("forbidden_zero_bit",forbidden_zero_bit,null)));
		t.add(new DefaultMutableTreeNode(new KVP("nal_ref_idc",nal_ref_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("nal_unit_type",nal_unit_type,getNALUnitTypeString(nal_unit_type))));
		t.add(new DefaultMutableTreeNode(new KVP("rbsp_byte",rbsp_byte,0,numBytesInRBSP,null)));
		t.add(new DefaultMutableTreeNode(new KVP("NumBytesInRBSP",numBytesInRBSP,null)));
		if(rbsp!=null){
			t.add(rbsp.getJTreeNode(modus));
		}
		return t;
	}

	
	public static String getNALUnitTypeString(final int nal_unit_type) {

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
		default:
			return "reserved";
		}
	}

}
