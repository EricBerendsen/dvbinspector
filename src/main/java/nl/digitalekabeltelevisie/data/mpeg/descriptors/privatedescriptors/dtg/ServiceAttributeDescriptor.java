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

import javax.swing.tree.DefaultMutableTreeNode;

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
	
	
	public class ServiceAttribute implements TreeNode{


		private int service_id;
		private int reserved;
		private int numeric_selection_flag;
		private int visible_service_flag;
		
		
		public ServiceAttribute(int service_id, int reserved, int numeric_selection_flag, int visible_service_flag) {
			super();
			this.service_id = service_id;
			this.reserved = reserved;
			this.numeric_selection_flag = numeric_selection_flag;
			this.visible_service_flag = visible_service_flag;
		}

		
		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("Service attribute"));
			s.add(new DefaultMutableTreeNode(new KVP("service_id",service_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			s.add(new DefaultMutableTreeNode(new KVP("numeric_selection_flag",numeric_selection_flag,null)));
			s.add(new DefaultMutableTreeNode(new KVP("visible_service_flag",visible_service_flag,getBehaviourString(numeric_selection_flag,visible_service_flag))));
			return s;
		}
		
		private String getBehaviourString(int numeric_selection_flag, int visible_service_flag) {
			if(visible_service_flag==1) {
				return "Service is visible and selectable";
			}
			if(numeric_selection_flag == 1) {
				return "Service is hidden but selectable through direct numeric entry";
			}
			return "Service is hidden and non-selectable";
		}
		
	}


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public ServiceAttributeDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final int service_id=getInt(b, offset+2+t,2,MASK_16BITS);
			final int reserved = getInt(b,offset+t+4,1,0b1111_1100) >>2;
			final int numeric_selection_flag=getInt(b, offset+t+4,1,0b0000_0010)>>1;
			final int visible_service_flag=getInt(b, offset+t+4,1,0b0000_001);
			final ServiceAttribute s = new ServiceAttribute(service_id, reserved, numeric_selection_flag,visible_service_flag);
			serviceAttributeList.add(s);
			t+=3;
		}
	}
	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,serviceAttributeList,modus,"Service Attributes");
		return t;
	}

	@Override
	public String getDescriptorname(){
		return "DTG Service attribute descriptor";
	}


}
