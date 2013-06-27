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

package nl.digitalekabeltelevisie.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

/**
 * @author Eric
 *
 */
public class JTreeLazyList{


	private int stepSize = 100;
	private TransportStream transportStream = null;
	private int modus;
	private int noPackets;
	private MutableTreeNode mutableTreeNode = null;


	public class RangeNode implements MutableTreeNode {

		/**
		 * @param level
		 * @param start
		 * @param end
		 */
		private RangeNode(int level, int start, int end) {
			super();
			this.level = level;
			this.start = start;
			this.end = end;
		}

		private RangeNode(int level, int start, int end,String label) {
			super();
			this.level = level;
			this.start = start;
			this.end = end;
			this.label = label;
		}

		int level;
		int start;
		int end;
		private MutableTreeNode[] children=null;
		private String label="";



		public String toString(){
			StringBuilder b = new StringBuilder();
			b.append(label).append('[').append(Integer.toString(start)).append("..").append(Integer.toString(end)).append(']');
			return b.toString();
		}

		private RangeNode getChild(int level, int currentStart, int index){
			int start = currentStart+(ipower(stepSize,level)*index);
			int end = (currentStart+(ipower(stepSize,level)*(index+1)))-1;
			end = Math.min(noPackets-1, end);
			return new RangeNode(level-1,start,end);
		}

		public javax.swing.tree.TreeNode getChildAt(int childIndex) {
			if(level>0){
				RangeNode t = null;
				if(children!=null){
					t = (RangeNode)children[childIndex];
					if(t==null){
						t=getChild(level, start, childIndex);
						children[childIndex]=t;
					}
				}else{
					children = new MutableTreeNode[stepSize];
					t=getChild(level, start, childIndex);
					children[childIndex]=t;
				}
				return t;
			}else{ //lowest level, now return leaf
				MutableTreeNode t = null;
				if(children!=null){
					t = children[childIndex];
					if(t==null){
						t=transportStream.getTSPacket(childIndex+start).getJTreeNode(modus);
						children[childIndex]=t;
					}
				}else{
					children = new MutableTreeNode[stepSize];
					t=transportStream.getTSPacket(childIndex+start).getJTreeNode(modus);
					children[childIndex]=t;
				}
				return t;
			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#getChildCount()
		 */
		@Override
		public int getChildCount() {
			if((start+ipower(stepSize, level+1))<=noPackets ){
				return stepSize;
			}else{
				if(level==0){
					return (end-start)+1;
				}else{
					return divideRoundUp((end-start),ipower(stepSize,level));
				}

			}
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#getParent()
		 */
		@Override
		public javax.swing.tree.TreeNode getParent() {
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
		 */
		@Override
		public int getIndex(javax.swing.tree.TreeNode node) {
			return 0;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#getAllowsChildren()
		 */
		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#isLeaf()
		 */
		@Override
		public boolean isLeaf() {
			return false;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#children()
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Enumeration children() {
			// first preload all children
			int childCount = getChildCount();
			for(int t=0;t<childCount;t++){
				getChildAt(t);
			}
			return new Vector(Arrays.asList(children)).elements();
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#insert(javax.swing.tree.MutableTreeNode, int)
		 */
		@Override
		public void insert(MutableTreeNode child, int index) {
			throw new IllegalStateException("node does not allow children");

		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#remove(int)
		 */
		@Override
		public void remove(int index) {
			throw new IllegalStateException("node does not allow remove");
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#remove(javax.swing.tree.MutableTreeNode)
		 */
		@Override
		public void remove(MutableTreeNode node) {
			System.out.println("remove");
			throw new IllegalStateException("node does not allow remove");

		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#setUserObject(java.lang.Object)
		 */
		@Override
		public void setUserObject(Object object) {
			System.out.println("setUserObject");
			// null

		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#removeFromParent()
		 */
		@Override
		public void removeFromParent() {
			System.out.println("removeFromParent");

		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#setParent(javax.swing.tree.MutableTreeNode)
		 */
		@Override
		public void setParent(MutableTreeNode newParent) {

		}

	}

	/**
	 *
	 */
	public JTreeLazyList(TransportStream ts,  int step, int modus, int noPackets) {
		transportStream = ts;
		this.stepSize = step;
		this.modus = modus;
		this.noPackets = noPackets;
	}


	public MutableTreeNode getJTreeNode(int modus, String label) {
		if(mutableTreeNode==null){
			int level = determineLevel(noPackets);
			mutableTreeNode = new RangeNode(level, 0, noPackets-1,label);
		}

		return mutableTreeNode;
	}


	/**
	 * @param noPackets2
	 * @return
	 */
	private int determineLevel(int noPackets2) {
		int l=0;
		while(ipower(stepSize,l+1)<noPackets2){
			l++;
		}
		return l;
	}


	static int ipower(int base, int exp)
	{
	    int result = 1;
	    while (exp>0){
            result *= base;
            exp--;
	    }
	    return result;
	}

	public static int divideRoundUp(int num, int divisor) {
	    return ((num + divisor) - 1) / divisor;
	}
}
