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
import java.util.function.Function;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.*;

/**
 * @author Eric Berendsen
 *
 */
public class Descriptor implements TreeNode {

	private static LookUpList metadata_application_format_list = new LookUpList.Builder().
			add(0x0000,0x000F,"Reserved").
			add(0x0010,"ISO 15706 (ISAN) encoded in its binary form").
			add(0x0011, "ISO 15706-2 (V-ISAN) encoded in its binary form").
			add(0x0012,0x00FF,"Reserved").
			add(0x0100,"metadata service contains TVA metadata as profiled according to DVB").
			add(0x0101,"metadata contained conforms to DTG D-Book Record List.").
			add(0x0102,0xFFFE,"User defined").
			add(0xFFFF,"Defined by the metadata_application_format_identifier field").
			build();
	private static LookUpList mpeg_carriage_flags_list = new LookUpList.Builder().
			add(0,"Carriage in the same transport stream where this metadata pointer descriptor is carried.").
			add(1,"Carriage in a different transport stream from where this metadata pointer descriptor is carried.").
			add(2,"Carriage in a program stream. This may or may not be the same program stream in which this metadata pointer descriptor is carried.").
			add(3,"may be used if there is no relevant metadata carried on the DVB network. In this case the metadata locator record shall be present").
			build();
	private static LookUpList metadata_format_list = new LookUpList.Builder().
			add(0,0x0f,"Reserved").
			add(0x10,"ISO/IEC 15938-1 TeM").
			add(0x11,"ISO/IEC 15938-1 BiM").
			add(0x12,0x3e,"Reserved").
			add(0x3f,"Defined by metadata application format").
			add(0x40,0xef,"User Defined").
			add(0xf0,"The encoding and encapsulation format as defined in clauses 9.3 and 9.4 of ETSI TS 102 323 V1.5.1").
			add(0xf1,0xf7,"DVB Reserved").
			add(0xf8,0xfe,"User Defined").
			add(0xff,"Defined by metadata_format_identifier field").
			build();
	protected int			descriptorTag		= 0;
	protected int			descriptorLength	= 0;

	protected final byte[]	privateData;
	private final int		descriptorOffset;
	protected int			privateDataOffset;

	protected TableSection	parentTableSection;


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public Descriptor(final byte[] b, final int offset, final TableSection parent) {
		privateData = b;
		descriptorOffset = offset;
		privateDataOffset = offset + 2;

		this.descriptorTag = toUnsignedInt(b[offset]);
		this.descriptorLength = toUnsignedInt(b[offset + 1]);
		parentTableSection = parent;
	}

	public int getDescriptorLength() {
		return descriptorLength;
	}

	public void setDescriptorLength(final int descriptorLength) {
		this.descriptorLength = descriptorLength;
	}

	public int getDescriptorTag() {
		return descriptorTag;
	}

	public void setDescriptorTag(final int descriptorTag) {
		this.descriptorTag = descriptorTag;
	}

	/**
	 * This method should be overridden by private descriptors where name/meaning is dependent on preceding Private Data Specifier Descriptor
	 * @return the (default)name for this descriptor
	 */
	public String getDescriptorname() {
		return getDescriptorname(descriptorTag, parentTableSection);
	}

	public static String getDescriptorname(final int tag, final TableSection tableSection) {

		switch (tag) {
		case 0:
			return "Reserved";
		case 1:
			return "Reserved";
		case 2:
			return "video_stream_descriptor";
		case 3:
			return "audio_stream_descriptor";
		case 4:
			return "hierarchy_descriptor";
		case 5:
			return "registration_descriptor";
		case 6:
			return "data_stream_alignment_descriptor";
		case 7:
			return "target_background_grid_descriptor";
		case 8:
			return "Video_window_descriptor";
		case 9:
			return "CA_descriptor";
		case 10:
			return "ISO_639_language_descriptor";
		case 11:
			return "System_clock_descriptor";
		case 12:
			return "Multiplex_buffer_utilization_descriptor";
		case 13:
			return "Copyright_descriptor";
		case 14:
			return "Maximum_bitrate_descriptor";
		case 15:
			return "Private_data_indicator_descriptor";
		case 16:
			return "Smoothing_buffer_descriptor";
		case 17:
			return "STD_descriptor";
		case 18:
			return "IBP_descriptor";

			// From DVBSnoop mpeg_descriptor.c 1.4.50

			/* 0x13 - 0x1A DSM-CC ISO13818-6, TR 102 006 */
		case 0x13:
			return "DSM-CC Carousel_Identifier_descriptor";
		case 0x14:
			return "DSM-CC Association_tag_descriptor";
		case 0x15:
			return "DSM-CC Deferred_Association_tags_descriptor";

			/* DSM-CC stream descriptors */
			// case 0x16: reserved....
		case 0x17:
			return "NPT_reference_descriptor";
		case 0x18:
			return "NPT_endpoint_descriptor";
		case 0x19:
			return "stream_mode_descriptor";
		case 0x1A:
			return "stream_event_descriptor";

			/* MPEG 4 */
		case 0x1B:
			return "MPEG4_video_descriptor";
		case 0x1C:
			return "MPEG4_audio_descriptor";
		case 0x1D:
			return "IOD_descriptor";
		case 0x1E:
			return "SL_descriptor";
		case 0x1F:
			return "FMC_descriptor";
		case 0x20:
			return "External_ES_ID_descriptor";
		case 0x21:
			return "MuxCode_descriptor";
		case 0x22:
			return "FMXBufferSize_descriptor";
		case 0x23:
			return "MultiplexBuffer_descriptor";
		case 0x24:
			return "ContentLabeling_descriptor";

			/* TV ANYTIME, TS 102 323 */
		case 0x25:
			return "metadata_pointer_descriptor";
		case 0x26:
			return "metadata_descriptor";
		case 0x27:
			return "metadata_STD_descriptor";

			/* H.222.0 AMD 3 */
			/* http://neuron2.net/library/avc/T-REC-H%5B1%5D.222.0-200403-I!Amd3!PDF-E.pdf */
		case 0x28:
			return "AVC_video_descriptor";
		case 0x29:
			return "IPMP_descriptor";
		case 0x2A:
			return "AVC_timing_and_HRD_descriptor";

			/* H.222.0 Corr 4 */
		case 0x2B:
			return "MPEG2_AAC_audio_descriptor";
		case 0x2C:
			return "FlexMuxTiming_descriptor";

			/* ISO/IEC 13818-1:2007/FPDAM5 */
		case 0x2D:
			return "MPEG-4_text_descriptor";
		case 0x2E:
			return "MPEG-4_audio_extension_descriptor";
		case 0x2F:
			return "Auxiliary_video_stream_descriptor";
		case 0x30:
			return "SVC extension descriptor";
		case 0x31:
			return "MVC extension descriptor";

			/* ISO/IEC 13818-1:2007/FPDAM5 - Transport of JPEG 2000 part 1 video */
		case 0x32:
			return "J2K video descriptor";

			/* Rec. ITU-T H.222.0 (06/2012) */
		case 51:
			return "MVC operation point descriptor";
		case 52:
			return "MPEG2_stereoscopic_video_format_descriptor";
		case 53:
			return "Stereoscopic_program_info_descriptor";
		case 54:
			return "Stereoscopic_video_info_descriptor";

			/* Rec. ITU-T H.222.0 (10/2014) */
		case 55:
			return "Transport_profile_descriptor";
		case 56:
			return "HEVC video descriptor";
		case 63:
			return "Extension_descriptor";

			// DVB

		case 0x40:
			return "network_name_descriptor";
		case 0x41:
			return "service_list_descriptor";
		case 0x42:
			return "stuffing_descriptor";
		case 0x43:
			return "satellite_delivery_system_descriptor";
		case 0x44:
			return "cable_delivery_system_descriptor";
		case 0x45:
			return "VBI_data_descriptor";
		case 0x46:
			return "VBI_teletext_descriptor";
		case 0x47:
			return "bouquet_name_descriptor";
		case 0x48:
			return "service_descriptor";
		case 0x49:
			return "country_availability_descriptor";
		case 0x4A:
			return "linkage_descriptor";
		case 0x4B:
			return "NVOD_reference_descriptor";
		case 0x4C:
			return "time_shifted_service_descriptor";
		case 0x4D:
			return "short_event_descriptor";
		case 0x4E:
			return "extended_event_descriptor";
		case 0x4F:
			return "time_shifted_event_descriptor";
		case 0x50:
			return "component_descriptor";
		case 0x51:
			return "mosaic_descriptor";
		case 0x52:
			return "stream_identifier_descriptor";
		case 0x53:
			return "CA_identifier_descriptor";
		case 0x54:
			return "content_descriptor";
		case 0x55:
			return "parental_rating_descriptor";
		case 0x56:
			return "teletext_descriptor";
		case 0x57:
			return "telephone_descriptor";
		case 0x58:
			return "local_time_offset_descriptor";
		case 0x59:
			return "subtitling_descriptor";
		case 0x5A:
			return "terrestrial_delivery_system_descriptor";
		case 0x5B:
			return "multilingual_network_name_descriptor";
		case 0x5C:
			return "multilingual_bouquet_name_descriptor";
		case 0x5D:
			return "multilingual_service_name_descriptor";
		case 0x5E:
			return "multilingual_component_descriptor";
		case 0x5F:
			return "private_data_specifier_descriptor";
		case 0x60:
			return "service_move_descriptor";
		case 0x61:
			return "short_smoothing_buffer_descriptor";
		case 0x62:
			return "frequency_list_descriptor";
		case 0x63:
			return "partial_transport_stream_descriptor";
		case 0x64:
			return "data_broadcast_descriptor";
		case 0x65:
			return "scrambling_descriptor";
		case 0x66:
			return "data_broadcast_id_descriptor";
		case 0x67:
			return "transport_stream_descriptor";
		case 0x68:
			return "DSNG_descriptor";
		case 0x69:
			return "PDC_descriptor";
		case 0x6A:
			return "AC-3_descriptor";
		case 0x6B:
			return "ancillary_data_descriptor";
		case 0x6C:
			return "cell_list_descriptor";
		case 0x6D:
			return "cell_frequency_link_descriptor";
		case 0x6E:
			return "announcement_support_descriptor";
		case 0x6F:
			return "application_signalling_descriptor";
		case 0x70:
			return "adaptation_field_data_descriptor";
		case 0x71:
			return "service_identifier_descriptor";
		case 0x72:
			return "service_availability_descriptor";
		case 0x73:
			return "default_authority_descriptor";
		case 0x74:
			return "related_content_descriptor";
		case 0x75:
			return "TVA_id_descriptor";
		case 0x76:
			return "content_identifier_descriptor/TV-Anytime serial recordings descriptor"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0x77:
			return "time_slice_fec_identifier_descriptor";
		case 0x78:
			return "ECM_repetition_rate_descriptor";
		case 0x79:
			return "S2_satellite_delivery_system_descriptor";
		case 0x7A:
			return "enhanced_AC-3_descriptor";
		case 0x7B:
			return "DTS descriptor";
		case 0x7C:
			return "AAC descriptor";
		case 0x7D:
			return "XAIT location descriptor";
		case 0x7E:
			return "FTA_content_management_descriptor";
		case 0x7F:
			return "extension descriptor";

		case 0x81:
			return "user defined: UPC logic_channel_descriptor/ATSC AC-3 audio descriptor";
			/* http://www.nordig.org/pdf/NorDig_RoOspec_0_9.pdf */
		case 0x82:
			return "user defined: Viasat private: Logic_channel_dscriptor";
		case 0x83:
			return "user defined: EACEM Logic_channel_descriptor / NorDig private: Logic_channel_descriptor version 1 / DTG logical_channel_descriptor";
		case 0x84:
			return "user defined: EACEM Preferred_name_list_descriptor / DTG preferred_name_list_descriptor";
		case 0x85:
			return "user defined: EACEM Preferred_name_identifier_descriptor / DTG preferred_name_identifier_descriptor";
		case 0x86:
			return "user defined: EACEM stream_identifier_descriptor / DTG service_attribute_descriptor";
		case 0x87:
			return "user defined: Ziggo/OpenTV Video On Demand delivery descriptor / NORDIG Logical_channel_descriptor version 2 / DTG short_service_name_descriptor";
		case 0x88:
			return "user defined: EACEM private: HD_simulcast_logical_channel_descriptor / YOUSEE Event tag descriptor / DTG HD_simulcast_logical_channel_descriptor/ hdmv_copy_control_descriptor";
			// http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf


		case 0x89:
			return "user defined: OpenTV private descriptor / DTG guidance_descriptor"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0x8A:
			return "user defined: SCTE-35 Cue Identifier Descriptor "; //http://www.scte.org/documents/pdf/Standards/ANSI_SCTE%2035%202014.pdf
		case 0x8C:
			return "user defined: VodaphoneZiggo / UPC blackout_descriptor"; // Wholesale kabel toegang Referentieaanbod VodafoneZiggo PSI/SI overview Annex behorende bij Bijlage 1: Technische specificaties
		case 0x90:
			return "user defined: OpenTV module_track_descriptor";// http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0x92:
			return "user defined: Extended location ID (YOUSEE)";// http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0x93:
			return "user defined: Ziggo/OpenTV Video On Demand URL";

		// ANSI/SCTE 128 2010-a AVC Video Systems and Transport Constraints for Cable Television
		// https://www.scte.org/documents/pdf/Standards/ANSI_SCTE%20128%202010-a.pdf
		// 6.3.2.3 SCTE Adaptation field data descriptor 
		case 0x97:
			return "user defined: SCTE adaptation field data descriptor";
		
		case 0xA0:
			return "user defined: NorDig Content Protection Descriptor";
			
			// https://professional.dolby.com/siteassets/pdfs/dolby-vision-bitstreams-in-mpeg-2-transport-stream-multiplex-v1.2.pdf
		case 0xB0:
			return "user defined: DOVI_video_stream_descriptor";

		case 0xCE:
			return "user defined: CI Protection Descriptor";

		case 0xD4:
			return "user defined: Ziggo Package Descriptor";

			
			// OpenCable™ Specifications Encoder Boundary Point Specification 			OC-SP-EBP-I01-130118
			// https://specification-search.cablelabs.com/encoder-boundary-point-specification
			
		case 0xE9:
			return "user defined: SCTE EBP_descriptor";

			/* http://www.nordig.org/pdf/NorDig_RoOspec_0_9.pdf */
		case 0xF1:
			return "user defined: Senda private: Channel_list_descriptor";
		case 0xFE:
			return "user defined: OpenTV track_tag_descriptor"; //
		case 0xFF:
			return "forbidden";

		default:
			if ((19 <= tag) && (tag <= 26)) {
				return "Defined in ISO/IEC 13818-6";
			}

			if (tag <= 63) {
				return "ITU-T Rec. H.222.0 | ISO/IEC 13818-1 Reserved";
			}

			if ((0x80 <= tag) && (tag <= 0xFE)) {
				return "user defined";
			}

			return "illegal descriptor tag value";

		}
	}

	public PID getParentPID() {
		return getParentTableSection().getParentPID();
	}

	public TransportStream getParentTransportStream() {
		return getParentPID().getParentTransportStream();
	}

	public PSI getPSI() {
		return getParentTransportStream().getPsi();
	}

	@Override
	public String toString() {
		return getDescriptorname() + ", (data)";
	}

	public String getRawDataString() {
		final StringBuilder b = new StringBuilder();
		b.append("0x").append(Utils.toHexString(privateData, privateDataOffset, descriptorLength)).append(" \"")
		.append(Utils.toSafeString(privateData, privateDataOffset, descriptorLength)).append("\"");
		return b.toString();
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Descriptor: " + getDescriptorname(),
				descriptorTag, null));

		addGeneralDescriptorInfo(modus, t);
		return t;
	}

	/**
	 * @param modus
	 * @param t
	 */
	protected void addGeneralDescriptorInfo(final int modus, final DefaultMutableTreeNode t) {
		if (!Utils.simpleModus(modus)) { // not simple layout, so show
			// details
			t.add(new DefaultMutableTreeNode(new KVP("descriptor_tag", descriptorTag, getDescriptorname())));
			t.add(new DefaultMutableTreeNode(new KVP("descriptor_length", descriptorLength, null)));
		}
		if ((this.getClass().equals(Descriptor.class)) || (!Utils.simpleModus(modus))) { // not simple layout, so show details
			t.add(new DefaultMutableTreeNode(new KVP("descriptor_data", privateData, descriptorOffset + 2,
					descriptorLength, null)));
		}
	}

	public TableSection getParentTableSection() {
		return parentTableSection;
	}

	public void setParentTableSection(final TableSection parentTableSection) {
		this.parentTableSection = parentTableSection;
	}


	public static String getFEC_innerString(final int fecInner) {
		switch (fecInner) {
		case 0:
			return "not defined";
		case 1:
			return "1/2 conv. code rate";
		case 2:
			return "2/3 conv. code rate";
		case 3:
			return "3/4 conv. code rate";
		case 4:
			return "5/6 conv. code rate";
		case 5:
			return "7/8 conv. code rate";
		case 6:
			return "8/9 conv. code rate";
		case 7:
			return "3/5 conv. code rate";
		case 8:
			return "4/5 conv. code rate";
		case 9:
			return "9/10 conv. code rate";
		case 15:
			return "no conv. Coding";
		default:
			return "reserved for future use";
		}
	}

	public static String formatCableFrequency(final String f) {
		final StringBuilder s = new StringBuilder();
		s.append(f.substring(0, 4)).append('.').append(f.substring(4, 8)).append(" MHz");
		return Utils.stripLeadingZeros(s.toString());
	}

	public static String formatCableFrequencyList(final String f) {
		final StringBuilder s = new StringBuilder();
		s.append(f.substring(1, 4)).append('.').append(f.substring(4, 8));
		return Utils.stripLeadingZeros(s.toString());
	}

	public static String formatSatelliteFrequency(final String f) {
		final StringBuilder s = new StringBuilder();
		s.append(f.substring(0, 3)).append('.').append(f.substring(3, 8)).append(" GHz");
		return Utils.stripLeadingZeros(s.toString());
	}

	public static String formatOrbitualPosition(final String f) {
		final StringBuilder s = new StringBuilder();
		s.append(f.substring(0, 3)).append('.').append(f.substring(3, 4)).append("°");
		return Utils.stripLeadingZeros(s.toString());
	}

	public static String formatTerrestrialFrequency(final long f) {
		final StringBuilder s = new StringBuilder();
		String freq = Long.toString(f * 10);
		if (freq.length() < 7) {
			freq = "0000000".substring(freq.length()) + freq;
		}
		s.append(freq.substring(0, freq.length() - 6)).append('.').append(
				freq.substring(freq.length() - 6, freq.length())).append(" MHz");
		return Utils.stripLeadingZeros(s.toString());
	}

	public static String formatSymbolRate(final String f) {
		final StringBuilder s = new StringBuilder();
		s.append(f.substring(0, 3)).append('.').append(f.substring(3, 7)).append(" Msymbol/s");
		return Utils.stripLeadingZeros(s.toString());
	}

	public static String getServiceTypeString(final int serviceType) {

		switch (serviceType) {
		case 0x00:
			return "reserved for future use";
		case 0x01:
			return "digital television service";
		case 0x02:
			return "digital radio sound service";
		case 0x03:
			return "Teletext service";
		case 0x04:
			return "NVOD reference service";
		case 0x05:
			return "NVOD time-shifted service";
		case 0x06:
			return "mosaic service";
		case 0x07:
			return "FM radio service"; 
		case 0x08:
			return "DVB SRM service";
		case 0x09:
			return "reserved for future use";
		case 0x0A:
			return "advanced codec digital radio sound service";
		case 0x0B:
			return "advanced codec mosaic service";
		case 0x0C:
			return "data broadcast service";
		case 0x0D:
			return "reserved for Common Interface Usage (EN 50221)";
		case 0x0E:
			return "RCS Map (see EN 301 790)";
		case 0x0F:
			return "RCS FLS (see EN 301 790)";
		case 0x10:
			return "DVB MHP service";
		case 0x11:
			return "MPEG-2 HD digital television service";
		case 0x12:
			return "reserved for future use";
		case 0x13:
			return "reserved for future use";
		case 0x14:
			return "reserved for future use";
		case 0x15:
			return "reserved for future use";
		case 0x16:
			return "H.264/AVC SD digital television service";
		case 0x17:
			return "H.264/AVC SD NVOD time-shifted service";
		case 0x18:
			return "H.264/AVC SD NVOD reference service";
		case 0x19:
			return "H.264/AVC HD digital television service";
		case 0x1A:
			return "H.264/AVC HD NVOD time-shifted service";
		case 0x1B:
			return "H.264/AVC HD NVOD reference service";
		case 0x1C:
			return "H.264/AVC frame compatible plano-stereoscopic HD digital television service";
		case 0x1D:
			return "H.264/AVC frame compatible plano-stereoscopic HD NVOD time-shifted service";
		case 0x1E:
			return "H.264/AVC frame compatible plano-stereoscopic HD NVOD reference service";
		case 0x1F:
			return "HEVC digital television service";
		case 0x20:
			return "HEVC UHD digital television service with HDR and/or a frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz, or a any combination of HDR and these frame rates"; 

		case 0x84:
			return "Sagem firmware download service"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
			// -- Mandatory for
			// legacy STB (ICD3000,
			// ICD4000 and ICD60)
		case 0x87:
			return "Sagem OpenTV out_of_list_service"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
			// -- Mandatory for
			// legacy STB (ICD3000,
			// ICD4000 and ICD60)
			// Service type for VOD
			// services. Mandatory
			// for STB supporting
			// VOD.
		case 0x88:
			return "Sagem OpenTV in_list_service"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
			// -- Mandatory for legacy
			// STB (ICD3000, ICD4000 and
			// ICD60).

		default:
			if ((0x21 <= serviceType) && (serviceType <= 0x7F)) {
				return "reserved for future use";
			}

			if ((0x80 <= serviceType) && (serviceType <= 0xFE)) {
				return "user defined";
			}
			return "Illegal value";
		}

	}

	public static String getServiceTypeStringShort(final int serviceType) {

		switch (serviceType) {
		case 0x00:
			return "reserved";
		case 0x01:
			return "TV (SD)";
		case 0x02:
			return "Radio";
		case 0x03:
			return "Teletext";
		case 0x04:
			return "NVOD reference";
		case 0x05:
			return "NVOD time-shifted";
		case 0x06:
			return "mosaic";
		case 0x07:
			return "PAL coded signal"; // http://www.nordig.org/pdf/NorDig_RoOspec_0_9.pdf,
			// p.14
		case 0x08:
			return "reserved";
		case 0x09:
			return "reserved";
		case 0x0A:
			return "radio (advanced)";
		case 0x0B:
			return "mosaic advanced)";
		case 0x0C:
			return "data broadcast";
		case 0x0D:
			return "Common Interface Usage";
		case 0x0E:
			return "RCS Map";
		case 0x0F:
			return "RCS FLS";
		case 0x10:
			return "DVB MHP service";
		case 0x11:
			return "TV (HD-MPEG2)";
		case 0x12:
			return "reserved";
		case 0x13:
			return "reserved";
		case 0x14:
			return "reserved";
		case 0x15:
			return "reserved";
		case 0x16:
			return "TV (SD-MPEG4)";
		case 0x17:
			return "advanced codec SD NVOD time-shifted ";
		case 0x18:
			return "advanced codec SD NVOD reference";
		case 0x19:
			return "TV (HD-MPEG4)";
		case 0x1A:
			return "advanced codec HD NVOD time-shifted";
		case 0x1B:
			return "advanced codec HD NVOD reference";

		default:
			if ((0x1C <= serviceType) && (serviceType <= 0x7F)) {
				return "reserved";
			}

			if ((0x80 <= serviceType) && (serviceType <= 0xFE)) {
				return "user defined";
			}
			return "Illegal value";
		}

	}

	public static String getComponentDescriptorString(final int stream_content,final int stream_content_ext, final int component_type) {
		switch (stream_content) {
		case 0x00:
			return "reserved for future use";
		case 0x01:
			return getComponentType0x01String(component_type);
		case 0x02:
			return getComponentType0x02String(component_type);
		case 0x03:
			return getComponentType0x03String(component_type);
		case 0x04:
			if (component_type < 0x7F) {
				return "reserved for AC-3 audio modes: " + AC3Descriptor.getComponentTypeString(component_type);
			}
			return "reserved for enhanced AC-3 audio modes: " + AC3Descriptor.getComponentTypeString(component_type);
		case 0x05:
			return getComponentType0x05String(component_type);
		case 0x06:
			return getComponentType0x06String(component_type);
		case 0x07:
			if (component_type < 0x7F) {
				return "reserved for DTS audio modes"; // TODO
			}
			return "reserved for future use";
		case 0x08:
			if (component_type == 0x00) {
				return "reserved for future use";
			} else if (component_type == 0x01) {
				return "DVB SRM data";
			}{
				return "reserved for DVB CPCM modes";
			}
		case 0x09:
			return getComponentType0x09String(stream_content_ext,component_type);
		case 0x0b:
			if(stream_content_ext==0xE){
				return getNextGenerationAudioComponentTypeString(component_type);
			}
			else if(stream_content_ext==0xf){
				if (component_type == 0x00) {
					return "less than 16:9 aspect ratio";
				} else if (component_type == 0x01) {
					return "16:9 aspect ratio";
				} else if (component_type == 0x02) {
					return "greater than 16:9 aspect ratio";
				} else if (component_type == 0x03) {
					return "plano-stereoscopic top and\r\n" +
							"bottom (TaB) frame-packing";
				} else if (component_type == 0x04) {
					return "HLG10 HDR";
				} else if (component_type == 0x05) {
					return "HEVC temporal video subset for a frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz";
				} else if (component_type == 0x06) {
					return "SMPTE ST 2094-10 DMI format";
				} else if (component_type == 0x07) {
					return "SL-HDR2 DMI format";
				} else if (component_type == 0x08) {
					return "SMPTE ST 2094-40 DMI format";
			}else{
					return "reserved for future use";
				}
			}else{
				return "reserved for future use";
			}
		default:
			if (stream_content < 0x0B) {
				return "reserved for future use";
			}
			return "user defined";
		}
	}

	/**
	 * @param stream_content_ext
	 * @param component_type
	 * @return
	 */
	public static String getComponentType0x09String(final int stream_content_ext, final int component_type) {
		switch(stream_content_ext){
		case 0x00:
			switch(component_type){
			case 0x00:
				return "HEVC Main Profile high definition video, 50 Hz";
			case 0x01:
				return "HEVC Main 10 Profile high definition video, 50 Hz";
			case 0x02:
				return "HEVC Main Profile high definition video, 60 Hz";
			case 0x03:
				return "HEVC Main 10 Profile high definition video, 60 Hz";
			case 0x04:
				return "HEVC ultra high definition video";
			case 0x05:
				return "HEVC ultra high definition video with PQ10 HDR with a frame rate lower than or equal to 60 Hz";
			case 0x06:
				return "HEVC ultra high definition video, frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz without a half frame rate HEVC temporal video sub-bitstream";
			case 0x07:
				return "HEVC ultra high definition video with PQ10 HDR, frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz without a half frame rate HEVC temporal video sub-bit-stream";
			default:
				return "reserved for future use";
			}
		case 0x01:
			switch(component_type){
			case 0x00:
				return "AC-4 main audio, mono";
			case 0x01:
				return "AC-4 main audio, mono, dialogue enhancement enabled";
			case 0x02:
				return "AC-4 main audio, stereo";
			case 0x03:
				return "AC-4 main audio, stereo, dialogue enhancement enabled";
			case 0x04:
				return "AC-4 main audio, multichannel";
			case 0x05:
				return "AC-4 main audio, multichannel, dialogue enhancement enabled";
			case 0x06:
				return "AC-4 broadcast-mix audio description, mono, for the visually impaired";
			case 0x07:
				return "AC-4 broadcast-mix audio description, mono, for the visually impaired, dialogue enhancement enabled";
			case 0x08:
				return "AC-4 broadcast-mix audio description, stereo, for the visually impaired";
			case 0x09:
				return "AC-4 broadcast-mix audio description, stereo, for the visually impaired, dialogue enhancement enabled";
			case 0x0a:
				return "AC-4 broadcast-mix audio description, multichannel, for the visually impaired";
			case 0x0b:
				return "AC-4 broadcast-mix audio description, multichannel, for the visually impaired, dialogue enhancement enabled";
			case 0x0c:
				return "AC-4 receiver-mix audio description, mono, for the visually impaired";
			case 0x0d:
				return "AC-4 receiver-mix audio description, stereo, for the visually impaired";
				// see a038_dvb_spec_december_2017_pdf.pdf
			case 0x0E: 
				return "AC-4 Part-2";
			case 0x0F: 
				return "MPEG-H Audio LC Profile";
			case 0x10:
				return "DTS-UHD main audio, mono";
			case 0x11:
				return "DTS-UHD main audio, mono, dialogue enhancement enabled ";
			case 0x12:
				return "DTS-UHD main audio, stereo";
			case 0x13:
				return "DTS-UHD main audio, stereo, dialogue enhancement enabled";
			case 0x14:
				return "DTS-UHD main audio, multichannel";
			case 0x15:
				return "DTS-UHD main audio, multichannel, dialogue enhancement enabled";
			case 0x16:
				return "DTS-UHD broadcast-mix audio description, mono, for the visually impaired ";
			case 0x17:
				return "DTS-UHD broadcast-mix audio description, mono, for the visually impaired, dialogue enhancement enabled";
			case 0x18:
				return "DTS-UHD broadcast-mix audio description, stereo, for the visually impaired";
			case 0x19:
				return "DTS-UHD broadcast-mix audio description, stereo, for the visually impaired, dialogue enhancement enabled";
			case 0x1a:
				return "DTS-UHD broadcast-mix audio description, multichannel, for the visually impaired";
			case 0x1b:
				return "DTS-UHD broadcast-mix audio description, multichannel, for the visually impaired, dialogue enhancement enabled ";
			case 0x1c:
				return "DTS-UHD receiver-mix audio description, mono, for the visually impaired ";
			case 0x1d:
				return "DTS-UHD receiver-mix audio description, stereo, for the visually impaired ";
			case 0x1e:
				return "DTS-UHD NGA Audio";
			default:
				return "reserved for future use";
			}
		case 0x02:
			// see a038_dvb_spec_december_2017_pdf.pdf
			return "TTML subtitles";
			

		}
		return "reserved for future use";
	}
	
	/**
	 * based on Table 27: Next generation audio component_type value assignments
	 * ETSI EN 300 468 V1.16.1 (2019-08) p.53
	 * @param component_type
	 * @return
	 */
	public static String getNextGenerationAudioComponentTypeString(final int component_type) {
		StringBuilder res = new StringBuilder();
		if((component_type & 0b0100_0000) != 0) {
			res.append("content is pre-rendered for consumption with headphones, ");
		}
		if((component_type & 0b0010_0000) != 0) {
			res.append("content enables interactivity, ");
		}
		if((component_type & 0b0001_0000) != 0) {
			res.append("content enables dialogue enhancement, ");
		}
		if((component_type & 0b0000_1000) != 0) {
			res.append("content contains spoken subtitles, ");
		}
		if((component_type & 0b0000_0100) != 0) {
			res.append("ccontent contains audio description, ");
		}
		res.append("preferred reproduction channel layout: ");
		switch (component_type & 0b0000_0011) {
		case 0b00:
			res.append("no preference");
			break;
		case 0b01:
			res.append("stereo");
			break;
		case 0b10:
			res.append("two-dimensional");
			break;
		case 0b11:
			res.append("three-dimensional");
			break;

		default:
			res.append("Illegal value");
			break;
		}
		return res.toString();
	}

	public static String getComponentType0x01String(final int component_type) {
		switch (component_type) {
		case 0x00:
			return "reserved for future use";
		case 0x01:
			return "MPEG-2 video, 4:3 aspect ratio, 25 Hz";
		case 0x02:
			return "MPEG-2 video, 16:9 aspect ratio with pan vectors, 25 Hz";
		case 0x03:
			return "MPEG-2 video, 16:9 aspect ratio without pan vectors, 25 Hz";
		case 0x04:
			return "MPEG-2 video, > 16:9 aspect ratio, 25 Hz";
		case 0x05:
			return "MPEG-2 video, 4:3 aspect ratio, 30 Hz";
		case 0x06:
			return "MPEG-2 video, 16:9 aspect ratio with pan vectors, 30 Hz";
		case 0x07:
			return "MPEG-2 video, 16:9 aspect ratio without pan vectors, 30 Hz";
		case 0x08:
			return "MPEG-2 video, > 16:9 aspect ratio, 30 Hz";
		case 0x09:
			return "MPEG-2 high definition video, 4:3 aspect ratio, 25 Hz";
		case 0x0A:
			return "MPEG-2 high definition video, 16:9 aspect ratio with pan vectors, 25 Hz";
		case 0x0B:
			return "MPEG-2 high definition video, 16:9 aspect ratio without pan vectors, 25 Hz";
		case 0x0C:
			return "MPEG-2 high definition video, > 16:9 aspect ratio, 25 Hz";
		case 0x0D:
			return "MPEG-2 high definition video, 4:3 aspect ratio, 30 Hz";
		case 0x0E:
			return "MPEG-2 high definition video, 16:9 aspect ratio with pan vectors, 30 Hz";
		case 0x0F:
			return "MPEG-2 high definition video, 16:9 aspect ratio without pan vectors, 30 Hz";
		case 0x10:
			return "MPEG-2 high definition video, > 16:9 aspect ratio, 30 Hz";
		case 0xFF:
			return "reserved for future use";

		default:
			if ((0x11 <= component_type) && (component_type <= 0xAF)) {
				return "reserved for future use";
			}
			if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
				return "user defined";
			}
			return "Illegal value";

		}
	}

	public static String getComponentType0x02String(final int component_type) {
		switch (component_type) {
		case 0x00:
			return "reserved for future use";
		case 0x01:
			return "MPEG-1 Layer 2 audio, single mono channel";
		case 0x02:
			return "MPEG-1 Layer 2 audio, dual mono channel";
		case 0x03:
			return "MPEG-1 Layer 2 audio, stereo (2 channel)";
		case 0x04:
			return "MPEG-1 Layer 2 audio, multi-lingual, multi-channel";
		case 0x05:
			return "MPEG-1 Layer 2 audio, surround sound";
		case 0x40:
			return "MPEG-1 Layer 2 audio description for the visually impaired";
		case 0x41:
			return "MPEG-1 Layer 2 audio for the hard of hearing";
		case 0x42:
			return "receiver-mixed supplementary audio as per annex E of TS 101 154";
		case 0x47:
			return "MPEG-1 Layer 2 audio, receiver-mix audio description";
		case 0x48:
			return "MPEG-1 Layer 2 audio, broadcast-mix audio description";
		case 0xFF:
			return "reserved for future use";

		default:
			if ((0x06 <= component_type) && (component_type <= 0x3F)) {
				return "reserved for future use";
			}
			if ((0x43 <= component_type) && (component_type <= 0xAF)) {
				return "reserved for future use";
			}
			if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
				return "user defined";
			}
			return "Illegal value";

		}
	}

	public static String getComponentType0x03String(final int component_type) {
		switch (component_type) {
		case 0x00:
			return "reserved for future use";
		case 0x01:
			return "EBU Teletext subtitles";
		case 0x02:
			return "associated EBU Teletext";
		case 0x03:
			return "VBI data";
		case 0x10:
			return "DVB subtitles (normal) with no monitor aspect ratio criticality";
		case 0x11:
			return "DVB subtitles (normal) for display on 4:3 aspect ratio monitor";
		case 0x12:
			return "DVB subtitles (normal) for display on 16:9 aspect ratio monitor";
		case 0x13:
			return "DVB subtitles (normal) for display on 2.21:1 aspect ratio monitor";
		case 0x14:
			return "DVB subtitles (normal) for display on a high definition monitor";
		case 0x15:
			return "DVB subtitles (normal) with plano-stereoscopic disparity for display on a high definition monitor";
		case 0x16:
			return "DVB subtitles (normal) for display on an ultra high definition monitor";
		case 0x20:
			return "DVB subtitles (for the hard of hearing) with no monitor aspect ratio criticality";
		case 0x21:
			return "DVB subtitles (for the hard of hearing) for display on 4:3 aspect ratio monitor";
		case 0x22:
			return "DVB subtitles (for the hard of hearing) for display on 16:9 aspect ratio monitor";
		case 0x23:
			return "DVB subtitles (for the hard of hearing) for display on 2.21:1 aspect ratio monitor";
		case 0x24:
			return "DVB subtitles (for the hard of hearing) for display on a high definition monitor";
		case 0x25:
			return "DVB subtitles (for the hard of hearing) with planostereoscopic disparity for display on a high definition monitor";
		case 0x26:
			return "DVB subtitles (for the hard of hearing) for display on an ultra high definition monitor";
		case 0x30:
			return "Open (in-vision) sign language interpretation for the deaf";
		case 0x31:
			return "Closed sign language interpretation for the deaf";
		case 0x40:
			return "video up-sampled from standard definition source material";
		case 0x41:
			return "Video is standard dynamic range (SDR)";
		case 0x42:
			return "Video is high dynamic range (HDR) remapped from standard dynamic range (SDR) source material";
		case 0x43:
			return "Video is high dynamic range (HDR) up-converted from standard dynamic range (SDR) source material";
		case 0x44:
			return "Video is standard frame rate, less than or equal to 60 Hz"; 
		case 0x45:
			return "High frame rate video generated from lower frame rate source material";
		case 0x80:
			return "dependent SAOC-DE data stream";
		case 0xFF:
			return "reserved for future use";

		default:
			if ((0x04 <= component_type) && (component_type <= 0x0F)) {
				return "reserved for future use";
			}
			if ((0x15 <= component_type) && (component_type <= 0x1F)) {
				return "reserved for future use";
			}
			if ((0x25 <= component_type) && (component_type <= 0x2F)) {
				return "reserved for future use";
			}
			if ((0x32 <= component_type) && (component_type <= 0xAF)) {
				return "reserved for future use";
			}
			if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
				return "user defined";
			}
			return "Illegal value";

		}
	}

	public static String getComponentType0x05String(final int component_type) {
		switch (component_type) {
		case 0x00:
			return "reserved for future use";
		case 0x01:
			return "H.264/AVC standard definition video, 4:3 aspect ratio, 25 Hz";
		case 0x02:
			return "reserved for future use";
		case 0x03:
			return "H.264/AVC standard definition video, 16:9 aspect ratio, 25 Hz";
		case 0x04:
			return "H.264/AVC standard definition video, > 16:9 aspect ratio, 25 Hz";
		case 0x05:
			return "H.264/AVC standard definition video, 4:3 aspect ratio, 30 Hz";
		case 0x06:
			return "reserved for future use";
		case 0x07:
			return "H.264/AVC standard definition video, 16:9 aspect ratio, 30 Hz";
		case 0x08:
			return "H.264/AVC standard definition video, > 16:9 aspect ratio, 30 Hz";
		case 0x0B:
			return "H.264/AVC high definition video, 16:9 aspect ratio, 25 Hz";
		case 0x0C:
			return "H.264/AVC high definition video, > 16:9 aspect ratio, 25 Hz";
		case 0x0F:
			return "H.264/AVC high definition video, 16:9 aspect ratio, 30 Hz";
		case 0x10:
			return "H.264/AVC high definition video, > 16:9 aspect ratio, 30 Hz";
		case 0x80:
			return "H.264/AVC planostereoscopic frame compatible high definition video, 16:9 aspect ratio, 25 Hz, Side-by-Side";
		case 0x81:
			return "H.264/AVC planostereoscopic frame compatible high definition video, 16:9 aspect ratio, 25 Hz, Top-and-Bottom";
		case 0x82:
			return "H.264/AVC planostereoscopic frame compatible high definition video, 16:9 aspect ratio, 30 Hz, Side-by-Side";
		case 0x83:
			return "H.264/AVC stereoscopic frame compatible high definition video, 16:9 aspect ratio, 30 Hz, Top-and-Bottom";
		case 0x84:
			return "H.264/MVC dependent view, plano-stereoscopic service compatible video";
		case 0xFF:
			return "reserved for future use";

		default:
			if ((0x09 <= component_type) && (component_type <= 0x0A)) {
				return "reserved for future use";
			}
			if ((0x0D <= component_type) && (component_type <= 0x0E)) {
				return "reserved for future use";
			}
			if ((0x11 <= component_type) && (component_type <= 0xAF)) {
				return "reserved for future use";
			}
			if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
				return "user defined";
			}
			return "Illegal value";

		}
	}

	public static String getComponentType0x06String(final int component_type) {
		switch (component_type) {
		case 0x00:
			return "reserved for future use";
		case 0x01:
			return "HE-AAC audio, single mono channel";
		case 0x02:
			return "reserved for future use";
		case 0x03:
			return "HE-AAC audio, stereo";
		case 0x04:
			return "reserved for future use";
		case 0x05:
			return "HE-AAC audio, surround sound";
		case 0x40:
			return "HE-AAC audio description for the visually impaired";
		case 0x41:
			return "HE-AAC audio for the hard of hearing";
		case 0x42:
			return "HE-AAC receiver-mixed supplementary audio as per annex E of TS 101 154 [10]";
		case 0x43:
			return "HE-AAC v2 audio, stereo";
		case 0x44:
			return "HE-AAC v2 audio description for the visually impaired";
		case 0x45:
			return "HE-AAC v2 audio for the hard of hearing";
		case 0x46:
			return "HE-AAC v2 receiver-mixed supplementary audio as per annex E of TS 101 154 [10]";
		case 0x47:
			return "HE-AAC receiver mix audio description for the visually impaired";
		case 0x48:
			return "HE-AAC broadcaster mix audio description for the visually impaired";
		case 0x49:
			return "HE-AAC v2 receiver mix audio description for the visually impaired";
		case 0x4A:
			return "HE-AAC v2 broadcaster mix audio description for the visually impaired";
		case 0xA0:
			return "HE AAC, or HE AAC v2 with SAOC-DE ancillary data";
		case 0xFF:
			return "reserved for future use";

		default:
			if ((0x06 <= component_type) && (component_type <= 0x3F)) {
				return "reserved for future use";
			}
			if ((0x4B <= component_type) && (component_type <= 0xAF)) {
				return "reserved for future use";
			}
			if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
				return "user defined";
			}
			return "Illegal value";

		}
	}


	/**
	 * @param descriptorList List of descriptors to be searched
	 * @param u class of the descriptors to be found
	 * @return List off all descriptors matching u
	 */
	@SuppressWarnings("unchecked")
	public static <U extends Descriptor> List<U> findGenericDescriptorsInList(final List<? extends Descriptor> descriptorList, final Class<U> u ) {

		final List<U> result = new ArrayList<>();
		for (final Descriptor element : descriptorList) {
			if (element.getClass().equals(u)) {
				result.add((U) element);
			}
		}
		return result;
	}

	/**
	 * Finds first descriptor of type u in descriptorList, and applies Function fun to it. Returns null when no
	 * descriptor of type u is found
	 *  
	 * @param <U>
	 * @param descriptorList
	 * @param u
	 * @param fun
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <U extends Descriptor> Object findDescriptorApplyFunc(final List<Descriptor> descriptorList, final Class<U> u , Function<U, Object> fun) {
		for (final Descriptor element : descriptorList) {
			if (element.getClass().equals(u)) {
				return fun.apply((U) element);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <U extends Descriptor> List<Object> findDescriptorApplyListFunc(final List<Descriptor> descriptorList, final Class<U> u , Function<U, List<Object>> fun) {
		for (final Descriptor element : descriptorList) {
			if (element.getClass().equals(u)) {
				return fun.apply((U) element);
			}
		}
		return null;
	}
	
	/**
	 * Get description, as defined in Table 2-71 – MPEG-4_audio_profile_and_level assignment values ISO/IEC 13818-1:2013
	 *
	 * Used for MPEG-4 audio descriptor and AAC_descriptor
	 *
	 * @param profile_and_level
	 * @return
	 */
	public static String getProfileLevelString(final int profile_and_level) {
		switch (profile_and_level) {

		case 0x10:
			return "Main profile, level 1";
		case 0x11:
			return "Main profile, level 2";
		case 0x12:
			return "Main profile, level 3";
		case 0x13:
			return "Main profile, level 4";
		case 0x18:
			return "Scalable Profile, level 1";
		case 0x19:
			return "Scalable Profile, level 2";
		case 0x1A:
			return "Scalable Profile, level 3";
		case 0x1B:
			return "Scalable Profile, level 4";
		case 0x20:
			return "Speech profile, level 1";
		case 0x21:
			return "Speech profile, level 2";
		case 0x28:
			return "Synthesis profile, level 1";
		case 0x29:
			return "Synthesis profile, level 2";
		case 0x2A:
			return "Synthesis profile, level 3";
		case 0x30:
			return "High quality audio profile, level 1";
		case 0x31:
			return "High quality audio profile, level 2";
		case 0x32:
			return "High quality audio profile, level 3";
		case 0x33:
			return "High quality audio profile, level 4";
		case 0x34:
			return "High quality audio profile, level 5";
		case 0x35:
			return "High quality audio profile, level 6";
		case 0x36:
			return "High quality audio profile, level 7";
		case 0x37:
			return "High quality audio profile, level 8";
		case 0x38:
			return "Low delay audio profile, level 1";
		case 0x39:
			return "Low delay audio profile, level 2";
		case 0x3A:
			return "Low delay audio profile, level 3";
		case 0x3B:
			return "Low delay audio profile, level 4";
		case 0x3C:
			return "Low delay audio profile, level 5";
		case 0x3D:
			return "Low delay audio profile, level 6";
		case 0x3E:
			return "Low delay audio profile, level 7";
		case 0x3F:
			return "Low delay audio profile, level 8";
		case 0x40:
			return "Natural audio profile, level 1";
		case 0x41:
			return "Natural audio profile, level 2";
		case 0x42:
			return "Natural audio profile, level 3";
		case 0x43:
			return "Natural audio profile, level 4";
		case 0x48:
			return "Mobile audio internetworking profile, level 1";
		case 0x49:
			return "Mobile audio internetworking profile, level 2";
		case 0x4A:
			return "Mobile audio internetworking profile, level 3";
		case 0x4B:
			return "Mobile audio internetworking profile, level 4";
		case 0x4C:
			return "Mobile audio internetworking profile, level 5";
		case 0x4D:
			return "Mobile audio internetworking profile, level 6";
		case 0x50:
			return "AAC profile, level 1";
		case 0x51:
			return "AAC profile, level 2";
		case 0x52:
			return "AAC profile, level 4";
		case 0x53:
			return "AAC profile, level 5";
		case 0x58:
			return "High efficiency AAC profile, level 2";
		case 0x59:
			return "High efficiency AAC profile, level 3";
		case 0x5A:
			return "High efficiency AAC profile, level 4";
		case 0x5B:
			return "High efficiency AAC profile, level 5";
		case 0x60:
			return "High efficiency AAC v2 profile, level 2";
		case 0x61:
			return "High efficiency AAC v2 profile, level 3";
		case 0x62:
			return "High efficiency AAC v2 profile, level 4";
		case 0x63:
			return "High efficiency AAC v2 profile, level 5";

		case 0xFF:
			return "Audio profile and level not specified by the MPEG-4_audio_profile_and_level " +
			"field in this descriptor";
		default:
			return "Reserved";

		}
	}

	protected static String getMPEGCarriageFlagsString(final int mPEG_carriage_flags) {
		return mpeg_carriage_flags_list.get(mPEG_carriage_flags);
	}

	protected static String getMetaDataApplicationFormatString(final int metadata_application_format) {
		return metadata_application_format_list.get(metadata_application_format);
	}

	public static String getMetaDataFormatString(final int metadata_format) {
		return metadata_format_list.get(metadata_format);
	}
}
