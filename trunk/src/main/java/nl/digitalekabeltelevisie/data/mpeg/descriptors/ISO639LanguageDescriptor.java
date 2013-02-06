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

public class ISO639LanguageDescriptor extends Descriptor {

	private final List<Language> languageList = new ArrayList<Language>();


	public static class Language implements TreeNode{
		/**
		 *
		 */
		private final String iso639LanguageCode;
		/**
		 *
		 */
		private final int audioType;


		public Language(final String lCode, final int audioT){
			iso639LanguageCode = lCode;
			audioType = audioT;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("language"));
			s.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("audio_type",audioType,getAudioTypeString(audioType))));
			return s;
		}


		public int getAudioType() {
			return audioType;
		}


		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}


		@Override
		public String toString(){
			return "code:'"+iso639LanguageCode+"', audio:"+audioType;
		}


	}

	public ISO639LanguageDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final String languageCode=getISO8859_1String(b, offset+t+2, 3);
			final int audio = getInt(b, offset+t+5, 1, MASK_8BITS);
			final Language s = new Language(languageCode, audio);
			languageList.add(s);
			t+=4;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Language language : languageList) {
			buf.append(language.toString());
		}


		return buf.toString();
	}

	public static String getAudioTypeString(final int audio) {
		switch (audio) {
		case 0: return "Main Audio";
		case 1: return "Clean effects";
		case 2: return "Hearing impaired";
		case 3: return "Visual impaired commentary";
		default:
			if ((audio >= 0x04) && (audio <= 0x7F)){
				return "User Private";
			}
			return "Reserved";
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,languageList,modus,"language_list");
		return t;
	}

	public List<Language> getLanguageList() {
		return languageList;
	}
}
