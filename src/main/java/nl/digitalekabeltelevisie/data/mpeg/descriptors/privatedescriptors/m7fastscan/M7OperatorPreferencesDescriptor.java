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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan;

import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class M7OperatorPreferencesDescriptor extends M7Descriptor {

	private String country_code;
	private String menu_ISO_639_language_code;
	private String audio1_ISO_639_language_code;
	private String audio2_ISO_639_language_code;
	private String subs_ISO_639_language_code;
	
	public M7OperatorPreferencesDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		country_code = getISO8859_1String(b,offset+2,3);
		menu_ISO_639_language_code = getISO8859_1String(b,offset+5,3);
		audio1_ISO_639_language_code = getISO8859_1String(b,offset+8,3);
		audio2_ISO_639_language_code = getISO8859_1String(b,offset+11,3);
		subs_ISO_639_language_code = getISO8859_1String(b,offset+14,3);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("country_code",country_code ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("menu_ISO_639_language_code",menu_ISO_639_language_code ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("audio1_ISO_639_language_code",audio1_ISO_639_language_code ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("audio2_ISO_639_language_code",audio2_ISO_639_language_code ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("subs_ISO_639_language_code",subs_ISO_639_language_code ,null)));
		return t;
	}

	public String getCountry_code() {
		return country_code;
	}

	public String getMenu_ISO_639_language_code() {
		return menu_ISO_639_language_code;
	}

	public String getAudio1_ISO_639_language_code() {
		return audio1_ISO_639_language_code;
	}

	public String getAudio2_ISO_639_language_code() {
		return audio2_ISO_639_language_code;
	}

	public String getSubs_ISO_639_language_code() {
		return subs_ISO_639_language_code;
	}

}
