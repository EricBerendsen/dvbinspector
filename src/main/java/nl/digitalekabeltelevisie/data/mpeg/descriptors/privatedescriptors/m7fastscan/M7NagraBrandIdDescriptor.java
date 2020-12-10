/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class M7NagraBrandIdDescriptor extends M7Descriptor {

	private final int nagra_brand_id;
	private final int ca_system_ID;
	private final int emm_stored;
	private final int reserved;
	byte[] emm_brand_ids = new byte[0];
	
	public M7NagraBrandIdDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		nagra_brand_id =getInt(b, offset+2,1,MASK_8BITS);
		ca_system_ID =getInt(b, offset+3,2,MASK_16BITS);
		emm_stored =getInt(b, offset+5,1,0x80)>>>7;
		reserved =getInt(b, offset+5,1,MASK_7BITS);
		if(emm_stored==0) {
			emm_brand_ids = getBytes(b, offset+6, descriptorLength-4);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("nagra_brand_id",nagra_brand_id ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("CA_system_ID",ca_system_ID ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("emm_stored",emm_stored ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved ,null)));
		if(emm_stored==0) {
				t.add(new DefaultMutableTreeNode(new KVP("emm_brand_ids",emm_brand_ids ,null)));
		}
		return t;
	}

}
