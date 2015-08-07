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

/**
 * @author Eric Berendsen
 * 
 */
public class ComponentDescriptor extends Descriptor {

	private int streamContentExt;

	private int streamContent;

	private final int componentType;

	private final int componentTag;

	private final String iso639LanguageCode;

	// TODO this should be a DVBString, but then it has to follow a length byte. That is not there....

	private final String text;

	public ComponentDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		streamContentExt = getInt(b, offset + 2, 2, 0xF0) >> 4;
		streamContent = getInt(b, offset + 2, 1, MASK_4BITS);
		componentType = getInt(b, offset + 3, 1, MASK_8BITS);
		componentTag = getInt(b, offset + 4, 1, MASK_8BITS);
		iso639LanguageCode = getISO8859_1String(b, offset + 5, 3);
		text = Utils.getString(b, offset + 8, descriptorLength - 6);
	}

	@Override
	public String toString() {
		return super.toString() + "streamContent=" + streamContent;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("stream_content_ext", streamContentExt, null)));
		t.add(new DefaultMutableTreeNode(new KVP("stream_content", streamContent, null)));
		t.add(new DefaultMutableTreeNode(new KVP("component_type", componentType, getComponentDescriptorString(streamContent, streamContentExt, componentType))));
		t.add(new DefaultMutableTreeNode(new KVP("component_tag", componentTag,	null)));
		t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code", iso639LanguageCode, null)));
		t.add(new DefaultMutableTreeNode(new KVP("text", text, null)));
		return t;
	}

	public int getStreamContent() {
		return streamContent;
	}

	public void setStreamContent(final int maximumBitrate) {
		this.streamContent = maximumBitrate;
	}

	public int getReserved() {
		return streamContentExt;
	}

	public void setReserved(final int reserverd) {
		this.streamContentExt = reserverd;
	}

	public int getStreamContentExt() {
		return streamContentExt;
	}

}
