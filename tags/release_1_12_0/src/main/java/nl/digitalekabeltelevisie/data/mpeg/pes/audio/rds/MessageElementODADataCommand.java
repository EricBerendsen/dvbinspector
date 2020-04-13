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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.rds;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.Utils;

public class MessageElementODADataCommand extends MessageElement {

	public MessageElementODADataCommand(final byte[] data, final int start) {
		super(data, start);
	}


	@Override
	public int getLength(){
		return 2+getMessageElementDataLength();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode messageElement = new DefaultMutableTreeNode(new KVP("Message Element ODA Data Command"));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Message Element Code",getMessageElementCode(),MessageElement.getMessageElementCodeString(getMessageElementCode()))));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Message Element Data Length",getMessageElementDataLength(),null)));
		// TODO
		messageElement.add(new DefaultMutableTreeNode(new KVP("Data",data, st+2,getMessageElementDataLength(),null)));


		return messageElement;
	}

	/* (non-Javadoc)
	 * Override the version in MessageElement because this Msg has no data set number or program service number
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.audio.rds.MessageElement#getMessageElementDataLength()
	 */
	@Override
	protected int getMessageElementDataLength() {
		return Utils.getInt(data, st+1, 1, Utils.MASK_8BITS);
	}

}
