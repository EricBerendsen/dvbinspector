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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * DVB BlueBook A038r16 – (April 2023) 6.4.18 Service Prominence Descriptor
 */
public class ServiceProminenceDescriptor extends DVBExtensionDescriptor {
	
	
	private static class ServiceOfGeneralInterest implements TreeNode{

		private static class TargetRegion implements TreeNode{
			
			private int reserved_future_use;
			private int country_code_flag;
			private int region_depth;
			private byte[] country_code;
			private int primary_region_code;
			private int secondary_region_code;
			private int tertiary_region_code;

			public TargetRegion(BitSource bs) {
				reserved_future_use = bs.readBits(5);
				country_code_flag = bs.readBits(1);
				region_depth = bs.readBits(2);
				if (country_code_flag == 0b1) {
					country_code = bs.readBytes(3);
				}
				if (region_depth >= 1) {
					primary_region_code = bs.readBits(8);
					if (region_depth >= 2) {
						secondary_region_code = bs.readBits(8);
						if (region_depth == 3) {
							tertiary_region_code = bs.readBits(16);
						}
					}
				}
				
			}

            @Override
            public KVP getJTreeNode(int modus) {
                KVP t = new KVP("Target Region");
                t.add(new KVP("reserved_future_use", reserved_future_use));
                t.add(new KVP("country_code_flag", country_code_flag));
                t.add(new KVP("region_depth", region_depth));
                if (country_code_flag == 0b1) {
                    t.add(new KVP("country_code", country_code, getISO8859_1String(country_code, 0, 3)));
                }
                if (region_depth >= 1) {
                    t.add(new KVP("primary_region_code", primary_region_code));
                    if (region_depth >= 2) {
                        t.add(new KVP("secondary_region_code", secondary_region_code));
                        if (region_depth == 3) {
                            t.add(new KVP("tertiary_region_code", tertiary_region_code));
                        }
                    }
                }
                return t;
            }
		}
		
		
		private int SOGI_flag;
		private int target_region_flag;
		private int service_flag;
		private int reserved_future_use;
		private int SOGI_priority;
		private int service_id;
		private int target_region_loop_length;
		
		private List<TargetRegion> targetRegionList = new ArrayList<>(); 

		public ServiceOfGeneralInterest(BitSource bs) {
			
			SOGI_flag = bs.readBits(1);
			target_region_flag = bs.readBits(1);
			service_flag = bs.readBits(1);
			reserved_future_use = bs.readBits(1);
			SOGI_priority = bs.readBits(12);
			if (service_flag == 0b1) {
				service_id = bs.readBits(16);
			}
			
			if (target_region_flag == 0b1) {
				target_region_loop_length = bs.readBits(8);
				BitSource targetRegionBitSource = new BitSource(bs, target_region_loop_length);
				while (targetRegionBitSource.available() > 0) {
					TargetRegion targetRegion = new TargetRegion(targetRegionBitSource);
					targetRegionList.add(targetRegion);
				}
				bs.advanceBytes(target_region_loop_length);
			}
		}

        @Override
        public KVP getJTreeNode(int modus) {
            KVP t = new KVP("Service Of General Interest");
            t.add(new KVP("SOGI_flag", SOGI_flag));
            t.add(new KVP("target_region_flag", target_region_flag));
            t.add(new KVP("service_flag", service_flag));
            t.add(new KVP("reserved_future_use", reserved_future_use));
            t.add(new KVP("SOGI_priority", SOGI_priority));
            if (service_flag == 0b1) {
                t.add(new KVP("service_id", service_id));
            }

            if (target_region_flag == 0b1) {
                t.add(new KVP("target_region_loop_length", target_region_loop_length));
                addToList(t, targetRegionList, modus);
            }
            return t;

        }
		
	}


	private final int sogi_list_length;
	
	private final List<ServiceOfGeneralInterest> sogiList = new ArrayList<>();
	
	private byte[] private_data_byte;


	public ServiceProminenceDescriptor( byte[] b, TableSection parent) {
		super(b, parent);
		sogi_list_length = getInt(b, 3, 1, MASK_8BITS);
		
		BitSource bitSourceWholeDescriptor =new BitSource(selector_byte, 1);
		 
		BitSource bitSourceOsgiList = new BitSource(bitSourceWholeDescriptor,sogi_list_length);
		while(bitSourceOsgiList.available()>=2) {
			ServiceOfGeneralInterest serviceOfGeneralInterest = new ServiceOfGeneralInterest(bitSourceOsgiList);
			sogiList.add(serviceOfGeneralInterest);
		}
		
		bitSourceWholeDescriptor.advanceBytes(sogi_list_length);
		private_data_byte = bitSourceWholeDescriptor.readBytes(bitSourceWholeDescriptor.available()/8);
	}


	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("SOGI_list_length",sogi_list_length));
		addToList(t, sogiList, modus);
		t.add(new KVP("private_data_byte",private_data_byte));

		return t;
	}

}
