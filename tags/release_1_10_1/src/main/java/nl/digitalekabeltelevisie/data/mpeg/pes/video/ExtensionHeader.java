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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;


/**
 * @author Eric Berendsen
 *
 */
public class ExtensionHeader extends VideoMPEG2Section {

	private final int extension_start_code_identifier;


	/**
	 * @param data
	 * @param offset
	 */
	public ExtensionHeader(final byte[] data, final int offset) {
		super(data, offset);
		extension_start_code_identifier = bs.readBits(4);

	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("extension_start_code_identifier",extension_start_code_identifier,getExtensionStartCodeIdentifierString(extension_start_code_identifier))));
		return t;
	}


	public static String getExtensionStartCodeIdentifierString(final int startCode) {

		switch (startCode) {
		case 0: return "reserved";
		case 1: return "Sequence Extension ID";
		case 2 : return "Sequence Display Extension ID";
		case 3 : return "Quant Matrix Extension ID";
		case 4 : return "Copyright Extension ID";
		case 5 : return "Sequence Scalable Extension ID";
		case 6 : return "reserved";
		case 7 : return "Picture Display Extension ID";
		case 8 : return "Picture Coding Extension ID";
		case 9 : return "Picture Spatial Scalable Extension ID";
		case 10 : return "Picture Temporal Scalable Extension ID";
		default:
			return "reserved";
		}
	}


	/**
	 * @return the extension_start_code_identifier
	 */
	public int getExtension_start_code_identifier() {
		return extension_start_code_identifier;
	}

}
