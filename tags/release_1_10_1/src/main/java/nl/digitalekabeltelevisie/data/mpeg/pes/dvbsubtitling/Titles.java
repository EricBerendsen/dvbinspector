package nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class Titles implements TreeNode {
	
	// only store complete display Sets.
	// Stuffing segments are not stored.
	// a display set can span multiple PesPackets. 
	
	private List<DisplaySet> displaySets = new ArrayList<DisplaySet>();
	private DisplaySet current = null;
	private ArrayList<DisplaySet> currentEpoch = null;

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t=new DefaultMutableTreeNode(new KVP("Titles"));
		addListJTree(t,displaySets,modus,"DisplaySets"); 
		return t;
	}

	// TODO this does not correctly handle if the first PESpacket is not the start of a DisplaySegment
	
	@SuppressWarnings("unchecked")
	public void add(DVBSubtitlingPESDataField title) {
		List<Segment> segmentList = title.getSegmentList();
		if((segmentList!=null)&&(segmentList.size()>0)){
			if(segmentList.get(0).getSegmentType()==0xFF){ //stuffing, may end current set
				if(current!=null){
					if(currentEpoch!=null){
						currentEpoch.add(current);
						current.setEpoch((ArrayList<DisplaySet>) currentEpoch.clone());
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
					if(segment instanceof PageCompositionSegment){
						PageCompositionSegment pageCompositionSegment = (PageCompositionSegment)segment;
						if((pageCompositionSegment.getPageState()==0x1)
							||(pageCompositionSegment.getPageState()==0x2)){
							currentEpoch = new ArrayList<DisplaySet>();
						}
					}
					if(segment.getSegmentType()==0x80){ // end of display set segment
						if(currentEpoch!=null){
							currentEpoch.add(current);
							current.setEpoch((ArrayList<DisplaySet>) currentEpoch.clone());
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
