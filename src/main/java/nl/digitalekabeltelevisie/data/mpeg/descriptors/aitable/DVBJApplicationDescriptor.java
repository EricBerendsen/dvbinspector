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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class DVBJApplicationDescriptor extends AITDescriptor {


	private List<Parameter> parameterList= new ArrayList<>();
	record Parameter(int parameter_length, byte[] parameter_byte) implements TreeNode{
		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("Parameter");
			s.add(new KVP("parameter_length", parameter_length));
			s.add(new KVP("parameter_byte", parameter_byte));
			return s;
		}

	}

	/**
	 * @param b
	 * @param parent
	 */
	public DVBJApplicationDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		int t = 0;

		while (t < descriptorLength) {
			int parameter_length = getInt(b, t + 2, 1, MASK_8BITS);
			byte[] parameter_bytes = Arrays.copyOfRange(b, t + 3, t + 3 + parameter_length);
			parameterList.add(new Parameter(parameter_length, parameter_bytes));
			t += parameter_length + 1;
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		addListJTree(t,parameterList,modus,"parameters");
		return t;
	}

}
