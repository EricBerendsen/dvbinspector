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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class VideoStreamDescriptor extends Descriptor {

	private int multipleFrameRateFlag;
	private int frameRateCode;
	private int mpeg1OnlyFlag;
	private int constrainedParameterFlag;
	private int stillPictureFlag;

	private int profileAndLevelIndication;
	private int chromaFormat;
	private int frameRateExtensionFlag;
	private int reserved;

	public VideoStreamDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		multipleFrameRateFlag = Utils.getInt(b, 2, 1, 0x80) >> 7;
		frameRateCode = Utils.getInt(b, 2, 1, 0x78) >> 3;
		mpeg1OnlyFlag = Utils.getInt(b, 2, 1, 0x04) >> 2;
		constrainedParameterFlag = Utils.getInt(b, 2, 1, 0x02) >> 1;
		stillPictureFlag = Utils.getInt(b, 2, 1, 0x01);
		if (mpeg1OnlyFlag == 0) {
			profileAndLevelIndication = Utils.getInt(b, 3, 1, 0xFF);
			chromaFormat = Utils.getInt(b, 4, 1, 0xC0) >> 6;
			frameRateExtensionFlag = Utils.getInt(b, 4, 1, 0x20) >> 5;
			reserved = Utils.getInt(b, 4, 1, 0x1F);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " multipleFrameRateFlag"+multipleFrameRateFlag ;
	}

	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("multiple_frame_rate_flag",multipleFrameRateFlag ,getMultipleFrameRateFlagString(multipleFrameRateFlag)));
		t.add(new KVP("frame_rate_code",frameRateCode ,getFrameRateString(frameRateCode)));
		t.add(new KVP("MPEG_1_only_flag",mpeg1OnlyFlag ,getMPEG_1_only_flagString(mpeg1OnlyFlag)));
		t.add(new KVP("constrained_parameter_flag",constrainedParameterFlag ,getConstrainedParameterFlagString(constrainedParameterFlag)));
		t.add(new KVP("still_picture_flag",stillPictureFlag ,getStillPictureFlagString(stillPictureFlag)));

		if(mpeg1OnlyFlag ==0){
			t.add(new KVP("profile_and_level_indication",profileAndLevelIndication ,getVideoProfileString(profileAndLevelIndication)+ ", "+getVideoLevelString(profileAndLevelIndication)));
			t.add(new KVP("chroma_format",chromaFormat ,getChromaFormatString(chromaFormat)));
			t.add(new KVP("frame_rate_extension_flag",frameRateExtensionFlag));
			t.add(new KVP("reserved",reserved));
		}

		return t;
	}

	public int getMultipleFrameRateFlag() {
		return multipleFrameRateFlag;
	}

	public static String getMultipleFrameRateFlagString(int multipleFrameRateFlag) {
		if(multipleFrameRateFlag==1){
			return "multiple frame rates may be present";
		}
		return "single frame rate is present";
	}

	public static String getMPEG_1_only_flagString(int mpeg1OnlyFlag) {
		if(mpeg1OnlyFlag==1){
			return "video stream contains only ISO/IEC 11172-2 data";
		}
		return "video stream may contain both ITU-T H.262 | ISO/IEC 13818-2 video data and constrained parameter ISO/IEC 11172-2 video data";
	}

	public static String getConstrainedParameterFlagString(int constrainedParameterFlag) {
		if(constrainedParameterFlag==1){
			return "video stream shall not contain unconstrained ISO/IEC 11172-2 video data";
		}
		return "video stream may contain both constrained parameters and unconstrained ISO/IEC 11172-2 video streams";
	}

	public static String getStillPictureFlagString(int stillPictureFlag) {
		if(stillPictureFlag==1){
			return "video stream contains only still pictures";
		}
		return "video stream may contain either moving or still picture data";
	}

	public int getMpeg1OnlyFlag() {
		return mpeg1OnlyFlag;
	}

	public int getFrameRateCode() {
		return frameRateCode;
	}

	public int getChromaFormat() {
		return chromaFormat;
	}

	public int getConstrainedParameterFlag() {
		return constrainedParameterFlag;
	}

	public int getFrameRateExtensionFlag() {
		return frameRateExtensionFlag;
	}

	public int getProfileAndLevelIndication() {
		return profileAndLevelIndication;
	}

	public int getReserved() {
		return reserved;
	}

	public int getStillPictureFlag() {
		return stillPictureFlag;
	}

	public static String getFrameRateString(int frameRateCode) {
		return switch (frameRateCode) {
		case 0 -> "forbidden";
		case 1 -> "24000÷1001 (23,976...)";
		case 2 -> "24 or 23,976";
		case 3 -> "25";
		case 4 -> "30 000÷1001 (29,97...) or 23,976";
		case 5 -> "30 or 23,976 24,0 29,97";
		case 6 -> "50 or 25,0";
		case 7 -> "60 000÷1001 (59,94...) or 23,976 29,97";
		case 8 -> "60 or 23,976 24,0 29,97 30,0 59,94";
		default -> "reserved";
		};
	}

	//based on si.c from tstool
	public static String getChromaFormatString(int code){

		return switch (code) {
		case 0 -> "forbidden";
		case 1 -> "4:2:0";
		case 2 -> "4:2:2";
		case 3 -> "4:4:4";
		default -> "illegal";
		};
		
	}

	//based on si.c from tstool
	/* "code" is 8 bits profile_and_level_indication */
	public static String getVideoProfileString(int  code){

		return switch ((code & 0x7f) >> 4) {
		case 0 -> "reserved profile";
		case 1 -> "high profile";
		case 2 -> "spatially scalable profile";
		case 3 -> "snr scalable profile";
		case 4 -> "main profile";
		case 5 -> "simple profile";
		default -> "";
		};
	}

	//	based on si.c from tstool
	/* "code" is 8 bits profile_and_level_indication */
	public static String  getVideoLevelString(int code){

		return switch (code & 0x0f) {
		case 4 -> "high level";
		case 6 -> "high 1440 level";
		case 8 -> "main level";
		case 10 -> "low level";
		default -> "reserved level";
		};

	}

}
