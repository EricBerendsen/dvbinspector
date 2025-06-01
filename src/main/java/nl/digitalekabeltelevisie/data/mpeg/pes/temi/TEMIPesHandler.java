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

package nl.digitalekabeltelevisie.data.mpeg.pes.temi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.TemiTimeStamp;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors.TimelineDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

/**
 * 
 */
public class TEMIPesHandler extends GeneralPesHandler {

	public TEMIPesHandler() {
		// TODO Auto-generated constructor stub
	}

	
	@Override
	protected void processPesDataBytes(PesPacketData pesData) {

		TEMIPesDataField temiPesDataField = new TEMIPesDataField(pesData);
		pesPackets.add(temiPesDataField);
		
		List<TimelineDescriptor> timelineDescriptors = Descriptor.findGenericDescriptorsInList(temiPesDataField.getAfDescriptors(),TimelineDescriptor.class);
		int packetNo = temiPesDataField.getStartPacketNo();
		HashMap<Integer, ArrayList<TemiTimeStamp>> temiList = getPID().getTemiList();
		for(TimelineDescriptor timelineDescriptor:timelineDescriptors) {
			if((timelineDescriptor.getHas_timestamp()==1)||
					(timelineDescriptor.getHas_timestamp()==2)){
					ArrayList<TemiTimeStamp> tl = temiList.computeIfAbsent(timelineDescriptor.getTimeline_id(), k -> new ArrayList<>());
					tl.add(new TemiTimeStamp(packetNo, timelineDescriptor.getMedia_timestamp(),timelineDescriptor.getTimescale(),timelineDescriptor.getDiscontinuity(),timelineDescriptor.getPaused()));
			}
		}
		
	}

	@Override
	public String getMenuDescription() {
		return "Parse TEMI";
	}

}
