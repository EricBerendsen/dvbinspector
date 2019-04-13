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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.intable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 */
public class TargetIPSlashDescriptor extends INTDescriptor {

	private List<IPAdress> ipList = new ArrayList<IPAdress>();


	public static class IPAdress implements TreeNode{

		private final byte[]IPv4_addr;
		private final int IPv4_slash_mask;

		public IPAdress(final byte[] pv4_addr, final int pv4_slash_mask) {
			super();
			IPv4_addr = pv4_addr;
			IPv4_slash_mask = pv4_slash_mask;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("ip-adress(es) "+Utils.formatIPNumber(IPv4_addr)+"/"+IPv4_slash_mask));
			s.add(new DefaultMutableTreeNode(new KVP("IPv4_addr",IPv4_addr,Utils.formatIPNumber(IPv4_addr))));
			s.add(new DefaultMutableTreeNode(new KVP("IPv4_slash_mask",IPv4_slash_mask,null)));
			return s;
		}

	}

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public TargetIPSlashDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		int t = 0;
		while (t<descriptorLength) {
			final byte[] adress = Utils.getBytes(b, offset+2+t, 4);
			final int mask  = Utils.getInt(b, offset+6+t, 1, 0xFF);
			final IPAdress a = new IPAdress(adress,mask);
			ipList.add(a);
			t+=5;
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		Utils.addListJTree(t,ipList,modus,"ip_list");
		return t;
	}

}
