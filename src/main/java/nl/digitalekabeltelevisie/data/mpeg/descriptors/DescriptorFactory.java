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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static java.lang.Byte.toUnsignedInt;

import java.util.*;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ciplus.CIProtectionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.GuidanceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.ServiceAttributeDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.nordig.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.opencable.EBPDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.scte.SCTEAdaptationFieldDataDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.upc.UPCLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.MessageDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.PreferencesManager;

public final class DescriptorFactory {

	/**
	 *
	 */
	private DescriptorFactory() {
		// static only
	}

	// TODO check which descriptors are allowed in which PSI tables
	private static final Logger	logger	= Logger.getLogger(DescriptorFactory.class.getName());


	/**
	 * @param data
	 * @param offset
	 * @param len
	 * @param tableSection
	 * @return List of Descriptor
	 */
	public static List<Descriptor> buildDescriptorList(final byte[] data, final int offset, final int len,
			final TableSection tableSection) {
		long private_data_specifier = PreferencesManager.getDefaultPrivateDataSpecifier();
		final List<Descriptor> r = new ArrayList<>();
		int t = 0;

		while (t < len) {

			Descriptor d = getDescriptor(data, offset + t, tableSection, private_data_specifier);

			t += d.getDescriptorLength() + 2;
			r.add(d);
			if (d instanceof PrivateDataSpecifierDescriptor) {
				final PrivateDataSpecifierDescriptor privateDescriptor = (PrivateDataSpecifierDescriptor) d;
				private_data_specifier = privateDescriptor.getPrivateDataSpecifier();
			}
			if (d instanceof PrivateDataIndicatorDescriptor) { // TODO check is this interchangeable with
				// PrivateDataSpecifierDescriptor?
				final PrivateDataIndicatorDescriptor privateDescriptor = (PrivateDataIndicatorDescriptor) d;
				private_data_specifier = privateDescriptor.getPrivateDataIndicator();
			}
		}

		return r;
	}

	/**
	 * @param data
	 * @param offset
	 * @param tableSection
	 * @param private_data_specifier
	 * @param localOffset
	 * @return
	 */
	static Descriptor getDescriptor(final byte[] data, final int offset, final TableSection tableSection,
			long private_data_specifier) {
		final int descriptorTag = toUnsignedInt(data[offset]);
		try {
			if(descriptorTag == 0xE9) {
				// OpenCable™ Specifications 
				// Encoder Boundary Point Specification 
				// OC-SP-EBP-I01-130118 
				// Should be user private descriptor, but no private_data_specifier
				// exists for OpenCable / SCTE
				// For now no conflict with other private descriptors
				// 
				// TODO Make this switchable (user preferences)
				//
				return new EBPDescriptor(data, offset, tableSection);
			}
			if (descriptorTag == 0x97) {
				// OpenCable™ Specifications 
				// Encoder Boundary Point Specification 
				// OC-SP-EBP-I01-130118 
				// Should be user private descriptor, but no private_data_specifier
				// exists for OpenCable / SCTE
				// For now no conflict with other private descriptors
				// 
				// TODO Make this switchable (user preferences)
				//
				
				return new SCTEAdaptationFieldDataDescriptor(data, offset, tableSection);
			}
			if (descriptorTag >= 0x80 && tableSection.getTableId() >= 0xBC && tableSection.getTableId() <= 0xBE
					&& PreferencesManager.isEnableM7Fastscan()) {
				return getM7Descriptor(data, offset, tableSection);
			}
			if (descriptorTag <= 0x3f) {
				if (tableSection.getTableId() == 0x4c) {
					return getINTDescriptor(data, offset, tableSection);
				}
				if (tableSection.getTableId() == 0x4b) {
					return getUNTDescriptor(data, offset, tableSection);
				}
				if (tableSection.getTableId() == 0x74) {
					return getAITDescriptor(data, offset, tableSection);
				}
				if (tableSection.getTableId() == 0xFC) {
					return getSCTE35Descriptor(data, offset, tableSection);
				}
				return getMPEGDescriptor(data, offset, tableSection);
				
			}
			if (descriptorTag <= 0x7f) {
				return getDVBSIDescriptor(data, offset, tableSection);
			}
			return  getPrivateDVBSIDescriptor(data, offset, tableSection, private_data_specifier);
			
		} catch (final RuntimeException iae) {
			// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
			// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
			Descriptor d = new Descriptor(data, offset, tableSection);
			logger.warning("Fall back for descriptor:" + toUnsignedInt(data[offset]) + " ("
					+ Descriptor.getDescriptorname(toUnsignedInt(data[offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString()+", RuntimeException:"+iae);
			return d;
		}
		
	}

	/**
	 * @param data
	 * @param offset
	 * @param tableSection
	 * @param localOffset
	 * @return
	 */
	static Descriptor getM7Descriptor(final byte[] data, final int offset, final TableSection tableSection) {
		int descriptorTag = toUnsignedInt(data[offset]);
		switch (descriptorTag) {
		case 0x83:
			return new M7LogicalChannelDescriptor(data, offset, tableSection);
		case 0x84:
			return new M7OperatorNameDescriptor(data, offset, tableSection);
		case 0x85:
			return new M7OperatorSublistNameDescriptor(data, offset, tableSection);
		case 0x86:
			return new M7OperatorPreferencesDescriptor(data, offset, tableSection);
		case 0x87:
			return new M7OperatorDiSEqCTDescriptor(data, offset, tableSection);
		case 0x88:
			return new M7OperatorOptionsDescriptor(data, offset, tableSection);
		case 0x89:
			return new M7NagraBrandIdDescriptor(data, offset, tableSection);
		default:
			Descriptor d = new M7Descriptor(data, offset, tableSection);
			logger.info("Not implemented M7Descriptor:" + descriptorTag + " ("
					+ M7Descriptor.getDescriptorname(descriptorTag) + ")in section "
					+ TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection + ",) data="
					+ d.getRawDataString());
			return d;
		}

	}

	/**
	 * @param data
	 * @param offset
	 * @param tableSection
	 * @param t
	 * @param private_data_specifier
	 * @return
	 */
	private static Descriptor getPrivateDVBSIDescriptor(final byte[] data, final int offset, final TableSection tableSection,
			final long private_data_specifier) {

		final int descriptor_tag =  toUnsignedInt(data[offset]);
		if (private_data_specifier == 0x600) { // UPC1
			switch (descriptor_tag) {
			case 0x81:
				return new UPCLogicalChannelDescriptor(data, offset, tableSection);
			case 0x87:
				return new ZiggoVodDeliveryDescriptor(data, offset, tableSection);
			}
		} else if (private_data_specifier == 0x16) { // Casema / Ziggo
			switch (descriptor_tag) {
			case 0x87:
				return new ZiggoVodDeliveryDescriptor(data, offset, tableSection);
			case 0x93:
				return new ZiggoVodURLDescriptor(data, offset, tableSection);
			case 0xD4:
				return new ZiggoPackageDescriptor(data, offset, tableSection);
			}
		} else if (private_data_specifier == 0x28) { // EACEM
			switch (descriptor_tag) {
			case 0x83:
				return new LogicalChannelDescriptor(data, offset, tableSection);
			case 0x86:
				return new EACEMStreamIdentifierDescriptor(data, offset, tableSection);
			case 0x88:
				return new HDSimulcastLogicalChannelDescriptor(data, offset, tableSection);
			}
		} else if (private_data_specifier == 0x29) { // Nordig
			switch (descriptor_tag) {
			case 0x83:
				return new NordigLogicalChannelDescriptorV1(data, offset, tableSection);
			case 0x87:
				return new NordigLogicalChannelDescriptorV2(data, offset, tableSection);
			}


		} else if (private_data_specifier == 0x40) { // CI Plus LLP
			switch (descriptor_tag) {
			case 0xCE:
				return new CIProtectionDescriptor(data, offset, tableSection);
			}
		} else if (private_data_specifier == 0x233a) { // DTG
			switch (descriptor_tag) {
			case 0x83: // can not re-use LogicalChannelDescriptor from EACEM, DTG has no visible flag
				return new nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.LogicalChannelDescriptor(data, offset, tableSection);
			case 0x86:
				return new ServiceAttributeDescriptor(data, offset, tableSection);
			case 0x89:
				return new GuidanceDescriptor(data, offset, tableSection);
			}
		}
		logger.info("Unimplemented private descriptor, private_data_specifier=" + private_data_specifier
					+ ", descriptortag=" + descriptor_tag + ", tableSection=" + tableSection);
		return new Descriptor(data, offset, tableSection);
	}

	private static Descriptor getMPEGDescriptor(final byte[] data, final int offset, final TableSection tableSection) {
		switch (toUnsignedInt(data[offset])) {
		case 0x02:
			return new VideoStreamDescriptor(data, offset, tableSection);
		case 0x03:
			return new AudioStreamDescriptor(data, offset, tableSection);
		case 0x04:
			return new HierarchyDescriptor(data, offset, tableSection);
		case 0x05:
			return new RegistrationDescriptor(data, offset, tableSection);
		case 0x06:
			return new DataStreamAlignmentDescriptor(data, offset, tableSection);
		case 0x07:
			return new TargetBackGroundDescriptor(data, offset, tableSection);
		case 0x08:
			return new VideoWindowDescriptor(data, offset, tableSection);
		case 0x09:
			return new CADescriptor(data, offset, tableSection);
		case 0x0A:
			return new ISO639LanguageDescriptor(data, offset, tableSection);
		case 0x0B:
			return new SystemClockDescriptor(data, offset, tableSection);
		case 0x0C:
			return new MultiplexBufferUtilizationDescriptor(data, offset, tableSection);
		case 0x0E:
			return new MaximumBitrateDescriptor(data, offset, tableSection);
		case 0x0F:
			return new PrivateDataIndicatorDescriptor(data, offset, tableSection);
		case 0x10:
			return new SmoothingBufferDescriptor(data, offset, tableSection);
		case 0x11:
			return new STDDescriptor(data, offset, tableSection);
			// 0x12 IBP_descriptor as found in iso/conformance/hhi.m2t
		case 0x12:
			return new IBPDescriptor(data, offset, tableSection);
		case 0x13:
			return new CarouselIdentifierDescriptor(data, offset, tableSection);
		case 0x14:
			return new AssociationTagDescriptor(data, offset, tableSection);
		case 0x1A:
			return new StreamEventDescriptor(data, offset, tableSection);
		case 0x1C:
			return new Mpeg4AudioDescriptor(data, offset, tableSection);
		case 0x25:
			return new MetaDataPointerDescriptor(data, offset, tableSection);
		case 0x26:
			return new MetaDataDescriptor(data, offset, tableSection);
		case 0x28:
			return new AVCVideoDescriptor(data, offset, tableSection);
		case 0x2A:
			return new AVCTimingAndHRDDescriptor(data, offset, tableSection);
		case 0x2B:
			return new AACMpeg2Descriptor(data, offset, tableSection);
		case 0x32:
			return new JPEG2000VideoDescriptor(data, offset, tableSection);
		case 0x38:
			return new HEVCVideoDescriptor(data, offset, tableSection);
		case 0x3F:
			return getMPEGExtendedDescriptor(data, offset, tableSection);
		default:
			final Descriptor descriptor = new Descriptor(data, offset, tableSection);
			logger.info("Not implemented descriptor:" + toUnsignedInt(data[offset]) + " ("
					+ Descriptor.getDescriptorname(toUnsignedInt(data[offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + descriptor.getRawDataString());

			return descriptor;
		}
	}
	
	private static MPEGExtensionDescriptor getMPEGExtendedDescriptor(final byte[] data, final int offset,
			final TableSection tableSection) {

		
		final int descriptor_tag_extension = toUnsignedInt(data[offset+2]);
		switch(descriptor_tag_extension){
		
		case 0x03:
			return new HEVCTimingAndHRDDescriptor(data, offset, tableSection);
		default:
			MPEGExtensionDescriptor d = new MPEGExtensionDescriptor(data, offset, tableSection);
			logger.warning("unimplemented MPEGExtensionDescriptor:" +
					d.getDescriptorTagString() +
					", TableSection:" + tableSection);
			return d;
		}

	}

	private static Descriptor getDVBSIDescriptor(final byte[] data, final int offset, final TableSection tableSection) {
		switch (toUnsignedInt(data[offset])) {

		case 0x40:
			return new NetworkNameDescriptor(data, offset, tableSection);
		case 0x41:
			return new ServiceListDescriptor(data, offset, tableSection);
		case 0x43:
			return new SatelliteDeliverySystemDescriptor(data, offset, tableSection);
		case 0x44:
			return new CableDeliverySystemDescriptor(data, offset, tableSection);
		case 0x45:
			return new VBIDataDescriptor(data, offset, tableSection);
		case 0x46: // semantics for the VBI teletext descriptor is the same as defined for the teletext descriptor
			return new TeletextDescriptor(data, offset, tableSection);
		case 0x47:
			return new BouquetNameDescriptor(data, offset, tableSection);
		case 0x48:
			return new ServiceDescriptor(data, offset, tableSection);
		case 0x49:
			return new CountryAvailabilityDescriptor(data, offset, tableSection);
		case 0x4A:
			return new LinkageDescriptor(data, offset, tableSection);
		case 0x4B:
			return new NVODReferenceDescriptor(data, offset, tableSection);
		case 0x4C:
			return new TimeShiftedServiceDescriptor(data, offset, tableSection);
		case 0x4D:
			return new ShortEventDescriptor(data, offset, tableSection);
		case 0x4E:
			return new ExtendedEventDescriptor(data, offset, tableSection);
		case 0x4F:
			return new TimeShiftedEventDescriptor(data, offset, tableSection);
		case 0x50:
			return new ComponentDescriptor(data, offset, tableSection);
		case 0x51:
			return new MosaicDescriptor(data, offset, tableSection);
		case 0x52:
			return new StreamIdentifierDescriptor(data, offset, tableSection);
		case 0x53:
			return new CAIdentifierDescriptor(data, offset, tableSection);
		case 0x54:
			return new ContentDescriptor(data, offset, tableSection);
		case 0x55:
			return new ParentalRatingDescriptor(data, offset, tableSection);
		case 0x56:
			return new TeletextDescriptor(data, offset, tableSection);
		case 0x58:
			return new LocalTimeOffsetDescriptor(data, offset, tableSection);
		case 0x59:
			return new SubtitlingDescriptor(data, offset, tableSection);
		case 0x5A:
			return new TerrestrialDeliverySystemDescriptor(data, offset, tableSection);
		case 0x5B:
			return new MultilingualNetworkNameDescriptor(data, offset, tableSection);
		case 0x5C:
			return new MultilingualBouquetNameDescriptor(data, offset, tableSection);
		case 0x5D:
			return new MultilingualServiceNameDescriptor(data, offset, tableSection);
		case 0x5F:
			return new PrivateDataSpecifierDescriptor(data, offset, tableSection);
		case 0x62:
			return new FrequencyListDescriptor(data, offset, tableSection);
		case 0x63:
			return new PartialTransportStreamDescriptor(data, offset, tableSection);
		case 0x64:
			return new DataBroadcastDescriptor(data, offset, tableSection);
		case 0x65:
			return new ScramblingDescriptor(data, offset, tableSection);
		case 0x66:
			return new DataBroadcastIDDescriptor(data, offset, tableSection);
		case 0x69:
			return new PDCDescriptor(data, offset, tableSection);
		case 0x6A:
			return new AC3Descriptor(data, offset, tableSection);
		case 0x6B:
			return new AncillaryDataDescriptor(data, offset, tableSection);
		case 0x6C:
			return new CellListDescriptor(data, offset, tableSection);
		case 0x6D:
			return new CellFrequencyLinkDescriptor(data, offset, tableSection);
		case 0x6F:
			return new ApplicationSignallingDescriptor(data, offset, tableSection);
		case 0x70:
			return new AdaptationFieldDataDescriptor(data, offset, tableSection);
		case 0x71:
			return new ServiceIdentifierDescriptor(data, offset, tableSection);
		case 0x72:
			return new ServiceAvailabilityDescriptor(data, offset, tableSection);
		case 0x73:
			return new DefaultAuthorityDescriptor(data, offset, tableSection);
		case 0x74:
			return new RelatedContentDescriptor(data, offset, tableSection);
		case 0x76:
			return new ContentIdentifierDescriptor(data, offset, tableSection);
		case 0x77:
			return new TimeSliceFecIdentifierDescriptor(data, offset, tableSection);
		case 0x79:
			return new S2SatelliteDeliverySystemDescriptor(data, offset, tableSection);
		case 0x7A:
			return new EnhancedAC3Descriptor(data, offset, tableSection);
		case 0x7C:
			return new AACDescriptor(data, offset, tableSection);
		case 0x7E:
			return new FTAContentManagmentDescriptor(data, offset, tableSection);
		case 0x7F:
			return getDVBExtendedDescriptor(data, offset, tableSection);

		default:
			Descriptor d =  new Descriptor(data, offset, tableSection);
			logger.info("Not implemented descriptor:" + toUnsignedInt(data[offset]) + " ("
					+ Descriptor.getDescriptorname(toUnsignedInt(data[offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
		}
	}

	/**
	 * @param data
	 * @param offset
	 * @param tableSection
	 * @param t
	 * @return
	 */
	private static DVBExtensionDescriptor getDVBExtendedDescriptor(final byte[] data, final int offset,
			final TableSection tableSection) {

		final int descriptor_tag_extension = toUnsignedInt(data[offset+2]);
		switch(descriptor_tag_extension){

		case 0x04:
			return new T2DeliverySystemDescriptor(data, offset, tableSection);
		case 0x05:
			return new SHDeliverySystemDescriptor(data, offset, tableSection);
		case 0x06:
			return new SupplementaryAudioDescriptor(data, offset, tableSection);
		case 0x07:
			return new NetworkChangeNotifyDescriptor(data, offset, tableSection);
		case 0x08:
			return new nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.MessageDescriptor(data, offset, tableSection);
		case 0x09:
			return new TargetRegionDescriptor(data, offset, tableSection);
		case 0x0A:
			return new TargetRegionNameDescriptor(data, offset, tableSection);
		case 0x0B:
			return new ServiceRelocatedDescriptor(data, offset, tableSection);
		case 0x11:
			return new T2MIDescriptor(data, offset, tableSection);
		case 0x13:
			return new URILinkageDescriptor(data, offset, tableSection);
		case 0x14:
			return new CIAncillaryDataDescriptor(data, offset, tableSection);
		case 0x15:
			return new AC4Descriptor(data, offset, tableSection);
		case 0x17:
			return new S2XSatelliteDeliverySystemDescriptor(data, offset, tableSection);
		case 0x19:
			return new AudioPreselectionDescriptor(data, offset, tableSection);
		case 0x20:
			return new TtmlSubtitlingDescriptor(data, offset, tableSection);

		default:
			DVBExtensionDescriptor d = new DVBExtensionDescriptor(data, offset, tableSection);
			logger.warning("unimplemented DVBExtensionDescriptor:" +
					d.getDescriptorTagString() +
					", TableSection:" + tableSection);
			return d;
		}

	}

	private static Descriptor getINTDescriptor(final byte[] data, final int offset, final TableSection tableSection) {

		switch (toUnsignedInt(data[offset])) {
		case 0x0C:
			return new IPMACPlatformNameDescriptor(data, offset, tableSection);
		case 0x0D:
			return new IPMACPlatformProviderNameDescriptor(data, offset, tableSection);
		case 0x0F:
			return new TargetIPSlashDescriptor(data, offset, tableSection);
		case 0x13:
			return new IPMACStreamLocationDescriptor(data, offset, tableSection);
		default:
			Descriptor d = new INTDescriptor(data, offset, tableSection);
			logger.info("Not implemented IntDescriptor:" + toUnsignedInt(data[offset]) + " ("
					+ INTDescriptor.getDescriptorname(toUnsignedInt(data[offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
		}
		
	}

	private static Descriptor getUNTDescriptor(final byte[] data, final int offset, final TableSection tableSection) {
		switch (toUnsignedInt(data[offset])) {
		case 0x01:
			return new SchedulingDescriptor(data, offset, tableSection);
		case 0x02:
			return new UpdateDescriptor(data, offset, tableSection);
		case 0x03:
			return new SSULocationDescriptor(data, offset, tableSection);
		case 0x04:
			return new MessageDescriptor(data, offset, tableSection);
		case 0x05:
			return new SSUEventNameDescriptor(data, offset, tableSection);
		case 0x06:
			return new TargetSmartcardDescriptor(data, offset, tableSection);
		case 0x0B:
			return new SSUSubgroupAssociationDescriptor(data, offset, tableSection);
		default:
			Descriptor d = new UNTDescriptor(data, offset, tableSection);
			logger.info("Not implemented UNTDescriptor:" + toUnsignedInt(data[offset]) + " ("
					+ UNTDescriptor.getDescriptorname(toUnsignedInt(data[offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString() + ", pid:"+tableSection.getParentPID().getPid());
			return d;
		}
	}

	private static Descriptor getAITDescriptor(final byte[] data, final int offset, final TableSection tableSection) {
		switch (toUnsignedInt(data[offset])) {
		case 0x00:
			return new ApplicationDescriptor(data, offset, tableSection);
		case 0x01:
			return new ApplicationNameDescriptor(data, offset, tableSection);
		case 0x02:
			return new TransportProtocolDescriptor(data, offset, tableSection);
		case 0x03:
			return new DVBJApplicationDescriptor(data, offset, tableSection);
		case 0x04:
			return new DVBJApplicationLocationDescriptor(data, offset, tableSection);
		case 0x05:
			return new ExternalApplicationAuthorizationDescriptor(data, offset, tableSection);
		case 0x15:
			return new SimpleApplicationLocationDescriptor(data, offset, tableSection);
		case 0x16:
			return new ApplicationUsageDescriptor(data, offset, tableSection);
		case 0x17:
			return new SimpleApplicationBoundaryDescriptor(data, offset, tableSection);
		default:
			Descriptor d = new AITDescriptor(data, offset, tableSection);
			logger.info("Not implemented AITDescriptor:" + toUnsignedInt(data[offset]) + " ("
					+ AITDescriptor.getDescriptorname(toUnsignedInt(data[offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
			}
	}

	private static Descriptor getSCTE35Descriptor(final byte[] data, final int offset, final TableSection tableSection) {

		switch (toUnsignedInt(data[offset])) {
		case 0x00:
			return new AvailDescriptor(data, offset, tableSection);
		case 0x02:
			return new SegmentationDescriptor(data, offset, tableSection);
		default:
			Descriptor d = new SCTE35Descriptor(data, offset, tableSection);
			logger.info("Not implemented SCTE35Descriptor:" + toUnsignedInt(data[offset]) + " ("
					+ SCTE35Descriptor.getDescriptorname(toUnsignedInt(data[offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
		}
	}

}
