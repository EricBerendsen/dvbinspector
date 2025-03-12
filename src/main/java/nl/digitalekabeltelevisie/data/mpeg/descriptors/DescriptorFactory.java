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
 * Change log:
 * - Feb 8th 2022: Handle JPEG-XS video descriptor
 */

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static java.lang.Byte.toUnsignedInt;

import java.util.*;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.avs.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosBatSelectionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosTimezoneDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosInformationParametersDescriptor;
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
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.uwa.CUVVVideoStreamDescriptor;
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


	public static List<Descriptor> buildDescriptorList(final byte[] data, final int offset, final int len,
			final TableSection tableSection) {
		
		DescriptorContext descriptorContext = new DescriptorContext();
		return buildDescriptorList(data, offset, len, tableSection, descriptorContext);
	
	}

	public static List<Descriptor> buildDescriptorList(final byte[] data, final int offset, final int len,
			final TableSection tableSection, DescriptorContext descriptorContext) {
		descriptorContext.setPrivate_data_specifier(PreferencesManager.getDefaultPrivateDataSpecifier());
		final List<Descriptor> r = new ArrayList<>();
		int t = 0;

		while (t < len) {
			
			// make a copy of the just the bytes for the descriptor. 
			// If the descriptor constructor reads further then descriptorLen it will cause a ArrayIndexOutOfBoundsException, 
			// which will result in fall back to a standard Descriptor.
			// Reasoning: better not to interpret the data, than to show it wrong without warning. 
			//
			// see https://github.com/EricBerendsen/dvbinspector/issues/22
			
			int descriptorLen = toUnsignedInt(data[offset + t+ 1]);
			byte[] descriptorData = Arrays.copyOfRange(data, offset + t, offset + t + descriptorLen + 2);

			Descriptor d = getDescriptor(descriptorData, tableSection, descriptorContext);

			t += d.getDescriptorLength() + 2;
			r.add(d);
			if (d instanceof final PrivateDataSpecifierDescriptor privateDescriptor) {
				descriptorContext.setPrivate_data_specifier(privateDescriptor.getPrivateDataSpecifier());
			}
		}

		return r;
	}

	/**
	 * @param localOffset
	 * @param data
	 * @param tableSection
	 * @param descriptorContext
	 * @return
	 */
	private static Descriptor getDescriptor(final byte[] data, final TableSection tableSection,
											DescriptorContext descriptorContext) {
		final int descriptorTag = toUnsignedInt(data[0]);
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
				return new EBPDescriptor(data, 0, tableSection);
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
				
				return new SCTEAdaptationFieldDataDescriptor(data, 0, tableSection);
			}
			if (descriptorTag == 0xF3 && data[2] == 'c' && data[3] == 'u' && data[4] == 'v' && data[5] == 'v' ) {
				// the CUVV video stream descriptor include 'cuvv' as a watermark
				return new CUVVVideoStreamDescriptor(data, 0, tableSection); 
			}
			if (descriptorTag >= 0x80 && tableSection.getTableId() >= 0xBC && tableSection.getTableId() <= 0xBE
					&& PreferencesManager.isEnableM7Fastscan()) {
				return getM7Descriptor(data, tableSection);
			}
			if (descriptorTag <= 0x3f) {
				if (tableSection.getTableId() == 0x4c) {
					return getINTDescriptor(data, tableSection);
				}
				if (tableSection.getTableId() == 0x4b) {
					return getUNTDescriptor(data, tableSection);
				}
				if (tableSection.getTableId() == 0x74) {
					return getAITDescriptor(data, tableSection);
				}
				if (tableSection.getTableId() == 0xFC) {
					return getSCTE35Descriptor(data, tableSection);
				}
				return getMPEGDescriptor(data, tableSection);
				
			}
			if (descriptorTag <= 0x7f) {
				return getDVBSIDescriptor(data, tableSection, descriptorContext);
			}
			return  getPrivateDVBSIDescriptor(data, tableSection, descriptorContext);
			
		} catch (final RuntimeException iae) {
			// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
			// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
			Descriptor d = new Descriptor(data, 0, tableSection);
			logger.warning("Fall back for descriptor:" + toUnsignedInt(data[0]) + " ("
					+ Descriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString()+", RuntimeException:"+iae);
			return d;
		}
		
	}

	/**
	 * @param localOffset
	 * @param data
	 * @param tableSection
	 * @return
	 */
	private static Descriptor getM7Descriptor(final byte[] data, final TableSection tableSection) {
		int descriptorTag = toUnsignedInt(data[0]);
		switch (descriptorTag) {
		case 0x83:
			return new M7LogicalChannelDescriptor(data, 0, tableSection);
		case 0x84:
			return new M7OperatorNameDescriptor(data, 0, tableSection);
		case 0x85:
			return new M7OperatorSublistNameDescriptor(data, 0, tableSection);
		case 0x86:
			return new M7OperatorPreferencesDescriptor(data, 0, tableSection);
		case 0x87:
			return new M7OperatorDiSEqCTDescriptor(data, 0, tableSection);
		case 0x88:
			return new M7OperatorOptionsDescriptor(data, 0, tableSection);
		case 0x89:
			return new M7NagraBrandIdDescriptor(data, 0, tableSection);
		default:
			Descriptor d = new M7Descriptor(data, 0, tableSection);
			logger.info("Not implemented M7Descriptor:" + descriptorTag + " ("
					+ M7Descriptor.getDescriptorname(descriptorTag) + ")in section "
					+ TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection + ",) data="
					+ d.getRawDataString());
			return d;
		}

	}

	/**
	 * @param t
	 * @param data
	 * @param tableSection
	 * @param DescriptorContext
	 * @return
	 */
	private static Descriptor getPrivateDVBSIDescriptor(final byte[] data, final TableSection tableSection,
														final DescriptorContext descriptorContext) {

		final long private_data_specifier = descriptorContext.getPrivate_data_specifier();
		final int descriptor_tag =  toUnsignedInt(data[0]);
		if (private_data_specifier == 0x600) { // UPC1
			switch (descriptor_tag) {
			case 0x81:
				return new UPCLogicalChannelDescriptor(data, 0, tableSection);
			case 0x87:
				return new ZiggoVodDeliveryDescriptor(data, 0, tableSection);
			}
		} else if (private_data_specifier == 0x16) { // Casema / Ziggo
			switch (descriptor_tag) {
			case 0x87:
				return new ZiggoVodDeliveryDescriptor(data, 0, tableSection);
			case 0x93:
				return new ZiggoVodURLDescriptor(data, 0, tableSection);
			case 0xD4:
				return new ZiggoPackageDescriptor(data, 0, tableSection);
			}
		} else if (private_data_specifier == 0x28) { // EACEM
			switch (descriptor_tag) {
			case 0x83:
				return new LogicalChannelDescriptor(data, 0, tableSection, descriptorContext);
			case 0x86:
				return new EACEMStreamIdentifierDescriptor(data, 0, tableSection);
			case 0x88:
				return new HDSimulcastLogicalChannelDescriptor(data, 0, tableSection, descriptorContext);
			}
		} else if (private_data_specifier == 0x29) { // Nordig
			switch (descriptor_tag) {
			case 0x83:
				return new NordigLogicalChannelDescriptorV1(data, 0, tableSection, descriptorContext);
			case 0x87:
				return new NordigLogicalChannelDescriptorV2(data, tableSection, descriptorContext);
			}


		} else if (private_data_specifier == 0x40) { // CI Plus LLP
			switch (descriptor_tag) {
			case 0xCE:
				return new CIProtectionDescriptor(data, 0, tableSection);
			}
		} else if (private_data_specifier == 0xa4) { // Canal + International
			switch (descriptor_tag) {
			case 0x80:
				return new CosBatSelectionDescriptor (data, tableSection);
			case 0x81:
				return new CosInformationParametersDescriptor(data, 0, tableSection);
			case 0x83:
				return new CosLogicalChannelDescriptor(data, 0, tableSection, descriptorContext);
			case 0x88:
				return new CosTimezoneDescriptor(data, 0, tableSection);
			}
		} else if (private_data_specifier == 0x233a) { // DTG
			switch (descriptor_tag) {
			case 0x83: // can not re-use LogicalChannelDescriptor from EACEM, DTG has no visible flag
				return new nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.LogicalChannelDescriptor(data, 0, tableSection, descriptorContext);
			case 0x86:
				return new ServiceAttributeDescriptor(data, 0, tableSection);
			case 0x89:
				return new GuidanceDescriptor(data, tableSection);
			}
		} else if (private_data_specifier >= 0x00003200 && private_data_specifier <= 0x0000320F ) { // FREE TV AUSTRALIA OPERATIONAL PRACTICE OP-40
			switch (descriptor_tag) {
			case 0x83:
				return new nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.au.LogicalChannelDescriptor(data, 0, tableSection, descriptorContext);
			}

		} else if (private_data_specifier == 0x41565356) { // AVS Video
			switch (descriptor_tag) {
				case 0xD1:
					return new AVS3VideoDescriptor(data, 0, tableSection);
			}
		}
		else if (private_data_specifier == 0x41565341) { // AVS Audio
			switch (descriptor_tag) {
				case 0xD2:
					return new AVS3AudioDescriptor(data, 0, tableSection);
			}
		}
		logger.info("Unimplemented private descriptor, private_data_specifier=" + private_data_specifier
					+ ", descriptortag=" + descriptor_tag + ", tableSection=" + tableSection);
		return new Descriptor(data, 0, tableSection);
	}

	private static Descriptor getMPEGDescriptor(final byte[] data, final TableSection tableSection) {
		switch (toUnsignedInt(data[0])) {
		case 0x02:
			return new VideoStreamDescriptor(data, 0, tableSection);
		case 0x03:
			return new AudioStreamDescriptor(data, 0, tableSection);
		case 0x04:
			return new HierarchyDescriptor(data, 0, tableSection);
		case 0x05:
			return new RegistrationDescriptor(data, 0, tableSection);
		case 0x06:
			return new DataStreamAlignmentDescriptor(data, 0, tableSection);
		case 0x07:
			return new TargetBackGroundDescriptor(data, 0, tableSection);
		case 0x08:
			return new VideoWindowDescriptor(data, 0, tableSection);
		case 0x09:
			return new CADescriptor(data, 0, tableSection);
		case 0x0A:
			return new ISO639LanguageDescriptor(data, 0, tableSection);
		case 0x0B:
			return new SystemClockDescriptor(data, 0, tableSection);
		case 0x0C:
			return new MultiplexBufferUtilizationDescriptor(data, 0, tableSection);
		case 0x0D:
			return new CopyrightDescriptor(data, 0, tableSection);
		case 0x0E:
			return new MaximumBitrateDescriptor(data, 0, tableSection);
		case 0x0F:
			return new PrivateDataIndicatorDescriptor(data, 0, tableSection);
		case 0x10:
			return new SmoothingBufferDescriptor(data, 0, tableSection);
		case 0x11:
			return new STDDescriptor(data, 0, tableSection);
			// 0x12 IBP_descriptor as found in iso/conformance/hhi.m2t
		case 0x12:
			return new IBPDescriptor(data, 0, tableSection);
		case 0x13:
			return new CarouselIdentifierDescriptor(data, tableSection);
		case 0x14:
			return new AssociationTagDescriptor(data, 0, tableSection);
		case 0x1A:
			return new StreamEventDescriptor(data, 0, tableSection);
		case 0x1C:
			return new Mpeg4AudioDescriptor(data, 0, tableSection);
		case 0x25:
			return new MetaDataPointerDescriptor(data, 0, tableSection);
		case 0x26:
			return new MetaDataDescriptor(data, 0, tableSection);
		case 0x28:
			return new AVCVideoDescriptor(data, 0, tableSection);
		case 0x2A:
			return new AVCTimingAndHRDDescriptor(data, 0, tableSection);
		case 0x2B:
			return new AACMpeg2Descriptor(data, 0, tableSection);
		case 0x32:
			return new JPEG2000VideoDescriptor(data, 0, tableSection);
		case 0x38:
			return new HEVCVideoDescriptor(data, 0, tableSection);
		case 0x39:
			return new VVCVideoDescriptor(data, 0, tableSection);
		case 0x3F:
			return getMPEGExtendedDescriptor(data, tableSection);
		default:
			final Descriptor descriptor = new Descriptor(data, 0, tableSection);
			logger.info("Not implemented descriptor:" + toUnsignedInt(data[0]) + " ("
					+ Descriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + descriptor.getRawDataString());

			return descriptor;
		}
	}
	
	private static MPEGExtensionDescriptor getMPEGExtendedDescriptor(final byte[] data,
																	 final TableSection tableSection) {

		
		final int descriptor_tag_extension = toUnsignedInt(data[2]);
		switch(descriptor_tag_extension){
		
		case 0x03:
			return new HEVCTimingAndHRDDescriptor(data, tableSection);
		case 0x14:
			return new JpegXsVideoDescriptor(data, tableSection);
		default:
			MPEGExtensionDescriptor d = new MPEGExtensionDescriptor(data, tableSection);
			logger.warning("unimplemented MPEGExtensionDescriptor:" +
					d.getDescriptorTagString() +
					", TableSection:" + tableSection);
			return d;
		}

	}

	private static Descriptor getDVBSIDescriptor(final byte[] data, final TableSection tableSection, DescriptorContext descriptorContext) {
		switch (toUnsignedInt(data[0])) {

		case 0x40:
			return new NetworkNameDescriptor(data, 0, tableSection);
		case 0x41:
			return new ServiceListDescriptor(data, 0, tableSection, descriptorContext);
		case 0x43:
			return new SatelliteDeliverySystemDescriptor(data, 0, tableSection);
		case 0x44:
			return new CableDeliverySystemDescriptor(data, 0, tableSection);
		case 0x45:
			return new VBIDataDescriptor(data, 0, tableSection);
		case 0x46: // semantics for the VBI teletext descriptor is the same as defined for the teletext descriptor
			return new TeletextDescriptor(data, 0, tableSection);
		case 0x47:
			return new BouquetNameDescriptor(data, tableSection);
		case 0x48:
			return new ServiceDescriptor(data, tableSection);
		case 0x49:
			return new CountryAvailabilityDescriptor(data, 0, tableSection);
		case 0x4A:
			return new LinkageDescriptor(data, tableSection);
		case 0x4B:
			return new NVODReferenceDescriptor(data, 0, tableSection);
		case 0x4C:
			return new TimeShiftedServiceDescriptor(data, 0, tableSection);
		case 0x4D:
			return new ShortEventDescriptor(data, tableSection);
		case 0x4E:
			return new ExtendedEventDescriptor(data, tableSection);
		case 0x4F:
			return new TimeShiftedEventDescriptor(data, 0, tableSection);
		case 0x50:
			return new ComponentDescriptor(data, tableSection);
		case 0x51:
			return new MosaicDescriptor(data, 0, tableSection);
		case 0x52:
			return new StreamIdentifierDescriptor(data, 0, tableSection);
		case 0x53:
			return new CAIdentifierDescriptor(data, 0, tableSection);
		case 0x54:
			return new ContentDescriptor(data, tableSection);
		case 0x55:
			return new ParentalRatingDescriptor(data, 0, tableSection);
		case 0x56:
			return new TeletextDescriptor(data, 0, tableSection);
		case 0x58:
			return new LocalTimeOffsetDescriptor(data, 0, tableSection);
		case 0x59:
			return new SubtitlingDescriptor(data, 0, tableSection);
		case 0x5A:
			return new TerrestrialDeliverySystemDescriptor(data, 0, tableSection);
		case 0x5B:
			return new MultilingualNetworkNameDescriptor(data, tableSection);
		case 0x5C:
			return new MultilingualBouquetNameDescriptor(data, tableSection);
		case 0x5D:
			return new MultilingualServiceNameDescriptor(data, tableSection);
		case 0x5F:
			return new PrivateDataSpecifierDescriptor(data, 0, tableSection);
		case 0x62:
			return new FrequencyListDescriptor(data, 0, tableSection);
		case 0x63:
			return new PartialTransportStreamDescriptor(data, 0, tableSection);
		case 0x64:
			return new DataBroadcastDescriptor(data, 0, tableSection);
		case 0x65:
			return new ScramblingDescriptor(data, 0, tableSection);
		case 0x66:
			return new DataBroadcastIDDescriptor(data, 0, tableSection);
		case 0x69:
			return new PDCDescriptor(data, 0, tableSection);
		case 0x6A:
			return new AC3Descriptor(data, 0, tableSection);
		case 0x6B:
			return new AncillaryDataDescriptor(data, 0, tableSection);
		case 0x6C:
			return new CellListDescriptor(data, 0, tableSection);
		case 0x6D:
			return new CellFrequencyLinkDescriptor(data, 0, tableSection);
		case 0x6F:
			return new ApplicationSignallingDescriptor(data, 0, tableSection);
		case 0x70:
			return new AdaptationFieldDataDescriptor(data, 0, tableSection);
		case 0x71:
			return new ServiceIdentifierDescriptor(data, tableSection);
		case 0x72:
			return new ServiceAvailabilityDescriptor(data, 0, tableSection);
		case 0x73:
			return new DefaultAuthorityDescriptor(data, 0, tableSection);
		case 0x74:
			return new RelatedContentDescriptor(data, 0, tableSection);
		case 0x76:
			return new ContentIdentifierDescriptor(data, 0, tableSection);
		case 0x77:
			return new TimeSliceFecIdentifierDescriptor(data, 0, tableSection);
		case 0x79:
			return new S2SatelliteDeliverySystemDescriptor(data, 0, tableSection);
		case 0x7A:
			return new EnhancedAC3Descriptor(data, 0, tableSection);
		case 0x7C:
			return new AACDescriptor(data, tableSection);
		case 0x7E:
			return new FTAContentManagmentDescriptor(data, 0, tableSection);
		case 0x7F:
			return getDVBExtendedDescriptor(data, tableSection);

		default:
			Descriptor d =  new Descriptor(data, 0, tableSection);
			logger.info("Not implemented descriptor:" + toUnsignedInt(data[0]) + " ("
					+ Descriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
		}
	}

	/**
	 * @param t
	 * @param data
	 * @param tableSection
	 * @return
	 */
	private static DVBExtensionDescriptor getDVBExtendedDescriptor(final byte[] data,
																   final TableSection tableSection) {

		final int descriptor_tag_extension = toUnsignedInt(data[2]);
		switch(descriptor_tag_extension){

		case 0x04:
			return new T2DeliverySystemDescriptor(data, 0, tableSection);
		case 0x05:
			return new SHDeliverySystemDescriptor(data, 0, tableSection);
		case 0x06:
			return new SupplementaryAudioDescriptor(data, 0, tableSection);
		case 0x07:
			return new NetworkChangeNotifyDescriptor(data, 0, tableSection);
		case 0x08:
			return new nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.MessageDescriptor(data, 0, tableSection);
		case 0x09:
			return new TargetRegionDescriptor(data, 0, tableSection);
		case 0x0A:
			return new TargetRegionNameDescriptor(data, 0, tableSection);
		case 0x0B:
			return new ServiceRelocatedDescriptor(data, 0, tableSection);
		case 0x11:
			return new T2MIDescriptor(data, 0, tableSection);
		case 0x13:
			return new URILinkageDescriptor(data, 0, tableSection);
		case 0x14:
			return new CIAncillaryDataDescriptor(data, tableSection);
		case 0x15:
			return new AC4Descriptor(data, 0, tableSection);
		case 0x17:
			return new S2XSatelliteDeliverySystemDescriptor(data, 0, tableSection);
		case 0x19:
			return new AudioPreselectionDescriptor(data, 0, tableSection);
		case 0x20:
			return new TtmlSubtitlingDescriptor(data, tableSection);
		case 0x22:
			return new ServiceProminenceDescriptor(data, 0, tableSection);
		case 0x23:
			return new VvcSubpicturesDescriptor(data, tableSection);

		default:
			DVBExtensionDescriptor d = new DVBExtensionDescriptor(data, 0, tableSection);
			logger.warning("unimplemented DVBExtensionDescriptor:" +
					d.getDescriptorTagString() +
					", TableSection:" + tableSection);
			return d;
		}

	}
	
	// TODO combine with getUNTDescriptor, see EN 301 192 V1.6.1  8.4.5.1 Descriptor identification and location
	// Note that descriptor tags from 0x00 to 0x3F share a common descriptor name space with UNT descriptors
	// (see ETSI TS 102 006 [18]).

	private static Descriptor getINTDescriptor(final byte[] data, final TableSection tableSection) {

		switch (toUnsignedInt(data[0])) {
		case 0x0C:
			return new IPMACPlatformNameDescriptor(data, tableSection);
		case 0x0D:
			return new IPMACPlatformProviderNameDescriptor(data, tableSection);
		case 0x0F:
			return new TargetIPSlashDescriptor(data, tableSection);
		case 0x13:
			return new IPMACStreamLocationDescriptor(data, tableSection);
		default:
			Descriptor d = new INTDescriptor(data, tableSection);
			logger.info("Not implemented IntDescriptor:" + toUnsignedInt(data[0]) + " ("
					+ INTDescriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection) + ")in section "
					+ TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection + ",) data=" + d.getRawDataString());
			return d;
		}

	}

	private static Descriptor getUNTDescriptor(final byte[] data, final TableSection tableSection) {
		switch (toUnsignedInt(data[0])) {
		case 0x01:
			return new SchedulingDescriptor(data, 0, tableSection);
		case 0x02:
			return new UpdateDescriptor(data, 0, tableSection);
		case 0x03:
			return new SSULocationDescriptor(data, 0, tableSection);
		case 0x04:
			return new MessageDescriptor(data, tableSection);
		case 0x05:
			return new SSUEventNameDescriptor(data, 0, tableSection);
		case 0x06:
			return new TargetSmartcardDescriptor(data, 0, tableSection);
		case 0x0B:
			return new SSUSubgroupAssociationDescriptor(data, 0, tableSection);
		default:
			Descriptor d = new UNTDescriptor(data, 0, tableSection);
			logger.info("Not implemented UNTDescriptor:" + toUnsignedInt(data[0]) + " ("
					+ UNTDescriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString() + ", pid:"+tableSection.getParentPID().getPid());
			return d;
		}
	}

	private static Descriptor getAITDescriptor(final byte[] data, final TableSection tableSection) {
		switch (toUnsignedInt(data[0])) {
		case 0x00:
			return new ApplicationDescriptor(data, 0, tableSection);
		case 0x01:
			return new ApplicationNameDescriptor(data, tableSection);
		case 0x02:
			return new TransportProtocolDescriptor(data, 0, tableSection);
		case 0x03:
			return new DVBJApplicationDescriptor(data, 0, tableSection);
		case 0x04:
			return new DVBJApplicationLocationDescriptor(data, 0, tableSection);
		case 0x05:
			return new ExternalApplicationAuthorizationDescriptor(data, 0, tableSection);
		case 0x15:
			return new SimpleApplicationLocationDescriptor(data, 0, tableSection);
		case 0x16:
			return new ApplicationUsageDescriptor(data, 0, tableSection);
		case 0x17:
			return new SimpleApplicationBoundaryDescriptor(data, 0, tableSection);
		default:
			Descriptor d = new AITDescriptor(data, 0, tableSection);
			logger.info("Not implemented AITDescriptor:" + toUnsignedInt(data[0]) + " ("
					+ AITDescriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
			}
	}

	private static Descriptor getSCTE35Descriptor(final byte[] data, final TableSection tableSection) {

		switch (toUnsignedInt(data[0])) {
		case 0x00:
			return new AvailDescriptor(data, 0, tableSection);
		case 0x02:
			return new SegmentationDescriptor(data, 0, tableSection);
		default:
			Descriptor d = new SCTE35Descriptor(data, 0, tableSection);
			logger.info("Not implemented SCTE35Descriptor:" + toUnsignedInt(data[0]) + " ("
					+ SCTE35Descriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
		}
	}

}
