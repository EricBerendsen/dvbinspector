/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.Arrays;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.gui.HTMLSource;


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
public class BitSource implements HTMLSource{
	private final byte[] bytes;
	private int byteOffset = 0;
	private int bitOffset = 0;

	private final int lastBytePlusOne;


	public static final int[] powerOf2 = {1,2,4,8, 16,32,64,128,
		256,512,1024,2048, 4096,8192,16384,32768,
		65536,131072,262144,524288, 1048576,2097152,4194304,8388608,
		16777216,33554432,67108864,134217728, 268435456, 536870912,1073741824};

	/**
	 * @param bytes bytes from which this will read bits. Bits will be read starting from the offset byte first.
	 * Bits are read within a byte from most-significant to least-significant bit.
	 */
	public BitSource(final byte[] bytes,final int offset) {
		this.bytes = bytes;
		this.byteOffset = offset;
		this.lastBytePlusOne=bytes.length;
	}


	/**
	 * Warning, there is no check for reading past len,  except when underlying bytes array runs out, then there is an ArrayIndexOutOfBounds
	 * @param bytes
	 * @param offset
	 * @param lastBytePlusOne index after last byte to be used, so NOT len of usefull bytes. ie to read bytes 5..8 use offset = 5, and len = 9 (NOT 4!)
	 */
	public BitSource(final byte[] bytes,final int offset,final int lastBytePlusOne) {
		this.bytes = bytes;
		this.byteOffset = offset;
		this.lastBytePlusOne=lastBytePlusOne;
	}

	
	/**
	 * @param src
	 * @param len number of whole bytes to read, so if src is not byteAligned, the first partial byte does not count 
	 */
	public BitSource(BitSource src, final int len) {
		this.bytes = src.bytes;
		this.byteOffset = src.byteOffset;
		this.bitOffset = src.bitOffset;
		this.lastBytePlusOne = src.byteOffset + len;
	}
	
	public BitSource(BitSource src) {
		this.bytes = src.bytes;
		this.byteOffset = src.byteOffset;
		this.bitOffset = src.bitOffset;
		this.lastBytePlusOne = src.lastBytePlusOne;
	}


	/**
	 * read bits from source, consuming them (no longer available)
	 * @param bits number of bits to read
	 * @return
	 */
	public int readBits(final int bits) {
		int numBits = bits;
		if ((numBits < 0) || (numBits > 32)) {
			throw new IllegalArgumentException();
		}

		int result = 0;

		// First, read remainder from current byte
		if (bitOffset > 0) {
			final int bitsLeft = 8 - bitOffset;
			final int toRead = Math.min(numBits, bitsLeft);
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

		return result;
	}

	/**
	 * read bits from source, consuming them (no longer available)
	 * @param bits number of bits to read
	 * @return
	 */
	public long readBitsLong(final int bits) {
		int numBits = bits;
		if ((numBits < 0) || (numBits > 64)) {
			throw new IllegalArgumentException();
		}

		long result = 0;

		// First, read remainder from current byte
		if (bitOffset > 0) {
			final int bitsLeft = 8 - bitOffset;
			final int toRead = Math.min(numBits, bitsLeft);
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

		return result;
	}

	/**
	 * read entire bytes from source, starting at new byte, consuming them (no longer available)
	 * if not at start of new byte (offset <>0) remainder bits are discarded without warning.
	 *
	 * @param noBytes
	 * @return
	 */
	public byte[] readBytes(final int noBytes) {
		byte[] result = null;

		skiptoByteBoundary();

		// Next read whole bytes
		if(noBytes>0){
			result = Arrays.copyOfRange(bytes, byteOffset,byteOffset+noBytes);
			byteOffset+=noBytes;
		}


		return result;
	}


	public byte[] readUnalignedBytes(final int noBytes) {
		byte[] result = new byte[noBytes];
		for (int i = 0; i < noBytes; i++) {
			result[i] = (byte) readBits(8);
		}
		
		return result;
	}

	public void advanceBytes(int number) {
		skiptoByteBoundary();
		byteOffset += number;
	}	
	
	/**
	 * skip remainder from current byte, if any 
	 */
	public void skiptoByteBoundary() {
		if (bitOffset > 0) {
			bitOffset = 0;
			byteOffset++;
		}
	}

	public boolean isByteAligned() {
		return (bitOffset == 0);
	}
	
	public DVBString readDVBString() {
		DVBString result = null;

		skiptoByteBoundary();

		// Next read whole bytes
		result = new DVBString(bytes, byteOffset);
		final int noBytes = result.getLength() + 1;
		byteOffset+=noBytes;


		return result;
	}


	/**
	 * @return number of bits that can be read successfully
	 */
	public int available() {
		return (8 * (lastBytePlusOne - byteOffset)) - bitOffset;
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
			final int toRead = Math.min(numBits, bitsLeft);
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

	public int readSignedInt(final int numBits) {
		if (numBits < 2) {
			throw new IllegalArgumentException("signed int should be at least 2 bits");
		}
		if (numBits >31) {
			throw new IllegalArgumentException("signed int can have at most 31 bits");
		}
		int i = readBits(numBits);

		if(i>=powerOf2[numBits-1]){
			i -= powerOf2[numBits];
		}
		return i;
	}

	public byte readSignedByte(final int numBits) {
		if (numBits < 2) {
			throw new IllegalArgumentException("signed byte should be at least 2 bits");
		}
		if (numBits >8) {
			throw new IllegalArgumentException("signed byte can have at most 8 bits");
		}
		final int i = readSignedInt(numBits);


		return (byte)i;
	}

	// unsigned integer using n bits
	public int u(final int v){
		return readBits(v);
	}

	// signed integer using n bits
	public int i(final int v){
		return readSignedInt(v);
	}

	// fixed-pattern bit string using n bits written (from left to right) with the left bit first. The parsing process for
	// this descriptor is specified by the return value of the function read_bits( n ).
	public int f(final int v){
		return readBits(v);
	}


	private int getCodeNum() {
		int leadingZeroBits = -1;
		int b = 0;
		while(b==0){
			b = readBits(1);
			leadingZeroBits++;
		}
		return (powerOf2[leadingZeroBits] - 1) + readBits(leadingZeroBits);

	}

	// h264 9.1 Parsing process for Exp-Golomb codes
	public int ue(){
		return getCodeNum();

	}

	// h264 9.1.1 Mapping process for signed Exp-Golomb codes

	public int se(){
		final int codeNum = getCodeNum();
		final int absVal = (codeNum+1)/2;
		final int sign = ((codeNum%2)==0)?-1:1;
		return absVal*sign;

	}

	public String toString(){
		final StringBuilder b = new StringBuilder("BitSource: ");
		if(bytes!=null){
			b.append("bytes.length=").append(bytes.length);
		}else{
			b.append("bytes == null");
		}
		b.append(", byteOffset=").append(byteOffset);
		b.append(", bitOffset=").append(bitOffset);

		return b.toString();
	}
	
	
	@Override
	public String getHTML() {
		StringBuilder b = new StringBuilder();
		int localByteOffset = byteOffset;
		if (bitOffset != 0) {
			int bitsLeft = 8 - bitOffset;
			int intValueWholeByte = Byte.toUnsignedInt(bytes[localByteOffset]);
			String binaryString = Integer.toBinaryString(intValueWholeByte);
			String binaryStringPadded = ("0000000"+binaryString).substring(binaryString.length()- bitsLeft+7);
			b.append(bitsLeft).append(" bits left of ").append(intValueWholeByte).append(" ( 0b").append(binaryStringPadded).append(")");
			b.append("<br><br>");
			localByteOffset++;

		}
		if (bytes != null) {
			b.append(Utils.getHTMLHexview(bytes, localByteOffset, lastBytePlusOne - localByteOffset));
		} else {
			b.append("bytes == null");
		}

		return b.toString();
	}
	
	
	// based on ETSI TS 103 190-1 V1.3.1 (2018-02)
	// 4.2.2 variable_bits - Variable bits
	// used for AC-4 PES
	
	public int variable_bits(int n_bits)
	{
		int b_read_more;
		int value = 0;
		do {
			value += readBits(n_bits) ; 
			b_read_more = readBits(1) ; 
			if (b_read_more==1) { 
				value <<= n_bits;
				value += (1<<n_bits);
			}
		} while(b_read_more==1);
		return value;
		}
}
