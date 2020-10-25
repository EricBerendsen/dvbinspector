/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x.sei;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * 
 * based on Rec. ITU-T H.265 v7 (11/2019) D.2.35 Content light level information SEI message syntax
 * and D.3.35 Content light level information SEI message semantics

 * @author Eric
 *
 */
public class ContentLightLevelInformationSei_message extends Sei_message {

	private int max_content_light_level;
	private int max_pic_average_light_level;

	/**
	 * @param bitSource
	 */
	public ContentLightLevelInformationSei_message(BitSource bitSource) {
		super(bitSource);

		// bitSource has been read by super(), so now convert payload back into BitSource
		var bitSourcePayload = new BitSource(payload, 0);
		
		max_content_light_level = bitSourcePayload.u(16);
		max_pic_average_light_level = bitSourcePayload.u(16);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=super.getJTreeNode(modus);
		s.add(new DefaultMutableTreeNode(new KVP("max_content_light_level",max_content_light_level,getCdm2x(max_content_light_level))));
		s.add(new DefaultMutableTreeNode(new KVP("max_pic_average_light_level",max_pic_average_light_level,getCdm2x(max_pic_average_light_level))));

		return s;
	}

	private static String getCdm2x(int i) {
		if (i > 0) {
			return String.format("%d cd/m²", i);
		}
		return "unknown";
	}

}
