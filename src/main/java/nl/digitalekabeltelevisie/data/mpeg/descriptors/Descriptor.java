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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static java.lang.Byte.toUnsignedInt;
import static nl.digitalekabeltelevisie.util.Utils.stripLeadingZeros;

import java.util.*;
import java.util.function.Function;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.gui.TableSource;
import nl.digitalekabeltelevisie.util.*;

/**
 * @author Eric Berendsen
 *
 */
public class Descriptor implements TreeNode {

	private static final LookUpList metadata_application_format_list = new LookUpList.Builder().
			add(0x0000,0x000F,"Reserved").
			add(0x0010,"ISO 15706 (ISAN) encoded in its binary form").
			add(0x0011, "ISO 15706-2 (V-ISAN) encoded in its binary form").
			add(0x0012,0x00FF,"Reserved").
			add(0x0100,"metadata service contains TVA metadata as profiled according to DVB").
			add(0x0101,"metadata contained conforms to DTG D-Book Record List.").
			add(0x0102,0xFFFE,"User defined").
			add(0xFFFF,"Defined by the metadata_application_format_identifier field").
			build();
	private static final LookUpList mpeg_carriage_flags_list = new LookUpList.Builder().
			add(0,"Carriage in the same transport stream where this metadata pointer descriptor is carried.").
			add(1,"Carriage in a different transport stream from where this metadata pointer descriptor is carried.").
			add(2,"Carriage in a program stream. This may or may not be the same program stream in which this metadata pointer descriptor is carried.").
			add(3,"may be used if there is no relevant metadata carried on the DVB network. In this case the metadata locator record shall be present").
			build();
	private static final LookUpList metadata_format_list = new LookUpList.Builder().
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
	protected final int			descriptorTag;
	protected final int			descriptorLength;

	protected final byte[]	privateData;
	private final int		descriptorOffset;
	protected int			privateDataOffset;

	protected final TableSection	parentTableSection;


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public Descriptor(final byte[] b, final int offset, final TableSection parent) {
		privateData = b;
		descriptorOffset = offset;
		privateDataOffset = offset + 2;

		descriptorTag = toUnsignedInt(b[offset]);
		descriptorLength = toUnsignedInt(b[offset + 1]);
		parentTableSection = parent;
	}

	public int getDescriptorLength() {
		return descriptorLength;
	}

	public int getDescriptorTag() {
		return descriptorTag;
	}

	/**
	 * This method should be overridden by private descriptors where name/meaning is dependent on preceding Private Data Specifier Descriptor
	 * @return the (default)name for this descriptor
	 */
	public String getDescriptorname() {
		return getDescriptorname(descriptorTag, parentTableSection);
	}

	public static String getDescriptorname(final int tag, final TableSection tableSection) {

		return switch (tag) {
			case 0 -> "Reserved";
			case 1 -> "Reserved";
			case 2 -> "video_stream_descriptor";
			case 3 -> "audio_stream_descriptor";
			case 4 -> "hierarchy_descriptor";
			case 5 -> "registration_descriptor";
			case 6 -> "data_stream_alignment_descriptor";
			case 7 -> "target_background_grid_descriptor";
			case 8 -> "Video_window_descriptor";
			case 9 -> "CA_descriptor";
			case 10 -> "ISO_639_language_descriptor";
			case 11 -> "System_clock_descriptor";
			case 12 -> "Multiplex_buffer_utilization_descriptor";
			case 13 -> "Copyright_descriptor";
			case 14 -> "Maximum_bitrate_descriptor";
			case 15 -> "Private_data_indicator_descriptor";
			case 16 -> "Smoothing_buffer_descriptor";
			case 17 -> "STD_descriptor";
			case 18 -> "IBP_descriptor";

			// From DVBSnoop mpeg_descriptor.c 1.4.50

			/* 0x13 - 0x1A DSM-CC ISO13818-6, TR 102 006 */
			case 0x13 -> "DSM-CC Carousel_Identifier_descriptor";
			case 0x14 -> "DSM-CC Association_tag_descriptor";
			case 0x15 -> "DSM-CC Deferred_Association_tags_descriptor";

			/* DSM-CC stream descriptors */
			// case 0x16: reserved....
			case 0x17 -> "NPT_reference_descriptor";
			case 0x18 -> "NPT_endpoint_descriptor";
			case 0x19 -> "stream_mode_descriptor";
			case 0x1A -> "stream_event_descriptor";

			/* MPEG 4 */
			case 0x1B -> "MPEG4_video_descriptor";
			case 0x1C -> "MPEG4_audio_descriptor";
			case 0x1D -> "IOD_descriptor";
			case 0x1E -> "SL_descriptor";
			case 0x1F -> "FMC_descriptor";
			case 0x20 -> "External_ES_ID_descriptor";
			case 0x21 -> "MuxCode_descriptor";
			case 0x22 -> "FMXBufferSize_descriptor";
			case 0x23 -> "MultiplexBuffer_descriptor";
			case 0x24 -> "ContentLabeling_descriptor";

			/* TV ANYTIME, TS 102 323 */
			case 0x25 -> "metadata_pointer_descriptor";
			case 0x26 -> "metadata_descriptor";
			case 0x27 -> "metadata_STD_descriptor";

			/* H.222.0 AMD 3 */
			/* http://neuron2.net/library/avc/T-REC-H%5B1%5D.222.0-200403-I!Amd3!PDF-E.pdf */
			case 0x28 -> "AVC_video_descriptor";
			case 0x29 -> "IPMP_descriptor";
			case 0x2A -> "AVC_timing_and_HRD_descriptor";

			/* H.222.0 Corr 4 */
			case 0x2B -> "MPEG2_AAC_audio_descriptor";
			case 0x2C -> "FlexMuxTiming_descriptor";

			/* ISO/IEC 13818-1:2007/FPDAM5 */
			case 0x2D -> "MPEG-4_text_descriptor";
			case 0x2E -> "MPEG-4_audio_extension_descriptor";
			case 0x2F -> "Auxiliary_video_stream_descriptor";
			case 0x30 -> "SVC extension descriptor";
			case 0x31 -> "MVC extension descriptor";

			/* ISO/IEC 13818-1:2007/FPDAM5 - Transport of JPEG 2000 part 1 video */
			case 0x32 -> "J2K video descriptor";

			/* Rec. ITU-T H.222.0 (06/2012) */
			case 51 -> "MVC operation point descriptor";
			case 52 -> "MPEG2_stereoscopic_video_format_descriptor";
			case 53 -> "Stereoscopic_program_info_descriptor";
			case 54 -> "Stereoscopic_video_info_descriptor";

			/* Rec. ITU-T H.222.0 (10/2014) */
			case 55 -> "Transport_profile_descriptor";
			case 56 -> "HEVC video descriptor";

			// Rec. ITU-T H.222.0 (06/2021)
			case 57 -> "VVC video descriptor";
			case 58 -> "EVC video descriptor";
			case 63 -> "Extension_descriptor";

			// DVB

			case 0x40 -> "network_name_descriptor";
			case 0x41 -> "service_list_descriptor";
			case 0x42 -> "stuffing_descriptor";
			case 0x43 -> "satellite_delivery_system_descriptor";
			case 0x44 -> "cable_delivery_system_descriptor";
			case 0x45 -> "VBI_data_descriptor";
			case 0x46 -> "VBI_teletext_descriptor";
			case 0x47 -> "bouquet_name_descriptor";
			case 0x48 -> "service_descriptor";
			case 0x49 -> "country_availability_descriptor";
			case 0x4A -> "linkage_descriptor";
			case 0x4B -> "NVOD_reference_descriptor";
			case 0x4C -> "time_shifted_service_descriptor";
			case 0x4D -> "short_event_descriptor";
			case 0x4E -> "extended_event_descriptor";
			case 0x4F -> "time_shifted_event_descriptor";
			case 0x50 -> "component_descriptor";
			case 0x51 -> "mosaic_descriptor";
			case 0x52 -> "stream_identifier_descriptor";
			case 0x53 -> "CA_identifier_descriptor";
			case 0x54 -> "content_descriptor";
			case 0x55 -> "parental_rating_descriptor";
			case 0x56 -> "teletext_descriptor";
			case 0x57 -> "telephone_descriptor";
			case 0x58 -> "local_time_offset_descriptor";
			case 0x59 -> "subtitling_descriptor";
			case 0x5A -> "terrestrial_delivery_system_descriptor";
			case 0x5B -> "multilingual_network_name_descriptor";
			case 0x5C -> "multilingual_bouquet_name_descriptor";
			case 0x5D -> "multilingual_service_name_descriptor";
			case 0x5E -> "multilingual_component_descriptor";
			case 0x5F -> "private_data_specifier_descriptor";
			case 0x60 -> "service_move_descriptor";
			case 0x61 -> "short_smoothing_buffer_descriptor";
			case 0x62 -> "frequency_list_descriptor";
			case 0x63 -> "partial_transport_stream_descriptor";
			case 0x64 -> "data_broadcast_descriptor";
			case 0x65 -> "scrambling_descriptor";
			case 0x66 -> "data_broadcast_id_descriptor";
			case 0x67 -> "transport_stream_descriptor";
			case 0x68 -> "DSNG_descriptor";
			case 0x69 -> "PDC_descriptor";
			case 0x6A -> "AC-3_descriptor";
			case 0x6B -> "ancillary_data_descriptor";
			case 0x6C -> "cell_list_descriptor";
			case 0x6D -> "cell_frequency_link_descriptor";
			case 0x6E -> "announcement_support_descriptor";
			case 0x6F -> "application_signalling_descriptor";
			case 0x70 -> "adaptation_field_data_descriptor";
			case 0x71 -> "service_identifier_descriptor";
			case 0x72 -> "service_availability_descriptor";
			case 0x73 -> "default_authority_descriptor";
			case 0x74 -> "related_content_descriptor";
			case 0x75 -> "TVA_id_descriptor";
			case 0x76 -> "content_identifier_descriptor/TV-Anytime serial recordings descriptor"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
			case 0x77 -> "time_slice_fec_identifier_descriptor";
			case 0x78 -> "ECM_repetition_rate_descriptor";
			case 0x79 -> "S2_satellite_delivery_system_descriptor";
			case 0x7A -> "enhanced_AC-3_descriptor";
			case 0x7B -> "DTS descriptor";
			case 0x7C -> "AAC descriptor";
			case 0x7D -> "XAIT location descriptor";
			case 0x7E -> "FTA_content_management_descriptor";
			case 0x7F -> "extension descriptor";
			case 0x81 -> "user defined: UPC logic_channel_descriptor/ATSC AC-3 audio descriptor";
			/* http://www.nordig.org/pdf/NorDig_RoOspec_0_9.pdf */
			case 0x82 -> "user defined: Viasat private: Logic_channel_dscriptor";
			case 0x83 -> "user defined: EACEM Logic_channel_descriptor / NorDig private: Logic_channel_descriptor version 1 / DTG logical_channel_descriptor";
			case 0x84 -> "user defined: EACEM Preferred_name_list_descriptor / DTG preferred_name_list_descriptor";
			case 0x85 -> "user defined: EACEM Preferred_name_identifier_descriptor / DTG preferred_name_identifier_descriptor";
			case 0x86 -> "user defined: EACEM stream_identifier_descriptor / DTG service_attribute_descriptor";
			case 0x87 -> "user defined: Ziggo/OpenTV Video On Demand delivery descriptor / NORDIG Logical_channel_descriptor version 2 / DTG short_service_name_descriptor";
			case 0x88 -> "user defined: EACEM private: HD_simulcast_logical_channel_descriptor / YOUSEE Event tag descriptor / DTG HD_simulcast_logical_channel_descriptor/ hdmv_copy_control_descriptor";
			// http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf


			case 0x89 -> "user defined: OpenTV private descriptor / DTG guidance_descriptor"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
			case 0x8A -> "user defined: SCTE-35 Cue Identifier Descriptor "; //http://www.scte.org/documents/pdf/Standards/ANSI_SCTE%2035%202014.pdf
			case 0x8C -> "user defined: VodaphoneZiggo / UPC blackout_descriptor"; // Wholesale kabel toegang Referentieaanbod VodafoneZiggo PSI/SI overview Annex behorende bij Bijlage 1: Technische specificaties
			case 0x90 -> "user defined: OpenTV module_track_descriptor";// http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
			case 0x92 -> "user defined: Extended location ID (YOUSEE)";// http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
			case 0x93 -> "user defined: Ziggo/OpenTV Video On Demand URL";

			// ANSI/SCTE 128 2010-a AVC Video Systems and Transport Constraints for Cable Television
			// https://www.scte.org/documents/pdf/Standards/ANSI_SCTE%20128%202010-a.pdf
			// 6.3.2.3 SCTE Adaptation field data descriptor
			case 0x97 -> "user defined: SCTE adaptation field data descriptor";
			case 0xA0 -> "user defined: NorDig Content Protection Descriptor";

			// https://professional.dolby.com/siteassets/pdfs/dolby-vision-bitstreams-in-mpeg-2-transport-stream-multiplex-v1.2.pdf
			case 0xB0 -> "user defined: DOVI_video_stream_descriptor";
			case 0xCE -> "user defined: CI Protection Descriptor";
			case 0xD4 -> "user defined: Ziggo Package Descriptor";


			// OpenCable™ Specifications Encoder Boundary Point Specification 			OC-SP-EBP-I01-130118
			// https://specification-search.cablelabs.com/encoder-boundary-point-specification

			case 0xE9 -> "user defined: SCTE EBP_descriptor";

			/* http://www.nordig.org/pdf/NorDig_RoOspec_0_9.pdf */
			case 0xF1 -> "user defined: Senda private: Channel_list_descriptor";
			case 0xFE -> "user defined: OpenTV track_tag_descriptor"; //
			case 0xFF -> "forbidden";
			default -> {
				if ((19 <= tag) && (tag <= 26)) {
					yield "Defined in ISO/IEC 13818-6";
				}
				if (tag <= 63) {
					yield "ITU-T Rec. H.222.0 | ISO/IEC 13818-1 Reserved";
				}
				if (tag <= 0xFE) {
					yield "user defined";
				}
				yield "illegal descriptor tag value";
			}
		};
	}

	public PID getParentPID() {
		return parentTableSection.getParentPID();
	}

	public TransportStream getParentTransportStream() {
		return getParentPID().getParentTransportStream();
	}

	public PSI getPSI() {
		return getParentTransportStream().getPsi();
	}

	public String getRawDataString() {
		return "0x" + Utils.toHexString(privateData, privateDataOffset, descriptorLength) + " \"" +
				Utils.toSafeString(privateData, privateDataOffset, descriptorLength) + "\"";
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		return getJTreeNode(modus, null);

	}
	
	public DefaultMutableTreeNode getJTreeNode(final int modus, TableSource tableSource) {

		final KVP kvp = new KVP("Descriptor: " + getDescriptorname(), descriptorTag, null);
		kvp.setTableSource(tableSource);
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvp);

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
		if ((getClass().equals(Descriptor.class)) || (!Utils.simpleModus(modus))) { // not simple layout, so show details
			t.add(new DefaultMutableTreeNode(new KVP("descriptor_data", privateData, descriptorOffset + 2,
					descriptorLength, null)));
		}
	}

	public static String getFEC_innerString(final int fecInner) {
		return switch (fecInner) {
			case 0 -> "not defined";
			case 1 -> "1/2 conv. code rate";
			case 2 -> "2/3 conv. code rate";
			case 3 -> "3/4 conv. code rate";
			case 4 -> "5/6 conv. code rate";
			case 5 -> "7/8 conv. code rate";
			case 6 -> "8/9 conv. code rate";
			case 7 -> "3/5 conv. code rate";
			case 8 -> "4/5 conv. code rate";
			case 9 -> "9/10 conv. code rate";
			case 15 -> "no conv. Coding";
			default -> "reserved for future use";
		};
	}

	public static String formatCableFrequency(final String f) {
		return stripLeadingZeros(f.substring(0, 4) + '.' + f.substring(4, 8) + " MHz");
	}

	public static String formatCableFrequencyList(final String f) {
		return stripLeadingZeros(f.substring(1, 4) + '.' + f.substring(4, 8));
	}

	public static String formatSatelliteFrequency(final String f) {
		return stripLeadingZeros(f.substring(0, 3) + '.' + f.substring(3, 8) + " GHz");
	}

	public static String formatOrbitualPosition(final String f) {
		return stripLeadingZeros(f.substring(0, 3) + '.' + f.charAt(3) + "°");
	}

	public static String formatTerrestrialFrequency(final long f) {
		final StringBuilder s = new StringBuilder();
		String freq = Long.toString(f * 10);
		if (freq.length() < 7) {
			freq = "0000000".substring(freq.length()) + freq;
		}
		s.append(freq, 0, freq.length() - 6).append('.').append(
				freq.substring(freq.length() - 6)).append(" MHz");
		return stripLeadingZeros(s.toString());
	}

	public static String formatSymbolRate(final String f) {
		return stripLeadingZeros(f.substring(0, 3) + '.' + f.substring(3, 7) + " Msymbol/s");
	}

	public static String getServiceTypeString(final int serviceType) {

		return switch (serviceType) {
			case 0x00 -> "reserved for future use";
			case 0x01 -> "digital television service";
			case 0x02 -> "digital radio sound service";
			case 0x03 -> "Teletext service";
			case 0x04 -> "NVOD reference service";
			case 0x05 -> "NVOD time-shifted service";
			case 0x06 -> "mosaic service";
			case 0x07 -> "FM radio service";
			case 0x08 -> "DVB SRM service";
			case 0x09 -> "reserved for future use";
			case 0x0A -> "advanced codec digital radio sound service";
			case 0x0B -> "advanced codec mosaic service";
			case 0x0C -> "data broadcast service";
			case 0x0D -> "reserved for Common Interface Usage (EN 50221)";
			case 0x0E -> "RCS Map (see EN 301 790)";
			case 0x0F -> "RCS FLS (see EN 301 790)";
			case 0x10 -> "DVB MHP service";
			case 0x11 -> "MPEG-2 HD digital television service";
			case 0x12 -> "reserved for future use";
			case 0x13 -> "reserved for future use";
			case 0x14 -> "reserved for future use";
			case 0x15 -> "reserved for future use";
			case 0x16 -> "H.264/AVC SD digital television service";
			case 0x17 -> "H.264/AVC SD NVOD time-shifted service";
			case 0x18 -> "H.264/AVC SD NVOD reference service";
			case 0x19 -> "H.264/AVC HD digital television service";
			case 0x1A -> "H.264/AVC HD NVOD time-shifted service";
			case 0x1B -> "H.264/AVC HD NVOD reference service";
			case 0x1C -> "H.264/AVC frame compatible plano-stereoscopic HD digital television service";
			case 0x1D -> "H.264/AVC frame compatible plano-stereoscopic HD NVOD time-shifted service";
			case 0x1E -> "H.264/AVC frame compatible plano-stereoscopic HD NVOD reference service";
			case 0x1F -> "HEVC digital television service";
			case 0x20 -> "HEVC UHD digital television service with HDR and/or a frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz, or a resolution greater than 3840x2160, SDR or HDR, with a frame rate up to 60Hz";
			case 0x22 -> "AVS3 digital television service";
			case 0x84 -> "Sagem firmware download service"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf -- Mandatory for legacy STB (ICD3000, ICD4000 and ICD60)
			case 0x87 -> "Sagem OpenTV out_of_list_service"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf  -- Mandatory for legacy STB (ICD3000, ICD4000 and ICD60)
			case 0x88 -> "Sagem OpenTV in_list_service"; // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf

			default -> {
				if ((0x21 <= serviceType) && (serviceType <= 0x7F)) {
					yield "reserved for future use";
				}
				if ((0x80 <= serviceType) && (serviceType <= 0xFE)) {
					yield "user defined";
				}
				yield "Illegal value";
			}
		};

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
			return switch (component_type) {
				case 0x00 -> "reserved for future use";
				case 0x01 -> "DVB SRM data";
				default -> "reserved for DVB CPCM modes";
			};
			case 0x09:
			return getComponentType0x09String(stream_content_ext,component_type);
		case 0x0b:
			if(stream_content_ext==0xE){
				return getNextGenerationAudioComponentTypeString(component_type);
			}
			else if(stream_content_ext==0xf){
				return switch (component_type) {
					case 0x00 -> "less than 16:9 aspect ratio";
					case 0x01 -> "16:9 aspect ratio";
					case 0x02 -> "greater than 16:9 aspect ratio";
					case 0x03 -> "plano-stereoscopic top and\r\n" +
							"bottom (TaB) frame-packing";
					case 0x04 -> "HLG10 HDR";
					case 0x05 -> "HEVC temporal video subset for a frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz";
					case 0x06 -> "SMPTE ST 2094-10 DMI format";
					case 0x07 -> "SL-HDR2 DMI format";
					case 0x08 -> "SMPTE ST 2094-40 DMI format";
					case 0x09 -> "PQ10 HDR";
					default -> "reserved for future use";
				};
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
		return switch (stream_content_ext) {
			case 0x00 -> switch (component_type) {
				case 0x00 -> "HEVC Main Profile high definition video, 50 Hz";
				case 0x01 -> "HEVC Main 10 Profile high definition video, 50 Hz";
				case 0x02 -> "HEVC Main Profile high definition video, 60 Hz";
				case 0x03 -> "HEVC Main 10 Profile high definition video, 60 Hz";
				case 0x04 -> "HEVC ultra high definition video";
				case 0x05 -> "HEVC ultra high definition video with PQ10 HDR with a frame rate lower than or equal to 60 Hz";
				case 0x06 -> "HEVC ultra high definition video, frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz without a half frame rate HEVC temporal video sub-bitstream";
				case 0x07 -> "HEVC ultra high definition video with PQ10 HDR, frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz without a half frame rate HEVC temporal video sub-bit-stream";
				case 0x08 -> "HEVC ultra high definition video with a resolution up to 7680x4320";
				case 0x20 -> "AVS3 High 10 Profile with resolution up to 3840x2160, frame rate up to 60 Hz";
				case 0x21 -> "AVS3 High 10 Profile with resolution up to 3840x2160, frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz ";
				case 0x22 -> "AVS3 High 10 Profile with resolution up to 7680x4320, frame rate up to 60 Hz";
				case 0x23 -> "AVS3 High 10 Profile with resolution up to 7680x4320, frame rate of 100 Hz, 120 000/1 001 Hz, or 120 Hz ";
				default -> "reserved for future use";
			};
			case 0x01 -> switch (component_type) {
				case 0x00 -> "AC-4 main audio, mono";
				case 0x01 -> "AC-4 main audio, mono, dialogue enhancement enabled";
				case 0x02 -> "AC-4 main audio, stereo";
				case 0x03 -> "AC-4 main audio, stereo, dialogue enhancement enabled";
				case 0x04 -> "AC-4 main audio, multichannel";
				case 0x05 -> "AC-4 main audio, multichannel, dialogue enhancement enabled";
				case 0x06 -> "AC-4 broadcast-mix audio description, mono, for the visually impaired";
				case 0x07 -> "AC-4 broadcast-mix audio description, mono, for the visually impaired, dialogue enhancement enabled";
				case 0x08 -> "AC-4 broadcast-mix audio description, stereo, for the visually impaired";
				case 0x09 -> "AC-4 broadcast-mix audio description, stereo, for the visually impaired, dialogue enhancement enabled";
				case 0x0a -> "AC-4 broadcast-mix audio description, multichannel, for the visually impaired";
				case 0x0b -> "AC-4 broadcast-mix audio description, multichannel, for the visually impaired, dialogue enhancement enabled";
				case 0x0c -> "AC-4 receiver-mix audio description, mono, for the visually impaired";
				case 0x0d -> "AC-4 receiver-mix audio description, stereo, for the visually impaired";
				// see a038_dvb_spec_december_2017_pdf.pdf
				case 0x0E -> "AC-4 Part-2";
				case 0x0F -> "MPEG-H Audio LC Profile";
				case 0x10 -> "DTS-UHD main audio, mono";
				case 0x11 -> "DTS-UHD main audio, mono, dialogue enhancement enabled ";
				case 0x12 -> "DTS-UHD main audio, stereo";
				case 0x13 -> "DTS-UHD main audio, stereo, dialogue enhancement enabled";
				case 0x14 -> "DTS-UHD main audio, multichannel";
				case 0x15 -> "DTS-UHD main audio, multichannel, dialogue enhancement enabled";
				case 0x16 -> "DTS-UHD broadcast-mix audio description, mono, for the visually impaired ";
				case 0x17 -> "DTS-UHD broadcast-mix audio description, mono, for the visually impaired, dialogue enhancement enabled";
				case 0x18 -> "DTS-UHD broadcast-mix audio description, stereo, for the visually impaired";
				case 0x19 -> "DTS-UHD broadcast-mix audio description, stereo, for the visually impaired, dialogue enhancement enabled";
				case 0x1a -> "DTS-UHD broadcast-mix audio description, multichannel, for the visually impaired";
				case 0x1b -> "DTS-UHD broadcast-mix audio description, multichannel, for the visually impaired, dialogue enhancement enabled ";
				case 0x1c -> "DTS-UHD receiver-mix audio description, mono, for the visually impaired ";
				case 0x1d -> "DTS-UHD receiver-mix audio description, stereo, for the visually impaired ";
				case 0x1e -> "DTS-UHD NGA Audio";
				default -> "reserved for future use";
			};
			case 0x02 ->
					// see a038_dvb_spec_december_2017_pdf.pdf
					"TTML subtitles";
			default -> "reserved for future use";
		};
	}
	
	/**
	 * based on Table 27: Next generation audio component_type value assignments
	 * ETSI EN 300 468 V1.16.1 (2019-08) p.53
	 * @param component_type
	 * @return
	 */
	public static String getNextGenerationAudioComponentTypeString(final int component_type) {
		final StringBuilder res = new StringBuilder();
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
			case 0b00 -> res.append("no preference");
			case 0b01 -> res.append("stereo");
			case 0b10 -> res.append("two-dimensional");
			case 0b11 -> res.append("three-dimensional");
			default -> res.append("Illegal value");
		}
		return res.toString();
	}

	public static String getComponentType0x01String(final int component_type) {
		return switch (component_type) {
			case 0x00 -> "reserved for future use";
			case 0x01 -> "MPEG-2 video, 4:3 aspect ratio, 25 Hz";
			case 0x02 -> "MPEG-2 video, 16:9 aspect ratio with pan vectors, 25 Hz";
			case 0x03 -> "MPEG-2 video, 16:9 aspect ratio without pan vectors, 25 Hz";
			case 0x04 -> "MPEG-2 video, > 16:9 aspect ratio, 25 Hz";
			case 0x05 -> "MPEG-2 video, 4:3 aspect ratio, 30 Hz";
			case 0x06 -> "MPEG-2 video, 16:9 aspect ratio with pan vectors, 30 Hz";
			case 0x07 -> "MPEG-2 video, 16:9 aspect ratio without pan vectors, 30 Hz";
			case 0x08 -> "MPEG-2 video, > 16:9 aspect ratio, 30 Hz";
			case 0x09 -> "MPEG-2 high definition video, 4:3 aspect ratio, 25 Hz";
			case 0x0A -> "MPEG-2 high definition video, 16:9 aspect ratio with pan vectors, 25 Hz";
			case 0x0B -> "MPEG-2 high definition video, 16:9 aspect ratio without pan vectors, 25 Hz";
			case 0x0C -> "MPEG-2 high definition video, > 16:9 aspect ratio, 25 Hz";
			case 0x0D -> "MPEG-2 high definition video, 4:3 aspect ratio, 30 Hz";
			case 0x0E -> "MPEG-2 high definition video, 16:9 aspect ratio with pan vectors, 30 Hz";
			case 0x0F -> "MPEG-2 high definition video, 16:9 aspect ratio without pan vectors, 30 Hz";
			case 0x10 -> "MPEG-2 high definition video, > 16:9 aspect ratio, 30 Hz";
			case 0xFF -> "reserved for future use";
			default -> {
				if ((0x11 <= component_type) && (component_type <= 0xAF)) {
					yield "reserved for future use";
				}
				if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
					yield "user defined";
				}
				yield "Illegal value";
			}
		};
	}

	public static String getComponentType0x02String(final int component_type) {
		return switch (component_type) {
			case 0x00 -> "reserved for future use";
			case 0x01 -> "MPEG-1 Layer 2 audio, single mono channel";
			case 0x02 -> "MPEG-1 Layer 2 audio, dual mono channel";
			case 0x03 -> "MPEG-1 Layer 2 audio, stereo (2 channel)";
			case 0x04 -> "MPEG-1 Layer 2 audio, multi-lingual, multi-channel";
			case 0x05 -> "MPEG-1 Layer 2 audio, surround sound";
			case 0x40 -> "MPEG-1 Layer 2 audio description for the visually impaired";
			case 0x41 -> "MPEG-1 Layer 2 audio for the hard of hearing";
			case 0x42 -> "receiver-mixed supplementary audio as per annex E of TS 101 154";
			case 0x47 -> "MPEG-1 Layer 2 audio, receiver-mix audio description";
			case 0x48 -> "MPEG-1 Layer 2 audio, broadcast-mix audio description";
			case 0xFF -> "reserved for future use";
			default -> {
				if ((0x06 <= component_type) && (component_type <= 0x3F)) {
					yield "reserved for future use";
				}
				if ((0x43 <= component_type) && (component_type <= 0xAF)) {
					yield "reserved for future use";
				}
				if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
					yield "user defined";
				}
				yield "Illegal value";
			}
		};
	}

	public static String getComponentType0x03String(final int component_type) {
		return switch (component_type) {
			case 0x00 -> "reserved for future use";
			case 0x01 -> "EBU Teletext subtitles";
			case 0x02 -> "associated EBU Teletext";
			case 0x03 -> "VBI data";
			case 0x10 -> "DVB subtitles (normal) with no monitor aspect ratio criticality";
			case 0x11 -> "DVB subtitles (normal) for display on 4:3 aspect ratio monitor";
			case 0x12 -> "DVB subtitles (normal) for display on 16:9 aspect ratio monitor";
			case 0x13 -> "DVB subtitles (normal) for display on 2.21:1 aspect ratio monitor";
			case 0x14 -> "DVB subtitles (normal) for display on a high definition monitor";
			case 0x15 -> "DVB subtitles (normal) with plano-stereoscopic disparity for display on a high definition monitor";
			case 0x16 -> "DVB subtitles (normal) for display on an ultra high definition monitor";
			case 0x20 -> "DVB subtitles (for the hard of hearing) with no monitor aspect ratio criticality";
			case 0x21 -> "DVB subtitles (for the hard of hearing) for display on 4:3 aspect ratio monitor";
			case 0x22 -> "DVB subtitles (for the hard of hearing) for display on 16:9 aspect ratio monitor";
			case 0x23 -> "DVB subtitles (for the hard of hearing) for display on 2.21:1 aspect ratio monitor";
			case 0x24 -> "DVB subtitles (for the hard of hearing) for display on a high definition monitor";
			case 0x25 -> "DVB subtitles (for the hard of hearing) with planostereoscopic disparity for display on a high definition monitor";
			case 0x26 -> "DVB subtitles (for the hard of hearing) for display on an ultra high definition monitor";
			case 0x30 -> "Open (in-vision) sign language interpretation for the deaf";
			case 0x31 -> "Closed sign language interpretation for the deaf";
			case 0x40 -> "video up-sampled from standard definition source material";
			case 0x41 -> "Video is standard dynamic range (SDR)";
			case 0x42 -> "Video is high dynamic range (HDR) remapped from standard dynamic range (SDR) source material";
			case 0x43 -> "Video is high dynamic range (HDR) up-converted from standard dynamic range (SDR) source material";
			case 0x44 -> "Video is standard frame rate, less than or equal to 60 Hz";
			case 0x45 -> "High frame rate video generated from lower frame rate source material";
			case 0x80 -> "dependent SAOC-DE data stream";
			case 0xFF -> "reserved for future use";
			default -> {
				if ((0x04 <= component_type) && (component_type <= 0x0F)) {
					yield "reserved for future use";
				}
				if ((0x15 <= component_type) && (component_type <= 0x1F)) {
					yield "reserved for future use";
				}
				if ((0x25 <= component_type) && (component_type <= 0x2F)) {
					yield "reserved for future use";
				}
				if ((0x32 <= component_type) && (component_type <= 0xAF)) {
					yield "reserved for future use";
				}
				if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
					yield "user defined";
				}
				yield "Illegal value";
			}
		};
	}

	public static String getComponentType0x05String(final int component_type) {
		return switch (component_type) {
			case 0x00 -> "reserved for future use";
			case 0x01 -> "H.264/AVC standard definition video, 4:3 aspect ratio, 25 Hz";
			case 0x02 -> "reserved for future use";
			case 0x03 -> "H.264/AVC standard definition video, 16:9 aspect ratio, 25 Hz";
			case 0x04 -> "H.264/AVC standard definition video, > 16:9 aspect ratio, 25 Hz";
			case 0x05 -> "H.264/AVC standard definition video, 4:3 aspect ratio, 30 Hz";
			case 0x06 -> "reserved for future use";
			case 0x07 -> "H.264/AVC standard definition video, 16:9 aspect ratio, 30 Hz";
			case 0x08 -> "H.264/AVC standard definition video, > 16:9 aspect ratio, 30 Hz";
			case 0x0B -> "H.264/AVC high definition video, 16:9 aspect ratio, 25 Hz";
			case 0x0C -> "H.264/AVC high definition video, > 16:9 aspect ratio, 25 Hz";
			case 0x0F -> "H.264/AVC high definition video, 16:9 aspect ratio, 30 Hz";
			case 0x10 -> "H.264/AVC high definition video, > 16:9 aspect ratio, 30 Hz";
			case 0x80 -> "H.264/AVC planostereoscopic frame compatible high definition video, 16:9 aspect ratio, 25 Hz, Side-by-Side";
			case 0x81 -> "H.264/AVC planostereoscopic frame compatible high definition video, 16:9 aspect ratio, 25 Hz, Top-and-Bottom";
			case 0x82 -> "H.264/AVC planostereoscopic frame compatible high definition video, 16:9 aspect ratio, 30 Hz, Side-by-Side";
			case 0x83 -> "H.264/AVC stereoscopic frame compatible high definition video, 16:9 aspect ratio, 30 Hz, Top-and-Bottom";
			case 0x84 -> "H.264/MVC dependent view, plano-stereoscopic service compatible video";
			case 0xFF -> "reserved for future use";
			default -> {
				if ((0x09 <= component_type) && (component_type <= 0x0A)) {
					yield "reserved for future use";
				}
				if ((0x0D <= component_type) && (component_type <= 0x0E)) {
					yield "reserved for future use";
				}
				if ((0x11 <= component_type) && (component_type <= 0xAF)) {
					yield "reserved for future use";
				}
				if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
					yield "user defined";
				}
				yield "Illegal value";
			}
		};
	}

	public static String getComponentType0x06String(final int component_type) {
		return switch (component_type) {
			case 0x00 -> "reserved for future use";
			case 0x01 -> "HE-AAC audio, single mono channel";
			case 0x02 -> "reserved for future use";
			case 0x03 -> "HE-AAC audio, stereo";
			case 0x04 -> "reserved for future use";
			case 0x05 -> "HE-AAC audio, surround sound";
			case 0x40 -> "HE-AAC audio description for the visually impaired";
			case 0x41 -> "HE-AAC audio for the hard of hearing";
			case 0x42 -> "HE-AAC receiver-mixed supplementary audio as per annex E of TS 101 154 [10]";
			case 0x43 -> "HE-AAC v2 audio, stereo";
			case 0x44 -> "HE-AAC v2 audio description for the visually impaired";
			case 0x45 -> "HE-AAC v2 audio for the hard of hearing";
			case 0x46 -> "HE-AAC v2 receiver-mixed supplementary audio as per annex E of TS 101 154 [10]";
			case 0x47 -> "HE-AAC receiver mix audio description for the visually impaired";
			case 0x48 -> "HE-AAC broadcaster mix audio description for the visually impaired";
			case 0x49 -> "HE-AAC v2 receiver mix audio description for the visually impaired";
			case 0x4A -> "HE-AAC v2 broadcaster mix audio description for the visually impaired";
			case 0xA0 -> "HE AAC, or HE AAC v2 with SAOC-DE ancillary data";
			case 0xFF -> "reserved for future use";
			default -> {
				if ((0x06 <= component_type) && (component_type <= 0x3F)) {
					yield "reserved for future use";
				}
				if ((0x4B <= component_type) && (component_type <= 0xAF)) {
					yield "reserved for future use";
				}
				if ((0xB0 <= component_type) && (component_type <= 0xFE)) {
					yield "user defined";
				}
				yield "Illegal value";
			}
		};
	}


	/**
	 * @param descriptorList List of descriptors to be searched
	 * @param u class of the descriptors to be found
	 * @return List off all descriptors matching u
	 */
	@SuppressWarnings("unchecked")
	public static <U extends Descriptor> List<U> findGenericDescriptorsInList(final Iterable<? extends Descriptor> descriptorList, final Class<U> u ) {

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
	public static <U extends Descriptor> Object findDescriptorApplyFunc(final Iterable<Descriptor> descriptorList, final Class<U> u , final Function<U, Object> fun) {
		for (final Descriptor element : descriptorList) {
			if (element.getClass().equals(u)) {
				return fun.apply((U) element);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <U extends Descriptor> List<Object> findDescriptorApplyListFunc(final Iterable<Descriptor> descriptorList, final Class<U> u , final Function<U, List<Object>> fun) {
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
		return switch (profile_and_level) {
			case 0x10 -> "Main profile, level 1";
			case 0x11 -> "Main profile, level 2";
			case 0x12 -> "Main profile, level 3";
			case 0x13 -> "Main profile, level 4";
			case 0x18 -> "Scalable Profile, level 1";
			case 0x19 -> "Scalable Profile, level 2";
			case 0x1A -> "Scalable Profile, level 3";
			case 0x1B -> "Scalable Profile, level 4";
			case 0x20 -> "Speech profile, level 1";
			case 0x21 -> "Speech profile, level 2";
			case 0x28 -> "Synthesis profile, level 1";
			case 0x29 -> "Synthesis profile, level 2";
			case 0x2A -> "Synthesis profile, level 3";
			case 0x30 -> "High quality audio profile, level 1";
			case 0x31 -> "High quality audio profile, level 2";
			case 0x32 -> "High quality audio profile, level 3";
			case 0x33 -> "High quality audio profile, level 4";
			case 0x34 -> "High quality audio profile, level 5";
			case 0x35 -> "High quality audio profile, level 6";
			case 0x36 -> "High quality audio profile, level 7";
			case 0x37 -> "High quality audio profile, level 8";
			case 0x38 -> "Low delay audio profile, level 1";
			case 0x39 -> "Low delay audio profile, level 2";
			case 0x3A -> "Low delay audio profile, level 3";
			case 0x3B -> "Low delay audio profile, level 4";
			case 0x3C -> "Low delay audio profile, level 5";
			case 0x3D -> "Low delay audio profile, level 6";
			case 0x3E -> "Low delay audio profile, level 7";
			case 0x3F -> "Low delay audio profile, level 8";
			case 0x40 -> "Natural audio profile, level 1";
			case 0x41 -> "Natural audio profile, level 2";
			case 0x42 -> "Natural audio profile, level 3";
			case 0x43 -> "Natural audio profile, level 4";
			case 0x48 -> "Mobile audio internetworking profile, level 1";
			case 0x49 -> "Mobile audio internetworking profile, level 2";
			case 0x4A -> "Mobile audio internetworking profile, level 3";
			case 0x4B -> "Mobile audio internetworking profile, level 4";
			case 0x4C -> "Mobile audio internetworking profile, level 5";
			case 0x4D -> "Mobile audio internetworking profile, level 6";
			case 0x50 -> "AAC profile, level 1";
			case 0x51 -> "AAC profile, level 2";
			case 0x52 -> "AAC profile, level 4";
			case 0x53 -> "AAC profile, level 5";
			case 0x58 -> "High efficiency AAC profile, level 2";
			case 0x59 -> "High efficiency AAC profile, level 3";
			case 0x5A -> "High efficiency AAC profile, level 4";
			case 0x5B -> "High efficiency AAC profile, level 5";
			case 0x60 -> "High efficiency AAC v2 profile, level 2";
			case 0x61 -> "High efficiency AAC v2 profile, level 3";
			case 0x62 -> "High efficiency AAC v2 profile, level 4";
			case 0x63 -> "High efficiency AAC v2 profile, level 5";
			case 0xFF -> "Audio profile and level not specified by the MPEG-4_audio_profile_and_level field in this descriptor";
			default -> "Reserved";
		};
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

	@Override
	public String toString() {
		return "Descriptor{" +
				"descriptorTag=" + descriptorTag +
				", descriptorLength=" + descriptorLength +
				", privateData=" + Arrays.toString(privateData) +
				", descriptorOffset=" + descriptorOffset +
				", privateDataOffset=" + privateDataOffset +
				", parentTableSection=" + parentTableSection +
				'}';
	}
}
