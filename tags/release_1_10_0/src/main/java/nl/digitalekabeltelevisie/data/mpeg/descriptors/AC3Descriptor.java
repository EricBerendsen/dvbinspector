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

public class AC3Descriptor extends Descriptor {


	private final int component_type_flag;
	private final int bsid_flag;
	private final int mainid_flag;
	private final int asvc_flag;
	private final int reserved_flags;

	private int component_type;
	private int bsid;
	private int mainid;
	private int asvc;
	private byte[] additional_info;


	public AC3Descriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		component_type_flag = Utils.getInt(b, offset+2, 1, 0x80)>>7;
		bsid_flag = Utils.getInt(b, offset+2, 1, 0x40)>>6;
		mainid_flag = Utils.getInt(b, offset+2, 1, 0x20)>>5;
		asvc_flag = Utils.getInt(b, offset+2, 1, 0x10)>>4;
		reserved_flags = Utils.getInt(b, offset+2, 1, 0x0F);
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
		t.add(new DefaultMutableTreeNode(new KVP("reserved_flags",reserved_flags,null)));

		if(component_type_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("component_type",component_type,getComponentTypeString(component_type))));
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
		if(additional_info!=null){
			t.add(new DefaultMutableTreeNode(new KVP("additional_info",additional_info,null)));
		}

		return t;
	}

	public static String getComponentTypeString(final int type){
		final StringBuilder s = new StringBuilder();
		if((type&0x80)==0x80){
			s.append("Enhanced AC-3, ");
		}else{
			s.append("AC-3, ");
		}
		if((type&0x40)==0x40){
			s.append("stream is a full service, ");
		}else{
			s.append("stream is intended to be combined with another audio stream, ");
		}
		if((type&0x07)==0){
			s.append("Mono, ");
		}else if((type&0x07)==0x01){
			s.append("1+1 Mode, ");
		}else if((type&0x07)==0x02){
			s.append("2 channel (stereo), ");
		}else if((type&0x07)==0x03){
			s.append("2 channel Dolby Surround encoded (stereo), ");
		}else if((type&0x07)==0x04){
			s.append("Multichannel audio (> 2 channels), ");
		}else if((type&0x87)==0x85){
			s.append("Multichannel audio (> 5.1 channels), ");
		}else if((type&0x87)==0x86){
			s.append("Elementary stream contains multiple programmes carried in independent substreams, ");
		}
		if((type&0x78)==0x40){
			s.append("Complete Main");
		}else if((type&0x78)==0x08){
			s.append("Music and Effects");
		}else if((type&0x38)==0x10){
			s.append("Visually Impaired");
		}else if((type&0x38)==0x18){
			s.append("Hearing Impaired");
		}else if((type&0x78)==0x20){
			s.append("Dialogue");
		}else if((type&0x3f)==0x28){
			s.append("Commentary");
		}else if((type&0x7f)==0x70){
			s.append("Emergency");
		}else if((type&0x7f)==0x78){
			s.append("Voiceover");
		}else if((type&0x78)==0x78){
			s.append("Karaoke");
		}
		return s.toString();
	}
}
