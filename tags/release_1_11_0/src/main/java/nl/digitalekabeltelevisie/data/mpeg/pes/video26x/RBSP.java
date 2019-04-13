/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

public abstract class RBSP implements TreeNode {

	public static String getChroma_format_idcString(int chroma_format_idc) {
		switch (chroma_format_idc) {
		case 0: return "monochrome";
		case 1: return "4:2:0";
		case 2: return "4:2:2";
		case 3: return "4:4:4";
	
	
		default:
			return "error";
		}
	
	}

	protected BitSource bitSource;
	protected byte[] bytes;
	protected int numBytesInRBSP;

	protected RBSP(byte[] rbsp_bytes, int numBytesInRBSP){
		bitSource = new BitSource(rbsp_bytes, 0,numBytesInRBSP);
		bytes = rbsp_bytes;
		this.numBytesInRBSP = numBytesInRBSP;
	}

	// used in Pic_parameter_set_rbsp and Seq_parameter_set_rbsp

	/**
	 * @param deltaScalingList on return has the deltas read
	 * @param sizeOfScalingList 16 or 64
	 * @param bitSource
	 * @return the number of delta's read, and present in deltaScalingList
	 */
	public static int scaling_list(int[] deltaScalingList, int sizeOfScalingList, BitSource bitSource) {
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

	public static DefaultMutableTreeNode getScalingListJTree(int[] deltaScalingList, int i,
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

	public static String getProfileIdcString(final int profile_idc) {
	
		switch (profile_idc) {
		case 66: return "Baseline profile";
		case 77: return "Main profile";
		case 88: return "Extended profile";
		case 100: return "High profile";
		case 110: return "High 10 profile";
		case 122: return "High 4:2:2 profile";
		case 44: return "CAVLC 4:4:4 Intra profile";
		case 144: return "High 4:4:4 Predictive profile";
	
		// these are used in Rec. ITU-T H.264 (03/2010) – Prepublished version, but not defined in Annex A
		// definitions from https://ffmpeg.org/pipermail/ffmpeg-cvslog/2014-March/074446.html
		case 83: return "Scalable Constrained High profile (SVC)";
		case 86: return "Scalable High Intra profile (SVC)";
		case 118: return "Stereo High profile (MVC)";
		case 128: return "Multiview High profile (MVC)";
		case 138: return "Multiview Depth High profile (MVCD)";
		case 244: return "High444 Predictive profile";

		// Rec. ITU-T H.264 (02/2014) J.10.1.1 Enhanced Multiview Depth High profile
		case 139: return "Enhanced Multiview Depth High profile (MVCD)";
		default:
			return "unknown";
		}
	}

	public BitSource getBitSource() {
		return bitSource;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getNumBytesInRBSP() {
		return numBytesInRBSP;
	}
}
