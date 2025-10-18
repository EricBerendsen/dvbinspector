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
 * Change log:
 * - Feb 8th 2022: Handle JPEG-XS video descriptor
 */

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.HEVCTimingAndHRDDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.JpegXsVideoDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.MPEGExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.avs.AVS3AudioDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.avs.AVS3VideoDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosBatSelectionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosInformationParametersDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international.CosTimezoneDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema.ZiggoPackageDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema.ZiggoVodDeliveryDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema.ZiggoVodURLDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ciplus.CIProtectionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.GuidanceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.ServiceAttributeDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.EACEMStreamIdentifierDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.HDSimulcastLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.LogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.nordig.NordigLogicalChannelDescriptorV1;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.nordig.NordigLogicalChannelDescriptorV2;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.opencable.EBPDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.scte.SCTEAdaptationFieldDataDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses.BouquetListDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses.ServiceListNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses.VirtualServiceIDDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.upc.UPCLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.uwa.CUVVVideoStreamDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35.AvailDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35.SCTE35Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35.SegmentationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35.TimeDescriptor;
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
				return new EBPDescriptor(data, tableSection);
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
				
				return new SCTEAdaptationFieldDataDescriptor(data, tableSection);
			}
			if (descriptorTag == 0xF3 && data[2] == 'c' && data[3] == 'u' && data[4] == 'v' && data[5] == 'v' ) {
				// the CUVV video stream descriptor include 'cuvv' as a watermark
				return new CUVVVideoStreamDescriptor(data, tableSection); 
			}
			if (descriptorTag >= 0x80 && tableSection.getTableId() >= 0xBC && tableSection.getTableId() <= 0xBE
					&& PreferencesManager.isEnableM7Fastscan()) {
				return getM7Descriptor(data, tableSection);
			}
			if (descriptorTag <= 0x3f) {
                return switch (tableSection.getTableId()) {
                    case 0x4c -> getINTDescriptor(data, tableSection);
                    case 0x4b -> getUNTDescriptor(data, tableSection);
                    case 0x74 -> getAITDescriptor(data, tableSection);
                    case 0xFC -> getSCTE35Descriptor(data, tableSection);
                    default -> getMPEGDescriptor(data, tableSection);
                };

            }
			if (descriptorTag <= 0x7f) {
				return getDVBSIDescriptor(data, tableSection, descriptorContext);
			}
			return  getPrivateDVBSIDescriptor(data, tableSection, descriptorContext);
			
		} catch (final RuntimeException iae) {
			// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
			// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
			Descriptor d = new Descriptor(data, tableSection);
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
			return new M7LogicalChannelDescriptor(data, tableSection);
		case 0x84:
			return new M7OperatorNameDescriptor(data, tableSection);
		case 0x85:
			return new M7OperatorSublistNameDescriptor(data, tableSection);
		case 0x86:
			return new M7OperatorPreferencesDescriptor(data, tableSection);
		case 0x87:
			return new M7OperatorDiSEqCTDescriptor(data, tableSection);
		case 0x88:
			return new M7OperatorOptionsDescriptor(data, tableSection);
		case 0x89:
			return new M7NagraBrandIdDescriptor(data, tableSection);
		case 0x8A:
			return new M7OttBrandIdDescriptor(data, tableSection);
		default:
			Descriptor d = new M7Descriptor(data, tableSection);
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
				return new UPCLogicalChannelDescriptor(data, tableSection);
			case 0x87:
				return new ZiggoVodDeliveryDescriptor(data, tableSection);
			}
		} else if (private_data_specifier == 0x16) { // Casema / Ziggo
			switch (descriptor_tag) {
			case 0x87:
				return new ZiggoVodDeliveryDescriptor(data, tableSection);
			case 0x93:
				return new ZiggoVodURLDescriptor(data, tableSection);
			case 0xD4:
				return new ZiggoPackageDescriptor(data, tableSection);
			}
		} else if (private_data_specifier == 0x28) { // EACEM
			switch (descriptor_tag) {
			case 0x83:
				return new LogicalChannelDescriptor(data, tableSection, descriptorContext);
			case 0x86:
				return new EACEMStreamIdentifierDescriptor(data, tableSection);
			case 0x88:
				return new HDSimulcastLogicalChannelDescriptor(data, tableSection, descriptorContext);
			}
		} else if (private_data_specifier == 0x29) { // Nordig
			switch (descriptor_tag) {
			case 0x83:
				return new NordigLogicalChannelDescriptorV1(data, tableSection, descriptorContext);
			case 0x87:
				return new NordigLogicalChannelDescriptorV2(data, tableSection, descriptorContext);
			}


		} else if (private_data_specifier == 0x40) { // CI Plus LLP
			switch (descriptor_tag) {
			case 0xCE:
				return new CIProtectionDescriptor(data, tableSection);
			}
		} else if (private_data_specifier == 0xa4) { // Canal + International
			switch (descriptor_tag) {
			case 0x80:
				return new CosBatSelectionDescriptor (data, tableSection);
			case 0x81:
				return new CosInformationParametersDescriptor(data, tableSection);
			case 0x83:
				return new CosLogicalChannelDescriptor(data, tableSection, descriptorContext);
			case 0x88:
				return new CosTimezoneDescriptor(data, tableSection);
			}
		} else if (private_data_specifier == 0x233a) { // DTG
			switch (descriptor_tag) {
			case 0x83: // can not re-use LogicalChannelDescriptor from EACEM, DTG has no visible flag
				return new nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.LogicalChannelDescriptor(data, tableSection, descriptorContext);
			case 0x86:
				return new ServiceAttributeDescriptor(data, tableSection);
			case 0x89:
				return new GuidanceDescriptor(data, tableSection);
			}
		} else if (private_data_specifier >= 0x00003200 && private_data_specifier <= 0x0000320F ) { // FREE TV AUSTRALIA OPERATIONAL PRACTICE OP-40
			switch (descriptor_tag) {
			case 0x83:
				return new nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.au.LogicalChannelDescriptor(data, tableSection, descriptorContext);
			}

		} else if (private_data_specifier == 0x41565356) { // AVS Video
			switch (descriptor_tag) {
				case 0xD1:
					return new AVS3VideoDescriptor(data, tableSection);
			}
		} else if (private_data_specifier == 0x41565341) { // AVS Audio
			switch (descriptor_tag) {
				case 0xD2:
					return new AVS3AudioDescriptor(data, tableSection);
			}
		} else if (private_data_specifier == 0x1) { // SES Astra
			switch (descriptor_tag) {
				case 0x88:
					return new ServiceListNameDescriptor(data, tableSection);
				case 0x93:
					return new BouquetListDescriptor(data, tableSection);
				case 0xD1:
					return new VirtualServiceIDDescriptor(data, tableSection);
					
					
			}
		
		}
		logger.info("Unimplemented private descriptor, private_data_specifier=" + private_data_specifier
					+ ", descriptortag=" + descriptor_tag + ", tableSection=" + tableSection);
		return new Descriptor(data, tableSection);
	}

	private static Descriptor getMPEGDescriptor(final byte[] data, final TableSection tableSection) {
		switch (toUnsignedInt(data[0])) {
		case 0x02:
			return new VideoStreamDescriptor(data, tableSection);
		case 0x03:
			return new AudioStreamDescriptor(data, tableSection);
		case 0x04:
			return new HierarchyDescriptor(data, tableSection);
		case 0x05:
			return new RegistrationDescriptor(data, tableSection);
		case 0x06:
			return new DataStreamAlignmentDescriptor(data, tableSection);
		case 0x07:
			return new TargetBackGroundDescriptor(data, tableSection);
		case 0x08:
			return new VideoWindowDescriptor(data, tableSection);
		case 0x09:
			return new CADescriptor(data, tableSection);
		case 0x0A:
			return new ISO639LanguageDescriptor(data, tableSection);
		case 0x0B:
			return new SystemClockDescriptor(data, tableSection);
		case 0x0C:
			return new MultiplexBufferUtilizationDescriptor(data, tableSection);
		case 0x0D:
			return new CopyrightDescriptor(data, tableSection);
		case 0x0E:
			return new MaximumBitrateDescriptor(data, tableSection);
		case 0x0F:
			return new PrivateDataIndicatorDescriptor(data, tableSection);
		case 0x10:
			return new SmoothingBufferDescriptor(data, tableSection);
		case 0x11:
			return new STDDescriptor(data, tableSection);
			// 0x12 IBP_descriptor as found in iso/conformance/hhi.m2t
		case 0x12:
			return new IBPDescriptor(data, tableSection);
		case 0x13:
			return new CarouselIdentifierDescriptor(data, tableSection);
		case 0x14:
			return new AssociationTagDescriptor(data, tableSection);
		case 0x1A:
			return new StreamEventDescriptor(data, tableSection);
		case 0x1C:
			return new Mpeg4AudioDescriptor(data, tableSection);
		case 0x25:
			return new MetaDataPointerDescriptor(data, tableSection);
		case 0x26:
			return new MetaDataDescriptor(data, tableSection);
		case 0x28:
			return new AVCVideoDescriptor(data, tableSection);
		case 0x2A:
			return new AVCTimingAndHRDDescriptor(data, tableSection);
		case 0x2B:
			return new AACMpeg2Descriptor(data, tableSection);
		case 0x32:
			return new JPEG2000VideoDescriptor(data, tableSection);
		case 0x38:
			return new HEVCVideoDescriptor(data, tableSection);
		case 0x39:
			return new VVCVideoDescriptor(data, tableSection);
		case 0x3F:
			return getMPEGExtendedDescriptor(data, tableSection);
		default:
			final Descriptor descriptor = new Descriptor(data, tableSection);
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
			return new NetworkNameDescriptor(data, tableSection);
		case 0x41:
			return new ServiceListDescriptor(data, tableSection, descriptorContext);
		case 0x43:
			return new SatelliteDeliverySystemDescriptor(data, tableSection);
		case 0x44:
			return new CableDeliverySystemDescriptor(data, tableSection);
		case 0x45:
			return new VBIDataDescriptor(data, tableSection);
		case 0x46: // semantics for the VBI teletext descriptor is the same as defined for the teletext descriptor
			return new TeletextDescriptor(data, tableSection);
		case 0x47:
			return new BouquetNameDescriptor(data, tableSection);
		case 0x48:
			return new ServiceDescriptor(data, tableSection);
		case 0x49:
			return new CountryAvailabilityDescriptor(data, tableSection);
		case 0x4A:
			return new LinkageDescriptor(data, tableSection);
		case 0x4B:
			return new NVODReferenceDescriptor(data, tableSection);
		case 0x4C:
			return new TimeShiftedServiceDescriptor(data, tableSection);
		case 0x4D:
			return new ShortEventDescriptor(data, tableSection);
		case 0x4E:
			return new ExtendedEventDescriptor(data, tableSection);
		case 0x4F:
			return new TimeShiftedEventDescriptor(data, tableSection);
		case 0x50:
			return new ComponentDescriptor(data, tableSection);
		case 0x51:
			return new MosaicDescriptor(data, tableSection);
		case 0x52:
			return new StreamIdentifierDescriptor(data, tableSection);
		case 0x53:
			return new CAIdentifierDescriptor(data, tableSection);
		case 0x54:
			return new ContentDescriptor(data, tableSection);
		case 0x55:
			return new ParentalRatingDescriptor(data, tableSection);
		case 0x56:
			return new TeletextDescriptor(data, tableSection);
		case 0x58:
			return new LocalTimeOffsetDescriptor(data, tableSection);
		case 0x59:
			return new SubtitlingDescriptor(data, tableSection);
		case 0x5A:
			return new TerrestrialDeliverySystemDescriptor(data, tableSection);
		case 0x5B:
			return new MultilingualNetworkNameDescriptor(data, tableSection);
		case 0x5C:
			return new MultilingualBouquetNameDescriptor(data, tableSection);
		case 0x5D:
			return new MultilingualServiceNameDescriptor(data, tableSection);
		case 0x5F:
			return new PrivateDataSpecifierDescriptor(data, tableSection);
		case 0x62:
			return new FrequencyListDescriptor(data, tableSection);
		case 0x63:
			return new PartialTransportStreamDescriptor(data, tableSection);
		case 0x64:
			return new DataBroadcastDescriptor(data, tableSection);
		case 0x65:
			return new ScramblingDescriptor(data, tableSection);
		case 0x66:
			return new DataBroadcastIDDescriptor(data, tableSection);
		case 0x69:
			return new PDCDescriptor(data, tableSection);
		case 0x6A:
			return new AC3Descriptor(data, tableSection);
		case 0x6B:
			return new AncillaryDataDescriptor(data, tableSection);
		case 0x6C:
			return new CellListDescriptor(data, tableSection);
		case 0x6D:
			return new CellFrequencyLinkDescriptor(data, tableSection);
		case 0x6F:
			return new ApplicationSignallingDescriptor(data, tableSection);
		case 0x70:
			return new AdaptationFieldDataDescriptor(data, tableSection);
		case 0x71:
			return new ServiceIdentifierDescriptor(data, tableSection);
		case 0x72:
			return new ServiceAvailabilityDescriptor(data, tableSection);
		case 0x73:
			return new DefaultAuthorityDescriptor(data, tableSection);
		case 0x74:
			return new RelatedContentDescriptor(data, tableSection);
		case 0x76:
			return new ContentIdentifierDescriptor(data, tableSection);
		case 0x77:
			return new TimeSliceFecIdentifierDescriptor(data, tableSection);
		case 0x79:
			return new S2SatelliteDeliverySystemDescriptor(data, tableSection);
		case 0x7A:
			return new EnhancedAC3Descriptor(data, tableSection);
		case 0x7C:
			return new AACDescriptor(data, tableSection);
		case 0x7E:
			return new FTAContentManagmentDescriptor(data, tableSection);
		case 0x7F:
			return getDVBExtendedDescriptor(data, tableSection);

		default:
			Descriptor d =  new Descriptor(data, tableSection);
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
			return new T2DeliverySystemDescriptor(data, tableSection);
		case 0x05:
			return new SHDeliverySystemDescriptor(data, tableSection);
		case 0x06:
			return new SupplementaryAudioDescriptor(data, tableSection);
		case 0x07:
			return new NetworkChangeNotifyDescriptor(data, tableSection);
		case 0x08:
			return new nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.MessageDescriptor(data, tableSection);
		case 0x09:
			return new TargetRegionDescriptor(data,tableSection);
		case 0x0A:
			return new TargetRegionNameDescriptor(data, tableSection);
		case 0x0B:
			return new ServiceRelocatedDescriptor(data, tableSection);
		case 0x11:
			return new T2MIDescriptor(data, tableSection);
		case 0x13:
			return new URILinkageDescriptor(data, tableSection);
		case 0x14:
			return new CIAncillaryDataDescriptor(data, tableSection);
		case 0x15:
			return new AC4Descriptor(data, tableSection);
		case 0x17:
			return new S2XSatelliteDeliverySystemDescriptor(data, tableSection);
		case 0x19:
			return new AudioPreselectionDescriptor(data, tableSection);
		case 0x20:
			return new TtmlSubtitlingDescriptor(data, tableSection);
		case 0x22:
			return new ServiceProminenceDescriptor(data, tableSection);
		case 0x23:
			return new VvcSubpicturesDescriptor(data, tableSection);

		default:
			DVBExtensionDescriptor d = new DVBExtensionDescriptor(data, tableSection);
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
			return new SchedulingDescriptor(data, tableSection);
		case 0x02:
			return new UpdateDescriptor(data, tableSection);
		case 0x03:
			return new SSULocationDescriptor(data, tableSection);
		case 0x04:
			return new MessageDescriptor(data, tableSection);
		case 0x05:
			return new SSUEventNameDescriptor(data, tableSection);
		case 0x06:
			return new TargetSmartcardDescriptor(data, tableSection);
		case 0x0B:
			return new SSUSubgroupAssociationDescriptor(data, tableSection);
		default:
			Descriptor d = new UNTDescriptor(data, tableSection);
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
			return new ApplicationDescriptor(data, tableSection);
		case 0x01:
			return new ApplicationNameDescriptor(data, tableSection);
		case 0x02:
			return new TransportProtocolDescriptor(data, tableSection);
		case 0x03:
			return new DVBJApplicationDescriptor(data, tableSection);
		case 0x04:
			return new DVBJApplicationLocationDescriptor(data, tableSection);
		case 0x05:
			return new ExternalApplicationAuthorizationDescriptor(data, tableSection);
		case 0x15:
			return new SimpleApplicationLocationDescriptor(data, tableSection);
		case 0x16:
			return new ApplicationUsageDescriptor(data, tableSection);
		case 0x17:
			return new SimpleApplicationBoundaryDescriptor(data, tableSection);
		default:
			Descriptor d = new AITDescriptor(data, tableSection);
			logger.info("Not implemented AITDescriptor:" + toUnsignedInt(data[0]) + " ("
					+ AITDescriptor.getDescriptorname(toUnsignedInt(data[0]))
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
			}
	}

	private static Descriptor getSCTE35Descriptor(final byte[] data, final TableSection tableSection) {

		switch (toUnsignedInt(data[0])) {
		case 0x00:
			return new AvailDescriptor(data, tableSection);
		case 0x02:
			return new SegmentationDescriptor(data, tableSection);
        case 0x03:
            return new TimeDescriptor(data, tableSection);			
		default:
			Descriptor d = new SCTE35Descriptor(data, tableSection);
			logger.info("Not implemented SCTE35Descriptor:" + toUnsignedInt(data[0]) + " ("
					+ SCTE35Descriptor.getDescriptorname(toUnsignedInt(data[0]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			return d;
		}
	}

}
