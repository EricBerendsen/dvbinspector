/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class ContentDescriptor extends Descriptor {

	private List<ContentItem> contentList = new ArrayList<>();

	public static record ContentItem(int contentNibbleLevel1, int contentNibbleLevel2, int user_byte) implements TreeNode{

		@Override
		public KVP getJTreeNode(final int modus) {
			final KVP s = new KVP("content type");
			s.add(new KVP("content_nibble_level_1", contentNibbleLevel1).setDescription(getContentNibbleLevel1String(contentNibbleLevel1)));
			s.add(new KVP("content_nibble_level_2", contentNibbleLevel2).setDescription(getContentNibbleLevel2String(contentNibbleLevel1, contentNibbleLevel2)));
			s.add(new KVP("user_byte", user_byte));
			return s;
		}

	}

	public ContentDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 0;
		while (t < descriptorLength) {
			int cNibble1 = Utils.getInt(b, t + 2, 1, 0xF0) >> 4;
			int cNnibble2 = Utils.getInt(b, t + 2, 1, Utils.MASK_4BITS);
			int user_byte = Utils.getInt(b, t + 3, 1, Utils.MASK_8BITS);
			ContentItem s = new ContentItem(cNibble1, cNnibble2, user_byte);
			contentList.add(s);
			t += 2;
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

	public static String getContentNibbleLevel1String(int nibble1) {
		return switch (nibble1) {
		case 0x0 -> "undefined content";
		case 0x1 -> "Movie/Drama:";
		case 0x2 -> "News/Current affairs:";
		case 0x3 -> "Show/Game show:";
		case 0x4 -> "Sports:";
		case 0x5 -> "Children's/Youth programmes:";
		case 0x6 -> "Music/Ballet/Dance:";
		case 0x7 -> "Arts/Culture (without music):";
		case 0x8 -> "Social/Political issues/Economics:";
		case 0x9 -> "Education/Science/Factual topics:";
		case 0xA -> "Leisure hobbies:";
		case 0xB -> "Special characteristics:";
		case 0xC -> "Adult:";
		case 0xF -> "user defined";
		default -> "reserved for future use:";
		};
	}

	public static String getContentNibbleLevel2String(int nibble1,int nibble2) {
		return switch (nibble1) {
		case 0x0:
			yield "";
		case 0x1: // Movie/Drama:
			yield switch (nibble2) {
						case 0x0 -> "movie/drama (general)";
						case 0x1 -> "detective/thriller";
						case 0x2 -> "adventure/western/war";
						case 0x3 -> "science fiction/fantasy/horror";
						case 0x4 -> "comedy";
						case 0x5 -> "soap/melodrama/folkloric";
						case 0x6 -> "romance";
						case 0x7 -> "serious/classical/religious/historical movie/drama";
						case 0x8 -> "adult movie/drama";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x2: // News/Current affairs:
			yield switch (nibble2) {
						case 0x0 -> "news/current affairs (general)";
						case 0x1 -> "news/weather report";
						case 0x2 -> "news magazine";
						case 0x3 -> "documentary";
						case 0x4 -> "discussion/interview/debate";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x3: // Show/Game show:
			yield switch (nibble2) {
						case 0x0 -> "show/game show (general)";
						case 0x1 -> "game show/quiz/contest";
						case 0x2 -> "variety show";
						case 0x3 -> "talk show";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x4: // Sports:
			yield switch (nibble2) {
						case 0x0 -> "sports (general)";
						case 0x1 -> "special events (Olympic Games, World Cup, etc.)";
						case 0x2 -> "sports magazines";
						case 0x3 -> "football/soccer";
						case 0x4 -> "tennis/squash";
						case 0x5 -> "team sports (excluding football)";
						case 0x6 -> "athletics";
						case 0x7 -> "motor sport";
						case 0x8 -> "water sport";
						case 0x9 -> "winter sports";
						case 0xA -> "equestrian";
						case 0xB -> "martial sports";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x5: // Children's/Youth programmes:
			yield switch (nibble2) {
						case 0x0 -> "children's/youth programmes (general)";
						case 0x1 -> "pre-school children's programmes";
						case 0x2 -> "entertainment programmes for 6 to14";
						case 0x3 -> "entertainment programmes for 10 to 16";
						case 0x4 -> "informational/educational/school programmes";
						case 0x5 -> "cartoons/puppets";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x6: // Music/Ballet/Dance:
			yield switch (nibble2) {
						case 0x0 -> "music/ballet/dance (general)";
						case 0x1 -> "rock/pop";
						case 0x2 -> "serious music/classical music";
						case 0x3 -> "folk/traditional music";
						case 0x4 -> "jazz";
						case 0x5 -> "musical/opera";
						case 0x6 -> "ballet";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x7: // Arts/Culture (without music):
			yield switch (nibble2) {
						case 0x0 -> "arts/culture (without music, general)";
						case 0x1 -> "performing arts";
						case 0x2 -> "fine arts";
						case 0x3 -> "religion";
						case 0x4 -> "popular culture/traditional arts";
						case 0x5 -> "literature";
						case 0x6 -> "film/cinema";
						case 0x7 -> "experimental film/video";
						case 0x8 -> "broadcasting/press";
						case 0x9 -> "new media";
						case 0xA -> "arts/culture magazines";
						case 0xB -> "fashion";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x8: // Social/Political issues/Economics:
			yield switch (nibble2) {
						case 0x0 -> "social/political issues/economics (general)";
						case 0x1 -> "magazines/reports/documentary";
						case 0x2 -> "economics/social advisory";
						case 0x3 -> "remarkable people";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0x9: // Education/Science/Factual topics:
			yield switch (nibble2) {
						case 0x0 -> "education/science/factual topics (general)";
						case 0x1 -> "nature/animals/environment";
						case 0x2 -> "technology/natural sciences";
						case 0x3 -> "medicine/physiology/psychology";
						case 0x4 -> "foreign countries/expeditions";
						case 0x5 -> "social/spiritual sciences";
						case 0x6 -> "further education";
						case 0x7 -> "languages";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0xA: // Leisure hobbies:
			yield switch (nibble2) {
						case 0x0 -> "leisure hobbies (general)";
						case 0x1 -> "tourism/travel";
						case 0x2 -> "handicraft";
						case 0x3 -> "motoring";
						case 0x4 -> "fitness and health";
						case 0x5 -> "cooking";
						case 0x6 -> "advertisement/shopping";
						case 0x7 -> "gardening";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0xB: // Special characteristics:
			yield switch (nibble2) {
						case 0x0 -> "original language";
						case 0x1 -> "black and white";
						case 0x2 -> "unpublished";
						case 0x3 -> "live broadcast";
						case 0x4 -> "plano-stereoscopic";
						case 0x5 -> "local or regional";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0xC: // Adult:
			yield switch (nibble2) {
						case 0x0 -> "adult (general)";
						case 0xF -> "user defined";
						default -> "reserved for future use";
						};
		case 0xF:
			yield "";
		default:
			yield "reserved for future use";
		};
	}

	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		Utils.addListJTree(t,contentList,modus,"content_list");
		return t;
	}

	public List<ContentItem> getContentList() {
		return contentList;
	}
}
