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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

public abstract class RBSP implements TreeNode {

	protected BitSource bitSource;

	protected RBSP(byte[] rbsp_bytes, int numBytesInRBSP){
		bitSource = new BitSource(rbsp_bytes, 0,numBytesInRBSP);
	}

	// used in Pic_parameter_set_rbsp and Seq_parameter_set_rbsp

	/**
	 * @param deltaScalingList on return has the deltas read
	 * @param sizeOfScalingList 16 or 64
	 * @param bitSource
	 * @return the number of delta's read, and present in deltaScalingList
	 */
	protected int scaling_list(int[] deltaScalingList, int sizeOfScalingList, BitSource bitSource) {
		int lastScale = 8;
		int nextScale = 8;
		int no_deltas=0;
		for(int j = 0; j < sizeOfScalingList; j++ ) {
			if( nextScale != 0 ) {
				int delta_scale =bitSource.se();
				no_deltas++;
				deltaScalingList[j]=delta_scale; // here remember just the raw data, not the result list
				nextScale = ( lastScale + delta_scale + 256 ) % 256;
			}
			lastScale = ( nextScale == 0 ) ? lastScale : nextScale; // scalingList[ j ];
		}
		return no_deltas;
	}

	// used in Pic_parameter_set_rbsp and Seq_parameter_set_rbsp

	protected DefaultMutableTreeNode getScalingListJTree(int[] deltaScalingList, int i,
			int sizeOfScalingList, int deltas_read) {

				final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("scaling_list["+i+"]"));

				int lastScale = 8;
				int nextScale = 8;
				for(int j = 0; j < deltas_read; j++ ) {
					if( nextScale != 0 ) {
						nextScale = ( lastScale + deltaScalingList[j] + 256 ) % 256;
						t.add(new DefaultMutableTreeNode(new KVP("delta_scale",deltaScalingList[j],"scaling_list["+i+"]["+j+"]="+nextScale)));

					}
					lastScale = ( nextScale == 0 ) ? lastScale : nextScale; // scalingList[ j ];
				}

				return t;
			}
}
