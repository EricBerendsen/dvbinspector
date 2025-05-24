package nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.*;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.LinkageDescriptor.BrandHomeTransponder;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan.ONTSection.OperatorBrand;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class M7Fastscan implements TreeNode {
	
	private final Map<Integer, Map<Integer,OperatorFastscan>> operators = new HashMap<>();
	private ONTSection[] ontSections;

	@SuppressWarnings("unused")
	private final PSI parentPSI;

	public static boolean isValidM7Code(int code) {
		return (code>= 0x7701) && (code<= 0x77FF);
	}
	
	public M7Fastscan(PSI psi) {
		this.parentPSI = psi;
	}


	public void update(ONTSection section){
		if(ontSections == null) {
			ontSections = new ONTSection[section.getSectionLastNumber()+1];
		}
		if(ontSections[section.getSectionNumber()]==null){
			ontSections[section.getSectionNumber()] = section;
		}else{
			TableSection last = ontSections[section.getSectionNumber()];
			AbstractPSITabel.updateSectionVersion(section, last);
		}	
	}
	
	public void update(FSTsection section){
		int pid = section.getParentPID().getPid();
		int operator = section.getOperatorNetworkID();
		
		findOrCreateOperatorFastscan(operator,pid).update(section);
	}

	
	private OperatorFastscan findOrCreateOperatorFastscan(int operator, int pid) {
		Map<Integer, OperatorFastscan> p = operators.computeIfAbsent(operator, k -> new HashMap<>());
		return p.computeIfAbsent(pid, k -> new OperatorFastscan(pid, this));
	}


	public void update(FNTsection section){
		int pid = section.getParentPID().getPid();
		int operator = section.getOperatorNetworkID();
		
		findOrCreateOperatorFastscan(operator,pid).update(section);
	}
	
	public String getOperatorName(int operator_network_id) {

		if (ontSections != null) {
			for (ONTSection ontSection : ontSections) {
				if (ontSection != null) {
					String r = ontSection.getOperatorName(operator_network_id);
					if (r != null) {
						return r;
					}
				}
			}
		}
		return null;
	}
	
	

	public String getOperatorSubListName(int operatorNetworkId, int fstPid) {
		
		NIT nit = parentPSI.getNit();
		int actualNetworkID = nit.getActualNetworkID();
		List<Descriptor> descriptors = nit.getNetworkDescriptors(actualNetworkID);
		
		Optional<LinkageDescriptor> optionalHomeTP_location_descriptor = descriptors.stream()
		    .filter(LinkageDescriptor.class::isInstance)
		    .map (LinkageDescriptor.class::cast)
		    .filter(linkageDescriptor -> (linkageDescriptor.getLinkageType() >= 0x88 &&linkageDescriptor.getLinkageType() <=0x8a)) // linkage_type(M7 Fastscan Home TP location descriptor)
		    .findFirst();
	    
		 if(optionalHomeTP_location_descriptor.isPresent()) {
			 LinkageDescriptor homeTP_location_descriptor = optionalHomeTP_location_descriptor.get();
			 Optional<BrandHomeTransponder> optionalBrandHomeTransponder = homeTP_location_descriptor.getM7BrandHomeTransponderList()
			 	.stream()
			 	.filter(brandHomeTransponder -> brandHomeTransponder.operator_network_id() == operatorNetworkId)
			 	.filter(brandHomeTransponder -> brandHomeTransponder.fst_pid() == fstPid)
			 	.findFirst();
			 

			 
			if (optionalBrandHomeTransponder.isPresent()) {
				BrandHomeTransponder brandHomeTransponder = optionalBrandHomeTransponder.get();
				int sublist_id = brandHomeTransponder.operator_sublist_id();
				String r = "sublist_id: " + sublist_id;
				if(ontSections!=null) {
					for(ONTSection ontSection:ontSections) {
						if(ontSection!=null) {
							String sublistNamme = ontSection.getOperatorSublistName(operatorNetworkId,sublist_id);
							if(sublistNamme != null) {
								return r+" : "+sublistNamme ;
							}
						}
					}
				}
				return r;
			}
		 }

		return null;
	}


	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("M7 Fastscan");
		
		if(ontSections!=null) {

			KVP ont = new KVP("ONT");
			ont.addTableSource(this::getTableModelOnt, "ONT");
			for (ONTSection ontSection : ontSections) {
				if(ontSection!= null){
					AbstractPSITabel.addSectionVersionsToJTree(ont, ontSection, modus);
				}
			}
			
			t.add(ont);
		}

		for (Integer operatorId : new TreeSet<>(operators.keySet())) {
			Map<Integer, OperatorFastscan> operatorsInPid = operators.get(operatorId);
			KVP operatorNode = new KVP("operator_network_id",operatorId,getOperatorName(operatorId));
			t.add(operatorNode);
			for (Integer pid : new TreeSet<>(operatorsInPid.keySet())) {
				OperatorFastscan operatorFastscan = operatorsInPid.get(pid);
				operatorNode.add(operatorFastscan.getJTreeNode(modus));
			}
			
		}
			
		return t;
	}

	public TableModel getTableModelOnt() {
		FlexTableModel<ONTSection,OperatorBrand> tableModel =  new FlexTableModel<>(ONTSection.buildOntTableHeader());

		for (ONTSection ontSection : ontSections) {
			if (ontSection != null) {
				tableModel.addData(ontSection, ontSection.getOperatorBrandList());
			}
		}

		tableModel.process();
		return tableModel;
	}

	public Map<Integer, Map<Integer, OperatorFastscan>> getOperators() {
		return operators;
	}

	public ONTSection[] getOntSections() {
		return ontSections;
	}


}
