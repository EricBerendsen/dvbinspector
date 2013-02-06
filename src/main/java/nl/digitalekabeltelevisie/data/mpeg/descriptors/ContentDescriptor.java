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

public class ContentDescriptor extends Descriptor {

	private List<ContentItem> contentList = new ArrayList<ContentItem>();


	public class ContentItem implements TreeNode{
		/**
		 *
		 */
		private final int contentNibbleLevel1 ;
		private final int contentNibbleLevel2 ;
		private final int userNibble1 ;
		private final int userNibble2;



		public ContentItem(final int contentNibbleLevel1, final int contentNibbleLevel2, final int userNibble1, final int userNibble2) {
			super();
			this.contentNibbleLevel1 = contentNibbleLevel1;
			this.contentNibbleLevel2 = contentNibbleLevel2;
			this.userNibble1 = userNibble1;
			this.userNibble2 = userNibble2;
		}



		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("content type"));
			s.add(new DefaultMutableTreeNode(new KVP("content_nibble_level_1",contentNibbleLevel1,getContentNibbleLevel1String(contentNibbleLevel1))));
			s.add(new DefaultMutableTreeNode(new KVP("content_nibble_level_2",contentNibbleLevel2,getContentNibbleLevel2String(contentNibbleLevel1, contentNibbleLevel2))));
			s.add(new DefaultMutableTreeNode(new KVP("user_nibble1",userNibble1,null)));
			s.add(new DefaultMutableTreeNode(new KVP("user_nibble2",userNibble2,null)));
			return s;
		}



		@Override
		public String toString(){
			return "content_nibble_level_1:"+contentNibbleLevel1 + ", content_nibble_level_2:"+contentNibbleLevel2;
		}



		public int getContentNibbleLevel1() {
			return contentNibbleLevel1;
		}



		public int getContentNibbleLevel2() {
			return contentNibbleLevel2;
		}



		public int getUserNibble1() {
			return userNibble1;
		}



		public int getUserNibble2() {
			return userNibble2;
		}


	}

	public ContentDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final int cNibble1 = Utils.getInt(b, offset+t+2, 1, 0xF0)>>4;
		final int cNnibble2 = Utils.getInt(b, offset+t+2, 1, Utils.MASK_4BITS);
		final int uNibble1 = Utils.getInt(b, offset+t+3, 1, 0xF0)>>4;
		final int uNnibble2 = Utils.getInt(b, offset+t+3, 1, Utils.MASK_4BITS);
		final ContentItem s = new ContentItem(cNibble1,cNnibble2,uNibble1,uNnibble2);
		contentList.add(s);
		t+=5;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (ContentItem contentItem : contentList) {
			buf.append(contentItem.toString());
		}


		return buf.toString();
	}

	public static String getContentNibbleLevel1String(final int nibble1) {
		switch (nibble1) {
		case 0x0: return "undefined content";
		case 0x1: return "Movie/Drama:";
		case 0x2: return "News/Current affairs:";
		case 0x3: return "Show/Game show:";
		case 0x4: return "Sports:";
		case 0x5: return "Children's/Youth programmes:";
		case 0x6: return "Music/Ballet/Dance:";
		case 0x7: return "Arts/Culture (without music)::";
		case 0x8: return "Social/Political issues/Economics:";
		case 0x9: return "Education/Science/Factual topics:";
		case 0xA: return "Leisure hobbies:";
		case 0xB: return "Special characteristics:";
		case 0xF: return "user defined";
		default: return "reserved for future use:";
		}
	}

	public static String getContentNibbleLevel2String(final int nibble1,final int nibble2) {
		switch (nibble1) {
		case 0x0: return "";
		case 0x1: // Movie/Drama:
			switch (nibble2) {
			case 0x0: return "movie/drama (general)";
			case 0x1: return "detective/thriller";
			case 0x2: return "adventure/western/war";
			case 0x3: return "science fiction/fantasy/horror";
			case 0x4: return "comedy";
			case 0x5: return "soap/melodrama/folkloric";
			case 0x6: return "romance";
			case 0x7: return "serious/classical/religious/historical movie/drama";
			case 0x8: return "adult movie/drama";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x2: // News/Current affairs:
			switch (nibble2) {
			case 0x0: return "news/current affairs (general)";
			case 0x1: return "news/weather report";
			case 0x2: return "news magazine";
			case 0x3: return "documentary";
			case 0x4: return "discussion/interview/debate";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x3: // Show/Game show:
			switch (nibble2) {
			case 0x0 : return "show/game show (general)";
			case 0x1 : return "game show/quiz/contest";
			case 0x2 : return "variety show";
			case 0x3 : return "talk show";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x4: // Sports:
			switch (nibble2) {
			case 0x0 : return "sports (general)";
			case 0x1 : return "special events (Olympic Games, World Cup, etc.)";
			case 0x2 : return "sports magazines";
			case 0x3 : return "football/soccer";
			case 0x4 : return "tennis/squash";
			case 0x5 : return "team sports (excluding football)";
			case 0x6 : return "athletics";
			case 0x7 : return "motor sport";
			case 0x8 : return "water sport";
			case 0x9 : return "winter sports";
			case 0xA : return "equestrian";
			case 0xB : return "martial sports";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x5: // Children's/Youth programmes:
			switch (nibble2) {
			case 0x0 : return "children's/youth programmes (general)";
			case 0x1 : return "pre-school children's programmes";
			case 0x2 : return "entertainment programmes for 6 to14";
			case 0x3 : return "entertainment programmes for 10 to 16";
			case 0x4 : return "informational/educational/school programmes";
			case 0x5 : return "cartoons/puppets";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x6: // Music/Ballet/Dance:
			switch (nibble2) {
			case 0x0 : return "music/ballet/dance (general)";
			case 0x1 : return "rock/pop";
			case 0x2 : return "serious music/classical music";
			case 0x3 : return "folk/traditional music";
			case 0x4 : return "jazz";
			case 0x5 : return "musical/opera";
			case 0x6 : return "ballet";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x7: // Arts/Culture (without music):
			switch (nibble2) {
			case 0x0 : return "arts/culture (without music, general)";
			case 0x1 : return "performing arts";
			case 0x2 : return "fine arts";
			case 0x3 : return "religion";
			case 0x4 : return "popular culture/traditional arts";
			case 0x5 : return "literature";
			case 0x6 : return "film/cinema";
			case 0x7 : return "experimental film/video";
			case 0x8 : return "broadcasting/press";
			case 0x9 : return "new media";
			case 0xA : return "arts/culture magazines";
			case 0xB : return "fashion";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x8: // Social/Political issues/Economics:
			switch (nibble2) {
			case 0x0 : return "social/political issues/economics (general)";
			case 0x1 : return "magazines/reports/documentary";
			case 0x2 : return "economics/social advisory";
			case 0x3 : return "remarkable people";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0x9: // Education/Science/Factual topics:
			switch (nibble2) {
			case 0x0 : return "education/science/factual topics (general)";
			case 0x1 : return "nature/animals/environment";
			case 0x2 : return "technology/natural sciences";
			case 0x3 : return "medicine/physiology/psychology";
			case 0x4 : return "foreign countries/expeditions";
			case 0x5 : return "social/spiritual sciences";
			case 0x6 : return "further education";
			case 0x7 : return "languages";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0xA: // Leisure hobbies:
			switch (nibble2) {
			case 0x0 : return "leisure hobbies (general)";
			case 0x1 : return "tourism/travel";
			case 0x2 : return "handicraft";
			case 0x3 : return "motoring";
			case 0x4 : return "fitness and health";
			case 0x5 : return "cooking";
			case 0x6 : return "advertisement/shopping";
			case 0x7 : return "gardening";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0xB: // Special characteristics:
			switch (nibble2) {
			case 0x0 : return "original language";
			case 0x1 : return "black and white";
			case 0x2 : return "unpublished";
			case 0x3 : return "live broadcast";
			case 0xF: return "user defined";
			default: return "reserved for future use";
			}
		case 0xF: return "";
		default: return "reserved for future use";
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		Utils.addListJTree(t,contentList,modus,"content_list");
		return t;
	}

	public List<ContentItem> getContentList() {
		return contentList;
	}
}
