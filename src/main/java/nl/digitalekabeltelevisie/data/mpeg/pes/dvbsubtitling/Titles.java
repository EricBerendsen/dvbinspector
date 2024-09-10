/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class Titles implements TreeNode {

	// only store complete display Sets.
	// Stuffing segments are not stored.
	// a display set can span multiple PesPackets.

	private List<DisplaySet> displaySets = new ArrayList<>();
	private DisplaySet current = null;
	private ArrayList<DisplaySet> currentEpoch = null;

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("Titles");
		addListJTree(t,displaySets,modus,"DisplaySets");
		return t;
	}

	// TODO this does not correctly handle if the first PESpacket is not the start of a DisplaySegment

	public void add(DVBSubtitlingPESDataField title) {
		List<Segment> segmentList = title.getSegmentList();
		if((segmentList!=null)&&(!segmentList.isEmpty())){
			if(segmentList.getFirst().getSegmentType()==0xFF){ //stuffing, may end current set
				if(current!=null){
					if(currentEpoch!=null){
						currentEpoch.add(current);
						current.setEpoch(new ArrayList<DisplaySet>(currentEpoch));
					}
					displaySets.add(current);
					current = null;
				}
			}else{ // not stuffing
				if(current==null){
					current = new DisplaySet(title.getPesHandler(),title.getPesHeader().getPts());
				}
				for (Segment segment : segmentList) {
					current.add(segment);
					if(segment instanceof PageCompositionSegment pageCompositionSegment){
						if((pageCompositionSegment.getPageState()==0x1)
							||(pageCompositionSegment.getPageState()==0x2)){
							currentEpoch = new ArrayList<>();
						}
					}
					if(segment.getSegmentType()==0x80){ // end of display set segment
						if(currentEpoch!=null){
							currentEpoch.add(current);
							current.setEpoch(new ArrayList<DisplaySet>(currentEpoch));
						}
						displaySets.add(current);
						current = null;
					}
				}
			}
		}
	}

	public List<DisplaySet> getDisplaySets() {
		return displaySets;
	}

}
