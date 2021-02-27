# DVB Inspector 

[DVB Inspector](http://www.digitalekabeltelevisie.nl/dvb_inspector/) is an open-source DVB analyzer, written in java. It can show the logical structure of the DVB SI and PSI data. It also shows bit rate usage data. DVB Inspector can be used to analyse contents; MPEG Video structure, teletext, DVB subtitles, DSM-CC Object carousels, SCTE-35 signaling, etc.

![DVB Inspector example screen](http://www.digitalekabeltelevisie.nl/dvb_inspector/img/details_panel_table.png)

## Prerequisites

DVB inspector requires java 11 or higher.

## Installation

DVB inspector comes in a single .zip file. It contains all necessary files for running DVB inspector.

Unzip DVBinspector-1.15.0-dist.zip to a directory of your choice. It will create the following directory structure;

### Directory structure

*    lib directory, contains external libraries that are required.
*    src/main/java, this directory contains sources for the java program.
*    src/main/resources, contains images needed by DVB Inspector
*    src/main/resources/res, contains comma separated resource files for bouquet_id, ca_system_id, data_broadcast_id, original_network, platform_id, oui_registration. The files can be updated if needed, see [site](http://www.digitalekabeltelevisie.nl/dvb_inspector/installation.shtml).
*    COPYING, contains the GPL license
*    dvb.bat Dos command script that starts the program. Can be modified if libraries are placed in a different location than default.
*    dvb.sh Bash script to start the program using Linux.
*    DVBInspector.jar contains the classes for the main program. Can be executed using the dvb.bat or dvb.sh script.
*    DVBinspector-1.15.0.jar. Self contained jar that contains all resources and libs. To execute just double click.

## Usage

### DVBinspector-1.15.0.jar

There are two ways to start DVBInspector. The easiest is to use DVBinspector-1.15.0.jar. This jar contains everything DVBInspector needs (apart from java), so there is no need to set class-paths, etc. On windows just double-click. Or from command line type "DVBinspector-1.15.0.jar" (without the quotes). If you want more control (to set heap size), and see some debug information you can use "java -jar DVBinspector-1.15.0.jar".

### dvb.bat 

This small windows batch file sets up the environment to start DVB Inspector. It includes the needed libraries in the class path. 

### dvb.sh

The linux/unix/mac version of dvb.bat. This small batch file sets up the environment to start DVB Inspector. It includes the needed libraries in the class path.

## Manual

See the [manual](http://www.digitalekabeltelevisie.nl/dvb_inspector/usermanual.shtml) for more information on available views and options of the program.

## Changelog

List of the most important changes between releases.

### Release 1.15.0 (10th anniversary release)
Release date: 31/01/2021

This release requires Java 11 or newer!

* added: support for AC-4
* added: display preroll time on splice_time for SCTE-35
* added: copy/save buttons to TableView
* fixed: fastscan ONT detection checks for originalNetworkId not actual
* added: fastscan M7 Nagra_brandID_descriptor
* added: support for SlHdrInfo in UserDataRegisteredItuT35Sei_message
* added: support for ST2094-10_data in AuxData
* changed: java version to 11
* fixed: VPS day/country decoding (Thanks to Stefan Pöschel)
* fixed: support for UTF-8 ISO/IEC10646-1
* extended: implementation of PES Header, including support for AD_descriptor in PES_private_data


### Release 1.14.0
Release date: 17/10/2020

* fixed: scaling for HiDPI displays
* improved: labeling PIDs shared between services
* fixed: (null) in SDT when service has no service_descriptor
* added: NVOD_reference_descriptor
* added: time_shifted_event_descriptor
* added: more detail for EIT Events
* added: TableView for TOT(sections)
* improved: EIT event HTMLView to group by language
* fixed: labeling PIDs whem multiple versions PMTSection present
* added: some UserDataTypes from ATSC
* added: several (private) stream_type and table_type descriptions
* fixed: reload stream shows progress indicator and can be interrupted
* added: support for TTML, including TtmlSubtitlingDescriptor, Downloadable Font Information Table and XMLView
* fixed: url_extension_byte off by one in TransportProtocolDescriptor (Thanks to Tomi Sarparanta)
* added: .trp to supported file extensions


### Release 1.13.0
Release date: 31/05/2020

* added: Tableview for several PSI tables
* updated: lookup files for DVB identifiers, like original_network_id, ca_system_id, etc.
* updated: oui list
* updated: dependencies like opencsv, jfreechart, etc.
* fixed: handling of CA_Descriptor in component descriptors
* fixed: EIT View not complete when spanning multiple tableIds
* improved: show table specific labels in treeview instead of table_id_extension


### Release 1.12.0
Release date: 13/04/2020

* updated: stream_type descriptions and MPEG Extension descriptor names based on H.222.0 (2018) Amendment 1
* fixed: a service is not uniquely identified by service_id only, but by original_network_id, transport_stream_id and service_id, chn.
* changed: SDT and EIT tree representation to match the fix for service_id
* updated: M7 Fastscan to v.8.2.0
* added: AC4Descriptor and AudioPreselectionDescriptor (DVBExtensionDescriptors)
* improved: MegaFrameInitializationPacket to support Optional MIP section parameters
* added: Name descriptor and Module link descriptor support for DSM-CC UN-Message
* improved: performance on DSMCCs getTreeNode by using parallel streams
* added: ScramblingDescriptor
* fixed: handle PesPackets without data for subtitling
* added: detect SL-HDR data (Annex A of TS 103 433-1)
* added: label for ST2094-10_data (HDR Signalling)
* updated: several descriptions to match DVB BlueBook A038 June 2019


### Release 1.11.0
Release date: 13/04/2019

* improved: make generic PSI tables, DSM-CC parsing and PCR/PTS view switchable, this will reduce memory usage and processing time.
* added: support for t2mi, including exporting plp as new .TS file
* improved: T2DeliverySystemDescriptor now full specification implemented
* added: TargetSmartcardDescriptor
* updated: use new values from ISO/IEC 13818-1:2018
* added: option to fully expand tree node using keyboard and/or context menu
* improved: do post processing after user interrupts file loading, so labels will be set and packets will display
* added: HDR_WCG_idc field to HEVCVideoDescriptor
* added: option to save byte[] from Tree
* added: filter to show only mpeg-ts files in File Open Dialog
* added: support for 14496-3 Audio (AAC) parsing
* added: display SCTE-35 time_signal in PCR/PTS Graph
* improved: support for h.265 video (HEVC), added graph with frame sizes, added Slice_segment_header, Slice_segment_layer_rbsp, st_ref_pic_set
* added: option to filter series in PCR/PTS/DTS Graph
* fixed: when multiple PrivateDataFields were present in AdaptationField the list was not constructed correctly
* added: SCTE adaptation field data descriptor and EBP_descriptor
* added: support for M7 Fastscan Tables


### Release 1.10.1 (bug fix release)
Release date: 09/09/2018

* fixed: parsing of 138181 Audio. This bug resulted in looping (when playing and in the graph) of the audio in a single PES Packet. 


### Release 1.10.0
Release date: 12/08/2018

* fixed: IPMACStreamLocationDescriptor componentTag was read wrong
* fixed: Parsing of Audio Access Units in Pes Packet, last unit of Pes packet was incorrectly shown at next Pes packet
* added: option to CTRL-C packet content from Grid view (after click to set focus)
* added: remember directory of dropped file, next "File Open" will open in that directory
* fixed: handling of TSPackets with transportErrorIndicator set, this caused Pid packetview to crash
* added: splice_command_type TimeSignal and SegmentationDescriptor support for SCTE-35
* added: added new streamType strings
* added: values from bluebook a038 dec 2017 for a.o. ContentDescriptor, LinkageDescriptor, DVBExtensionDescriptor
* improved: error handling for errors in adaptationField
* changed: upgraded to java 1.8
* added: display SCTE35 Program Splice Points (with spliceTime) in PCR/PTS Graph
* added: error handling when PESHeader not complete in TSPacket

Thanks to every one that has reported bugs in DVB Inspector! This helps to make the program better and more robust. Please contact me if you think there is an error or option for improvement in the program. I really do appreciate it!


### Release 1.9.0
Release date: 27/10/2017

* added: NetworkChangeNotifyDescriptor
* added: drag-and-drop support
* fixed: PSI table handling of new versions with more subtables than initial table
* added: SHDeliverySystemDescriptor
* fixed: value of streamContextEExt was reported wrong in ComponentDescriptor
* fixed: last PESPacket was not handled when not followed by payload_start_indicator. Now it is handled when all bytes are read (does not work for video, because there PES_packet_length is not set)
* added: S2XSatelliteDeliverySystemDescriptor based on DVB BlueBook A038 jan 2017
* added: URILinkageDescriptor
* added: ServiceRelocatedDescriptor and CIAncillaryDataDescriptor
* fixed: handle start_time = 0xFFFFFFFF (undefined) for EITsection Event (like VOD)
* added: support for TEMI information in Adaptation Field, added Timeline descriptor
* added: show TEMI data in TimeStampChart (second y-axis)
* added: panning in BitRateChart and TimeStampChart (use CTRL+mouse for panning/drag)
* fixed: detection of SCTE35 pid in PMT
* added: Teletext support for G0 character sets, switching between normal and alternate G0 set
* fixed: wrong line endings in dvb.sh
* upgraded: versions of jfreechart and jcommon
* fixed: some smaller bugs

Thanks to Michael Cullen for contributing the NetworkChangeNotifyDescriptor.


### Release 1.8.0
Release date: 07/01/2017

* removed: option to enable/disable TS packets (now it is always enabled)
* added: some values from DVB BlueBook a038 Oct 2016
* fixed: handle missing PCRPid
* added: copy and save action to EITView
* fixed: in H264 frame graph last access unit was not drawn when not followed by delimiter
* improved: performance when parsing PES packets
* improved: less memory usage for loading TS packets
* added: display message when stream can not be displayed (most likely heap error)
* added: display minimum and maximum distance between repeating table sections


### Release 1.7.0
Release date: 23/09/2016

* added: option to select default Teletext G0 and G2 Character Set Designation
* updated: several lookup tables from http://www.dvbservices.com/identifiers/
* fixed: tertiary_region_code is 16 bits in TargetRegionDescriptor and TargetRegionNameDescriptor
* fixed: don't treat char > 0x7f as mosaic graphics in teletext pages
* fixed: bug regarding the National Option Subset which leads to wrong display of e.g. the page 109 of the ZDF Teletext
* fixed: some bugs


### Release 1.6.0
Release date: 17/01/2016

* added: support for SCTE-35 (Digital Program Insertion Cueing Message for Cable)
* fixed: support for ISO/IEC 10646 encoding
* added: MultilingualBouquetNameDescriptor
* added: MVC video sub-bitstream support (H264)
* added: text search in Tree View
* added: mnemonics and accelerators to menu
* fixed: last data byte of teletext data was not shown
* fixed: last field of EBU data PES field was not shown
* fixed: handle adaptation_field_length ==0 correctly (do not show flags that are not there)
* fixed: some bugs

Thanks to Richard Mars for fixing the TxtDataField and EBUPESDataField


### Release 1.5.0
Release date: 07/08/2015

* added: Support for h.265 (HEVC) parsing
* added: MPEG Extension descriptor
* added: HEVC video descriptor
* added: HEVC_timing_and_HRD_descriptor
* added: support for new stream types in PMT as defined in ITU-T H.222.0 (10/2014)
* added: SIT (Selection Information Table) and PartialTransportStreamDescriptor
* updated: component descriptor to use Final draft ETSI EN 300 468 V1.15.1
* fixed: rendering of DVB titles when display_window_flag is used
* fixed: some bugs

Thanks to Daniel Kamil Kozar for adding MPEG Extension descriptor, HEVC video descriptor and HEVC_timing_and_HRD_descriptor


### Release 1.4.0
Release date: 18/01/2015

* added: PCR/PTS/DTS Chart
* added: show PesHeader in TSPacket
* fixed: improved support for national characters in teletext
* fixed: fixed bitrate calculation, now handles (ignores) PCRs that have smaller value than previous
* fixed: some bugs

Thanks to Daniel Kamil Kozar for the fix for improved support for national characters in teletext!


### Release 1.3.0
Release date: 25/08/2014

* added: support for Related Content Tables (RCT)
* added: support for Coding of Data Fields in the Private Data Bytes of the Adaptation Field
* added: AdaptationFieldDataDescriptor, FTAConentManagmentDescriptor, MetaDataDescriptor, MetaDataPointerDescriptor, RelatedContentDescriptor, DTG-GuidanceDescriptor, DTG-LogicalChannelDescriptor, Mpeg4AudioDescriptor
* added: message, t2delivery, targetregion, targetregionname extension descriptors
* added: support for 188,192,204 and 208 bytes size TSPackets
* added: add gridlines to gridview, make gridview zoomable
* fixed: added encoding string for type 0x1f
* added: linkageType==0x0D (event linkage) to LinkageDescriptor
* fixed: some bugs


### Release 1.2.0
Release date: 14/01/2014

* fixed: Audio access units were not handled right when not aligned with PESPackets
* added: Graph of MPEG 1/2 audio
* added: option to play MPEG 1/2 audio
* fixed: show multiple versions of CATSections/SDTSections in DVBTree
* fixed: Mouse over in EITView now has correct behavior for emphasis and lf/cr DBVStrings
* improved: Changed BitRateChart for better labels on time-axis, and improved mouse support
* changed: upgraded to java 1.7
* fixed: some bugs


### Release 1.1.0
Release date: 11/10/2013

* added: Show contents of TS packets per PID
* added: Progress indicator when loading file
* added: graph to show frame type/order and size in H.264 Video
* added: SequenceDisplayExtension (MPEG2 Video)
* fixed: eventIds in BIOPStreamEventMessage
* improved: scrolling in EIT and Grid View
* added: option to show version number on Table Sections
* fixed: some bugs


### Release 1.0.0
Release date: 21/06/2013

* added: Show contents of TS packets, both in Tree and Grid View
* improved: performance of Grid View
* fixed: version number in PSI tables is 5 bits, not 4
* Added: support for DSM-CC stream descriptor list
* fixed: usage of version info in DSM-CC Object carousel
* fixed: usage of transaction_id in DSM-CC Object carousel
* changed: method of checking CRC on PSI tables, should be more robust now
* changed: method how different versions of tables are stored, now all version are stored, even if version number wraps around
* added: MHP Label descriptor
* added: Multilingual Network Name descriptor
* added: Multilingual Service Name descriptor
* added: DSM-CC Stream descriptor


### Release 0.0.10
Release date: 28/04/2013

* added: graph to show frame order (IBP) and size in MPEG2 Video
* added: toggle legend on Bitrate chart
* added: copy to clip board functionality on detail panel
* added: save as... for images in detail panel
* added: resize images in detail panel
* added: remember window size/position
* changed: order of tabs (EPG grid now second, so all 'filtered' views are at the end)
* fixed: getDisplayWindowFlag in DisplayDefinitionSegment.java
* added: frame_packing_SEI_not_present_flag to AVCVideoDescriptor.java
* fixed: handle missing object_data_segments in DisplaySet

Thanks to Dave Evans for the fixes for getDisplayWindowFlag, frame_packing_SEI_not_present_flag and missing object_data_segments!


### Release 0.0.9
Release date: 06/02/2013

* added: EIT grid view


### Release 0.0.8
Release date: 27/01/2013

* fixed: MPEG2 Video preview did not work on PES packets, because of adding PTS labels.
* added: some detail to PDC TxtTriplets for teletext


### Release 0.0.7
Release date: 20/01/2013

* added: descriptors: S2SatelliteDeliverySystemDescriptor, AACMpeg2Descriptor, JPEG2000VideoDescriptor
* added: AC3 (also alternative syntax) and E-AC3 support
* updated: General PES support, also show PES packets for which no dedicated support is available
* added: option show PTS on PES packets (easier to find certain packet)

Thanks to Asif Raza for the contribution of the AACMpeg2Descriptor and JPEG2000VideoDescriptor.


### Release 0.0.6
Release date: 23/08/2012

* added: preview for MPEG2 Video (based on MPVDecoder from ProjectX)
* updated DVB subtitles (now show complete Display Sets), and show Video background


### Release 0.0.5
Release date: 06/08/2012

* added: Support for h.264
* added: descriptors: DefaultAuthorityDescriptor, ServiceIdentifierDescriptor, NordigLogicalChannelDescriptorV1 and NordigLogicalChannelDescriptorV2
* fixed: filtering of non printable characters (range 0x80 to 0x9F) in text items
* changed: source encoding to UTF-8
* added: option to number items in lists
* fixed: some bugs
* new: put sources on sourceforge


### Release 0.0.4
Release date: 15/05/2012

* added: Support for DSM-CC Object carousels (HbbTV, MHP, MHEG5)
* added: Ancillary data for 138183-Audio (RDS over UECP)
* added: Mega-frame Initialization Packet (MIP) for DVB-T
* added: hex-viewer for raw data
* changed: improved usability, now remembers last used directory, default private_data_specifier and view-modus
* fixed: some bugs
* added: descriptors: TargetBackGroundDescriptor, VideoWindowDescriptor and ApplicationUsageDescriptor (AIT)


### Release 0.0.3
Release date: 23/08/2011

* added: Support for AIT
* added: level 2.5 graphical view for teletext pages
* fixed: some bugs
* added: some descriptors
* changed: maven enabled (if you want to build it yourself)


### Release 0.0.2
Release date: 26/02/2011

* changed: the way PES data is processed, less memory use and higher processing speed
* added: grid view for TS packets
* added: graphical view for teletext pages
* added: graphical view for DVB subtitles
* added: 'copy to clipboard' actions to tree view
* changed: when changing the order of PIDs in the bitrate view, they keep their original color


### Release 0.0.1
Release date: 31/01/2011

* Initial release. 
