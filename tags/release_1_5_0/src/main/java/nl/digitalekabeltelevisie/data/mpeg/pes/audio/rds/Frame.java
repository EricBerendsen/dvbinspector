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

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class Frame implements TreeNode {

	private static final Logger	logger	= Logger.getLogger(Frame.class.getName());

	private byte[] data;

	public Frame(final byte[] data, final int start, final int stop) {
		super();
		this.data = data;
		this.start = start;
		this.stop = stop;
		// undo bytestuffing
		final int fdIndex = Utils.indexOf(data, new byte[]{-3}, start);
		if((fdIndex!=-1)&&(fdIndex<stop)){ // 0xFD somewhere in data between start and stop
			final byte [] tmp=new byte[(1+stop)-start]; // new version will be shorter, so this is long enough
			int target=0;
			for (int i = start; i <= stop; i++) {
				if(data[i]==-3){ // 0xFD
					tmp[target++]=(byte)(data[i]+data[i+1]); // 0xFD 00 => 0xFD
					// 0xFD 01 => 0xFE
					// 0xFD 02 => 0xFF
					i++; // read the extra byte
				}else{
					tmp[target++]=data[i];
				}
			}
			this.data=tmp;
			this.start=0;
			this.stop=target;
		}
		messageElement = MessageElementFactory.createMessageElement(this.data, this.start+5);
		if(messageElement.getLength()!=getMessageFieldLength()){
			logger.info("length of Message "+messageElement.getMessageElementCodeString() +" ("+messageElement.getLength()+", starting at "+messageElement.getStart()+") does not equal length of frame ("+getMessageFieldLength()+")");
		}
	}

	private int start; // index of the STA byte in data[]
	private int stop; // index of the STP byte in data[]

	private final MessageElement messageElement; // TODO now only one per Frame, should be more

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode frame = new DefaultMutableTreeNode(new KVP("Frame ("+MessageElement.getMessageElementCodeString(messageElement.getMessageElementCode())+")"));
		frame.add(new DefaultMutableTreeNode(new KVP("data",data,start,(stop-start)+1,null)));

		frame.add(new DefaultMutableTreeNode(new KVP("Start Code",getStart(),null)));
		frame.add(new DefaultMutableTreeNode(new KVP("Addres",getAddres(),null)));
		frame.add(new DefaultMutableTreeNode(new KVP("Sequence Counter",getSequenceCounter(),null)));
		frame.add(new DefaultMutableTreeNode(new KVP("Message field length",getMessageFieldLength(),null)));
		frame.add(new DefaultMutableTreeNode(new KVP("Cyclic Redundancy Check",getCyclicRedundancyCheck(),null)));
		frame.add(messageElement.getJTreeNode(modus));
		return frame;
	}


	private int getStart() {
		return Utils.getInt(data, start, 1, Utils.MASK_8BITS);
	}

	private int getAddres() {
		return Utils.getInt(data, start+1, 2, Utils.MASK_16BITS);
	}

	private int getSequenceCounter() {
		return Utils.getInt(data, start+3, 1, Utils.MASK_8BITS);
	}

	private int getMessageFieldLength() {
		return Utils.getInt(data, start+4, 1, Utils.MASK_8BITS);
	}

	private int getCyclicRedundancyCheck() {
		return Utils.getInt(data, start+5+getMessageFieldLength(), 2, Utils.MASK_16BITS);
	}


	public int getStop() {
		return stop;
	}


}