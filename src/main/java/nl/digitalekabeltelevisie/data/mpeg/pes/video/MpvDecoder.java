/* Copyright (C) 1996, MPEG Software Simulation Group. All Rights Reserved. */

/*
 * Disclaimer of Warranty
 *
 * These software programs are available to the user without any license fee or
 * royalty on an "as is" basis.  The MPEG Software Simulation Group disclaims
 * any and all warranties, whether express, implied, or statuary, including any
 * implied warranties or merchantability or of fitness for a particular
 * purpose.  In no event shall the copyright-holder be liable for any
 * incidental, punitive, or consequential damages of any kind whatsoever
 * arising from the use of these programs.
 *
 * This disclaimer of warranty extends to the user of these programs and user's
 * customers, employees, agents, transferees, successors, and assigns.
 *
 * The MPEG Software Simulation Group does not represent or warrant that the
 * programs furnished hereunder are free of infringement of any third-party
 * patents.
 *
 * Commercial implementations of MPEG-1 and MPEG-2 video, including shareware,
 * are subject to royalty fees to patent holders.  Many of these patents are
 * general enough such that they are unavoidable regardless of implementation
 * design.
 *
 */


/*
 * @(#)MpvDecoder.java - still Picture Decoder
 *
 * Copyright (c) 2003-2010 by dvb.matt, All Rights Reserved.
 *
 * This file is part of ProjectX, a free Java based demux utility.
 * By the authors, ProjectX is intended for educational purposes only,
 * as a non-commercial test project.
 *
 * necessary codes are derived from the MSSG mpeg2dec
 *
 * display modifications: shows I-Frames only
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

/**
 *
 *  This class is based on MpvDecoder.java of ProjectX (see above), which in itself is based on
 *  mpeg2dec.c and other sources, Copyright (C) 1996, MPEG Software Simulation Group.
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;


import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class MpvDecoder extends Object {


	private int[] pixels = new int[250]; //full pixel data

	private int Fault_Flag = 0;
	private int BitPos = 0;
	private int BufferPos = 0;
	private int YGain = 0;

	private long StartPos = 0;

	private boolean FAST = false;
	private boolean DIRECTION = false;
	private boolean ERROR1 = false;
	private boolean ERROR2 = false;
	private boolean ERROR3 = false;
	private boolean ERROR4 = false;
	private boolean ERROR5 = false;
	private boolean ERROR6 = false;
	private boolean viewGOP = true;

	private final List PositionList = new ArrayList();

	private byte[] buf = new byte[0];

	private final int[] LastPosVal = new int[2];

	/**
	 * integer matrix by dukios
	 */

	static int ref_dct_matrix_i[] = new int[64];

	/**
	 *
	 */
	public MpvDecoder()
	{

	}



	private final static int PICTURE_START_CODE=0x100;
	private final static int SLICE_START_CODE_MIN=0x101;
	private final static int SLICE_START_CODE_MAX=0x1AF;
	private final static int USER_DATA_START_CODE=0x1B2;
	private final static int SEQUENCE_HEADER_CODE=0x1B3;
	private final static int EXTENSION_START_CODE=0x1B5;
	private final static int GROUP_START_CODE=0x1B8;
	private int ERROR_CODE1=0;

	/* extension start code IDs */
	private final static int SEQUENCE_EXTENSION_ID=1;
	private final static int SEQUENCE_DISPLAY_EXTENSION_ID=2;
	private final static int QUANT_MATRIX_EXTENSION_ID=3;
	private final static int COPYRIGHT_EXTENSION_ID=4;
	private final static int PICTURE_DISPLAY_EXTENSION_ID=7;
	private final static int PICTURE_CODING_EXTENSION_ID=8;
	private final static int ZIG_ZAG=0;
	private final static int MC_FIELD=1;
	private final static int MC_FRAME=2;
	private final static int MC_16X8=2;
	private final static int MC_DMV=3;
	private final static int MV_FIELD=0;
	private final static int MV_FRAME=1;
	private final static int I_TYPE=1;
	private final static int P_TYPE=2;
	private final static int B_TYPE=3;
	private final static int BOTTOM_FIELD=2;
	private final static int FRAME_PICTURE=3;
	private final static int MACROBLOCK_INTRA=1;
	private final static int MACROBLOCK_PATTERN=2;
	private final static int MACROBLOCK_MOTION_BACKWARD=4;
	private final static int MACROBLOCK_MOTION_FORWARD=8;
	private final static int MACROBLOCK_QUANT=16;
	private final static int CHROMA420=1;
	private final static int CHROMA422=2;
	private final static int CHROMA444=3;
	private final static int IDCT_CLIP_TABLE_OFFSET=512;

	private int q_scale_type=0;  //1
	private int quantizer_scale=0, alternate_scan=0;//1
	private int Coded_Picture_Width=0, Coded_Picture_Height=0, Chroma_Width=0;
	private int block_count=0, Second_Field=0;
	private int horizontal_size=0, vertical_size=0, mb_width=0, mb_height=0;

	/* ISO/IEC 13818-2 section 6.2.2.1:  sequence_header() */
	private int frame_rate_code=0;
	private int aspect_ratio_information=0;

	/* ISO/IEC 13818-2 section 6.2.2.3:  sequence_extension() */
	private int progressive_sequence=1;  //prog.s std
	private int chroma_format=1;  //4:2:0std
	private int profile_and_level_indication;
	private int video_format;
	/* ISO/IEC 13818-2 section 6.2.3: picture_header() */
	private int picture_coding_type=0;
	private int temporal_reference=0;

	/* ISO/IEC 13818-2 section 6.2.3.1: picture_coding_extension() header */
	private final int f_code[][] = new int[2][2];
	private int picture_structure=3;  //0
	private int frame_pred_frame_dct=1; //0
	private int progressive_frame=1;  //0
	private int concealment_motion_vectors=0;
	private int intra_dc_precision=0; //8bit
	private int top_field_first=0;
	private int repeat_first_field=0;
	private int intra_vlc_format=0; //

	private final int intra_quantizer_matrix[] = new int[64];
	private final int non_intra_quantizer_matrix[] = new int[64];
	private final int chroma_intra_quantizer_matrix[] = new int[64];
	private final int chroma_non_intra_quantizer_matrix[] = new int[64];

	private int load_intra_quantizer_matrix=0;
	private int load_non_intra_quantizer_matrix=0;
	private int load_chroma_intra_quantizer_matrix=0;
	private int load_chroma_non_intra_quantizer_matrix=0;

	private final short block[][]=new short[12][64]; //macroblocks

	public static final String picture_coding_type_string[] = {
		"bad","I","P","B","D"
	};

	public static final String progressive_string[] = {
		"i","p"
	};

	public static final String aspect_ratio_string[] = {
		"bad","1:1","4:3","16:9","2.21:1","0.8055","0.8437","0.9375","0.9815","1.0255","1.0695","1.1250","1.1575","1.2015"
	};

	/* cosine transform matrix for 8x1 IDCT */

	public static final float ref_dct_matrix[][] = {
		{    // [0][0-7]
			3.5355339059327379e-001f,  3.5355339059327379e-001f,
			3.5355339059327379e-001f,  3.5355339059327379e-001f,
			3.5355339059327379e-001f,  3.5355339059327379e-001f,
			3.5355339059327379e-001f,  3.5355339059327379e-001f,
		}, { // [1][0-7]
			4.9039264020161522e-001f,  4.1573480615127262e-001f,
			2.7778511650980114e-001f,  9.7545161008064166e-002f,
			-9.7545161008064096e-002f, -2.7778511650980098e-001f,
			-4.1573480615127267e-001f, -4.9039264020161522e-001f,
		}, { // [2][0-7]
			4.6193976625564337e-001f,  1.9134171618254492e-001f,
			-1.9134171618254486e-001f, -4.6193976625564337e-001f,
			-4.6193976625564342e-001f, -1.9134171618254517e-001f,
			1.9134171618254500e-001f,  4.6193976625564326e-001f,
		}, { // [3][0-7]
			4.1573480615127262e-001f, -9.7545161008064096e-002f,
			-4.9039264020161522e-001f, -2.7778511650980109e-001f,
			2.7778511650980092e-001f,  4.9039264020161522e-001f,
			9.7545161008064388e-002f, -4.1573480615127256e-001f,
		}, { // [4][0-7]
			3.5355339059327379e-001f, -3.5355339059327373e-001f,
			-3.5355339059327384e-001f,  3.5355339059327368e-001f,
			3.5355339059327384e-001f, -3.5355339059327334e-001f,
			-3.5355339059327356e-001f,  3.5355339059327329e-001f,
		}, { // [5][0-7]
			2.7778511650980114e-001f, -4.9039264020161522e-001f,
			9.7545161008064152e-002f,  4.1573480615127273e-001f,
			-4.1573480615127256e-001f, -9.7545161008064013e-002f,
			4.9039264020161533e-001f, -2.7778511650980076e-001f,
		}, { // [6][0-7]
			1.9134171618254492e-001f, -4.6193976625564342e-001f,
			4.6193976625564326e-001f, -1.9134171618254495e-001f,
			-1.9134171618254528e-001f,  4.6193976625564337e-001f,
			-4.6193976625564320e-001f,  1.9134171618254478e-001f,
		}, { // [7][0-7]
			9.7545161008064166e-002f, -2.7778511650980109e-001f,
			4.1573480615127273e-001f, -4.9039264020161533e-001f,
			4.9039264020161522e-001f, -4.1573480615127251e-001f,
			2.7778511650980076e-001f, -9.7545161008064291e-002f,
		},
	};

	/**/
	//dukios
	static {
		for(int i = 0; i < 8; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				ref_dct_matrix_i[(i * 8) + j] = Math.round(ref_dct_matrix[i][j] * 65536.0f);
			}
		}
	}
	/**/

	final static short idct_clip_table[] = {
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-256,-256,-256,-256,-256,-256,-256,
		-256,-255,-254,-253,-252,-251,-250,-249,
		-248,-247,-246,-245,-244,-243,-242,-241,
		-240,-239,-238,-237,-236,-235,-234,-233,
		-232,-231,-230,-229,-228,-227,-226,-225,
		-224,-223,-222,-221,-220,-219,-218,-217,
		-216,-215,-214,-213,-212,-211,-210,-209,
		-208,-207,-206,-205,-204,-203,-202,-201,
		-200,-199,-198,-197,-196,-195,-194,-193,
		-192,-191,-190,-189,-188,-187,-186,-185,
		-184,-183,-182,-181,-180,-179,-178,-177,
		-176,-175,-174,-173,-172,-171,-170,-169,
		-168,-167,-166,-165,-164,-163,-162,-161,
		-160,-159,-158,-157,-156,-155,-154,-153,
		-152,-151,-150,-149,-148,-147,-146,-145,
		-144,-143,-142,-141,-140,-139,-138,-137,
		-136,-135,-134,-133,-132,-131,-130,-129,
		-128,-127,-126,-125,-124,-123,-122,-121,
		-120,-119,-118,-117,-116,-115,-114,-113,
		-112,-111,-110,-109,-108,-107,-106,-105,
		-104,-103,-102,-101,-100, -99, -98, -97,
		-96, -95, -94, -93, -92, -91, -90, -89,
		-88, -87, -86, -85, -84, -83, -82, -81,
		-80, -79, -78, -77, -76, -75, -74, -73,
		-72, -71, -70, -69, -68, -67, -66, -65,
		-64, -63, -62, -61, -60, -59, -58, -57,
		-56, -55, -54, -53, -52, -51, -50, -49,
		-48, -47, -46, -45, -44, -43, -42, -41,
		-40, -39, -38, -37, -36, -35, -34, -33,
		-32, -31, -30, -29, -28, -27, -26, -25,
		-24, -23, -22, -21, -20, -19, -18, -17,
		-16, -15, -14, -13, -12, -11, -10,  -9,
		-8,  -7,  -6,  -5,  -4,  -3,  -2,  -1,
		0,   1,   2,   3,   4,   5,   6,   7,
		8,   9,  10,  11,  12,  13,  14,  15,
		16,  17,  18,  19,  20,  21,  22,  23,
		24,  25,  26,  27,  28,  29,  30,  31,
		32,  33,  34,  35,  36,  37,  38,  39,
		40,  41,  42,  43,  44,  45,  46,  47,
		48,  49,  50,  51,  52,  53,  54,  55,
		56,  57,  58,  59,  60,  61,  62,  63,
		64,  65,  66,  67,  68,  69,  70,  71,
		72,  73,  74,  75,  76,  77,  78,  79,
		80,  81,  82,  83,  84,  85,  86,  87,
		88,  89,  90,  91,  92,  93,  94,  95,
		96,  97,  98,  99, 100, 101, 102, 103,
		104, 105, 106, 107, 108, 109, 110, 111,
		112, 113, 114, 115, 116, 117, 118, 119,
		120, 121, 122, 123, 124, 125, 126, 127,
		128, 129, 130, 131, 132, 133, 134, 135,
		136, 137, 138, 139, 140, 141, 142, 143,
		144, 145, 146, 147, 148, 149, 150, 151,
		152, 153, 154, 155, 156, 157, 158, 159,
		160, 161, 162, 163, 164, 165, 166, 167,
		168, 169, 170, 171, 172, 173, 174, 175,
		176, 177, 178, 179, 180, 181, 182, 183,
		184, 185, 186, 187, 188, 189, 190, 191,
		192, 193, 194, 195, 196, 197, 198, 199,
		200, 201, 202, 203, 204, 205, 206, 207,
		208, 209, 210, 211, 212, 213, 214, 215,
		216, 217, 218, 219, 220, 221, 222, 223,
		224, 225, 226, 227, 228, 229, 230, 231,
		232, 233, 234, 235, 236, 237, 238, 239,
		240, 241, 242, 243, 244, 245, 246, 247,
		248, 249, 250, 251, 252, 253, 254, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
		255, 255, 255, 255, 255, 255, 255, 255,
	};

	public final static byte cc_table[] = {
		0, 0, 0, 0, 1, 2, 1, 2, 1, 2, 1, 2
	};

	public final static int ChromaFormat[] = {
		0, 6, 8, 12
	};

	/* non-linear quantization coefficient table */
	public final static byte Non_Linear_quantizer_scale[] = {
		0, 1, 2, 3, 4, 5, 6, 7,
		8, 10, 12, 14, 16, 18, 20, 22,
		24, 28, 32, 36, 40, 44, 48, 52,
		56, 64, 72, 80, 88, 96, 104, 112
	};

	public final static byte MBAtab1[][] = {  //VLCtab val,len
		{-1,0}, {-1,0}, {7,5}, {6,5}, {5,4}, {5,4}, {4,4},
		{4,4}, {3,3}, {3,3}, {3,3}, {3,3}, {2,3}, {2,3}, {2,3}, {2,3}
	};

	/* Table B-1, macroblock_address_increment, codes 00000011000 ... 0000111xxxx */
	public final static byte MBAtab2[][] = {  //VLCtab val,len
		{33,11}, {32,11}, {31,11}, {30,11}, {29,11}, {28,11}, {27,11}, {26,11},
		{25,11}, {24,11}, {23,11}, {22,11}, {21,10}, {21,10}, {20,10}, {20,10},
		{19,10}, {19,10}, {18,10}, {18,10}, {17,10}, {17,10}, {16,10}, {16,10},
		{15,8},  {15,8},  {15,8},  {15,8},  {15,8},  {15,8},  {15,8},  {15,8},
		{14,8},  {14,8},  {14,8},  {14,8},  {14,8},  {14,8},  {14,8},  {14,8},
		{13,8},  {13,8},  {13,8},  {13,8},  {13,8},  {13,8},  {13,8},  {13,8},
		{12,8},  {12,8},  {12,8},  {12,8},  {12,8},  {12,8},  {12,8},  {12,8},
		{11,8},  {11,8},  {11,8},  {11,8},  {11,8},  {11,8},  {11,8},  {11,8},
		{10,8},  {10,8},  {10,8},  {10,8},  {10,8},  {10,8},  {10,8},  {10,8},
		{9,7},   {9,7},   {9,7},   {9,7},   {9,7},   {9,7},   {9,7},   {9,7},
		{9,7},   {9,7},   {9,7},   {9,7},   {9,7},   {9,7},   {9,7},   {9,7},
		{8,7},   {8,7},   {8,7},   {8,7},   {8,7},   {8,7},   {8,7},   {8,7},
		{8,7},   {8,7},   {8,7},   {8,7},   {8,7},   {8,7},   {8,7},   {8,7}
	};


	/* default intra quantization matrix */
	public final static int default_intra_quantizer_matrix[] = {
		8, 16, 19, 22, 26, 27, 29, 34,
		16, 16, 22, 24, 27, 29, 34, 37,
		19, 22, 26, 27, 29, 34, 34, 38,
		22, 22, 26, 27, 29, 34, 37, 40,
		22, 26, 27, 29, 32, 35, 40, 48,
		26, 27, 29, 32, 35, 40, 48, 58,
		26, 27, 29, 34, 38, 46, 56, 69,
		27, 29, 35, 38, 46, 56, 69, 83
	};

	/* zig-zag and alternate scan patterns */
	public final static byte scan[][] = {
		{ /* Zig-Zag scan pattern  */
			0,  1,  8, 16,  9,  2,  3, 10,
			17, 24, 32, 25, 18, 11,  4,  5,
			12, 19, 26, 33, 40, 48, 41, 34,
			27, 20, 13,  6,  7, 14, 21, 28,
			35, 42, 49, 56, 57, 50, 43, 36,
			29, 22, 15, 23, 30, 37, 44, 51,
			58, 59, 52, 45, 38, 31, 39, 46,
			53, 60, 61, 54, 47, 55, 62, 63
		}
		,
		{ /* Alternate scan pattern */
			0,  8, 16, 24,  1,  9,  2, 10,
			17, 25, 32, 40, 48, 56, 57, 49,
			41, 33, 26, 18,  3, 11, 4,  12,
			19, 27, 34, 42, 50, 58, 35, 43,
			51, 59, 20, 28,  5, 13,  6, 14,
			21, 29, 36, 44, 52, 60, 37, 45,
			53, 61, 22, 30,  7, 15, 23, 31,
			38, 46, 54, 62, 39, 47, 55, 63
		}
	};


	/* Table B-14, DCT coefficients table zero,
	 * codes 000001xx ... 00111xxx  */
	public final static byte DCTtab0[][] = {
		{65,0,6}, {65,0,6}, {65,0,6}, {65,0,6}, /* Escape */
		{2,2,7}, {2,2,7}, {9,1,7}, {9,1,7},
		{0,4,7}, {0,4,7}, {8,1,7}, {8,1,7},
		{7,1,6}, {7,1,6}, {7,1,6}, {7,1,6},
		{6,1,6}, {6,1,6}, {6,1,6}, {6,1,6},
		{1,2,6}, {1,2,6}, {1,2,6}, {1,2,6},
		{5,1,6}, {5,1,6}, {5,1,6}, {5,1,6},
		{13,1,8}, {0,6,8}, {12,1,8}, {11,1,8},
		{3,2,8}, {1,3,8}, {0,5,8}, {10,1,8},
		{0,3,5}, {0,3,5}, {0,3,5}, {0,3,5},
		{0,3,5}, {0,3,5}, {0,3,5}, {0,3,5},
		{4,1,5}, {4,1,5}, {4,1,5}, {4,1,5},
		{4,1,5}, {4,1,5}, {4,1,5}, {4,1,5},
		{3,1,5}, {3,1,5}, {3,1,5}, {3,1,5},
		{3,1,5}, {3,1,5}, {3,1,5}, {3,1,5}
	};

	/* Table B-15, DCT coefficients table one,
	 * codes 000001xx ... 11111111 */
	public final static byte DCTtab0a[][] = {
		{65,0,6}, {65,0,6}, {65,0,6}, {65,0,6}, /* Escape */
		{7,1,7}, {7,1,7}, {8,1,7}, {8,1,7},
		{6,1,7}, {6,1,7}, {2,2,7}, {2,2,7},
		{0,7,6}, {0,7,6}, {0,7,6}, {0,7,6},
		{0,6,6}, {0,6,6}, {0,6,6}, {0,6,6},
		{4,1,6}, {4,1,6}, {4,1,6}, {4,1,6},
		{5,1,6}, {5,1,6}, {5,1,6}, {5,1,6},
		{1,5,8}, {11,1,8}, {0,11,8}, {0,10,8},
		{13,1,8}, {12,1,8}, {3,2,8}, {1,4,8},
		{2,1,5}, {2,1,5}, {2,1,5}, {2,1,5},
		{2,1,5}, {2,1,5}, {2,1,5}, {2,1,5},
		{1,2,5}, {1,2,5}, {1,2,5}, {1,2,5},
		{1,2,5}, {1,2,5}, {1,2,5}, {1,2,5},
		{3,1,5}, {3,1,5}, {3,1,5}, {3,1,5},
		{3,1,5}, {3,1,5}, {3,1,5}, {3,1,5},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{1,1,3}, {1,1,3}, {1,1,3}, {1,1,3},
		{64,0,4}, {64,0,4}, {64,0,4}, {64,0,4}, /* EOB */
		{64,0,4}, {64,0,4}, {64,0,4}, {64,0,4},
		{64,0,4}, {64,0,4}, {64,0,4}, {64,0,4},
		{64,0,4}, {64,0,4}, {64,0,4}, {64,0,4},
		{0,3,4}, {0,3,4}, {0,3,4}, {0,3,4},
		{0,3,4}, {0,3,4}, {0,3,4}, {0,3,4},
		{0,3,4}, {0,3,4}, {0,3,4}, {0,3,4},
		{0,3,4}, {0,3,4}, {0,3,4}, {0,3,4},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,1,2}, {0,1,2}, {0,1,2}, {0,1,2},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,2,3}, {0,2,3}, {0,2,3}, {0,2,3},
		{0,4,5}, {0,4,5}, {0,4,5}, {0,4,5},
		{0,4,5}, {0,4,5}, {0,4,5}, {0,4,5},
		{0,5,5}, {0,5,5}, {0,5,5}, {0,5,5},
		{0,5,5}, {0,5,5}, {0,5,5}, {0,5,5},
		{9,1,7}, {9,1,7}, {1,3,7}, {1,3,7},
		{10,1,7}, {10,1,7}, {0,8,7}, {0,8,7},
		{0,9,7}, {0,9,7}, {0,12,8}, {0,13,8},
		{2,3,8}, {4,2,8}, {0,14,8}, {0,15,8}
	};

	/* Table B-14, DCT coefficients table zero,
	 * codes 0000001000 ... 0000001111 */
	public final static byte DCTtab1[][] = {
		{16,1,10}, {5,2,10}, {0,7,10}, {2,3,10},
		{1,4,10}, {15,1,10}, {14,1,10}, {4,2,10}
	};

	/* Table B-15, DCT coefficients table one,
	 * codes 000000100x ... 000000111x */
	public final static byte DCTtab1a[][] = {
		{5,2,9}, {5,2,9}, {14,1,9}, {14,1,9},
		{2,4,10}, {16,1,10}, {15,1,9}, {15,1,9}
	};

	/* Table B-14/15, DCT coefficients table zero / one,
	 * codes 000000010000 ... 000000011111 */
	public final static byte DCTtab2[][] = {
		{0,11,12}, {8,2,12}, {4,3,12}, {0,10,12},
		{2,4,12}, {7,2,12}, {21,1,12}, {20,1,12},
		{0,9,12}, {19,1,12}, {18,1,12}, {1,5,12},
		{3,3,12}, {0,8,12}, {6,2,12}, {17,1,12}
	};

	/* Table B-14/15, DCT coefficients table zero / one,
	 * codes 0000000010000 ... 0000000011111 */
	public final static byte DCTtab3[][] = {
		{10,2,13}, {9,2,13}, {5,3,13}, {3,4,13},
		{2,5,13}, {1,7,13}, {1,6,13}, {0,15,13},
		{0,14,13}, {0,13,13}, {0,12,13}, {26,1,13},
		{25,1,13}, {24,1,13}, {23,1,13}, {22,1,13}
	};

	/* Table B-14/15, DCT coefficients table zero / one,
	 * codes 00000000010000 ... 00000000011111 */
	public final static byte DCTtab4[][] = {
		{0,31,14}, {0,30,14}, {0,29,14}, {0,28,14},
		{0,27,14}, {0,26,14}, {0,25,14}, {0,24,14},
		{0,23,14}, {0,22,14}, {0,21,14}, {0,20,14},
		{0,19,14}, {0,18,14}, {0,17,14}, {0,16,14}
	};

	/* Table B-14/15, DCT coefficients table zero / one,
	 * codes 000000000010000 ... 000000000011111 */
	public final static byte DCTtab5[][] = {
		{0,40,15}, {0,39,15}, {0,38,15}, {0,37,15},
		{0,36,15}, {0,35,15}, {0,34,15}, {0,33,15},
		{0,32,15}, {1,14,15}, {1,13,15}, {1,12,15},
		{1,11,15}, {1,10,15}, {1,9,15}, {1,8,15}
	};

	/* Table B-14/15, DCT coefficients table zero / one,
	 * codes 0000000000010000 ... 0000000000011111 */
	public final static byte DCTtab6[][] = {  //DCTtab run,level,len
		{1,18,16}, {1,17,16}, {1,16,16}, {1,15,16},
		{6,3,16}, {16,2,16}, {15,2,16}, {14,2,16},
		{13,2,16}, {12,2,16}, {11,2,16}, {31,1,16},
		{30,1,16}, {29,1,16}, {28,1,16}, {27,1,16}
	};

	/* Table B-14, DCT coefficients table zero,
	 * codes 0100 ... 1xxx (used for first (DC) coefficient)  */
	public final static byte DCTtabfirst[][] = {
		{0,2,4}, {2,1,4}, {1,1,3}, {1,1,3},
		{0,1,1}, {0,1,1}, {0,1,1}, {0,1,1},
		{0,1,1}, {0,1,1}, {0,1,1}, {0,1,1}
	};

	/* Table B-14, DCT coefficients table zero,
	 * codes 0100 ... 1xxx (used for all other coefficients) */
	public final static byte DCTtabnext[][] = {
		{0,2,4},  {2,1,4},  {1,1,3},  {1,1,3},
		{64,0,2}, {64,0,2}, {64,0,2}, {64,0,2}, /* EOB */
		{0,1,2},  {0,1,2},  {0,1,2},  {0,1,2}
	};

	/* Table B-9, coded_block_pattern, codes 01000 ... 111xx */
	public final static byte CBPtab0[][] = {
		{-1,0}, {-1,0}, {-1,0}, {-1,0},	{-1,0}, {-1,0}, {-1,0}, {-1,0},
		{62,5}, {2,5},  {61,5}, {1,5},  {56,5}, {52,5}, {44,5}, {28,5},
		{40,5}, {20,5}, {48,5}, {12,5}, {32,4}, {32,4}, {16,4}, {16,4},
		{8,4},  {8,4},  {4,4},  {4,4},  {60,3}, {60,3}, {60,3}, {60,3}
	};

	/* Table B-9, coded_block_pattern, codes 00000100 ... 001111xx */
	public final static byte CBPtab1[][] = {
		{-1,0}, {-1,0}, {-1,0}, {-1,0},	{58,8}, {54,8}, {46,8}, {30,8},
		{57,8}, {53,8}, {45,8}, {29,8}, {38,8}, {26,8}, {37,8}, {25,8},
		{43,8}, {23,8}, {51,8}, {15,8}, {42,8}, {22,8}, {50,8}, {14,8},
		{41,8}, {21,8}, {49,8}, {13,8}, {35,8}, {19,8}, {11,8}, {7,8},
		{34,7}, {34,7}, {18,7}, {18,7}, {10,7}, {10,7}, {6,7},  {6,7},
		{33,7}, {33,7}, {17,7}, {17,7}, {9,7},  {9,7},  {5,7},  {5,7},
		{63,6}, {63,6}, {63,6}, {63,6}, {3,6},  {3,6},  {3,6},  {3,6},
		{36,6}, {36,6}, {36,6}, {36,6}, {24,6}, {24,6}, {24,6}, {24,6}
	};

	/* Table B-9, coded_block_pattern, codes 000000001 ... 000000111 */
	public final static byte CBPtab2[][] = {
		{-1,0}, {0,9}, {39,9}, {27,9}, {59,9}, {55,9}, {47,9}, {31,9}
	};

	/* Table B-12, dct_dc_size_luminance, codes 00xxx ... 11110 */
	public final static byte DClumtab0[][] = {
		{1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2},
		{2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2},
		{0, 3}, {0, 3}, {0, 3}, {0, 3}, {3, 3}, {3, 3}, {3, 3}, {3, 3},
		{4, 3}, {4, 3}, {4, 3}, {4, 3}, {5, 4}, {5, 4}, {6, 5}, {-1, 0}
	};

	/* Table B-12, dct_dc_size_luminance, codes 111110xxx ... 111111111 */
	public final static byte DClumtab1[][] = {
		{7, 6}, {7, 6}, {7, 6}, {7, 6}, {7, 6}, {7, 6}, {7, 6}, {7, 6},
		{8, 7}, {8, 7}, {8, 7}, {8, 7}, {9, 8}, {9, 8}, {10,9}, {11,9}
	};

	/* Table B-13, dct_dc_size_chrominance, codes 00xxx ... 11110 */
	public final static byte DCchromtab0[][] = {
		{0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2},
		{1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2}, {1, 2},
		{2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2}, {2, 2},
		{3, 3}, {3, 3}, {3, 3}, {3, 3}, {4, 4}, {4, 4}, {5, 5}, {-1, 0}
	};

	/* Table B-13, dct_dc_size_chrominance, codes 111110xxxx ... 1111111111 */
	public final static byte DCchromtab1[][] = {
		{6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6},
		{6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6}, {6, 6},
		{7, 7}, {7, 7}, {7, 7}, {7, 7}, {7, 7}, {7, 7}, {7, 7}, {7, 7},
		{8, 8}, {8, 8}, {8, 8}, {8, 8}, {9, 9}, {9, 9}, {10,10}, {11,10}
	};

	/* Table B-10, motion_code, codes 0001 ... 01xx */
	public final static byte MVtab0[][] = {
		{-1,0}, {3,3}, {2,2}, {2,2}, {1,1}, {1,1}, {1,1}, {1,1}
	};

	/* Table B-10, motion_code, codes 0000011 ... 000011x */
	public final static byte MVtab1[][] = {
		{-1,0}, {-1,0}, {-1,0}, {7,6}, {6,6}, {5,6}, {4,5}, {4,5}
	};

	/* Table B-10, motion_code, codes 0000001100 ... 000001011x */
	public final static byte MVtab2[][] = {
		{16,9}, {15,9}, {14,9}, {13,9},
		{12,9}, {11,9}, {10,8}, {10,8},
		{9,8},  {9,8},  {8,8},  {8,8}
	};


	/* Table B-3, macroblock_type in P-pictures, codes 001..1xx */
	public final static byte PMBtab0[][] = {
		{-1,0},
		{MACROBLOCK_MOTION_FORWARD,3},
		{MACROBLOCK_PATTERN,2}, {MACROBLOCK_PATTERN,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,1},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,1},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,1},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,1}
	};

	/* Table B-3, macroblock_type in P-pictures, codes 000001..00011x */
	public final static byte PMBtab1[][] = {
		{-1,0},
		{MACROBLOCK_QUANT|MACROBLOCK_INTRA,6},
		{MACROBLOCK_QUANT|MACROBLOCK_PATTERN,5}, {MACROBLOCK_QUANT|MACROBLOCK_PATTERN,5},
		{MACROBLOCK_QUANT|MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,5}, {MACROBLOCK_QUANT|MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,5},
		{MACROBLOCK_INTRA,5}, {MACROBLOCK_INTRA,5}
	};

	/* Table B-4, macroblock_type in B-pictures, codes 0010..11xx */
	public final static byte BMBtab0[][] = {
		{-1,0},
		{-1,0},
		{MACROBLOCK_MOTION_FORWARD,4},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,4},
		{MACROBLOCK_MOTION_BACKWARD,3},
		{MACROBLOCK_MOTION_BACKWARD,3},
		{MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,3},
		{MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,3},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,2},
		{MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,2}
	};

	/* Table B-4, macroblock_type in B-pictures, codes 000001..00011x */
	public final static byte BMBtab1[][] = {
		{-1,0},
		{MACROBLOCK_QUANT|MACROBLOCK_INTRA,6},
		{MACROBLOCK_QUANT|MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,6},
		{MACROBLOCK_QUANT|MACROBLOCK_MOTION_FORWARD|MACROBLOCK_PATTERN,6},
		{MACROBLOCK_QUANT|MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,5},
		{MACROBLOCK_QUANT|MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD|MACROBLOCK_PATTERN,5},
		{MACROBLOCK_INTRA,5},
		{MACROBLOCK_INTRA,5}
	};

	public final static double frame_rate_Table[] = {
		0.0,
		((24.0*1000.0)/1001.0),
		24.0,
		25.0,
		((30.0*1000.0)/1001.0),
		30.0,
		50.0,
		((60.0*1000.0)/1001.0),
		60.0,

		-1,		// reserved
		-1,
		-1,
		-1,
		-1,
		-1,
		-1
	};

	/* global value */
	private final byte backward_reference_frame[] = new byte[3], forward_reference_frame[] = new byte[3];
	private final byte auxframe[] = new byte[3], current_frame[] = new byte[3];
	private float frame_rate;
	/**
	 *
	 */
	private void Clear_Block(final int comp) // assembler?
	{
		Arrays.fill(block[comp],(short)0);	//clear macroblaock
	}

	/**
	 *
	 */
	private int Get_Bits(final int N)
	{
		int Pos, Val, a;
		Pos = BitPos>>>3;
		a = Pos;

		if(a >= buf.length){
			ERROR3 = true;
		}

		if (a == LastPosVal[0]) {
			Val = LastPosVal[1];
		} else
		{
			Val =  (0xFF & buf[a++])<<24;

			if (a < buf.length) {
				Val |= (0xFF & buf[a++])<<16;
			}

			if (a < buf.length) {
				Val |= (0xFF & buf[a++])<<8;
			}

			if (a < buf.length) {
				Val |= (0xFF & buf[a]);
			}
		}

		LastPosVal[0] = Pos;
		LastPosVal[1] = Val;

		Val <<= BitPos & 7;
		Val >>>= 32-N;

		BitPos += N;
		BufferPos = BitPos>>>3;

				return Val;
	}

	/**
	 *
	 */
	private int Show_Bits(final int N)
	{
		int Pos, Val, a;
		Pos = BitPos>>>3;
				a = Pos;

				if (a >= buf.length) {
					ERROR3 = true;
				}

				if (a == LastPosVal[0]) {
					Val = LastPosVal[1];
				} else
				{
					Val =  (0xFF & buf[a++])<<24;

					if (a < buf.length) {
						Val |= (0xFF & buf[a++])<<16;
					}

					if (a < buf.length) {
						Val |= (0xFF & buf[a++])<<8;
					}

					if (a < buf.length) {
						Val |= (0xFF & buf[a]);
					}
				}

				LastPosVal[0] = Pos;
				LastPosVal[1] = Val;

				Val <<= BitPos & 7;
				Val >>>= 32 - N;

				return Val;
	}

	/**
	 *
	 */
	private void Flush_Bits(final int N)
	{
		BitPos += N;
		BufferPos = BitPos>>>3;
	}


	/* decode headers from one input stream */
	public int extern_Get_Hdr() {
		int start_code;

		for (;;){
			/* look for next_start_code */
			if (DIRECTION) {
				previous_start_code();
			} else {
				next_start_code();
			}

			if ((start_code=Get_Bits(32))==SEQUENCE_HEADER_CODE){
				resetDecoder(); //DM26112003 081.5++
				StartPos=BufferPos-4;
				sequence_header();
				next_start_code();

				if ((start_code=Get_Bits(32))==GROUP_START_CODE)
				{
					group_of_pictures_header();
					next_start_code();

					if ((start_code=Get_Bits(32))==PICTURE_START_CODE)
					{
						picture_header();
						return 1;
					}
				}
				else if (start_code==PICTURE_START_CODE) //decode pic even without gopheader
				{
					picture_header();
					return 1;
				}

			}
			else if (viewGOP && (start_code==GROUP_START_CODE)){
				StartPos=BufferPos-4;
				group_of_pictures_header();
				next_start_code();
				if ((start_code=Get_Bits(32))==PICTURE_START_CODE){
					picture_header();
					return 1;
				}
			}

			else if (!viewGOP && (start_code==GROUP_START_CODE)){
				if (DIRECTION) {
					Flush_Bits(-40);
				}

				ERROR5 = true;
			}
			else if (DIRECTION) {
				Flush_Bits(-40);
			}
		}
	}

	/* align to start of next next_start_code */
	private void next_start_code()
	{
		Flush_Bits((8 - (BitPos & 7)) & 7);

		while (Show_Bits(24) != 1) {
			Flush_Bits(8);
		}
	}

	/* align to start of next next_start_code */
	private void previous_start_code()
	{
		Flush_Bits((8 - (BitPos & 7)) & 7);

		while (Show_Bits(24) != 1) {
			Flush_Bits(-8);
		}
	}

	/* decode sequence header */
	private void sequence_header(){
		int i;

		horizontal_size             = Get_Bits(12);
		vertical_size               = Get_Bits(12);
		aspect_ratio_information    = Get_Bits(4);
		frame_rate_code             = Get_Bits(4);
		Get_Bits(18);
		Flush_Bits(1);	// marker bit
		Get_Bits(10);
		Get_Bits(1);


		if ((load_intra_quantizer_matrix = Get_Bits(1))>0)
		{
			for (i=0; i<64; i++) {
				intra_quantizer_matrix[scan[ZIG_ZAG][i]] = Get_Bits(8);
			}

		}
		else
		{
			System.arraycopy(default_intra_quantizer_matrix,0,intra_quantizer_matrix,0,64);
		}

		if ((load_non_intra_quantizer_matrix = Get_Bits(1))>0)
		{
			for (i=0; i<64; i++) {
				non_intra_quantizer_matrix[scan[ZIG_ZAG][i]] = Get_Bits(8);
			}

		}
		else
		{
			Arrays.fill(non_intra_quantizer_matrix,16);
		}



		/* copy luminance to chrominance matrices */
		System.arraycopy(intra_quantizer_matrix,0,chroma_intra_quantizer_matrix,0,64);
		System.arraycopy(non_intra_quantizer_matrix,0,chroma_non_intra_quantizer_matrix,0,64);

		frame_rate = (float)frame_rate_Table[frame_rate_code]; //DM06022004 081.6 int15 add

		extension_and_user_data();



	}


	/* decode group of pictures header */
	/* ISO/IEC 13818-2 section 6.2.2.6 */
	private void group_of_pictures_header(){
		Get_Bits(1);
		Get_Bits(5);
		Get_Bits(6);
		Flush_Bits(1);	// marker bit
		Get_Bits(6);
		Get_Bits(6);
		Get_Bits(1);
		Get_Bits(1);

		extension_and_user_data();
	}

	/* decode extension and user data */
	/* ISO/IEC 13818-2 section 6.2.2.2 */
	private void extension_and_user_data(){
		int code, ext_ID;

		next_start_code();

		while (((code = Show_Bits(32))==EXTENSION_START_CODE) || (code==USER_DATA_START_CODE)){
			if (code==EXTENSION_START_CODE)	{
				Flush_Bits(32);
				ext_ID = Get_Bits(4);

				switch (ext_ID)	{
				case SEQUENCE_EXTENSION_ID:
					sequence_extension();
					break;
				case SEQUENCE_DISPLAY_EXTENSION_ID:
					sequence_display_extension();
					break;
				case QUANT_MATRIX_EXTENSION_ID:
					quant_matrix_extension();
					break;
				case PICTURE_DISPLAY_EXTENSION_ID:
					picture_display_extension();
					break;
				case PICTURE_CODING_EXTENSION_ID:
					picture_coding_extension();
					break;
				case COPYRIGHT_EXTENSION_ID:
					copyright_extension();
					break;
				}
				next_start_code();
			}else{

				Flush_Bits(32);	// ISO/IEC 13818-2  sections 6.3.4.1 and 6.2.2.2.2
				next_start_code();	// skip user data
			}
		}
	}

	/* decode picture header */
	/* ISO/IEC 13818-2 section 6.2.3 */
	private void picture_header(){
		temporal_reference  = Get_Bits(10);
		picture_coding_type = Get_Bits(3);

		Get_Bits(16);

		if ((picture_coding_type==P_TYPE) || (picture_coding_type==B_TYPE))	{
			Get_Bits(1);
			Get_Bits(3);
		}

		if (picture_coding_type==B_TYPE){
			Get_Bits(1);
			Get_Bits(3);
		}

		extra_bit_information();
		extension_and_user_data();
	}

	/* decode sequence extension */
	/* ISO/IEC 13818-2 section 6.2.2.3 */
	private void sequence_extension()
	{
		int frame_rate_extension_n;
		int frame_rate_extension_d;

		int horizontal_size_extension;
		int vertical_size_extension;
		profile_and_level_indication = Get_Bits(8);
		progressive_sequence         = Get_Bits(1);
		chroma_format                = Get_Bits(2);
		horizontal_size_extension    = Get_Bits(2);
		vertical_size_extension      = Get_Bits(2);
		Get_Bits(12);
		Flush_Bits(1);	// marker bit
		Get_Bits(8);
		Get_Bits(1);

		frame_rate_extension_n       = Get_Bits(2);
		frame_rate_extension_d       = Get_Bits(5);
		frame_rate = (frame_rate * (frame_rate_extension_n+1)) / (frame_rate_extension_d+1); //DM06022004 081.6 int15 changed

		horizontal_size = (horizontal_size_extension<<12) | (horizontal_size&0xfff);
		vertical_size = (vertical_size_extension<<12) | (vertical_size&0xfff);
	}

	/* decode sequence display extension */
	private void sequence_display_extension()
	{
		int color_description;
		video_format      = Get_Bits(3);
		color_description = Get_Bits(1);

		if (color_description>0)
		{
			Get_Bits(8);
			Get_Bits(8);
			Get_Bits(8);
		}

		Get_Bits(14);
		Flush_Bits(1);	// marker bit
		Get_Bits(14);
	}

	/* decode quant matrix entension */
	/* ISO/IEC 13818-2 section 6.2.3.2 */
	private void quant_matrix_extension()
	{
		int i;

		if ((load_intra_quantizer_matrix = Get_Bits(1))>0) {
			for (i=0; i<64; i++) {
				chroma_intra_quantizer_matrix[scan[ZIG_ZAG][i]]
						= intra_quantizer_matrix[scan[ZIG_ZAG][i]]
								= Get_Bits(8);
			}
		}

		if ((load_non_intra_quantizer_matrix = Get_Bits(1))>0) {
			for (i=0; i<64; i++) {
				chroma_non_intra_quantizer_matrix[scan[ZIG_ZAG][i]]
						= non_intra_quantizer_matrix[scan[ZIG_ZAG][i]]
								= Get_Bits(8);
			}
		}

		if ((load_chroma_intra_quantizer_matrix = Get_Bits(1))>0) {
			for (i=0; i<64; i++) {
				chroma_intra_quantizer_matrix[scan[ZIG_ZAG][i]] = Get_Bits(8);
			}
		}

		if ((load_chroma_non_intra_quantizer_matrix = Get_Bits(1))>0) {
			for (i=0; i<64; i++) {
				chroma_non_intra_quantizer_matrix[scan[ZIG_ZAG][i]] = Get_Bits(8);
			}
		}
	}

	/* decode picture display extension */
	/* ISO/IEC 13818-2 section 6.2.3.3. */
	private void picture_display_extension()
	{
		final int frame_center_horizontal_offset[] = new int[3];
		final int frame_center_vertical_offset[] = new int[3];

		int i;
		int number_of_frame_center_offsets;

		/* based on ISO/IEC 13818-2 section 6.3.12
		(November 1994) Picture display extensions */

		/* derive number_of_frame_center_offsets */
		if (progressive_sequence>0)
		{
			if (repeat_first_field>0)
			{
				if (top_field_first>0) {
					number_of_frame_center_offsets = 3;
				} else {
					number_of_frame_center_offsets = 2;
				}
			} else {
				number_of_frame_center_offsets = 1;
			}
		}
		else
		{
			if (picture_structure!=FRAME_PICTURE) {
				number_of_frame_center_offsets = 1;
			} else
			{
				if (repeat_first_field>0) {
					number_of_frame_center_offsets = 3;
				} else {
					number_of_frame_center_offsets = 2;
				}
			}
		}


		/* now parse */
		for (i=0; i<number_of_frame_center_offsets; i++)
		{
			frame_center_horizontal_offset[i] = Get_Bits(16);
			Flush_Bits(1);	// marker bit

			frame_center_vertical_offset[i] = Get_Bits(16);
			Flush_Bits(1);	// marker bit

		}
	}

	/* decode picture coding extension */
	private void picture_coding_extension()
	{
		int composite_display_flag;
		f_code[0][0] = Get_Bits(4);
		f_code[0][1] = Get_Bits(4);
		f_code[1][0] = Get_Bits(4);
		f_code[1][1] = Get_Bits(4);

		intra_dc_precision		= Get_Bits(2);
		picture_structure		= Get_Bits(2);
		top_field_first			= Get_Bits(1);
		frame_pred_frame_dct		= Get_Bits(1);
		concealment_motion_vectors	= Get_Bits(1);
		q_scale_type			= Get_Bits(1);
		intra_vlc_format		= Get_Bits(1);
		alternate_scan			= Get_Bits(1);
		repeat_first_field		= Get_Bits(1);
		Get_Bits(1);
		progressive_frame		= Get_Bits(1);
		composite_display_flag		= Get_Bits(1);


		if (composite_display_flag>0)
		{
			Get_Bits(1);
			Get_Bits(3);
			Get_Bits(1);
			Get_Bits(7);
			Get_Bits(8);

		}
	}

	/* Copyright extension */
	/* ISO/IEC 13818-2 section 6.2.3.6. */
	/* (header added in November, 1994 to the IS document) */
	private void copyright_extension()
	{
		Get_Bits(1);
		Get_Bits(8);
		Get_Bits(1);

		Get_Bits(7);

		Flush_Bits(1); // marker bit
		Get_Bits(20);
		Flush_Bits(1); // marker bit
		Get_Bits(22);
		Flush_Bits(1); // marker bit
		Get_Bits(22);
	}

	/* set std for lower profiles as mpeg1 */
	private void resetDecoder()
	{
		Fault_Flag=0; //DM14052004 081.7 int02 add,fix
		picture_coding_type=0; //DM14052004 081.7 int02 add,fix
		video_format=5;
		progressive_sequence=1;
		chroma_format=1;
		profile_and_level_indication=0;
		Second_Field=0;
		intra_dc_precision=0;
		picture_structure=FRAME_PICTURE;
		top_field_first=0;
		frame_pred_frame_dct=1;
		concealment_motion_vectors=0;
		intra_vlc_format=0;
		repeat_first_field=0;
		progressive_frame=1;
		q_scale_type=0;
		quantizer_scale=0;
		alternate_scan=0;
	}

	private void InitialDecoder()
	{

		mb_width = (horizontal_size + 15)>>>4;
					mb_height = (progressive_sequence>0) ? (vertical_size+15)>>>4 : ((vertical_size + 31)>>>5)<<1;

			Coded_Picture_Width = mb_width<<4;
			Coded_Picture_Height = mb_height<<4;

			Chroma_Width = (chroma_format==CHROMA444) ? Coded_Picture_Width : Coded_Picture_Width>>1;
			block_count = ChromaFormat[chroma_format];

			if (picture_coding_type==I_TYPE) {
				resizePixels(Coded_Picture_Width, Coded_Picture_Height, horizontal_size, vertical_size);
			}
	}

	public void resizePixels(final int cw, final int ch, final int hs, final int vs)
	{
		//value set from outside
		Coded_Picture_Width = cw;
		Coded_Picture_Height = ch;
		horizontal_size = hs;
		vertical_size = vs;

		if (pixels.length != (Coded_Picture_Width * Coded_Picture_Height)) {
			pixels = new int[Coded_Picture_Width * Coded_Picture_Height];
		} else {
			Arrays.fill(pixels, 0);
		}
	}

	public void Decode_Picture(){
		if ((picture_structure==FRAME_PICTURE) && (Second_Field>0)) {
			Second_Field = 0;
		}

		Update_Picture_Buffers();
		picture_data();

		if (picture_structure!=FRAME_PICTURE) {
			Second_Field ^= Second_Field;
		}
	}

	/* reuse old picture buffers as soon as they are no longer needed */
	public void Update_Picture_Buffers(){

		int cc;              /* color component index */
		byte tmp;  /* temporary swap pointer */

		for (cc=0; cc<3; cc++)	{
			/* B pictures  do not need to be save for future reference */
			if (picture_coding_type==B_TYPE) {
				current_frame[cc] = auxframe[cc];
			} else{
				if (Second_Field<1){
					/* only update at the beginning of the coded frame */
					tmp = forward_reference_frame[cc];

					/* the previously decoded reference frame is stored coincident with the
				   location where the backward reference frame is stored (backwards
				   prediction is not needed in P pictures) */
					forward_reference_frame[cc] = backward_reference_frame[cc];

					/* update pointer for potential future B pictures */
					backward_reference_frame[cc] = tmp;
				}

				/* can erase over old backward reference frame since it is not used
			   in a P picture, and since any subsequent B pictures will use the
			   previously decoded I or P frame as the backward_reference_frame */
				current_frame[cc] = backward_reference_frame[cc];
			}

			if (picture_structure==BOTTOM_FIELD) {
				current_frame[cc] += (cc==0) ? Coded_Picture_Width : Chroma_Width;
			}
		}
	}

	/* decode all macroblocks of the current picture */
	/* stages described in ISO/IEC 13818-2 section 7 */
	public void picture_data(){
		int MBAmax;
		/* number of macroblocks per picture */
		MBAmax = mb_width*mb_height;

		if (picture_structure!=FRAME_PICTURE) {
			MBAmax>>=1;
		}

		for (;;) {
			if (slice(MBAmax)<0) {
				return;
			}
		}
	}

	/* decode slice header */
	/* ISO/IEC 13818-2 section 6.2.4 */
	public int slice_header(){
		final int slice_vertical_position_extension = vertical_size>2800 ? Get_Bits(3) : 0;

		final int quantizer_scale_code = Get_Bits(5);
		quantizer_scale = (q_scale_type>0) ? Non_Linear_quantizer_scale[quantizer_scale_code] : quantizer_scale_code<<1;

		/* slice_id introduced in March 1995 as part of the video corridendum
	   (after the IS was drafted in November 1994) */
		if (Get_Bits(1)>0){
			Get_Bits(1);	// intra slice

			Get_Bits(1);
			Get_Bits(6);

			extra_bit_information();
		}

		return slice_vertical_position_extension;
	}

	/* decode extra bit information */
	/* ISO/IEC 13818-2 section 6.2.3.4. */
	public int extra_bit_information(){
		int Byte_Count = 0;

		while (Get_Bits(1)>0){
			Flush_Bits(8);
			Byte_Count ++;
		}

		return Byte_Count;
	}



	/* decode all macroblocks of the current picture */
	/* ISO/IEC 13818-2 section 6.3.16 */
	/* return 0 : go to next slice */
	/* return -1: go to next picture */
	public int slice(final int MBAmax){

		final int MBA[] = {0}, MBAinc[] ={0}, macroblock_type[]={0}, motion_type[]={0}, dct_type[]={0};
		int ret=0;
		final int dc_dct_pred[] = new int[3], PMV[][][] = new int[2][2][2],
				motion_vertical_field_select[][] = new int[2][2], dmvector[] = new int[2];

		if ((ret=start_of_slice(MBA, MBAinc, dc_dct_pred, PMV))!=1) {
			return ret;
		}

		for (;;){
			/* this is how we properly exit out of picture */
			if (MBA[0]>=MBAmax)
			{
				return -1;		// all macroblocks decoded
			}

			if (MBAinc[0]==0)	{
				if ((Show_Bits(23)<1) || (Fault_Flag>0)){	// next_start_code or fault
					Fault_Flag = 0;
					return 0;	// trigger: go to next slice
				}else{	/* neither next_start_code nor Fault_Flag */
					/* decode macroblock address increment */
					MBAinc[0] = Get_macroblock_address_increment();
					if (Fault_Flag>0) {
						Fault_Flag = 0;
						return 0;	// trigger: go to next slice
					}
				}
			}


			if (MBAinc[0]==1) { /* not skipped */
				if (decode_macroblock(macroblock_type, motion_type, dct_type, PMV,
						dc_dct_pred, motion_vertical_field_select, dmvector)<1) {
					Fault_Flag = 0;
					//DM14052004 081.7 int02 changed
					return 0; //return -1; // return 0;	// trigger: go to next slice
				}
			}else{ /* MBAinc[0]!=1: skipped macroblock */	/* ISO/IEC 13818-2 section 7.6.6 */
				skipped_macroblock(dc_dct_pred, PMV, motion_type,
						motion_vertical_field_select, macroblock_type);
			}

			/* ISO/IEC 13818-2 section 7.6 */
			motion_compensation(MBA, macroblock_type, motion_type, PMV,
					motion_vertical_field_select, dmvector, dct_type);

			/* advance to next macroblock */
			MBA[0]++; MBAinc[0]--;

			if (MBA[0]>=MBAmax)
			{
				return -1;		// all macroblocks decoded
			}
		}
	}


	/* ISO/IEC 13818-2 section 7.6.6 */
	public void skipped_macroblock(final int dc_dct_pred[], final int PMV[][][], final int motion_type[],
			final int motion_vertical_field_select[][], final int macroblock_type[]){

		int comp;

		for (comp=0; comp<block_count; comp++) {
			Clear_Block(comp);
		}

		/* reset intra_dc predictors */
		/* ISO/IEC 13818-2 section 7.2.1: DC coefficients in intra blocks */
		dc_dct_pred[0]=dc_dct_pred[1]=dc_dct_pred[2]=0;

		/* reset motion vector predictors */
		/* ISO/IEC 13818-2 section 7.6.3.4: Resetting motion vector predictors */
		if (picture_coding_type==P_TYPE) {
			PMV[0][0][0]=PMV[0][0][1]=PMV[1][0][0]=PMV[1][0][1]=0;
		}

		/* derive motion_type */
		if (picture_structure==FRAME_PICTURE) {
			motion_type[0] = MC_FRAME;
		} else{
			motion_type[0] = MC_FIELD;
			motion_vertical_field_select[0][0] = motion_vertical_field_select[0][1] =
					((picture_structure==BOTTOM_FIELD)?1:0);
		}

		/* clear MACROBLOCK_INTRA */
		macroblock_type[0] &= ~MACROBLOCK_INTRA;
	}

	/* ISO/IEC 13818-2 sections 7.2 through 7.5 */
	public int decode_macroblock(final int macroblock_type[], final int motion_type[], final int dct_type[],
			final int PMV[][][], final int dc_dct_pred[],
			final int motion_vertical_field_select[][], final int dmvector[]){

		int quantizer_scale_code, comp;
		final int motion_vector_count[]={0}, mv_format[]={0};
		final int dmv[]={0}, mvscale[]={0};
		int coded_block_pattern;

		/* ISO/IEC 13818-2 section 6.3.17.1: Macroblock modes */
		macroblock_modes(macroblock_type, motion_type, motion_vector_count, mv_format,
				dmv, mvscale, dct_type);

		if (Fault_Flag>0)
		{
			return 0;	// trigger: go to next slice
		}

		if ( (macroblock_type[0] & MACROBLOCK_QUANT)>0 ){

			quantizer_scale_code = Get_Bits(5);

			/* ISO/IEC 13818-2 section 7.4.2.2: Quantizer scale factor */
			quantizer_scale = (q_scale_type>0) ?
					Non_Linear_quantizer_scale[quantizer_scale_code] : (quantizer_scale_code << 1);
		}

		/* ISO/IEC 13818-2 section 6.3.17.2: Motion vectors */
		/* decode forward motion vectors */
		if ( ((macroblock_type[0] & MACROBLOCK_MOTION_FORWARD)>0)
				|| (((macroblock_type[0] & MACROBLOCK_INTRA)>0) && (concealment_motion_vectors>0))) {
			motion_vectors(PMV, dmvector, motion_vertical_field_select, 0,
					motion_vector_count, mv_format, f_code[0][0]-1, f_code[0][1]-1, dmv, mvscale);
		}

		if (Fault_Flag>0)
		{
			return 0;	// trigger: go to next slice
		}

		/* decode backward motion vectors */
		if ((macroblock_type[0] & MACROBLOCK_MOTION_BACKWARD)>0) {
			motion_vectors(PMV, dmvector, motion_vertical_field_select, 1,
					motion_vector_count,mv_format, f_code[1][0]-1, f_code[1][1]-1, new int[1], mvscale);
		}

		if (Fault_Flag>0)
		{
			return 0;  // trigger: go to next slice
		}

		if (((macroblock_type[0] & MACROBLOCK_INTRA)>0) && (concealment_motion_vectors>0))
		{
			Flush_Bits(1);	// marker bit
		}

		/* macroblock_pattern */
		/* ISO/IEC 13818-2 section 6.3.17.4: Coded block pattern */
		if ((macroblock_type[0] & MACROBLOCK_PATTERN)>0)	{
			coded_block_pattern = Get_coded_block_pattern();

			if (chroma_format==CHROMA422) {
				coded_block_pattern = (coded_block_pattern<<2) | Get_Bits(2);
			} else if (chroma_format==CHROMA444) {
				coded_block_pattern = (coded_block_pattern<<6) | Get_Bits(6);
			}
		} else {
			coded_block_pattern = ((macroblock_type[0] & MACROBLOCK_INTRA)>0) ? (1<<block_count)-1 : 0;
		}

		if (Fault_Flag>0)
		{
			return 0;	// trigger: go to next slice
		}


		/* decode blocks */
		for (comp=0; comp<block_count; comp++)	{
			Clear_Block(comp);

			if ((coded_block_pattern & (1<<(block_count-1-comp)))>0){
				if ((macroblock_type[0] & MACROBLOCK_INTRA)>0) {
					Decode_MPEG2_Intra_Block(comp, dc_dct_pred);
				} else {
					Decode_MPEG2_Non_Intra_Block(comp);
				}

				if (Fault_Flag>0)
				{
					return 0;	// trigger: go to next slice
				}
			}
		}

		/* reset intra_dc predictors */
		/* ISO/IEC 13818-2 section 7.2.1: DC coefficients in intra blocks */
		if ((macroblock_type[0] & MACROBLOCK_INTRA)<1) {
			dc_dct_pred[0]=dc_dct_pred[1]=dc_dct_pred[2]=0;
		}

		/* reset motion vector predictors */
		if (((macroblock_type[0] & MACROBLOCK_INTRA)>0) && (concealment_motion_vectors<1) ) {
			/* intra mb without concealment motion vectors */
			/* ISO/IEC 13818-2 section 7.6.3.4: Resetting motion vector predictors */
			PMV[0][0][0]=PMV[0][0][1]=PMV[1][0][0]=PMV[1][0][1]=0;
			PMV[0][1][0]=PMV[0][1][1]=PMV[1][1][0]=PMV[1][1][1]=0;
		}

		/* special "No_MC" macroblock_type case */
		/* ISO/IEC 13818-2 section 7.6.3.5: Prediction in P pictures */
		if ((picture_coding_type==P_TYPE)
				&& ((macroblock_type[0] & (MACROBLOCK_MOTION_FORWARD|MACROBLOCK_INTRA))<1))	{

			/* non-intra mb without forward mv in a P picture */
			/* ISO/IEC 13818-2 section 7.6.3.4: Resetting motion vector predictors */
			PMV[0][0][0]=PMV[0][0][1]=PMV[1][0][0]=PMV[1][0][1]=0;

			/* derive motion_type */
			/* ISO/IEC 13818-2 section 6.3.17.1: Macroblock modes, frame_motion_type */
			if (picture_structure==FRAME_PICTURE) {
				motion_type[0] = MC_FRAME;
			} else{
				motion_type[0] = MC_FIELD;
				motion_vertical_field_select[0][0] = (picture_structure==BOTTOM_FIELD)?1:0;
			}
		}
		/* successfully decoded macroblock */
		return 1 ;
	}


	/* decode one intra coded MPEG-2 block */
	private void Decode_MPEG2_Intra_Block(final int comp, final int dc_dct_pred[])
	{
		int val=0, i, j, sign, qmat[]; //qmat woanders??
		int code;
		byte tab[];
		short bp[];  //bp woanders array?

		bp = block[comp];  //macroblock
		qmat = ((comp<4) || (chroma_format==CHROMA420))
				? intra_quantizer_matrix : chroma_intra_quantizer_matrix;

		/* ISO/IEC 13818-2 section 7.2.1: decode DC coefficients */
		switch (cc_table[comp])
		{
		case 0:
			val = (dc_dct_pred[0]+= Get_Luma_DC_dct_diff());
			break;

		case 1:
			val = (dc_dct_pred[1]+= Get_Chroma_DC_dct_diff());
			break;

		case 2:
			val = (dc_dct_pred[2]+= Get_Chroma_DC_dct_diff());
			break;
		}

		bp[0] = (short)(val << (3-intra_dc_precision));  //the top-left pixel value of block

		/* decode AC coefficients */
		for (i=1; ; i++)
		{
			code = Show_Bits(16);

			if ((code>=16384) && (intra_vlc_format<1)) {
				tab = DCTtabnext[(code>>12)-4];
			} else if (code>=1024)
			{
				if (intra_vlc_format>0) {
					tab = DCTtab0a[(code>>8)-4];
				} else {
					tab = DCTtab0[(code>>8)-4];
				}
			}
			else if (code>=512)
			{
				if (intra_vlc_format>0) {
					tab = DCTtab1a[(code>>6)-8];
				} else {
					tab = DCTtab1[(code>>6)-8];
				}
			}
			else if (code>=256) {
				tab = DCTtab2[(code>>4)-16];
			} else if (code>=128) {
				tab = DCTtab3[(code>>3)-16];
			} else if (code>=64) {
				tab = DCTtab4[(code>>2)-16];
			} else if (code>=32) {
				tab = DCTtab5[(code>>1)-16];
			} else if (code>=16) {
				tab = DCTtab6[code-16];
			} else
			{
				Fault_Flag = 1;
				return;
			}

			Flush_Bits(tab[2]);

			if (tab[0]<64)
			{
				i+= tab[0];
				val = tab[1];
				sign = Get_Bits(1);
			}
			else if (tab[0]==64) {
				return;
			} else
			{ /* escape */
				if (profile_and_level_indication==0)
				{
					//mpeg1 //DM28112003 081.5++
					i+= Get_Bits(6);
					val = Get_Bits(8);

					if (val==0) {
						val = Get_Bits(8);
					} else if(val==128) {
						val = Get_Bits(8)-128;
					} else if(val>128) {
						val-=256;
					}
					sign = 0;
				}
				else
				{
					//mpeg2
					i+= Get_Bits(6);
					val = Get_Bits(12);

					if ( (sign = (val>=2048)?1:0) >0) {
						val = 4096 - val;
					}
				}
			}

			//prevent outside index
			i = i > 63 ? 63 : i;

			j = scan[alternate_scan][i];

			val = (val * quantizer_scale * qmat[j]) >> 4;
			bp[j] = (short)((sign>0) ? -val : val);
		}
	}


	/* decode one non-intra coded MPEG-2 block */
	private void Decode_MPEG2_Non_Intra_Block(final int comp)
	{
		int val, i, j, sign, qmat[];
		int code;
		byte tab[];
		short bp[];

		bp = block[comp];
		qmat = ((comp<4) || (chroma_format==CHROMA420))
				? non_intra_quantizer_matrix : chroma_non_intra_quantizer_matrix;

		/* decode AC coefficients */
		for (i=0; ; i++)
		{
			code = Show_Bits(16);

			if (code>=16384)
			{
				if (i==0) {
					tab = DCTtabfirst[(code>>12)-4];
				} else {
					tab = DCTtabnext[(code>>12)-4];
				}
			}
			else if (code>=1024) {
				tab = DCTtab0[(code>>8)-4];
			} else if (code>=512) {
				tab = DCTtab1[(code>>6)-8];
			} else if (code>=256) {
				tab = DCTtab2[(code>>4)-16];
			} else if (code>=128) {
				tab = DCTtab3[(code>>3)-16];
			} else if (code>=64) {
				tab = DCTtab4[(code>>2)-16];
			} else if (code>=32) {
				tab = DCTtab5[(code>>1)-16];
			} else if (code>=16) {
				tab = DCTtab6[code-16];
			} else
			{
				Fault_Flag = 1;
				return;
			}

			Flush_Bits(tab[2]);

			if (tab[0]<64)
			{
				i+= tab[0];
				val = tab[1];
				sign = Get_Bits(1);
			}
			else if (tab[0]==64) {
				return;
			} else
			{ /* escape */
				i+= Get_Bits(6);
				val = Get_Bits(12);

				if ( (sign = (val>=2048)?1:0)>0 ) {
					val = 4096 - val;
				}
			}

			j = scan[alternate_scan][i];

			val = (((val<<1)+1) * quantizer_scale * qmat[j]) >> 5;
			bp[j] = (short)((sign>0) ? -val : val);
		}
	}


	/*
	parse VLC and perform dct_diff arithmetic.
	MPEG-2:  ISO/IEC 13818-2 section 7.2.1

	Note: the arithmetic here is presented more elegantly than
	the spec, yet the results, dct_diff, are the same.
	 */
	private int Get_Luma_DC_dct_diff()
	{
		int code, size, dct_diff;

		/* decode length */
		code = Show_Bits(5);

		if (code<31)
		{
			size = DClumtab0[code][0];
			Flush_Bits(DClumtab0[code][1]);
		}
		else
		{
			code = Show_Bits(9) - 0x1f0;
			size = DClumtab1[code][0];
			Flush_Bits(DClumtab1[code][1]);
		}

		if (size==0) {
			dct_diff = 0;
		} else
		{
			dct_diff = Get_Bits(size);

			if ((dct_diff & (1<<(size-1)))==0) {
				dct_diff-= (1<<size) - 1;
			}
		}

		return dct_diff;
	}

	private int Get_Chroma_DC_dct_diff()
	{
		int code, size, dct_diff;

		/* decode length */
		code = Show_Bits(5);

		if (code<31)
		{
			size = DCchromtab0[code][0];
			Flush_Bits(DCchromtab0[code][1]);
		}
		else
		{
			code = Show_Bits(10) - 0x3e0;
			size = DCchromtab1[code][0];
			Flush_Bits(DCchromtab1[code][1]);
		}

		if (size==0) {
			dct_diff = 0;
		} else
		{
			dct_diff = Get_Bits(size);

			if ((dct_diff & (1<<(size-1)))==0) {
				dct_diff-= (1<<size) - 1;
			}
		}

		return dct_diff;
	}


	private int Get_coded_block_pattern()
	{
		int code;

		if ((code = Show_Bits(9))>=128)
		{
			code >>= 4;
				Flush_Bits(CBPtab0[code][1]);

				return CBPtab0[code][0];
		}

		if (code>=8)
		{
			code >>= 1;
		Flush_Bits(CBPtab1[code][1]);

		return CBPtab1[code][0];
		}

		if (code<1)
		{
			Fault_Flag = 3;
			return 0;
		}

		Flush_Bits(CBPtab2[code][1]);

		return CBPtab2[code][0];
	}


	/* return==-1 means go to next picture */
	/* the expression "start of slice" is used throughout the normative
	body of the MPEG specification */
	private int start_of_slice(final int MBA[], final int MBAinc[],  final int dc_dct_pred[], final int PMV[][][])
	{

		next_start_code();
		final int code = Get_Bits(32);

		if ((code<SLICE_START_CODE_MIN) || (code>SLICE_START_CODE_MAX))
		{
			// only slice headers are allowed in picture_data
			Fault_Flag = 10;
			return -1;
		}

		/* decode slice header (may change quantizer_scale) */
		final int slice_vert_pos_ext = slice_header();

		/* decode macroblock address increment */
		MBAinc[0] = Get_macroblock_address_increment();

		if (Fault_Flag>0) {
			return -1;
		}

		/* set current location */
		/* NOTE: the arithmetic used to derive macroblock_address below is
		equivalent to ISO/IEC 13818-2 section 6.3.17: Macroblock */
		MBA[0] = (((((slice_vert_pos_ext<<7) + (code&255)) - 1) * mb_width) + MBAinc[0]) - 1;
		MBAinc[0] = 1;	// first macroblock in slice: not skipped

		/* reset all DC coefficient and motion vector predictors */
		/* ISO/IEC 13818-2 section 7.2.1: DC coefficients in intra blocks */
		dc_dct_pred[0]=dc_dct_pred[1]=dc_dct_pred[2]=0;

		/* ISO/IEC 13818-2 section 7.6.3.4: Resetting motion vector predictors */
		PMV[0][0][0]=PMV[0][0][1]=PMV[1][0][0]=PMV[1][0][1]=0;
		PMV[0][1][0]=PMV[0][1][1]=PMV[1][1][0]=PMV[1][1][1]=0;

		/* successfull: trigger decode macroblocks in slice */
		return 1;
	}


	private int Get_macroblock_address_increment()
	{
		int code=0, val=0;

		while ((code = Show_Bits(11))<24)
		{
			if (code!=15)
			{ /* if not macroblock_stuffing */
				if (code==8) {
					val+= 33;
				} else
				{
					Fault_Flag = 4;
					return 1;
				}
			}
			Flush_Bits(11);
		}

		/* macroblock_address_increment == 1 */
		/* ('1' is in the MSB position of the lookahead) */
		if (code>=1024)
		{
			Flush_Bits(1);
			return (val + 1);
		}

		/* codes 00010 ... 011xx */
		if (code>=128)
		{
			/* remove leading zeros */
			code >>= 6;
		Flush_Bits(MBAtab1[code][1]);

		return (val + MBAtab1[code][0]);
		}

		/* codes 00000011000 ... 0000111xxxx */
		code-= 24; /* remove common base */
		Flush_Bits(MBAtab2[code][1]);

		return (val + MBAtab2[code][0]);
	}

	/* ISO/IEC 13818-2 sections 6.2.5.2, 6.3.17.2, and 7.6.3: Motion vectors */
	private void motion_vectors(final int PMV[][][],final int dmvector[],
			final int motion_vertical_field_select[][], final int s,
			final int motion_vector_count[], final int mv_format[], final int h_r_size,
			final int v_r_size, final int dmv[], final int mvscale[])
	{

		if (motion_vector_count[0]==1)
		{
			if ((mv_format[0]==MV_FIELD) && (dmv[0]<1)) {
				motion_vertical_field_select[1][s] =
						motion_vertical_field_select[0][s] = Get_Bits(1);
			}

			motion_vector(PMV[0][s],dmvector,h_r_size,v_r_size,dmv,mvscale,0);

			/* update other motion vector predictors */
			PMV[1][s][0] = PMV[0][s][0];
			PMV[1][s][1] = PMV[0][s][1];
		}
		else
		{
			motion_vertical_field_select[0][s] = Get_Bits(1);
			motion_vector(PMV[0][s],dmvector,h_r_size,v_r_size,dmv,mvscale,0);
			motion_vertical_field_select[1][s] = Get_Bits(1);
			motion_vector(PMV[1][s],dmvector,h_r_size,v_r_size,dmv,mvscale,0);
		}
	}

	/* get and decode motion vector and differential motion vector for one prediction */
	private void motion_vector(final int PMV[], final int dmvector[], final int h_r_size, final int v_r_size,
			final int dmv[], final int mvscale[], final int full_pel_vector)
	{

		int motion_code, motion_residual;

		/* horizontal component */
		/* ISO/IEC 13818-2 Table B-10 */
		motion_code = Get_motion_code();

		motion_residual = ((h_r_size!=0) && (motion_code!=0)) ? Get_Bits(h_r_size) : 0;

		decode_motion_vector(PMV[0],h_r_size,motion_code,motion_residual,full_pel_vector);

		if (dmv[0]>0) {
			dmvector[0] = Get_dmvector();
		}

		/* vertical component */
		motion_code     = Get_motion_code();
		motion_residual = ((v_r_size!=0) && (motion_code!=0)) ? Get_Bits(v_r_size) : 0;

		if (mvscale[0]>0) {
			PMV[1] >>= 1; /* DIV 2 */
		}

		decode_motion_vector(PMV[1],v_r_size,motion_code,motion_residual,full_pel_vector);

		if (mvscale[0]>0) {
			PMV[1] <<= 1;
		}

		if (dmv[0]>0) {
			dmvector[1] = Get_dmvector();
		}
	}

	/* calculate motion vector component */
	/* ISO/IEC 13818-2 section 7.6.3.1: Decoding the motion vectors */
	/* Note: the arithmetic here is more elegant than that which is shown
	in 7.6.3.1.  The end results (PMV[][][]) should, however, be the same.  */
	private void decode_motion_vector(int pred, final int r_size, final int motion_code,
			final int motion_residual, final int full_pel_vector)
	{

		int lim, vec;

		lim = 16<<r_size;
		vec = (full_pel_vector>0) ? (pred >> 1) : (pred);

		if (motion_code>0)
		{
			vec+= ((motion_code-1)<<r_size) + motion_residual + 1;
			if (vec>=lim) {
				vec-= lim + lim;
			}
		}
		else if (motion_code<0)
		{
			vec-= ((-motion_code-1)<<r_size) + motion_residual + 1;

			if (vec<-lim) {
				vec+= lim + lim;
			}
		}

		pred = (full_pel_vector>0) ? (vec<<1) : vec;
	}

	/* ISO/IEC 13818-2 section 7.6 */
	private void motion_compensation(final int MBA[], final int macroblock_type[], final int motion_type[],
			final int PMV[][][], final int motion_vertical_field_select[][],
			final int dmvector[], final int dct_type[])
	{

		int bx, by;
		int comp;

		/* derive current macroblock position within picture */
		/* ISO/IEC 13818-2 section 6.3.1.6 and 6.3.1.7 */
		bx = (MBA[0] % mb_width)<<4;
		by = (MBA[0] / mb_width)<<4;


		/* copy or add block data into picture */
		for (comp=0; comp<block_count; comp++)
		{
			/* ISO/IEC 13818-2 section Annex A: inverse DCT */
			if (FAST) {
				IDCT_referenceFAST(block[comp]);
			} else {
				IDCT_reference1(block[comp]);
			}

			//	IDCT_reference(block[comp], FAST ? 1 : 8);

			/* ISO/IEC 13818-2 section 7.6.8: Adding prediction and coefficient data */
			Add_Block(comp, bx, by, dct_type, (macroblock_type[0] & MACROBLOCK_INTRA)==0);
		}
	}

	/**/
	private void IDCT_referenceFAST(final short block[])
	{
		int v;

		final long tmp0  = ref_dct_matrix_i[0] * block[0];
		final long partial_product = ref_dct_matrix_i[0] * tmp0;

		v = (int) (partial_product >> 32);
		block[0] = idct_clip_table[IDCT_CLIP_TABLE_OFFSET + v];

		Arrays.fill(block,block[0]);
	}

	private void IDCT_reference1(final short block[])
	{
		int i, j, v;

		final long tmp[] = new long[64];

		int i8 = 0;

		try{
			for (i = 0; i < 8; i++)
			{
				for (j = 0; j < 8; j++)
				{
					tmp[i8 + j] = (
							(ref_dct_matrix_i[0 + j] * block[i8]) +
							(ref_dct_matrix_i[8 + j] * block[i8 + 1]) +
							(ref_dct_matrix_i[16 + j] * block[i8 + 2]) +
							(ref_dct_matrix_i[24 + j] * block[i8 + 3]) +
							(ref_dct_matrix_i[32 + j] * block[i8 + 4]) +
							(ref_dct_matrix_i[40 + j] * block[i8 + 5]) +
							(ref_dct_matrix_i[48 + j] * block[i8 + 6]) +
							(ref_dct_matrix_i[56 + j] * block[i8 + 7]));

				}
				i8 += 8;
			}

			// Transpose operation is integrated into address mapping by switching loop order of i and j
			for (j = 0; j < 8; j++)
			{
				for (i = 0; i < 8; i++)
				{
					final long partial_product = (
							(ref_dct_matrix_i[i] * tmp[j]) +
							(ref_dct_matrix_i[8 + i] * tmp[8 + j]) +
							(ref_dct_matrix_i[16 + i] * tmp[16 + j]) +
							(ref_dct_matrix_i[24 + i] * tmp[24 + j]) +
							(ref_dct_matrix_i[32 + i] * tmp[32 + j]) +
							(ref_dct_matrix_i[40 + i] * tmp[40 + j]) +
							(ref_dct_matrix_i[48 + i] * tmp[48 + j]) +
							(ref_dct_matrix_i[56 + i] * tmp[56 + j]));

					v = (int) (partial_product >> 32);
					block[(8 * i) + j] = idct_clip_table[IDCT_CLIP_TABLE_OFFSET + v];
				}
			}
		} catch (final Exception e) {
			//Common.setExceptionMessage(e);
		}

	}
	/**/
	//dukios end


	/* move/add 8x8-Block from block[comp] to backward_reference_frame */
	/* copy reconstructed 8x8 block from block[comp] to current_frame[]
		ISO/IEC 13818-2 section 7.6.8: Adding prediction and coefficient data
		This stage also embodies some of the operations implied by:
		- ISO/IEC 13818-2 section 7.6.7: Combining predictions
		- ISO/IEC 13818-2 section 6.1.3: Macroblock
	 */
	//DM02092003+ changed
	//DM08022004 081.6 int16 changed to float
	private void Add_Block(final int comp, int bx, final int by, final int dct_type[], final boolean addflag)
	{
		int cc, iincr;
		int rfp;
		final short Block_Ptr[] = block[comp];

		/* derive color component index */
		/* equivalent to ISO/IEC 13818-2 Table 7-1 */
		cc = cc_table[comp];


		if (cc == 0)
		{
			if (picture_structure == FRAME_PICTURE)
			{   //progressive
				if (dct_type[0] > 0)
				{
					rfp = current_frame[0] + (Coded_Picture_Width*(by+((comp&2)>>1))) + bx + ((comp&1)<<3);
					iincr = (Coded_Picture_Width<<1) - 8;
				}
				else
				{
					rfp = current_frame[0] + (Coded_Picture_Width*(by+((comp&2)<<2))) + bx + ((comp&1)<<3);
					iincr = Coded_Picture_Width - 8;
				}
			}
			else
			{
				rfp = current_frame[0] + ((Coded_Picture_Width<<1)*(by+((comp&2)<<2))) + bx + ((comp&1)<<3);
				iincr = (Coded_Picture_Width<<1) - 8;
			}
		}
		else
		{
			// chrominance
			// scale coordinates
			if (chroma_format != CHROMA444)
			{
				bx >>= 1;
			}

			if (picture_structure == FRAME_PICTURE)
			{  //two fields in one pic=std
				if ((dct_type[0] > 0) && (chroma_format != CHROMA420))
				{
					// field DCT coding
					rfp = current_frame[cc] + (Chroma_Width*(by+((comp&2)>>1))) + bx + (comp&8);
					iincr = (Chroma_Width<<1) - 8;
				}
				else
				{
					// frame DCT coding
					rfp = current_frame[cc] + (Chroma_Width*(by+((comp&2)<<2))) + bx + (comp&8);
					iincr = Chroma_Width - 8;
				}
			}
			else
			{
				// field picture, one field in one pic
				rfp = current_frame[cc] + ((Chroma_Width<<1)*(by+((comp&2)<<2))) + bx + (comp&8);
				iincr = (Chroma_Width<<1) - 8;
			}
		}

		iincr += 8;

		//DM02092003+
		int val, pPos;
		int gain = picture_coding_type == I_TYPE ? 128 : 0;

		gain += YGain;

		final int chroma_value_444 = chroma_format == CHROMA444 ? 1 : 0;
		final int chroma_value_420 = chroma_format != CHROMA420 ? 1 : 0;
		final int cc_value = cc == 1 ? 8 : 0;
		int x, y;
		int tmp1, tmp2, tmp3;

		//DM20082004 081.7 int10 changed
		if (cc == 0) //lumi
		{
			//	for (y = 0; y < 8; y++)
			for (y = 8; --y >= 0; )
			{
				tmp1 = rfp + (y * iincr);
				tmp2 = y<<3;

				//		for (x = 0; x < 8; x++)
				for (x = 8; --x >= 0; )
				{
					pPos = x + tmp1;
					val = Block_Ptr[x + tmp2] + gain;
					val = val < 0 ? 0 : (val > 255 ? 255 : val);

					pixels[pPos] |= val<<16; //Y
				}
			}
		}
		else
		{    //chroma cc1 = Cb, cc2=Cr
			if (chroma_format != CHROMA444)
			{
				rfp <<= 1;
				iincr <<= 1;
			}

			for (y = 0; y < 16; y++)
			{
				tmp1 = rfp + ((y >>(chroma_value_420)) * iincr);
				tmp3 = y >>1;
				tmp2 = (chroma_format != CHROMA420 ? tmp3 : ((y & 1) == 0 ? (tmp3 & ~dct_type[0]) : (tmp3 | dct_type[0])))<<3;

				for (x = 0; x < 16; x++)
				{
					pPos = (x >> chroma_value_444) + tmp1;
					val = 128 + Block_Ptr[(x >>1) + tmp2];
					val = val < 0 ? 0 : (val > 255 ? 255 : val);

					// cc==1 -> U  else V
					pixels[pPos] |= val << cc_value;

					x += chroma_value_444;
				}

				y += chroma_value_420;
			}
		}
	}
	//DM02092003-


	/* ISO/IEC 13818-2 section 6.3.17.1: Macroblock modes */
	private void macroblock_modes(final int pmacroblock_type[], final int pmotion_type[],
			final int pmotion_vector_count[], final int pmv_format[],
			final int pdmv[], final int pmvscale[], final int pdct_type[])
	{

		int macroblock_type, motion_type=0, motion_vector_count;
		int mv_format, dmv, mvscale, dct_type;

		/* get macroblock_type */
		macroblock_type = Get_macroblock_type();

		if (Fault_Flag > 0) {
			return;
		}

		/* get frame/field motion type */
		if ((macroblock_type & (MACROBLOCK_MOTION_FORWARD|MACROBLOCK_MOTION_BACKWARD)) > 0)
		{
			if (picture_structure==FRAME_PICTURE) {
				motion_type = (frame_pred_frame_dct>0) ? MC_FRAME : Get_Bits(2);
			} else {
				motion_type = Get_Bits(2);
			}
		}
		else if (((macroblock_type & MACROBLOCK_INTRA) > 0) && (concealment_motion_vectors > 0)) {
			motion_type = (picture_structure==FRAME_PICTURE) ? MC_FRAME : MC_FIELD;
		}

		/* derive motion_vector_count, mv_format and dmv, (table 6-17, 6-18) */
		if (picture_structure == FRAME_PICTURE)
		{
			motion_vector_count = (motion_type == MC_FIELD) ? 2 : 1;
			mv_format = (motion_type == MC_FRAME) ? MV_FRAME : MV_FIELD;
		}
		else
		{
			motion_vector_count = (motion_type == MC_16X8) ? 2 : 1;
			mv_format = MV_FIELD;
		}

		dmv = (motion_type == MC_DMV) ? 1 : 0; /* dual prime */

		/*
		field mv predictions in frame pictures have to be scaled
		ISO/IEC 13818-2 section 7.6.3.1 Decoding the motion vectors
		 */
		mvscale = ((mv_format==MV_FIELD) && (picture_structure == FRAME_PICTURE)) ? 1 : 0;

		/* get dct_type (frame DCT / field DCT) */
		dct_type = (picture_structure == FRAME_PICTURE) && (frame_pred_frame_dct < 1)
				&& ((macroblock_type & (MACROBLOCK_PATTERN|MACROBLOCK_INTRA)) > 0 )
				? Get_Bits(1) : 0;

				/* return values */
				pmacroblock_type[0] = macroblock_type;
				pmotion_type[0] = motion_type;
				pmotion_vector_count[0] = motion_vector_count;
				pmv_format[0] = mv_format;
				pdmv[0] = dmv;
				pmvscale[0] = mvscale;
				pdct_type[0] = dct_type;
	}


	private int Get_macroblock_type()
	{
		int macroblock_type=0;

		switch (picture_coding_type)
		{
		case I_TYPE:
			macroblock_type = Get_I_macroblock_type();
			break;

		case P_TYPE:
			macroblock_type = Get_P_macroblock_type();
			break;

		case B_TYPE:
			macroblock_type = Get_B_macroblock_type();
			break;
		}

		return macroblock_type;
	}


	private int Get_I_macroblock_type()
	{
		if (Get_Bits(1) > 0) {
			return 1;
		}

		if (Get_Bits(1) < 1) {
			Fault_Flag = 2;
		}

		return 17;
	}


	private int Get_P_macroblock_type()
	{
		int code;

		if ((code = Show_Bits(6)) >= 8)
		{
			code >>= 3;
				Flush_Bits(PMBtab0[code][1]);

				return PMBtab0[code][0];
		}

		if (code == 0)
		{
			Fault_Flag = 2;

			return 0;
		}

		Flush_Bits(PMBtab1[code][1]);

		return PMBtab1[code][0];
	}


	private int Get_B_macroblock_type()
	{
		int code;

		if ((code = Show_Bits(6)) >= 8)
		{
			code >>= 2;
				Flush_Bits(BMBtab0[code][1]);

				return BMBtab0[code][0];
		}

		if (code == 0)
		{
			Fault_Flag = 2;

			return 0;
		}

		Flush_Bits(BMBtab1[code][1]);

		return BMBtab1[code][0];
	}


	private int Get_motion_code()
	{
		int code;

		if (Get_Bits(1) > 0) {
			return 0;
		}

		if ((code = Show_Bits(9)) >= 64)
		{
			code >>= 6;
					Flush_Bits(MVtab0[code][1]);

					return ((Get_Bits(1) > 0) ? -MVtab0[code][0] : MVtab0[code][0]);
		}

		if (code >= 24)
		{
			code >>= 3;
					Flush_Bits(MVtab1[code][1]);

					return ((Get_Bits(1) > 0) ? -MVtab1[code][0] : MVtab1[code][0]);
		}

		if ((code -= 12) < 0)
		{
			Fault_Flag = 10;

			return 0;
		}

		Flush_Bits(MVtab2[code][1]);

		return ((Get_Bits(1)>0) ? -MVtab2[code][0] : MVtab2[code][0]);
	}


	/**
	 * get differential motion vector (for dual prime prediction)
	 */
	private int Get_dmvector()
	{
		if (Get_Bits(1) > 0) {
			return ((Get_Bits(1) > 0) ? -1 : 1);
		} else {
			return 0;
		}
	}


	/**
	 * performs YUV to RGB conversion
	 */
	private int YUVtoRGB(final int YUV)
	{
		final int T  = 0xFF;
		final int Y  = 0xFF & (YUV>>>16);
		final int Cb = 0xFF & (YUV>>>8);
		final int Cr = 0xFF & YUV;

		if (Y == 0) {
			return 0;
		}

		int R = (int)(Y +(1.402f * (Cr-128)));
		int G = (int)(Y -(0.34414 * (Cb-128)) -(0.71414 * (Cr-128)));
		int B = (int)(Y +(1.722 * (Cb-128)));

		R = R < 0 ? 0 : (R > 0xFF ? 0xFF : R);
		G = G < 0 ? 0 : (G > 0xFF ? 0xFF : G);
		B = B < 0 ? 0 : (B > 0xFF ? 0xFF : B);

		return ((T<<24) | (R<<16) | (G<<8) | B);
	}


	/**
	 *
	 */
	public int[] getPixels()
	{
		return pixels;
	}


	/**
	 *
	 */
	public int getWidth()
	{
		return horizontal_size;
	}

	/**
	 *
	 */
	public int getHeight()
	{
		return vertical_size;
	}


	/**
	 *
	 */
	public void resetProcessedPosition()
	{
		PositionList.clear();
	}


	/**
	 *
	 */
	public List getPositions()
	{
		return PositionList;
	}


	/**
	 *
	 */
	public int getErrors()
	{
		return (0 | (ERROR1 ? 1 : 0) | (ERROR2 ? 2 : 0) | (ERROR3 ? 4 : 0) | (ERROR4 ? 8 : 0) | (ERROR5 ? 0x10 : 0) | (ERROR6 ? 0x20 : 0));
	}


	/**
	 * returns arrays byteposition offset of 1st successful decoded GOP
	 * interface, entry point to decode picture for preview
	 *
	 * @param1 - ES byte array
	 * @param2 - search direction
	 * @param3 - enable GOPheader alignment
	 * @param4 - simple_fast decode
	 * @return
	 */
	public long decodeArray(final byte array[], final boolean direction, final boolean _viewGOP, final boolean fast, final int yGain)
	{
		return decodeArray(array, 0, direction, _viewGOP, fast, yGain, false);
	}

	/**
	 * returns arrays byteposition offset of 1st successful decoded GOP
	 * interface, entry point to decode picture for preview
	 *
	 * @param1 - ES byte array
	 * @param2 - search direction
	 * @param3 - enable GOPheader alignment
	 * @param4 - simple_fast decode
	 * @return
	 */
	public long decodeArray(final byte array[], final boolean direction, final boolean _viewGOP, final boolean fast, final int yGain, final boolean silent)
	{
		return decodeArray(array, 0, direction, _viewGOP, fast, yGain, silent);
	}

	/**
	 * returns arrays byteposition offset of 1st successful decoded GOP
	 * interface, entry point to decode picture for preview
	 *
	 * @param1 - ES byte array
	 * @param2 - start index position
	 * @param3 - search direction
	 * @param4 - enable GOPheader alignment
	 * @param5 - simple_fast decode
	 * @return
	 */
	public long decodeArray(final byte[] array, final int start_position, final boolean direction, final boolean _viewGOP, final boolean fast, final int yGain, final boolean silent)
	{
		// setAcceleration(Common.getSettings().getBooleanProperty(Keys.KEY_Preview_fastDecode));

		FAST = fast;
		DIRECTION = direction;
		YGain = yGain;

		ERROR1 = false;
		ERROR2 = false;
		ERROR3 = false;
		ERROR4 = false;
		ERROR5 = false;
		ERROR6 = false;
		Arrays.fill(LastPosVal, -1);

		buf = array;
		BufferPos = start_position;
		BitPos = BufferPos<<3;
		StartPos = BufferPos;
		viewGOP = _viewGOP;

		if (DIRECTION)
		{
			StartPos = BufferPos = buf.length - 4;
			BitPos = BufferPos<<3;
		}

		try {

			while ((BufferPos < buf.length) && (BufferPos >= 0))
			{
				ERROR_CODE1 = extern_Get_Hdr();

				if ( ERROR_CODE1 == 1 )
				{
					if (picture_coding_type != I_TYPE)
					{
						BufferPos += 2048;
						continue;
					}
					InitialDecoder();
					Decode_Picture();

					return StartPos;
				}

				else if (ERROR_CODE1 == 2 )
				{
					return 0;
				} else {
					BufferPos++;
				}
			}

			ERROR2 = true;

		} catch (final ArrayIndexOutOfBoundsException ae) {
			ERROR1 = true;

		} catch (final Error ee) {
			ERROR1 = true;
			ERROR6 = ee.toString().indexOf("OutOfMemory") > 0;
		}


		return 0;
	}




	public BufferedImage getImage() {
		return getImage(getWidth(),getHeight());
	}

	public BufferedImage getImage(final int w, final int h) {

		final int pix[] = new int[w*h];
		//~50ms
		for (int y = 0;  y < h;  y++)
		{
			final int oY= getWidth()*((y*getHeight())/h);
			for (int x = 0;  x < w;  x++){
				final int oX=(x*getWidth())/w;
				pix[x + (y*w)] = YUVtoRGB(pixels[oX + oY]);
			}
		}

		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, w, h, pix, 0, w);
		return image;
	}

	///


}