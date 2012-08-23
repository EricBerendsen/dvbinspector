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

public class SystemClockDescriptor extends Descriptor {

	private int externalClockReferenceIndicator;
	private int reserved;
	private int clockAccuracyInteger;
	private int clockAccuracyExponent;
	private int reserved2;

	public SystemClockDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		externalClockReferenceIndicator = getInt(b,offset+2,1,0x80)>>7;
		reserved = getInt(b,offset+2,1,0x40)>>6;
		clockAccuracyInteger = getInt(b,offset+2,1,MASK_6BITS);
		clockAccuracyExponent = getInt(b,offset+3,1,0xE0)>>5;
		reserved2 = getInt(b,offset+3,1,MASK_5BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "externalClockReferenceIndicator="+externalClockReferenceIndicator;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("external_clock_reference_indicator",externalClockReferenceIndicator ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("clock_accuracy_integer",clockAccuracyInteger ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("clock_accuracy_exponent",clockAccuracyExponent ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2 ,null)));
		return t;
	}

	public int getClockAccuracyExponent() {
		return clockAccuracyExponent;
	}

	public void setClockAccuracyExponent(final int clockAccuracyExponent) {
		this.clockAccuracyExponent = clockAccuracyExponent;
	}

	public int getClockAccuracyInteger() {
		return clockAccuracyInteger;
	}

	public void setClockAccuracyInteger(final int clockAccuracyInteger) {
		this.clockAccuracyInteger = clockAccuracyInteger;
	}

	public int getExternalClockReferenceIndicator() {
		return externalClockReferenceIndicator;
	}

	public void setExternalClockReferenceIndicator(
			final int externalClockReferenceIndicator) {
		this.externalClockReferenceIndicator = externalClockReferenceIndicator;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(final int reserved) {
		this.reserved = reserved;
	}

	public int getReserved2() {
		return reserved2;
	}

	public void setReserved2(final int reserved2) {
		this.reserved2 = reserved2;
	}




}
