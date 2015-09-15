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

public class MessageElementPTY extends MessageElement {

	public MessageElementPTY(final byte[] data, final int start) {
		super(data, start);
	}


	@Override
	public int getLength(){
		return 4;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode messageElement = new DefaultMutableTreeNode(new KVP("Message Element PTY"));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Message Element Code",getMessageElementCode(),MessageElement.getMessageElementCodeString(getMessageElementCode()))));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Data Set Number",getDataSetNumber(),MessageElement.getDataSetNumberString(getDataSetNumber()))));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Program Service Number",getProgramServiceNumber(),null)));
		messageElement.add(new DefaultMutableTreeNode(new KVP("PTY",getPTY(),getPTY(getPTY()))));

		return messageElement;
	}

	protected int getPTY() {
		return Utils.getInt(data, st+3, 1, Utils.MASK_8BITS);
	}



	private static String getPTY(final int tatp){
		switch (tatp) {
		case 0:
			return "No programma type or undefined";
		case 1:
			return "News";
		case 2:
			return "Current Affairs";
		case 3:
			return "Information";
		case 4:
			return "Sport";
		case 5:
			return "Education";
		case 6:
			return "Drama";
		case 7:
			return "Culture";
		case 8:
			return "Science";
		case 9:
			return "Varied";
		case 10:
			return "Pop Music";
		case 11:
			return "Rock Music";
		case 12:
			return "Easy Listening Music";
		case 13:
			return "Light Classical";
		case 14:
			return "Serious Classical";
		case 15:
			return "Other Music";
		case 16:
			return "Weather";
		case 17:
			return "Finance";
		case 18:
			return "Children's programmes";
		case 19:
			return "Social Affairs";
		case 20:
			return "Religion";
		case 21:
			return "Phone In";
		case 22:
			return "Travel";
		case 23:
			return "Leisure";
		case 24:
			return "Jazz Music";
		case 25:
			return "Country Music";
		case 26:
			return "National Music";
		case 27:
			return "Oldies Music";
		case 28:
			return "Folk Music";
		case 29:
			return "Documentary";
		case 30:
			return "Alarm Test";
		case 31:
			return "Alarm";

		default:
			return "Illegal Value";
		}
	}
}
