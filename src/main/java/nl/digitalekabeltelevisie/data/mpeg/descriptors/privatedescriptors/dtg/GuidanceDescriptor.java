/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class GuidanceDescriptor extends Descriptor  {
	
	// based on  D-Book 7 Part A v 1 March 2011 ch 8.5.3.20 Guidance Descriptor


	int reserved;
	int guidance_type;
	String iso_639_language_code;
	DVBString guidance_char;
	int guidance_mode;
	int reserved2;
	byte[] reserved_for_future_use;


	public GuidanceDescriptor( byte[] b, TableSection parent) {
		super(b, parent);
		reserved = getInt(b, 2, 1, 0xFC)>>2;
		guidance_type = getInt(b, 2, 1, MASK_2BITS);
		if(guidance_type==0){
			iso_639_language_code=getISO8859_1String(b, 3, 3);
			guidance_char = new DVBString(b, 6, descriptorLength-4);
		}else if(guidance_type==1){
			reserved2 = getInt(b, 3, 1, 0xFE)>>1;
			guidance_mode = getInt(b, 3, 1, MASK_1BIT);
			iso_639_language_code=getISO8859_1String(b, 4, 3);
			guidance_char = new DVBString(b, 7, descriptorLength-5);
		}else{
			reserved_for_future_use = getBytes(b, 3, descriptorLength-3);
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {

		DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new KVP("reserved", reserved));
		t.add(new KVP("guidance_type", guidance_type));
		if (guidance_type == 0) {
			t.add(new KVP("ISO_639_language_code", iso_639_language_code));
			t.add(new KVP("guidance_char", guidance_char));
		} else if (guidance_type == 1) {
			t.add(new KVP("reserved", reserved2));
			t.add(new KVP("guidance_mode", guidance_mode)
					.setDescription((guidance_mode == 1) ? "guidance for content unsuitable for broadcast until after the watershed is appropriate." : null));
			t.add(new KVP("ISO_639_language_code", iso_639_language_code));
			t.add(new KVP("guidance_char", guidance_char));
		} else {
			t.add(new KVP("reserved_for_future_use", reserved_for_future_use));
		}

		return t;
	}

	@Override
	public String getDescriptorname(){
		return "DTG guidance_descriptor";
	}

}
