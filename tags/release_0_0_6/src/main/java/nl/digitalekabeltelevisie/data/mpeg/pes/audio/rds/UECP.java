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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

/**
 * @author Eric
 *
 */
public class UECP implements TreeNode {

	/**
	 * @param data
	 */
	public UECP(final byte[] data) {
		super();
		// find complete frames
		int s = 0;
		while(s<data.length){
			while((s<data.length)&&(data[s]!=-2)){ // 0xFE==-2
				s++;
			}
			// now s==data.length OR new start found
			if(s<data.length){
				int end=s+1; // frame should be at least 8 bytes, check later
				while((end<data.length)&&(data[end]!=-1)){ // 0xFF ==-1
					end++;
				}
				if(end<data.length){ // end found
					if((end-s)>8){ //complete frame is at least 8 byte, STA 1, ADD 2, SQC 1, MFL 1, CRC 2, STP 1
						final Frame f = new Frame(data, s, end);
						frames.add(f);
					}
					s=end+1; //advance to possible start of next frame
				}else{ // no end, no complete frame found, set s to end to exit loop
					s=end;
				}
			}
		}
	}

	private final List<Frame> frames = new ArrayList<Frame>();

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode uecp = new DefaultMutableTreeNode(new KVP("UECP"));
		addListJTree(uecp,frames,modus,"Frames");
		return uecp;
	}

}
