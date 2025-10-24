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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class DataBroadcastIDDescriptor extends Descriptor {
	// TODO refactor, common code DataBroadcastIDDescriptor and DataBroadcastDescriptor should not be duplicated

	private final List<OUIEntry> ouiList = new ArrayList<>();
	private final List<ApplicationType> applicationTypeList = new ArrayList<>();
	private final List<MHEG5ApplicationType> mheg5ApplicationTypeList = new ArrayList<>();

	private final List<Platform> platformList = new ArrayList<>();

	/**
	 * broadcast IDs that will be interpreted as indicating an object carousel.
	 */
	private static final Set<Integer> BROADCASTIDS_WITH_OBJECT_CAROUSEL = Set.of(
			0x0007, // DVB object carousel
			0x00f0,	// MHP Object
			0x0106,	// MHEG5(The Digital Network)
			0x0123,	// HBBTV Carousel
			0x0150, // OIPF Object Carousel
			0xBBB2	// BBG Object Carousel
	);

	public static record Platform(int platform_id, int action_type, int int_versioning_flag, int int_version) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("platforms");
			s.add(new KVP("platform_id", platform_id, getPlatformIDString(platform_id)));
			s.add(new KVP("action_type", action_type, getActionTypeString(action_type)));
			s.add(new KVP("INT_versioning_flag", int_versioning_flag));
			s.add(new KVP("INT_version", int_version));
			return s;
		}
	}


	public static record ApplicationType(int applicationType) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus){
			return new KVP("application_type",applicationType,getAppTypeIDString(applicationType));
		}

	}

	public static record MHEG5ApplicationType(int applicationTypeCode, int boot_priority_hint, int application_specific_data_length, byte[] application_specific_data_byte) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus){
			KVP s=new KVP("MHEG5ApplicationType");
			s.add(new KVP("application_type_code",applicationTypeCode,getMHEG5ApplicationTypeString(applicationTypeCode)));
			s.add(new KVP("boot_priority_hint",boot_priority_hint));
			s.add(new KVP("application_specific_data_length",application_specific_data_length));
			s.add(new KVP("application_specific_data_byte",application_specific_data_byte));

			return s;
		}

	}
	public record OUIEntry( int oui,  int updateType,  int updateVersioningFlag,  int updateVersion,  int selectorLength,  byte[] ouiSelectorBytes) implements TreeNode{



		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("OUI");
			s.add(new KVP("oui", oui, Utils.getOUIString(oui)));
			s.add(new KVP("update_type", updateType, getUpdateTypeString(updateType)));
			s.add(new KVP("update_versioning_flag", updateVersioningFlag,
					updateVersioningFlag == 0 ? "no relevant versioning information is carried in the version field"
							: "version field reflects changes in the system software update service component")); // ETSI TS 102 006 V1.2.1 p7.1
			s.add(new KVP("update_version", updateVersion));
			s.add(new KVP("selector_length", selectorLength));
			s.add(new KVP("selector_bytes", ouiSelectorBytes));
			return s;
		}


	}

	private final int dataBroadcastId;
	private int OUI_data_length;

	private int platform_id_data_length;

	private byte[] selectorByte;
	private byte[] privateDataByte;

	private int MAC_address_range;
	private int MAC_IP_mapping_flag;
	private int alignment_indicator;
	private int max_sections_per_datagram ;


	public DataBroadcastIDDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		dataBroadcastId = Utils.getInt(b, 2, 2, Utils.MASK_16BITS);
        switch (dataBroadcastId) {
            case 0x0005 -> {
                MAC_address_range = Utils.getInt(b, 4, 1, 0xE0) >>> 5;
                MAC_IP_mapping_flag = Utils.getInt(b, 4, 1, 0x10) >>> 4;
                alignment_indicator = Utils.getInt(b, 4, 1, 0x08) >>> 3;
                max_sections_per_datagram = Utils.getInt(b, 5, 1, Utils.MASK_8BITS);
            }
            case 0x000a -> { // system software update service, TS 102 006 V1.4.1 Ch 7.1
                OUI_data_length = getInt(b, 4, 1, MASK_8BITS);
                int r = 0;
                while (r < OUI_data_length) {
                    int oui = getInt(b, 5 + r, 3, Utils.MASK_24BITS);
                    int updateType = getInt(b, 8 + r, 1, Utils.MASK_4BITS);
                    int updateVersioningFlag = getInt(b, 9 + r, 1, 0x20) >> 5;
                    int updateVersion = getInt(b, 9 + r, 1, Utils.MASK_5BITS);
                    int selectorLength = getInt(b, r + 10, 1, MASK_8BITS);
                    byte[] selector_byte = copyOfRange(b, r + 11, r + 11 + selectorLength);
                    OUIEntry ouiEntry = new OUIEntry(oui, updateType, updateVersioningFlag, updateVersion, selectorLength, selector_byte);
                    ouiList.add(ouiEntry);
                    r = r + 6 + selectorLength;
                }
                privateDataByte = copyOfRange(b, 5 + r, descriptorLength + 2);
            }
            case 0x000b -> { //IP/MAC_notification_info structure ETSI EN 301 192 V1.4.2
                platform_id_data_length = getInt(b, 4, 1, MASK_8BITS);
                int r = 0;
                while (r < platform_id_data_length) {
                    int platform_id = getInt(b, 5 + r, 3, Utils.MASK_24BITS);
                    int action_type = getInt(b, 8 + r, 1, Utils.MASK_8BITS);

                    int INT_versioning_flag = getInt(b, 9 + r, 1, 0x20) >>> 5;
                    int INT_version = getInt(b, 9 + r, 1, Utils.MASK_5BITS);

                    Platform p = new Platform(platform_id, action_type, INT_versioning_flag, INT_version);
                    platformList.add(p);
                    r = r + 5;
                }
                privateDataByte = copyOfRange(b, 5 + r, descriptorLength + 2);


            }
            case 0x00f0, 0x00f1 -> { // MHP
                int r = 0;
                while (r < (descriptorLength - 2)) {
                    int at = getInt(b, 4 + r, 2, Utils.MASK_15BITS);
                    ApplicationType appT = new ApplicationType(at);
                    applicationTypeList.add(appT);
                    r += 2;
                }

            }
            case 0x0106 -> { //ETSI ES 202 184 V2.2.1 (2011-03) 9.3.2.1 data_broadcast_id_descriptor
                int r = 0;
                while (r < (descriptorLength - 2)) {
                    int at = getInt(b, 4 + r, 2, Utils.MASK_16BITS);
                    int bootPrio = getInt(b, 6 + r, 1, Utils.MASK_8BITS);
                    int appDataLen = getInt(b, 7 + r, 1, Utils.MASK_8BITS);
                    byte[] appData = copyOfRange(b, r + 8, r + 8 + appDataLen);
                    MHEG5ApplicationType mheg5App = new MHEG5ApplicationType(at, bootPrio, appDataLen, appData);
                    mheg5ApplicationTypeList.add(mheg5App);
                    r = r + 4 + appDataLen;

                }
            }
            default -> selectorByte = copyOfRange(b, 4, descriptorLength + 2);
        }

	}






	public static String getUpdateTypeString(int updateType) {

		return switch (updateType) {
		case 0x00 -> "proprietary update solution";
		case 0x01 -> "standard update carousel (i.e. without notification table) via broadcast";
		case 0x02 -> "system software Update with Notification Table (UNT) via broadcast";
		case 0x03 -> "system software update using return channel with UNT";
		default -> "reserved for future use";
		};
	}


	@Override
	public String toString() {

		return super.toString() + "dataBroadcastId="+dataBroadcastId;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);

		t.add(new KVP("data_broadcast_id", dataBroadcastId, Utils.getDataBroadCastIDString(dataBroadcastId)));

        switch (dataBroadcastId) {
            case 0x0005 -> {
                t.add(new KVP("MAC_address_range", MAC_address_range, DataBroadcastDescriptor.getvalidMACaddressBytesString(MAC_address_range)));
                t.add(new KVP("MAC_IP_mapping_flag", MAC_IP_mapping_flag,
                        MAC_IP_mapping_flag == 1 ? "uses IP to MAC mapping as described in RFC 1112 and RFC 2464" : "mapping not defined"));
                t.add(new KVP("alignment_indicator", alignment_indicator,
                        alignment_indicator == 1 ? "alignment in bits: 32" : "alignment in bits: 8 (default)"));
                t.add(new KVP("max_sections_per_datagram", max_sections_per_datagram));
            }
            case 0x000a -> {
                addListJTree(t, ouiList, modus, "Systems Software Update");
                t.add(new KVP("private_data_byte", privateDataByte));
            }
            case 0x000b -> {
                addListJTree(t, platformList, modus, "IP/MAC platform");
                t.add(new KVP("private_data_byte", privateDataByte));
            }
            case 0x00f0, 0x00f1 ->  // MHP
                    addListJTree(t, applicationTypeList, modus, "application_type");
            case 0x0106 -> addListJTree(t, mheg5ApplicationTypeList, modus, "MHEG5 Applications");
            default -> t.add(new KVP("id_selector_bytes", selectorByte));
        }

		return t;
	}

	public boolean describesObjectCarousel() {
		return BROADCASTIDS_WITH_OBJECT_CAROUSEL.contains(dataBroadcastId);
	}

	public boolean describesSSU() {
		if(dataBroadcastId ==0xa){
			// SSU now see if there is a standard update carousel, or one with UNT
			for(OUIEntry entry:ouiList){
				if ((entry.updateType() == 0x01) || (entry.updateType() == 0x02)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public int getAlignment_indicator() {
		return alignment_indicator;
	}

	public List<ApplicationType> getApplicationTypeList() {
		return applicationTypeList;
	}

	public int getDataBroadcastId() {
		return dataBroadcastId;
	}

	public int getMAC_address_range() {
		return MAC_address_range;
	}

	public int getMAC_IP_mapping_flag() {
		return MAC_IP_mapping_flag;
	}

	public int getMax_sections_per_datagram() {
		return max_sections_per_datagram;
	}

	public int getOUI_data_length() {
		return OUI_data_length;
	}

	public List<OUIEntry> getOuiList() {
		return ouiList;
	}

	public int getPlatform_id_data_length() {
		return platform_id_data_length;
	}

	public List<Platform> getPlatformList() {
		return platformList;
	}

	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}

	public byte[] getSelectorByte() {
		return selectorByte;
	}

	public static String getMHEG5ApplicationTypeString(int appType) {

        return switch (appType) {
            case 0x00 -> "NULL_APPLICATION_TYPE";
            case 0x0101 -> "UK_PROFILE_LAUNCH";
            case 0x0505 -> "UK_PROFILE_BASELINE_1";
            default -> "unknown";
        };
	}


}
