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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

/**
 * @author Eric Berendsen
 *
 * Based on ETSI EN 300 706 V1.2.1 (Enhanced Teletext specification) 12.3 Non-spacing attributes and additional characters
 * and ETSI EN 300 231 V1.3.1  (Specification of the domestic video Programme Delivery Control system (PDC))  7.3.2.3 Coding of preselection data in extension packets X/26
 *
 */
public class TxtTriplet extends
Triplet implements TreeNode {


	// From CharSet.java , part of ProjectX
	//DM10082004 081.7 int08 changed
	//A=65 .. Z=90
	private static final short diacritical_uppercase_char_map[][] = {
		{ 0, 192, 193, 194, 195, 256, 258, 0, 196, 0, 197, 0, 0, 0, 260, 258 }, //A
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //B
		{ 0, 0, 262, 264, 0, 0, 268, 266, 0, 0, 0, 199, 0, 0, 0, 268 }, //C
		{ 0, 0, 0, 0, 0, 0, 270, 0, 0, 0, 0, 0, 0, 0, 0, 270 }, //D
		{ 0, 200, 201, 202, 0, 274, 276, 278, 203, 0, 0, 0, 0, 0, 280, 282 }, //E
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //F
		{ 0, 0, 0, 284, 0, 0, 286, 288, 0, 0, 0, 290, 0, 0, 0, 0 }, //G
		{ 0, 0, 0, 292, 0, 294, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //H
		{ 0, 204, 205, 206, 296, 298, 300, 304, 207, 0, 0, 0, 0, 0, 302, 300 }, //I
		{ 0, 0, 0, 308, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //J
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 310, 0, 0, 0, 0 }, //K
		{ 0, 0, 313, 0, 0, 0, 0, 319, 0, 0, 0, 315, 0, 0, 0, 317 }, //L
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //M
		{ 0, 0, 323, 0, 209, 0, 0, 0, 0, 0, 0, 325, 0, 0, 0, 327 }, //N
		{ 0, 210, 211, 212, 213, 332, 334, 0, 214, 0, 0, 0, 0, 336, 0, 334 }, //O
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //P
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //Q
		{ 0, 0, 340, 0, 0, 0, 0, 0, 0, 0, 0, 342, 0, 0, 0, 344 }, //R
		{ 0, 0, 346, 348, 0, 0, 0, 0, 0, 0, 0, 350, 0, 0, 0, 352 }, //S
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 354, 0, 0, 0, 356 }, //T
		{ 0, 217, 218, 219, 360, 362, 364, 0, 220, 0, 366, 0, 0, 368, 370, 364 }, //U
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //V
		{ 0, 0, 0, 372, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //W
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //X
		{ 0, 0, 221, 374, 0, 0, 0, 0, 376, 0, 0, 0, 0, 0, 0, 0 }, //Y
		{ 0, 0, 377, 0, 0, 0, 0, 379, 0, 0, 0, 0, 0, 0, 0, 381 }, //Z
	};

	//DM10082004 081.7 int08 changed
	//a=97 .. z=122
	private static final short diacritical_lowercase_char_map[][] = {
		{ 0, 224, 225, 226, 227, 257, 259, 0, 228, 0, 229, 0, 0, 0, 261, 259 }, //a
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //b
		{ 0, 0, 263, 265, 0, 0, 269, 267, 0, 0, 0, 231, 0, 0, 0, 269 }, //c
		{ 0, 0, 0, 0, 0, 0, 271, 0, 0, 0, 0, 0, 0, 0, 0, 271 }, //d
		{ 0, 232, 233, 234, 0, 275, 277, 279, 235, 0, 0, 0, 0, 0, 281, 283 }, //e
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //f
		{ 0, 0, 0, 285, 0, 0, 287, 289, 0, 0, 0, 291, 0, 0, 0, 0 }, //g
		{ 0, 0, 0, 293, 0, 295, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //h
		{ 0, 236, 237, 238, 297, 299, 301, 305, 239, 0, 0, 0, 0, 0, 303, 301 }, //i
		{ 0, 0, 0, 309, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //j
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 311, 0, 0, 0, 0 }, //k
		{ 0, 0, 314, 0, 0, 0, 0, 320, 0, 0, 0, 316, 0, 0, 0, 318 }, //l
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //m
		{ 0, 0, 324, 0, 241, 0, 0, 0, 0, 0, 0, 326, 0, 0, 0, 328 }, //n
		{ 0, 242, 243, 244, 245, 333, 335, 0, 246, 0, 0, 0, 0, 337, 0, 335 }, //o
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //p
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //q
		{ 0, 0, 341, 0, 0, 0, 0, 0, 0, 0, 0, 343, 0, 0, 0, 345 }, //r
		{ 0, 0, 347, 349, 0, 0, 0, 0, 0, 0, 0, 351, 0, 0, 0, 353 }, //s
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 355, 0, 0, 0, 357 }, //t
		{ 0, 249, 250, 251, 361, 363, 365, 0, 252, 0, 367, 0, 0, 369, 371, 365 }, //u
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //v
		{ 0, 0, 0, 373, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //w
		{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //x
		{ 0, 0, 253, 375, 0, 0, 0, 0, 255, 0, 0, 0, 0, 0, 0, 0 }, //y
		{ 0, 0, 378, 0, 0, 0, 0, 380, 0, 0, 0, 0, 0, 0, 0, 382 }, //z
	};


	public static short getCombinedCharacter(final int basic_char, final int combine_char)
	{
		short val = 0;

		if ((basic_char >= 65) && (basic_char <= 90)) {
			val = diacritical_uppercase_char_map[basic_char - 65][combine_char];
		} else if ((basic_char >= 97) && (basic_char <= 122)) {
			val = diacritical_lowercase_char_map[basic_char - 97][combine_char];
		}


		if (val == 0) {
			return (short)basic_char;
		} else {
			return val;
		}
	}

	public static final short G0_sets[][] = {
		{
			//0 = latin
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
			0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
			0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f,
			0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f,
			0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f,
			0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e, 0x0020
		},{
			//1 = cyrillic-1, //DM08082004 081.7 int08 changed
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
			0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
			0x0427, 0x0410, 0x0411, 0x0426, 0x0414, 0x0415, 0x0424, 0x0413, 0x0425, 0x0418, 0x0408, 0x041a, 0x041b, 0x041c, 0x041d, 0x041e,
			0x041f, 0x040c, 0x0420, 0x0421, 0x0422, 0x0423, 0x0412, 0x0403, 0x0409, 0x040a, 0x0417, 0x040b, 0x0416, 0x0402, 0x0428, 0x040f,
			0x0447, 0x0430, 0x0431, 0x0446, 0x0434, 0x0435, 0x0444, 0x0433, 0x0445, 0x0438, 0x0458, 0x043a, 0x043b, 0x043c, 0x043d, 0x043e,
			0x043f, 0x045c, 0x0440, 0x0441, 0x0442, 0x0443, 0x0432, 0x0453, 0x0459, 0x045a, 0x044d, 0x045b, 0x0436, 0x0452, 0x0448, 0x0020
		},{
			//2 = cyrillic-2, //DM08082004 081.7 int08 changed
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x044b, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
			0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
			0x042e, 0x0410, 0x0411, 0x0426, 0x0414, 0x0415, 0x0424, 0x0413, 0x0425, 0x0418, 0x0419, 0x041a, 0x041b, 0x041c, 0x041d, 0x041e,
			0x041f, 0x042f, 0x0420, 0x0421, 0x0422, 0x0423, 0x0416, 0x0412, 0x042c, 0x042a, 0x0417, 0x0428, 0x042d, 0x0429, 0x0427, 0x042b,
			0x044e, 0x0430, 0x0431, 0x0446, 0x0434, 0x0435, 0x0444, 0x0433, 0x0445, 0x0438, 0x0439, 0x043a, 0x043b, 0x043c, 0x043d, 0x043e,
			0x043f, 0x044f, 0x0440, 0x0441, 0x0442, 0x0443, 0x0436, 0x0432, 0x044c, 0x044a, 0x0437, 0x0448, 0x044d, 0x0449, 0x0447, 0x0020
		},{
			//3 = cyrillic-3, //DM08082004 081.7 int08 changed
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x0457, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
			0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
			0x042e, 0x0410, 0x0411, 0x0426, 0x0414, 0x0415, 0x0424, 0x0413, 0x0425, 0x0418, 0x0419, 0x041a, 0x041b, 0x041c, 0x041d, 0x041e,
			0x041f, 0x042f, 0x0420, 0x0421, 0x0422, 0x0423, 0x0416, 0x0412, 0x042c, 0x0406, 0x0417, 0x0428, 0x0404, 0x0429, 0x0427, 0x0407,
			0x044e, 0x0430, 0x0431, 0x0446, 0x0434, 0x0435, 0x0444, 0x0433, 0x0445, 0x0438, 0x0439, 0x043a, 0x043b, 0x043c, 0x043d, 0x043e,
			0x043f, 0x044f, 0x0440, 0x0441, 0x0442, 0x0443, 0x0436, 0x0432, 0x044c, 0x0456, 0x0437, 0x0448, 0x0454, 0x0449, 0x0447, 0x0020
		},{
			//4 = greek, //DM08082004 081.7 int08 changed
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
			0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x00ab, 0x003d, 0x00bb, 0x003f,
			0x0390, 0x0391, 0x0392, 0x0393, 0x0394, 0x0395, 0x0396, 0x0397, 0x0398, 0x0399, 0x039a, 0x039b, 0x039c, 0x039d, 0x039e, 0x039f,
			0x03a0, 0x03a1, 0x0384, 0x03a3, 0x03a4, 0x03a5, 0x03a6, 0x03a7, 0x03a8, 0x03a9, 0x03aa, 0x03ab, 0x03ac, 0x03ad, 0x03ae, 0x03af,
			0x03b0, 0x03b1, 0x03b2, 0x03b3, 0x03b4, 0x03b5, 0x03b6, 0x03b7, 0x03b8, 0x03b9, 0x03ba, 0x03bb, 0x03bc, 0x03bd, 0x03be, 0x03bf,
			0x03c0, 0x03c1, 0x03c2, 0x03c3, 0x03c4, 0x03c5, 0x03c6, 0x03c7, 0x03c8, 0x03c9, 0x03ca, 0x03cb, 0x03cc, 0x03cd, 0x03ce, 0x0020
		},{
			//5 = arabic, still a copy of latin!
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
			0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x061f,
			0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f,
			0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f,
			0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f,
			0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e, 0x0020
		},{
			//6 = hebrew, //DM08082004 081.7 int08 changed
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0021, 0x0022, 0x0023, 0x00a4, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f,
			0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f,
			0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f,
			0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x2190, 0x00bd, 0x2192, 0x2191, 0x0023,
			0x05d0, 0x05d1, 0x05d2, 0x05d3, 0x05d4, 0x05d5, 0x05d6, 0x05d7, 0x05d8, 0x05d9, 0x05da, 0x05db, 0x05dc, 0x05dd, 0x05de, 0x05df,
			0x05e0, 0x05e1, 0x05e2, 0x05e3, 0x05e4, 0x05e5, 0x05e6, 0x05e7, 0x05e8, 0x05e9, 0x05ea, 0x0020, 0x05f0, 0x00bc, 0x00f7, 0x0020
		}
	};

	//DM10082004 081.7 int08 changed
	public static final short G2_sets[][] = {
		{
			//0 = latin
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x00a1, 0x00a2, 0x00a3, 0x0024, 0x00a5, 0x0023, 0x00a7, 0x00a4, 0x00b4, 0x0022, 0x00ab, 0x003c, 0x005e, 0x003d, 0x0076,
			0x00b0, 0x00b1, 0x00b2, 0x00b3, 0x00d7, 0x00b5, 0x00b6, 0x00b7, 0x00f7, 0x00b4, 0x0022, 0x00bb, 0x00bc, 0x00bd, 0x00be, 0x00bf,
			0x0020, 0x0060, 0x00b4, 0x02c6, 0x007e, 0x02c9, 0x02d8, 0x02d9, 0x0308, 0x002e, 0x02da, 0x0020, 0x005f, 0x0022, 0x0020, 0x02d8,
			0x002d, 0x00b9, 0x00ae, 0x00a9, 0x2122, 0x002a, 0x20ac, 0x2030, 0x03b1, 0x0020, 0x0020, 0x0020, 0x002a, 0x002a, 0x002a, 0x002a,
			0x03a9, 0x00c6, 0x0110, 0x0061, 0x0126, 0x0020, 0x0132, 0x013f, 0x0141, 0x00d8, 0x0152, 0x006f, 0x00de, 0x0166, 0x014a, 0x0149,
			0x0138, 0x00e6, 0x0111, 0x010f, 0x0127, 0x0131, 0x0133, 0x0140, 0x0142, 0x00f8, 0x0153, 0x00df, 0x00fe, 0x0167, 0x014b, 0x0020,
		},{
			//1 = cyrillic, //DM08082004 081.7 int08 changed
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x00a1, 0x00a2, 0x00a3, 0x0024, 0x00a5, 0x0020, 0x00a7, 0x0020, 0x00b4, 0x0022, 0x00ab, 0x003c, 0x005e, 0x003d, 0x0076,
			0x00b0, 0x00b1, 0x00b2, 0x00b3, 0x00d7, 0x00b5, 0x00b6, 0x00b7, 0x00f7, 0x00b4, 0x0022, 0x00bb, 0x00bc, 0x00bd, 0x00be, 0x00bf,
			0x0020, 0x0060, 0x00b4, 0x02c6, 0x007e, 0x02c9, 0x02d8, 0x02d9, 0x0308, 0x002e, 0x02da, 0x0020, 0x005f, 0x0022, 0x0020, 0x02d8,
			0x002d, 0x00b9, 0x00ae, 0x00a9, 0x2122, 0x002a, 0x20ac, 0x2030, 0x03b1, 0x0141, 0x0142, 0x00df, 0x002a, 0x002a, 0x002a, 0x002a,
			0x0044, 0x0045, 0x0046, 0x0047, 0x0049, 0x004a, 0x004b, 0x004c, 0x004e, 0x0051, 0x0052, 0x0053, 0x0055, 0x0056, 0x0057, 0x005a,
			0x0064, 0x0065, 0x0066, 0x0067, 0x0069, 0x006a, 0x006b, 0x006c, 0x006e, 0x0071, 0x0072, 0x0073, 0x0075, 0x0076, 0x0077, 0x007a,
		},{
			//2 = greek, //DM08082004 081.7 int08 changed
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0061, 0x0062, 0x00a3, 0x0065, 0x0068, 0x0069, 0x00a7, 0x003a, 0x00b4, 0x0022, 0x006b, 0x003c, 0x005e, 0x003d, 0x0076,
			0x00b0, 0x00b1, 0x00b2, 0x00b3, 0x00d7, 0x006d, 0x006e, 0x0070, 0x00f7, 0x00b4, 0x0022, 0x0074, 0x00bc, 0x00bd, 0x00be, 0x0078,
			0x0020, 0x0060, 0x00b4, 0x02c6, 0x007e, 0x02c9, 0x02d8, 0x02d9, 0x0308, 0x002e, 0x02da, 0x0020, 0x005f, 0x0022, 0x0020, 0x02d8,
			0x003f, 0x00b9, 0x00ae, 0x00a9, 0x2122, 0x002a, 0x20ac, 0x2030, 0x03b1, 0x038a, 0x038e, 0x038f, 0x002a, 0x002a, 0x002a, 0x002a,
			0x0043, 0x0044, 0x0046, 0x0047, 0x004a, 0x004c, 0x0051, 0x0052, 0x0053, 0x0055, 0x0056, 0x0057, 0x0059, 0x005a, 0x0386, 0x0389,
			0x0063, 0x0064, 0x0066, 0x0067, 0x006a, 0x006c, 0x0071, 0x0072, 0x0073, 0x0075, 0x0076, 0x0077, 0x0079, 0x007a, 0x0388, 0x0020,
		},{
			//3 = arabic, still a copy of latin!
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020, 0x0020,
			0x0020, 0x00a1, 0x00a2, 0x00a3, 0x0024, 0x00a5, 0x0023, 0x00a7, 0x00a4, 0x00b4, 0x0022, 0x00ab, 0x003c, 0x005e, 0x003d, 0x0076,
			0x00b0, 0x00b1, 0x00b2, 0x00b3, 0x00d7, 0x00b5, 0x00b6, 0x00b7, 0x00f7, 0x00b4, 0x0022, 0x00bb, 0x00bc, 0x00bd, 0x00be, 0x00bf,
			0x0020, 0x0060, 0x00b4, 0x02c6, 0x007e, 0x02c9, 0x02d8, 0x02d9, 0x0308, 0x002e, 0x02da, 0x0020, 0x005f, 0x0022, 0x0020, 0x02d8,
			0x002d, 0x00b9, 0x00ae, 0x00a9, 0x2122, 0x002a, 0x20ac, 0x2030, 0x0020, 0x0020, 0x0020, 0x0020, 0x002a, 0x002a, 0x002a, 0x002a,
			0x03a9, 0x00c6, 0x0110, 0x0061, 0x0126, 0x0020, 0x0132, 0x013f, 0x0141, 0x00d8, 0x0152, 0x006f, 0x00de, 0x0166, 0x014a, 0x0149,
			0x0138, 0x00e6, 0x0111, 0x010f, 0x0127, 0x0131, 0x0133, 0x0140, 0x0142, 0x00f8, 0x0153, 0x00df, 0x00fe, 0x0167, 0x014b, 0x0020,
		}
	};


	private static final short national_subsets[][] = {
		// 0x23     0x24    0x40    0x5b    0x5c    0x5d    0x5e    0x5f   0x60    0x7b     0x7c   0x7d    0x7e
		{ 0x00a3, 0x0024, 0x0040, 0x00ab, 0x00bd, 0x00bb, 0x005e, 0x0023, 0x002d, 0x00bc, 0x00a6, 0x00be, 0x00f7 }, // english ,000
		{ 0x00e9, 0x00ef, 0x00e0, 0x00eb, 0x00ea, 0x00f9, 0x00ee, 0x0023, 0x00e8, 0x00e2, 0x00f4, 0x00fb, 0x00e7 }, // french  ,001
		{ 0x0023, 0x00a4, 0x00c9, 0x00c4, 0x00d6, 0x00c5, 0x00dc, 0x005f, 0x00e9, 0x00e4, 0x00f6, 0x00e5, 0x00fc }, // swedish,finnish,hungarian ,010
		{ 0x0023, 0x016f, 0x010d, 0x0165, 0x017e, 0x00fd, 0x00ed, 0x0159, 0x00e9, 0x00e1, 0x0115, 0x00fa, 0x0161 }, // czech,slovak  ,011
		{ 0x0023, 0x0024, 0x00a7, 0x00c4, 0x00d6, 0x00dc, 0x005e, 0x005f, 0x00b0, 0x00e4, 0x00f6, 0x00fc, 0x00df }, // german ,100
		{ 0x00e7, 0x0024, 0x00a1, 0x00e1, 0x00e9, 0x00ed, 0x00f3, 0x00fa, 0x00bf, 0x00fc, 0x00f1, 0x00e8, 0x00e0 }, // portuguese,spanish ,101
		{ 0x00a3, 0x0024, 0x00e9, 0x00b0, 0x00e7, 0x00bb, 0x005e, 0x0023, 0x00f9, 0x00e0, 0x00f2, 0x00e8, 0x00ec }, // italian  ,110
		{ 0x0023, 0x00a4, 0x0162, 0x00c2, 0x015e, 0x0102, 0x00ce, 0x0131, 0x0163, 0x00e2, 0x015f, 0x0103, 0x00ee }, // rumanian ,111
		{ 0x0023, 0x0024, 0x0160, 0x0117, 0x0119, 0x017d, 0x010d, 0x016b, 0x0161, 0x0105, 0x0173, 0x017e, 0x012f }, // lettish,lithuanian ,1000
		{ 0x0023, 0x0144, 0x0105, 0x005a, 0x015a, 0x0141, 0x0107, 0x00f3, 0x0119, 0x017c, 0x015b, 0x0142, 0x017a }, // polish,  1001
		{ 0x0023, 0x00cb, 0x010c, 0x0106, 0x017d, 0x0110, 0x0160, 0x00eb, 0x010d, 0x0107, 0x017e, 0x0111, 0x0161 }, // serbian,croatian,slovenian, 1010
		{ 0x0023, 0x00f5, 0x0160, 0x00c4, 0x00d6, 0x017e, 0x00dc, 0x00d5, 0x0161, 0x00e4, 0x00f6, 0x017e, 0x00fc }, // estonian  ,1011
		{ 0x0054, 0x011f, 0x0130, 0x015e, 0x00d6, 0x00c7, 0x00dc, 0x011e, 0x0131, 0x015f, 0x00f6, 0x00e7, 0x00fc }, // turkish  ,1100
		null  //res.
	};

	//DM10082004 081.7 int08 changed
	//4 bits main tripl + 3 bits character_set
	private static final int national_subset_mapping[][] = {
		{ 0, 1, 2, 3, 4, 5, 6, 7 },  //0, en,fr,se,cz,de,es,it,ro
		{ 9, 1, 2, 3, 4, 5, 6, 7 },  //1, pl,fr,se,cz,de,es,it,ro
		{ 0, 1, 2, 12, 4, 5, 6, 7 }, //2, en,fr,se,tr,de,es,it,ro
		{ 13, 13, 13, 13, 13, 10, 13, 7 }, //3, en,fr,se,cz,de,cr,it,ro
		{ 13, 13, 11, 3, 4, 13, 8, 13 }, //4, et,cz,de,cr
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //5, res!
		{ 13, 13, 13, 12, 13, 13, 13, 13 }, //6, res,res,res,tr,res,res,res,(gr)
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //7, res!
		{ 0, 1, 13, 13, 13, 13, 13, 13 },  //8, en,fr,res,res,res,res,res,(ar)
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //9, res!
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //10, res,res,res,res,res,(he),res,(ar)
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //11, res!
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //12, res!
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //13, res!
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //14, res!
		{ 13, 13, 13, 13, 13, 13, 13, 13 },  //15, res!
	};




	/**
	 *
	 */
	public TxtTriplet(final byte[] data, final int offset) {
		super(data,offset);
	}

	public int getAddress(){
		return val&0x00003f;
	}

	public int getMode(){
		return (val&0x007c0)>>6;
	}

	public int getData(){
		return (val&0x3f800)>>11;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final int address = val&0x00003f;
		final int mode = (val&0x007c0)>>6;
		final int data = (val&0x3f800)>>11;

		// Should be StringBuilder, so sue me...
		String str = "";

		if(address < 40){
			str +="column "+address+", ";
			if(mode == 0){ //Foreground Colour
				final int clut = (data & 0x18)>>3;
				final int clutEntry = (data & 0x7);
				str += "clut "+clut+", entry "+clutEntry;
			}
			if(mode == 3){ //Background Colour
				final int clut = (data & 0x18)>>3;
				final int clutEntry = (data & 0x7);
				str += "clut "+clut+", entry "+clutEntry;
			}
			if(mode == 6){ // PDC - Cursor Column & Announced Starting & Finishing Time Minutes
				final int minutesUnits = (data & 0x0F);
				final int minutesTens = (data & 0x70)>>4;
				str += "minutes "+minutesTens+minutesUnits; // add (append) as string, not numbers
			}
			if(mode == 12){ //Display attributes
				final int doubleWidth = (data & 0x40)>>6;
				final int underline = (data & 0x20)>>5;
				final int invert = (data & 0x10)>>4;
				final int conceal = (data & 0x4)>>2;
				final int boxing = (data & 0x2)>>1;
				final int doubleHeigth = (data & 0x1);
				str+= "Double Width:"+doubleWidth+", Underline/Separated Mosaics:"+underline+", Invert Colour:"+invert+", Conceal:"+conceal+", Boxing/Window:"+boxing+", Double Height:"+doubleHeigth;
			}
			if(mode == 13){ //DRCS Character Invocation
				final int normal = (data & 0x40)>>6;
				final int chr = (data & 0x3F);
				str+= (normal==1?"normal":"global")+", character "+chr;
			}

			if(mode > 16){ //char from G0 set w/ diacr.

				str += (char)data + "+ " + (char)G2_sets[0][64+ (mode & 0xF)]+"  ";  // no typo, need G2_sets, but I don't understand why......????
				str += (char)getCombinedCharacter(data, mode & 0xF);
			}
			if(mode == 15){ //Character from the G2 Supplementary Set
				str += ""+(char)G2_sets[0][data]+"";
			}
			if(mode==16){
				//NOTE 3: The @ symbol replaces the * symbol at position 2/A when the table is accessed via a packet X/26 Column
				// Address triplet with Mode Description = 10 000 and Data = 0101010. See clause 12.2.4.
				str += ""+(char)G0_sets[0][data]+"";
			}
		}else{ // address >= 40
			if(mode==0x04){// set active position
				str += "Row "+ getRow(address)+", column "+ ((data<40)? data:"undefined");
			}
			// PDC related
			if(mode==0x08){// PDC - Country of Origin and Programme Source
				str += "Country of Origin "+ (address&0x0F)+", Programme Source "+ (data&0x03F); //ETSI EN 300 231 V1.3.1 says in §7.3.2.3:  "4 least-significant bits: Country of Origin", but TS 101 231 Codes Register (2010-12) only matches when we use 6
			}
			if(mode==0x09){// PDC - Month & Day
				str += "Month "+ (address&0xF)+", Day "+ ((data&0x30)>>4)+""+(data&0x0F);
			}
			if(mode==0x0A){// PDC - Cursor Row & Announced Starting Time Hours

				str += "Row "+ getRow(address)+", hours "+ getHoursString(data)+", Controlled Access Flag "+ ((data&0x40)>0?"controlled access":"free access");
			}
			if(mode==0x0B){// PDC - Cursor Row & Announce Finishing Time Hours
				str += "Row "+ getRow(address)+", hours "+ getHoursString(data)+", duration "+((data&0x40)>0?"programme duration":"finishing time");
			}
			if(mode==0x10){// Origin Modifier
				str += "row offset  "+ (address-40)+", column offset "+ (data&0x0F);
			}

			if((mode>=0x11)&&(mode<=0x13)){// Object Invocation
				final int objectSource = (address&0x18)>>3;
			final int objectType = (mode&0x3);
			final int subPageS1 =data&0xF;
			final int ptrLocation = address &0x3;
			final int tripletOffset = (data & 0x60)>>5;
			final int ptrPosition = (data&0x10)>>4;
			final int objectNo = (ptrLocation<<3)|(tripletOffset<<1)|ptrPosition;

			str += "object source"+ objectSource+" ("+EBUTeletextHandler.getObjectSourceString(objectSource)+"), object type:"+ objectType+
					" ("+EBUTeletextHandler.getObjectTypeString(objectType) +") objectNo:"+ objectNo +", SubPageS1:"+subPageS1+", ptrLocation:"+ptrLocation+", tripletOffset "+tripletOffset+" ptrPosition "+ptrPosition;
			}
			if((mode>=0x15)&&(mode<=0x17)){// Object Definition
				String objectUsage ="";
				switch ((address&0x18)>>3) {
				case 0:
					objectUsage ="illegal";
					break;
				case 1:
					objectUsage ="Level 2.5";
					break;
				case 2:
					objectUsage ="Level 3.5";
					break;
				case 3:
					objectUsage ="Levels 2.5 and 3.5";
					break;
				default:
					objectUsage ="illegal";
					break;
				}
				str += "Object Usage "+ objectUsage+", packet within object page containing the pointer to this object "+ (1+(address&0x03));
			}

		}
		if(str.length()>0){
			str = " ("+str+")";
		}

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Triplet",val,getModeString(mode,address)+str));
		t.add(new DefaultMutableTreeNode(new KVP("mode",mode,getModeString(mode,address))));
		t.add(new DefaultMutableTreeNode(new KVP("address/data word A",address,(address<=39)?"Column Address Group":"Row Address Group "+(address==40?24:address-40))));
		t.add(new DefaultMutableTreeNode(new KVP("data/data word B",data,null)));
		return t;
	}

	/**
	 * @param data
	 * @return
	 */
	private static String getHoursString(final int data) {
		StringBuilder hoursString = new StringBuilder();
		int hoursUnits= data & 0x0F;
		int hoursTens= ((data&0x30)>>4);
		hoursString.append(hoursTens).append(hoursUnits);
		return hoursString.toString();
	}

	/**
	 * @param address
	 * @return
	 */
	protected int getRow(final int address) {
		int row;
		if(address==40){
			row=24;
		}else{
			row = address-40;
		}
		return row;
	}

	public static String getModeString(final int mode, final int address){
		if(address<=39){
			switch (mode) {

			case 0: return "Foreground Colour";
			case 1: return "Block Mosaic Character from the G1 set";
			case 2: return "Line Drawing or Smoothed Mosaic Character from the G3 set (Level 1.5)";
			case 3: return "Background Colour";
			case 4: return "Reserved";
			case 5: return "Reserved";
			case 6: return "PDC - Cursor Column & Announced Starting & Finishing Time Minutes";
			case 7: return "Additional Flash Functions";
			case 8: return "Modified G0 and G2 Character Set Design.";
			case 9: return "Character from the G0 set (Levels 2.5 & 3.5)";
			case 10: return "Reserved";
			case 11: return "Line Drawing or Smoothed Mosaic Character from the G3 set (Levels 2.5 & 3.5)";
			case 12: return "Display Attributes";
			case 13: return "DRCS Character Invocation";
			case 14: return "Font Style";
			case 15: return "Character from the G2 set";
			case 16: return "G0 character without diacritical mark";
			case 17: return "G0 character with diacritical mark";
			case 18: return "G0 character with diacritical mark";
			case 19: return "G0 character with diacritical mark";
			case 20: return "G0 character with diacritical mark";
			case 21: return "G0 character with diacritical mark";
			case 22: return "G0 character with diacritical mark";
			case 23: return "G0 character with diacritical mark";
			case 24: return "G0 character with diacritical mark";
			case 25: return "G0 character with diacritical mark";
			case 26: return "G0 character with diacritical mark";
			case 27: return "G0 character with diacritical mark";
			case 28: return "G0 character with diacritical mark";
			case 29: return "G0 character with diacritical mark";
			case 30: return "G0 character with diacritical mark";
			case 31: return "G0 character with diacritical mark";
			default:
				return "Illegal value";
			}
		}else{
			switch (mode) {

			case 0: return "Full Screen Colour";
			case 1: return "Full Row Colour";
			case 2: return "Reserved";
			case 3: return "Reserved";
			case 4: return "Set Active Position";
			case 5: return "Reserved";
			case 6: return "Reserved";
			case 7: return "Address Display Row 0";
			case 8: return "PDC - Country of Origin and Programme Source";
			case 9: return "PDC - Month & Day";
			case 10: return "PDC - Cursor Row & Announced Starting Time Hours";
			case 11: return "PDC - Cursor Row & Announce Finishing Time Hours";
			case 12: return "PDC - Cursor Row & Local Time Offset";
			case 13: return "PDC - Series Identifier and Series Code";
			case 14: return "Reserved";
			case 15: return "Reserved";
			case 16: return "Origin Modifier";
			case 17: return "Active Object Invocation";
			case 18: return "Adaptive Object Invocation";
			case 19: return "Passive Object Invocation";
			case 20: return "Reserved";
			case 21: return "Active Object Definition";
			case 22: return "Adaptive Object Definition";
			case 23: return "Passive Object Definition";
			case 24: return "DRCS Mode";
			case 25: return "Reserved";
			case 26: return "Reserved";
			case 27: return "Reserved";
			case 28: return "Reserved";
			case 29: return "Reserved";
			case 30: return "Reserved";
			case 31: return "Termination Marker";
			default:
				return "Illegal value";
			}

		}
	}

	/**
	 * @return the val
	 */
	@Override
	public int getVal() {
		return val;
	}

	/**
	 * @return
	 */
	public boolean isTerminationMarker() {
		return (getMode()==31)&&(getAddress()>=40);
	}

	/**
	 * @return
	 */
	public boolean isObjectDefinition() {
		return (getMode()>=0x15)&&(getMode()<=0x17)&&(getAddress()>=40);
	}

	@Override
	public String toString(){
		return "TxtTriplet "+getModeString(getMode(), getAddress());

	}
	
	/**
	 * Get a national character for the character {@code ch}, according to the
	 * national option selection bits sent in {@code nocs}.
	 * @param ch The character to be converted to a national character, as it
	 * appeared in the original teletext data.
	 * @param nocs "Character Set Designation and National Option Selection",
	 * coming from the page header or one of the page enhancement packets. Only
	 * the last 7 bits are considered.
	 * @return The national character, as mapped by {@code ch} and {@code nocs}.
	 * If no mapping could be found, {@code ch} is returned.
	 */
	public static char getNationalOptionChar(final byte ch, final int nocs) {
		char targetChar1;
		int subset_idx = national_subset_mapping[(nocs & 0x78) >>> 3][nocs & 0x7];
		switch (ch) // special national characters
		{
		case 0x23:
			targetChar1 = (char) national_subsets[subset_idx][0];
			break;
		case 0x24:
			targetChar1 = (char) national_subsets[subset_idx][1];
			break;
		case 0x40:
			targetChar1 = (char) national_subsets[subset_idx][2];
			break;
		case 0x5b:
			targetChar1 = (char) national_subsets[subset_idx][3];
			break;
		case 0x5c:
			targetChar1 = (char) national_subsets[subset_idx][4];
			break;
		case 0x5d:
			targetChar1 = (char) national_subsets[subset_idx][5];
			break;
		case 0x5e:
			targetChar1 = (char) national_subsets[subset_idx][6];
			break;
		case 0x5f:
			targetChar1 = (char) national_subsets[subset_idx][7];
			break;
		case 0x60:
			targetChar1 = (char) national_subsets[subset_idx][8];
			break;
		case 0x7b:
			targetChar1 = (char) national_subsets[subset_idx][9];
			break;
		case 0x7c:
			targetChar1 = (char) national_subsets[subset_idx][10];
			break;
		case 0x7d:
			targetChar1 = (char) national_subsets[subset_idx][11];
			break;
		case 0x7e:
			targetChar1 = (char) national_subsets[subset_idx][12];
			break;
		case 0x7f:
			targetChar1 = (char) 0x25A0;
			break;
		default:
			targetChar1 = (char) ch;
		}
		return targetChar1;
	}

}
