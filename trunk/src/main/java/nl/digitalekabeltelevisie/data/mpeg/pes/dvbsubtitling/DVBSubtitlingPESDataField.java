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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 * 
 * PES_data_field() as defined in EN 300 743 V1.3.1
 * Digital Video Broadcasting (DVB); Subtitling systems
 * 
 */

public class DVBSubtitlingPESDataField extends PesPacketData implements TreeNode, ImageSource {

	/**
	 * 
	 */
	private static Logger logger = Logger.getLogger(DVBSubtitlingPESDataField.class.getName());

	private final int data_identifier;

	private DisplayDefinitionSegment displayDefinitionSegment = null;
	private final Map<Integer, RegionCompositionSegment> regionCompositionsSegments = new HashMap<Integer, RegionCompositionSegment>();
	private final Map<Integer, CLUTDefinitionSegment> clutDefinitions= new HashMap<Integer, CLUTDefinitionSegment>();
	private final Map<Integer, ObjectDataSegment> objects= new HashMap<Integer, ObjectDataSegment>();
	private static BufferedImage bgImage576;
	private static BufferedImage bgImage720;
	private static BufferedImage bgImage1080;

	private static final ClassLoader classLoader = DVBSubtitlingPESDataField.class.getClassLoader();


	/**
	 * 
	 */
	private final int subtitle_stream_id;

	static {
		try {
			InputStream fileInputStream = classLoader.getResourceAsStream("monitors576.bmp");
			bgImage576 = ImageIO.read(fileInputStream);
			fileInputStream = classLoader.getResourceAsStream("monitors720.bmp");
			bgImage720 = ImageIO.read(fileInputStream);
			fileInputStream = classLoader.getResourceAsStream("monitors1080.bmp");
			bgImage1080 = ImageIO.read(fileInputStream);
		} catch (final Exception e) {
			logger.log(Level.WARNING, "error reading image ", e);
		}

	}


	/**
	 * 
	 */
	private final List<Segment> segmentList = new ArrayList<Segment>();


	/**
	 * @param data
	 * @param offset
	 * @param len
	 * @param pts
	 */
	public DVBSubtitlingPESDataField(final PesPacketData pesPacket){

		super(pesPacket);

		final int offset = getPesDataStart();
		data_identifier = getInt(data, offset, 1, MASK_8BITS);
		subtitle_stream_id = getInt(data, offset + 1, 1, MASK_8BITS);


		int t = 2;
		while (data[offset + t] == 0x0f) { // sync byte
			Segment segment;
			final int segment_type = getInt(data, offset + t + 1, 1, MASK_8BITS);
			switch (segment_type) {
			case 0x10:
				segment = new PageCompositionSegment(data, offset + t);
				break;
			case 0x11:
				final RegionCompositionSegment rc = new RegionCompositionSegment(data, offset + t);
				segment = rc;
				regionCompositionsSegments.put(rc.getRegionId(), rc);
				break;
			case 0x12:
				final CLUTDefinitionSegment cl = new CLUTDefinitionSegment(data, offset + t);
				segment = cl;
				clutDefinitions.put(cl.getCLUTId(), cl);
				break;
			case 0x13:
				final ObjectDataSegment obj = new ObjectDataSegment(data, offset + t);
				segment = obj;
				objects.put(obj.getObjectId(), obj);
				break;
			case 0x14:
				segment = new DisplayDefinitionSegment(data, offset + t);
				displayDefinitionSegment = (DisplayDefinitionSegment)segment;
				break;

			default:
				segment = new Segment(data, offset + t);
				break;
			}

			segmentList.add(segment);
			t += 6 + getInt(data, offset + t + 4, 2, MASK_16BITS);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode s = super.getJTreeNode(modus);
		s.setUserObject(new KVP("DVBSubtitlingSegments",this));

		s.add(new DefaultMutableTreeNode(new KVP("data_identifier",data_identifier, getDataIDString(data_identifier))));
		s.add(new DefaultMutableTreeNode(new KVP("subtitle_stream_id",subtitle_stream_id, null)));
		addListJTree(s, segmentList, modus, "segments");
		return s;
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	public BufferedImage getImage() {
		int width=720;
		int height=576;
		BufferedImage bgImage = bgImage576;
		// display_definition_segment for other size
		if(displayDefinitionSegment!=null){
			width = displayDefinitionSegment.getDisplayWidth()+1;
			height = displayDefinitionSegment.getDisplayHeight()+1;
			if(height==1080){
				bgImage = bgImage1080;
			}else if(height==720){
				bgImage = bgImage720;
			}
			// TODO handle display_window_flag and display_window_horizontal_position_minimum, etc
		}
		final BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gd = img.createGraphics();
		gd.drawImage(bgImage, 0, 0,null);
		//		gd.setColor(new Color(0x00ff0000));
		//		gd.fillRect(0, 0, width, height);
		for (final Segment element: segmentList) {
			if(element.getSegmentType()==0x10 ){// page composition segment)
				final PageCompositionSegment pcSegment = (PageCompositionSegment)element;
				for(final PageCompositionSegment.Region region :pcSegment.getRegions()){
					final RegionCompositionSegment rc = regionCompositionsSegments.get(region.getRegion_id());
					if(rc!=null){
						int clutId = rc.getCLUTId();
						CLUTDefinitionSegment clutDefinitionSegment = clutDefinitions.get(clutId);
						//TODO fix 2 bit CLUT (or other error). PID 1037, segment 68 of  "07-20_CINE SKY (por)_Um Espírito Atrás de Mim_01.ts" 

						//fill the region
							if((rc.getRegionFillFlag()==1)&&(clutDefinitionSegment!=null)){
								int index = 0;
								switch (rc.getRegionDepth()) {
								case 1: // 2 bit
									index = rc.getRegion2BitPixelCode();
									break;
								case 2: // 4 bit. TODO  if the region depth is 8 bit while the region_level_of_compatibility specifies that a 4-bit CLUT is within the minimum requirements.
									// ETSI EN 300 743 V1.3.1 (2006-11) P 25 bottom.
									index = rc.getRegion4BitPixelCode();
									break;
								case 3:
									index = rc.getRegion8BitPixelCode();
									break;
								}
	
								IndexColorModel colorModel = clutDefinitionSegment.getColorModel(rc.getRegionDepth());
								int rgb = colorModel.getRGB(index);
								Color bgColor = new Color(rgb,true);
								gd.setColor(bgColor);
								Rectangle rect = new Rectangle(region.getRegion_horizontal_address(), region.getRegion_vertical_address(), rc.getRegionWidth(), rc.getRegionHeight());
								gd.fill(rect);
							}
							// next line only for debugging..
							//gd.draw3DRect(region.getRegion_horizontal_address() , region.getRegion_vertical_address(), rc.getRegionWidth(), rc.getRegionHeight(), true);
						for(final RegionCompositionSegment.RegionObject regionObject: rc.getRegionObjects()){
							if(clutDefinitionSegment!=null){
								// This is the colorModel for this region composition segment
								final ColorModel cm = clutDefinitionSegment.getColorModel(rc.getRegionDepth());

							

								int object_id = regionObject.getObject_id();
								ObjectDataSegment objectDataSegment = objects.get(object_id);
								// objectDataSegment (or one or both of its PixelDataSubBlocks) can have a bit_map_table, which means we have to remap the CLUT
								// not clear from specs, where can bit_map_table occur, only at start of PixelDataSubBlocks, or anywhere?
								// do top_field and bottom_field always have the same bit_map_table?
								// assume for now always and only at start of PixelDataSubBlocks, and always same for top and lower.
								
								//clutDefinitionSegment.getCLUTEntries();
								
								
								
								final WritableRaster raster = objectDataSegment.getRaster();
								final BufferedImage i = new BufferedImage(cm, raster, false, null);
								gd.drawImage(i, region.getRegion_horizontal_address()+regionObject.getObject_horizontal_position(), region.getRegion_vertical_address()+regionObject.getObject_vertical_position(), null);
							}
						}
					}
				}
			}
		}


		return img;
	}

	/**
	 * @return
	 */
	public List<Segment> getSegmentList() {
		return segmentList;
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getSegmentTypeString(final int type) {

		if ((0x40 <= type) && (type <= 0x7f)) {
			return "reserved for future use";
		}
		if ((0x81 <= type) && (type <= 0xef)) {
			return "private data";
		}
		switch (type) {

		case 0x10:
			return "page composition segment";
		case 0x11:
			return "region composition segment";
		case 0x12:
			return "CLUT definition segment";
		case 0x13:
			return "object data segment";
		case 0x14:
			return "display definition segment";
		case 0x80:
			return "end of display set segment";
		case 0xFF:
			return "stuffing";
		default:
			return "reserved for future use";
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getPageStateString(final int type) {

		switch (type) {

		case 0x0:
			return "normal case - The display set contains only the subtitle elements that are changed from the previous page instance.";
		case 0x1:
			return "acquisition point - The display set contains all subtitle elements needed to display the next page instance.";
		case 0x2:
			return "mode change - The display set contains all subtitle elements needed to display the new page.";
		case 0x3:
			return "reserved";
		default:
			return "Illegal value";
		}
	}

}
