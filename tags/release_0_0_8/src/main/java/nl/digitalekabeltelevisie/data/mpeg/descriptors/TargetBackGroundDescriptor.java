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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class TargetBackGroundDescriptor extends Descriptor {

	private final int horizontalSize ;
	private final int verticalSize ;
	private final int aspectRatioInformation ;

	public TargetBackGroundDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		horizontalSize = getInt(b,offset+2,2,0xFFFC)>>2; // first 14 bits
		verticalSize = getInt(b,offset+3,3,0x03FFF0)>>4; // 14 bits over 3 bytes...
		aspectRatioInformation = getInt(b,offset+5,1,MASK_4BITS); // 4 bits
	}

	@Override
	public String toString() {
		return super.toString() + "horizontalSize="+horizontalSize+ ",verticalSize="+verticalSize;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("horizontal_size ",horizontalSize ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vertical_size",verticalSize ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("aspect_ratio_information ",aspectRatioInformation ,Utils.getAspectRatioInformationString(aspectRatioInformation))));
		return t;
	}




}
