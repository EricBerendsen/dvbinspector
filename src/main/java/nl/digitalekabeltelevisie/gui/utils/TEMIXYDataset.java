/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
package nl.digitalekabeltelevisie.gui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.PCR;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TemiTimeStamp;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;

public class TEMIXYDataset implements XYDataset {
	
	/** The group that the dataset belongs to. */
	private DatasetGroup group;

	private final List<List<TemiTimeStamp>> seriesList = new ArrayList<>();
	private final List<String> seriesKeys = new ArrayList<>();


	private long firstPcrPacketNo;

	private long pcrBase;

	private long packetsPerSec;

	public TEMIXYDataset(PMTsection pmt, TransportStream transportStream, ViewContext viewContext) {
		
		long bitRate = transportStream.getBitRate();
		int packetInBits = transportStream.getPacketLenghth() * 8;
		
		packetsPerSec = bitRate / packetInBits;
		
		
		PID pcrPid = transportStream.getPID(pmt.getPcrPid());
		PCR firstPCR = pcrPid.getFirstPCR();
		firstPcrPacketNo = pcrPid.getFirstPCRpacketNo();
		pcrBase = firstPCR.getProgram_clock_reference_base();

		for (Component c : pmt.getComponentenList()) {
			short componentPid = (short) c.getElementaryPID();
			String componentLabel = componentPid + " - " + transportStream.getShortLabel(componentPid);
			PID pid = transportStream.getPID(componentPid);
			if (pid != null) {
				Map<Integer, ArrayList<TemiTimeStamp>> temiMap = pid.getTemiMap();
				for (Entry<Integer, ArrayList<TemiTimeStamp>> entry : temiMap.entrySet()) {
					int time_line_id = entry.getKey();
					List<TemiTimeStamp> value = entry.getValue();
					addToSeriesList(value, componentLabel + " TEMI time_line_id:" + time_line_id);
				}
			}
		}
	}

	private void addToSeriesList(List<TemiTimeStamp> list, String componentLabel) {
		if ((list != null) && (!list.isEmpty())) {
			seriesList.add(list);
			seriesKeys.add(componentLabel);
		}
	}

	@Override
	public int getSeriesCount() {
		return seriesList.size();
	}

	@Override
	public String getSeriesKey(int series) {
		return seriesKeys.get(series);
	}

	@Override
	public int indexOf(Comparable seriesKey) {
		return seriesKeys.indexOf(seriesKey);
	}

	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		// NOT USED

	}

	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		// NOT USED

	}

	@Override
	public DatasetGroup getGroup() {
		return group;
	}

	@Override
	public void setGroup(DatasetGroup group) {
		this.group = group;
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

	@Override
	public int getItemCount(int series) {
		return seriesList.get(series).size();
	}

	@Override
	public Number getX(int series, int item) {
		return calcPacketNoFromPts(series, item);
	}

	private long calcPacketNoFromPts(int series, int item) {
		TemiTimeStamp temiTimeStamp = seriesList.get(series).get(item);
		long pts = temiTimeStamp.getPts();
		
		long delta = pts - pcrBase;
		
		if(pts<pcrBase) { // pcr wrapped around
			delta = 0x2_0000_0000L + pts - pcrBase;
		}
		
		return firstPcrPacketNo + (packetsPerSec * delta / 90_000L);
	}

	protected TemiTimeStamp getTimestamp(int series, int item) {
		return seriesList.get(series).get(item);
	}

	@Override
	public double getXValue(int series, int item) {
		return calcPacketNoFromPts(series, item);

	}

	@Override
	public Number getY(int series, int item) {
		return seriesList.get(series).get(item).getTime();
	}

	@Override
	public double getYValue(int series, int item) {
		return seriesList.get(series).get(item).getTime();
	}

}
