package nl.digitalekabeltelevisie.util;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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


import static org.junit.Assert.assertEquals;

import org.junit.*;

import java.time.LocalDateTime;
import java.util.HexFormat;

public class UtilsTest {

    static boolean oldEnableSecondsTimestamp;

    @BeforeClass
    public static void setup(){
        oldEnableSecondsTimestamp = PreferencesManager.isEnableSecondsTimestamp();
    }

    @AfterClass
    public static void teardown(){
        PreferencesManager.setEnableSecondsTimestamp(oldEnableSecondsTimestamp);
    }
    // start_time: 0xD6E5092900 "...)." => 2009/07/01 09:29:00
    static final byte[] JULY_FIRST = HexFormat.of().parseHex("D6E5092900");

    // start_time: 0xEC4E053900 ".N.9." => 2024/07/03 05:39:00

    static final byte[] JULY_THIRD = HexFormat.of().parseHex("EC4E053900");

    // UTC_time: 0xE7BD192825 "...(%" => 2021/04/21 19:28:25
    static final byte[] APRIL_21 = HexFormat.of().parseHex("E7BD192825");

    static long MAX_PCR =  2_576_980_377_599L;

    @Test
    public void getUTCFormattedString() {
        assertEquals(Utils.getUTCFormattedString(JULY_FIRST),"2009/07/01 09:29:00");
        assertEquals(Utils.getUTCFormattedString(JULY_THIRD),"2024/07/03 05:39:00");
        assertEquals(Utils.getUTCFormattedString(APRIL_21),"2021/04/21 19:28:25");
    }

    @Test
    public void getUTCLocalDateTime() {

        LocalDateTime res = Utils.getUTCLocalDateTime(JULY_FIRST);
        LocalDateTime expected = LocalDateTime.of(2009,7,1,9,29,0);
        assertEquals(res,expected);
    }

    @Test

    public void printPCRTime(){

        PreferencesManager.setEnableSecondsTimestamp(true);
        assertEquals("0.000000",Utils.printPCRTime(0L));
        assertEquals("0.015712",Utils.printPCRTime(424242L));
        assertEquals("15712.682379",Utils.printPCRTime(424242424242L));
        assertEquals("95443.717688",Utils.printPCRTime(MAX_PCR));
        PreferencesManager.setEnableSecondsTimestamp(false);
        assertEquals("0:00:00.000000",Utils.printPCRTime(0L));
        assertEquals("0:00:00.015712",Utils.printPCRTime(424242L));
        assertEquals("4:21:52.682379",Utils.printPCRTime(424242424242L));
        assertEquals("26:30:43.717688",Utils.printPCRTime(MAX_PCR));


    };



}