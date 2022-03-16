/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;

import static java.lang.Byte.toUnsignedInt;
import static nl.digitalekabeltelevisie.data.mpeg.pes.video.ExtensionHeader.*;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 *
 */

@JsonIgnoreProperties({"pesHandler","image"})
public class VideoPESDataField extends PesPacketData implements TreeNode, ImageSource {


	private final List<VideoMPEG2Section> sections= new ArrayList<VideoMPEG2Section>();

	private static final Logger logger = Logger.getLogger(VideoPESDataField.class.getName());

	/**
	 * Creates a new VideoPESDataField
	 * @param pesPacket
	 */

	public VideoPESDataField(final PesPacketData pesPacket) {
		super(pesPacket);

		int i = pesDataStart;
		while((i<(data.length))&&(i>=0)){
			i = indexOf(data, new byte[]{0,0,1},i);
			if(i>=0){
				VideoMPEG2Section section;
				if(toUnsignedInt(data[i+3])==0x00){
					section = new PictureHeader(data,i+3);
				}else if(toUnsignedInt(data[i+3])==0xB2){
					section = new UserData(data,i+3);
				}else if(toUnsignedInt(data[i+3])==0xB3){
					section = new SequenceHeader(data,i+3);
				}else if(toUnsignedInt(data[i+3])==0xB5){ // extension, use extension_start_code_identifier to make sub selection
					final int extensionStartCodeIdentifier = (toUnsignedInt(data[i+4])&0xF0)>>4;
					if(extensionStartCodeIdentifier==1){ // Sequence extension
						section = new SequenceExtension(data,i+3);
					}else if(extensionStartCodeIdentifier==8){ // Picture coding extension
						section = new PictureCodingExtension(data,i+3);
					}else if(extensionStartCodeIdentifier==2){ // Picture coding extension
						section = new SequenceDisplayExtension(data,i+3);
					}else{
						section = new ExtensionHeader(data,i+3); // default Base Extension
						logger.warning("Not implemented extendsion start code identifier:"+extensionStartCodeIdentifier+" ("+getExtensionStartCodeIdentifierString(extensionStartCodeIdentifier)+")");
					}
				}else if(toUnsignedInt(data[i+3])==0xB8){
					section = new GroupOfPicturesHeader(data,i+3);
				}else{
					section = new VideoMPEG2Section(data,i+3);
				}
				sections.add(section);
				i+=3;
			}
		}
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final List<VideoMPEG2Section> picts = findSectionInList(sections, 0);
		final StringBuilder type = new StringBuilder();
		if((picts!=null)&&(picts.size()>0)){
			type.append(" (Pictures ");
			for(final VideoMPEG2Section section: picts) {
				type.append(((PictureHeader)section).getPictureCodingTypeShortString());
			}
			type.append(")");
		}
		final DefaultMutableTreeNode s = super.getJTreeNode(modus,new KVP("Video PES Packet"+type,this));
		addListJTree(s,sections,modus,"Sections");
		return s;
	}



	public static String getStartCodeString(final int startCode) {
		if ((startCode >= 1) && (startCode <= 0xAF)) {
			return "slice_start_code ";
		}
		switch (startCode) {
		case 0x00:
			return "picture_start_code";
		case 0xB0:
			return "reserved";
		case 0xB1:
			return "reserved";
		case 0xB2:
			return "user_data_start_code";
		case 0xB3:
			return "sequence_header_code";
		case 0xB4:
			return "sequence_error_code";
		case 0xB5:
			return "extension_start_code";
		case 0xB6:
			return "reserved";
		case 0xB7:
			return "sequence_end_code";
		case 0xB8:
			return "group_start_code";
		case 0xB9:
			return "MPEG_program_stream_end (PS)";
		case 0xBA:
			return "MPEG_pack_start (PS)";
		case 0xBB:
			return "MPEG_system_header_start (PS)";
		default:
			return "unknown/error";
		}
	}

	public static String getSectionTypeString(final int startCode) {
		if ((startCode >= 1) && (startCode <= 0xAF)) {
			return "Slice " + startCode;
		}
		switch (startCode) {
		case 0x00:
			return "Picture header";
		case 0xB0:
			return "reserved";
		case 0xB1:
			return "reserved";
		case 0xB2:
			return "User Data";
		case 0xB3:
			return "Sequence header";
		case 0xB4:
			return "sequence_error_code";
		case 0xB5:
			return "Extension data";
		case 0xB6:
			return "reserved";
		case 0xB7:
			return "sequence_end_code";
		case 0xB8:
			return "Group Of Pictures";
		case 0xB9:
			return "MPEG_program_stream_end (PS)";
		case 0xBA:
			return "MPEG_pack_start (PS)";
		case 0xBB:
			return "MPEG_system_header_start (PS)";
		default:
			return "unknown/error";
		}
	}

	/**
	 * @param sectionList List of  sections to be searched
	 * @param startCode code of target section
	 * @return List of all sections matching startCode
	 */
	public static List<VideoMPEG2Section> findSectionInList(final List<VideoMPEG2Section> sectionList, final int startCode) {

		final List<VideoMPEG2Section> result = new ArrayList<VideoMPEG2Section>();
		for (final VideoMPEG2Section element : sectionList) {
			if (element.getStartCode() == startCode) {
				result.add(element);
			}
		}
		return result;
	}


	/**
	 * Returns an unmodifiable list of the VideoMPEG2Sections in this PESPacket
	 * @return the VideoMPEG2Sections
	 */
	public List<VideoMPEG2Section> getSections() {
		return sections;
	}


	/**
	 * If this VideoPESDataField contains a PictureHeader with picture_coding_type = "I"
	 * tries to create an image from this I-Frame. If the VideoPESDataField does not contain all slices for the frame, the image will not be complete.
	 *
	 * The image has the exact dimensions of the source I-Frame, so aspect ratio may be incorrect.
	 *
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	@Override
	public BufferedImage getImage() {

		if(hasIFrame()){
			MpvDecoder mpvDecoder = new MpvDecoder();
			mpvDecoder.decodeArray(data, false, false, false, 0);

			return mpvDecoder.getImage();
		}else{
			return null;
		}

	}


	/**
	 * If this VideoPESDataField contains a PictureHeader with picture_coding_type = "I"
	 * tries to create an image from this I-Frame. If the VideoPESDataField does not contain all slices for the frame, the image will not be complete.
	 *
	 * This is used to give a real background for subtitles. Caller must set width and height, to make sure aspect ratio is as desired.
	 *
	 * @param w width
	 * @param h height
	 * @return
	 */
	public BufferedImage getImage(int w, int h) {

		if(hasIFrame()){
			MpvDecoder mpvDecoder = new MpvDecoder();
			mpvDecoder.decodeArray(data, false, false, false, 0);

			return mpvDecoder.getImage(w,h);
		}else{
			return null;
		}

	}

	/**
	 * Determine whether this if VideoPESDataField contains at least a PictureHeader with Picture_coding_type== "I".
	 * Can have more, can also contain other types of PictureHeader.
	 * <br>
	 * <b>Does not imply this VideoPESDataField contains a complete I-Frame.</b>
	 *
	 *
	 * @return true if VideoPESDataField contains PictureHeader with Picture_coding_type== "I", else false
	 */
	public boolean hasIFrame() {

		final List<VideoMPEG2Section> picts = findSectionInList(sections, 0);
		if((picts!=null)&&(picts.size()>0)){
			for(final VideoMPEG2Section section: picts) {
				if(((PictureHeader)section).getPicture_coding_type()==1){
					return true;
				}
			}
		}

		return false;
	}


}
