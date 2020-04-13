/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

/**
 *	representation of the value encoded Hamming 24/18
 * @see § 8.3 ETSI EN 300 706
 * 
 * transmission Order
 * Byte N Byte N + 1 Byte N + 2
 * 	P1 P2 D1 P3 D2 D3 D4 P4 D5 D6 D7 D8 D9 D10 D11 P5 D12 D13 D14 D15 D16 D17 D18 P6
 * 
 * value as returned D18 D17 D16 ... D2 D1
 */
public class Triplet implements TreeNode{

	protected int val;

	/**
	 * 
	 */
	public Triplet(final byte[] data, final int offset) {
		val = getHamming24_8Byte(data,offset);
	}


	/**
	 * @return the val, , in reverse order as transmitted. so
	 */
	public int getVal() {
		return val;
	}


	/**
	 * @param val the val to set
	 */
	public void setVal(final int val) {
		this.val = val;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		return new DefaultMutableTreeNode(new KVP("Triplet",val,Utils.toBinaryString(val, 18)));

	}
	/**
	 * @param tr1
	 * @return
	 */
	public int getPageFunction() {

		return val & MASK_4BITS;
	}


}