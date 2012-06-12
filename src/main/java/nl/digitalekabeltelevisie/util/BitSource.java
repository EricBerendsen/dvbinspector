/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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


/**
 * wrapper around a byte[] to read bits at a time.
 * based on http://zxing.googlecode.com/svn/trunk/core/src/com/google/zxing/common/BitSource.java
 * see http://code.google.com/p/zxing/source/browse/trunk/core/src/com/google/zxing/common/BitSource.java
 * 
 * <p>This provides an easy abstraction to read bits at a time from a sequence of bytes, where the
 * number of bits read is not often a multiple of 8.</p>
 *
 * <p>This class is thread-safe but not reentrant. Unless the caller modifies the bytes array
 * it passed in, in which case all bets are off.</p>
 * 
 * 
 */
public class BitSource {
	private final byte[] bytes;
	private int byteOffset = 0;
	private int bitOffset = 0;

	/**
	 * @param bytes bytes from which this will read bits. Bits will be read starting from the offset byte first.
	 * Bits are read within a byte from most-significant to least-significant bit.
	 */
	public BitSource(final byte[] bytes,final int offset) {
		this.bytes = bytes;
		this.byteOffset = offset;
	}

	/**
	 * read bits from source, consuming them (no longer available)
	 * @param bits number of bits to read
	 * @return
	 */
	public int readBits(final int bits) {
		int numBits = bits;
		if ((numBits < 1) || (numBits > 32)) {
			throw new IllegalArgumentException();
		}

		int result = 0;

		// First, read remainder from current byte
		if (bitOffset > 0) {
			final int bitsLeft = 8 - bitOffset;
			final int toRead = numBits < bitsLeft ? numBits : bitsLeft;
			final int bitsToNotRead = bitsLeft - toRead;
			final int mask = (0xFF >> (8 - toRead)) << bitsToNotRead;
			result = (bytes[byteOffset] & mask) >> bitsToNotRead;
			numBits -= toRead;
			bitOffset += toRead;
			if (bitOffset == 8) {
				bitOffset = 0;
				byteOffset++;
			}
		}

		// Next read whole bytes
		if (numBits > 0) {
			while (numBits >= 8) {
				result = (result << 8) | (bytes[byteOffset] & 0xFF);
				byteOffset++;
				numBits -= 8;
			}

			// Finally read a partial byte
			if (numBits > 0) {
				final int bitsToNotRead = 8 - numBits;
				final int mask = (0xFF >> bitsToNotRead) << bitsToNotRead;
				result = (result << numBits) | ((bytes[byteOffset] & mask) >> bitsToNotRead);
				bitOffset += numBits;
			}
		}

		return result;
	}

	/**
	 * @return number of bits that can be read successfully
	 */
	public int available() {
		return (8 * (bytes.length - byteOffset)) - bitOffset;
	}

	public int getNextFullByteOffset() {
		return (bitOffset == 0)? byteOffset : (byteOffset +1);
	}

	/**
	 * preview bits from source, without removing them
	 * @param bits
	 * @return
	 */
	public int nextBits(final int bits) {
		int numBits = bits;
		int localBitOffset = this.bitOffset;
		int localByteOffset = this.byteOffset;

		if ((numBits < 1) || (numBits > 32)) {
			throw new IllegalArgumentException();
		}

		int result = 0;

		// First, read remainder from current byte
		if (localBitOffset > 0) {
			final int bitsLeft = 8 - localBitOffset;
			final int toRead = numBits < bitsLeft ? numBits : bitsLeft;
			final int bitsToNotRead = bitsLeft - toRead;
			final int mask = (0xFF >> (8 - toRead)) << bitsToNotRead;
			result = (bytes[localByteOffset] & mask) >> bitsToNotRead;
			numBits -= toRead;
			localBitOffset += toRead;
			if (localBitOffset == 8) {
				localBitOffset = 0;
				localByteOffset++;
			}
		}

		// Next read whole bytes
		if (numBits > 0) {
			while (numBits >= 8) {
				result = (result << 8) | (bytes[localByteOffset] & 0xFF);
				localByteOffset++;
				numBits -= 8;
			}

			// Finally read a partial byte
			if (numBits > 0) {
				final int bitsToNotRead = 8 - numBits;
				final int mask = (0xFF >> bitsToNotRead) << bitsToNotRead;
				result = (result << numBits) | ((bytes[localByteOffset] & mask) >> bitsToNotRead);
				localBitOffset += numBits;
			}
		}

		return result;
	}


}
