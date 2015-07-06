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

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video264.*;

/**
 * @author Eric
 *
 */
public class Sei_rbsp extends RBSP {

	private List<Sei_message> sei_messages= new ArrayList<Sei_message>();

	public Sei_rbsp(final byte[] rbsp_bytes, final int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);


		sei_messages = Sei_messageFactory.buildSei_messageList(bitSource);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("sei_rbsp"));
		addListJTree(s,sei_messages,modus,"sei_messages");
		return s;
	}

	public List<Sei_message> getSei_messages() {
		return sei_messages;
	}

}
