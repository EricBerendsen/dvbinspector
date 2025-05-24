/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.*;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory.buildDescriptorList;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class ONTSection extends TableSectionExtendedSyntax {
	
	private static final Logger	logger	= Logger.getLogger(ONTSection.class.getName());

	public record OperatorBrand(int operator_network_id, int operator_sublist_id, int reserved_future_use,
								int operator_descriptors_length, List<Descriptor> descriptorList) implements TreeNode {


	@Override
		public KVP getJTreeNode(int modus) {

			KVP t = new KVP("operator_brand", operator_network_id + "-" + operator_sublist_id);

			t.add(new KVP("operator_network_id", operator_network_id));
			t.add(new KVP("operator_sublist_id", operator_sublist_id));
			t.add(new KVP("reserved_future_use", reserved_future_use));
			t.add(new KVP("operator_descriptors_length", operator_descriptors_length));

			Utils.addListJTree(t, descriptorList, modus, "operator_descriptors");

			return t;
		}

	}

	private final int bouquet_descriptors_loop_length;
	private final List<Descriptor> bouquetDescriptorList;
	private final int operator_brands_loop_length;
	private final List<OperatorBrand> operatorBrandList;

	public ONTSection(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);
		bouquet_descriptors_loop_length = getInt(raw_data.getData(), 8, 2, Utils.MASK_12BITS);
		bouquetDescriptorList = buildDescriptorList(raw_data.getData(),10,bouquet_descriptors_loop_length,this);
		operator_brands_loop_length = getInt(raw_data.getData(), 10 +bouquet_descriptors_loop_length , 2, Utils.MASK_12BITS);
		operatorBrandList = buildOperatorBrandList(raw_data.getData(), 12+bouquet_descriptors_loop_length, operator_brands_loop_length);
	}

	private List<OperatorBrand> buildOperatorBrandList(byte[] data, int i, int operator_brands_loop_length2) {
		List<OperatorBrand> r = new ArrayList<>();
		int t = 0;
		try {
			while (t < operator_brands_loop_length2) {
				int operatorNetworkId = getInt(data, i + t, 2, Utils.MASK_16BITS);
				int operatorSublistId = getInt(data, i + t + 2, 1, Utils.MASK_8BITS);
				int reservedFutureUse = getInt(data, i + t + 3, 1, 0xF0) >>> 4;
				int operator_descriptors_length = getInt(data, i + t + 3, 2, Utils.MASK_12BITS);
				List<Descriptor> descriptorList = buildDescriptorList(data, i + t + 5, operator_descriptors_length, this);

				OperatorBrand operatorBrand = new OperatorBrand(operatorNetworkId, operatorSublistId, reservedFutureUse, operator_descriptors_length, descriptorList);
				r.add(operatorBrand);
				t += 5 + operator_descriptors_length;
			}
		} catch (RuntimeException re) {
			logger.info("RuntimeException in buildOperatorBrandList;" + re);
		}

		return r;
	}

	@Override
	public KVP getJTreeNode(int modus){

		KVP kvp = (KVP) super.getJTreeNode(modus);
		kvp.addTableSource(this::getTableModel, "ONT Section");

		kvp.add( new KVP("bouquet_descriptors_loop_length",bouquet_descriptors_loop_length ));
		kvp.addList(bouquetDescriptorList,modus,"bouquet_descriptors");
		kvp.add( new KVP("operator_brands_loop_length",operator_brands_loop_length ));
		kvp.addList(operatorBrandList,modus,"operator_brands_loop");

		return kvp;
	}

	public String getOperatorName(int operator_network_id) {
		
		for( OperatorBrand operatorBrand: operatorBrandList) {
			if(operatorBrand.operator_network_id()==operator_network_id) {
				List<M7OperatorNameDescriptor> operatorNameDescriptors = findGenericDescriptorsInList(operatorBrand.descriptorList(), M7OperatorNameDescriptor.class);
				if(!operatorNameDescriptors.isEmpty()) {
					return operatorNameDescriptors.getFirst().getOperatorName().toString();
				}
			}
		}
		return null;
	}

	public String getOperatorSublistName(int operator_network_id, int operator_sublist_id) {
		
		for( OperatorBrand operatorBrand: operatorBrandList) {
			if((operatorBrand.operator_network_id()==operator_network_id) &&
					(operatorBrand.operator_sublist_id()==operator_sublist_id)){
				List<M7OperatorSublistNameDescriptor> operatorSublistNameDescriptors = findGenericDescriptorsInList(operatorBrand.descriptorList(), M7OperatorSublistNameDescriptor.class);
				if(!operatorSublistNameDescriptors.isEmpty()) {
					return operatorSublistNameDescriptors.getFirst().getOperatorSublistName().toString();
				}
			}
		}
		return null;
	}

	@Override
    protected String getTableIdExtensionLabel() {
		return "bouquet_id";
	}

	public TableModel getTableModel() {
		FlexTableModel<ONTSection,OperatorBrand> tableModel =  new FlexTableModel<>(buildOntTableHeader());

		tableModel.addData(this, operatorBrandList);

		tableModel.process();
		return tableModel;
	}

	static TableHeader<ONTSection,OperatorBrand>  buildOntTableHeader() {
		return new TableHeaderBuilder<ONTSection,OperatorBrand>().
				addRequiredRowColumn("operator network id", OperatorBrand::operator_network_id, Integer.class).
				addRequiredRowColumn("operator_sublist_id", OperatorBrand::operator_sublist_id, Integer.class).

				addOptionalRowColumn("operator name",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorNameDescriptor.class,
								ond -> ond.getOperatorName().toString()),
						String.class).
				addOptionalRowColumn("operator sublist name",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorSublistNameDescriptor.class,
								osnd -> osnd.getOperatorSublistName().toString()),
						String.class).
				addOptionalRowColumn("nagra brand id",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7NagraBrandIdDescriptor.class,
								nbid -> nbid.getNagra_brand_id()),
						Integer.class).
				addOptionalRowColumn("nagra ca system id",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7NagraBrandIdDescriptor.class,
								nbid -> nbid.getCa_system_ID()),
						Integer.class).
				addOptionalRowColumn("ott brand id",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OttBrandIdDescriptor.class,
								obid -> obid.getOtt_brand_id().toString()),
						String.class).
				addOptionalRowColumn("country code",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getCountry_code),
						String.class).
				addOptionalRowColumn("menu osd",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getMenu_ISO_639_language_code),
						String.class).
				addOptionalRowColumn("audio 1",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getAudio1_ISO_639_language_code),
						String.class).
				addOptionalRowColumn("audio 2",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getAudio2_ISO_639_language_code),
						String.class).
				addOptionalRowColumn("subs lang",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getSubs_ISO_639_language_code),
						String.class).

				addOptionalRowColumn("subs usage",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getSubtitles_enabled),
						Integer.class).
				addOptionalRowColumn("parental control",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getParentalControlString),
						String.class).

				addOptionalRowColumn("FST char set",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getEncodingTypeString),
						String.class).
				addOptionalRowColumn("region",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getSpecial_regions_setup),
						Integer.class).

				addOptionalRepeatingRowColumn("DiSeq Pos",
						operator -> findDescriptorApplyListFunc(operator.descriptorList(),
								M7OperatorDiSEqCTDescriptor.class,
								odd -> odd.getDiSEqCList().
								stream().
								map(M7OperatorDiSEqCTDescriptor.DiSEqC::getTotalPositionString).
								collect(Collectors.toList())),

						String.class).

				build();
	}

	public List<OperatorBrand> getOperatorBrandList() {
		return operatorBrandList;
	}

}
