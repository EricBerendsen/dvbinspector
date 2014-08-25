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

public class AdaptationFieldDataDescriptor extends Descriptor {




	private final int adaptationFieldDataIdentifier;
	public AdaptationFieldDataDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		adaptationFieldDataIdentifier = getInt(b,offset+2,1,MASK_8BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "adaptationFieldDataIdentifier="+adaptationFieldDataIdentifier;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_data_identifier",adaptationFieldDataIdentifier ,getAdaptationFieldDataIdentifierString(adaptationFieldDataIdentifier))));
		return t;
	}

	/**
	 * @param adaptationFieldDataIdentifier2
	 * @return
	 */
	private String getAdaptationFieldDataIdentifierString(int adaptationFieldDataIdentifier) {
		StringBuilder sb = new StringBuilder();
		if((adaptationFieldDataIdentifier & 0x01)!=0){
			sb.append("announcement switching data field");
		}
		if((adaptationFieldDataIdentifier & 0x02)!=0){
			sb.append("AU_information data field");
		}
		if((adaptationFieldDataIdentifier & 0x04)!=0){
			sb.append("PVR_assist_information_data_field");
		}

		return sb.toString();
	}



}
