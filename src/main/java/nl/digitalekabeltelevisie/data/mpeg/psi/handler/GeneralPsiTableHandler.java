/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.DSMCCs;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.FNTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.FSTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.M7Fastscan;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.ONTSection;
import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * @author Eric
 *
 */
public class GeneralPsiTableHandler extends GeneralPidHandler {

	
	private PAT pat;
	private CAT cat;
	private BAT bat;
	private NIT nit;
	private SDT sdt;
	private PMTs pmts;
	private EIT eit;
	private TDT tdt;
	private TOT to;
	private SIT sit;
	
	private INT int_table;
	private UNTs unt_table;
	private AITs ait_table;
	private RCTs rct_table;
	private DSMCCs dsm_table;
	private SCTE35 scte35_table;
	private DFITs dfit_table;
	
	private M7Fastscan m7fastscan;
	


	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		var node = new DefaultMutableTreeNode(new KVP("GeneralPsiTableHandler"));
		addToNodeIfNotNull(node, pat, modus);
		addToNodeIfNotNull(node, pmts, modus);
		addToNodeIfNotNull(node, nit, modus);
		addToNodeIfNotNull(node, bat, modus);
		addToNodeIfNotNull(node, eit, modus);
		return node;
	}



	private static void addToNodeIfNotNull(DefaultMutableTreeNode node,  AbstractPSITabel abstractPSITabel, int modus) {
		if(abstractPSITabel!=null) {
			node.add(abstractPSITabel.getJTreeNode(modus));
		}
	}

	
	
	@Override
	public void processTSPacket(TSPacket packet) {
		// EMPTY all action in postProcess 
		System.err.println("processTSPacket");
	}

	@Override
	public void postProcess() {
		// EMPTY default
		initialized = true;
		System.err.println("postProcess");
		
		GeneralPSITable psi = pid.getPsi();
		Map<Integer, HashMap<Integer, TableSection[]>> typeSections = psi.getData();
		
		final TreeSet<Integer> tableIDs = new TreeSet<>(typeSections.keySet());

		for (Integer tableID : tableIDs) {

			final HashMap<Integer, TableSection[]> table = typeSections.get(tableID);

			final TreeSet<Integer> serviceSet = new TreeSet<>(table.keySet());
			for (Integer tableIdExt : serviceSet) {
				final TableSection[] sections = table.get(tableIdExt);
				if (tableID == 0x00) {
					handlePAT(sections);
					
				}else if (tableID == 0x02) {
					handlePMT(sections);
				}else if (tableID == 0x10) {
					handleNIT(sections);
				}else if (tableID == 0x4A) {
					handleBAT(sections);
				}else if((0x4E<=tableID)&&(tableID<=0x6F)){
					handleEIT(sections);
				}
				
				
//				
//				
//				
//			}else if((tableId==0x01)&&(pid==0x01)){
//				transportStream.getPsi().getCat().update(new CAsection(this,parentPID));
//			}else if((tableId==0x4A)&&(pid==0x11)){
//				transportStream.getPsi().getBat().update(new BATsection(this,parentPID));
//			}else if((0x4E<=tableId)&&(tableId<=0x6F)&&(pid==0x12)){
//				transportStream.getPsi().getEit().update(new EITsection(this,parentPID));
//			}else if((pid==0x14) &&(tableId==0x70)){
//				transportStream.getPsi().getTdt().update(new TDTsection(this,parentPID));
//			}else if((pid==0x14) &&(tableId==0x73)){
//				transportStream.getPsi().getTot().update(new TOTsection(this,parentPID));
//			}else if((pid==0x11) &&((tableId==0x42)||(tableId==0x46))){
//				transportStream.getPsi().getSdt().update(new SDTsection(this,parentPID));
//			}else if((pid==0x1F) &&(tableId==0x7F)){
//				transportStream.getPsi().getSit().update(new SITsection(this,parentPID));
//			}else if((tableId==0x4c)&&isINTSection(pid)){ // check for linkage descriptors 0x0B located in the NIT  //ETSI EN 301 192 V1.4.2
//				transportStream.getPsi().getInt().update(new INTsection(this,parentPID));
//			}else if((tableId==0x4b)&&isUNTSection(pid)){
//				transportStream.getPsi().getUnts().update(new UNTsection(this,parentPID));
//			}else if((tableId==0x74)&&isAITSection(pid)){
//				transportStream.getPsi().getAits().update(new AITsection(this,parentPID));
//			}else if((tableId==0x76)&&isRCTSection(pid)){
//				transportStream.getPsi().getRcts().update(new RCTsection(this,parentPID));
//			}else if((tableId==0xFC)&&isSpliceInfoSection(pid)){
//				transportStream.getPsi().getScte35_table().update(new SpliceInfoSection(this,parentPID));
//			}else if(isDIFTSection(pid)) { // no check for table ID, as this might change 
//				transportStream.getPsi().getDfit_table().update(new DFITSection(this, parentPID));
//			}else if((tableId>=0x37)&&(tableId<=0x3F)){
//				// also include all PES streams component (ISO/IEC 13818-6 type B) which
//				// do not have a data_broadcast_id_descriptor associated with it,
//				// but do have a Association_tag_descriptor (or a stream_identifier_descriptor)
//				// These might be referenced from DSI in other stream (or even from multiple)
//				// Also, include PMTs to store the stream_identifier_descriptor
//				// all handled in DSMCCs.
//				if(PreferencesManager.isEnableDSMCC()) {
//					transportStream.getPsi().getDsms().update(new TableSectionExtendedSyntax(this,parentPID));
//				}
//			}else if(PreferencesManager.isEnableM7Fastscan()) {
//				if(tableId== 0xBC){
//					transportStream.getPsi().getM7fastscan().update(new FNTsection(this, parentPID));
//				}else if(tableId== 0xBD) {
//					transportStream.getPsi().getM7fastscan().update(new FSTsection(this, parentPID));
//				}else if((tableId== 0xBE) && transportStream.isONTSection(pid)) {
//					transportStream.getPsi().getM7fastscan().update(new ONTSection(this, parentPID));
//				}
//			}

			}
		}

	}
	
	private void handlePAT(final TableSection[] sections) {
		if (pat == null) {
			pat = new PAT(getTransportStream().getPsi());
		}
		for (TableSection tableSection : sections) {
			if (tableSection != null) {
				PATsection pmt = new PATsection(tableSection.getRaw_data(), pid);
				copyMetaData(tableSection, pmt);
				pat.update(pmt);
			}
		}
	}


	private void handlePMT(final TableSection[] sections) {
		if (pmts == null) {
			pmts = new PMTs(getTransportStream().getPsi());
		}
		for (TableSection tableSection : sections) {
			if (tableSection != null) {
				PMTsection pmt = new PMTsection(tableSection.getRaw_data(), pid);
				copyMetaData(tableSection, pmt);
				pmts.update(pmt);
			}
		}
	}


	private void handleNIT(final TableSection[] sections) {
		if (nit == null) {
			nit = new NIT(getTransportStream().getPsi());
		}
		for (TableSection tableSection : sections) {
			if (tableSection != null) {
				NITsection s = new NITsection(tableSection.getRaw_data(), pid);
				copyMetaData(tableSection, s);
				nit.update(s);
			}
		}
	}


	private void handleBAT(final TableSection[] sections) {
		if (bat == null) {
			bat = new BAT(getTransportStream().getPsi());
		}
		for (TableSection tableSection : sections) {
			if (tableSection != null) {
				BATsection s = new BATsection(tableSection.getRaw_data(), pid);
				copyMetaData(tableSection, s);
				bat.update(s);
			}
		}
	}
	
	private void handleEIT(final TableSection[] sections) {
		if (eit == null) {
			eit = new EIT(getTransportStream().getPsi());
		}
		for (TableSection tableSection : sections) {
			if (tableSection != null) {
				EITsection s = new EITsection(tableSection.getRaw_data(), pid);
				copyMetaData(tableSection, s);
				eit.update(s);
			}
		}
	}


	private static void copyMetaData(TableSection source, TableSection dest) {
		dest.setFirst_packet_no(source.getFirst_packet_no());
		dest.setLast_packet_no(source.getLast_packet_no());
		dest.setMaxPacketDistance(source.getMaxPacketDistance());
		dest.setMinPacketDistance(source.getMinPacketDistance());
		dest.setOccurrence_count(source.getOccurrence_count());
	}


}
