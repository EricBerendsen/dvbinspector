/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;

@SuppressWarnings("ALL")
public class TtmlSubtitlingDescriptor extends DVBExtensionDescriptor {

    public record Font(int font_id) implements TreeNode {

        public Font(int font_id) {
            this.font_id = font_id & 0b0111_1111;
        }

        @Override
        public KVP getJTreeNode(int modus) {
            return new KVP("font_id", font_id);
        }

    }

	public static class Qualifier implements TreeNode{
		
		final LookUpList size_lookup_list = new LookUpList.Builder().
				add(0x0 ,"broadcaster's default size").
				add(0x1 ,"small").
				add(0x2 ,"medium").
				add(0x3 ,"large").
				add(0x4 ,0xF ,"reserved for future use").
				build();

		final LookUpList cadence_lookup_list = new LookUpList.Builder().
				add(0x0, "broadcaster's default cadence").
				add(0x1, "slow").
				add(0x2, "medium").
				add(0x3, "fast").
				add(0x4, 0xF,"reserved for future use").
				build();

		final LookUpList position_lookup_list = new LookUpList.Builder().
				add(0x0, "default").
				add(0x1, "top").
				add(0x2, "bottom").
				add(0x3, "speaker").
				add(0x4, "dynamic").
				add(0x5, 0xF,"reserved for future use").
				build();
		
		public Qualifier(long qualifier) {
            this.qualifier = qualifier;
			size = (int) ((qualifier & 0xF0000000L)>>28);
			cadence = (int) ((qualifier & 0x0F000000L)>>24);
			monochrome_flag = (int) ((qualifier & 0x00800000L)>>23);
			enhanced_accessibility_contrast_flag = (int) ((qualifier & 0x00400000L)>>22);
			position = (int) ((qualifier & 0x003B0000L)>>18);
			reserved_zero_future_use = (int) ((qualifier & 0x0003FFFFL)>>18);
		}

		private final long qualifier;
		
		private final int size;
		private final int cadence;
		private final int monochrome_flag ;
		private final int enhanced_accessibility_contrast_flag ;
		private final int position;
		private final int reserved_zero_future_use;

		@Override
		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("qualifier",qualifier);
			t.add(new KVP("size", size).setDescription(size_lookup_list.get(size)));
			t.add(new KVP("cadence", cadence).setDescription(cadence_lookup_list.get(cadence)));
			t.add(new KVP("monochrome_flag", monochrome_flag));
			t.add(new KVP("enhanced_accessibility_contrast_flag", enhanced_accessibility_contrast_flag));
			t.add(new KVP("position", position).setDescription(position_lookup_list.get(position)));
			t.add(new KVP("reserved_zero_future_use", reserved_zero_future_use));
			return t;
		}
		
	}
	
	public static class DvbTtmlProfile implements TreeNode{
		
		private final LookUpList dvb_ttml_profile_lookup_list = new LookUpList.Builder().
				add(0x00, "etd1†|im1t").
				add(0x01, "im1t").
				add(0x02, "etd1").
				add(0x03, 0xFF, "reserved for future use").
				build();
				
				
		public DvbTtmlProfile(int dvb_ttml_profile) {
            this.dvb_ttml_profile = dvb_ttml_profile;
		}

		private final int dvb_ttml_profile;

		@Override
		public KVP getJTreeNode(int modus) {
			return new KVP("dvb_ttml_profile",dvb_ttml_profile).setDescription(dvb_ttml_profile_lookup_list.get(dvb_ttml_profile));
		}
	
	}

	private final String iso639LanguageCode;
	private final int subtitle_purpose;
	private final int tts_suitability;
	private final int essential_font_usage_flag;
	private final int qualifier_present_flag;
	private final int reserved_zero_future_use;
	private final int dvb_ttml_profile_count;
	private final List<DvbTtmlProfile> profileList = new ArrayList<>();

	private long qualifier;
	
	private int font_count;
	private final List<Font> fontList = new ArrayList<>();
	
	
	private final DVBString text;


	private final LookUpList subtitle_purpose_list = new LookUpList.Builder().
			add(0x00, "same-lang-dialogue").
			add(0x01, "other-lang-dialogue").
			add(0x02, "all-dialogue").
			add(0x03, 0x0F ,"reserved for future use").
			add(0x10, "hard-of-hearing").
			add(0x11, "other-lang-dialoguewith-hard-of-hearing").
			add(0x12, "all-dialogue-with-hardof-hearing").
			add(0x13, 0x2F ,"reserved for future use").
			add(0x30, "audio-description").
			add(0x31 ,"content-relatedcommentary").
			add(0x32 ,0x3F, "reserved for future use ").
			build();
					
	private final LookUpList tts_suitability_list  = new LookUpList.Builder().
			add(0x0 ,"unknown suitability for TTS").
			add(0x1 ,"suitable for TTS").
			add(0x2 ,"not suitable for TTS").
			add(0x3 ,"reserved for future use").
			build(); 

	public TtmlSubtitlingDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int localOffset = PRIVATE_DATA_OFFSET;
		iso639LanguageCode = getISO8859_1String(b, localOffset, 3);
		localOffset +=3;
		subtitle_purpose = getInt(b, localOffset, 1, 0b1111_1100)>>2;
		tts_suitability = getInt(b, localOffset++, 1, MASK_2BITS);
		essential_font_usage_flag  = getInt(b, localOffset, 1, 0b1000_0000)>>7;
		qualifier_present_flag  = getInt(b, localOffset, 1, 0b0100_0000)>>6;
		reserved_zero_future_use = getInt(b, localOffset, 1, 0b0011_0000)>>4;
		dvb_ttml_profile_count = getInt(b, localOffset++, 1, MASK_4BITS);
		for (int i = 0; i < dvb_ttml_profile_count; i++) {
			DvbTtmlProfile profile = new DvbTtmlProfile(getInt(b, localOffset++, 1, MASK_8BITS));
			profileList.add(profile);
		}
		if(qualifier_present_flag == 1) {
			qualifier = getLong(b, localOffset, 4, MASK_32BITS);
			localOffset += 4;
		}
		if (essential_font_usage_flag == 1){
			 font_count = getInt(b, localOffset++, 1, MASK_8BITS);
			 for(int i=0; i<font_count; i++){
				 int f  = getInt(b, localOffset++, 1, MASK_8BITS);
				 Font font = new Font(f);
				 fontList.add(font);
			 }
		}
		
		text = new DVBString(b, localOffset);
		
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("iso639LanguageCode", iso639LanguageCode));
		t.add(new KVP("subtitle_purpose", subtitle_purpose).setDescription(subtitle_purpose_list.get(subtitle_purpose)));
		t.add(new KVP("TTS_suitability", tts_suitability).setDescription(tts_suitability_list.get(tts_suitability)));
		t.add(new KVP("essential_font_usage_flag", essential_font_usage_flag));
		t.add(new KVP("qualifier_present_flag", qualifier_present_flag));
		t.add(new KVP("reserved_zero_future_use", reserved_zero_future_use));
		t.add(new KVP("dvb_ttml_profile_count", dvb_ttml_profile_count));
		addListJTree(t,profileList,modus,"dvb_ttml_profile_list");
		
		if(qualifier_present_flag == 1) {
			t.add(new Qualifier(qualifier).getJTreeNode(modus));
		}
		
		if (essential_font_usage_flag == 1){
			t.add(new KVP("font_count", font_count));
			addListJTree(t,fontList,modus,"font_list");
		}

		t.add(new KVP("text",text));
		
		

		return t;
	}

}
