package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.CRCcheck;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;

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
