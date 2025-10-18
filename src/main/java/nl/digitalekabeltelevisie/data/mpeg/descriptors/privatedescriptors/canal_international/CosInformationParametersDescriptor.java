/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric
 *
 */
public class CosInformationParametersDescriptor extends Descriptor {
	
	private final List<InformationParameter> informationParameters = new ArrayList<>();
	
	public record InformationParameter(int info_parameter_id, int info_parameter_length, byte[] info_parameter_byte) implements TreeNode{


		@Override
		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("InformationParameter");
			t.add(new KVP("info_parameter_id",info_parameter_id,getInfoParameterIdDescription(info_parameter_id)));
			t.add(new KVP("info_parameter_length",info_parameter_length));
			t.add(new KVP("info_parameter_byte",info_parameter_byte));
			return t;
		}


		private static String getInfoParameterIdDescription(int info_parameter_id) {
			return switch (info_parameter_id) {
			case 1 -> "Call center phone number";
			case 2 -> "Web address";
			default -> "Reserved for future use";
			};

		}
	}

	public CosInformationParametersDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		int t = 0;
		while (t < descriptorLength) {
			int info_parameter_id = getInt(b, t + 2, 1, MASK_8BITS);
			int info_parameter_length = getInt(b, t + 3, 1, MASK_8BITS);
			byte[] info_parameter_byte = Utils.getBytes(b, t + 4, info_parameter_length);
			informationParameters.add(new InformationParameter(info_parameter_id, info_parameter_length, info_parameter_byte));
			t += 2 + info_parameter_length;
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		addListJTree(t, informationParameters, modus, "Information Parameters");
		return t;
	}

	@Override
	public String getDescriptorname(){
		return "cos_information_parameters_descriptor";
	}

}
