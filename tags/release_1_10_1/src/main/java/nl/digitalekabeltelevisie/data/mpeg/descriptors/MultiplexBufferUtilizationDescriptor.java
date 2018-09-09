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

public class MultiplexBufferUtilizationDescriptor extends Descriptor {

	private final int boundValidFlag;
	private final int ltwOffsetLowerBound;
	private final int ltwOffsetUpperBound;

	public MultiplexBufferUtilizationDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		boundValidFlag = getInt(b,offset+2,1,0x80)>>7;
		ltwOffsetLowerBound = getInt(b,offset+2,2,MASK_15BITS);
		ltwOffsetUpperBound = getInt(b,offset+4,2,MASK_15BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "ltwOffsetLowerBound="+ltwOffsetLowerBound;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("bound_valid_flag",boundValidFlag ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("LTW_offset_lower_bound",ltwOffsetLowerBound ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("LTW_offset_upper_bound",ltwOffsetUpperBound ,null)));
		return t;
	}

	public int getLtwOffsetLowerBound() {
		return ltwOffsetLowerBound;
	}

	public int getBoundValidFlag() {
		return boundValidFlag;
	}

	public int getLtwOffsetUpperBound() {
		return ltwOffsetUpperBound;
	}



}
