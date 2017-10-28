/**
 * 
 * http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 * This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * 
 * This file is part of DVB Inspector.
 * 
 * DVB Inspector is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DVB Inspector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DVB Inspector. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * The author requests that he be notified of any application, applet, or other binary that makes use of this code, but
 * that's more out of curiosity than anything and is not required.
 * 
 */

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.video264.*;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public abstract class AbstractNALUnit  implements TreeNode{

	protected static final Logger logger = Logger.getLogger(NALUnit.class.getName());

	public abstract DefaultMutableTreeNode getJTreeNode(int modus);
	public abstract String getNALUnitTypeString(final int nal_unit_type);


	protected abstract void createRBSP();

	protected final byte[] bytes;
	protected final int offset;
	protected final int numBytesInNALunit;
	protected BitSource bs;
	protected final byte[] rbsp_byte;
	protected int numBytesInRBSP = 0;
	protected RBSP rbsp = null;

	/**
	 * 
	 */
	public AbstractNALUnit(final byte[] bytes, final int offset, final int len) {
		super();
		this.bytes = bytes;
		this.offset = offset;
		this.numBytesInNALunit = len;
		rbsp_byte = new byte[len]; // max len, maybe a bit shorter


		bs = new BitSource(bytes, offset);

	}
	/**
	 * 
	 */
	protected void readRBSPBytes() {
		numBytesInRBSP = 0;
		for(int i = 1; i < numBytesInNALunit; i++ ) {
			if( ((i + 2) < numBytesInNALunit) && (bs.nextBits(24) == 0x000003) ) {
				rbsp_byte[ numBytesInRBSP++ ]=bs.readSignedByte(8); // All b(8)
				rbsp_byte[ numBytesInRBSP++ ]=bs.readSignedByte(8);// All b(8)
				i += 2;
				/* emulation_prevention_three_byte =*/ bs.readSignedByte(8); // equal to 0x03 */ All f(8) ignore result
			} else{
				rbsp_byte[ numBytesInRBSP++ ]= bs.readSignedByte(8);
			}
		}
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getNumBytesInNALunit() {
		return numBytesInNALunit;
	}


	public byte[] getRbsp_byte() {
		return rbsp_byte;
	}

	public int getNumBytesInRBSP() {
		return numBytesInRBSP;
	}

	public RBSP getRbsp() {
		return rbsp;
	}

}