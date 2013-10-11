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

public class CADescriptor extends Descriptor {

	private int caSystemID;
	private int caPID;
	private byte[] privateDataByte;

	public CADescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		caSystemID = getInt(b,offset+2,2,0xFFFF);
		caPID = getInt(b,offset+4,2,0x1FFF);
		privateDataByte = copyOfRange(b, offset+6, offset+descriptorLength+2);
	}

	@Override
	public String toString() {
		return super.toString() + "caSystemID="+caSystemID;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("CA_system_ID",caSystemID ,getCASystemIDString(caSystemID))));
		t.add(new DefaultMutableTreeNode(new KVP("CA_PID",caPID ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		return t;
	}

	public int getCaPID() {
		return caPID;
	}



	public int getCaSystemID() {
		return caSystemID;
	}

	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}

}
