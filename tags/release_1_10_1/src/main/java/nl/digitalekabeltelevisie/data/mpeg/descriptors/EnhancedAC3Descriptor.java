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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.AbstractAC3SyncFrame;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class EnhancedAC3Descriptor extends Descriptor {


	private final int component_type_flag;
	private final int bsid_flag;
	private final int mainid_flag;
	private final int asvc_flag;
	private final int mixinfoexists;
	private final int substream1_flag;
	private final int substream2_flag;
	private final int substream3_flag;

	private int component_type;
	private int bsid;
	private int mainid;
	private int asvc;
	private int substream1;
	private int substream2;
	private int substream3;
	private byte[] additional_info;


	public EnhancedAC3Descriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		component_type_flag = Utils.getInt(b, offset+2, 1, 0x80)>>7;
		bsid_flag = Utils.getInt(b, offset+2, 1, 0x40)>>6;
		mainid_flag = Utils.getInt(b, offset+2, 1, 0x20)>>5;
		asvc_flag = Utils.getInt(b, offset+2, 1, 0x10)>>4;
		mixinfoexists = Utils.getInt(b, offset+2, 1, 0x08)>>3;
		substream1_flag = Utils.getInt(b, offset+2, 1, 0x04)>>2;
		substream2_flag = Utils.getInt(b, offset+2, 1, 0x02)>>1;
		substream3_flag = Utils.getInt(b, offset+2, 1, 0x01);

		int t=offset+3;
		if(component_type_flag!=0){
			component_type = Utils.getInt(b, t++, 1, Utils.MASK_8BITS);
		}
		if(bsid_flag!=0){
			bsid = Utils.getInt(b, t++, 1, Utils.MASK_8BITS);
		}
		if(mainid_flag!=0){
			mainid = Utils.getInt(b, t++, 1, Utils.MASK_8BITS);
		}
		if(asvc_flag!=0){
			asvc = Utils.getInt(b, t++, 1, Utils.MASK_8BITS);
		}
		if(substream1_flag!=0){
			substream1 = Utils.getInt(b, t++, 1, Utils.MASK_8BITS);
		}
		if(substream2_flag!=0){
			substream2 = Utils.getInt(b, t++, 1, Utils.MASK_8BITS);
		}
		if(substream3_flag!=0){
			substream3 = Utils.getInt(b, t++, 1, Utils.MASK_8BITS);
		}
		if(t<descriptorLength){
			additional_info=Utils.getBytes(b, t, descriptorLength-t);
		}
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("component_type_flag",component_type_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("bsid_flag",bsid_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("mainid_flag",mainid_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("asvc_flag",asvc_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("mixinfoexists",mixinfoexists,null)));
		t.add(new DefaultMutableTreeNode(new KVP("substream1_flag",substream1_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("substream2_flag",substream2_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("substream3_flag",substream3_flag,null)));

		if(component_type_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("component_type",component_type,AC3Descriptor.getComponentTypeString(component_type))));
		}
		if(bsid_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("bsid",bsid,AbstractAC3SyncFrame.getBsidString(bsid))));
		}
		if(mainid_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("mainid",mainid,null)));
		}
		if(asvc_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("asvc",asvc,null)));
		}
		if(substream1_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("substream1",substream1,"type of audio carried in independent substream 1 of the Enhanced AC-3 elementary stream")));
		}
		if(substream2_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("substream2",substream2,"type of audio carried in independent substream 2 of the Enhanced AC-3 elementary stream")));
		}
		if(substream3_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("substream3",substream3,"type of audio carried in independent substream 3 of the Enhanced AC-3 elementary stream")));
		}
		if(additional_info!=null){
			t.add(new DefaultMutableTreeNode(new KVP("additional_info",additional_info,null)));
		}

		return t;
	}
}
