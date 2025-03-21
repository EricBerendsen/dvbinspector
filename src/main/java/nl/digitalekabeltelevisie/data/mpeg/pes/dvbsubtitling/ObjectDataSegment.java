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

package nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.util.BitSource;


/**
 * @author Eric Berendsen
 *
 */
public class ObjectDataSegment extends Segment implements ImageSource {

	private static final Logger	logger	= Logger.getLogger(ObjectDataSegment.class.getName());


	// for coding of pixels
	private final List<PixelDataSubBlock> topFieldDataBlocks = new ArrayList<>();
	private List<PixelDataSubBlock> bottomFieldDataBlocks = new ArrayList<>();

	// For coded as a string of characters
	private int number_of_codes;
	private String character_code_string;


	private static byte [] default_2_to_4_bit_map_table = {0x0,0x7,0x8,0xf};
	private static byte [] default_2_to_8_bit_map_table = {0x00,0x77,-0x78,-0x01};
	private static byte [] default_4_to_8_bit_map_table = {0x00,0x11,0x22,0x33,
														   0x44,0x55,0x66,0x77,
														  -0x78,-0x67,-0x56,0x45,
														  -0x34,-0x23,-0x12,-0x01
														  };


	public static class PixelDataSubBlock implements TreeNode{

		protected byte[] data_block;

		protected int offset;

		private final int dataType;

		protected byte[] pixels = new byte[720];
		protected int no_pixels;


		private byte [] table_2_to_4_bit_map_table = null;
		private byte [] table_2_to_8_bit_map_table = null;
		private byte [] table_4_to_8_bit_map_table = null;

		public PixelDataSubBlock(byte[] data, int offset) {
			this.data_block = data;
			this.offset = offset;
			dataType = getInt(data_block,offset, 1, MASK_8BITS);
		}

		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("pixel-data_sub-block "+getDataTypeString(dataType));
			s.add(new KVP("data_type",dataType).setDescription(getDataTypeString(dataType)));
			if((dataType >=0x10 )&&(dataType <= 0x12)){ // pixels
				s.add(new KVP("no_pixels",no_pixels));
				s.add(new KVP("pixels",pixels,0,no_pixels));
			}else if(dataType ==0x20 ){ // 2_to_4_bit_map_table data
				if(table_2_to_4_bit_map_table!=null){
					for (int i = 0; i < table_2_to_4_bit_map_table.length; i++) {
						s.add(new KVP("entry ["+i+"]",table_2_to_4_bit_map_table[i]));
					}
				}
			}else if(dataType ==0x21 ){ // 2_to_8-bit_map-table data data
				if(table_2_to_8_bit_map_table!=null){
					for (int i = 0; i < table_2_to_8_bit_map_table.length; i++) {
						s.add(new KVP("entry ["+i+"]",table_2_to_8_bit_map_table[i]));
					}
				}
			}else if((dataType ==0x22)&& (table_4_to_8_bit_map_table!=null)){// 4_to_8-bit_map-table data
				for (int i = 0; i < table_4_to_8_bit_map_table.length; i++) {
					s.add(new KVP("entry ["+i+"]",table_4_to_8_bit_map_table[i]));
				}
			}

			return s;
		}


		public void addPixel(byte p){
			if(no_pixels>= pixels.length){
				pixels = Arrays.copyOf(pixels, no_pixels *2);
			}
			pixels[no_pixels++] = p;
		}

		public void addIdenticalPixel(byte pixel, int no){
			if(pixels==null){
				pixels = new byte[720];
			}
			/*
			 * n always <= 284, so ass longs initial size pixels > 284 this will work
			 * otherwise need to increase size with at least n bytes
			 */
			if((no+no_pixels)>= pixels.length){
				pixels = Arrays.copyOf(pixels, no_pixels *2);
			}
			Arrays.fill(pixels,no_pixels,no_pixels+no,pixel);
			no_pixels += no;
		}




		public int getDataType() {
			return dataType;
		}


		public int getNo_pixels() {
			return no_pixels;
		}

		public byte[] getPixels() {
			return pixels;
		}


		public byte[] getTable_2_to_4_bit_map_table() {
			return table_2_to_4_bit_map_table;
		}

		public void setTable_2_to_4_bit_map_table(byte[] table_2_to_4_bit_map_table) {
			this.table_2_to_4_bit_map_table = table_2_to_4_bit_map_table;
		}

		public byte[] getTable_2_to_8_bit_map_table() {
			return table_2_to_8_bit_map_table;
		}

		public void setTable_2_to_8_bit_map_table(byte[] table_2_to_8_bit_map_table) {
			this.table_2_to_8_bit_map_table = table_2_to_8_bit_map_table;
		}

		public byte[] getTable_4_to_8_bit_map_table() {
			return table_4_to_8_bit_map_table;
		}

		public void setTable_4_to_8_bit_map_table(byte[] table_4_to_8_bit_map_table) {
			this.table_4_to_8_bit_map_table = table_4_to_8_bit_map_table;
		}
	}


	public ObjectDataSegment(byte[] data, int offset) {
		super(data, offset);

		if(getObjectCodingMethod()==0){ // coding of pixels
			int processed_length = 0;
			processed_length = readFieldDataBlock(data, offset+13, getTopFieldDataBlockLength(), topFieldDataBlocks);
			if(getBottomFieldDataBlockLength()!=0){
				processed_length = readFieldDataBlock(data, offset+13+processed_length, getBottomFieldDataBlockLength(),bottomFieldDataBlocks);
			}else{
				bottomFieldDataBlocks = topFieldDataBlocks;
			}


		}else if(getObjectCodingMethod()==1){ // coded as a string of characters
			number_of_codes = getInt(data,offset+9, 1, MASK_8BITS);
			int[] text = new int[number_of_codes];
			int txtLen=0;
			for(int i = 0; i < number_of_codes; i ++){
				int character_code = (int)getLong(data,offset+10+(i*2), 2, MASK_16BITS);
				if(character_code>=32){ // skip unprintable chars (ugly!, why needed?)
					text[txtLen++]=character_code;
				}
			}
			// Specifies a character through its index number in the character table identified in the subtitle_descriptor
			// (7.2.5 Object data segment, ETSI EN 300 743 V1.3.1 (2006-11)
			// subtitling descriptor specifies only a language, not a character table
			// ISO language may also contain something else than language...
			// The private agreements required to enable these features are beyond the scope of ETSI EN 300 743 V1.3.1 (2006-11) (p.12)

			// also, first character seems to be 0x05, is this the DVB Selection of character table as in Annex A.2 of EN 300 468?
			character_code_string = new String(text,0,txtLen);
		}
	}


	private int readFieldDataBlock(byte[] data, int offset, int fieldDataBlockLength, List<PixelDataSubBlock> pixelDataSubBlockList) {
		int processed_length = 0;
		while((processed_length < fieldDataBlockLength)&&isMoreDataAvaliable(offset, processed_length)){
			int blockStart = offset + processed_length;
			int dataType = getInt(data_block,blockStart, 1, MASK_8BITS);
			PixelDataSubBlock b = new PixelDataSubBlock(data, blockStart);
			BitSource bs = new BitSource(data, blockStart+1);

			switch (dataType) {
			case 0x10: // 2-bit/pixel code string
				two_bit_pixel_code_string(bs,b);
				break;
			case 0x11: // 4-bit/pixel code string
				four_bit_pixel_code_string(bs,b);
				break;
			case 0x12: // 8-bit/pixel code string
				eigth_bit_pixel_code_string(bs,b);
				break;
			case 0x20: // 2_to_4-bit_map-table data
				two_to_4_bit_map_table(bs,b);
				break;
			case 0x21: // 2_to_8-bit_map-table data
				two_to_8_bit_map_table(bs, b);
				break;
			case 0x22: // 4_to_8-bit_map-table data
				four_to_8_bit_map_table(bs, b);
				break;
			case 0xf0: // end of object line code
				break;
			default:
				break;
			}
			processed_length += bs.getNextFullByteOffset() - blockStart;
			pixelDataSubBlockList.add(b);
		}
		return processed_length;
	}

	private boolean isMoreDataAvaliable(int offset, int processed_length) {
		boolean moreDataAvailable = (offset + processed_length)<data_block.length;
		if(!moreDataAvailable){
			logger.warning("less data available than expected; (offset:"+offset+"+"+"processed_length:"+processed_length+")>=data_block.length:"+data_block.length);
		}
		return moreDataAvailable;
	}


	private static void two_to_4_bit_map_table(BitSource bs, PixelDataSubBlock b) {
		byte [] table = new byte[4];
		for (int i = 0; i < 4; i++) {
			table[i]=(byte)bs.readBits(4);
		}
		b.setTable_2_to_4_bit_map_table(table);
	}


	private static void two_to_8_bit_map_table(BitSource bs, PixelDataSubBlock b) {
		byte [] table = new byte[4];
		for (int i = 0; i < 4; i++) {
			table[i]=bs.readSignedByte(8);
		}
		b.setTable_2_to_8_bit_map_table(table);
	}

	/**
	 * @param bs
	 * @param b
	 */
	private static void four_to_8_bit_map_table(BitSource bs, PixelDataSubBlock b) {
		byte [] table = new byte[16];
		for (int i = 0; i < 4; i++) {
			table[i]=bs.readSignedByte(8);
		}
		b.setTable_4_to_8_bit_map_table(table);
	}


	/**
	 * @param bs
	 * @param b
	 */
	private static void two_bit_pixel_code_string(BitSource bs, PixelDataSubBlock b) {

		boolean readMore = true;
		do{
			int bits = bs.readBits(2);
			if (bits != 0) {
				b.addPixel((byte)bits);
			} else {
				int switch_1= bs.readBits(1); //switch_1 1 bslbf

				if (switch_1 == 1) {
					int run_length_3_10 = bs.readBits(3);
					int two_bit_pixel_code = bs.readBits(2);
					b.addIdenticalPixel((byte)two_bit_pixel_code,run_length_3_10+3);
				} else {
					int switch_2 = bs.readBits(1); // switch_21 bslbf
					if (switch_2 == 0) {
						int switch_3 = bs.readBits(2); //switch_3 2 bslbf
						if(switch_3 == 2){ // (switch_3 == '10') {
							int run_length_12_27 = bs.readBits(4);
							int two_bit_pixel_code = bs.readBits(2);
							b.addIdenticalPixel((byte)two_bit_pixel_code,run_length_12_27+12);
						}
						if (switch_3 == 3) {//switch_3 == '11'
							int run_length_29_284 = bs.readBits(8);// 8 uimsbf
							int two_bit_pixel_code = bs.readBits(2);
							b.addIdenticalPixel((byte)two_bit_pixel_code,run_length_29_284+29);
						}
						if(switch_3 == 0 ){ // not in spec, but we have to stop somewhere, see 11 Structure of the pixel code strings (informative) ETSI EN 300 743 V1.3.1 (2006-11)
							readMore = false;
						}
						if(switch_3 == 1 ){
							b.addIdenticalPixel((byte)0,2); // two pixels in colour 0 , see 11 Structure of the pixel code strings (informative) ETSI EN 300 743 V1.3.1 (2006-11)
						}
					}else{ //switch_2 == 1
						b.addPixel((byte)0); // one pixel in colour 0, see 11 Structure of the pixel code strings (informative) ETSI EN 300 743 V1.3.1 (2006-11)
					}
				}
			}
		}while(readMore);
	}

	/**
	 * @param bs
	 * @param b
	 */
	private static void four_bit_pixel_code_string(BitSource bs, PixelDataSubBlock b) {

		boolean readMore = true;
		do {
			if (bs.available() < 4) {
				readMore = false;
				logger.warning("bits available: "+bs.available());
			} else {
				int bits = bs.readBits(4);
				if (bits != 0) { //if (nextbits() != '0000') {
					b.addPixel((byte) bits); // 4-bit_pixel-code 4 bslbf
				} else {
					// 4-bit_zero 4 bslbf
					int switch_1 = bs.readBits(1);// switch_1 1 bslbf
					if (switch_1 == 0) { // if (switch_1 == '0') {
						int nextbits = bs.readBits(3);//if (nextbits() != '000')
						if (nextbits != 000) {
							int run_length_3_9 = nextbits;
							b.addIdenticalPixel((byte) 0, run_length_3_9 + 2);
						} else {
							readMore = false; //end_of_string_signal 3 bslbf
						}
					} else {
						int switch_2 = bs.readBits(1); //switch_2 1 bslbf
						if (switch_2 == 0) {
							int run_length_4_7 = bs.readBits(2);//  2 bslbf
							int pixel_code = bs.readBits(4);// 4 bslbf
							b.addIdenticalPixel((byte) pixel_code, run_length_4_7 + 4);
						} else {
							int switch_3 = bs.readBits(2);//switch_3 2 bslbf
							if (switch_3 == 0) { //if (switch_3 == '0') {
								b.addPixel((byte) 0);
							}
							if (switch_3 == 1) { //if (switch_3 == '01') {
								b.addIdenticalPixel((byte) 0, 2);
							}
							if (switch_3 == 2) { //if (switch_3 == '10') {
								int run_length_9_24 = bs.readBits(4);//4 uimsbf
								int pixel_code = bs.readBits(4);// 4 bslbf
								b.addIdenticalPixel((byte) pixel_code, run_length_9_24 + 9);
							}
							if (switch_3 == 3) {//	if (switch_3 == '11') {
								int run_length_25_280 = bs.readBits(8);//8 uimsbf
								int pixel_code = bs.readBits(4);// 4 bslbf
								b.addIdenticalPixel((byte) pixel_code, run_length_25_280 + 25);
							}
						}
					}
				}
			}
		} while (readMore);
	}

	/**
	 * @param bs
	 * @param b
	 */
	private static void eigth_bit_pixel_code_string(BitSource bs, PixelDataSubBlock b) {

		boolean readMore = true;
		do{
			int bits = bs.readBits(8);
			if (bits != 0) { //if (nextbits() != '0000') {
				b.addPixel(getInt2UnsignedByte(bits)); // 8-bit_pixel-code 8 bslbf
			} else {
				// 8-bit_zero 4 bslbf
				int switch_1 = bs.readBits(1);// switch_1 1 bslbf
				if (switch_1 == 0) { // if (switch_1 == '0') {
					int nextbits = bs.readBits(7);//if (nextbits() != '0000 0000') {
					if(nextbits !=0){
						int run_length_1_127 = nextbits;
						b.addIdenticalPixel((byte)0,run_length_1_127);
					} else{
						readMore = false; //end_of_string_signal 3 bslbf
					}
				}else{
					int run_length_3_127= bs.readBits(7);//  2 bslbf
					int pixel_code = bs.readBits(8) ;// 4 bslbf
					b.addIdenticalPixel(getInt2UnsignedByte(pixel_code),run_length_3_127);
				}
			}
		}while(readMore);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.Segment#getJTreeNode(int)
	 */
	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = super.getJTreeNode(modus).addImageSource(this, "Object Data Segment");
		s.add(new KVP("object_id", getObjectId()));
		s.add(new KVP("object_version_number", getObjectVersionNumber()));
		s.add(new KVP("object_coding_method", getObjectCodingMethod()).setDescription(getObjectCodingMethodString(getObjectCodingMethod())));
		s.add(new KVP("non_modifying_colour_flag", getNonModifyingColourFlag()));
		if(getObjectCodingMethod()==0){
			s.add(new KVP("top_field_data_block_length", getTopFieldDataBlockLength()));
			s.add(new KVP("bottom_field_data_block_length", getBottomFieldDataBlockLength()));
			addListJTree(s, topFieldDataBlocks,modus,"top field pixel-data_sub-block");
			if(getBottomFieldDataBlockLength()!=0){
				addListJTree(s, bottomFieldDataBlocks,modus,"bottom field pixel-data_sub-block");
			}

		}else if(getObjectCodingMethod()==1){
			s.add(new KVP("number_of_codes", number_of_codes));
			s.add(new KVP("character_codes", character_code_string));

		}


		return s;
	}

	/**
	 * @return
	 */
	private int getTopFieldDataBlockLength() {
		return getInt(data_block, offset + 9, 2, MASK_16BITS);
	}

	/**
	 * @return
	 */
	private int getBottomFieldDataBlockLength() {
		return getInt(data_block, offset + 11, 2, MASK_16BITS);
	}

	/**
	 * @return
	 */
	public int getObjectCodingMethod() {
		return getInt(data_block, offset + 8, 1, 0x0C) >> 2;
	}


	/**
	 * @return
	 */
	public int getNonModifyingColourFlag() {
		return getInt(data_block, offset + 8, 1, 0x02) >> 1;
	}



	/**
	 * @return
	 */
	public int getObjectVersionNumber() {
		return getInt(data_block, offset + 8, 1, 0xF0) >> 4;
	}


	/**
	 * @return
	 */
	public int getObjectId() {
		return getInt(data_block, offset + 6, 2, MASK_16BITS);
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getObjectCodingMethodString(int type) {

        return switch (type) {
            case 0x0 -> "coding of pixels";
            case 0x1 -> "coded as a string of characters";
            case 0x2 -> "progressive coding of pixels";
            case 0x3 -> "reserved";
            default -> "Illegal value";
        };
	}


	/**
	 * @param type
	 * @return
	 */
	public static String getDataTypeString(int type) {

        return switch (type) {
            case 0x10 -> "2-bit/pixel code string";
            case 0x11 -> "4-bit/pixel code string";
            case 0x12 -> "8-bit/pixel code string";
            case 0x20 -> "2_to_4-bit_map-table data";
            case 0x21 -> "2_to_8-bit_map-table data";
            case 0x22 -> "4_to_8-bit_map-table data";
            case 0xF0 -> "end of object line code";
            default -> "reserved";
        };
	}

	public BufferedImage getImage() {

		// check for objectCodingMethod characters (like in 07-20_CINE SKY (por)_Um Espírito Atrás de Mim_01.ts, PID 1036, segment 19

		if(getObjectCodingMethod()==0){
			BufferedImage bi=null;
			WritableRaster wr = getRaster(2);
			if(wr==null){
				return null;
			}

			IndexColorModel cm =  CLUTDefinitionSegment.getDefault_CLUT_8bitColorModel();

			bi = new BufferedImage(cm, wr, false, null);
			return bi;
		}
		return null;
	}


	public List<PixelDataSubBlock> getTopFieldDataBlocks() {
		return topFieldDataBlocks;
	}

	public List<PixelDataSubBlock> getBottomFieldDataBlocks() {
		return bottomFieldDataBlocks;
	}

	public static Logger getLogger() {
		return logger;
	}

	public int getNumber_of_codes() {
		return number_of_codes;
	}

	public String getCharacter_code_string() {
		return character_code_string;
	}

	/**
	 * improved version of getRaster(), taking into account bit depth of region containing this ObjectDataSegment
	 * @param regionDepth 1=2 bits, 2=4 bits, 3= 8 bits
	 * @return
	 */
	public WritableRaster getRaster(int regionDepth) {
		if(getObjectCodingMethod()!=0){ // only for bitmaps
			return null;
		}

		// right edge does not have to be straight. so we take the longest line.
		int width=-1;
		int height=-1;

		int topLines=0;
		int bottomLines=0;

		// first count lines (heigth) and width.
		int linewidth = 0;
		for(PixelDataSubBlock block: topFieldDataBlocks){
			int dataType = block.getDataType();
			if((dataType>=0x10)&&(dataType<=0x12)){
				linewidth += block.getNo_pixels();
			}else if(dataType==0xF0){ // end of object line code
				topLines++;
				if(linewidth>width){
					width=linewidth;
				}
				linewidth = 0;
			}
		}


		for(PixelDataSubBlock block: bottomFieldDataBlocks){
			int dataType = block.getDataType();
			if((dataType>=0x10)&&(dataType<=0x12)){
				linewidth += block.getNo_pixels();
			}else if(dataType==0xF0){ // end of object line code
				bottomLines++;
				if(linewidth>width){
					width=linewidth;
				}
				linewidth = 0;
			}
		}

		if((topLines!=bottomLines)&&(topLines!=(bottomLines+1))){
			logger.log (Level.WARNING,"topLines "+topLines+" not matching bottomLines "+bottomLines);
			return null;
		}

		height = topLines + bottomLines;

		byte[] dataBuffer = new byte[height * width];

		int line=0;
		int linepos = 0;
		// start with default tables
		byte [] two_to_4_bit_map_table = default_2_to_4_bit_map_table ;
		byte [] two_to_8_bit_map_table = default_2_to_8_bit_map_table ;
		byte [] four_to_8_bit_map_table = default_4_to_8_bit_map_table;

		for(PixelDataSubBlock block: topFieldDataBlocks){
			int dataType = block.getDataType();
			if((dataType>=0x10)&&(dataType<=0x12)){
				if((dataType-15)>=regionDepth){ // ugly hack, means depth of block > requested depth, so no need to remap
					System.arraycopy(block.getPixels(), 0, dataBuffer, (2*line*width)+linepos, block.getNo_pixels());
				}else{ // remap
					byte[] remappedPix = mapTable(regionDepth,
							two_to_4_bit_map_table, two_to_8_bit_map_table,
							four_to_8_bit_map_table, block, dataType);
					System.arraycopy(remappedPix, 0, dataBuffer, (2*line*width)+linepos, block.getNo_pixels());
				}

				linepos += block.getNo_pixels();
			}else if(dataType==0x20){
				two_to_4_bit_map_table = block.getTable_2_to_4_bit_map_table();
			}else if(dataType==0x21){
				two_to_8_bit_map_table = block.getTable_2_to_8_bit_map_table();
			}else if(dataType<=0x22){ // bit_map-table
				four_to_8_bit_map_table = block.getTable_4_to_8_bit_map_table();
			}else if(dataType==0xF0){ // end of object line code
				line++;
				linepos=0;
			}
		}

		line=0;
		linepos = 0;
		// start with default tables
		two_to_4_bit_map_table = default_2_to_4_bit_map_table ;
		two_to_8_bit_map_table = default_2_to_8_bit_map_table ;
		four_to_8_bit_map_table = default_4_to_8_bit_map_table;

		for(PixelDataSubBlock block: bottomFieldDataBlocks){
			int dataType = block.getDataType();
			if((dataType>=0x10)&&(dataType<=0x12)){
				if((dataType-15)>=regionDepth){ // ugly hack, means depth of block > requested depth, so no need to remap
					System.arraycopy(block.getPixels(), 0, dataBuffer, ((1+(2*line))*width)+linepos, block.getNo_pixels());
				}else{ // remap
					byte[] remappedPix = mapTable(regionDepth,
							two_to_4_bit_map_table, two_to_8_bit_map_table,
							four_to_8_bit_map_table, block, dataType);
					System.arraycopy(remappedPix, 0, dataBuffer, ((1+(2*line))*width)+linepos, block.getNo_pixels());
				}

				linepos += block.getNo_pixels();
			}else if(dataType==0x20){
				two_to_4_bit_map_table = block.getTable_2_to_4_bit_map_table();
			}else if(dataType==0x21){
				two_to_8_bit_map_table = block.getTable_2_to_8_bit_map_table();
			}else if(dataType<=0x22){ // bit_map-table
				four_to_8_bit_map_table = block.getTable_4_to_8_bit_map_table();
			}else if(dataType==0xF0){ // end of object line code
				line++;
				linepos=0;
			}
		}

		DataBuffer dBuffer = new DataBufferByte(dataBuffer, width * height);
		return Raster.createInterleavedRaster(dBuffer,width,height,width,1,new int[]{0},null);
	}

	private static byte[] mapTable(int regionDepth, byte[] two_to_4_bit_map_table,
                                   byte[] two_to_8_bit_map_table, byte[] four_to_8_bit_map_table,
                                   PixelDataSubBlock block, int dataType) {
		byte [] useMap;
		if(dataType==0x10){ // two bits PixelDataSubBlock
			if(regionDepth==2 ){ // four bits
				useMap = two_to_4_bit_map_table;
			}else{ // 8 bits
				useMap = two_to_8_bit_map_table;
			}
		}else{ // 0x11, 4 bits PixelDataSubBlock
			useMap = four_to_8_bit_map_table;
		}
		byte[] orgPix = block.getPixels();
		byte[] remappedPix = new byte[block.getNo_pixels()];
		for (int i = 0; i < block.getNo_pixels(); i++) {
			remappedPix[i]=useMap[orgPix[i]];
		}
		return remappedPix;
	}


}