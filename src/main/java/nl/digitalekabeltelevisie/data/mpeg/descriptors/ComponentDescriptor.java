/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_4BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 * 
 */
public class ComponentDescriptor extends LanguageDependentEitDescriptor{

	private int streamContentExt;

	private int streamContent;

	private final int componentType;

	private final int componentTag;

	private final String iso639LanguageCode;

	private final DVBString text;

	public ComponentDescriptor(byte[] b, final TableSection parent) {
		super(b, parent);
		streamContentExt = getInt(b, 2, 1, 0xF0) >> 4;
		streamContent = getInt(b, 2, 1, MASK_4BITS);
		componentType = getInt(b, 3, 1, MASK_8BITS);
		componentTag = getInt(b, 4, 1, MASK_8BITS);
		iso639LanguageCode = getISO8859_1String(b, 5, 3);
		text =  new DVBString(b, 8, descriptorLength - 6);
	}

	@Override
	public String toString() {
		return super.toString() + "streamContent=" + streamContent;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("stream_content_ext", streamContentExt));
		t.add(new KVP("stream_content", streamContent));
		t.add(new KVP("component_type", componentType).setDescription(getStreamTypeString()));
		t.add(new KVP("component_tag", componentTag));
		t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
		t.add(new KVP("text", text));
		return t;
	}

	public String getStreamTypeString() {
		return getComponentDescriptorString(streamContent, streamContentExt, componentType);
	}

	public int getStreamContent() {
		return streamContent;
	}

	public int getReserved() {
		return streamContentExt;
	}

	public int getStreamContentExt() {
		return streamContentExt;
	}

	public int getComponentType() {
		return componentType;
	}

	public int getComponentTag() {
		return componentTag;
	}

	@Override
	public String getIso639LanguageCode() {
		return iso639LanguageCode;
	}

	public DVBString getText() {
		return text;
	}

}
