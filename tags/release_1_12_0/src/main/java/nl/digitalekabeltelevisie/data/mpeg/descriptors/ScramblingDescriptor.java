/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import nl.digitalekabeltelevisie.util.*;

public class ScramblingDescriptor extends Descriptor {
	
	static LookUpList scramblingModeList = new LookUpList.Builder().
			add(0x00,"Reserved for future use").
			add(0x01,"DVB-CSA1 (default mode)").
			add(0x02,"DVB-CSA2").
			add(0x03,"DVB-CSA3").
			add(0x04,0x0F,"Reserved for future use").
			add(0x10,"DVB-CISSA version 1").
			add(0x11,0x1F,"Reserved for future DVB-CISSA versions").
			add(0x20,0x6F,"Reserved for future use").
			add(0x70,0x7F,"ATIS defined (see annex J of ATIS-0800006)").
			add(0x80,0xFE,"User defined").
			add(0xFF,"Reserved for future use").
			build();
	
	private int scrambling_mode;

	public ScramblingDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		scrambling_mode = getInt(b,offset+2,1,Utils.MASK_8BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "scrambling_mode="+scrambling_mode;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("scrambling_mode",scrambling_mode ,scramblingModeList.get(scrambling_mode,"Illegal value"))));
		return t;
	}



}
