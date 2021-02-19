package nl.digitalekabeltelevisie.data.mpeg.psi;

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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.util.LookUpList;
import nl.digitalekabeltelevisie.util.Utils;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

// based on EN 303 560 V1.1.1 (2018-05) 5.3.2.3.1 DFIT structure

public class DFITSection extends TableSectionExtendedSyntax {
	
	public static class FontInfo implements TreeNode{
		
		final LookUpList fontInfoTypeList = new LookUpList.Builder().
				add(0x00, "font_style_weight").
				add(0x01, "font file URI").
				add(0x02, "font_size").
				add(0x03, "font_family").
				add(0x04 , 0xFF, "reserved").
				build();

		final LookUpList fontStyleList = new LookUpList.Builder().
				add(0x00, "undefined").
				add(0x01, "normal").
				add(0x02, "italic").
				add(0x03, "oblique").
				add(0x04,0x07, "reserved for future use").
				build();

		final LookUpList fontWeightList = new LookUpList.Builder().
				add(0x00, "undefined").
				add(0x01, "normal").
				add(0x02, "bold").
				add(0x03,0x0F, "reserved for future use").
				build();
		
		final LookUpList fontFileFormatList = new LookUpList.Builder().
				add(0x00, "Open Font Format").
				add(0x01, "Web Open Font Format").
				add(0x02,0x0F, "reserved for future use").
				build();
		
		

		private int font_info_type;
		private int font_style;
		private int font_weight;
		private int reserved_zero_future_use;
		
		private int font_file_format;
		private int uri_length;
		private byte[] uri_char;
		
		private int font_size;
		
		int font_info_length;
		byte[] text_char;

		
		
		public FontInfo(int font_info_type) {
			this.font_info_type = font_info_type;
		}



		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Font Info"));
			t.add(new DefaultMutableTreeNode(new KVP("font_info_type",font_info_type,fontInfoTypeList.get(font_info_type))));
			
			
			if(font_info_type==0) {
				t.add(new DefaultMutableTreeNode(new KVP("font_style",font_style,fontStyleList.get(font_style))));
				t.add(new DefaultMutableTreeNode(new KVP("font_weight",font_weight,fontWeightList.get(font_weight))));
				t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use,null)));
			}else if(font_info_type==1) {
				t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use,null)));
				t.add(new DefaultMutableTreeNode(new KVP("font_file_format",font_file_format,fontFileFormatList.get(font_file_format))));
				t.add(new DefaultMutableTreeNode(new KVP("uri_length",uri_length,null)));
				t.add(new DefaultMutableTreeNode(new KVP("uri_char",uri_char,null)));
				
			}else if (font_info_type == 0x02) {
				t.add(new DefaultMutableTreeNode(new KVP("font_size",font_size,null)));
			}else { // if (font_info_type >= 0x02)  // p.31 ETSI EN 303 560 V1.1.1 (2018-05), should be >0x02
				t.add(new DefaultMutableTreeNode(new KVP("font_info_length",font_info_length,null)));
				t.add(new DefaultMutableTreeNode(new KVP("text_char",text_char,null)));
			}

			return t;
		}

		public int getFont_info_type() {
			return font_info_type;
		}

		public void setFont_info_type(int font_info_type) {
			this.font_info_type = font_info_type;
		}

		public int getFont_style() {
			return font_style;
		}

		public void setFont_style(int font_style) {
			this.font_style = font_style;
		}

		public int getFont_weight() {
			return font_weight;
		}

		public void setFont_weight(int font_weight) {
			this.font_weight = font_weight;
		}

		public int getReserved_zero_future_use() {
			return reserved_zero_future_use;
		}

		public void setReserved_zero_future_use(int reserved_zero_future_use) {
			this.reserved_zero_future_use = reserved_zero_future_use;
		}

		public int getFont_file_format() {
			return font_file_format;
		}

		public void setFont_file_format(int font_file_format) {
			this.font_file_format = font_file_format;
		}

		public int getUri_length() {
			return uri_length;
		}

		public void setUri_length(int uri_length) {
			this.uri_length = uri_length;
		}

		public byte[] getUri_char() {
			return uri_char;
		}

		public void setUri_char(byte[] uri_char) {
			this.uri_char = uri_char;
		}

		public int getFont_size() {
			return font_size;
		}

		public void setFont_size(int font_size) {
			this.font_size = font_size;
		}

		public int getFont_info_length() {
			return font_info_length;
		}

		public void setFont_info_length(int font_info_length) {
			this.font_info_length = font_info_length;
		}

		public byte[] getText_char() {
			return text_char;
		}

		public void setText_char(byte[] text_char) {
			this.text_char = text_char;
		}
		
	}

	private final int font_id_extension;
	private final int font_id;
	private final List<FontInfo> fontInfoList = new ArrayList<>();

	public DFITSection(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);

		byte[] sectionData = raw_data.getData();
		font_id_extension = Utils.getInt(sectionData, 3, 2, 0xFF80)>>7; // tableIdExtension first 9 bit
		font_id = Utils.getInt(sectionData, 4, 1, MASK_7BITS); // tableIdExtension rest

		
		int offset = 8;
		
		while(offset < (sectionLength -5 -4)){ // 5 preceding, 4 CRC
			int font_info_type = getInt(sectionData, offset++, 1, MASK_8BITS);
			FontInfo fi = new FontInfo(font_info_type);
			fontInfoList.add(fi);
			if(font_info_type==0) {
				int font_style = getInt(sectionData, offset, 1, 0b1110_0000)>>5;
				int font_weight = getInt(sectionData, offset, 1, 0b0001_1110)>>1;
				int reserved_zero_future_use = getInt(sectionData, offset++, 1, MASK_1BIT);
				
				fi.setFont_style(font_style);
				fi.setFont_weight(font_weight);
				fi.setReserved_zero_future_use(reserved_zero_future_use);
			}else if(font_info_type==1) {
				int reserved_zero_future_use = getInt(sectionData, offset, 1, 0b1111_0000)>>4;
				int font_file_format = getInt(sectionData, offset++, 1, MASK_4BITS);
				int uri_length = getInt(sectionData, offset++, 1, MASK_8BITS);
				byte[] uri_char = getBytes(sectionData, offset, uri_length);
				
				offset += uri_length;
				
				fi.setReserved_zero_future_use(reserved_zero_future_use);
				fi.setFont_file_format(font_file_format);
				fi.setUri_length(uri_length);
				fi.setUri_char(uri_char);
				
			}else if (font_info_type == 0x02) {
				int font_size= getInt(sectionData, offset, 2, MASK_16BITS);
				offset +=2;
				
				fi.setFont_size(font_size);
			}else { // if (font_info_type >= 0x02)  // p.31 ETSI EN 303 560 V1.1.1 (2018-05), should be >0x02
				int font_info_length = getInt(sectionData, offset++, 1, MASK_8BITS);
				byte[] text_char = getBytes(sectionData, offset, font_info_length);
				
				offset += font_info_length;
				
				fi.setFont_info_length(font_info_length);
				fi.setText_char(text_char);
			}
		}
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "font_id_extension (9) / font_id (7)";
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("font_id_extension", font_id_extension, null)));
		t.add(new DefaultMutableTreeNode(new KVP("font_id", font_id, null)));
		
		addListJTree(t,fontInfoList,modus,"Font Info List");
		return t;
	}

	public int getFont_id_extension() {
		return font_id_extension;
	}

	public int getFont_id() {
		return font_id;
	}

}
