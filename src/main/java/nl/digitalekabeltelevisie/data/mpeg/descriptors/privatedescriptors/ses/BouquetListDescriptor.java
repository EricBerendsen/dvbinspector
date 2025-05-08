/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * 
 */
public class BouquetListDescriptor extends Descriptor {
	
	public record BouquetName(DVBString dvbString) implements TreeNode{

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			return new KVP("bouquet_name", dvbString);
		}
		
	}
	List<BouquetName> bouquet_names = new ArrayList<>();

	public BouquetListDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t=0;
		while (t<descriptorLength) {
			DVBString bouquet_name = new DVBString(b, t + 2);
			bouquet_names.add(new BouquetName(bouquet_name));
			t += bouquet_name.getLength() + 1;
			
		}

	}
	@Override
	public String getDescriptorname(){
		return "Bouquet List Descriptor";
	}

	@Override
	public KVP getJTreeNode(int modus){

		KVP t = (KVP)super.getJTreeNode(modus);
		addListJTree(t,bouquet_names, modus,"bouquet_names");
		return t;
	}

}
