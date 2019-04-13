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

public class MaximumBitrateDescriptor extends Descriptor {

	private int reserved;
	private int maximumBitrate;

	public MaximumBitrateDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		reserved = getInt(b,offset+2,2,MASK_2BITS);
		maximumBitrate = getInt(b,offset+2,3,MASK_22BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "maximumBitrate="+maximumBitrate;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("maximum_bitrate",maximumBitrate ,KVP.formatInt(maximumBitrate*50)+" bytes/second")));
		return t;
	}


	public int getMaximumBitrate() {
		return maximumBitrate;
	}

	public void setMaximumBitrate(final int maximumBitrate) {
		this.maximumBitrate = maximumBitrate;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(final int reserverd) {
		this.reserved = reserverd;
	}

}
