/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * @author Eric
 * 
 * does not correspond to structure in standard, but is common parent for AC4SubstreamInfoChan 
 * and AC4SubstreamInfoObj (and maybe ac4_substream_info_ajoc which is not implmented yet)
 *
 */
public abstract class AC4SubstreamInfo {
	
	public class BAudioNdot implements TreeNode{
		
		private int b_audio_ndot;
		
		public BAudioNdot(int b_audio_ndot) {
			this.b_audio_ndot = b_audio_ndot;
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("b_audio_ndot",b_audio_ndot,null));
			return t;
		}
	}

	public static LookUpList bitrate_indicator_list = new LookUpList.Builder().
				add(0, "16 kbit/s").
				add(1, "20 kbit/s").
				add(2, "24 kbit/s").
				add(3, "28 kbit/s").
				add(4, "32 kbit/s").
				add(5, "40 kbit/s").
				add(6, "48 kbit/s").
				add(7, "56 kbit/s").
				add(8, "64 kbit/s").
				add(9, "80 kbit/s").
				add(10, "96 kbit/s").
				add(11, "112 kbit/s").
				add(12,19,"Unlimited").
				build();

	protected List<BAudioNdot> b_audio_ndot_list = new ArrayList<>();

	/**
	 * @param bs
	 * @return
	 * 
	 * based on TS 103 190-1 V1.3.1 (2018-02) 4.3.3.7.5 bitrate_indicator - bit-rate indicator
	 */
	protected static int readBitrateIndiciator(BitSource bs) {
		int threeBits = bs.readBits(3);
		switch(threeBits) {
		case 0:
			return 0;
		case 2:
			return 1;
		case 4:
			return 2;
		case 6:
			return 3;
		default:	
			int fiveBits = (threeBits * 4) + bs.readBits(2);
			switch (fiveBits) {
			case 4:
				return 4;
			case 5:
				return 5;
			case 6:
				return 6;
			case 7:
				return 7;
			case 12:
				return 8;
			case 13:
				return 9;
			case 14:
				return 10;
			case 15:
				return 11;
			
	
			default:
				// TODO 0b1X1XX (8 further values) Unlimited 12…19
				return 12;
			}
		}
		
	}

	/**
	 * 
	 */
	public AC4SubstreamInfo() {
		super();
	}

}

