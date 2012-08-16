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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.AbstractPesHandler;
import nl.digitalekabeltelevisie.gui.ImageSource;

public class DisplaySet implements TreeNode, ImageSource {

	
	/**
	 * 
	 */
	private static Logger logger = Logger.getLogger(DisplaySet.class.getName());


	
	private List<Segment>  segments = new ArrayList<Segment>();
	// all sets up to and including this one from start of epoch ("mode change" or "acquisition point")
	// so all we need to draw image
	private ArrayList<DisplaySet> epoch = null;
	private AbstractPesHandler pesHandler =null;
	
	private long pts = 0;

	public DisplaySet(AbstractPesHandler pesHandler, long pts) {
		this.pesHandler = pesHandler;
		this.pts = pts;
	}

	public long getPts() {
		return pts;
	}

	public void setPts(long pts) {
		this.pts = pts;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t=new DefaultMutableTreeNode(new KVP("Display Set",this));
		t.add(new DefaultMutableTreeNode(new KVP("pts",pts, printTimebase90kHz(pts))));
		addListJTree(t, segments, modus, "segments");
		if(epoch!=null){
			t.add(new DefaultMutableTreeNode(new KVP("epoch length",epoch.size(), null)));
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("epoch == null")));
			
		}

		return t;
	}

	@Override
	public BufferedImage getImage() {
		BufferedImage res = null;
		if(epoch!=null){
			RegionCompositionSegment regions[] = new RegionCompositionSegment [256];
			//BufferedImage regionData[] = new BufferedImage[256];
			WritableRaster regionRaster[] = new WritableRaster[256];
			CLUTDefinitionSegment cluts[] = new CLUTDefinitionSegment[256]; 
			// do first display set to initialize all segments
			DisplaySet initDisplaySet = epoch.get(0);
			List<Segment> initDisplaySegments = initDisplaySet.getSegments();
			Map<Integer, ObjectDataSegment> objects = new HashMap<Integer,ObjectDataSegment>();
			
			int width=720;
			int height=576;
			// which segment are we processing
			int i=0;
			
			if(initDisplaySegments.get(0).getSegmentType()==0x14){ // display definition segment
				DisplayDefinitionSegment displayDefinitionSegment = (DisplayDefinitionSegment)initDisplaySegments.get(0);
				width = displayDefinitionSegment.getDisplayWidth()+1;
				height = displayDefinitionSegment.getDisplayHeight()+1;
				i++;
			}
			BufferedImage bgImage = pesHandler.getBGImage(height, width,pts);
			res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

			Graphics2D resGraphics = res.createGraphics();
			resGraphics.drawImage(bgImage,0,0,null);

			if(initDisplaySegments.get(i).getSegmentType()==0x10 ){// page composition segment, should be present at start of epoch
				final PageCompositionSegment pcSegment = (PageCompositionSegment)initDisplaySegments.get(i++);
				while(initDisplaySegments.get(i).getSegmentType()==0x11){ // region composition
					RegionCompositionSegment rcs = (RegionCompositionSegment)initDisplaySegments.get(i++);
					regions[rcs.getRegionId()] = rcs;
					
				}
				while(initDisplaySegments.get(i).getSegmentType()==0x12){ // CLUT
					CLUTDefinitionSegment cds = (CLUTDefinitionSegment)initDisplaySegments.get(i++);
					cluts[cds.getCLUTId()] = cds;
					
				}
				for (int j = 0; j < regions.length; j++) {
					final RegionCompositionSegment rcs = regions[j];
					if(rcs!=null){
//						IndexColorModel cm = null;
//						CLUTDefinitionSegment clut = cluts[rcs.getCLUTId()];
//						if(clut!=null){
//							cm = cluts[rcs.getCLUTId()].getColorModel(rcs.getRegionDepth());
//						}else{
//							cm = CLUTDefinitionSegment.getDefaultColorModel(rcs.getRegionDepth());
//						}
						regionRaster[rcs.getRegionId()] = Raster.createInterleavedRaster(new DataBufferByte(new byte[rcs.getRegionHeight() * rcs.getRegionWidth()], rcs.getRegionWidth() * rcs.getRegionHeight()),rcs.getRegionWidth(),rcs.getRegionHeight(),rcs.getRegionWidth(),1,new int[]{0},null);
						//regionData[rcs.getRegionId()] = new BufferedImage(rcs.getRegionWidth(), rcs.getRegionHeight(), BufferedImage.TYPE_INT_ARGB,cm);
					}
				}
				// now handle objects
				while(initDisplaySegments.get(i).getSegmentType()==0x13){ // object data segment
					final ObjectDataSegment ods = (ObjectDataSegment)initDisplaySegments.get(i++);
					objects.put(ods.getObjectId(), ods);
				}
				// we got everything, lets start drawing. 
				
				for(final PageCompositionSegment.Region region :pcSegment.getRegions()){
					final RegionCompositionSegment regionCompositionSegment = regions[region.getRegion_id()];
					if(regionCompositionSegment!=null){
						int clutId = regionCompositionSegment.getCLUTId();
						//CLUTDefinitionSegment clutDefinitionSegment = cluts[clutId];
						// BufferedImage regionImage = regionData[region.getRegion_id()];
						WritableRaster regionRaste = regionRaster[region.getRegion_id()];
//						ColorModel colorModel = regionImage.getColorModel();
//						IndexColorModel iColorModel = (IndexColorModel)colorModel;
//						Graphics2D gd = regionImage.createGraphics();

						//fill the region
						if(regionCompositionSegment.getRegionFillFlag()==1){
							int index = 0;
							switch (regionCompositionSegment.getRegionDepth()) {
							case 1: // 2 bit
								index = regionCompositionSegment.getRegion2BitPixelCode();
								break;
							case 2: // 4 bit. 
								index = regionCompositionSegment.getRegion4BitPixelCode();
								break;
							case 3:
								index = regionCompositionSegment.getRegion8BitPixelCode();
								break;
							}
							//create new byte[] to fill 
							
							RegionCompositionSegment rcs = regions[region.getRegion_id()];
							byte[] dataArray = new byte[rcs.getRegionHeight() * rcs.getRegionWidth()];
							Arrays.fill(dataArray, getSignedByte(index));
							regionRaster[region.getRegion_id()] = Raster.createInterleavedRaster(new DataBufferByte(dataArray, rcs.getRegionWidth() * rcs.getRegionHeight()),rcs.getRegionWidth(),rcs.getRegionHeight(),rcs.getRegionWidth(),1,new int[]{0},null);


						}
						// now draw the objects on this region
						for(final RegionCompositionSegment.RegionObject regionObject: regionCompositionSegment.getRegionObjects()){
							int object_id = regionObject.getObject_id();
							ObjectDataSegment objectDataSegment = objects.get(object_id);
							
							if(objectDataSegment.getObjectCodingMethod()==0){ // if bitmap
								final WritableRaster raster = objectDataSegment.getRaster(regionCompositionSegment.getRegionDepth());
								
								regionRaster[regionCompositionSegment.getRegionId()].setDataElements(regionObject.getObject_horizontal_position(), regionObject.getObject_vertical_position(), raster);
								//final BufferedImage img = new BufferedImage(iColorModel, raster, false, null);
								//gd.drawImage(img, regionObject.getObject_horizontal_position(), regionObject.getObject_vertical_position(), null);
							}else if(objectDataSegment.getObjectCodingMethod()==1){ // TODO char
//								final BufferedImage img = new BufferedImage(regionCompositionSegment.getRegionWidth(),regionCompositionSegment.getRegionHeight(),BufferedImage.TYPE_BYTE_INDEXED,iColorModel);
//								Graphics2D graphics = img.createGraphics();
//								// Font type/size is not defined in standard, this looks OK.
//								Font font = new Font("Arial", Font.BOLD,30);
//								graphics.setFont(font);
//								graphics.setColor(new Color(iColorModel.getRGB(regionObject.getForeground_pixel_code())));
//								graphics.setBackground(new Color(iColorModel.getRGB(regionObject.getBackground_pixel_code())));
//								graphics.drawString(objectDataSegment.getCharacter_code_string(), 0, 30);
//								gd.drawImage(img, regionObject.getObject_horizontal_position(), regionObject.getObject_vertical_position(), null);
							}
						}
					}
					WritableRaster regionRas = regionRaster[region.getRegion_id()];
					IndexColorModel iColorModel = cluts[regionCompositionSegment.getCLUTId()].getColorModel(regionCompositionSegment.getRegionDepth());
					final BufferedImage img = new BufferedImage(iColorModel, regionRas, false, null);

					resGraphics.drawImage(img, region.getRegion_horizontal_address(), region.getRegion_vertical_address(),null); // no observer
				}

			}
			return res;
		}else{ // no epoch
			return null;
		}
	}

	public void add(Segment segment) {
		segments.add(segment);
	}

	public Segment getSegment(int i) {
		return segments.get(i);
	}

	public ArrayList<DisplaySet> getEpoch() {
		return epoch;
	}

	public void setEpoch(ArrayList<DisplaySet> epoch) {
		this.epoch = epoch;
	}

	public AbstractPesHandler getPesHandler() {
		return pesHandler;
	}

	public void setPesHandler(AbstractPesHandler pesHandler) {
		this.pesHandler = pesHandler;
	}

	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(List<Segment> segments) {
		this.segments = segments;
	}

}
