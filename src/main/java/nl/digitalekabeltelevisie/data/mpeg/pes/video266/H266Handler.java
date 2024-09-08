/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video266;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.general.DefaultKeyedValues2DDataset;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.H26xHandler;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 *
 */
public class H266Handler extends H26xHandler<Video266PESDataField, H266NALUnit>  implements ImageSource{

	private static final int MAX_NAL_TYPES = 32; // incl potential extensions

	
	// 7.4.2.4.3 Order of PUs and their association to AUs
	
	private static final EnumSet<H266NALUnitType> startElementsOfAU = EnumSet.of(H266NALUnitType.AUD_NUT,
			H266NALUnitType.OPI_NUT, H266NALUnitType.DCI_NUT, H266NALUnitType.VPS_NUT, H266NALUnitType.SPS_NUT,
			H266NALUnitType.PPS_NUT, H266NALUnitType.PREFIX_APS_NUT, H266NALUnitType.PH_NUT,
			H266NALUnitType.PREFIX_SEI_NUT, H266NALUnitType.RSV_NVCL_26, H266NALUnitType.UNSPEC_28,
			H266NALUnitType.UNSPEC_29);
	
	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	protected void processPesDataBytes(final PesPacketData pesData){
		pesPackets.add(new Video266PESDataField(pesData));

	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("H.266 PES Data").addImageSource(this, "Frames"));
		addListJTree(s, pesPackets, modus, "PES Packets");
		addCCDataToTree(modus, s);

		return s;
	}

	@Override
	protected Video266PESDataField createH26xPESDataField(PesPacketData pesData) {
		return new Video266PESDataField(pesData);
	}
	
	@Override
	public BufferedImage getImage() {

		final List<int[]> frameSize  = new ArrayList<>();
		final List<ChartLabel> labels = new ArrayList<>();

		int[] accessUnitData = new int[MAX_NAL_TYPES]; 
		boolean [] typeUsed  = new boolean[MAX_NAL_TYPES]; 
		
		int count = 0;

		final NALUnitIterator nalIter = new NALUnitIterator();
		H266NALUnit unit = nalIter.next();
		while (unit != null) {

			// 7.4.2.4.3 Order of PUs and their association to AUs
			if (startElementsOfAU.contains(unit.getNal_unit_type())) {

				count = drawBarAccessUnit(frameSize, labels, accessUnitData, count);
				accessUnitData = new int[MAX_NAL_TYPES];
				while (unit != null && startElementsOfAU.contains(unit.getNal_unit_type())) {
					accessUnitData[unit.getNal_unit_type().getType()] += unit.getNumBytesInRBSP();
					typeUsed[unit.getNal_unit_type().getType()] = true;
					unit = nalIter.next();
				}
			}
			accessUnitData[unit.getNal_unit_type().getType()] += unit.getNumBytesInRBSP();
			typeUsed[unit.getNal_unit_type().getType()] = true;

			unit = nalIter.next();
		}
		// last unit is not followed by delimiter
		drawBarAccessUnit(frameSize, labels, accessUnitData, count);

		final DefaultKeyedValues2DDataset dataset = new DefaultKeyedValues2DDataset();


		for (int i = 0; i < MAX_NAL_TYPES; i++) {
			H266NALUnitType nalType = H266NALUnitType.getByType(i);
			if (nalType != null) {
				if(typeUsed[i]) {
					final String typeName = nalType.name();
					final Iterator<int[]> frameSizeIter = frameSize.iterator();
					for (final ChartLabel l : labels) {
						if (frameSizeIter.hasNext()) {
							final int[] v = frameSizeIter.next();
							dataset.setValue(v[i], typeName, l);
						}
					}
				}
			}
		}

		final StackedBarRenderer renderer = new StackedBarRenderer();
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(false);
		renderer.setItemMargin(0.0);

		final CategoryAxis categoryAxis = new CategoryAxis("time");
		categoryAxis.setCategoryMargin(0.0);
		categoryAxis.setUpperMargin(0.0);
		categoryAxis.setLowerMargin(0.0);
		categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		final ValueAxis valueAxis = new NumberAxis("frame size (bytes)");

		final CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
		final String title = getPID().getLabelMaker()+" (Access Units, Transmission Order)";
		final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT,plot, true	);

		return chart.createBufferedImage(( frameSize.size()*18)+100, 640);
	}


}
