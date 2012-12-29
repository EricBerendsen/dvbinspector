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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AACDescriptor extends Descriptor {


	private final int profile_and_level;
	private final int aac_type_flag;
	private int aac_type;
	private byte[] additional_info;


	public AACDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		profile_and_level = getInt(b, offset+2, 1, MASK_8BITS);
		aac_type_flag = getInt(b, offset+3, 1, 0x80)>>7;
		int t=offset+4;
		if(aac_type_flag==1){
			aac_type = getInt(b, t++, 1, MASK_8BITS);
		}
		if(t<descriptorLength){
			additional_info=getBytes(b, t, descriptorLength-t);
		}
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("profile_and_level",profile_and_level,getProfileLevelString(profile_and_level))));
		t.add(new DefaultMutableTreeNode(new KVP("aac_type_flag",aac_type_flag,null)));
		if(aac_type_flag==1){
			t.add(new DefaultMutableTreeNode(new KVP("aac_type",aac_type,getComponentType0x06String(aac_type))));
		}
		if(additional_info!=null){
			t.add(new DefaultMutableTreeNode(new KVP("additional_info",additional_info,null)));
		}

		return t;
	}

	public static String getProfileLevelString(final int profile_and_level) {
		switch (profile_and_level) {

		case 0x10:
			return "Main profile, level 1";
		case 0x11:
			return "Main profile, level 2";
		case 0x12:
			return "Main profile, level 3";
		case 0x13:
			return "Main profile, level 4";
		case 0x18:
			return "Scalable Profile, level 1";
		case 0x19:
			return "Scalable Profile, level 2";
		case 0x1A:
			return "Scalable Profile, level 3";
		case 0x1B:
			return "Scalable Profile, level 4";
		case 0x20:
			return "Speech profile, level 1";
		case 0x21:
			return "Speech profile, level 2";
		case 0x28:
			return "Synthesis profile, level 1";
		case 0x29:
			return "Synthesis profile, level 2";
		case 0x2A:
			return "Synthesis profile, level 3";
		case 0x30:
			return "High quality audio profile, level 1";
		case 0x31:
			return "High quality audio profile, level 2";
		case 0x32:
			return "High quality audio profile, level 3";
		case 0x33:
			return "High quality audio profile, level 4";
		case 0x34:
			return "High quality audio profile, level 5";
		case 0x35:
			return "High quality audio profile, level 6";
		case 0x36:
			return "High quality audio profile, level 7";
		case 0x37:
			return "High quality audio profile, level 8";
		case 0x38:
			return "Low delay audio profile, level 1";
		case 0x39:
			return "Low delay audio profile, level 2";
		case 0x3A:
			return "Low delay audio profile, level 3";
		case 0x3B:
			return "Low delay audio profile, level 4";
		case 0x3C:
			return "Low delay audio profile, level 5";
		case 0x3D:
			return "Low delay audio profile, level 6";
		case 0x3E:
			return "Low delay audio profile, level 7";
		case 0x3F:
			return "Low delay audio profile, level 8";
		case 0x40:
			return "Natural audio profile, level 1";
		case 0x41:
			return "Natural audio profile, level 2";
		case 0x42:
			return "Natural audio profile, level 3";
		case 0x43:
			return "Natural audio profile, level 4";
		case 0x48:
			return "Mobile audio internetworking profile, level 1";
		case 0x49:
			return "Mobile audio internetworking profile, level 2";
		case 0x4A:
			return "Mobile audio internetworking profile, level 3";
		case 0x4B:
			return "Mobile audio internetworking profile, level 4";
		case 0x4C:
			return "Mobile audio internetworking profile, level 5";
		case 0x4D:
			return "Mobile audio internetworking profile, level 6";
		case 0x50:
			return "AAC profile, level 1";
		case 0x51:
			return "AAC profile, level 2";
		case 0x52:
			return "AAC profile, level 4";
		case 0x53:
			return "AAC profile, level 5";
		case 0x58:
			return "High efficiency AAC profile, level 2";
		case 0x59:
			return "High efficiency AAC profile, level 3";
		case 0x5A:
			return "High efficiency AAC profile, level 4";
		case 0x5B:
			return "High efficiency AAC profile, level 5";
		case 0x60:
			return "High efficiency AAC v2 profile, level 2";
		case 0x61:
			return "High efficiency AAC v2 profile, level 3";
		case 0x62:
			return "High efficiency AAC v2 profile, level 4";
		case 0x63:
			return "High efficiency AAC v2 profile, level 5";

		case 0xFF:
			return "Audio profile and level not specified by the MPEG-4_audio_profile_and_level " +
					"field in this descriptor";
		default:
			return "Reserved";

		}
	}

}
