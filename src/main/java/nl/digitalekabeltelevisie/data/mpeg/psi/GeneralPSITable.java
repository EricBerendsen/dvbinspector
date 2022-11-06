package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.PreferencesManager;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * This class represents a PSI table, without interpreting it.
 *
 */
public class GeneralPSITable extends AbstractPSITabel{
	
	public class TableSectionOccurrence{

		private final int packetNo;
		private final TableSection tableSection;

		public TableSectionOccurrence(int packetNo, TableSection tableSection) {
			super();
			this.packetNo = packetNo;
			this.tableSection = tableSection;
		}

		public int getPacketNo() {
			return packetNo;
		}

		public TableSection getTableSection() {
			return tableSection;
		}

		@Override
		public String toString() {
			return "TableSectionOccurrence [packetNo=" + packetNo + ", tableSection=" + tableSection + "]";
		}

	}

	// Contains only unique table section (long syntax. ie section_syntax_indicator==1)
	// Each section keeps track of count, first and last occurrence, min/max time between occurrences
	//
	//                   tableId    tableIdExtension       sectionNumber
	private final TreeMap<Integer, TreeMap<Integer,TableSection []>> longSections = new TreeMap<>();
	// Contains only unique table section (short syntax. ie section_syntax_indicator==0)
	private LinkedHashMap<TableSection, TableSection> simpleSectionsd = new LinkedHashMap<>();
	
	// contains entry for each occurrence of a TableSection, with packetNo at which it really started 
	// (The packetyNo in the referenced  TableSection is the first occurence
	private List<TableSectionOccurrence> tableSectionOccurrences = new ArrayList<>();
	


	public GeneralPSITable(final PSI parent){
		super(parent);
	}

	public void update(final TableSection section){

		int startPacket = section.getPacket_no();

		if(section.sectionSyntaxIndicator==0x01){ // long suntax, section_syntax_indicator==1

			final int tableId = section.getTableId();
			TreeMap<Integer, TableSection[]> table = longSections.computeIfAbsent(tableId, k -> new TreeMap<>());
			TableSection[] sections = table.computeIfAbsent(section.getTableIdExtension(), k -> new TableSection[section.getSectionLastNumber() + 1]);

			if(sections.length<=section.getSectionNumber()){ //resize if needed
				sections = Arrays.copyOf(sections, section.getSectionNumber()+1);
				table.put(section.getTableIdExtension(),sections);
			}
			if(sections[section.getSectionNumber()]==null){
				sections[section.getSectionNumber()] = section;
				tableSectionOccurrences.add(new TableSectionOccurrence(startPacket, section));
			}else{
				final TableSection last = sections[section.getSectionNumber()];
				TableSection refSection = updateSectionVersion(section, last);
				tableSectionOccurrences.add(new TableSectionOccurrence(startPacket, refSection));
			}
		}else{ // short syntax, section_syntax_indicator==0
			// look for duplicates, if so update counters on existing on
			TableSection existingSection = simpleSectionsd.get(section);
			if (existingSection != null)
			{
				int previousPacketNo = existingSection.getLast_packet_no();
				int distance = section.getPacket_no() - previousPacketNo;
				if(distance>existingSection.getMaxPacketDistance()){
					existingSection.setMaxPacketDistance(distance);
				}
				if(distance<existingSection.getMinPacketDistance()){
					existingSection.setMinPacketDistance(distance);
				}

				existingSection.setLast_packet_no(section.getPacket_no());
				existingSection.setOccurrence_count(existingSection.getOccurrence_count()+1);
				tableSectionOccurrences.add(new TableSectionOccurrence(startPacket, existingSection));
			}else{
				simpleSectionsd.put(section, section);
				tableSectionOccurrences.add(new TableSectionOccurrence(startPacket, section));
			}
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PSI Data"));
		if(!PreferencesManager.isEnableGenericPSI()) {
		    t.add(new DefaultMutableTreeNode(GuiUtils.getErrorKVP ("Generic PSI not enabled, select 'Settings -> Enable Generic PSI' to enable ")));
		    return t;
		}
		
		for (Entry<Integer, TreeMap<Integer, TableSection[]>> tableIDSections : longSections.entrySet()) {
			int tableId = tableIDSections.getKey();
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("table_id", tableId, TableSection.getTableType(tableId)));
			final TreeMap<Integer, TableSection[]> tableIdExtensionSections = tableIDSections.getValue();

			for ( Entry<Integer, TableSection[]> tableIdExtensionSection : tableIdExtensionSections.entrySet()) {
				int tableIdExt = tableIdExtensionSection.getKey();
				final DefaultMutableTreeNode o = new DefaultMutableTreeNode(new KVP("table_id_extension", tableIdExt, null));
				final TableSection[] sections = tableIdExtensionSection.getValue();
				for (TableSection section : sections) {
					if (section != null) {
						if (!Utils.simpleModus(modus)) { // show all versions
							addSectionVersionsToJTree(o, section, modus);
						} else { // keep it simple
							o.add(section.getJTreeNode(modus));
						}
					}
				}
				n.add(o);
			}
			t.add(n);
		}
		if(!simpleSectionsd.isEmpty()){
			Utils.addListJTree(t, simpleSectionsd.keySet(), modus, "syntax0");
		}
		return t;
	}


	public boolean exists(final int tableId, final int tableIdExtension, final int section){
		return ((longSections.get(tableId)!=null) &&
				(longSections.get(tableId).get(tableIdExtension)!=null) &&
				(longSections.get(tableId).get(tableIdExtension).length >section) &&
				(longSections.get(tableId).get(tableIdExtension)[section]!=null));
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = (PRIME * result) + ((simpleSectionsd == null) ? 0 : simpleSectionsd.hashCode());
		result = PRIME * result + longSections.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		final GeneralPSITable other = (GeneralPSITable) obj;
		if (simpleSectionsd == null) {
			if (other.simpleSectionsd != null){
				return false;
			}
		} else if (!simpleSectionsd.equals(other.simpleSectionsd)){
			return false;
		}
		return longSections.equals(other.longSections);
	}

	public Map<Integer,TreeMap<Integer,TableSection[]>> getLongSections() {
		return longSections;
	}

	public Collection<TableSection> getSimpleSectionsd() {
		return simpleSectionsd.keySet();
	}

	public List<TableSectionOccurrence> getTableSectionOccurrences() {
		return tableSectionOccurrences;
	}

}
