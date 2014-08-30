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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.util.Utils;


/**
 *
 */
public class DRCSCharacter implements TreeNode, ImageSource {

	private final int mode;
	private final TxtDataField line;
	private final int drcsNumber;
	private BufferedImage fBufferedImage = null;
	private DataBuffer dataBuffer = null;
	// Get the writable raster so that data can be changed.
	private WritableRaster writableRaster = null;


	/**
	 * @param drcsMode
	 * @param page
	 * @param i
	 */
	public DRCSCharacter(final int drcsMode, final SubPage page, final int i) {
		drcsNumber = i;
		mode = drcsMode;
		line = page.linesList[1+(i/2)];
		if((line!=null)&&(mode==0)){// TODO support other than mode 0 12x10x1
			final byte []rawData = line.getPageDataBytes();
			int offset=0;
			if((i%2)!=0){
				offset=20;
			}

			final int fWidth = 12, fHeight = 10;
			byte [] imgData = null;
			final int bytesPerRow = (fWidth/8) + ((fWidth%8)!=0?1:0);
			imgData = new byte[fHeight * bytesPerRow];
			for (int j = 0; j < 20; j+=2){
				final byte left = Utils.getInt2UnsignedByte(((rawData[offset+j]&0x3F)<<2) |((rawData[offset+j+1]&0x30)>>4)); // 6 bits + 2 bits
				final byte right = Utils.getInt2UnsignedByte((rawData[offset+j+1]& 0x0f)<<4); // 4 bits
				imgData[j]=left;
				imgData[j+1]=right;
			}

			final byte[] bw = {(byte) 0xff, (byte) 0};
			final IndexColorModel blackAndWhite = new IndexColorModel(
					1, // One bit per pixel
					2, // Two values in the component arrays
					bw, // Red Components
					bw, // Green Components
					bw);// Blue Components

			dataBuffer = new DataBufferByte( imgData, imgData.length);
			// Get the writable raster so that data can be changed.
			writableRaster = Raster.createPackedRaster(dataBuffer, fWidth, fHeight, 1, null);
			// Create the BufferedImage
			fBufferedImage = new BufferedImage(blackAndWhite, writableRaster, true, null);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		return new DefaultMutableTreeNode(new KVP("DRCS Character "+drcsNumber+", mode="+mode +" (" +getDRCSModeString(mode)+")",this));
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	public BufferedImage getImage() {
		return fBufferedImage;
	}

	public static String getDRCSModeString(final int drcsMode) {

		switch (drcsMode) {
		case 0x00: return "12 x 10 x 1";
		case 0x01: return "12 x 10 x 2";
		case 0x02: return "12 x 10 x 4";
		case 0x03: return "6 x 5 x 4";
		case 0x0E: return "Subsequent PTU of a Mode 1 or 2 character";
		case 0x0F: return "No data for the corresponding character";
		default: return "reserved for future use";
		}
	}


	/**
	 * @return the dataBuffer
	 */
	public DataBuffer getDataBuffer() {
		return dataBuffer;
	}


	/**
	 * @return the drcsNumber
	 */
	public int getDrcsNumber() {
		return drcsNumber;
	}


	/**
	 * @return the fBufferedImage
	 */
	public BufferedImage getFBufferedImage() {
		return fBufferedImage;
	}


	/**
	 * @return the line
	 */
	public TxtDataField getLine() {
		return line;
	}


	/**
	 * @return the mode
	 */
	public int getMode() {
		return mode;
	}


	/**
	 * @return the writableRaster
	 */
	public WritableRaster getWritableRaster() {
		return writableRaster;
	}



}
