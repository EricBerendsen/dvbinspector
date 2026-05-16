package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.CRCcheck;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.CaptionServiceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ExtendedChannelNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ServiceLocationDescriptor;

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
		assertEquals(0L, CRCcheck.crc32(section, section.length));
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
		assertEquals(0L, CRCcheck.crc32(section, section.length));
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
