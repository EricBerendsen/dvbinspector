/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pid.t2mi;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

public class Payload implements TreeNode {

	byte[] data;
	String packetType;

	public Payload(byte[] data, String packetType) {
		this.data = data;
		this.packetType = packetType;
	}

	protected Payload(byte[] data) {
		this.data = data;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(new KVP("payload"));
		payloadNode.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP(packetType)));
		return payloadNode;
	}

	public int getFrameIdx() {
		return Byte.toUnsignedInt(data[6]);
	}

	public int getPlpId() {
		return Byte.toUnsignedInt(data[7]);
	}

}
