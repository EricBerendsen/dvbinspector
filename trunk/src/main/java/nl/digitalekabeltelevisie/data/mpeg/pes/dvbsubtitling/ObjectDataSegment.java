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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 */
public class ObjectDataSegment extends Segment implements TreeNode, ImageSource {

	private final List<PixelDataSubBlock> topFieldDataBlocks = new ArrayList<PixelDataSubBlock>();
	private final List<PixelDataSubBlock> bottomFieldDataBlocks = new ArrayList<PixelDataSubBlock>();
	private static Logger	logger	= Logger.getLogger(ObjectDataSegment.class.getName());


	/**
	 *
	 */
	public class PixelDataSubBlock implements TreeNode{

		protected byte[] data_block;

		protected int offset;

		private final int dataType;

		protected byte[] pixels;
		protected int no_pixels;

		public PixelDataSubBlock(final byte[] data, final int offset) {
			this.data_block = data;
			this.offset = offset;
			dataType = getInt(data_block,offset, 1, MASK_8BITS);
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("pixel-data_sub-block "+getDataTypeString(dataType)));
			s.add(new DefaultMutableTreeNode(new KVP("data_type",dataType,getDataTypeString(dataType))));
			if((dataType >-0x10 )&&(dataType <= 0x12)){ // pixels
				s.add(new DefaultMutableTreeNode(new KVP("no_pixels",no_pixels,null)));
				s.add(new DefaultMutableTreeNode(new KVP("pixels",pixels,0,no_pixels,null)));
			}

			return s;
		}

		public void addPixel(final byte p){
			if(pixels==null){
				pixels = new byte[720];
			}
			if(no_pixels>= pixels.length){
				pixels = Arrays.copyOf(pixels, no_pixels *2);
			}
			pixels[no_pixels++] = p;
		}

		public void addPixel(final byte p, final int n){
			if(pixels==null){
				pixels = new byte[720];
			}
			/*
			 * n always <= 284, so ass longs initial size pixels > 284 this will work
			 * otherwise need to increase size with at least n bytes
			 */
			if(no_pixels>= pixels.length){
				pixels = Arrays.copyOf(pixels, no_pixels *2);
			}
			Arrays.fill(pixels,no_pixels,no_pixels+n,p);
			no_pixels += n;
		}



		/**
		 * @return the dataType
		 */
		public int getDataType() {
			return dataType;
		}


		/**
		 * @return the no_pixels
		 */
		public int getNo_pixels() {
			return no_pixels;
		}


		/**
		 * @return the pixels
		 */
		public byte[] getPixels() {
			return pixels;
		}

	}

	/**
	 * @param data
	 * @param offset
	 */
	public ObjectDataSegment(final byte[] data, final int offset) {
		super(data, offset);

		if(getObjectCodingMethod()==0){
			int processed_length = 0;
			processed_length = readFieldDataBlock(data, offset+13, getTopFieldDataBlockLength(), topFieldDataBlocks);
			processed_length = readFieldDataBlock(data, offset+13+processed_length, getBottomFieldDataBlockLength(),bottomFieldDataBlocks);


		}
	}

	/**
	 * @param data
	 * @param offset
	 * @param fieldDataBlockLength
	 * @param processed_length
	 * @return
	 */
	private int readFieldDataBlock(final byte[] data, final int offset, final int fieldDataBlockLength, final List<PixelDataSubBlock> pixelDataSubBlockList) {
		int processed_length = 0;
		while(processed_length < fieldDataBlockLength  ){
			final int blockStart = offset + processed_length;
			final int dataType = getInt(data_block,blockStart, 1, MASK_8BITS);
			final PixelDataSubBlock b = new PixelDataSubBlock(data, blockStart);
			final BitSource bs = new BitSource(data, blockStart+1);

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

	/**
	 * @param bs
	 * @param b
	 */
	private static void two_to_4_bit_map_table(final BitSource bs, final PixelDataSubBlock b) {
		for (int i = 0; i < 4; i++) {
			final int bits = bs.readBits(4);
			b.addPixel(Utils.getInt2UnsignedByte(bits));
		}
	}

	/**
	 * @param bs
	 * @param b
	 */
	private static void two_to_8_bit_map_table(final BitSource bs, final PixelDataSubBlock b) {
		for (int i = 0; i < 4; i++) {
			final int bits = bs.readBits(8);
			b.addPixel(Utils.getInt2UnsignedByte(bits));
		}
	}

	/**
	 * @param bs
	 * @param b
	 */
	private static void four_to_8_bit_map_table(final BitSource bs, final PixelDataSubBlock b) {
		for (int i = 0; i < 16; i++) {
			final int bits = bs.readBits(8);
			b.addPixel(Utils.getInt2UnsignedByte(bits));
		}
	}


	/**
	 * @param bs
	 * @param b
	 */
	private static void two_bit_pixel_code_string(final BitSource bs, final PixelDataSubBlock b) {

		boolean readMore = true;
		do{
			final int bits = bs.readBits(2);
			if (bits != 0) {
				b.addPixel((byte)bits);
			} else {
				final int switch_1= bs.readBits(1); //switch_1 1 bslbf

				if (switch_1 == 1) {
					final int run_length_3_10 = bs.readBits(3);
					final int two_bit_pixel_code = bs.readBits(2);
					b.addPixel((byte)two_bit_pixel_code,run_length_3_10+3);
				} else {
					final int switch_2 = bs.readBits(1); // switch_21 bslbf
					if (switch_2 == 0) {
						final int switch_3 = bs.readBits(2); //switch_3 2 bslbf
						if(switch_3 == 2){ // (switch_3 == '10') {
							final int run_length_12_27 = bs.readBits(4);
							final int two_bit_pixel_code = bs.readBits(2);
							b.addPixel((byte)two_bit_pixel_code,run_length_12_27+12);
						}
						if (switch_3 == 3) {//switch_3 == '11'
							final int run_length_29_284 = bs.readBits(8);// 8 uimsbf
							final int two_bit_pixel_code = bs.readBits(2);
							b.addPixel((byte)two_bit_pixel_code,run_length_29_284+29);
						}
						if(switch_3 == 0 ){ // not in spec, but we have to stop somewhere, see 11 Structure of the pixel code strings (informative) ETSI EN 300 743 V1.3.1 (2006-11)
							readMore = false;
						}
						if(switch_3 == 1 ){
							b.addPixel((byte)0,2); // two pixels in colour 0 , see 11 Structure of the pixel code strings (informative) ETSI EN 300 743 V1.3.1 (2006-11)
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
	private static void four_bit_pixel_code_string(final BitSource bs, final PixelDataSubBlock b) {

		boolean readMore = true;
		do{
			final int bits = bs.readBits(4);
			if (bits != 0) { //if (nextbits() != '0000') {
				b.addPixel((byte)bits); // 4-bit_pixel-code 4 bslbf
			} else {
				// 4-bit_zero 4 bslbf
				final int switch_1 = bs.readBits(1);// switch_1 1 bslbf
				if (switch_1 == 0) { // if (switch_1 == '0') {
					final int nextbits = bs.readBits(3);//if (nextbits() != '000')
					if(nextbits !=000){
						final int run_length_3_9 = nextbits;
						b.addPixel((byte)0,run_length_3_9 +2);
					} else{
						readMore = false; //end_of_string_signal 3 bslbf
					}
				}else{
					final int switch_2= bs.readBits(1);  //switch_2 1 bslbf
					if(switch_2 == 0){
						final int run_length_4_7= bs.readBits(2);//  2 bslbf
						final int pixel_code = bs.readBits(4) ;// 4 bslbf
						b.addPixel((byte)pixel_code,run_length_4_7 +4);
					}else{
						final int switch_3 = bs.readBits(2);//switch_3 2 bslbf
						if (switch_3 == 0) { //if (switch_3 == '0') {
							b.addPixel((byte)0);
						}
						if (switch_3 == 1) { //if (switch_3 == '01') {
							b.addPixel((byte)0,2);
						}
						if (switch_3 == 2) { //if (switch_3 == '10') {
							final int run_length_9_24 = bs.readBits(4);//4 uimsbf
							final int pixel_code = bs.readBits(4) ;// 4 bslbf
							b.addPixel((byte)pixel_code,run_length_9_24 +9);
						}
						if (switch_3 == 3) {//	if (switch_3 == '11') {
							final int  run_length_25_280 = bs.readBits(8);//8 uimsbf
							final int pixel_code = bs.readBits(4) ;// 4 bslbf
							b.addPixel((byte)pixel_code,run_length_25_280 +25);
						}
					}
				}
			}
		}while(readMore);
	}


	/**
	 * @param bs
	 * @param b
	 */
	private static void eigth_bit_pixel_code_string(final BitSource bs, final PixelDataSubBlock b) {

		boolean readMore = true;
		do{
			final int bits = bs.readBits(8);
			if (bits != 0) { //if (nextbits() != '0000') {
				b.addPixel(Utils.getInt2UnsignedByte(bits)); // 8-bit_pixel-code 8 bslbf
			} else {
				// 8-bit_zero 4 bslbf
				final int switch_1 = bs.readBits(1);// switch_1 1 bslbf
				if (switch_1 == 0) { // if (switch_1 == '0') {
					final int nextbits = bs.readBits(7);//if (nextbits() != '0000 0000') {
					if(nextbits !=0){
						final int run_length_1_127 = nextbits;
						b.addPixel((byte)0,run_length_1_127);
					} else{
						readMore = false; //end_of_string_signal 3 bslbf
					}
				}else{
					final int run_length_3_127= bs.readBits(7);//  2 bslbf
					final int pixel_code = bs.readBits(8) ;// 4 bslbf
					b.addPixel(Utils.getInt2UnsignedByte(pixel_code),run_length_3_127);
				}
			}
		}while(readMore);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.Segment#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus,this);
		s.add(new DefaultMutableTreeNode(new KVP("object_id", getObjectId(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("object_version_number", getObjectVersionNumber(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("object_coding_method", getObjectCodingMethod(), getObjectCodingMethodString(getObjectCodingMethod()))));
		s.add(new DefaultMutableTreeNode(new KVP("non_modifying_colour_flag", getNonModifyingColourFlag(), null)));
		if(getObjectCodingMethod()==0){
			s.add(new DefaultMutableTreeNode(new KVP("top_field_data_block_length", getTopFieldDataBlockLength(), null)));
			s.add(new DefaultMutableTreeNode(new KVP("bottom_field_data_block_length", getBottomFieldDataBlockLength(), null)));
			addListJTree(s, topFieldDataBlocks,modus,"top field pixel-data_sub-block");
			addListJTree(s, bottomFieldDataBlocks,modus,"bottom field pixel-data_sub-block");

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
	public static String getObjectCodingMethodString(final int type) {

		switch (type) {

		case 0x0:
			return "coding of pixels";
		case 0x1:
			return "coded as a string of characters";
		case 0x2:
			return "reserved";
		case 0x3:
			return "reserved";
		default:
			return "Illegal value";
		}
	}


	/**
	 * @param type
	 * @return
	 */
	public static String getDataTypeString(final int type) {

		switch (type) {


		case 0x10 :
			return "2-bit/pixel code string";
		case 0x11 :
			return "4-bit/pixel code string";
		case 0x12 :
			return "8-bit/pixel code string";
		case 0x20 :
			return "2_to_4-bit_map-table data";
		case 0x21 :
			return "2_to_8-bit_map-table data";
		case 0x22 :
			return "4_to_8-bit_map-table data";
		case 0xF0 :
			return "end of object line code";
		default:
			return "reserved";
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	public BufferedImage getImage() {
		BufferedImage bi=null;
		final WritableRaster wr = getRaster();
		if(wr==null){
			return null;
		}

		final IndexColorModel cm =  CLUTDefinitionSegment.getDefault_CLUT_4bitColorModel();

		bi = new BufferedImage(cm, wr, false, null);
		return bi;
	}

	/**
	 * @return
	 */
	public WritableRaster getRaster() {
		int width=-1;
		int height=-1;
		int dataType =-1;

		int topLines=0;
		int bottomLines=0;

		for(final PixelDataSubBlock block: topFieldDataBlocks){
			if((block.getDataType()>=0x10)&&(block.getDataType()<=0x12)){
				if(dataType ==-1){ // first block found
					dataType = block.getDataType();
					width= block.getNo_pixels();
				}
				if((width!=block.getNo_pixels())&&(width!=-1)){
					logger.log(Level.INFO,"Top pixel_no "+block.getNo_pixels()+" not matching current width "+width+" after line "+topLines);
					return null;
				}
				if((dataType!=block.getDataType())&&(dataType!=-1)){
					logger.log(Level.INFO,"Top block.getDataType() "+block.getDataType()+"not matching current dataType "+dataType+" after line "+topLines);
					return null;
				}
				topLines++;
			}
		}


		for(final PixelDataSubBlock block: bottomFieldDataBlocks){
			if((block.getDataType()>=0x10)&&(block.getDataType()<=0x12)){
				if((width!=block.getNo_pixels())&&(width!=-1)){
					logger.log(Level.INFO,"Bottom pixel_no "+block.getNo_pixels()+" not matching current width "+width+" after line "+bottomLines);
					return null;
				}
				if((dataType!=block.getDataType())&&(dataType!=-1)){
					logger.log(Level.INFO,"Bottom block.getDataType() "+block.getDataType()+"not matching current dataType "+dataType+" after line "+bottomLines);
					return null;
				}
				bottomLines++;
			}
		}

		if((topLines!=bottomLines)&&(topLines!=(bottomLines+1))){
			logger.log (Level.WARNING,"topLines "+topLines+" not matching bottomLines "+bottomLines);
			return null;
		}

		height = topLines + bottomLines;

		final byte[] dataBuffer = new byte[height * width];

		final Iterator<PixelDataSubBlock> topIter = topFieldDataBlocks.iterator();
		final Iterator<PixelDataSubBlock> bottomIter = bottomFieldDataBlocks.iterator();
		PixelDataSubBlock block;
		for (int i = 0; i < topLines; i++) {
			block = topIter.next();
			while((block.getDataType()<0x10) ||(block.getDataType()>0x12)){
				block = topIter.next();
			}
			System.arraycopy(block.getPixels(), 0, dataBuffer, 2*i*width, block.getNo_pixels());
			if(i<bottomLines){  // bottomlines can be one less than toplines
				block = bottomIter.next();
				while((block.getDataType()<0x10) ||(block.getDataType()>0x12)){
					block = bottomIter.next();
				}
				System.arraycopy(block.getPixels(), 0, dataBuffer, ((2*i)+1)*width, block.getNo_pixels());
			}

		}
		final DataBuffer dBuffer = new DataBufferByte(dataBuffer, width * height);
		return Raster.createInterleavedRaster(dBuffer,width,height,width,1,new int[]{0},null);
	}


}