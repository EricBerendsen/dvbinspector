package nl.digitalekabeltelevisie.CLI;

import org.junit.Assert;
import org.junit.Test;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.util.CLI.DVBinspectorCLI;
import nl.digitalekabeltelevisie.util.CLI.beans.RangeData;

public class DVBinspectorCLITest {

    DVBinspectorCLI dvbInspectorCLI = new DVBinspectorCLI(null);

    @Test
    public void extractOffsetValuesTest() throws Exception {

        final String fileName = "./src/test/resources/digitenne 482000 06-30 19-43-54.ts";
        final TransportStream ts = new TransportStream(fileName);
        ts.parseStream();

        final String offset = "2k";
        final String length = "3p";

        RangeData offsetValues = dvbInspectorCLI.extractOffsetValues(offset, ts);
        RangeData lengthValues = dvbInspectorCLI.extractOffsetValues(length, ts);

        Long offsetValue = (long) 2 * 1024;
        Long lengthValue = (long) 3 * 188;

        Assert.assertEquals(offsetValue, offsetValues.getByteValue());
        Assert.assertEquals("Kb", offsetValues.getUnit());

        Assert.assertEquals(lengthValue, lengthValues.getByteValue());
        Assert.assertEquals("packets", lengthValues.getUnit());
    }
}
