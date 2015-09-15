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

import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class CLUTDefinitionSegment extends Segment implements TreeNode {

	private int CLUT_2bit[];
	private int CLUT_4bit[];
	private int CLUT_8bit[];

	private static final int default_CLUT_2bit[] = {
		0, 0xFFFFFFFF, 0xFF000000, 0xFF808080
	};

	private static final int default_CLUT_4bit[] = {
		0, 0xFFFF0000, 0xFF00FF00, 0xFFFFFF00, 0xFF0000FF, 0xFFFF00FF,
		0xFF00FFFF, 0xFFFFFFFF, 0xFF000000, 0xFF800000, 0xFF008000,
		0xFF808000, 0xFF000080, 0xFF800080, 0xFF008080, 0xFF808080
	};

	private static final int	default_CLUT_8bit[]	= { 0x0, 0x40ff0000, 0x4000ff00, 0x40ffff00, 0x400000ff,
		0x40ff00ff, 0x4000ffff, 0x40ffffff, 0x80000000, 0x80550000, 0x80005500, 0x80555500, 0x80000055, 0x80550055,
		0x80005555, 0x80555555, 0xffaa0000, 0xffff0000, 0xffaa5500, 0xffff5500, 0xffaa0055, 0xffff0055, 0xffaa5555,
		0xffff5555, 0x80aa0000, 0x80ff0000, 0x80aa5500, 0x80ff5500, 0x80aa0055, 0x80ff0055, 0x80aa5555, 0x80ff5555,
		0xff00aa00, 0xff55aa00, 0xff00ff00, 0xff55ff00, 0xff00aa55, 0xff55aa55, 0xff00ff55, 0xff55ff55, 0x8000aa00,
		0x8055aa00, 0x8000ff00, 0x8055ff00, 0x8000aa55, 0x8055aa55, 0x8000ff55, 0x8055ff55, 0xffaaaa00, 0xffffaa00,
		0xffaaff00, 0xffffff00, 0xffaaaa55, 0xffffaa55, 0xffaaff55, 0xffffff55, 0x80aaaa00, 0x80ffaa00, 0x80aaff00,
		0x80ffff00, 0x80aaaa55, 0x80ffaa55, 0x80aaff55, 0x80ffff55, 0xff0000aa, 0xff5500aa, 0xff0055aa, 0xff5555aa,
		0xff0000ff, 0xff5500ff, 0xff0055ff, 0xff5555ff, 0x800000aa, 0x805500aa, 0x800055aa, 0x805555aa, 0x800000ff,
		0x805500ff, 0x800055ff, 0x805555ff, 0xffaa00aa, 0xffff00aa, 0xffaa55aa, 0xffff55aa, 0xffaa00ff, 0xffff00ff,
		0xffaa55ff, 0xffff55ff, 0x80aa00aa, 0x80ff00aa, 0x80aa55aa, 0x80ff55aa, 0x80aa00ff, 0x80ff00ff, 0x80aa55ff,
		0x80ff55ff, 0xff00aaaa, 0xff55aaaa, 0xff00ffaa, 0xff55ffaa, 0xff00aaff, 0xff55aaff, 0xff00ffff, 0xff55ffff,
		0x8000aaaa, 0x8055aaaa, 0x8000ffaa, 0x8055ffaa, 0x8000aaff, 0x8055aaff, 0x8000ffff, 0x8055ffff, 0xffaaaaaa,
		0xffffaaaa, 0xffaaffaa, 0xffffffaa, 0xffaaaaff, 0xffffaaff, 0xffaaffff, 0xffffffff, 0x80aaaaaa, 0x80ffaaaa,
		0x80aaffaa, 0x80ffffaa, 0x80aaaaff, 0x80ffaaff, 0x80aaffff, 0x80ffffff, 0xff808080, 0xffaa8080, 0xff80aa80,
		0xffaaaa80, 0xff8080aa, 0xffaa80aa, 0xff80aaaa, 0xffaaaaaa, 0xff000000, 0xff2a0000, 0xff002a00, 0xff2a2a00,
		0xff00002a, 0xff2a002a, 0xff002a2a, 0xff2a2a2a, 0xffd58080, 0xffff8080, 0xffd5aa80, 0xffffaa80, 0xffd580aa,
		0xffff80aa, 0xffd5aaaa, 0xffffaaaa, 0xff550000, 0xff7f0000, 0xff552a00, 0xff7f2a00, 0xff55002a, 0xff7f002a,
		0xff552a2a, 0xff7f2a2a, 0xff80d580, 0xffaad580, 0xff80ff80, 0xffaaff80, 0xff80d5aa, 0xffaad5aa, 0xff80ffaa,
		0xffaaffaa, 0xff005500, 0xff2a5500, 0xff007f00, 0xff2a7f00, 0xff00552a, 0xff2a552a, 0xff007f2a, 0xff2a7f2a,
		0xffd5d580, 0xffffd580, 0xffd5ff80, 0xffffff80, 0xffd5d5aa, 0xffffd5aa, 0xffd5ffaa, 0xffffffaa, 0xff555500,
		0xff7f5500, 0xff557f00, 0xff7f7f00, 0xff55552a, 0xff7f552a, 0xff557f2a, 0xff7f7f2a, 0xff8080d5, 0xffaa80d5,
		0xff80aad5, 0xffaaaad5, 0xff8080ff, 0xffaa80ff, 0xff80aaff, 0xffaaaaff, 0xff000055, 0xff2a0055, 0xff002a55,
		0xff2a2a55, 0xff00007f, 0xff2a007f, 0xff002a7f, 0xff2a2a7f, 0xffd580d5, 0xffff80d5, 0xffd5aad5, 0xffffaad5,
		0xffd580ff, 0xffff80ff, 0xffd5aaff, 0xffffaaff, 0xff550055, 0xff7f0055, 0xff552a55, 0xff7f2a55, 0xff55007f,
		0xff7f007f, 0xff552a7f, 0xff7f2a7f, 0xff80d5d5, 0xffaad5d5, 0xff80ffd5, 0xffaaffd5, 0xff80d5ff, 0xffaad5ff,
		0xff80ffff, 0xffaaffff, 0xff005555, 0xff2a5555, 0xff007f55, 0xff2a7f55, 0xff00557f, 0xff2a557f, 0xff007f7f,
		0xff2a7f7f, 0xffd5d5d5, 0xffffd5d5, 0xffd5ffd5, 0xffffffd5, 0xffd5d5ff, 0xffffd5ff, 0xffd5ffff, 0xffffffff,
		0xff555555, 0xff7f5555, 0xff557f55, 0xff7f7f55, 0xff55557f, 0xff7f557f, 0xff557f7f, 0xff7f7f7f

	};


	public static class CLUTEntry implements TreeNode {
		private final int CLUT_entry_id;

		private final int CLUT_flag_2_bit_entry;

		private final int CLUT_flag_4_bit_entry;

		private final int CLUT_flag_8_bit_entry;

		private final int full_range_flag;

		private final int y_value;

		private final int cr_value;

		private final int cb_value;

		private final int t_value;

		public CLUTEntry(final int clut_entry_id, final int clut_flag_2_bit_entry,
				final int clut_flag_4_bit_entry, final int clut_flag_8_bit_entry,
				final int full_range_flag, final int y_value, final int cr_value, final int cb_value,
				final int t_value) {
			super();
			CLUT_entry_id = clut_entry_id;
			CLUT_flag_2_bit_entry = clut_flag_2_bit_entry;
			CLUT_flag_4_bit_entry = clut_flag_4_bit_entry;
			CLUT_flag_8_bit_entry = clut_flag_8_bit_entry;
			this.full_range_flag = full_range_flag;
			this.y_value = y_value;
			this.cr_value = cr_value;
			this.cb_value = cb_value;
			this.t_value = t_value;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			int r, g, b;
			float y, cr, cb;
			if (full_range_flag == 1) {
				y = y_value;
				cb = cb_value;
				cr = cr_value;
			} else {
				y = (y_value * 255) / 63;
				cb = (cb_value * 255) / 15;
				cr = (cr_value * 255) / 15;
			}


			// based on Project X, DVBSubpicture

			r = (int)(y +(1.402f * (cr-128)));
			g = (int)(y -(0.34414 * (cb-128)) -(0.71414 * (cr-128)));
			b = (int)(y +(1.722 * (cb-128)));
			r = boundRange(r);
			g = boundRange(g);
			b = boundRange(b);

			if(y==0){
				r=0;
				g=0;
				b=0;
			}

			// alternative, see http://www.intersil.com/data/an/an9717.pdf, R'G'B' Generation

			//			r = (int) ((y - 16) * 1.164 + 1.596 * (cr - 128));
			//			g = (int) ((y - 16) * 1.164 - (0.813 * (cr - 128)) - 0.391 * (cb - 128));
			//			b = (int) ((y - 16) * 1.164 + (2.018 * (cb - 128)));

			final String bgColor = "#" + Utils.toHexStringUnformatted(r, 2)
					+ Utils.toHexStringUnformatted(g, 2)
					+ Utils.toHexStringUnformatted(b, 2);
			final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("CLUT_entry <code><span style=\"background-color: "+ bgColor
					+ "; color: white;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></code>",
					"CLUT_entry id "+ CLUT_entry_id));  // plain text alternative
			s.add(new DefaultMutableTreeNode(new KVP("CLUT_entry_id",
					CLUT_entry_id, null)));
			s.add(new DefaultMutableTreeNode(new KVP("2-bit/entry_CLUT_flag",
					CLUT_flag_2_bit_entry, null)));
			s.add(new DefaultMutableTreeNode(new KVP("4-bit/entry_CLUT_flag",
					CLUT_flag_4_bit_entry, null)));
			s.add(new DefaultMutableTreeNode(new KVP("8-bit/entry_CLUT_flag",
					CLUT_flag_8_bit_entry, null)));
			s.add(new DefaultMutableTreeNode(new KVP("full_range_flag",
					full_range_flag, null)));
			s
			.add(new DefaultMutableTreeNode(new KVP("Y-value", y_value,
					null)));
			s.add(new DefaultMutableTreeNode(
					new KVP("Cr-value", cr_value, null)));
			s.add(new DefaultMutableTreeNode(
					new KVP("Cb-value", cb_value, null)));
			s
			.add(new DefaultMutableTreeNode(new KVP("T-value", t_value,
					null)));
			return s;
		}

		/**
		 * @param r
		 * @return
		 */
		private static int boundRange(final int r) {
			return r < 0 ? 0 : (r > 0xFF ? 0xFF : r);
		}


		/**
		 * @return the cb_value
		 */
		public int getCb_value() {
			return cb_value;
		}


		/**
		 * @return the cLUT_entry_id
		 */
		public int getCLUT_entry_id() {
			return CLUT_entry_id;
		}


		/**
		 * @return the cLUT_flag_2_bit_entry
		 */
		public int getCLUT_flag_2_bit_entry() {
			return CLUT_flag_2_bit_entry;
		}


		/**
		 * @return the cLUT_flag_4_bit_entry
		 */
		public int getCLUT_flag_4_bit_entry() {
			return CLUT_flag_4_bit_entry;
		}


		/**
		 * @return the cLUT_flag_8_bit_entry
		 */
		public int getCLUT_flag_8_bit_entry() {
			return CLUT_flag_8_bit_entry;
		}


		/**
		 * @return the cr_value
		 */
		public int getCr_value() {
			return cr_value;
		}


		/**
		 * @return the full_range_flag
		 */
		public int getFull_range_flag() {
			return full_range_flag;
		}


		/**
		 * @return the t_value
		 */
		public int getT_value() {
			return t_value;
		}


		/**
		 * @return the y_value
		 */
		public int getY_value() {
			return y_value;
		}

		public int getARGB(){
			int r, g, b,a;
			float y, cr, cb;
			if (full_range_flag == 1) {
				y = y_value;
				cb = cb_value;
				cr = cr_value;
				a = 255- t_value; // A value of zero identifies no transparency in DVB, in java other way around!
			} else {
				y = (y_value * 255) / 63;
				cb = (cb_value * 255) / 15;
				cr = (cr_value * 255) / 15;
				a = 255-((t_value * 256) / 4); // A value of zero identifies no transparency
			}


			// based on Project X, DVBSubpicture

			r = (int)(y +(1.402f * (cr-128)));
			g = (int)(y -(0.34414 * (cb-128)) -(0.71414 * (cr-128)));
			b = (int)(y +(1.722 * (cb-128)));
			r = boundRange(r);
			g = boundRange(g);
			b = boundRange(b);

			if(y==0){
				r=0;
				g=0;
				b=0;
				a=0; // full transparant
			}
			return ((a<<24) | (r<<16) | (g<<8) | b);

		}
	}

	public CLUTDefinitionSegment(final byte[] data, final int offset) {
		super(data, offset);
		for(final CLUTEntry clutEntry : getCLUTEntries()){
			if(clutEntry.getCLUT_flag_2_bit_entry()==1){
				if(CLUT_2bit==null){
					CLUT_2bit=Arrays.copyOf(default_CLUT_2bit, default_CLUT_2bit.length);
				}
				if(clutEntry.getCLUT_entry_id()<CLUT_2bit.length){ // blame lovejoy
					CLUT_2bit[clutEntry.getCLUT_entry_id()]=clutEntry.getARGB();
				}
			}
			if(clutEntry.getCLUT_flag_4_bit_entry()==1){
				if(CLUT_4bit==null){
					CLUT_4bit=Arrays.copyOf(default_CLUT_4bit, default_CLUT_4bit.length);
				}
				if(clutEntry.getCLUT_entry_id()<CLUT_4bit.length){
					CLUT_4bit[clutEntry.getCLUT_entry_id()]=clutEntry.getARGB();
				}
			}
			if(clutEntry.getCLUT_flag_8_bit_entry()==1){
				if(CLUT_8bit==null){
					CLUT_8bit=Arrays.copyOf(default_CLUT_8bit, default_CLUT_8bit.length);
				}
				if(clutEntry.getCLUT_entry_id()<CLUT_8bit.length){
					CLUT_8bit[clutEntry.getCLUT_entry_id()]=clutEntry.getARGB();
				}
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus);
		s.add(new DefaultMutableTreeNode(new KVP("CLUT-id", getCLUTId(),null)));
		s.add(new DefaultMutableTreeNode(new KVP("CLUT_version_number",	getCLUTVersionNumber(), null)));
		addListJTree(s, getCLUTEntries(), modus, "CLUTEntries");

		return s;
	}

	/**
	 * @return
	 */
	public int getCLUTVersionNumber() {
		return getInt(data_block, offset + 7, 1, 0xF0) >> 4;
	}

	/**
	 * @return
	 */
	public int getCLUTId() {
		return getInt(data_block, offset + 6, 1, MASK_8BITS);
	}

	/**
	 * @return
	 */
	public final List<CLUTEntry> getCLUTEntries() {
		final ArrayList<CLUTEntry> clutEntries = new ArrayList<CLUTEntry>();
		int t = 0;
		while ((t + 2) < getSegmentLength()) {
			final int CLUT_entry_id = getInt(data_block, offset + 8 + t, 1,
					MASK_8BITS);
			final int flag_2bit = getInt(data_block, offset + 9 + t, 1, 0x80) >> 7;
			final int flag_4bit = getInt(data_block, offset + 9 + t, 1, 0x40) >> 6;
			final int flag_8bit = getInt(data_block, offset + 9 + t, 1, 0x20) >> 5;
			final int full_range_flag = getInt(data_block, offset + 9 + t, 1, 0x01);
			int y_value, cr_value, cb_value, t_value;
			if (full_range_flag == 1) {
				y_value = getInt(data_block, offset + 10 + t, 1, MASK_8BITS);
				cr_value = getInt(data_block, offset + 11 + t, 1, MASK_8BITS);
				cb_value = getInt(data_block, offset + 12 + t, 1, MASK_8BITS);
				t_value = getInt(data_block, offset + 13 + t, 1, MASK_8BITS);
				t += 6;
			} else {
				y_value = getInt(data_block, offset + 10 + t, 1, 0xFC) >> 2;
				cr_value = getInt(data_block, offset + 10 + t, 2, 0x03C) >> 6;
				cb_value = getInt(data_block, offset + 11 + t, 1, 0x3C) >> 2;
				t_value = getInt(data_block, offset + 11 + t, 1, MASK_2BITS);
				t += 4;
			}

			clutEntries.add(new CLUTEntry(CLUT_entry_id, flag_2bit, flag_4bit,
					flag_8bit, full_range_flag, y_value, cr_value, cb_value,
					t_value));
		}
		return clutEntries;
	}

	public static IndexColorModel getDefault_CLUT_2bitColorModel(){
		return getIndexColorModel(2,4,default_CLUT_2bit, 0,true,0,DataBuffer.TYPE_BYTE);
	}

	public static IndexColorModel getDefault_CLUT_4bitColorModel(){
		return getIndexColorModel(4,16,default_CLUT_4bit, 0,true,0,DataBuffer.TYPE_BYTE);
	}

	public static IndexColorModel getDefault_CLUT_8bitColorModel(){
		return getIndexColorModel(8,256,default_CLUT_8bit, 0,true,0,DataBuffer.TYPE_BYTE);
	}

	public static IndexColorModel getDefaultColorModel(final int regionDepth){

		switch (regionDepth) {
		case 1: // 2 bit
			return getDefault_CLUT_2bitColorModel();
		case 2: // 4 bit
			return getDefault_CLUT_4bitColorModel();
		case 3:
			return getDefault_CLUT_8bitColorModel();
		default:
			return null;
		}
	}


	public IndexColorModel getColorModel(final int regionDepth){

		switch (regionDepth) {
		case 1: // 2 bit
			if(CLUT_2bit==null){
				return getDefault_CLUT_2bitColorModel();
			}else{
				return getIndexColorModel(2,4,CLUT_2bit, 0,true,-1,DataBuffer.TYPE_BYTE);
			}
		case 2: // 4 bit
			if(CLUT_4bit==null){
				return getDefault_CLUT_4bitColorModel();
			}else{
				return getIndexColorModel(4,16,CLUT_4bit,0,true,-1,DataBuffer.TYPE_BYTE);
			}
		case 3:
			if(CLUT_8bit==null){
				return getDefault_CLUT_8bitColorModel();
			}else{
				return getIndexColorModel(8,256,CLUT_8bit, 0,true,-1,DataBuffer.TYPE_BYTE);
			}
		default:
			return null;
		}
	}



	/**
	 * Fix for incorrect constructor IndexColorModel(int bits, int size, byte[] cmap, int start, boolean hasalpha, int trans)
	 * because it ignores hasalpha. Quick fix, convert to 4 byte[] .
	 * int start, boolean hasalpha, int trans, int transferType are ignored.
	 *
	 * @param bits
	 * @param size
	 * @param cmap
	 * @param start
	 * @param hasalpha
	 * @param trans
	 * @param transferType
	 * @return
	 */
	private static IndexColorModel getIndexColorModel(final int bits, final int size, final int cmap[], final int start, final boolean hasalpha, final int trans, final int transferType) {
		final byte[] r =new byte[cmap.length];
		final byte[] g =new byte[cmap.length];
		final byte[] b =new byte[cmap.length];
		final byte[] a =new byte[cmap.length];

		for (int i = 0; i < cmap.length; i++) {
			r[i] = Utils.getInt2UnsignedByte((cmap[i]& 0xFF0000)>>16);
			g[i] = Utils.getInt2UnsignedByte((cmap[i]& 0x00FF00)>>8);
			b[i] = Utils.getInt2UnsignedByte((cmap[i]& 0x0000FF));
			a[i] = Utils.getInt2UnsignedByte((cmap[i]& 0xFF000000)>>>24);
		}
		return new IndexColorModel(bits,size,r,g,b,a);
	}

}