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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

/**
 * @author Eric Berendsen
 *
 */
public class RegionCompositionSegment extends Segment implements TreeNode {

	/**
	 * @author Eric Berendsen
	 *
	 */
	public static class RegionObject implements TreeNode {
		/**
		 *
		 */
		private int object_id;

		/**
		 *
		 */
		private int object_type;

		/**
		 *
		 */
		private int object_provider_flag;

		/**
		 *
		 */
		private int object_horizontal_position;

		/**
		 *
		 */
		private int object_vertical_position;

		/**
		 *
		 */
		private int foreground_pixel_code = 0;

		/**
		 *
		 */
		private int background_pixel_code = 0;

		/**
		 * @param object_id
		 * @param object_type
		 * @param object_provider_flag
		 * @param object_horizontal_position
		 * @param object_vertical_position
		 * @param foreground_pixel_code
		 * @param background_pixel_code
		 */
		public RegionObject(final int object_id, final int object_type,
				final int object_provider_flag, final int object_horizontal_position,
				final int object_vertical_position, final int foreground_pixel_code,
				final int background_pixel_code) {
			super();
			this.object_id = object_id;
			this.object_type = object_type;
			this.object_provider_flag = object_provider_flag;
			this.object_horizontal_position = object_horizontal_position;
			this.object_vertical_position = object_vertical_position;
			this.foreground_pixel_code = foreground_pixel_code;
			this.background_pixel_code = background_pixel_code;
		}

		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("Object"));
			s.add(new DefaultMutableTreeNode(new KVP("object_id", object_id,
					null)));
			s.add(new DefaultMutableTreeNode(new KVP("object_type",
					object_type, getObjectTypeString(object_type))));
			s.add(new DefaultMutableTreeNode(new KVP("object_provider_flag",
					object_provider_flag,
					getObjectProviderString(object_provider_flag))));
			s.add(new DefaultMutableTreeNode(new KVP(
					"object_horizontal_position", object_horizontal_position,
					null)));
			s
			.add(new DefaultMutableTreeNode(new KVP(
					"object_vertical_position",
					object_vertical_position, null)));
			if ((object_type == 0x01) || (object_type == 0x02)) {
				s.add(new DefaultMutableTreeNode(new KVP(
						"foreground_pixel_code", foreground_pixel_code, null)));
				s.add(new DefaultMutableTreeNode(new KVP(
						"background_pixel_code", background_pixel_code, null)));
			}
			return s;
		}


		/**
		 * @return the background_pixel_code
		 */
		public int getBackground_pixel_code() {
			return background_pixel_code;
		}


		/**
		 * @return the foreground_pixel_code
		 */
		public int getForeground_pixel_code() {
			return foreground_pixel_code;
		}


		/**
		 * @return the object_horizontal_position
		 */
		public int getObject_horizontal_position() {
			return object_horizontal_position;
		}


		/**
		 * @return the object_id
		 */
		public int getObject_id() {
			return object_id;
		}


		/**
		 * @return the object_provider_flag
		 */
		public int getObject_provider_flag() {
			return object_provider_flag;
		}


		/**
		 * @return the object_type
		 */
		public int getObject_type() {
			return object_type;
		}


		/**
		 * @return the object_vertical_position
		 */
		public int getObject_vertical_position() {
			return object_vertical_position;
		}

	}

	/**
	 * @param data
	 * @param offset
	 */
	public RegionCompositionSegment(final byte[] data, final int offset) {
		super(data, offset);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.Segment#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus);
		s.add(new DefaultMutableTreeNode(new KVP("region_id", getRegionId(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_version_number", getRegionVersionNumber(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_fill_flag", getRegionFillFlag(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_width", getRegionWidth(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_height", getRegionHeight(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_level_of_compatibility", getRegionLevelOfCompatibility(),
				getRegionLevelOfCompatibilityString(getRegionLevelOfCompatibility()))));
		s.add(new DefaultMutableTreeNode(new KVP("region_depth", getRegionDepth(),
				getRegionDepthString(getRegionDepth()))));
		s.add(new DefaultMutableTreeNode(new KVP("CLUT_id", getCLUTId(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_8-bit_pixel_code", getRegion8BitPixelCode(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_4-bit_pixel-code", getRegion4BitPixelCode(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("region_2-bit_pixel-code", getRegion2BitPixelCode(), null)));

		addListJTree(s, getRegionObjects(), modus, "regions");

		return s;
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegion2BitPixelCode() {
		return getInt(data_block, offset + 15, 1, 0x0C) >> 2;
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegion4BitPixelCode() {
		return getInt(data_block, offset + 15, 1, 0xF0) >> 4;
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegion8BitPixelCode() {
		return getInt(data_block, offset + 14, 1, MASK_8BITS);
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getCLUTId() {
		return getInt(data_block, offset + 13, 1, MASK_8BITS);
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegionHeight() {
		return getInt(data_block, offset + 10, 2, MASK_16BITS);
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegionWidth() {
		return getInt(data_block, offset + 8, 2, MASK_16BITS);
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegionFillFlag() {
		return getInt(data_block, offset + 7, 1, 0x08) >> 3;
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegionVersionNumber() {
		return getInt(data_block, offset + 7, 1, 0xF0) >> 4;
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegionId() {
		return getInt(data_block, offset + 6, 1, MASK_8BITS);
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public List<RegionObject> getRegionObjects() {
		final ArrayList<RegionObject> regions = new ArrayList<RegionObject>();
		int t = 0;
		while ((t + 10) < getSegmentLength()) {
			final int object_id = getInt(data_block, offset + 16 + t, 2, MASK_16BITS);
			final int object_type = getInt(data_block, offset + 18 + t, 1, 0xC0) >> 6;
		final int object_provider_flag = getInt(data_block, offset + 18 + t, 1,
				0x30) >> 4;
				final int object_horizontal_position = getInt(data_block,
						offset + 18 + t, 2, MASK_12BITS);
				final int object_vertical_position = getInt(data_block, offset + 20 + t,
						2, MASK_12BITS);
				int foreground_pixel_code = 0;
				int background_pixel_code = 0;
				if ((object_type == 0x01) || (object_type == 0x02)) {
					foreground_pixel_code = getInt(data_block, offset + 22 + t, 1,
							MASK_8BITS);
					background_pixel_code = getInt(data_block, offset + 23 + t, 1,
							MASK_8BITS);
					t += 2;
				}
				regions.add(new RegionObject(object_id, object_type,
						object_provider_flag, object_horizontal_position,
						object_vertical_position, foreground_pixel_code,
						background_pixel_code));
				t += 6;
		}
		return regions;
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegionDepth() {
		return getInt(data_block, offset + 12, 1, 0x1C) >> 2;
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	public int getRegionLevelOfCompatibility() {
		return getInt(data_block, offset + 12, 1, 0xE0) >> 5;
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getRegionLevelOfCompatibilityString(final int type) {

		switch (type) {

		case 0x0:
			return "reserved";
		case 0x1:
			return "2-bit/entry CLUT required";
		case 0x2:
			return "4-bit/entry CLUT required";
		case 0x3:
			return "8-bit/entry CLUT required";
		default:
			return "reserved";
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getRegionDepthString(final int type) {

		switch (type) {

		case 0x0:
			return "reserved";
		case 0x1:
			return "2 bit";
		case 0x2:
			return "4 bit";
		case 0x3:
			return "8 bit";
		default:
			return "reserved";
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getObjectTypeString(final int type) {

		switch (type) {

		case 0x0:
			return "basic_object, bitmap";
		case 0x1:
			return "basic_object, character";
		case 0x2:
			return "composite_object, string of characters";
		case 0x3:
			return "reserved";
		default:
			return "Illegal value";
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public static String getObjectProviderString(final int type) {

		switch (type) {

		case 0x0:
			return "provided in the subtitling stream";
		case 0x1:
			return "provided by a ROM in the IRD";
		case 0x2:
			return "Reserved";
		case 0x3:
			return "reserved";
		default:
			return "Illegal value";
		}
	}

}