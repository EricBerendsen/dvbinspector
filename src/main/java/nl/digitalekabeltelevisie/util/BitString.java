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

import java.nio.BufferUnderflowException;


/**
 * Helper class for manipulating bit strings. Mainly used from within teletext related classes, because the order of bits in teletext is reversed from the normal DVB order.
 * Can be used to concatenate groups of bits together.
 * Not to be used for large chunks of data, because uses a lot of memory; 1 character (2 bytes) for every bit.
 * If you need to just read bits from a byte[] see {@link BitSource} .
 * Uses Strings of '1' and '0'.
 */
public class BitString {
	private final StringBuilder bits;

	/**
	 * @return the noBits
	 */
	public int getNoBits() {
		return bits.length();
	}

	/**
	 * create a BitString from String of '0' and '1's.
	 * @param data
	 */
	public BitString(final String data){
		if(data==null){
			throw new NullPointerException(); // NOPMD by Eric on 23-8-14 13:16
		}
		if(!data.matches("[01]*")){
			throw new IllegalArgumentException("use only '0' and '1' for data.");
		}
		bits=new StringBuilder(data);
	}

	/**
	 * create a BitString from array of bytes, where data[0] is placed at the front (left) of the BitString
	 * @param data
	 */
	public BitString(final byte[] data){
		if(data==null){
			throw new NullPointerException(); // NOPMD by Eric on 23-8-14 13:16
		}
		bits = new StringBuilder();
		for(final byte b:data){
			int t=b;
			if(t<0){ // if negative byte (first bit set) add 256 to make a positive int
				t+=256;
			}
			addIntBits(t, 8);
		}

	}


	public BitString(){
		bits = new StringBuilder();
	}



	/**
	 * add data to the end (right) of this bitString, using numberOfbits bits.
	 * @param data
	 * @param numberOfbits
	 */
	public final void addIntBits(final int data, final int numberOfbits){
		final StringBuilder t = new StringBuilder(Integer.toString(data, 2));
		if(t.length()>numberOfbits)
		{
			throw new IllegalArgumentException("data "+data+" does not fit in "+numberOfbits+" bits");
		}
		final int s = numberOfbits - t.length();
		bits.append("0".repeat(Math.max(0, s)));
		bits.append(t);
	}



	/**
	 * reverse data using numberOfbits bits, then add to the end (right) of this bitString.
	 * @param data
	 * @param numberOfbits
	 */
	public void addIntBitsReverse(final int data, final int numberOfbits){
		final StringBuilder t = new StringBuilder(Integer.toString(data, 2));
		if(t.length()>numberOfbits)
		{
			throw new IllegalArgumentException("data "+data+" does not fit in "+numberOfbits+" bits");
		}
		final int s = numberOfbits - t.length();
		bits.append(t.reverse());
		bits.append("0".repeat(Math.max(0, s)));

	}

	/**
	 * get numberOfBits bits from start (left) of this string, and return as an integer (might be negative)
	 * @param numberOfBits
	 * @return
	 */
	public int getIntBits(final int numberOfBits){
		checkLength(numberOfBits);

		final int result = Integer.parseInt(bits.substring(0, numberOfBits),2);
		bits.delete(0,numberOfBits);
		return result;
	}

	/**
	 * get numberOfBits bits from start (left) of this string, reverse order, and return as an integer (might be negative)
	 * @param numberOfBits
	 * @return
	 */
	public int getIntBitsReverse(final int numberOfBits){
		checkLength(numberOfBits);

		final StringBuilder t = new StringBuilder(bits.substring(0, numberOfBits));
		final int result = Integer.parseInt(t.reverse().toString(),2);
		bits.delete(0,numberOfBits);
		return result;
	}

	private void checkLength(final int numberOfBits) {
		if(numberOfBits>bits.length()){
			throw new BufferUnderflowException();
		}
		if(numberOfBits>Integer.SIZE){
			throw new IllegalArgumentException("int can contain only "+Integer.SIZE+" bits.");
		}
		if(numberOfBits<0){
			throw new IllegalArgumentException("negative number of bits "+numberOfBits);
		}
	}

	@Override
	public String toString(){
		return bits.toString();
	}

	/**
	 * get numberOfBits bits from end (right) of this string, and return as an integer (might be negative)
	 * @param numberOfBits
	 * @return
	 */
	public int getIntBitsEnd(final int numberOfBits){
		checkLength(numberOfBits);

		final int totalBits = bits.length();
		final int result = Integer.parseInt(bits.substring(totalBits- numberOfBits,totalBits),2);
		bits.delete(totalBits- numberOfBits,totalBits);
		return result;
	}

}
