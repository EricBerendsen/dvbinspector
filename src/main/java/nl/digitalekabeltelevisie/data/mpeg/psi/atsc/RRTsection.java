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

package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.AtscMultipleString;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class RRTsection extends TableSectionExtendedSyntax {

	private final int protocolVersion;
	private final int ratingRegionNameLength;
	private final AtscMultipleString ratingRegionNameText;
	private final int dimensionsDefined;
	private final List<Dimension> dimensions = new ArrayList<>();
	private final int descriptorsLength;
	private final List<Descriptor> descriptorList;

	public RRTsection(final PsiSectionData rawData, final PID parent) {
		super(rawData, parent);

		byte[] data = rawData.getData();
		protocolVersion = getInt(data, 8, 1, MASK_8BITS);
		ratingRegionNameLength = getInt(data, 9, 1, MASK_8BITS);
		ratingRegionNameText = new AtscMultipleString(data, 10, ratingRegionNameLength);
		dimensionsDefined = getInt(data, 10 + ratingRegionNameLength, 1, MASK_8BITS);

		int offset = 11 + ratingRegionNameLength;
		for (int i = 0; i < dimensionsDefined; i++) {
			Dimension dimension = new Dimension(data, offset, this, i);
			dimensions.add(dimension);
			offset += dimension.getLength();
		}

		descriptorsLength = getInt(data, offset, 2, MASK_10BITS);
		descriptorList = DescriptorFactory.buildDescriptorList(data, offset + 2, descriptorsLength, this);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.addTableSource(this::getTableModel, "Rating Values");
		t.add(new KVP("protocol_version", protocolVersion));
		t.add(new KVP("rating_region", getRatingRegion()));
		t.add(new KVP("rating_region_name_length", ratingRegionNameLength));
		t.add(new KVP("rating_region_name", getRatingRegionName()));
		t.add(ratingRegionNameText.getJTreeNode(modus));
		t.add(new KVP("dimensions_defined", dimensionsDefined));
		addListJTree(t, dimensions, modus, "dimensions");
		t.add(new KVP("descriptors_length", descriptorsLength));
		addListJTree(t, descriptorList, modus, "descriptors");
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "rating_region";
	}

	@Override
	protected String getTableIdExtensionDescription(final int tableIdExtension) {
		return Integer.toString(tableIdExtension & MASK_8BITS);
	}

	public int getRatingRegion() {
		return getTableIdExtension() & MASK_8BITS;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public int getRatingRegionNameLength() {
		return ratingRegionNameLength;
	}

	public AtscMultipleString getRatingRegionNameText() {
		return ratingRegionNameText;
	}

	public String getRatingRegionName() {
		return ratingRegionNameText.getText();
	}

	public int getDimensionsDefined() {
		return dimensionsDefined;
	}

	public List<Dimension> getDimensions() {
		return dimensions;
	}

	public Dimension getDimension(final int dimensionIndex) {
		for (Dimension dimension : dimensions) {
			if (dimension.getDimensionIndex() == dimensionIndex) {
				return dimension;
			}
		}
		return null;
	}

	public int getDescriptorsLength() {
		return descriptorsLength;
	}

	public List<Descriptor> getDescriptorList() {
		return descriptorList;
	}

	public TableModel getTableModel() {
		FlexTableModel<Dimension, RatingValue> tableModel = new FlexTableModel<>(buildRrtTableHeader());
		for (Dimension dimension : dimensions) {
			tableModel.addData(dimension, dimension.getRatingValues());
		}
		tableModel.process();
		return tableModel;
	}

	static TableHeader<Dimension, RatingValue> buildRrtTableHeader() {
		return new TableHeaderBuilder<Dimension, RatingValue>()
				.addRequiredBaseColumn("rating_region", Dimension::getRatingRegion, Integer.class)
				.addRequiredBaseColumn("rating_region_name", Dimension::getRatingRegionName, String.class)
				.addRequiredBaseColumn("dimension", Dimension::getDimensionIndex, Integer.class)
				.addRequiredBaseColumn("dimension_name", Dimension::getDimensionName, String.class)
				.addRequiredBaseColumn("graduated_scale", Dimension::getGraduatedScale, Integer.class)
				.addRequiredRowColumn("rating_value", RatingValue::getRatingValue, Integer.class)
				.addRequiredRowColumn("abbrev_rating_value", RatingValue::getAbbrevRatingValue, String.class)
				.addRequiredRowColumn("rating_value_text", RatingValue::getRatingValueTextString, String.class)
				.build();
	}

	public static class Dimension implements TreeNode {

		private final RRTsection parent;
		private final int dimensionIndex;
		private final int dimensionNameLength;
		private final AtscMultipleString dimensionNameText;
		private final int graduatedScale;
		private final int valuesDefined;
		private final List<RatingValue> ratingValues = new ArrayList<>();
		private final int length;

		Dimension(final byte[] data, final int offset, final RRTsection parent, final int dimensionIndex) {
			this.parent = parent;
			this.dimensionIndex = dimensionIndex;
			dimensionNameLength = getInt(data, offset, 1, MASK_8BITS);
			dimensionNameText = new AtscMultipleString(data, offset + 1, dimensionNameLength);
			int localOffset = offset + 1 + dimensionNameLength;
			int flags = getInt(data, localOffset, 1, MASK_8BITS);
			graduatedScale = (flags >> 4) & 0x01;
			valuesDefined = flags & 0x0F;
			localOffset++;
			for (int i = 0; i < valuesDefined; i++) {
				RatingValue ratingValue = new RatingValue(data, localOffset, i);
				ratingValues.add(ratingValue);
				localOffset += ratingValue.getLength();
			}
			length = localOffset - offset;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("dimension", dimensionIndex, getDimensionName());
			t.add(new KVP("dimension_name_length", dimensionNameLength));
			t.add(new KVP("dimension_name", getDimensionName()));
			t.add(dimensionNameText.getJTreeNode(modus));
			t.add(new KVP("graduated_scale", graduatedScale));
			t.add(new KVP("values_defined", valuesDefined));
			addListJTree(t, ratingValues, modus, "rating_values");
			return t;
		}

		public int getRatingRegion() {
			return parent.getRatingRegion();
		}

		public String getRatingRegionName() {
			return parent.getRatingRegionName();
		}

		public int getDimensionIndex() {
			return dimensionIndex;
		}

		public int getDimensionNameLength() {
			return dimensionNameLength;
		}

		public AtscMultipleString getDimensionNameText() {
			return dimensionNameText;
		}

		public String getDimensionName() {
			return dimensionNameText.getText();
		}

		public int getGraduatedScale() {
			return graduatedScale;
		}

		public int getValuesDefined() {
			return valuesDefined;
		}

		public List<RatingValue> getRatingValues() {
			return ratingValues;
		}

		public RatingValue getRatingValue(final int ratingValue) {
			for (RatingValue value : ratingValues) {
				if (value.getRatingValue() == ratingValue) {
					return value;
				}
			}
			return null;
		}

		int getLength() {
			return length;
		}
	}

	public static class RatingValue implements TreeNode {

		private final int ratingValue;
		private final int abbrevRatingValueLength;
		private final AtscMultipleString abbrevRatingValueText;
		private final int ratingValueLength;
		private final AtscMultipleString ratingValueText;
		private final int length;

		RatingValue(final byte[] data, final int offset, final int ratingValue) {
			this.ratingValue = ratingValue;
			abbrevRatingValueLength = getInt(data, offset, 1, MASK_8BITS);
			abbrevRatingValueText = new AtscMultipleString(data, offset + 1, abbrevRatingValueLength);
			int localOffset = offset + 1 + abbrevRatingValueLength;
			ratingValueLength = getInt(data, localOffset, 1, MASK_8BITS);
			ratingValueText = new AtscMultipleString(data, localOffset + 1, ratingValueLength);
			length = 2 + abbrevRatingValueLength + ratingValueLength;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("rating_value", ratingValue, getRatingValueTextString());
			t.add(new KVP("abbrev_rating_value_length", abbrevRatingValueLength));
			t.add(new KVP("abbrev_rating_value", getAbbrevRatingValue()));
			t.add(abbrevRatingValueText.getJTreeNode(modus));
			t.add(new KVP("rating_value_length", ratingValueLength));
			t.add(new KVP("rating_value_text", getRatingValueTextString()));
			t.add(ratingValueText.getJTreeNode(modus));
			return t;
		}

		public int getRatingValue() {
			return ratingValue;
		}

		public int getAbbrevRatingValueLength() {
			return abbrevRatingValueLength;
		}

		public AtscMultipleString getAbbrevRatingValueText() {
			return abbrevRatingValueText;
		}

		public String getAbbrevRatingValue() {
			return abbrevRatingValueText.getText();
		}

		public int getRatingValueLength() {
			return ratingValueLength;
		}

		public AtscMultipleString getRatingValueText() {
			return ratingValueText;
		}

		public String getRatingValueTextString() {
			return ratingValueText.getText();
		}

		int getLength() {
			return length;
		}
	}
}
