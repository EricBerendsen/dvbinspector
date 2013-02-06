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

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class PDCDescriptor extends Descriptor {

	private final int day;
	private final int month;
	private final int hour;
	private final int minute;
	private final int programmeIdentificationLabel;


	public PDCDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		programmeIdentificationLabel = Utils.getInt(b, offset+2, 3,Utils.MASK_20BITS);
		day = getInt(b,offset+2,2,0x0F80)>>7;
		month = getInt(b,offset+3,1,0x78)>>3;
		hour = getInt(b,offset+3,2,0x07C0)>>6;
		minute = getInt(b,offset+4,1,Utils.MASK_6BITS);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("programme_identification_label",programmeIdentificationLabel ,"day:"+day+" month:"+month+" hour:"+hour+" minute:"+minute)));
		return t;
	}


}
