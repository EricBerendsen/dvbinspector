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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.gui.ImageSource;

public class DisplaySet implements TreeNode, ImageSource {


	private List<Segment>  segments = new ArrayList<Segment>();
	// all sets up to and including this one from start of epoch ("mode change" or "acquisition point")
	// so all we need to draw image
	private List<DisplaySet> epoch = null;
	private GeneralPesHandler pesHandler =null;

	private long pts = 0;

	public DisplaySet(final GeneralPesHandler pesHandler, final long pts) {
		this.pesHandler = pesHandler;
		this.pts = pts;
	}

	public long getPts() {
		return pts;
	}

	public void setPts(final long pts) {
		this.pts = pts;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t=new DefaultMutableTreeNode(new KVP("Display Set",this));
		t.add(new DefaultMutableTreeNode(new KVP("pts",pts, printTimebase90kHz(pts))));
		addListJTree(t, segments, modus, "segments");

		return t;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 *
	 * Known BUG (???), i think the error is in the TS of France DVB-T
	 *
	 * On subtitles in mux_R5.ts like pid: 0xF0 (240) => France 2 HD ITU-T Rec. H.222.0 | ISO/IEC 13818-1 PES packets containing private data DVB subtitling
	 * this breaks because region composition segment is 38 heigh,
	 * and contains 3 regions, last one with vertical position 37. This should be one pix heigh to
	 * fit, and it has only one line in top field pixel-data_sub-block. That means it is 2 lines heigh!
	 *
	 * It can not be one line heigh, because ETSI EN 300 743 V1.3.1 (2006-11)  7.2.5 Object data segment says;
	 *
	 *  For each object the pixel-data sub-block for the top field and the pixel-data sub-block for the bottom field shall be
	 *  carried in the same object_data_segment. If this segment carries no data for the bottom field, i.e. the
	 *  bottom_field_data_block_length contains the value '0x0000', then the data for the top field shall be valid for the bottom
	 *  field also.
	 *
	 *  NOTE: This effectively forbids an object from having a height of only one TV picture line. Isolated objects of
	 *  this height would be liable to suffer unpleasant flicker effects at the TV display frame rate when displayed
	 *  on an interlaced display.
	 *
	 */
	@Override
	public BufferedImage getImage() {
		BufferedImage res = null;
		if(epoch!=null){
			final RegionCompositionSegment regions[] = new RegionCompositionSegment [256];
			final WritableRaster regionRaster[] = new WritableRaster[256];
			final CLUTDefinitionSegment cluts[] = new CLUTDefinitionSegment[256];
			final DisplaySet initDisplaySet = epoch.get(0);
			final List<Segment> initDisplaySegments = initDisplaySet.getSegments();
			final Map<Integer, ObjectDataSegment> objects = new HashMap<Integer,ObjectDataSegment>();
			DisplayDefinitionSegment displayDefinitionSegment = null;
			PageCompositionSegment lastPCS = null;

			// which segment are we processing

			List<RegionCompositionSegment> localRegions = new ArrayList<RegionCompositionSegment>();
			for (final Segment segment : initDisplaySegments) {

				if(segment.getSegmentType()==0x14){ // display definition segment
					displayDefinitionSegment = (DisplayDefinitionSegment)segment;
				}


				if(segment.getSegmentType()==0x10 ){// page composition segment, should be present at start of epoch
					final PageCompositionSegment pcSegment = (PageCompositionSegment)segment;
					lastPCS = pcSegment;
				}
				if(segment.getSegmentType()==0x11){ // region composition
					final RegionCompositionSegment rcs = (RegionCompositionSegment)segment;
					regions[rcs.getRegionId()] = rcs;
					localRegions.add(rcs);
				}
				if(segment.getSegmentType()==0x12){ // CLUT
					final CLUTDefinitionSegment cds = (CLUTDefinitionSegment)segment;
					cluts[cds.getCLUTId()] = cds;

				}
				if(segment.getSegmentType()==0x13){ // object data segment
					final ObjectDataSegment ods = (ObjectDataSegment)segment;
					objects.put(ods.getObjectId(), ods);
				}
			}
			// create raster for all regions, and fill immediately
			for (int j = 0; j < regions.length; j++) {
				final RegionCompositionSegment rcs = regions[j];
				if(rcs!=null){
					//fill the region's, ignore the fill flag, it has to be done anyway
					int index = 0;
					switch (rcs.getRegionDepth()) {
					case 1: // 2 bit
						index = rcs.getRegion2BitPixelCode();
						break;
					case 2: // 4 bit.
						index = rcs.getRegion4BitPixelCode();
						break;
					case 3:
						index = rcs.getRegion8BitPixelCode();
						break;
					}
					//create new byte[] to fill
					final byte[] dataArray = new byte[rcs.getRegionHeight() * rcs.getRegionWidth()];
					Arrays.fill(dataArray, getInt2UnsignedByte(index));
					regionRaster[j] = Raster.createInterleavedRaster(new DataBufferByte(dataArray, rcs.getRegionWidth() * rcs.getRegionHeight()),rcs.getRegionWidth(),rcs.getRegionHeight(),rcs.getRegionWidth(),1,new int[]{0},null);
				}
			}
			// we got everything, lets start drawing.

			paintObjectsOnRegions(objects, localRegions, regionRaster);

			// did displayset[0]
			// now loop over other sets
			int k=1;
			while(k< epoch.size()){
				final DisplaySet displaySet = epoch.get(k++);
				// used to remember which regions are in this set, because we need to update them with the objects later
				localRegions = new ArrayList<RegionCompositionSegment>();
				final List<Segment> segmentList = displaySet.getSegments();
				for (final Segment segment : segmentList) {
					if(segment.getSegmentType()==0x10 ){// page composition segment,
						final PageCompositionSegment pcSegment = (PageCompositionSegment)segment;
						lastPCS = pcSegment;
					}else if(segment.getSegmentType()==0x11){ // region composition
						final RegionCompositionSegment rcs = (RegionCompositionSegment)segment;
						regions[rcs.getRegionId()] = rcs;
						localRegions.add(rcs);
					}else if(segment.getSegmentType()==0x12){ // CLUT
						final CLUTDefinitionSegment cds = (CLUTDefinitionSegment)segment;
						cluts[cds.getCLUTId()] = cds;
					}else if(segment.getSegmentType()==0x13){ // object data segment
						final ObjectDataSegment ods = (ObjectDataSegment)segment;
						objects.put(ods.getObjectId(), ods);
					}
				}// got all segments, now update regions
				paintObjectsOnRegions(objects, localRegions, regionRaster);
			}

			int width=720;
			int height=576;
			if(displayDefinitionSegment !=null){
				width = displayDefinitionSegment.getDisplayWidth()+1;
				height = displayDefinitionSegment.getDisplayHeight()+1;
			}
			final BufferedImage bgImage = pesHandler.getBGImage(height, width,pts);
			res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D resGraphics = res.createGraphics();
			resGraphics.drawImage(bgImage,0,0,null);

			// do the actual drawing
			if(lastPCS!=null){
				for(final PageCompositionSegment.Region region :lastPCS.getRegions()){
					final RegionCompositionSegment regionCompositionSegment = regions[region.getRegion_id()];
					if(regionCompositionSegment!=null){
						final WritableRaster regionRaste = regionRaster[region.getRegion_id()];
						final IndexColorModel iColorModel = cluts[regionCompositionSegment.getCLUTId()].getColorModel(regionCompositionSegment.getRegionDepth());
						final BufferedImage img = new BufferedImage(iColorModel, regionRaste, false, null);
						resGraphics.drawImage(img, region.getRegion_horizontal_address(), region.getRegion_vertical_address(),null); // no observer
					}
				}
			}

			return res;
		}else{ // no epoch
			return null;
		}
	}

	private void paintObjectsOnRegions(final Map<Integer, ObjectDataSegment> objects,
			final List<RegionCompositionSegment> localRegions,
			final WritableRaster[] regionRaster) {
		for (final RegionCompositionSegment rcs : localRegions) {
			for(final RegionCompositionSegment.RegionObject regionObject: rcs.getRegionObjects()){
				final int object_id = regionObject.getObject_id();
				final ObjectDataSegment objectDataSegment = objects.get(object_id);

				if(objectDataSegment.getObjectCodingMethod()==0){ // if bitmap
					final WritableRaster raster = objectDataSegment.getRaster(rcs.getRegionDepth());
					regionRaster[rcs.getRegionId()].setDataElements(regionObject.getObject_horizontal_position(), regionObject.getObject_vertical_position(), raster);
				}else if(objectDataSegment.getObjectCodingMethod()==1){ // chars
					final Font font = new Font("Arial", Font.BOLD,30);
					// can not draw on raster directly, create img,draw on it and get its raster

					// first determine needed dimensions of image, so create another tmp image to get size
					final BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED);
					Graphics2D g2d = tmp.createGraphics();
					g2d.setFont(font);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
					final FontRenderContext fontRenderContext  = g2d.getFontRenderContext();
					final String txt = objectDataSegment.getCharacter_code_string();
					final Rectangle2D rect = font.getStringBounds(txt, fontRenderContext);

					// now we can create the image to draw on

					final IndexColorModel icm = CLUTDefinitionSegment.getDefaultColorModel(rcs.getRegionDepth());
					final BufferedImage tmp2 = new BufferedImage((int)rect.getWidth(),(int)rect.getHeight(),BufferedImage.TYPE_BYTE_INDEXED,icm);
					g2d = tmp2.createGraphics();
					g2d.setFont(font);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
					g2d.setBackground(new Color(icm.getRGB(regionObject.getBackground_pixel_code())));
					g2d.setColor(new Color(icm.getRGB(regionObject.getForeground_pixel_code())));
					g2d.drawString(txt, 0, (int)-rect.getY());
					final WritableRaster raster = tmp2.getRaster();

					regionRaster[rcs.getRegionId()].setDataElements(regionObject.getObject_horizontal_position(), regionObject.getObject_vertical_position(), raster);
				}
			}
		}
	}

	public void add(final Segment segment) {
		segments.add(segment);
	}

	public Segment getSegment(final int i) {
		return segments.get(i);
	}

	public List<DisplaySet> getEpoch() {
		return epoch;
	}

	public void setEpoch(final List<DisplaySet> epoch) {
		this.epoch = epoch;
	}

	public GeneralPesHandler getPesHandler() {
		return pesHandler;
	}

	public void setPesHandler(final GeneralPesHandler pesHandler) {
		this.pesHandler = pesHandler;
	}

	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(final List<Segment> segments) {
		this.segments = segments;
	}

}
