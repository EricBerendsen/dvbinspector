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

import java.awt.*;
import java.awt.image.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.imageio.ImageIO;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.PesHeader;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 *
 * PES_data_field() as defined in EN 300 743 V1.3.1
 * Digital Video Broadcasting (DVB); Subtitling systems
 *
 */

public class DVBSubtitlingPESDataField extends PesPacketData implements ImageSource {

	/**
	 *
	 */
    private static final Logger logger = Logger.getLogger(DVBSubtitlingPESDataField.class.getName());

	private int data_identifier;
	private int subtitle_stream_id;

	private DisplayDefinitionSegment displayDefinitionSegment = null;
	private final Map<Integer, RegionCompositionSegment> regionCompositionsSegments = new HashMap<>();
	private final Map<Integer, CLUTDefinitionSegment> clutDefinitions= new HashMap<>();
	private final Map<Integer, ObjectDataSegment> objects= new HashMap<>();
	public static BufferedImage bgImage576;
	public static BufferedImage bgImage720;
	public static BufferedImage bgImage1080;

	private static final ClassLoader classLoader = DVBSubtitlingPESDataField.class.getClassLoader();




	static {
		try (InputStream fileInputStream576 = classLoader.getResourceAsStream("monitors576.jpg");
				InputStream fileInputStream720 = classLoader.getResourceAsStream("monitors720.jpg");
				InputStream fileInputStream1080 = classLoader.getResourceAsStream("monitors1080.jpg");
				){
			bgImage576 = ImageIO.read(fileInputStream576);
			bgImage720 = ImageIO.read(fileInputStream720);
			bgImage1080 = ImageIO.read(fileInputStream1080);
		} catch (Exception e) {
			logger.log(Level.WARNING, "error reading image ", e);
		}

	}


	/**
	 *
	 */
	private final List<Segment> segmentList = new ArrayList<>();



	public DVBSubtitlingPESDataField(PesPacketData pesPacket){

		super(pesPacket);

		int offset = getPesDataStart();
		if(pesDataLen>0) { // PES packet with more than just header
			data_identifier = getInt(data, offset, 1, MASK_8BITS);
			if(data_identifier==0x20){ // For DVB subtitle streams the data_identifier field shall be coded with the value 0x20. 300 743 V1.3.1 p.20
				subtitle_stream_id = getInt(data, offset + 1, 1, MASK_8BITS);


				int t = 2;
				while (data[offset + t] == 0x0f) { // sync byte
					Segment segment;
					int segment_type = getInt(data, offset + t + 1, 1, MASK_8BITS);
					switch (segment_type) {
					case 0x10:
						segment = new PageCompositionSegment(data, offset + t);
						break;
					case 0x11:
						RegionCompositionSegment rc = new RegionCompositionSegment(data, offset + t);
						segment = rc;
						regionCompositionsSegments.put(rc.getRegionId(), rc);
						break;
					case 0x12:
						CLUTDefinitionSegment cl = new CLUTDefinitionSegment(data, offset + t);
						segment = cl;
						clutDefinitions.put(cl.getCLUTId(), cl);
						break;
					case 0x13:
						ObjectDataSegment obj = new ObjectDataSegment(data, offset + t);
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
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {

		DefaultMutableTreeNode s = super.getJTreeNode(modus,new KVP("DVBSubtitlingSegments").addImageSource(this, "DVBSubtitlingSegments"));
		if(pesDataLen>0) { // PES packet with more than just header
			s.add(new KVP("data_identifier",data_identifier).setDescription(getDataIDString(data_identifier)));
			if(data_identifier==0x20){ // For DVB subtitle streams the data_identifier field shall be coded with the value 0x20. 300 743 V1.3.1 p.20
				s.add(new KVP("subtitle_stream_id",subtitle_stream_id));
				addListJTree(s, segmentList, modus, "segments");
			}
		}
		return s;
	}


	/**
	 * Renders an image of the subtitles in this PESPacket.
	 * <p>
	 * Known issues/todo's;
	 * <ol>
	 * <li>a display set can be spread across multiple PES</li>
	 * <li>Page ID is ignored</li>
	 * <li>only handles page_state== page refresh or new page, not incremental update </li>
	 * <li>nice to have; emulate decoder pixel depths of 2, 4 and 8 bits.</li>
	 * </ol>
	 * This is different from the getImage in DisplaySet as this draws directly on the background, not on regions.
     */
	@Override
	public BufferedImage getImage() {
		PesHeader pesHeader = getPesHeader();
		if((data_identifier==0x20)&& // For DVB subtitle streams the data_identifier field shall be coded with the value 0x20. 300 743 V1.3.1 p.20
				(pesHeader.getPts_dts_flags()>=2)){ //Is this a packet that has PTS? If not, it is not meant for display.
			int width=720;
			int height=576;
			int x_offset = 0;
			int y_offset = 0;
			if(displayDefinitionSegment!=null){
				width = displayDefinitionSegment.getDisplayWidth()+1;
				height = displayDefinitionSegment.getDisplayHeight()+1;
				if(displayDefinitionSegment.getDisplayWindowFlag()==1){
					x_offset = displayDefinitionSegment.getDisplayWindowHorizontalPositionMinimum();
					y_offset = displayDefinitionSegment.getDisplayWindowVerticalPositionMinimum();

				}
			}
			BufferedImage bgImage = pesHandler.getBGImage(height, width,pesHeader.getPts());
			BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
			Graphics2D gd = img.createGraphics();
			gd.drawImage(bgImage, 0, 0,null);
			for (Segment element: segmentList) {
				if(element.getSegmentType()==0x10 ){// page composition segment)
					PageCompositionSegment pcSegment = (PageCompositionSegment)element;
					for(PageCompositionSegment.Region region :pcSegment.getRegions()){
						RegionCompositionSegment regionCompositionSegment = regionCompositionsSegments.get(region.getRegion_id());
						if(regionCompositionSegment!=null){
							int clutId = regionCompositionSegment.getCLUTId();
							CLUTDefinitionSegment clutDefinitionSegment = clutDefinitions.get(clutId);

							//fill the region
							if((regionCompositionSegment.getRegionFillFlag()==1)&&(clutDefinitionSegment!=null)){
								int index = getPixelCode(regionCompositionSegment);

								IndexColorModel colorModel = clutDefinitionSegment.getColorModel(regionCompositionSegment.getRegionDepth());
								int rgb = colorModel.getRGB(index);
								Color bgColor = new Color(rgb,true);
								gd.setColor(bgColor);
								Rectangle rect = new Rectangle(region.getRegion_horizontal_address(), region.getRegion_vertical_address(), regionCompositionSegment.getRegionWidth(), regionCompositionSegment.getRegionHeight());
								gd.fill(rect);
							}
							for(RegionCompositionSegment.RegionObject regionObject: regionCompositionSegment.getRegionObjects()){
								if(clutDefinitionSegment!=null){
									// This is the colorModel for this region composition segment
									IndexColorModel cm = clutDefinitionSegment.getColorModel(regionCompositionSegment.getRegionDepth());

									int object_id = regionObject.getObject_id();
									ObjectDataSegment objectDataSegment = objects.get(object_id);

									if(objectDataSegment != null){
										if(objectDataSegment.getObjectCodingMethod()==0){ // if bitmap
											WritableRaster raster = objectDataSegment.getRaster(regionCompositionSegment.getRegionDepth());
											BufferedImage i = new BufferedImage(cm, raster, false, null);
											gd.drawImage(i, region.getRegion_horizontal_address()+regionObject.getObject_horizontal_position()+x_offset, region.getRegion_vertical_address()+regionObject.getObject_vertical_position()+y_offset, null);
										}else if(objectDataSegment.getObjectCodingMethod()==1){
											BufferedImage i = new BufferedImage(regionCompositionSegment.getRegionWidth(),regionCompositionSegment.getRegionHeight(),BufferedImage.TYPE_BYTE_INDEXED,cm);
											Graphics2D graphics = i.createGraphics();
											// Font type/size is never defined in standard, this looks OK.
											Font font = new Font("Arial", Font.BOLD,30);
											graphics.setFont(font);
											graphics.setColor(new Color(cm.getRGB(regionObject.getForeground_pixel_code())));
											graphics.setBackground(new Color(cm.getRGB(regionObject.getBackground_pixel_code())));
											graphics.drawString(objectDataSegment.getCharacter_code_string(), 0, 30);
											gd.drawImage(i, region.getRegion_horizontal_address()+regionObject.getObject_horizontal_position()+x_offset, region.getRegion_vertical_address()+regionObject.getObject_vertical_position()+y_offset, null);
										}
									}
								}
							}
						}
					}
				}
			}
			return img;
		}
		return null;
	}

	/**
	 * @param regionCompositionSegment
	 * @return
	 */
	public static int getPixelCode(RegionCompositionSegment regionCompositionSegment) {
		int index = switch (regionCompositionSegment.getRegionDepth()) {
            case 1 -> regionCompositionSegment.getRegion2BitPixelCode(); // 2 bit
            case 2 -> regionCompositionSegment.getRegion4BitPixelCode(); // 4 bit. TODO  if the region depth is 8 bit while the region_level_of_compatibility specifies that a 4-bit CLUT is within the minimum requirements. ETSI EN 300 743 V1.3.1 (2006-11) P 25 bottom.
            case 3 -> regionCompositionSegment.getRegion8BitPixelCode();
            default -> 0;
        };
        return index;
	}

	/**
	 * @return
	 */
	public List<Segment> getSegmentList() {
		return segmentList;
	}

	public int getData_identifier() {
		return data_identifier;
	}

	public int getSubtitle_stream_id() {
		return subtitle_stream_id;
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getSegmentTypeString(int type) {

		if ((0x40 <= type) && (type <= 0x7f)) {
			return "reserved for future use";
		}
		if ((0x81 <= type) && (type <= 0xef)) {
			return "private data";
		}
		return switch (type) {
		case 0x10 -> "page composition segment";
		case 0x11 -> "region composition segment";
		case 0x12 -> "CLUT definition segment";
		case 0x13 -> "object data segment";
		case 0x14 -> "display definition segment";
		case 0x15 -> "disparity signalling segment";
		case 0x16 -> "alternative_CLUT_segment";
		case 0x80 -> "end of display set segment";
		case 0xFF -> "stuffing";
		default -> "reserved for future use";
		};
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getPageStateString(int type) {

		return switch (type) {
		case 0x0 -> "normal case - The display set contains only the subtitle elements that are changed from the previous page instance.";
		case 0x1 -> "acquisition point - The display set contains all subtitle elements needed to display the next page instance.";
		case 0x2 -> "mode change - The display set contains all subtitle elements needed to display the new page.";
		case 0x3 -> "reserved";
		default -> "Illegal value";
		};
	}

}
