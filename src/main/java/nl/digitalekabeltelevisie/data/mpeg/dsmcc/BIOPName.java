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

import static java.util.Arrays.copyOfRange;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric
 *
 */
public class BIOPName implements TreeNode {

	public record NameComponent(int id_length, byte[] id_data_byte, int kind_length,
								byte[] kind_data_byte) implements TreeNode {
			public NameComponent {
		}

			public DefaultMutableTreeNode getJTreeNode(int modus) {
				DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("NameComponent"));
				t.add(new DefaultMutableTreeNode(new KVP("id_length", id_length, null)));
				t.add(new DefaultMutableTreeNode(new KVP("id_data_byte", id_data_byte, null)));
				t.add(new DefaultMutableTreeNode(new KVP("kind_length", kind_length, null)));
				t.add(new DefaultMutableTreeNode(new KVP("kind_data_byte", kind_data_byte, null)));
				return t;
			}


	}

	private final int nameComponents_count;
	private final List<NameComponent> nameComponents = new ArrayList<>();
	private int len = 0;


	public BIOPName(byte[] data, int r) {
		nameComponents_count = Utils.getInt(data, r, 1, Utils.MASK_8BITS);
		len += 1;
		for (int i = 0; i < nameComponents_count; i++) {
			int id_length = Utils.getInt(data, r+len, 1, Utils.MASK_8BITS);
			len += 1;
			byte[] id_data_byte = copyOfRange(data, r + len, r + len + id_length);
			len += id_length;
			int kind_length = Utils.getInt(data, r+len, 1, Utils.MASK_8BITS);
			len += 1;
			byte[] kind_data_byte = copyOfRange(data, r + len, r + len + kind_length);
			len += kind_length;
			NameComponent nameComponent = new NameComponent(id_length, id_data_byte, kind_length, kind_data_byte);
			nameComponents.add(nameComponent);
		}

	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */

	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("BIOP::Name");
		t.add(new KVP("nameComponents_count",nameComponents_count));
		t.addList(nameComponents,modus,"Name Components");
		return t;
	}


	public int getNameComponents_count() {
		return nameComponents_count;
	}


	public List<NameComponent> getNameComponents() {
		return nameComponents;
	}


	public int getLen() {
		return len;
	}

	public String getName(){
		String r =null;
		NameComponent comp = nameComponents.getFirst();
		if(comp!=null){
			byte[] b= copyOfRange(comp.id_data_byte(), 0, comp.id_length() - 1);
			r =Utils.toSafeString(b);
		}
		return r;
	}

}
