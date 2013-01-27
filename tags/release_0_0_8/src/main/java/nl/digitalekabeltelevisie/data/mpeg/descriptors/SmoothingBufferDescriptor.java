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

public class SmoothingBufferDescriptor extends Descriptor {

	private int reserved;
	private int sbLeakRate;
	private int reserved2;
	private int sbSize;

	public SmoothingBufferDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		reserved = getInt(b,offset+2,2,MASK_2BITS);
		sbLeakRate = getInt(b,offset+2,3,MASK_22BITS);
		reserved2 = getInt(b,offset+5,2,MASK_2BITS);
		sbSize = getInt(b,offset+5,3,MASK_22BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "sbLeakRate="+sbLeakRate;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sb_leak_rate",sbLeakRate ,KVP.formatInt(sbLeakRate*400)+" bits/second")));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2 ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sb_size",sbSize ," byte")));
		return t;
	}


	public int getSbLeakRate() {
		return sbLeakRate;
	}

	public void setSbLeakRate(final int maximumBitrate) {
		this.sbLeakRate = maximumBitrate;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(final int reserverd) {
		this.reserved = reserverd;
	}

	public int getReserved2() {
		return reserved2;
	}

	public void setReserved2(final int reserved2) {
		this.reserved2 = reserved2;
	}

	public int getSbSize() {
		return sbSize;
	}

	public void setSbSize(final int sbSize) {
		this.sbSize = sbSize;
	}

}
