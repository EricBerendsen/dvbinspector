package nl.digitalekabeltelevisie.data.mpeg.pes.smpte;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.pes.smpte.AncillaryDataPacket.DataWord;
import nl.digitalekabeltelevisie.util.BitSource;

public class Smpte2038Test {

	@Test
	public void basicTest() {

		final byte[] data = { (byte) 0b0000_0010, (byte) 0b0011_0001, (byte) 0b0110_0010, (byte) 0b1100_0101,
				(byte) 0b1000_1011, (byte) 0b0001_0110, (byte) 0b0010_0000, (byte) 0b011_1000, (byte) 0b1011_0000,
				(byte) 0b0110_0001, (byte) 0b1100_0011, (byte) 0b1000_0111, (byte) 0b0000_1111 };

		final int offset = 0;

        final BitSource bs = new BitSource(data, offset);
        
		final AncillaryDataPacket anc = new AncillaryDataPacket(bs);

		Assert.assertEquals(anc.getB0(), 0);
		Assert.assertEquals(anc.getC_not_y_channel_flag(), 1);
		Assert.assertEquals(anc.getLine_number(), 197);
		Assert.assertEquals(anc.getHorizontal_offset(), 2225);
		Assert.assertEquals(anc.getDID() & 0xFF, 139);
		Assert.assertEquals(anc.getSDID() & 0xFF, 88);
		Assert.assertEquals(anc.getData_count() & 0xFF, 3);

		final List<DataWord> user_data_words = anc.getUser_data_word();
        final DataWord userDataWords0 = new DataWord(0b1000101100);
        final DataWord userDataWords1 = new DataWord(0b0001100001); 
        final DataWord userDataWords2 = new DataWord(0b1100001110); 

		Assert.assertEquals(user_data_words.get(0), userDataWords0);
		Assert.assertEquals(user_data_words.get(1), userDataWords1);
		Assert.assertEquals(user_data_words.get(2), userDataWords2);

		Assert.assertEquals(anc.getChecksum_word(), 112);
	}

}
