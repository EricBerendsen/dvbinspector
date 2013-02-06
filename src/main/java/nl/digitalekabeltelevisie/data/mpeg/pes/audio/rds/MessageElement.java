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
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class MessageElement implements TreeNode{

	/**
	 *
	 */
	protected byte[] data;
	protected int st;
	public MessageElement(final byte[] data, final int start){
		super();
		this.data =data;
		this.st = start;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode messageElement = new DefaultMutableTreeNode(new KVP("Message Element"));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Message Element Code",getMessageElementCode(),MessageElement.getMessageElementCodeString(getMessageElementCode()))));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Data Set Number",getDataSetNumber(),MessageElement.getDataSetNumberString(getDataSetNumber()))));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Program Service Number",getProgramServiceNumber(),null)));
		messageElement.add(new DefaultMutableTreeNode(new KVP("Message Element Data Length",getMessageElementDataLength(),null)));
		return messageElement;
	}

	public int getMessageElementCode() {
		return Utils.getInt(data, st, 1, Utils.MASK_8BITS);
	}

	protected int getDataSetNumber() {
		return Utils.getInt(data, st+1, 1, Utils.MASK_8BITS);
	}


	protected int getProgramServiceNumber() {
		return Utils.getInt(data, st+2, 1, Utils.MASK_8BITS);
	}

	protected int getMessageElementDataLength() {
		return Utils.getInt(data, st+3, 1, Utils.MASK_8BITS);
	}

	// default length of this element, assuming MessageElementDataLength is set.
	// if a MessageElement does not use this field this method should be overriden.
	public int getLength(){
		return getMessageElementDataLength()+4;
	}

	public static String getDataSetNumberString(final int dsn){
		if((1<=dsn)&&(dsn<=253)){
			return "Specific data set";
		}
		else if (dsn==0){
			return "Current data set";
		}
		else if (dsn==254){
			return "All data sets except the current data set";
		}
		else if (dsn==255){
			return "All data sets";
		}else{
			return "return invalid value";
		}

	}
	public String getMessageElementCodeString(){
		return getMessageElementCodeString(getMessageElementCode());
	}

	public static String getMessageElementCodeString(final int mec){

		if((0xEC<=mec)&&(mec<=0xFC)){
			return "Astra ADR 3) special commands";
		}

		switch (mec) {
		case 0x01: return "PI";
		case 0x02: return "PS";
		case 0x03: return "TA/TP";
		case 0x04: return "DI";
		case 0x05: return "MS";
		case 0x06: return "PIN";
		case 0x07: return "PTY";
		case 0x08: return "Paging call with numeric message (10 digits)";
		case 0x09: return "Real time clock correction";
		case 0x0A: return "RT";
		case 0x0B: return "PSN enable/disable";
		case 0x0C: return "Paging call without message";
		case 0x0D: return "Real time clock";
		case 0x0E: return "RDS level";
		case 0x0F: return "ARI area (BK)";
		case 0x10: return "International paging with functions message";
		case 0x11: return "International paging with numeric message (15 digits)";
		case 0x12: return "Transmitter network group designation";
		case 0x13: return "AF";
		case 0x14: return "EON - AF";
		case 0x15: return "EON - TA control";
		case 0x16: return "Group sequence";
		case 0x17: return "Request message";
		case 0x18: return "Message acknowledgment";
		case 0x19: return "CT On/Off";
		case 0x1A: return "Slow Labeling codes";
		case 0x1B: return "Paging call with alphanumeric message (80 characters)";
		case 0x1C: return "Data set select";
		case 0x1D: return "Reference input select";
		case 0x1E: return "RDS On/Off";
		case 0x1F: return "ARI level";
		case 0x20: return "Paging call with numeric message (18 digits)";
		case 0x21: return "ARI On/Off";
		case 0x22: return "RDS phase";
		case 0x23: return "Site address";
		case 0x24: return "Free-format group";
		case 0x25: return "IH";
		case 0x26: return "TDC";
		case 0x27: return "Encoder address";
		case 0x28: return "Make PSN list";
		case 0x29: return "Group variant code sequence";
		case 0x2A: return "TA control";
		case 0x2B: return "EWS";
		case 0x2C: return "Communication mode";
		case 0x2D: return "Manufacturer's specific command";
		case 0x2E: return "Linkage information";
		case 0x2F: return "PS character code table selection";
		case 0x30: return "TMC";
		case 0x31: return "EPP transmitter information";
		case 0x32: return "EPP call without additional message";
		case 0x33: return "EPP national and international call with alphanumeric message";
		case 0x34: return "EPP national and international call with variable length numeric message";
		case 0x35: return "EPP national and international call with variable length functions message";
		case 0x38: return "Extended group sequence";
		case 0x3A: return "Encoder Access Right";
		case 0x3B: return "Communications Port Configuration - Mode";
		case 0x3C: return "Communications Port Configuration - Speed";
		case 0x3D: return "Communications Port Configuration - Timeout";
		case 0x3E: return "PTYN";
		case 0x40: return "ODA configuration and short message command";
		case 0x41: return "ODA identification group usage sequence";
		case 0x42: return "ODA free-format group";
		case 0x43: return "ODA relative priority group sequence";
		case 0x44: return "ODA “Burst mode” control";
		case 0x45: return "ODA “Spinning Wheel” timing control";
		case 0x46: return "ODA Data / Radiotext Plus tags"; //http://cvs.tuxbox.org/tuxbox/apps/tuxbox/enigma/src/rds_text.cpp?rev=1.4&view=markup
		case 0x47: return "ODA Data command access right";
		case 0x48: return "DAB Dynamic Label command";
		case 0xAA: return "DAB Dynamic Label message (DL)";
		case 0xDA: return "RASS"; //http://cvs.tuxbox.org/tuxbox/apps/tuxbox/neutrino/src/driver/radiotext.cpp?rev=1.6&view=markup
		default:
			return "illegal value";
		}
	}

	public int getStart() {
		return st;
	}
}