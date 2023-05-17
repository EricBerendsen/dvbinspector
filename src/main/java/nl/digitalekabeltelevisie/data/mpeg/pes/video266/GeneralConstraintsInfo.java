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

package nl.digitalekabeltelevisie.data.mpeg.pes.video266;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 * 
 * Based on Rec. ITU-T H.266 (04/2022) 7.3.3.2 General constraints information syntax
 *
 */
public class GeneralConstraintsInfo implements TreeNode {

	private static final Logger logger = Logger.getLogger(GeneralConstraintsInfo.class.getName());

	
	private int gci_present_flag;


	public GeneralConstraintsInfo(BitSource bitSource) {
		gci_present_flag = bitSource.u(1);
		if (gci_present_flag == 1) {
			logger.info("gci_present_flag==1 not implemented");

		}
		bitSource.skiptoByteBoundary();

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("general_constraints_info"));
		t.add(new DefaultMutableTreeNode(new KVP("gci_present_flag",gci_present_flag,gci_present_flag==0?"no additional syntax elements are present in general_constraints_info":"additional syntax elements are present in general_constraints_info")));
		if(gci_present_flag==1) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("gci_present_flag==1")));
		}
		return t;
	}

}
