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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class SubtitlingDescriptor extends Descriptor {

	private final List<Subtitle> subtitleList = new ArrayList<Subtitle>();


	public static class Subtitle implements TreeNode{
		/**
		 *
		 */
		private final String iso639LanguageCode;
		private final int subtitlingType ;
		private final int compositionPageId;
		private final int ancillaryPageId;


		public Subtitle(final String lCode, final int sType,final int sCompositionPageDd,final int aPageId){
			iso639LanguageCode = lCode;
			subtitlingType = sType;
			compositionPageId = sCompositionPageDd;
			ancillaryPageId = aPageId;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("subtitle"));
			s.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("subtitling_type",subtitlingType,getComponentType0x03String(subtitlingType))));
			s.add(new DefaultMutableTreeNode(new KVP("composition_page_id",compositionPageId,null)));
			s.add(new DefaultMutableTreeNode(new KVP("ancillary_page_id",ancillaryPageId,null)));
			return s;
		}



		@Override
		public String toString(){
			return "code:'"+iso639LanguageCode;
		}


		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}


		public int getSubtitlingType() {
			return subtitlingType;
		}


		public int getCompositionPageId() {
			return compositionPageId;
		}


		public int getAncillaryPageId() {
			return ancillaryPageId;
		}


	}

	public SubtitlingDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final String languageCode=Utils.getISO8859_1String(b, offset+2+t, 3);
			final int subtitling_type = Utils.getInt(b, offset+5+t, 1, Utils.MASK_8BITS);
			final int composition_page_id = Utils.getInt(b, offset+6+t, 2, Utils.MASK_16BITS);
			final int ancillary_page_id = Utils.getInt(b, offset+8+t, 2, Utils.MASK_16BITS);
			final Subtitle s = new Subtitle(languageCode, subtitling_type,composition_page_id,ancillary_page_id);
			subtitleList.add(s);
			t+=8;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Subtitle subtitle : subtitleList) {
			buf.append(subtitle.toString());
		}


		return buf.toString();
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		Utils.addListJTree(t,subtitleList,modus,"subtitle_list");
		return t;
	}

	public List<Subtitle> getSubtitleList() {
		return subtitleList;
	}
}
