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

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class ShortEventDescriptor extends Descriptor {


	private String  iso639LanguageCode;
	private final DVBString eventName;
	private DVBString text;

	public ShortEventDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		iso639LanguageCode = getISO8859_1String(b,offset+2,3);
		final int eventNameLength = getInt(b, offset+5, 1, MASK_8BITS);
		eventName = new DVBString(b,offset+5);
		text = new DVBString(b,offset+6 +eventNameLength);

	}

	public String getIso639LanguageCode() {
		return iso639LanguageCode;
	}

	public void setIso639LanguageCode(final String networkName) {
		this.iso639LanguageCode = networkName;
	}

	@Override
	public String toString() {
		return super.toString() + "ISO_639_language_code="+getIso639LanguageCode();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		if(Utils.simpleModus(modus)){
			t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode ,null)));
			if(eventName.getLength()>0){
				t.add(new DefaultMutableTreeNode(new KVP("event_name",eventName ,null)));
			}
			if(text.getLength()>0){
				t.add(new DefaultMutableTreeNode(new KVP("text",text,null)));
			}
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("event_name_encoding",eventName.getEncodingString(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("event_name_length",eventName.getLength(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("event_name",eventName ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("text_encoding",text.getEncodingString(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("text_length",text.getLength(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("text",text,null)));

		}
		return t;
	}

	public DVBString getEventName() {
		return eventName;
	}

	public DVBString getText() {
		return text;
	}

	public void setText(final DVBString text) {
		this.text = text;
	}

}
