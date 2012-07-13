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
		//int [] scalingList = new int[sizeOfScalingList];
		int no_deltas=0;
		//boolean useDefaultScalingMatrixFlag = false;
		for(int j = 0; j < sizeOfScalingList; j++ ) {
			if( nextScale != 0 ) {
				int delta_scale =bitSource.se();
				no_deltas++;
				deltaScalingList[j]=delta_scale; // here remember just the raw data, not the result list
				nextScale = ( lastScale + delta_scale + 256 ) % 256;
				//useDefaultScalingMatrixFlag = ( j == 0 && nextScale == 0 );
			}
			//scalingList[ j ] = ( nextScale == 0 ) ? lastScale : nextScale;
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
				//int [] scalingList = new int[sizeOfScalingList];
				for(int j = 0; j < deltas_read; j++ ) {
					if( nextScale != 0 ) {
						//int delta_scale =bitSource.se();
						nextScale = ( lastScale + deltaScalingList[j] + 256 ) % 256;
						t.add(new DefaultMutableTreeNode(new KVP("delta_scale",deltaScalingList[j],"scaling_list["+i+"]["+j+"]="+nextScale)));
			
						//useDefaultScalingMatrixFlag = ( j == 0 && nextScale == 0 );
					}
					//scalingList[ j ] = ( nextScale == 0 ) ? lastScale : nextScale;
					lastScale = ( nextScale == 0 ) ? lastScale : nextScale; // scalingList[ j ];
				}	
			
				return t;
			}
}
