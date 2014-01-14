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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 * Also used as VBI teletext descriptor, as "the semantics for the VBI teletext descriptor is the same as defined for the teletext descriptor" ETSI EN 300 468 V1.11.1 (2010-04)
 * (don0t understand the difference, and sometimes both are used. See astra/2010-7-19-13-36-11856-27500-S.ts
 *
 */
public class TeletextDescriptor extends Descriptor {

	private final List<Teletext> teletextList = new ArrayList<Teletext>();


	public static class Teletext implements TreeNode{
		/**
		 *
		 */
		private final String iso639LanguageCode;
		private final int teletextType ;
		private final int teletextMagazineNumber;
		private final int teletextPageNumber;


		public Teletext(final String lCode, final int tType,final int tMagazine,final int tPage){
			iso639LanguageCode = lCode;
			teletextType = tType;
			teletextMagazineNumber = tMagazine;
			teletextPageNumber = tPage;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("teletext"));
			s.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("teletext_type",teletextType,getTeletextTypeString(teletextType))));
			s.add(new DefaultMutableTreeNode(new KVP("teletext_magazine_number",teletextMagazineNumber,null)));
			s.add(new DefaultMutableTreeNode(new KVP("teletext_page_number",teletextPageNumber,null)));
			return s;
		}



		@Override
		public String toString(){
			return "code:'"+iso639LanguageCode;
		}


		public int getTeletextType() {
			return teletextType;
		}


		public int getTeletextMagazineNumber() {
			return teletextMagazineNumber;
		}


		public int getTeletextPageNumber() {
			return teletextPageNumber;
		}


		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}


	}

	public TeletextDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while ((t+4)<descriptorLength) {
			final String languageCode=getISO8859_1String(b, offset+2+t, 3);
			final int teletext_type = getInt(b,offset+ t+5, 1, 0xF8)>>3;
		final int teletext_magazine_number = getInt(b, offset+t+5, 1, 0x07);
		final int teletext__page_number = getInt(b, offset+t+6, 1, 0xFF);
		final Teletext s = new Teletext(languageCode, teletext_type,teletext_magazine_number,teletext__page_number);
		teletextList.add(s);
		t+=5;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Teletext teletext : teletextList) {
			buf.append(teletext.toString());
		}


		return buf.toString();
	}

	public static String getTeletextTypeString(final int type) {
		switch (type) {
		case 0: return "reserved for future use";
		case 1: return "initial Teletext page";
		case 2: return "Teletext subtitle page";
		case 3: return "additional information page";
		case 4: return "programme schedule page";
		case 5: return "Teletext subtitle page for hearing impaired people";
		default: return "reserved for future use";
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,teletextList,modus,"teletext_list");
		return t;
	}

	public List<Teletext> getTeletextList() {
		return teletextList;
	}
}
