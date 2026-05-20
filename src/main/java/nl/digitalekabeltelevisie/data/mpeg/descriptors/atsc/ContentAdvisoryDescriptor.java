/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ContentAdvisoryDescriptor extends AtscDescriptor {

	private final int ratingRegionCount;
	private final List<RatingRegion> ratingRegions = new ArrayList<>();

	public ContentAdvisoryDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		ratingRegionCount = getInt(b, PRIVATE_DATA_OFFSET, 1, 0x3F);
		int offset = PRIVATE_DATA_OFFSET + 1;
		for (int i = 0; i < ratingRegionCount; i++) {
			RatingRegion ratingRegion = new RatingRegion(b, offset, this);
			ratingRegions.add(ratingRegion);
			offset += ratingRegion.getLength();
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("rating_region_count", ratingRegionCount));
		addListJTree(t, ratingRegions, modus, "rating_regions");
		return t;
	}

	public int getRatingRegionCount() {
		return ratingRegionCount;
	}

	public List<RatingRegion> getRatingRegions() {
		return ratingRegions;
	}

	public String getRatingSummaryString() {
		List<String> values = new ArrayList<>();
		for (RatingRegion region : ratingRegions) {
			for (RatedDimension dimension : region.getDimensions()) {
				String text = dimension.getRatingValueText();
				if ((text == null) || text.isBlank()) {
					text = "region " + region.getRatingRegion() + " dimension " + dimension.ratingDimension()
							+ " value " + dimension.ratingValue();
				}
				values.add(text);
			}
			String description = region.getRatingDescriptionText().getText();
			if ((description != null) && !description.isBlank()) {
				values.add(description);
			}
		}
		return values.isEmpty() ? null : String.join(", ", values);
	}

	private String getRatingDimensionName(final int ratingRegion, final int ratingDimension) {
		try {
			return getPSI().getAtsc().getRrt().getRatingDimensionName(ratingRegion, ratingDimension);
		} catch (RuntimeException e) {
			return null;
		}
	}

	private String getRatingValueText(final int ratingRegion, final int ratingDimension, final int ratingValue) {
		try {
			return getPSI().getAtsc().getRrt().getRatingValueText(ratingRegion, ratingDimension, ratingValue);
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static class RatingRegion implements TreeNode {

		private final ContentAdvisoryDescriptor parent;
		private final int ratingRegion;
		private final int ratedDimensions;
		private final List<RatedDimension> dimensions = new ArrayList<>();
		private final int ratingDescriptionLength;
		private final AtscMultipleString ratingDescriptionText;
		private final int length;

		RatingRegion(final byte[] data, final int offset, final ContentAdvisoryDescriptor parent) {
			this.parent = parent;
			ratingRegion = getInt(data, offset, 1, MASK_8BITS);
			ratedDimensions = getInt(data, offset + 1, 1, MASK_8BITS);
			int localOffset = offset + 2;
			for (int i = 0; i < ratedDimensions; i++) {
				dimensions.add(new RatedDimension(data, localOffset, this));
				localOffset += 2;
			}
			ratingDescriptionLength = getInt(data, localOffset, 1, MASK_8BITS);
			ratingDescriptionText = new AtscMultipleString(data, localOffset + 1, ratingDescriptionLength);
			length = localOffset + 1 + ratingDescriptionLength - offset;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("rating_region", ratingRegion);
			t.add(new KVP("rated_dimensions", ratedDimensions));
			addListJTree(t, dimensions, modus, "rated_dimensions");
			t.add(new KVP("rating_description_length", ratingDescriptionLength));
			t.add(new KVP("rating_description", ratingDescriptionText.getText()));
			t.add(ratingDescriptionText.getJTreeNode(modus));
			return t;
		}

		public int getRatingRegion() {
			return ratingRegion;
		}

		public int getRatedDimensions() {
			return ratedDimensions;
		}

		public List<RatedDimension> getDimensions() {
			return dimensions;
		}

		public int getRatingDescriptionLength() {
			return ratingDescriptionLength;
		}

		public AtscMultipleString getRatingDescriptionText() {
			return ratingDescriptionText;
		}

		int getLength() {
			return length;
		}

		private String getRatingDimensionName(final int ratingDimension) {
			return parent.getRatingDimensionName(ratingRegion, ratingDimension);
		}

		private String getRatingValueText(final int ratingDimension, final int ratingValue) {
			return parent.getRatingValueText(ratingRegion, ratingDimension, ratingValue);
		}
	}

	public record RatedDimension(int ratingDimension, int ratingValue, RatingRegion parent) implements TreeNode {

		public RatedDimension(final int ratingDimension, final int ratingValue) {
			this(ratingDimension, ratingValue, null);
		}

		RatedDimension(final byte[] data, final int offset, final RatingRegion parent) {
			this(getInt(data, offset, 1, MASK_8BITS), getInt(data, offset + 1, 1, 0x0F), parent);
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("rated_dimension", ratingDimension, getRatingDimensionName());
			t.add(new KVP("rating_value", ratingValue, getRatingValueText()));
			return t;
		}

		public String getRatingDimensionName() {
			return parent == null ? null : parent.getRatingDimensionName(ratingDimension);
		}

		public String getRatingValueText() {
			return parent == null ? null : parent.getRatingValueText(ratingDimension, ratingValue);
		}
	}
}
