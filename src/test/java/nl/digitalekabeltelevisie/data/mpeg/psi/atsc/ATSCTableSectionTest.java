package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.table.TableModel;

import org.junit.Test;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.CRCcheck;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.AtscAC3AudioStreamDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.AtscEnhancedAC3AudioDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.AtscPrivateInformationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.CaptionServiceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ComponentNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ContentAdvisoryDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.DCCRequestDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ExtendedChannelNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.GenreDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.RedistributionControlDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ServiceLocationDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.StuffingDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.TimeShiftedServiceDescriptor;
import nl.digitalekabeltelevisie.gui.TableSource;

public class ATSCTableSectionTest {

	@Test
	public void parsesSystemTimeTable() {
		byte[] section = withCrc(new byte[] {
				(byte) 0xCD, (byte) 0xF0, 0x11,
				0x00, 0x00, (byte) 0xC1, 0x00, 0x00,
				0x00,
				0x00, 0x00, 0x03, (byte) 0xE8,
				0x12,
				(byte) 0x81, 0x17,
				0x00, 0x00, 0x00, 0x00
		});

		STTsection stt = new STTsection(new PsiSectionData(section), null);

		assertEquals(0xCD, stt.getTableId());
		assertEquals(0, stt.getProtocolVersion());
		assertEquals(1000L, stt.getSystemTime());
		assertEquals(18, stt.getGpsUtcOffset());
		assertEquals(0x8117, stt.getDaylightSaving());
		assertEquals(1, stt.getDsStatus());
		assertEquals(1, stt.getDsDayOfMonth());
		assertEquals(23, stt.getDsHour());
		assertEquals("1980-01-06T00:16:22Z", stt.getUtcTimeString());
		assertEquals(0, stt.getDescriptorList().size());
		assertEquals("1980-01-06T00:16:22Z",
				stt.getTableModel().getValueAt(0, findColumn(stt.getTableModel(), "UTC_time")));
		assertEquals(0L, CRCcheck.crc32(section, section.length));
	}

	@Test
	public void showsSystemTimeTableSummary() {
		STT stt = new STT(null);
		byte[] section = withCrc(new byte[] {
				(byte) 0xCD, (byte) 0xF0, 0x11,
				0x00, 0x00, (byte) 0xC1, 0x00, 0x00,
				0x00,
				0x00, 0x00, 0x03, (byte) 0xE8,
				0x12,
				(byte) 0x81, 0x17,
				0x00, 0x00, 0x00, 0x00
		});

		stt.update(new STTsection(new PsiSectionData(section), null));

		TableModel tableModel = stt.getTableModel();
		assertEquals(1, tableModel.getRowCount());
		assertEquals(1000L, tableModel.getValueAt(0, findColumn(tableModel, "system_time")));
		assertEquals(18, tableModel.getValueAt(0, findColumn(tableModel, "GPS_UTC_offset")));
		assertEquals("1980-01-06T00:16:22Z", tableModel.getValueAt(0, findColumn(tableModel, "UTC_time")));
	}

	@Test
	public void parsesMasterGuideTable() {
		byte[] section = withCrc(new byte[] {
				(byte) 0xC7, (byte) 0xF0, 0x19,
				0x00, 0x00, (byte) 0xC7, 0x00, 0x00,
				0x00,
				0x00, 0x01,
				0x01, 0x00,
				(byte) 0xFF, (byte) 0xFB,
				(byte) 0xE4,
				0x00, 0x00, 0x01, 0x2C,
				(byte) 0xF0, 0x00,
				(byte) 0xF0, 0x00,
				0x00, 0x00, 0x00, 0x00
		});

		MGTsection mgt = new MGTsection(new PsiSectionData(section), null);
		MGTsection.TableTypeEntry entry = mgt.getTableTypeEntries().getFirst();
		ATSCTables atscTables = new ATSCTables(null);
		atscTables.update(mgt);

		assertEquals(0xC7, mgt.getTableId());
		assertEquals(3, mgt.getVersion());
		assertEquals(0, mgt.getProtocolVersion());
		assertEquals(1, mgt.getTablesDefined());
		assertEquals(0, mgt.getDescriptorsLength());
		assertEquals(0x0100, entry.getTableType());
		assertEquals("Event Information Table 0", MGTsection.getTableTypeDescription(entry.getTableType()));
		assertEquals(0x1FFB, entry.getTableTypePid());
		assertEquals(4, entry.getTableTypeVersionNumber());
		assertEquals(300L, entry.getNumberBytes());
		assertEquals(0, entry.getTableTypeDescriptorsLength());
		assertEquals(true, atscTables.isAtscEitPid(0x1FFB));
		assertEquals(0L, CRCcheck.crc32(section, section.length));
	}

	@Test
	public void parsesEventInformationTable() {
		byte[] section = withCrc(new byte[] {
				(byte) 0xCB, (byte) 0xF0, 0x23,
				0x10, 0x01, (byte) 0xC1, 0x00, 0x00,
				0x00, 0x01,
				(byte) 0xC1, 0x23,
				0x00, 0x00, 0x03, (byte) 0xE8,
				(byte) 0xD0, 0x07, 0x08,
				0x0C,
				0x01, 0x65, 0x6E, 0x67, 0x01, 0x00, 0x00, 0x04,
				0x4E, 0x65, 0x77, 0x73,
				(byte) 0xF0, 0x00,
				0x00, 0x00, 0x00, 0x00
		});

		ATSCEITsection eit = new ATSCEITsection(new PsiSectionData(section), null);
		ATSCEITsection.Event event = eit.getEvents().getFirst();

		assertEquals(0xCB, eit.getTableId());
		assertEquals(0x1001, eit.getSourceId());
		assertEquals(0, eit.getProtocolVersion());
		assertEquals(1, eit.getNumEventsInSection());
		assertEquals(0x0123, event.getEventId());
		assertEquals(1000L, event.getStartTime());
		assertEquals(1, event.getEtmLocation());
		assertEquals(1800, event.getLengthInSeconds());
		assertEquals("News", event.getTitle());
		assertEquals(0, event.getDescriptorsLength());
		assertEquals(0L, CRCcheck.crc32(section, section.length));
	}

	@Test
	public void keepsEitSectionsGroupedByLatestCompleteVersion() {
		ATSCTables atscTables = new ATSCTables(null);

		atscTables.update(new ATSCEITsection(new PsiSectionData(eitSection(2, 0, 0, 0x1001, 0x0101, "Base")), null));
		atscTables.update(new ATSCEITsection(new PsiSectionData(eitSection(3, 0, 1, 0x1001, 0x0201, "Morning")), null));

		TableModel latestCompleteTable = atscTables.getEit().getTableModel();
		assertEquals(1, latestCompleteTable.getRowCount());
		assertEquals("Base", latestCompleteTable.getValueAt(0, findColumn(latestCompleteTable, "title")));

		atscTables.update(new ATSCEITsection(new PsiSectionData(eitSection(3, 1, 1, 0x1001, 0x0202, "Evening")), null));

		TableModel latestVersionTable = atscTables.getEit().getTableModel();
		assertEquals(2, latestVersionTable.getRowCount());
		assertEquals("Morning", latestVersionTable.getValueAt(0, findColumn(latestVersionTable, "title")));
		assertEquals("Evening", latestVersionTable.getValueAt(1, findColumn(latestVersionTable, "title")));

		TableModel allVersionsTable = atscTables.getEit().getAllVersionsTableModel();
		assertEquals(3, allVersionsTable.getRowCount());

		TableModel sectionTable = atscTables.getEit().getSectionTableModel();
		assertEquals(2, sectionTable.getRowCount());
		assertEquals(1, sectionTable.getValueAt(0, findColumn(sectionTable, "num_events")));

		KVP treeNode = atscTables.getEit().getJTreeNode(0);
		KVP tableNode = (KVP) treeNode.getChildAt(0);
		KVP sourceNode = (KVP) tableNode.getChildAt(0);
		assertEquals("version 2", sourceNode.getChildAt(0).toString());
		assertEquals("version 3", sourceNode.getChildAt(1).toString());

		KVP versionThreeNode = (KVP) sourceNode.getChildAt(1);
		TableSource versionTableSource = (TableSource) versionThreeNode.getDetailViews().get(0).detailSource();
		assertEquals(2, versionTableSource.getTableModel().getRowCount());

		KVP sectionOneNode = (KVP) versionThreeNode.getChildAt(1);
		TableSource defaultTableSource = (TableSource) sectionOneNode.getDetailViews().get(0).detailSource();
		assertEquals(2, defaultTableSource.getTableModel().getRowCount());
	}

	@Test
	public void showsEmptyEitSectionsInSectionSummaryTable() {
		ATSCEIT eit = new ATSCEIT(null);

		eit.update(new ATSCEITsection(new PsiSectionData(eitSection(5, 0, 0, 0x1001)), null), 0x0101);

		TableModel eventsTable = eit.getTableModel();
		assertEquals(0, eventsTable.getRowCount());

		TableModel sectionTable = eit.getSectionTableModel();
		assertEquals(1, sectionTable.getRowCount());
		assertEquals("Event Information Table 1", sectionTable.getValueAt(0, findColumn(sectionTable, "table_type")));
		assertEquals(0, sectionTable.getValueAt(0, findColumn(sectionTable, "num_events")));

		KVP treeNode = eit.getJTreeNode(0);
		assertEquals("EIT Sections (latest complete versions)", treeNode.getDetailViews().get(0).label());
		KVP tableNode = (KVP) treeNode.getChildAt(0);
		assertEquals("EIT Sections (latest complete versions)", tableNode.getDetailViews().get(0).label());
		KVP sourceNode = (KVP) tableNode.getChildAt(0);
		assertEquals("EIT Sections (latest complete versions)", sourceNode.getDetailViews().get(0).label());
		KVP versionNode = (KVP) sourceNode.getChildAt(0);
		assertEquals("EIT Sections", versionNode.getDetailViews().get(0).label());
	}

	@Test
	public void parsesExtendedTextTable() {
		byte[] mgtSection = withCrc(new byte[] {
				(byte) 0xC7, (byte) 0xF0, 0x19,
				0x00, 0x00, (byte) 0xC1, 0x00, 0x00,
				0x00,
				0x00, 0x01,
				0x02, 0x00,
				(byte) 0xFF, (byte) 0xFA,
				(byte) 0xE4,
				0x00, 0x00, 0x00, 0x64,
				(byte) 0xF0, 0x00,
				(byte) 0xF0, 0x00,
				0x00, 0x00, 0x00, 0x00
		});
		byte[] section = withCrc(new byte[] {
				(byte) 0xCC, (byte) 0xF0, 0x26,
				0x02, 0x00, (byte) 0xC1, 0x00, 0x00,
				0x00,
				0x10, 0x01, 0x04, (byte) 0x8E,
				0x01, 0x65, 0x6E, 0x67, 0x01, 0x00, 0x00, 0x10,
				0x4C, 0x6F, 0x6E, 0x67, 0x20, 0x64, 0x65, 0x73,
				0x63, 0x72, 0x69, 0x70, 0x74, 0x69, 0x6F, 0x6E,
				0x00, 0x00, 0x00, 0x00
		});

		ATSCETTsection ett = new ATSCETTsection(new PsiSectionData(section), null);
		ATSCTables atscTables = new ATSCTables(null);
		atscTables.update(new MGTsection(new PsiSectionData(mgtSection), null));
		atscTables.update(ett);

		assertEquals(0xCC, ett.getTableId());
		assertEquals(0x0200, ett.getTableIdExtension());
		assertEquals(0, ett.getProtocolVersion());
		assertEquals(0x1001048EL, ett.getEtmId());
		assertEquals(0x1001, ett.getSourceId());
		assertEquals(0x0123, ett.getEventId());
		assertEquals("Long description", ett.getExtendedText());
		assertEquals(true, atscTables.isAtscEttPid(0x1FFA));
		assertEquals(0L, CRCcheck.crc32(section, section.length));
	}

	@Test
	public void separatesChannelAndEventExtendedText() {
		ATSCTables atscTables = new ATSCTables(null);

		atscTables.update(new ATSCETTsection(new PsiSectionData(ettSection(0x0004, 0x1001, 0, "Channel text")), null));
		atscTables.update(new ATSCETTsection(new PsiSectionData(ettSection(0x0200, 0x1001, 0x0123, "Event text")), null));

		assertEquals("Channel text", atscTables.getEtt().getChannelText(0x1001));
		assertEquals("Event text", atscTables.getEtt().getEventText(0x1001, 0x0123));
	}

	@Test
	public void parsesTerrestrialVirtualChannelTable() {
		byte[] section = withCrc(new byte[] {
				(byte) 0xC8, (byte) 0xF0, 0x4F,
				0x12, 0x34, (byte) 0xC1, 0x00, 0x00,
				0x00, 0x01,
				0x00, 0x57, 0x00, 0x58, 0x00, 0x59, 0x00, 0x5A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				(byte) 0xF0, 0x1C, 0x01,
				0x04,
				0x00, 0x00, 0x00, 0x00,
				0x12, 0x34,
				0x00, 0x03,
				0x4D, (byte) 0xC2,
				0x10, 0x01,
				(byte) 0xFC, 0x22,
				(byte) 0xA0, 0x0F,
				0x01, 0x65, 0x6E, 0x67, 0x01, 0x00, 0x00, 0x07,
				0x57, 0x58, 0x59, 0x5A, 0x20, 0x44, 0x54,
				(byte) 0xA1, 0x0F,
				(byte) 0xE0, 0x31, 0x02,
				0x02, (byte) 0xE0, 0x31, 0x00, 0x00, 0x00,
				(byte) 0x81, (byte) 0xE0, 0x34, 0x65, 0x6E, 0x67,
				(byte) 0xFC, 0x00,
				0x00, 0x00, 0x00, 0x00
		});

		TVCTsection tvct = new TVCTsection(new PsiSectionData(section), null);
		VCTsection.VirtualChannel channel = tvct.getVirtualChannels().getFirst();
		ATSCTables atscTables = new ATSCTables(null);
		atscTables.update(tvct);

		assertEquals(0xC8, tvct.getTableId());
		assertEquals(0x1234, tvct.getTableIdExtension());
		assertEquals(1, tvct.getNumChannelsInSection());
		assertEquals("WXYZ", channel.getShortName());
		assertEquals(7, channel.getMajorChannelNumber());
		assertEquals(1, channel.getMinorChannelNumber());
		assertEquals("7.1", channel.getChannelNumberString());
		assertEquals(0x04, channel.getModulationMode());
		assertEquals(0x1234, channel.getChannelTsid());
		assertEquals(3, channel.getProgramNumber());
		assertEquals(1, channel.getEtmLocation());
		assertEquals(0, channel.getAccessControlled());
		assertEquals(0, channel.getHidden());
		assertEquals(0, channel.getHideGuide());
		assertEquals(0x02, channel.getServiceType());
		assertEquals(0x1001, channel.getSourceId());
		assertEquals(0x22, channel.getDescriptorsLength());
		assertEquals(2, channel.getDescriptorList().size());
		Descriptor firstDescriptor = channel.getDescriptorList().get(0);
		Descriptor secondDescriptor = channel.getDescriptorList().get(1);
		assertEquals(ExtendedChannelNameDescriptor.class, firstDescriptor.getClass());
		assertEquals(ServiceLocationDescriptor.class, secondDescriptor.getClass());
		assertEquals("WXYZ DT", ((ExtendedChannelNameDescriptor) firstDescriptor).getLongChannelName());
		ServiceLocationDescriptor serviceLocationDescriptor = (ServiceLocationDescriptor) secondDescriptor;
		assertEquals(0x31, serviceLocationDescriptor.getPcrPid());
		assertEquals(2, serviceLocationDescriptor.getNumberElements());
		assertEquals(0x02, serviceLocationDescriptor.getElements().get(0).getStreamType());
		assertEquals(0x31, serviceLocationDescriptor.getElements().get(0).getElementaryPid());
		assertEquals("\0\0\0", serviceLocationDescriptor.getElements().get(0).getIso639LanguageCode());
		assertEquals(0x81, serviceLocationDescriptor.getElements().get(1).getStreamType());
		assertEquals(0x34, serviceLocationDescriptor.getElements().get(1).getElementaryPid());
		assertEquals("eng", serviceLocationDescriptor.getElements().get(1).getIso639LanguageCode());
		assertEquals("7.1 WXYZ DT", atscTables.getServiceNameOptional(3).orElseThrow());
		assertEquals(0, tvct.getAdditionalDescriptorsLength());
		assertEquals(0L, CRCcheck.crc32(section, section.length));
	}

	@Test
	public void keepsVctSectionsWhenLaterVersionAddsSection() {
		ATSCTables atscTables = new ATSCTables(null);

		atscTables.update(new TVCTsection(new PsiSectionData(tvctSection(2, 0, 0, "BASE", 7, 1, 3, 0x1001)), null));
		atscTables.update(new TVCTsection(new PsiSectionData(tvctSection(3, 0, 1, "WXYZ", 7, 1, 3, 0x1001)), null));

		TableModel latestCompleteTable = atscTables.getTvct().getTableModel();
		assertEquals(1, latestCompleteTable.getRowCount());
		assertEquals("BASE", latestCompleteTable.getValueAt(0, findColumn(latestCompleteTable, "short_name")));

		atscTables.update(new TVCTsection(new PsiSectionData(tvctSection(3, 1, 1, "KNEW", 7, 2, 4, 0x1002)), null));

		VCTsection[] sections = atscTables.getTvct().getSections();
		assertEquals(2, sections.length);
		assertEquals(0, sections[0].getSectionNumber());
		assertEquals(1, sections[1].getSectionNumber());

		TableModel latestVersionTable = atscTables.getTvct().getTableModel();
		assertEquals(2, latestVersionTable.getRowCount());
		assertEquals("WXYZ", latestVersionTable.getValueAt(0, findColumn(latestVersionTable, "short_name")));
		assertEquals("KNEW", latestVersionTable.getValueAt(1, findColumn(latestVersionTable, "short_name")));

		TableModel allVersionsTable = atscTables.getTvct().getAllVersionsTableModel();
		assertEquals(3, allVersionsTable.getRowCount());

		KVP treeNode = atscTables.getTvct().getJTreeNode(0);
		assertEquals("version 2", treeNode.getChildAt(0).toString());
		assertEquals("version 3", treeNode.getChildAt(1).toString());

		KVP versionThreeNode = (KVP) treeNode.getChildAt(1);
		TableSource versionTableSource = (TableSource) versionThreeNode.getDetailViews().get(0).detailSource();
		assertEquals(2, versionTableSource.getTableModel().getRowCount());

		KVP sectionOneNode = (KVP) versionThreeNode.getChildAt(1);
		TableSource defaultTableSource = (TableSource) sectionOneNode.getDetailViews().get(0).detailSource();
		assertEquals(2, defaultTableSource.getTableModel().getRowCount());
	}

	@Test
	public void showsAtscProgramsAndChannelsSummary() {
		ATSCTables atscTables = new ATSCTables(null);

		atscTables.update(new TVCTsection(new PsiSectionData(tvctSection(2, 0, 0, "WXYZ", 7, 1, 3, 0x1001)), null));
		atscTables.update(new ATSCEITsection(new PsiSectionData(eitSection(2, 0, 0, 0x1001, 0x0101, "Morning News")), null));

		TableModel tableModel = atscTables.getProgramsTableModel();
		assertEquals(1, tableModel.getRowCount());
		assertEquals("TVCT", tableModel.getValueAt(0, findColumn(tableModel, "VCT")));
		assertEquals("7.1", tableModel.getValueAt(0, findColumn(tableModel, "channel")));
		assertEquals("WXYZ", tableModel.getValueAt(0, findColumn(tableModel, "short_name")));
		assertEquals(3, tableModel.getValueAt(0, findColumn(tableModel, "program_number")));
		assertEquals(0x1001, tableModel.getValueAt(0, findColumn(tableModel, "source_id")));
		assertEquals(1, tableModel.getValueAt(0, findColumn(tableModel, "events")));
		assertEquals("Morning News", tableModel.getValueAt(0, findColumn(tableModel, "current_event")));

		KVP treeNode = atscTables.getJTreeNode(0);
		assertEquals("Programs / Channels", treeNode.getDetailViews().get(0).label());
	}

	@Test
	public void parsesCableVirtualChannelTableOnePartChannelNumber() {
		byte[] section = withCrc(new byte[] {
				(byte) 0xC9, (byte) 0xF0, 0x2D,
				0x12, 0x34, (byte) 0xC1, 0x00, 0x00,
				0x00, 0x01,
				0x00, 0x43, 0x00, 0x41, 0x00, 0x42, 0x00, 0x4C, 0x00, 0x45, 0x00, 0x00, 0x00, 0x00,
				(byte) 0xFF, (byte) 0xC8, 0x05,
				0x03,
				0x00, 0x00, 0x00, 0x00,
				0x12, 0x34,
				0x00, 0x04,
				(byte) 0xBB, (byte) 0xC3,
				0x10, 0x02,
				(byte) 0xFC, 0x00,
				(byte) 0xFC, 0x00,
				0x00, 0x00, 0x00, 0x00
		});

		CVCTsection cvct = new CVCTsection(new PsiSectionData(section), null);
		VCTsection.VirtualChannel channel = cvct.getVirtualChannels().getFirst();

		assertEquals(0xC9, cvct.getTableId());
		assertEquals("CABLE", channel.getShortName());
		assertEquals(0x3F2, channel.getMajorChannelNumber());
		assertEquals(5, channel.getMinorChannelNumber());
		assertEquals(2053, channel.getOnePartChannelNumber());
		assertEquals("2053", channel.getChannelNumberString());
		assertEquals(1, channel.getAccessControlled());
		assertEquals(1, channel.getHidden());
		assertEquals(1, channel.getPathSelect());
		assertEquals(0, channel.getOutOfBand());
		assertEquals(1, channel.getHideGuide());
		assertEquals(0x03, channel.getServiceType());
		assertEquals(0x1002, channel.getSourceId());
		assertEquals(0L, CRCcheck.crc32(section, section.length));
	}

	@Test
	public void parsesRatingRegionTable() {
		byte[] section = withCrc(rrtSection());

		RRTsection rrt = new RRTsection(new PsiSectionData(section), null);
		RRTsection.Dimension dimension = rrt.getDimensions().getFirst();
		RRTsection.RatingValue unrated = dimension.getRatingValues().getFirst();
		RRTsection.RatingValue pg13 = dimension.getRatingValues().get(1);
		ATSCTables atscTables = new ATSCTables(null);
		atscTables.update(rrt);
		TableModel tableModel = atscTables.getRrt().getTableModel();

		assertEquals(0xCA, rrt.getTableId());
		assertEquals(0xFF01, rrt.getTableIdExtension());
		assertEquals(1, rrt.getRatingRegion());
		assertEquals(0, rrt.getProtocolVersion());
		assertEquals("US", rrt.getRatingRegionName());
		assertEquals(1, rrt.getDimensionsDefined());
		assertEquals(0, dimension.getDimensionIndex());
		assertEquals("MPAA", dimension.getDimensionName());
		assertEquals(1, dimension.getGraduatedScale());
		assertEquals(2, dimension.getValuesDefined());
		assertEquals(0, unrated.getRatingValue());
		assertEquals("NR", unrated.getAbbrevRatingValue());
		assertEquals("Not Rated", unrated.getRatingValueTextString());
		assertEquals(1, pg13.getRatingValue());
		assertEquals("PG13", pg13.getAbbrevRatingValue());
		assertEquals("PG-13", pg13.getRatingValueTextString());
		assertEquals(0, rrt.getDescriptorsLength());
		assertEquals(2, tableModel.getRowCount());
		assertEquals("US", tableModel.getValueAt(0, findColumn(tableModel, "rating_region_name")));
		assertEquals("MPAA", tableModel.getValueAt(0, findColumn(tableModel, "dimension_name")));
		assertEquals("PG-13", tableModel.getValueAt(1, findColumn(tableModel, "rating_value_text")));
		assertEquals(0L, CRCcheck.crc32(section, section.length));
	}

	@Test
	public void parsesAtscAc3AudioStreamDescriptor() {
		AtscAC3AudioStreamDescriptor descriptor = new AtscAC3AudioStreamDescriptor(new byte[] {
				(byte) 0x81, 0x0D,
				0x08,
				0x3A,
				0x05,
				(byte) 0xFF,
				0x6F,
				0x05, 0x45, 0x4E,
				(byte) 0xBF,
				0x65, 0x6E, 0x67,
				0x55
		}, null);

		assertEquals(0, descriptor.getSampleRateCode());
		assertEquals(8, descriptor.getBsid());
		assertEquals(14, descriptor.getBitRateCode());
		assertEquals(2, descriptor.getSurroundMode());
		assertEquals(0, descriptor.getBsmod());
		assertEquals(2, descriptor.getNumChannels());
		assertEquals(1, descriptor.getFullSvc());
		assertEquals(Integer.valueOf(0xFF), descriptor.getLangcod());
		assertEquals(Integer.valueOf(3), descriptor.getMainid());
		assertEquals(Integer.valueOf(1), descriptor.getPriority());
		assertEquals("EN", descriptor.getText());
		assertEquals("eng", descriptor.getLanguage());
		assertEquals(1, descriptor.getAdditionalInfo().length);
	}

	@Test
	public void parsesAtscEnhancedAc3AudioDescriptor() {
		AtscEnhancedAC3AudioDescriptor descriptor = new AtscEnhancedAC3AudioDescriptor(new byte[] {
				(byte) 0xCC, 0x0D,
				(byte) 0xFC,
				(byte) 0xD4,
				(byte) 0xB0,
				(byte) 0xEB,
				0x01,
				0x42,
				0x65, 0x6E, 0x67,
				0x73, 0x70, 0x61,
				(byte) 0x99
		}, null);

		assertEquals(1, descriptor.getBsidFlag());
		assertEquals(1, descriptor.getMainidFlag());
		assertEquals(1, descriptor.getAsvcFlag());
		assertEquals(1, descriptor.getMixinfoexists());
		assertEquals(1, descriptor.getSubstream1Flag());
		assertEquals(0, descriptor.getSubstream2Flag());
		assertEquals(0, descriptor.getSubstream3Flag());
		assertEquals(1, descriptor.getFullServiceFlag());
		assertEquals(2, descriptor.getAudioServiceType());
		assertEquals(4, descriptor.getNumberOfChannels());
		assertEquals(1, descriptor.getLanguageFlag());
		assertEquals(0, descriptor.getLanguageFlag2());
		assertEquals(16, descriptor.getBsidOrZeroBits());
		assertEquals(Integer.valueOf(1), descriptor.getPriority());
		assertEquals(Integer.valueOf(3), descriptor.getMainid());
		assertEquals(Integer.valueOf(1), descriptor.getAsvc());
		assertEquals(Integer.valueOf(0x42), descriptor.getSubstream1());
		assertEquals("eng", descriptor.getLanguage());
		assertEquals("spa", descriptor.getSubstream1Lang());
		assertEquals(1, descriptor.getAdditionalInfo().length);
	}

	@Test
	public void parsesTimeShiftedServiceDescriptor() {
		TimeShiftedServiceDescriptor descriptor = new TimeShiftedServiceDescriptor(new byte[] {
				(byte) 0xA2, 0x06,
				(byte) 0xE1,
				(byte) 0xFC, 0x78,
				(byte) 0xF0, 0x28, 0x03
		}, null);

		assertEquals(7, descriptor.getReserved());
		assertEquals(1, descriptor.getNumberOfServices());
		assertEquals(1, descriptor.getServices().size());
		TimeShiftedServiceDescriptor.Service service = descriptor.getServices().getFirst();
		assertEquals(63, service.getReserved1());
		assertEquals(120, service.getTimeShift());
		assertEquals(15, service.getReserved2());
		assertEquals(10, service.getMajorChannelNumber());
		assertEquals(3, service.getMinorChannelNumber());
	}

	@Test
	public void parsesComponentNameDescriptor() {
		ComponentNameDescriptor descriptor = new ComponentNameDescriptor(new byte[] {
				(byte) 0xA3, 0x0C,
				0x01,
				0x65, 0x6E, 0x67,
				0x01,
				0x00, 0x00, 0x04,
				0x4D, 0x61, 0x69, 0x6E
		}, null);

		assertEquals("Main", descriptor.getComponentName());
		assertEquals(1, descriptor.getComponentNameString().getNumberStrings());
	}

	@Test
	public void parsesStuffingDescriptor() {
		StuffingDescriptor descriptor = new StuffingDescriptor(new byte[] {
				(byte) 0x80, 0x03,
				0x11, 0x22, 0x33
		}, null);

		assertEquals(3, descriptor.getStuffingBytes().length);
		assertEquals(0x22, descriptor.getStuffingBytes()[1]);
	}

	@Test
	public void parsesDccRequestDescriptor() {
		DCCRequestDescriptor descriptor = new DCCRequestDescriptor(new byte[] {
				(byte) 0xA8, 0x0C,
				0x02,
				0x0A,
				0x01,
				0x65, 0x6E, 0x67,
				0x01,
				0x00, 0x00, 0x02,
				0x47, 0x6F
		}, null);

		assertEquals(2, descriptor.getRequestType());
		assertEquals(10, descriptor.getRequestTextLength());
		assertEquals("Go", descriptor.getRequestText());
		assertEquals(1, descriptor.getRequestTextStructure().getNumberStrings());
	}

	@Test
	public void parsesRedistributionControlDescriptor() {
		RedistributionControlDescriptor descriptor = new RedistributionControlDescriptor(new byte[] {
				(byte) 0xAA, 0x02,
				0x12, 0x34
		}, null);

		assertEquals(2, descriptor.getRcInformation().length);
		assertEquals(0x34, descriptor.getRcInformation()[1]);
	}

	@Test
	public void parsesAtscPrivateInformationDescriptor() {
		AtscPrivateInformationDescriptor descriptor = new AtscPrivateInformationDescriptor(new byte[] {
				(byte) 0xAD, 0x06,
				0x54, 0x45, 0x53, 0x54,
				0x12, 0x34
		}, null);

		assertEquals(4, descriptor.getFormatIdentifier().length);
		assertEquals(0x54, descriptor.getFormatIdentifier()[0]);
		assertEquals(2, descriptor.getPrivateDataBytes().length);
		assertEquals(0x34, descriptor.getPrivateDataBytes()[1]);
	}

	@Test
	public void parsesCaptionServiceDescriptor() {
		CaptionServiceDescriptor descriptor = new CaptionServiceDescriptor(new byte[] {
				(byte) 0x86, 0x0D,
				(byte) 0xE2,
				0x65, 0x6E, 0x67,
				(byte) 0x81, (byte) 0xC0, (byte) 0xFF,
				0x73, 0x70, 0x61,
				0x3F, 0x00, (byte) 0xFF
		}, null);

		assertEquals(2, descriptor.getNumberOfServices());
		CaptionServiceDescriptor.CaptionService cea708 = descriptor.getServices().get(0);
		assertEquals("eng", cea708.getLanguage());
		assertEquals(1, cea708.getDigitalCc());
		assertEquals(1, cea708.getCaptionServiceNumber());
		assertEquals(1, cea708.getEasyReader());
		assertEquals(1, cea708.getWideAspectRatio());
		CaptionServiceDescriptor.CaptionService cea608 = descriptor.getServices().get(1);
		assertEquals("spa", cea608.getLanguage());
		assertEquals(0, cea608.getDigitalCc());
		assertEquals(1, cea608.getLine21Field());
		assertEquals(0, cea608.getEasyReader());
		assertEquals(0, cea608.getWideAspectRatio());
	}

	@Test
	public void parsesContentAdvisoryDescriptor() {
		ContentAdvisoryDescriptor descriptor = new ContentAdvisoryDescriptor(new byte[] {
				(byte) 0x87, 0x11,
				(byte) 0xC1,
				0x01, 0x02,
				0x00, (byte) 0xF3,
				0x01, (byte) 0xF4,
				0x0A,
				0x01, 0x65, 0x6E, 0x67, 0x01, 0x00, 0x00, 0x02, 0x54, 0x56
		}, null);

		assertEquals(1, descriptor.getRatingRegionCount());
		ContentAdvisoryDescriptor.RatingRegion region = descriptor.getRatingRegions().get(0);
		assertEquals(1, region.getRatingRegion());
		assertEquals(2, region.getRatedDimensions());
		assertEquals(0, region.getDimensions().get(0).ratingDimension());
		assertEquals(3, region.getDimensions().get(0).ratingValue());
		assertEquals(1, region.getDimensions().get(1).ratingDimension());
		assertEquals(4, region.getDimensions().get(1).ratingValue());
		assertEquals("TV", region.getRatingDescriptionText().getText());
		assertEquals("region 1 dimension 0 value 3, region 1 dimension 1 value 4, TV",
				descriptor.getRatingSummaryString());
		ContentAdvisoryDescriptor.RatedDimension ratedDimension =
				new ContentAdvisoryDescriptor.RatedDimension(2, 5);
		assertEquals(2, ratedDimension.ratingDimension());
		assertEquals(5, ratedDimension.ratingValue());
	}

	@Test
	public void parsesGenreDescriptor() {
		GenreDescriptor descriptor = new GenreDescriptor(new byte[] {
				(byte) 0xAB, 0x04,
				(byte) 0xE3,
				0x20, 0x21, 0x22
		}, null);

		assertEquals(3, descriptor.getAttributeCount());
		assertEquals(0x20, descriptor.getAttributes().get(0).attribute());
		assertEquals(0x21, descriptor.getAttributes().get(1).attribute());
		assertEquals(0x22, descriptor.getAttributes().get(2).attribute());
	}

	private static byte[] rrtSection() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xCA);
		out.write(0xF0);
		out.write(0x00);
		out.write(0xFF);
		out.write(0x01);
		out.write(0xC1);
		out.write(0x00);
		out.write(0x00);
		out.write(0x00);
		byte[] regionName = atscString("US");
		out.write(regionName.length);
		out.writeBytes(regionName);
		out.write(0x01);
		byte[] dimensionName = atscString("MPAA");
		out.write(dimensionName.length);
		out.writeBytes(dimensionName);
		out.write(0xF2);
		writeRatingValue(out, "NR", "Not Rated");
		writeRatingValue(out, "PG13", "PG-13");
		out.write(0xFC);
		out.write(0x00);
		out.writeBytes(new byte[4]);
		byte[] section = out.toByteArray();
		int sectionLength = section.length - 3;
		section[1] = (byte) (0xF0 | ((sectionLength >> 8) & 0x0F));
		section[2] = (byte) sectionLength;
		return section;
	}

	private static void writeRatingValue(final ByteArrayOutputStream out, final String abbrev, final String rating) {
		byte[] abbrevText = atscString(abbrev);
		out.write(abbrevText.length);
		out.writeBytes(abbrevText);
		byte[] ratingText = atscString(rating);
		out.write(ratingText.length);
		out.writeBytes(ratingText);
	}

	private static byte[] atscString(final String text) {
		byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0x01);
		out.writeBytes(new byte[] { 0x65, 0x6E, 0x67, 0x01, 0x00, 0x00, (byte) bytes.length });
		out.writeBytes(bytes);
		return out.toByteArray();
	}

	private static byte[] eitSection(final int version, final int sectionNumber, final int lastSectionNumber,
			final int sourceId, final int eventId, final String title) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xCB);
		out.write(0xF0);
		out.write(0x00);
		write16(out, sourceId);
		out.write(0xC0 | ((version & 0x1F) << 1) | 0x01);
		out.write(sectionNumber);
		out.write(lastSectionNumber);
		out.write(0x00);
		out.write(0x01);
		writeEitEvent(out, eventId, title);
		out.writeBytes(new byte[4]);
		byte[] section = out.toByteArray();
		int sectionLength = section.length - 3;
		section[1] = (byte) (0xF0 | ((sectionLength >> 8) & 0x0F));
		section[2] = (byte) sectionLength;
		return withCrc(section);
	}

	private static byte[] eitSection(final int version, final int sectionNumber, final int lastSectionNumber,
			final int sourceId) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xCB);
		out.write(0xF0);
		out.write(0x00);
		write16(out, sourceId);
		out.write(0xC0 | ((version & 0x1F) << 1) | 0x01);
		out.write(sectionNumber);
		out.write(lastSectionNumber);
		out.write(0x00);
		out.write(0x00);
		out.writeBytes(new byte[4]);
		byte[] section = out.toByteArray();
		int sectionLength = section.length - 3;
		section[1] = (byte) (0xF0 | ((sectionLength >> 8) & 0x0F));
		section[2] = (byte) sectionLength;
		return withCrc(section);
	}

	private static void writeEitEvent(final ByteArrayOutputStream out, final int eventId, final String title) {
		write16(out, 0xC000 | (eventId & 0x3FFF));
		write32(out, 1000 + eventId);
		write24(out, 0xC00000 | (0x01 << 20) | 1800);
		byte[] titleBytes = atscString(title);
		out.write(titleBytes.length);
		out.writeBytes(titleBytes);
		write16(out, 0xF000);
	}

	private static byte[] ettSection(final int tableIdExtension, final int sourceId, final int eventId,
			final String text) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xCC);
		out.write(0xF0);
		out.write(0x00);
		write16(out, tableIdExtension);
		out.write(0xC1);
		out.write(0x00);
		out.write(0x00);
		out.write(0x00);
		write32(out, ((long) sourceId << 16) | ((long) eventId << 2) | (eventId == 0 ? 0 : 0x02));
		byte[] textBytes = atscString(text);
		out.writeBytes(textBytes);
		out.writeBytes(new byte[4]);
		byte[] section = out.toByteArray();
		int sectionLength = section.length - 3;
		section[1] = (byte) (0xF0 | ((sectionLength >> 8) & 0x0F));
		section[2] = (byte) sectionLength;
		return withCrc(section);
	}

	private static byte[] tvctSection(final int version, final int sectionNumber, final int lastSectionNumber,
			final String shortName, final int majorChannelNumber, final int minorChannelNumber,
			final int programNumber, final int sourceId) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(0xC8);
		out.write(0xF0);
		out.write(0x00);
		write16(out, 0x1234);
		out.write(0xC0 | ((version & 0x1F) << 1) | 0x01);
		out.write(sectionNumber);
		out.write(lastSectionNumber);
		out.write(0x00);
		out.write(0x01);
		writeTvctChannel(out, shortName, majorChannelNumber, minorChannelNumber, programNumber, sourceId);
		write16(out, 0xFC00);
		out.writeBytes(new byte[4]);
		byte[] section = out.toByteArray();
		int sectionLength = section.length - 3;
		section[1] = (byte) (0xF0 | ((sectionLength >> 8) & 0x0F));
		section[2] = (byte) sectionLength;
		return withCrc(section);
	}

	private static void writeTvctChannel(final ByteArrayOutputStream out, final String shortName,
			final int majorChannelNumber, final int minorChannelNumber, final int programNumber, final int sourceId) {
		byte[] name = shortName.getBytes(StandardCharsets.UTF_16BE);
		for (int i = 0; i < 14; i++) {
			out.write(i < name.length ? name[i] : 0);
		}
		write24(out, 0xF00000 | ((majorChannelNumber & 0x3FF) << 10) | (minorChannelNumber & 0x3FF));
		out.write(0x04);
		write32(out, 0);
		write16(out, 0x1234);
		write16(out, programNumber);
		write16(out, 0x0002);
		write16(out, sourceId);
		write16(out, 0xFC00);
	}

	private static void write16(final ByteArrayOutputStream out, final int value) {
		out.write((value >> 8) & 0xFF);
		out.write(value & 0xFF);
	}

	private static void write24(final ByteArrayOutputStream out, final int value) {
		out.write((value >> 16) & 0xFF);
		out.write((value >> 8) & 0xFF);
		out.write(value & 0xFF);
	}

	private static void write32(final ByteArrayOutputStream out, final long value) {
		out.write((int) ((value >> 24) & 0xFF));
		out.write((int) ((value >> 16) & 0xFF));
		out.write((int) ((value >> 8) & 0xFF));
		out.write((int) (value & 0xFF));
	}

	private static int findColumn(final TableModel tableModel, final String name) {
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			if (name.equals(tableModel.getColumnName(i))) {
				return i;
			}
		}
		return -1;
	}

	private static byte[] withCrc(byte[] section) {
		long crc = CRCcheck.crc32(section, section.length - 4);
		int offset = section.length - 4;
		section[offset] = (byte) (crc >> 24);
		section[offset + 1] = (byte) (crc >> 16);
		section[offset + 2] = (byte) (crc >> 8);
		section[offset + 3] = (byte) crc;
		return section;
	}
}
