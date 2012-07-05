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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc.DSMCCDescriptorFactory;
import nl.digitalekabeltelevisie.util.Utils;

public class BIOPModuleInfo implements TreeNode {
	private final long moduleTimeOut;
	private final long blockTimeOut;
	private final long minBlockTime;
	private final int taps_count;
	private List<Tap> taps = new ArrayList<Tap>();
	private final int userInfoLength;
	private List<Descriptor> descriptors;


	public BIOPModuleInfo(final byte[] data,final int offset) {
		moduleTimeOut= Utils.getLong(data, offset, 4, Utils.MASK_32BITS);
		blockTimeOut= Utils.getLong(data, offset+4, 4, Utils.MASK_32BITS);
		minBlockTime= Utils.getLong(data, offset+8, 4, Utils.MASK_32BITS);
		taps_count= Utils.getInt(data, offset+12, 1, Utils.MASK_8BITS);

		int off= offset+13;

		for (int i = 0; i < taps_count; i++) {
			final Tap tap =new Tap(data,off);
			taps.add(tap);
			off+= tap.getSelector_length()+7;

		}
		userInfoLength= Utils.getInt(data, off, 1, Utils.MASK_8BITS);
		descriptors = DSMCCDescriptorFactory.buildDescriptorList(data, off+1, userInfoLength);

	}



	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(
				"BIOP::ModuleInfo"));
		t.add(new DefaultMutableTreeNode(new KVP("ModuleTimeOut",moduleTimeOut ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("BlockTimeOut",blockTimeOut ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("MinBlockTime",minBlockTime ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("taps_count",taps_count ,null)));
		addListJTree(t,taps,modus,"BIOP::Taps");
		t.add(new DefaultMutableTreeNode(new KVP("userInfoLength",userInfoLength ,null)));
		Utils.addListJTree(t,descriptors,modus,"descriptors");

		return t;
	}



	public long getModuleTimeOut() {
		return moduleTimeOut;
	}



	public long getBlockTimeOut() {
		return blockTimeOut;
	}



	public long getMinBlockTime() {
		return minBlockTime;
	}



	public int getTaps_count() {
		return taps_count;
	}



	public List<Tap> getTaps() {
		return taps;
	}



	public int getUserInfoLength() {
		return userInfoLength;
	}



	public List<Descriptor> getDescriptors() {
		return descriptors;
	}

}
