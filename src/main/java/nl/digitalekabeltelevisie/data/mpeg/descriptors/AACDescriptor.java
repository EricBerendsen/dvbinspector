/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getBytes;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AACDescriptor extends Descriptor {


	private final int profile_and_level;
	private int aac_type_flag = 0;
	private int saoc_de_flag = 0;
	private int aac_type;
	
	private byte[] additional_info;

	public AACDescriptor(final byte[] b, final TableSection parent) {
		super(b ,parent);
		profile_and_level = getInt(b, 2, 1, MASK_8BITS);
		if(descriptorLength > 1){
			aac_type_flag = getInt(b, 3, 1, 0b1000_0000)>>7;
			saoc_de_flag = getInt(b, 3, 1, 0b0100_0000)>>6;
		
			int t = 4;
			if(aac_type_flag==1){
				aac_type = getInt(b, t++, 1, MASK_8BITS);
			}
			if(t<descriptorLength){
				additional_info=getBytes(b, t, 2 + descriptorLength - t);
			}
		}
	}

	@Override
	public KVP getJTreeNode(final int modus){

		final KVP t = super.getJTreeNode(modus);
		t.add(new KVP("profile_and_level",profile_and_level,getProfileLevelString(profile_and_level)));
		if(descriptorLength > 1){
			t.add(new KVP("AAC_type_flag",aac_type_flag));
			t.add(new KVP("SAOC_DE_flag",saoc_de_flag,saoc_de_flag == 1?"SAOC-DE parametric data shall be present":"SAOC-DE parametric data shall not be present"));
			if(aac_type_flag==1){
				t.add(new KVP("aac_type",aac_type,getComponentType0x06String(aac_type)));
			}
			if(additional_info!=null){
				t.add(new KVP("additional_info",additional_info));
			}
		}

		return t;
	}

}
