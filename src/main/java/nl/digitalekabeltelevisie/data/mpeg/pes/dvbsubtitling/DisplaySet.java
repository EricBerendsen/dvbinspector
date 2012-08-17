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

import java.awt.Graphics2D;
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
import nl.digitalekabeltelevisie.data.mpeg.pes.AbstractPesHandler;
import nl.digitalekabeltelevisie.gui.ImageSource;

public class DisplaySet implements TreeNode, ImageSource {

		
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

		return t;
	}

	@Override
	public BufferedImage getImage() {
		BufferedImage res = null;
		if(epoch!=null){
			RegionCompositionSegment regions[] = new RegionCompositionSegment [256];
			WritableRaster regionRaster[] = new WritableRaster[256];
			CLUTDefinitionSegment cluts[] = new CLUTDefinitionSegment[256]; 
			DisplaySet initDisplaySet = epoch.get(0);
			List<Segment> initDisplaySegments = initDisplaySet.getSegments();
			Map<Integer, ObjectDataSegment> objects = new HashMap<Integer,ObjectDataSegment>();
			DisplayDefinitionSegment displayDefinitionSegment = null;
			PageCompositionSegment lastPCS = null;
			
			// which segment are we processing
			//int i=0;
			
			List<RegionCompositionSegment> localRegions = new ArrayList<RegionCompositionSegment>();
			for (Segment segment : initDisplaySegments) {
				
				if(segment.getSegmentType()==0x14){ // display definition segment
					displayDefinitionSegment = (DisplayDefinitionSegment)segment;
				}


				if(segment.getSegmentType()==0x10 ){// page composition segment, should be present at start of epoch
					final PageCompositionSegment pcSegment = (PageCompositionSegment)segment;
					lastPCS = pcSegment;
				}
				if(segment.getSegmentType()==0x11){ // region composition
					RegionCompositionSegment rcs = (RegionCompositionSegment)segment;
					regions[rcs.getRegionId()] = rcs;
					localRegions.add(rcs);
				}
				if(segment.getSegmentType()==0x12){ // CLUT
					CLUTDefinitionSegment cds = (CLUTDefinitionSegment)segment;
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
					byte[] dataArray = new byte[rcs.getRegionHeight() * rcs.getRegionWidth()];
					Arrays.fill(dataArray, getSignedByte(index));
					regionRaster[j] = Raster.createInterleavedRaster(new DataBufferByte(dataArray, rcs.getRegionWidth() * rcs.getRegionHeight()),rcs.getRegionWidth(),rcs.getRegionHeight(),rcs.getRegionWidth(),1,new int[]{0},null);
				}
			}
			// we got everything, lets start drawing. 
			
			paintObjectsOnRegions(objects, localRegions, regionRaster);

			// did displayset[0]
			// now loop over other sets
			int k=1;
			while(k< epoch.size()){
				DisplaySet displaySet = epoch.get(k++);
				// used to remember which regions are in this set, because we need to update them with the objects later
				localRegions = new ArrayList<RegionCompositionSegment>();
				List<Segment> segments = displaySet.getSegments();
				for (Segment segment : segments) {
					if(segment.getSegmentType()==0x10 ){// page composition segment, 
						final PageCompositionSegment pcSegment = (PageCompositionSegment)segment;
						lastPCS = pcSegment;
					}else if(segment.getSegmentType()==0x11){ // region composition
						RegionCompositionSegment rcs = (RegionCompositionSegment)segment;
						regions[rcs.getRegionId()] = rcs;
						localRegions.add(rcs);
					}else if(segment.getSegmentType()==0x12){ // CLUT
						CLUTDefinitionSegment cds = (CLUTDefinitionSegment)segment;
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
			BufferedImage bgImage = pesHandler.getBGImage(height, width,pts);
			res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D resGraphics = res.createGraphics();
			resGraphics.drawImage(bgImage,0,0,null);
			
			// do the actual drawing
			if(lastPCS!=null){
				for(final PageCompositionSegment.Region region :lastPCS.getRegions()){
					final RegionCompositionSegment regionCompositionSegment = regions[region.getRegion_id()];
					if(regionCompositionSegment!=null){
						WritableRaster regionRaste = regionRaster[region.getRegion_id()];
						IndexColorModel iColorModel = cluts[regionCompositionSegment.getCLUTId()].getColorModel(regionCompositionSegment.getRegionDepth());
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

	private void paintObjectsOnRegions(Map<Integer, ObjectDataSegment> objects,
			List<RegionCompositionSegment> localRegions,
			WritableRaster[] regionRaster) {
		for (RegionCompositionSegment rcs : localRegions) {
			for(final RegionCompositionSegment.RegionObject regionObject: rcs.getRegionObjects()){
				int object_id = regionObject.getObject_id();
				ObjectDataSegment objectDataSegment = objects.get(object_id);
				
				if(objectDataSegment.getObjectCodingMethod()==0){ // if bitmap
					final WritableRaster raster = objectDataSegment.getRaster(rcs.getRegionDepth());
					regionRaster[rcs.getRegionId()].setDataElements(regionObject.getObject_horizontal_position(), regionObject.getObject_vertical_position(), raster);
				} // TODO chars
			}
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
