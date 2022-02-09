/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  (C) RIEDEL Communications Canada, Inc. All rights reserved
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

package nl.digitalekabeltelevisie.util;

import java.io.IOException;
import java.util.Arrays;

/**
 * Convenience class used to read a byte array. Automatically increment read index
 * on read and assumes values are unsigned.
 *
 * @author Simon Provost
 */
public class ByteBufferReader {
    private final byte[] buffer;
    private int readOffset;

    public ByteBufferReader(byte[] buffer) {
        this(buffer, 0);
    }

    public ByteBufferReader(byte[] buffer, int readOffset) {
        this.buffer = buffer;
        this.readOffset = readOffset;
    }

    public void setReadOffset(int readOffset) {
        this.readOffset = readOffset;
    }

    public long read(int readSize) throws IOException {
        if (readOffset + readSize > buffer.length) {
            throw new IOException(new ArrayIndexOutOfBoundsException("Cannot read " + readSize + " at offset " +
                    readOffset + ". Buffer only has " + buffer.length + " bytes"));
        }

        long value = Utils.getLong(buffer, readOffset, readSize, Long.MAX_VALUE);
        readOffset += readSize;
        return value;
    }

    public byte[] readRemaining() {
        return readOffset < buffer.length ? Arrays.copyOfRange(buffer, readOffset, buffer.length) : new byte[0];
    }
}
