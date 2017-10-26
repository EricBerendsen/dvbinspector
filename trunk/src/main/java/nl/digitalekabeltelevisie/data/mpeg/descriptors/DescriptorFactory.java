/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.AITDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.ApplicationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.ApplicationNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.ApplicationUsageDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.DVBJApplicationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.DVBJApplicationLocationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.SimpleApplicationBoundaryDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.SimpleApplicationLocationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable.TransportProtocolDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.HEVCTimingAndHRDDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg.MPEGExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.INTDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.IPMACPlatformNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.IPMACPlatformProviderNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.IPMACStreamLocationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.intable.TargetIPSlashDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema.ZiggoPackageDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema.ZiggoVodDeliveryDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema.ZiggoVodURLDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ciplus.CIProtectionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.GuidanceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.EACEMStreamIdentifierDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.HDSimulcastLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.LogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.nordig.NordigLogicalChannelDescriptorV1;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.nordig.NordigLogicalChannelDescriptorV2;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.upc.UPCLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.MessageDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.SSUEventNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.SSULocationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.SSUSubgroupAssociationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.SchedulingDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.UNTDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.untable.UpdateDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.util.Utils;

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
		long private_data_specifier = tableSection.getParentTransportStream().getDefaultPrivateDataSpecifier();
		final List<Descriptor> r = new ArrayList<Descriptor>();
		int t = 0;

		while (t < len) {

			Descriptor d;
			final int descriptorTag = Utils.getUnsignedByte(data[t + offset]);
			try {
				if (tableSection.getTableId() == 0xFC) {
					d = getSCTE35Descriptor(data, offset, tableSection, t);
				} else if (descriptorTag <= 0x3f) {
					if (tableSection.getTableId() == 0x4c) {
						d = getINTDescriptor(data, offset, tableSection, t);
					} else if (tableSection.getTableId() == 0x4b) {
						d = getUNTDescriptor(data, offset, tableSection, t);
					} else if (tableSection.getTableId() == 0x74) {
						d = getAITDescriptor(data, offset, tableSection, t);
					} else {
						d = getMPEGDescriptor(data, offset, tableSection, t);
					}
				} else if (descriptorTag <= 0x7f) {
					d = getDVBSIDescriptor(data, offset, tableSection, t);
				} else {
					d = getPrivateDVBSIDescriptor(data, offset, tableSection, t, private_data_specifier);

				}
			} catch (final RuntimeException iae) {
				// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
				// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
				d = new Descriptor(data, t + offset, tableSection);
				logger.warning("Fall back for descriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
						+ Descriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]), tableSection)
						+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
						+ ",) data=" + d.getRawDataString()+", RuntimeException:"+iae);
			}

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
	 * @param t
	 * @param private_data_specifier
	 * @return
	 */
	private static Descriptor getPrivateDVBSIDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t,
			final long private_data_specifier) {
		Descriptor d = null;

		if (private_data_specifier == 0x600) { // UPC1
			switch (Utils.getUnsignedByte(data[t + offset])) {
			case 0x81:
				d = new UPCLogicalChannelDescriptor(data, t + offset, tableSection);
				break;
			case 0x87:
				d = new ZiggoVodDeliveryDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x16) { // Casema / Ziggo
			switch (Utils.getUnsignedByte(data[t + offset])) {
			case 0x87:
				d = new ZiggoVodDeliveryDescriptor(data, t + offset, tableSection);
				break;
			case 0x93:
				d = new ZiggoVodURLDescriptor(data, t + offset, tableSection);
				break;
			case 0xD4:
				d = new ZiggoPackageDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x28) { // EACEM
			switch (Utils.getUnsignedByte(data[t + offset])) {
			case 0x83:
				d = new LogicalChannelDescriptor(data, t + offset, tableSection);
				break;
			case 0x86:
				d = new EACEMStreamIdentifierDescriptor(data, t + offset, tableSection);
				break;
			case 0x88:
				d = new HDSimulcastLogicalChannelDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x29) { // Nordig
			switch (Utils.getUnsignedByte(data[t + offset])) {
			case 0x83:
				d = new NordigLogicalChannelDescriptorV1(data, t + offset, tableSection);
				break;
			case 0x87:
				d = new NordigLogicalChannelDescriptorV2(data, t + offset, tableSection);
				break;
			}


		} else if (private_data_specifier == 0x40) { // CI Plus LLP
			switch (Utils.getUnsignedByte(data[t + offset])) {
			case 0xCE:
				d = new CIProtectionDescriptor(data, t + offset, tableSection);
				break;
			}
		} else if (private_data_specifier == 0x233a) { // DTG
			switch (Utils.getUnsignedByte(data[t + offset])) {
			case 0x83: // can not re-use LogicalChannelDescriptor from EACEM, DTG has no visible flag
				d = new nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg.LogicalChannelDescriptor(data, t + offset, tableSection);
				break;
			case 0x89:
				d = new GuidanceDescriptor(data, t + offset, tableSection);
				break;
			}
		}
		if (d == null) {
			logger.info("Unimplemented private descriptor, private_data_specifier=" + private_data_specifier
					+ ", descriptortag=" + Utils.getUnsignedByte(data[t + offset]) + ", tableSection=" + tableSection);
			d = new Descriptor(data, t + offset, tableSection);
		}

		return d;
	}

	private static Descriptor getMPEGDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (Utils.getUnsignedByte(data[t + offset])) {
		case 0x02:
			d = new VideoStreamDescriptor(data, t + offset, tableSection);
			break;
		case 0x03:
			d = new AudioStreamDescriptor(data, t + offset, tableSection);
			break;
		case 0x04:
			d = new HierarchyDescriptor(data, t + offset, tableSection);
			break;
		case 0x05:
			d = new RegistrationDescriptor(data, t + offset, tableSection);
			break;
		case 0x06:
			d = new DataStreamAlignmentDescriptor(data, t + offset, tableSection);
			break;
		case 0x07:
			d = new TargetBackGroundDescriptor(data, t + offset, tableSection);
			break;
		case 0x08:
			d = new VideoWindowDescriptor(data, t + offset, tableSection);
			break;
		case 0x09:
			d = new CADescriptor(data, t + offset, tableSection);
			break;
		case 0x0A:
			d = new ISO639LanguageDescriptor(data, t + offset, tableSection);
			break;
		case 0x0B:
			d = new SystemClockDescriptor(data, t + offset, tableSection);
			break;
		case 0x0C:
			d = new MultiplexBufferUtilizationDescriptor(data, t + offset, tableSection);
			break;
		case 0x0E:
			d = new MaximumBitrateDescriptor(data, t + offset, tableSection);
			break;
		case 0x0F:
			d = new PrivateDataIndicatorDescriptor(data, t + offset, tableSection);
			break;
		case 0x10:
			d = new SmoothingBufferDescriptor(data, t + offset, tableSection);
			break;
		case 0x11:
			d = new STDDescriptor(data, t + offset, tableSection);
			break;
			// 0x12 IBP_descriptor as found in iso/conformance/hhi.m2t
		case 0x12:
			d = new IBPDescriptor(data, t + offset, tableSection);
			break;
		case 0x13:
			d = new CarouselIdentifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x14:
			d = new AssociationTagDescriptor(data, t + offset, tableSection);
			break;
		case 0x1A:
			d = new StreamEventDescriptor(data, t + offset, tableSection);
			break;
		case 0x1C:
			d = new Mpeg4AudioDescriptor(data, t + offset, tableSection);
			break;
		case 0x25:
			d = new MetaDataPointerDescriptor(data, t + offset, tableSection);
			break;
		case 0x26:
			d = new MetaDataDescriptor(data, t + offset, tableSection);
			break;
		case 0x28:
			d = new AVCVideoDescriptor(data, t + offset, tableSection);
			break;
		case 0x2A:
			d = new AVCTimingAndHRDDescriptor(data, t + offset, tableSection);
			break;
		case 0x2B:
			d = new AACMpeg2Descriptor(data, t + offset, tableSection);
			break;
		case 0x32:
			d = new JPEG2000VideoDescriptor(data, t + offset, tableSection);
			break;
		case 0x38:
			d = new HEVCVideoDescriptor(data, t + offset, tableSection);
			break;
		case 0x3F:
			d = getMPEGExtendedDescriptor(data, offset, tableSection, t);
			break;
		default:
			d = new Descriptor(data, t + offset, tableSection);
			logger.info("Not implemented descriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
					+ Descriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}
	
	private static MPEGExtensionDescriptor getMPEGExtendedDescriptor(final byte[] data, final int offset,
			final TableSection tableSection, final int t) {

		MPEGExtensionDescriptor d;
		final int descriptor_tag_extension = Utils.getUnsignedByte(data[t + offset+2]);
		switch(descriptor_tag_extension){
		
		case 0x03:
			d = new HEVCTimingAndHRDDescriptor(data, t + offset, tableSection);
			break;
		default:
			d = new MPEGExtensionDescriptor(data, t + offset, tableSection);
			logger.warning("unimplemented MPEGExtensionDescriptor:" +
					d.getDescriptorTagString() +
					", TableSection:" + tableSection);
		}

		return d;
	}

	private static Descriptor getDVBSIDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (Utils.getUnsignedByte(data[t + offset])) {

		case 0x40:
			d = new NetworkNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x41:
			d = new ServiceListDescriptor(data, t + offset, tableSection);
			break;
		case 0x43:
			d = new SatelliteDeliverySystemDescriptor(data, t + offset, tableSection);
			break;
		case 0x44:
			d = new CableDeliverySystemDescriptor(data, t + offset, tableSection);
			break;
		case 0x45:
			d = new VBIDataDescriptor(data, t + offset, tableSection);
			break;
		case 0x46: // semantics for the VBI teletext descriptor is the same as defined for the teletext descriptor
			d = new TeletextDescriptor(data, t + offset, tableSection);
			break;
		case 0x47:
			d = new BouquetNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x48:
			d = new ServiceDescriptor(data, t + offset, tableSection);
			break;
		case 0x49:
			d = new CountryAvailabilityDescriptor(data, t + offset, tableSection);
			break;
		case 0x4A:
			d = new LinkageDescriptor(data, t + offset, tableSection);
			break;
		case 0x4C:
			d = new TimeShiftedServiceDescriptor(data, t + offset, tableSection);
			break;
		case 0x4D:
			d = new ShortEventDescriptor(data, t + offset, tableSection);
			break;
		case 0x4E:
			d = new ExtendedEventDescriptor(data, t + offset, tableSection);
			break;
		case 0x50:
			d = new ComponentDescriptor(data, t + offset, tableSection);
			break;
		case 0x51:
			d = new MosaicDescriptor(data, t + offset, tableSection);
			break;
		case 0x52:
			d = new StreamIdentifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x53:
			d = new CAIdentifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x54:
			d = new ContentDescriptor(data, t + offset, tableSection);
			break;
		case 0x55:
			d = new ParentalRatingDescriptor(data, t + offset, tableSection);
			break;
		case 0x56:
			d = new TeletextDescriptor(data, t + offset, tableSection);
			break;
		case 0x58:
			d = new LocalTimeOffsetDescriptor(data, t + offset, tableSection);
			break;
		case 0x59:
			d = new SubtitlingDescriptor(data, t + offset, tableSection);
			break;
		case 0x5A:
			d = new TerrestrialDeliverySystemDescriptor(data, t + offset, tableSection);
			break;
		case 0x5B:
			d = new MultilingualNetworkNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x5C:
			d = new MultilingualBouquetNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x5D:
			d = new MultilingualServiceNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x5F:
			d = new PrivateDataSpecifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x62:
			d = new FrequencyListDescriptor(data, t + offset, tableSection);
			break;
		case 0x63:
			d = new PartialTransportStreamDescriptor(data, t + offset, tableSection);
			break;
		case 0x64:
			d = new DataBroadcastDescriptor(data, t + offset, tableSection);
			break;
		case 0x66:
			d = new DataBroadcastIDDescriptor(data, t + offset, tableSection);
			break;
		case 0x69:
			d = new PDCDescriptor(data, t + offset, tableSection);
			break;
		case 0x6A:
			d = new AC3Descriptor(data, t + offset, tableSection);
			break;
		case 0x6B:
			d = new AncillaryDataDescriptor(data, t + offset, tableSection);
			break;
		case 0x6C:
			d = new CellListDescriptor(data, t + offset, tableSection);
			break;
		case 0x6D:
			d = new CellFrequencyLinkDescriptor(data, t + offset, tableSection);
			break;
		case 0x6F:
			d = new ApplicationSignallingDescriptor(data, t + offset, tableSection);
			break;
		case 0x70:
			d = new AdaptationFieldDataDescriptor(data, t + offset, tableSection);
			break;
		case 0x71:
			d = new ServiceIdentifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x73:
			d = new DefaultAuthorityDescriptor(data, t + offset, tableSection);
			break;
		case 0x74:
			d = new RelatedContentDescriptor(data, t + offset, tableSection);
			break;
		case 0x76:
			d = new ContentIdentifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x77:
			d = new TimeSliceFecIdentifierDescriptor(data, t + offset, tableSection);
			break;
		case 0x79:
			d = new S2SatelliteDeliverySystemDescriptor(data, t + offset, tableSection);
			break;
		case 0x7A:
			d = new EnhancedAC3Descriptor(data, t + offset, tableSection);
			break;
		case 0x7C:
			d = new AACDescriptor(data, t + offset, tableSection);
			break;
		case 0x7E:
			d = new FTAContentManagmentDescriptor(data, t + offset, tableSection);
			break;
		case 0x7F:
			d = getDVBExtendedDescriptor(data, offset, tableSection, t);
			break;



		default:
			d = new Descriptor(data, t + offset, tableSection);
			logger.info("Not implemented descriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
					+ Descriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

	/**
	 * @param data
	 * @param offset
	 * @param tableSection
	 * @param t
	 * @return
	 */
	private static DVBExtensionDescriptor getDVBExtendedDescriptor(final byte[] data, final int offset,
			final TableSection tableSection, final int t) {

		DVBExtensionDescriptor d;
		final int descriptor_tag_extension = Utils.getUnsignedByte(data[t + offset+2]);
		switch(descriptor_tag_extension){

		case 0x04:
			d = new T2DeliverySystemDescriptor(data, t + offset, tableSection);
			break;
		case 0x05:
			d = new SHDeliverySystemDescriptor(data, t + offset, tableSection);
			break;
		case 0x06:
			d = new SupplementaryAudioDescriptor(data, t + offset, tableSection);
			break;
		case 0x07:
			d = new NetworkChangeNotifyDescriptor(data, t+offset, tableSection);
			break;
		case 0x08:
			d = new nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.MessageDescriptor(data, t + offset, tableSection);
			break;
		case 0x09:
			d = new TargetRegionDescriptor(data, t + offset, tableSection);
			break;
		case 0x0A:
			d = new TargetRegionNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x0B:
			d = new ServiceRelocatedDescriptor(data, t + offset, tableSection);
			break;
		case 0x13:
			d = new URILinkageDescriptor(data, t + offset, tableSection);
			break;
		case 0x14:
			d = new CIAncillaryDataDescriptor(data, t + offset, tableSection);
			break;
		case 0x17:
			d = new S2XSatelliteDeliverySystemDescriptor(data, t + offset, tableSection);
			break;



		default:
			d = new DVBExtensionDescriptor(data, t + offset, tableSection);
			logger.warning("unimplemented DVBExtensionDescriptor:" +
					d.getDescriptorTagString() +
					", TableSection:" + tableSection);
		}

		return d;
	}

	private static Descriptor getINTDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (Utils.getUnsignedByte(data[t + offset])) {
		case 0x0C:
			d = new IPMACPlatformNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x0D:
			d = new IPMACPlatformProviderNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x0F:
			d = new TargetIPSlashDescriptor(data, t + offset, tableSection);
			break;
		case 0x13:
			d = new IPMACStreamLocationDescriptor(data, t + offset, tableSection);
			break;
		default:
			d = new INTDescriptor(data, t + offset, tableSection);
			logger.info("Not implemented IntDescriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
					+ INTDescriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

	private static Descriptor getUNTDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (Utils.getUnsignedByte(data[t + offset])) {
		case 0x01:
			d = new SchedulingDescriptor(data, t + offset, tableSection);
			break;
		case 0x02:
			d = new UpdateDescriptor(data, t + offset, tableSection);
			break;
		case 0x03:
			d = new SSULocationDescriptor(data, t + offset, tableSection);
			break;
		case 0x04:
			d = new MessageDescriptor(data, t + offset, tableSection);
			break;
		case 0x05:
			d = new SSUEventNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x0B:
			d = new SSUSubgroupAssociationDescriptor(data, t + offset, tableSection);
			break;
		default:
			d = new UNTDescriptor(data, t + offset, tableSection);
			logger.info("Not implemented UNTDescriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
					+ UNTDescriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

	private static Descriptor getAITDescriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (Utils.getUnsignedByte(data[t + offset])) {
		case 0x00:
			d = new ApplicationDescriptor(data, t + offset, tableSection);
			break;
		case 0x01:
			d = new ApplicationNameDescriptor(data, t + offset, tableSection);
			break;
		case 0x02:
			d = new TransportProtocolDescriptor(data, t + offset, tableSection);
			break;
		case 0x03:
			d = new DVBJApplicationDescriptor(data, t + offset, tableSection);
			break;
		case 0x04:
			d = new DVBJApplicationLocationDescriptor(data, t + offset, tableSection);
			break;


		case 0x15:
			d = new SimpleApplicationLocationDescriptor(data, t + offset, tableSection);
			break;
		case 0x16:
			d = new ApplicationUsageDescriptor(data, t + offset, tableSection);
			break;
		case 0x17:
			d = new SimpleApplicationBoundaryDescriptor(data, t + offset, tableSection);
			break;
		default:
			d = new AITDescriptor(data, t + offset, tableSection);
			logger.info("Not implemented AITDescriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
					+ AITDescriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

	private static Descriptor getSCTE35Descriptor(final byte[] data, final int offset, final TableSection tableSection, final int t) {
		Descriptor d;
		switch (Utils.getUnsignedByte(data[t + offset])) {
		case 0x00:
			d = new AvailDescriptor(data, t + offset, tableSection);
			break;
		default:
			d = new SCTE35Descriptor(data, t + offset, tableSection);
			logger.info("Not implemented SCTE35Descriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
					+ SCTE35Descriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]), tableSection)
					+ ")in section " + TableSection.getTableType(tableSection.getTableId()) + " (" + tableSection
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

}
