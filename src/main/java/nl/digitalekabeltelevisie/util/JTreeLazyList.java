/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.util.Collections;
import java.util.Enumeration;


import javax.swing.tree.MutableTreeNode;

/**
 * Class for holding a (long) list of MutableTreeNode's, and loading them only when needed (Lazy evaluation)
 * Will group items by STEP_SIZE (100), and create a tree with as many levels as needed.
 *
 *
 *Example: a list of 239 items will look like this;
 * <pre>
 * Items [0..238]
 * +[0..99]
 * +[100..199]
 * +[200..238]
 *   Item [200]
 *   Item [201]
 *   .
 *   .
 *   Item [238]
 *
 * </pre>
 * @author Eric
 *
 */
public class JTreeLazyList {


	private static final int STEP_SIZE = 100;
	private RangeNode mutableTreeNode = null;

	LazyListItemGetter itemGetter =null;


	public class RangeNode implements MutableTreeNode {

		/**
		 * level = 0 -> All children are leafs
		 * level > 0, direct children are RangeNodes with new level = level - 1
		 */
		final int level;
		final int start;
		final int end;
		private MutableTreeNode[] children=null;
		
		private MutableTreeNode parent;
		
		private String label="";

		/**
		 * @param level
		 * @param start
		 * @param end
		 */
		private RangeNode(int level, int start, int end, MutableTreeNode parent) {
			super();
			this.level = level;
			this.start = start;
			this.end = end;
			this.parent = parent;
		}

		private RangeNode(int level, int start, int end,String label) {
			super();
			this.level = level;
			this.start = start;
			this.end = end;
			this.label = label;
		}

		public String toString(){
			StringBuilder b = new StringBuilder();
			b.append(label).append('[').
				append(itemGetter.getActualNumberForIndex(start)).
				append("..").
				append(itemGetter.getActualNumberForIndex(end - 1)).
				append(']');
			return b.toString();
		}

		private RangeNode createChild(int level, int currentStart, int index) {
			final int maxLeafsChild = ipower(STEP_SIZE, level);
			int childStart = currentStart + (maxLeafsChild * index);
			int childEnd = (currentStart + (maxLeafsChild * (index + 1)));
			childEnd = Math.min(itemGetter.getNoItems(), childEnd);
			return new RangeNode(level - 1, childStart, childEnd, this);
		}

		@Override
		public MutableTreeNode getChildAt(int childIndex) {
			if (level > 0) {
				RangeNode t = null;
				
				if (children == null) {
					children = new MutableTreeNode[STEP_SIZE];
				}
				t = (RangeNode)children[childIndex];
				if(t==null){
					t=createChild(level, start, childIndex);
					children[childIndex]=t;
				}
				return t;
			}
			MutableTreeNode t = null;
			if (children == null) {
				children = new MutableTreeNode[STEP_SIZE];
			}
			t = children[childIndex];
			if (t == null) {
				t = itemGetter.getTreeNode(childIndex + start);
				t.setParent(this);
				children[childIndex] = t;
			}
			return t;
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#getChildCount()
		 */
		@Override
		public int getChildCount() {
			final int maxLeafsCurrentNode = ipower(STEP_SIZE, level+1);
			if ((start + maxLeafsCurrentNode) <= itemGetter.getNoItems()) {
				return STEP_SIZE;
			}
			if (level == 0) {
				return end - start;
			}
			return divideRoundUp((end-start),ipower(STEP_SIZE,level));
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeNode#getParent()
		 */
		@Override
		public javax.swing.tree.TreeNode getParent() {
			return parent;
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
		@Override
		public Enumeration<MutableTreeNode> children() {
			// first preload all children
			int childCount = getChildCount();
			for(int t=0;t<childCount;t++){
				getChildAt(t);
			}
			return Collections.enumeration(Arrays.asList(children));
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
			throw new IllegalStateException("node does not allow remove");

		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#setUserObject(java.lang.Object)
		 */
		@Override
		public void setUserObject(Object object) {
			// null
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#removeFromParent()
		 */
		@Override
		public void removeFromParent() {
			// empty
		}

		/* (non-Javadoc)
		 * @see javax.swing.tree.MutableTreeNode#setParent(javax.swing.tree.MutableTreeNode)
		 */
		@Override
		public void setParent(MutableTreeNode newParent) {
			parent = newParent;
		}

		public String getLabel() {
			return label;
		}

		/**
		 * @param children = new MutableTreeNode[STEP_SIZE];
		 * @return
		 */
		public MutableTreeNode findChildForActual(int actual) {
			int index = itemGetter.getIndexForActualNumber(actual);
			return findChildForIndex(index);
		}

		/**
		 * @param index
		 * @return
		 */
		private MutableTreeNode findChildForIndex(int index) {
			if(level == 0) {
				return getChildAt(index);
			}
			int divisor = ipower(STEP_SIZE,level);			
			MutableTreeNode child = getChildAt(Integer.divideUnsigned(index, divisor));
			int newIndex = Integer.remainderUnsigned(index, divisor);
			if(child instanceof RangeNode rangeNode) {
				return rangeNode.findChildForIndex(newIndex);
			}

			return null;
		}

	}

	/**
	 *
	 */
	public JTreeLazyList(LazyListItemGetter itemGetter) {
		this.itemGetter = itemGetter;
	}


	/**
	 * @param modus
	 * @param label display name for the top-element of the tree
	 * @return
	 */
	public RangeNode getJTreeNode(int modus, String label) {
		if(mutableTreeNode==null){
			int level = determineLevel(itemGetter.getNoItems());
			mutableTreeNode = new RangeNode(level, 0, itemGetter.getNoItems(),label);
		}

		return mutableTreeNode;
	}

	/**
	 * @param noPackets2
	 * @return
	 */
	private static int determineLevel(int noPackets2) {
		int level=0;
		int upperBound = STEP_SIZE;
		while(upperBound < noPackets2){
			upperBound *= STEP_SIZE;
			level++;
		}
		return level;
	}


	static int ipower(int base, int exponent) {
		int exp = exponent;
		int result = 1;
		while (exp > 0) {
			result *= base;
			exp--;
		}
		return result;
	}

	public static int divideRoundUp(int num, int divisor) {
		return ((num + divisor) - 1) / divisor;
	}
}
