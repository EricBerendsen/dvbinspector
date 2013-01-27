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

public class MessageElementAF extends MessageElement {

	public MessageElementAF(final byte[] data, final int start) {
		super(data, start);
	}




	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode messageElement = new DefaultMutableTreeNode(new KVP("Message Element AF"));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Message Element Code",getMessageElementCode(),MessageElement.getMessageElementCodeString(getMessageElementCode()))));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Data Set Number",getDataSetNumber(),MessageElement.getDataSetNumberString(getDataSetNumber()))));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Program Service Number",getProgramServiceNumber(),null)));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Message Element Data Length",getMessageElementDataLength(),null)));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Start Location (High)",getStartLocationHigh(),null)));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Start Location (Low)",getStartLocationLow(),null)));
		for (int i = 0; i < (getMessageElementDataLength()-2); i++) {
			final int afCode = Utils.getInt(data, st+6+i, 1, Utils.MASK_8BITS);
			messageElement.add(new DefaultMutableTreeNode(new KVP("AF Data",afCode,getAFDataString(afCode))));


		}


		return messageElement;
	}


	protected int getStartLocationHigh() {
		return Utils.getInt(data, st+4, 1, Utils.MASK_8BITS);
	}

	protected int getStartLocationLow() {
		return Utils.getInt(data, st+5, 1, Utils.MASK_8BITS);
	}


	private static String getAFDataString(final int afd){
		if((afd>=1)&&(afd<=204)){
			return (87.5+(afd/10.0))+" MHz";
		}else if((afd>=225)&&(afd<=249)){
			return (afd-224) +"AF(s) follows";
		}
		switch (afd) {
		case 0:
			return "Not to be used/filler code";
		case 205:
			return "filler code";
		case 224:
			return "No AF exists";
		case 250:
			return "An LF/MF frequency follows";


		default:
			return "Not assigned";
		}
	}

}
