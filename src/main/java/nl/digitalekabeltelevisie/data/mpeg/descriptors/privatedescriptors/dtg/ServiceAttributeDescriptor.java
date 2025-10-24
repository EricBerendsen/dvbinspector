/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric
 *
 *Based on  D-Book 7 Part A v 1 March 2011 ch 8.5.3.9 Service attribute descriptor
 */
public class ServiceAttributeDescriptor extends Descriptor {
	
	List<ServiceAttribute> serviceAttributeList = new ArrayList<>();
	
	
	public record ServiceAttribute(int service_id, int reserved, int numeric_selection_flag, int visible_service_flag) implements TreeNode{


		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("Service attribute");
			s.add(new KVP("service_id", service_id));
			s.add(new KVP("reserved", reserved));
			s.add(new KVP("numeric_selection_flag", numeric_selection_flag));
			s.add(new KVP("visible_service_flag", visible_service_flag, getBehaviourString(numeric_selection_flag, visible_service_flag)));
			return s;
		}

		private static String getBehaviourString(int numeric_selection_flag, int visible_service_flag) {
			if(visible_service_flag==1) {
				return "Service is visible and selectable";
			}
			if(numeric_selection_flag == 1) {
				return "Service is hidden but selectable through direct numeric entry";
			}
			return "Service is hidden and non-selectable";
		}
		
	}

	public ServiceAttributeDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 0;
		while (t < descriptorLength) {
			int service_id = getInt(b, 2 + t, 2, MASK_16BITS);
			int reserved = getInt(b, t + 4, 1, 0b1111_1100) >> 2;
			int numeric_selection_flag = getInt(b, t + 4, 1, 0b0000_0010) >> 1;
			int visible_service_flag = getInt(b, t + 4, 1, 0b0000_001);
			ServiceAttribute s = new ServiceAttribute(service_id, reserved, numeric_selection_flag, visible_service_flag);
			serviceAttributeList.add(s);
			t += 3;
		}
	}
	
	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		addListJTree(t,serviceAttributeList,modus,"Service Attributes");
		return t;
	}

	@Override
	public String getDescriptorname(){
		return "DTG Service attribute descriptor";
	}


}
