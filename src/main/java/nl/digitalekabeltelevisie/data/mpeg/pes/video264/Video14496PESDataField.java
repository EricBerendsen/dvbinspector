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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;

/**
 * @author Eric Berendsen
 *
 */

public class Video14496PESDataField extends PesPacketData implements TreeNode {



	private List<NALUnit> nalUnits = new ArrayList<NALUnit>();


	//private final List<VideoMPEG2Section> sections= new ArrayList<VideoMPEG2Section>();

	public Video14496PESDataField(final PesPacketData pesPacket) {
		//public Video14496PESDataField(byte[] data, int offset, int len, long pts) {
		super(pesPacket);

		int i = pesDataStart;
		while((i<(data.length))&&(i>=0)){
			i = indexOf(data, new byte[]{0,0,1},i);
			if(i>=0){ // found start_code_prefix_one_3bytes

				i+=3; // start of NAL unit
				// now look for end, either byte[]{0,0,1} or byte[]{0,0,0} or  TODO end of PES data
				int zeroEnd = indexOf(data, new byte[]{0,0,0},i);
				int oneEnd = indexOf(data, new byte[]{0,0,1},i);
				final int end;
				if(zeroEnd>=0){
					if(oneEnd>=0){
						end = Math.min(zeroEnd, oneEnd);
					}else{  // oneEnd not found, use zero Ende
						end = zeroEnd;
					}
				}else if(oneEnd>=0){
					end = oneEnd;
				}else{ // both not found, use pesLen
					end = pesDataLen;
				}
				NALUnit nalUnit = new NALUnit(data,i, end-i);
				i=end;
				nalUnits.add(nalUnit);

			}
		}
	}



	public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode s = super.getJTreeNode(modus,"Video 14496 PES Packet");
			addListJTree(s,nalUnits,modus,"NAL Units");
			return s;

	}



	public List<NALUnit> getNalUnits() {
		return nalUnits;
	}





}
