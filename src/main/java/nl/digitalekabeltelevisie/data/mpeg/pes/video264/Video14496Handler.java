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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;



import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.Filler_data_rbsp;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.H26xHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.Sei_rbsp;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 *
 */
public class Video14496Handler extends H26xHandler<Video14496PESDataField, NALUnit> implements ImageSource{



	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
	 */
	@Override
	public BufferedImage getImage() {

		final List<int[]> frameSize  = new ArrayList<>();
		final List<ChartLabel> labels = new ArrayList<>();

		int[] accessUnitData = new int[6]; // 0= P, 1 = B, 2 = I, 3 = SP, 4 = SI (all mod 5), 5 = Filler data
		int count = 0;

		final NALUnitIterator nalIter = new NALUnitIterator();
		NALUnit unit = nalIter.next();
		while(unit!=null){
			final RBSP rbsp = unit.getRbsp();
			if(rbsp!=null){
				
				// Rec. ITU-T H.264 (06/2019)
				// 7.4.1.2.3 Order of NAL units and coded pictures and association to access units
				// TODO add NAL units with nal_unit_type in the range of 14 to 18, inclusive
				// TODO only slice NALs and Filler are included in Graph, ignore rest?
				if(( rbsp instanceof Access_unit_delimiter_rbsp) ||
						( rbsp instanceof Seq_parameter_set_rbsp) ||
						( rbsp instanceof Pic_parameter_set_rbsp) ||
						( rbsp instanceof Sei_rbsp)){

					count = drawBarAccessUnit(frameSize, labels, accessUnitData, count);
					accessUnitData = new int[6];
				} else if (rbsp instanceof Slice_layer_without_partitioning_rbsp slice_layer_without_partitioning_rbsp) {
					final Slice_header header = slice_layer_without_partitioning_rbsp.getSlice_header();
					final int slice_type = header.getSlice_type();
					final int size = unit.getNumBytesInRBSP();
					accessUnitData[slice_type % 5] += size;
				} else if (rbsp instanceof Slice_layer_extension_rbsp slice_layer_extension_rbsp) {
					final Slice_header header = slice_layer_extension_rbsp.getSlice_header();
					if (header != null) {
						final int slice_type = header.getSlice_type();
						final int size = unit.getNumBytesInRBSP();
						accessUnitData[slice_type % 5] += size;
					}
				}else if( rbsp instanceof Filler_data_rbsp){
					final int size = unit.getNumBytesInRBSP();
					accessUnitData[5] += size;
				}
			}
			unit =  nalIter.next();
		}
		// last unit is not followed by delimiter
		drawBarAccessUnit(frameSize, labels, accessUnitData, count);

		final DefaultKeyedValues2DDataset dataset = new DefaultKeyedValues2DDataset();


		for (int i = 0; i < 6; i++) {
			final String type=getSlice_typeString(i);
			final Iterator<int[]> frameSizeIter = frameSize.iterator();
			for(final ChartLabel l:labels){
				if(frameSizeIter.hasNext()){
					final int[] v = frameSizeIter.next();
					dataset.setValue(v[i], type,l);
				}
			}
		}

		final StackedBarRenderer renderer = new StackedBarRenderer();
		renderer.setShadowVisible(false);
		renderer.setDrawBarOutline(false);
		renderer.setItemMargin(0.0);

		renderer.setSeriesPaint(0, Color.BLUE); // p
		renderer.setSeriesPaint(1, Color.GREEN); // b
		renderer.setSeriesPaint(2, Color.RED); // i
		renderer.setSeriesPaint(3, Color.YELLOW); //SP
		renderer.setSeriesPaint(4, Color.PINK); //SI
		renderer.setSeriesPaint(5, Color.GRAY); // filler

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

	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = new KVP("H.264 PES Data").addImageSource(this, "Frames");
		addListJTree(s,pesPackets,modus,"PES Packets");
		addCCDataToTree(modus, s);
		return s;
	}


	/**
	 * @param pesData
	 * @return
	 */
	protected Video14496PESDataField createH26xPESDataField(final PesPacketData pesData) {
		return new Video14496PESDataField(pesData);
	}

	private static String getSlice_typeString(final int slice_type){
		switch (slice_type) {
		case  0: return "P";
		case  1 : return "B";
		case  2 : return "I";
		case  3 : return "SP";
		case  4 : return "SI";
		case  5 : return "Filler Data";

		default:
			return "??";
		}

	}


}
