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

import static java.lang.Byte.toUnsignedInt;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.tree.DefaultMutableTreeNode;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;
import nl.digitalekabeltelevisie.gui.DVBtree;

/**
 * Only static helper methods
 *
 * @author Eric Berendsen
 *
 */
public final class Utils {

	
	/**
	 * Helper int[] to reverse order of bits in a National Option Set 
	 */

	public static int[] invNationalOptionSet = {
			0b000, 
			0b100,
			0b010, 
			0b110,
			0b001,
			0b101,
			0b011,
			0b111 
	};


	/**
	 *
	 */
	private Utils() {
		// private to avoid instantion
	}


	private static final Logger	logger	= Logger.getLogger(Utils.class.getName());
	private static final ClassLoader classL = Utils.class.getClassLoader();


	public static final int MASK_1BIT=0x01;
	public static final int MASK_2BITS=0x03;
	public static final int MASK_3BITS=0x07;
	public static final int MASK_4BITS=0x0F;
	public static final int MASK_5BITS=0x1F;
	public static final int MASK_6BITS=0x3F;
	public static final int MASK_7BITS=0x7F;
	public static final int MASK_8BITS=0xFF;
	public static final int MASK_9BITS=0x01FF;
	public static final int MASK_10BITS=0x03FF;
	public static final int MASK_12BITS=0x0FFF;
	public static final int MASK_13BITS=0x1FFF;
	public static final int MASK_14BITS=0x3FFF;
	public static final int MASK_15BITS=0x7FFF;
	public static final int MASK_16BITS=0xFFFF;
	public static final int MASK_18BITS=0x3_FFFF;
	public static final int MASK_20BITS=0xF_FFFF;
	public static final int MASK_22BITS=0x3F_FFFF;
	public static final int MASK_24BITS=0xFF_FFFF;
	public static final int MASK_31BITS=0x7FF_FFFFF;

	public static final int MASK_32BITS=0xFFFF_FFFF;
	public static final long MASK_33BITS=0x1_FFFF_FFFFL;
	public static final long MASK_40BITS=0xFF_FFFF_FFFFL;
	public static final long MASK_48BITS=0xDDFF_FFFF_FFFFL;
	public static final long MASK_64BITS=0xFFFF_FFFF_FFFF_FFFFL;

	private static final Map<Integer, String>oui = new HashMap<>();
	private static final RangeHashMap<Integer,String> bat = new RangeHashMap<>();
	private static final RangeHashMap<Integer,String> dataBroadcast = new RangeHashMap<>();
	private static final RangeHashMap<Integer,String> ca_system_id = new RangeHashMap<>();
	private static final RangeHashMap<Integer,String> original_network_id = new RangeHashMap<>();
	private static final RangeHashMap<Integer,String> platform_id = new RangeHashMap<>();
	private static final RangeHashMap<Integer,String> cni = new RangeHashMap<>();
	private static final RangeHashMap<Integer,String> app_type_id = new RangeHashMap<>();
	private static final RangeHashMap<Long,String> mhp_organisation_id = new RangeHashMap<>();
	private static final RangeHashMap<Integer,String> itu35_country_code = new RangeHashMap<>();

	private static final RangeHashMap<Long,String> private_data_spec_id = new RangeHashMap<>();

	private static final DecimalFormat f2 = new DecimalFormat("00");
	private static final DecimalFormat f4 = new DecimalFormat("0000");
	private static final DecimalFormat f6 = new DecimalFormat("000000");


	private static final char[] hexChars = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F' };


	static{
		readOIUCsv("res/oui.csv", oui);

		readCSVIdString("res/bouquet_id.csv",bat);
		readCSVIdString("res/data_broadcast_id.csv",dataBroadcast);
		readCSVIdString("res/ca_system_id.csv",ca_system_id);
		readCSVIdString("res/original_network_id.csv",original_network_id);
		readCSVIdString("res/platform_id.csv",platform_id);
		readCSVIdString("res/ni.csv",cni);
		readCSVIdString("res/app_type_id.csv",app_type_id);
		readCSVIdString("res/itu35country_codes.csv",itu35_country_code);
		readCSVIdLongString("res/mhp_organisation_id.csv",mhp_organisation_id);
		readCSVIdLongString("res/private_data_spec_id.csv", private_data_spec_id);

	}

	
	private static void readOIUCsv(final String fileName, final Map<Integer,String> m) {
		try (final CSVReader reader = new CSVReader(new InputStreamReader(classL.getResourceAsStream(fileName), StandardCharsets.UTF_8))){

			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				// value[0] is MA-L
				final int key = Integer.parseInt(nextLine[1],16);
				final String name = nextLine[2]; //Organization Name
				m.put(key,name);

			}

		}catch(final IOException | CsvValidationException e){
			logger.severe("There was a problem reading file: \""+fileName +"\", exception:"+ e);
		}
	}
	

	private static void readCSVIdString(final String fileName, final RangeHashMap<Integer,String> m) {
		try (final CSVReader reader = new CSVReader(new InputStreamReader(classL.getResourceAsStream(fileName), StandardCharsets.UTF_16))){

			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				final int lower = decode(nextLine[0]);
				final int upper = decode(nextLine[1]);

				m.put(lower, upper, nextLine[2]);

			}

		}catch(final IOException | CsvValidationException e){
			logger.severe("There was a problem reading file: \""+fileName +"\", exception:"+ e);
		}
	}

	private static int decode(final String text) {
		return (text.startsWith("0b")||text.startsWith("0B")) ? Integer.parseInt(text.substring(2), 2)
				: Integer.decode(text);
	}

	private static void readCSVIdLongString(final String fileName, final RangeHashMap<Long,String> m) {
		try (final CSVReader reader = new CSVReader(new InputStreamReader(classL.getResourceAsStream(fileName), StandardCharsets.UTF_16))){
			String [] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				// nextLine[] is an array of values from the line
				final long lower = Long.decode(nextLine[0]);
				final long upper = Long.decode(nextLine[1]);

				m.put(lower, upper, nextLine[2]);

			}
		}catch(final IOException | NumberFormatException | CsvValidationException e){
			logger.severe("There was a problem reading file: \""+fileName +"\", exception:"+ e);
		}
	}

	public static Image readIconImage(final String fileName) {
		Image image = null;
		try(InputStream fileInputStream = classL.getResourceAsStream(fileName)){
			image = ImageIO.read(fileInputStream);
		} catch (final Exception e) {
			logger.log(Level.WARNING, "Error reading icon image: exception:{0}", e);
		}
		return image;
	}

	public static String getOUIString(final int i) {
		String r = oui.get(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getBouquetIDString(final int i) {
		String r = bat.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}


	public static String getDataBroadCastIDString(final int i) {
		String r = dataBroadcast.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getCASystemIDString(final int i) {
		String r = ca_system_id.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getOriginalNetworkIDString(final int i) {
		String r = original_network_id.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getPlatformIDString(final int i) {
		String r = platform_id.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getPrivateDataSpecString(final long l) {
		String r = private_data_spec_id.find(l);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getNIString(final int l) {
		String r = cni.find(l);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getAppTypeIDString(final int i) {
		String r = app_type_id.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getItu35CountryCodeString(final int i) {
		String r = itu35_country_code.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getMHPOrganistionIdString(final long i) {
		String r = mhp_organisation_id.find(i);
		if (r==null){
			r="unknown";
		}
		return r;
	}

	public static String getActionTypeString(final int actionType) {

		switch (actionType) {
		case 0x00: return "reserved";
		case 0x01: return "location of IP/MAC streams in DVB networks";
		default: return "reserved for future use";
		}
	}

	public static String getUNTActionTypeString(final int actionType) {

		switch (actionType) {
		case 0x00: return "reserved";
		case 0x01: return "System Software Update";
		default: return "reserved for future use";
		}
	}
	public static String getUNTProcessingOrderString(final int p) {

		if((p>=0x01)&&(p<=0xfe)){
			return "subsequent actions (ascending)";
		}else if(p == 0x00){
			return "first action";
		}else{
			return "reserved for future use";
		}
	}
	/**
	 * Converts a byte array to hex string
	 */
	public static String toHexString(final byte[] block) {
		final StringBuilder buf = new StringBuilder();
		if(block==null){
			return buf.toString();
		}
		int high = 0;
		int low = 0;
		for (byte b : block) {
			high = ((b & 0xf0) >> 4);
			low = (b & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		return buf.toString();
	}

	public static String toHexString(final long l, final int pos){
		String r ="0000000000000000"+ Long.toHexString(l);
		r="0x"+ r.substring(r.length()-pos);
		return r;
	}

	public static String toHexStringUnformatted(final long l, final int pos){
		String r ="0000000000000000"+ Long.toHexString(l);
		r = r.substring(r.length()-pos);
		return r;
	}

	public static String toHexString(final byte[] block, final int l) {
		final StringBuilder buf = new StringBuilder();
		if(block==null){
			return buf.toString();
		}
		int high = 0;
		int low = 0;
		for (int i = 0; i < l; i++) {
			high = ((block[i] & 0xf0) >> 4);
			low = (block[i] & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		return buf.toString();
	}

	public static String toHexString(final byte[] block,final int offset, final int l) {
		final StringBuilder buf = new StringBuilder();
		if(block==null){
			return buf.toString();
		}
		int high = 0;
		int low = 0;
		final int end = Math.min(block.length, offset+l);
		for (int i = offset; i < end; i++) {
			high = ((block[i] & 0xf0) >> 4);
			low = (block[i] & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		return buf.toString();
	}

	
	public static String toBinaryString(final long l, final int pos){
		String r ="00000000000000000000000000000000"+ Long.toBinaryString(l);
		r="0b"+ r.substring(r.length()-pos);
		return r;
	}

	/**
	 * convert integer between 0 and 255 (inclusive) into byte (byte is interpreted as unsigned, even though that does not exist in java)
	 *
	 * @param b
	 * @return
	 */

	public static byte getInt2UnsignedByte(final int b){
		if(b<=127){
			return (byte)b;
		}
		return (byte) (b-256);
	}


	/**
	 * Get an positive integer from array of bytes
	 *
	 * @param bytes
	 * @param offset starting position
	 * @param len number of bytes to interpret
	 * @param mask bitmask to select which bits to use
	 * @return
	 */
	public static int getInt(final byte[] bytes, final int offset, final int len, final int mask){
		int r=0;
		for (int i = 0; i < len; i++) {
			r = (r<<8) | toUnsignedInt(bytes[offset+i]);
		}
		return (r&mask);
	}

	/**
	 * @param bytes
	 * @param offset in bytes array where to start
	 * @param len length in bytes (ec 2 bytes if you need 12 bits)
	 * @param mask used to remove unwanted bits
	 * @return
	 */
	public static long getLong(final byte[] bytes, final int offset, final int len, final long mask){
		long r=0;
		for (int i = 0; i < len; i++) {
			r = (r<<8) | toUnsignedInt(bytes[offset+i]);
		}
		return (r&mask);
	}
	
	public static BigInteger getBigInteger(final byte[] bytes, final int offset, final int len){
		return new BigInteger(1,Arrays.copyOfRange(bytes,offset,offset+len));
		
	}

	/**
	 * Get single bit from a byte
	 * Numbering starts from high order bit, starts at 1.
	 *
	 * @param b single byte
	 * @param i position of bit in byte, start from 1 up to 8
	 * @return 0 or 1 bit value
	 */
	public static int getBit(final byte b, final int i) {
		return (( b & (0x80 >> (i-1))));
	}

	/**
	 * Get sequence of bits from a byte
	 * @param b
	 * @param i position of the starting bit
	 * @param len number of bits to get starting from i
	 * @return sequence of bits as int
	 * @example To get bit 3 and 4 call as getBits(b, 3, 2)
	 *
	 */
	public static int getBits(final byte b, final int i, final int len) {
		int mask = 0x00;

		for(int pos = i; pos < (i+len); ++pos)
		{
			mask |= 0x80 >> (pos-1);
		}

		return (b & mask) >> (9 - i - len);
	}

	/**
	 * Convert byte[] into string with BCD representation. Each byte consists of two nibbles.
	 * Real BCD should only contain values 0 - 9 in each nibble. 0xA - 0xF are illegal. This method will allow them but log an error.
	 * So it should not be abused for hex formatting.
	 * 
	 * @param b byte array 
	 * @param startNibbleNo offset of first nibble. Note: not in bytes, so if you want to start at byte x, specify x*2
	 * @param len number of nibbles needed
	 * @return String with length len
	 */
	public static String getBCD(final byte[] b, final int startNibbleNo, final int len) {
		final StringBuilder buf =  new StringBuilder();
		for (int i = 0; i < len; i++) {
			final int byteNo=(startNibbleNo+i)/2;
			final boolean shift=((startNibbleNo+i)%2)==0;
			int t;
			if(shift){
				t= (toUnsignedInt(b[byteNo]) & 0xF0)>>4;
			}else{
				t= (toUnsignedInt(b[byteNo]) & 0x0F);
			}
			if(t>9){
				logger.warning("Error parsing BCD: "+toHexString(b)+" ,nibble_no: "+startNibbleNo+" ,len: "+len);
				//return "Error parsing BCD";
			}
			buf.append(Integer.toString(t,16));
		}
		return buf.toString();
	}

	/**
	 * returns a copy of bytes from b, starting at offset with total length len. returns empy [] when len == 0
	 * @param b
	 * @param offset
	 * @param len
	 * @return
	 */
	public static byte[] getBytes(final byte[] b, final int offset, final int len) {
		if(len==0){
			return new byte[0];
		}
		return Utils.copyOfRange(b, offset, offset+len);
	}


	public static String getEscapedHTML(final List<DVBString> dvbStrings, final int maxWidth){
		StringBuilder raw = new StringBuilder();
		for(DVBString str:dvbStrings) {
			raw.append(str.toRawString());
		}
		String rawString = raw.toString();
		
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		int currentLineLength = 0;
		int rawLength = raw.length();
		while(i < rawLength) {
			StringBuilder plainSection = new StringBuilder();
			while (i < rawLength && !isControlCharacter(rawString.charAt(i))) {
				plainSection.append(rawString.charAt(i));
				i++;
			}
			final StringTokenizer st = new StringTokenizer(plainSection.toString());

			// append words to sb as long as lineLen < max
			while (st.hasMoreTokens()) {
				final String s = st.nextToken();
				if((maxWidth!=0)&& ((currentLineLength+s.length())>maxWidth)){
					sb.append("<br>").append(escapeHTML(s));
					currentLineLength=s.length();
				}else{
					sb.append(' ').append(escapeHTML(s));
					currentLineLength+=1+s.length();
				}
			}
			// now the special chars 0x80 - 0x97 (or EOF)
			while ((i < rawLength) && (isControlCharacter(rawString.charAt(i)))) { 
				if(rawString.charAt(i)==0x8A){ // 0x8A, CR/LF
					sb.append("<br>");
					currentLineLength=0;
				}else if(rawString.charAt(i) == 0x86){ // 0x86, character emphasis on
					sb.append("<em>");
				}else if(rawString.charAt(i) == 0x87){ // 0x87, character emphasis off
					sb.append("</em>");
				}
				i++;
			}


		}
		return sb.toString();
	}


	/**
	 *
	 * Parse an array of bytes into a java String, according to ETSI EN 300 468 V1.11.1 Annex A (normative): Coding of text characters
	 * Control chars (0x80.. 0x9f) are removed
	 *
	 * @param b array of source bytes
	 * @param off offset where relevant data starts in array b
	 * @param len number of bytes to be parsed
	 * @return a java String, according to ETSI EN 300 468 V1.11.1 Annex A,
	 */
	public static String getString(final byte[] b, final int off, final int len) {
		String decoded = getCharDecodedStringWithControls(b, off, len);
		return removeControlChars(decoded);

	}


	/**
	 * remove formatting, like newlines and character emphasis 
	 * see table A.1 ETSI EN 300 468 V1.16.1 (2019-08)
	 * @param decoded
	 * @return
	 */
	private static String removeControlChars(String decoded) {
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < decoded.length(); i++) {
			final char c = decoded.charAt(i);
			if(!isControlCharacter(c)) {
				result.append(c);
			}
		}
		
		return result.toString();
	}


	private static boolean isControlCharacter(final char c) {
		return (c >= 0x80) && (c <= 0x9f);
	}


	public static String getCharDecodedStringWithControls(final byte[] b, final int off, final int len) {
		int length = len;
		int offset = off;
		if(length<=0){
			return "";
		}
		final Charset charset = getCharSet(b, offset, length);
		final int charSetLen = getCharSetLen(b, offset);

		length -= charSetLen;
		offset += charSetLen;
		
		String decoded;

		if(charset==null){
			decoded = Iso6937ToUnicode.convert(b, offset, length); //default for DVB
		}else{
			decoded = new String(b, offset, length,charset);
		}
		return decoded;
	}


	public static Charset getCharSet(final byte[] b, final int offset, final int length){
		Charset charset = null;
		if((length>0)&&(b[offset]<0x20)&&(b[offset]>=0)){ //Selection of character table
			final int selectorByte=b[offset];
			try {
				if((selectorByte>0)&&(selectorByte<=0x0b)){
					charset = Charset.forName("ISO-8859-"+(selectorByte+4));
				}else if((selectorByte==0x10)){
					if(b[offset+1]==0x0){
						charset = Charset.forName("ISO-8859-"+b[offset+2]);
					} // else == reserved for future use, so not implemented
				}else if((selectorByte==0x11 )){ // ISO/IEC 10646
					charset = StandardCharsets.UTF_16;
				}else if((selectorByte==0x14 )){ // Big5 subset of ISO/IEC 10646
					charset = Charset.forName("Big5");
				}else if((selectorByte==0x15 )){ // UTF-8 encoding of ISO/IEC 10646
					charset = StandardCharsets.UTF_8;
				}
			} catch (IllegalArgumentException e) {
				logger.info("IllegalArgumentException in getCharSet:"+e);
				charset = StandardCharsets.ISO_8859_1;
			}
			if(charset==null){
				charset = StandardCharsets.ISO_8859_1;
			}
		}
		return charset;
	}

	private static int getCharSetLen(final byte[] b, final int offset){
		int charsetLen = 0;
		if((b[offset]<0x20)&&(b[offset]>=0)){ //Selection of character table
			final int selectorByte=b[offset];
			if((selectorByte>0)&&(selectorByte<=0x0b)){
				charsetLen = 1;
			}else if((selectorByte==0x10)){
				if(b[offset+1]==0x0){
					charsetLen = 3;
				}
			}else if((selectorByte==0x11 )){ // ISO/IEC 10646
				charsetLen = 1;
			}else if((selectorByte==0x14 )){ // Big5 subset of ISO/IEC 10646
				charsetLen = 1;
			}else if((selectorByte==0x15 )){ // UTF-8 encoding of ISO/IEC 10646
				charsetLen = 1;
			}else if((selectorByte==0x1F )){ // described by encoding_type_id
				charsetLen = 2;
			}
		}
		return charsetLen;
	}


	public static String getISO8859_1String(final byte[] b, final int offset, final int length) {
		if(length<=0){
			return "";
		}
		return new String(b, offset, length, StandardCharsets.ISO_8859_1);
	}


	public static String toCodePointString(final String in){
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < in.length(); i++) {
			b.append( Integer.toHexString(in.codePointAt(i))).append(" ");
		}
		return b.toString();
	}


	public static String getUTCFormattedString(final byte[] UTC_time) {
		final long mjd = getLong(UTC_time, 0, 2, 0xFFFF);
		final String hours = getBCD(UTC_time, 4, 2);
		final String minutes = getBCD(UTC_time, 6, 2);
		final String secs= getBCD(UTC_time, 8, 2);

		// algo: ETSI EN 300 468 - ANNEX C

		long y =  (long) ((mjd  - 15078.2) / 365.25);
		long m =  (long) ((mjd - 14956.1 - (long)(y * 365.25) ) / 30.6001);
		final long d =  (mjd - 14956 - (long)(y * 365.25) - (long)(m * 30.6001));
		final long k =  ((m == 14) || (m == 15)) ? 1 : 0;
		y = y + k + 1900;
		m = m - 1 - (k*12);


		return y +"/"+df2pos.format(m)+"/"+df2pos.format(d)+" "+hours+":"+minutes+":"+secs;
	}
	
	public static String getEITStartTimeAsString(final byte[] UTC_time) {
		if(isUndefined(UTC_time)){
			return "undefined";
		}
		return getUTCFormattedString(UTC_time);
	}
	
	/**
	 * Determines if the byte[] contains only FF (unsigned)
	 * returns true if byte[] is empty
	 *
	 * @param uTC_time
	 * @return true if all elements are 0xFF (unsigned) 
	 */
	public static boolean isUndefined(byte[] uTC_time) {
		for (byte b : uTC_time) {
			if (b != -1) {
				return false;
			}
		}
		return true;
	}



	public static Date getUTCDate(final byte[] UTC_time) {
		final Calendar t = getUTCCalender(UTC_time);
		if(t!=null){
			return t.getTime();
		}
		return null;
	}

	public static long getDurationMillis(final String eventDuration){
		final int hours = Integer.parseInt(eventDuration.substring(0, 2));
		final int minutes = Integer.parseInt(eventDuration.substring(2, 4));
		final int seconds = Integer.parseInt(eventDuration.substring(4, 6));
		return 1000*(((( hours*60) +minutes)* 60) + seconds);
	}

	/**
	 * create Calender from time as specified in ETSI EN 300 468 - ANNEX C 
	 * @param UTC_time
	 * @return  Calender from time as specified in ETSI EN 300 468 - ANNEX C, null if parsing failed (incorrect BCD)
	 */
	public static Calendar getUTCCalender(final byte[] UTC_time) {
		final long mjd = getLong(UTC_time, 0, 2, 0xFFFF);
		final String hours = getBCD(UTC_time, 4, 2);
		final String minutes = getBCD(UTC_time, 6, 2);
		final String secs= getBCD(UTC_time, 8, 2);

		// algo: ETSI EN 300 468 - ANNEX C

		long y =  (long) ((mjd  - 15078.2) / 365.25);
		long m =  (long) ((mjd - 14956.1 - (long)(y * 365.25) ) / 30.6001);
		final long d =  (mjd - 14956 - (long)(y * 365.25) - (long)(m * 30.6001));
		final long k =  ((m == 14) || (m == 15)) ? 1 : 0;
		y = y + k + 1900;
		m = m - 1 - (k*12);

		try{
			final int h= Integer.parseInt(hours);
			final int mins = Integer.parseInt(minutes);
			final int s =Integer.parseInt(secs);
			return new GregorianCalendar((int)y, (int)m-1, (int)d, h, mins, s);

		}catch(final NumberFormatException ne)
		{
			logger.log(Level.WARNING, "error parsing calendar:", ne);
			return null;
		}


	}


	public static String getStreamTypeString(final int tag){
		switch (tag) {
		case 0x00: return"ITU-T | ISO/IEC Reserved";
		case 0x01: return"ISO/IEC 11172 Video";
		case 0x02: return"ITU-T Rec. H.262 | ISO/IEC 13818-2 Video or ISO/IEC 11172-2 constrained parameter video stream";
		case 0x03: return"ISO/IEC 11172 Audio";
		case 0x04: return"ISO/IEC 13818-3 Audio";
		case 0x05: return"ITU-T Rec. H.222.0 | ISO/IEC 13818-1 private_sections";
		case 0x06: return"ITU-T Rec. H.222.0 | ISO/IEC 13818-1 PES packets containing private data";
		case 0x07: return"ISO/IEC 13522 MHEG";
		case 0x08: return"ITU-T Rec. H.222.0 | ISO/IEC 13818-1 Annex A DSM-CC";
		case 0x09: return"ITU-T Rec. H.222.1";
		case 0x0A: return"Multi-protocol Encapsulation";
		case 0x0B: return"DSM-CC U-N Messages";
		case 0x0C: return"DSM-CC Stream Descriptors";
		case 0x0D: return"DSM-CC Sections (any type, including private data)";
		case 0x0E: return"ITU-T Rec. H.222.0 | ISO/IEC 13818-1 auxiliary";
		case 0x0F: return"ISO/IEC 13818-7 Audio with ADTS transport syntax";
		case 0x10: return"ISO/IEC 14496-2 Visual";
		case 0x11: return"ISO/IEC 14496-3 Audio with the LATM transport syntax as defined in ISO/IEC 14496-3 / AMD 1";
		case 0x12: return"ISO/IEC 14496-1 SL-packetized stream or FlexMux stream carried in PES packets";
		case 0x13: return"ISO/IEC 14496-1 SL-packetized stream or FlexMux stream carried in ISO/IEC14496_sections.";
		case 0x14: return"DSM-CC Synchronized Download Protocol";

		// 29n9184t.doc Text of ISO/IEC 13818-1:2007/FPDAM 3.2 - Transport of Scalable Video over ITU-T Rec H.222.0 | ISO/IEC 13818-1
		// Amendment 3: Transport of Scalable Video over ITU-T Rec H.222.0 | ISO/IEC 13818-1
		// ISO/IEC 13818-1:2007/FPDAM 3.2

		case 0x15: return"Metadata carried in PES packets";
		case 0x16: return"Metadata carried in metadata_sections";
		case 0x17: return"Metadata carried in ISO/IEC 13818-6 Data Carousel";
		case 0x18: return"Metadata carried in ISO/IEC 13818-6 Object Carousel";
		case 0x19: return"Metadata carried in ISO/IEC 13818-6 Synchronized Download Protocol";
		case 0x1A: return"IPMP stream (defined in ISO/IEC 13818-11, MPEG-2 IPMP)";
		case 0x1B: return"AVC video stream as defined in ITU-T Rec. H.264 | ISO/IEC 14496-10 Video, or AVC base layer of an HEVC video stream as defined in ITU-T H.265 | ISO/IEC 23008-2 ";
		case 0x1C: return"ISO/IEC 14496-3 Audio, without using any additional transport syntax, such as DST, ALS and SLS";
		case 0x1D: return"ISO/IEC 14496-17 Text";
		case 0x1E: return"Auxiliary video stream as defined in ISO/IEC 23002-3";
		case 0x1F: return"SVC video sub-bitstream of a video stream as defined in the Annex G of ITU-T Rec. H.264 | ISO/IEC 14496-10 Video";

		case 0x20: return"MVC video sub-bitstream of an AVC video stream conforming to one or more profiles defined in Annex H of ITU-T Rec. H.264 | ISO/IEC 14496-10";
		case 0x21: return"J2K Video stream conforming to one or more profiles as defined in ITU-T Rec T.800 | ISO/IEC 15444-1";
		case 0x22: return"Additional view Rec. ITU-T H.262 | ISO/IEC 13818-2 video stream for service-compatible stereoscopic 3D services";
		case 0x23: return"Additional view Rec. ITU-T H.264 | ISO/IEC 14496-10 video stream conforming to one or more profiles defined in Annex A for service-compatible stereoscopic 3D services";
		case 0x24: return"ITU-T H.265 | ISO/IEC 23008-2 video stream or an HEVC temporal video sub-bitstream";
		case 0x25: return"HEVC temporal video subset of an HEVC video stream conforming to one or more profiles defined in Annex A of Rec. ITU-T H.265 | ISO/IEC 23008-2";
		case 0x26: return "TMVCD video sub-bitstream of an AVC video stream conforming to one or more profiles defined in Annex I of Rec. ITU-T H.264 | ISO/IEC 14496-10";
		// ISO/IEC 13818-1:2015/Amd.1:2015 (E) /R ec. ITU-T H.222.0 (2014)/Amd.1 (04/2015)
		case 0x27: return "Timeline and External Media Information Stream"; 
		// ISO/IEC 13818-1:2015/Amd.2:2016 (E) / Rec. ITU-T H.222.0 (2014)/Amd.2 (12/2015)
		case 0x28: return "HEVC enhancement sub-partition which includes TemporalId 0 of an HEVC video stream where all NALs units contained in the stream conform to one or more profiles defined in Annex G of Rec. ITU-T H.265 | ISO/IEC 23008-2 ";
		case 0x29: return "HEVC temporal enhancement sub-partition of an HEVC video stream where all NAL units contained in the stream conform to one or more profiles defined in Annex G of Rec. ITU-T H.265 | ISO/IEC 23008-2";
		case 0x2a: return "HEVC enhancement sub-partition which includes TemporalId 0 of an HEVC video stream where all NAL units contained in the stream conform to one or more profiles defined in Annex H of Rec. ITU-T H.265 | ISO/IEC 23008-2";
		case 0x2b: return "HEVC temporal enhancement sub-partition of an HEVC video stream where all NAL units contained in the stream conform to one or more profiles defined in Annex H of Rec. ITU-T H.265 | ISO/IEC 23008-2";
		
		
		//ISO/IEC 13818-1:2018 (E)
		case 0x2c: return "Green access units carried in MPEG-2 sections";
		case 0x2d: return "ISO/IEC 23008-3 Audio with MHAS transport syntax – main stream";
		case 0x2e: return "ISO/IEC 23008-3 Audio with MHAS transport syntax – auxiliary stream";
		case 0x2f: return "Quality access units carried in sections";
		//ISO/IEC 13818-1:2019/Amd.1:2020(E)
		case 0x30: return "Media Orchestration Access Units carried in sections";
		case 0x31: return "Substream of a Rec. ITU-T H.265 | ISO/IEC 23008 2 video stream that contains a Motion Constrained Tile Set, parameter sets, slice headers or a combination thereof.";
		case 0x32: return "JPEG XS video stream conforming to one or more profiles as defined in ISO/IEC 21122-2";

		case 0x7f: return"IPMP stream";

		// sources https://www.wikiwand.com/en/Program-specific_information
		// https://fossies.org/linux/MediaInfo_CLI/MediaInfoLib/Source/MediaInfo/Multiple/File_Mpeg_Psi.cpp
		
		case 0x80: return"User Private / MPEG Video (ATSC) / PCM (HDMV)";
		case 0x81: return"User Private / AC-3 (ATSC/HDMV)";
		case 0x82: return"User Private / SCTE-27 subtitling / DTS 6 ch (HDMV)";
		case 0x83: return"User Private / Isochronous Data (SCTE) / AC-3 (TrueHD) (HDMV)";
		case 0x84: return"User Private / E-AC-3 up to 16 ch. (HDMV)";
		case 0x85: return"User Private / Program Identifier (SCTE) / DTS 8 ch.(HD-HRA) (HDMV)";
		case 0x86: return"User Private / SCTE-35 splice_info_section / DTS 8 ch. (HD-MA) (HDMV)";
		case 0x87: return"User Private / E-AC-3 (ATSC)";

		case 0x88: return"User Private / Microsoft Windows Media Video 9 (VC-1) (lower bit-rate video)";

		case 0x90: return"User Private / Time Slicing - MPE-FEC (DVB) / Presentation Graphic Stream (subtitling) (HDMV)";
		case 0x91: return"User Private / Presentation Graphic Stream (subtitling) (HDMV)";
		case 0x92: return"User Private / Subtitle text (TEXTST) (HDMV)";

		case 0x95: return"User Private / Data Service Table, Network Resources Table (ATSC)";

		case 0xA1: return"User Private / AC-3 (HDMV)";
		case 0xA2: return"User Private / DTS (HDMV)";
		
		case 0xC0: return"User Private / DigiCipher II text";
		
		case 0xC1: return"User Private / Dolby Digital (AC-3) up to six channel audio with AES-128-CBC data encryption";
		case 0xC2: return"User Private / DSM CC synchronous data (ATSC) / Dolby Digital Plus up to 16 channel audio with AES-128-CBC data encryption";
		case 0xCF: return"User Private / ISO/IEC 13818-7 ADTS AAC with AES-128-CBC frame encryption";
		case 0xD1: return"User Private / BBC Dirac (Ultra HD video)";
		case 0xDB: return"User Private / ITU-T Rec. H.264 and ISO/IEC 14496-10 with AES-128-CBC slice encryption";
		case 0xEA: return"User Private / Microsoft Windows Media Video 9 (VC-1) (lower bit-rate video)";
		

		default:
			if((0x2c<=tag)&&(tag<=0x7e)){
				return "ITU-T Rec. H.222.0 | ISO/IEC 13818-1 Reserved";
			}

			if((0x80<=tag)&&(tag<=0xFF)){
				return "User Private";
			}

			
			return "illegal/unknown value";
		}
	}


	// ETSI EN 300 706 V1.2.1 §11.3.3 Page Function Coding
	public static String getMIPPageFunctionString(final int pageCode){
		if((0x02<=pageCode)&&(pageCode<=0x4f)){
			return "Normal page, #sub pages "+pageCode;
		}

		if((0x52<=pageCode)&&(pageCode<=0x6f)){
			return "Reserved";
		}


		if((0x70<=pageCode)&&(pageCode<=0x77)){
			return "Subtitle page";
		}
		if((0x82<=pageCode)&&(pageCode<=0xcf)){
			return "TV schedule pages, multi-page set, #sub pages "+(pageCode-0x80);
		}
		if((0xf0<=pageCode)&&(pageCode<=0xf3)){
			return "Systems Pages for Broadcasters use (downstream processing)";
		}
		if((0xf4<=pageCode)&&(pageCode<=0xf6)){
			return "Engineering Test pages";
		}

		switch (pageCode) {
		case 0x00: return"Page not in transmission";
		case 0x01: return"Single normal page";
		case 0x50: return"Normal page, multi-page set Sub-pages in the range 80 to 2^12-1";
		case 0x51: return"Normal page, multi-page set Sub-pages in the range 2^12 to 2^13-2";
		case 0x78: return"Subtitle Menu Page";
		case 0x79: return"Page not following normal sub-code rules";
		case 0x7a: return"TV programme related warning page";
		case 0x7b: return"Current TV Programme information, multi-page set";
		case 0x7c: return"Current TV Programme information, single page";
		case 0x7d: return"\"Now and Next\" TV Programmes";
		case 0x7e: return"Index page to TV-related pages, multi-page set";
		case 0x7f: return"Index page to TV-related pages, single page";
		case 0x80: return"Page transmitted but NOT part of the public service";
		case 0x81: return"Single Page containing TV schedule information";
		case 0xd0: return"TV schedule pages, multi-page set, Sub-pages in the range 80 to 2^12-1";
		case 0xd1: return"TV schedule pages, multi-page set, Sub-pages in the range 2^12 to 2^13-2";
		case 0xe0: return"Page Format - CA - data broadcasting page, Sub-pages in the range 1 to 2^12-1";
		case 0xe1: return"Page Format - CA - data broadcasting page, Sub-pages in the range 2^12 to 2^13-2";
		case 0xe2: return"Page Format - CA - data broadcasting page, Number of sub-pages not defined in packets with Y = 15 to Y = 24";
		case 0xe3: return"Page Format - Clear data broadcasting page including EPG data";
		case 0xe4: return"Page Format - Clear data broadcasting page but not carrying EPG data";
		case 0xe5: return"DRCS page (use not defined)";
		case 0xe6: return"Object page (use not defined)";
		case 0xe7: return"Systems page without displayable element. Function defined by page number";
		case 0xe8: return"DRCS page referenced in the MOT for this magazine";
		case 0xe9: return"DRCS page referenced in the MOT for this magazine but not required by a page in this magazine";
		case 0xea: return"DRCS page referenced in the MOT for a different magazine but not required by a page in this magazine";
		case 0xeb: return"DRCS page not referenced in the MOT for a different magazine and required by a page in another magazine";
		case 0xec: return"Object page referenced in the MOT for this magazine";
		case 0xed: return"Object page referenced in the MOT for this magazine but not required by a page in this magazine";
		case 0xee: return"Object page referenced in the MOT for a different magazine but not required by a page in this magazine";
		case 0xef: return"Object page not referenced in the MOT for a different magazine and required by a page in another magazine";
		case 0xf7: return"Systems page with displayable element. Function defined by page number";
		case 0xf8: return"Keyword Search list page, multi-page set";
		case 0xf9: return"Keyword Search list page, single page";
		case 0xfc: return"Trigger message page";
		case 0xfd: return"Automatic Channel Installation (ACI)";
		case 0xfe: return"TOP page (BTT, AIT, MPT or MPT-EX)";


		default:
			return "Reserved";
		}
	}

	public static String getStreamTypeShortString(final int tag){
		switch (tag) {
		case 0x00: return"ITU-T | ISO/IEC Reserved";
		case 0x01: return"Video MPEG1";
		case 0x02: return"Video MPEG2";
		case 0x03: return"Audio MPEG1";
		case 0x04: return"Audio MPEG2";
		case 0x05: return"private_sections MPEG2";
		case 0x06: return"PES packets private data";
		case 0x07: return"MHEG";
		case 0x08: return"ITU-T Rec. H.222.0 | ISO/IEC 13818-1 Annex A DSM-CC";
		case 0x09: return"ITU-T Rec. H.222.1";
		case 0x0A: return"Multi-protocol Encapsulation";
		case 0x0B: return"DSM-CC U-N Messages";
		case 0x0C: return"DSM-CC Stream Descriptors";
		case 0x0D: return"DSM-CC Sections (any)";
		case 0x0E: return"ITU-T Rec. H.222.0 | ISO/IEC 13818-1 auxiliary";
		case 0x0F: return"ISO/IEC 13818-7 Audio with ADTS transport syntax";
		case 0x10: return"ISO/IEC 14496-2 Visual";
		case 0x11: return"Audio AAC";
		case 0x12: return"ISO/IEC 14496-1 SL-packetized stream or FlexMux stream carried in PES packets";
		case 0x13: return"ISO/IEC 14496-1 SL-packetized stream or FlexMux stream carried in ISO/IEC14496_sections.";
		case 0x14: return"DSM-CC Synchronized Download Protocol";

		// 29n9184t.doc Text of ISO/IEC 13818-1:2007/FPDAM 3.2 - Transport of Scalable Video over ITU-T Rec H.222.0 | ISO/IEC 13818-1
		// Amendment 3: Transport of Scalable Video over ITU-T Rec H.222.0 | ISO/IEC 13818-1
		// ISO/IEC 13818-1:2007/FPDAM 3.2

		case 0x15: return"Metadata in PES packets";
		case 0x16: return"Metadata in metadata_sections";
		case 0x17: return"Metadata 13818-6 Data Carousel";
		case 0x18: return"Metadata 13818-6 Object Carousel";
		case 0x19: return"Metadata 13818-6 Synchronized Download Protocol";
		case 0x1A: return"IPMP stream (13818-11, MPEG-2 IPMP)";
		case 0x1B: return"Video H.264";
		case 0x1C: return"ISO/IEC 14496-3 Audio, like DST, ALS and SLS";
		case 0x1D: return"ISO/IEC 14496-17 Text";
		case 0x1E: return"ISO/IEC 23002-3 Aux. video stream ";
		case 0x1F: return"ISO/IEC 14496-10 Video sub-bitstream";
		/* ISO/IEC 13818-1:2007/FPDAM5 */
		case 0x20: return"MVC video sub-bitstream";
		case 0x21: return"J2K Video stream";
		case 0x22: return"H.262 video stream for 3D services";
		case 0x23: return"H.264 video stream for 3D services";
		case 0x24: return"Video HEVC";
		case 0x25: return"H.265 temporal video subset";
		case 0x26: return"MVCD video sub-bitstream of an AVC video stream";
		// ISO/IEC 13818-1:2015/Amd.1:2015 (E) /R ec. ITU-T H.222.0 (2014)/Amd.1 (04/2015)
		case 0x27: return "TEMI Stream"; 
		// ISO/IEC 13818-1:2015/Amd.2:2016 (E) / Rec. ITU-T H.222.0 (2014)/Amd.2 (12/2015)
		case 0x28: return "HEVC enhancement sub-partition which includes TemporalId 0 of an HEVC video stream where all NALs units contained in the stream conform to one or more profiles defined in Annex G of Rec. ITU-T H.265 | ISO/IEC 23008-2 ";
		case 0x29: return "HEVC temporal enhancement sub-partition of an HEVC video stream where all NAL units contained in the stream conform to one or more profiles defined in Annex G of Rec. ITU-T H.265 | ISO/IEC 23008-2";
		case 0x2a: return "HEVC enhancement sub-partition which includes TemporalId 0 of an HEVC video stream where all NAL units contained in the stream conform to one or more profiles defined in Annex H of Rec. ITU-T H.265 | ISO/IEC 23008-2";
		case 0x2b: return "HEVC temporal enhancement sub-partition of an HEVC video stream where all NAL units contained in the stream conform to one or more profiles defined in Annex H of Rec. ITU-T H.265 | ISO/IEC 23008-2";
		
		
		//ISO/IEC 13818-1:2018 (E)
		case 0x2c: return "Green access units carried in MPEG-2 sections";
		case 0x2d: return "ISO/IEC 23008-3 Audio with MHAS transport syntax – main stream";
		case 0x2e: return "ISO/IEC 23008-3 Audio with MHAS transport syntax – auxiliary stream";
		case 0x2f: return "Quality access units carried in sections";

		//ISO/IEC 13818-1:2019/Amd.1:2020(E)
		case 0x30: return "Media Orchestration Access Units carried in sections";
		case 0x31: return "Substream of a Rec. ITU-T H.265 | ISO/IEC 23008 2 video stream that contains a Motion Constrained Tile Set, parameter sets, slice headers or a combination thereof.";
		case 0x32: return "JPEG XS video stream conforming to one or more profiles as defined in ISO/IEC 21122-2";
		
		case 0x7f: return"IPMP stream";
		

		// sources https://www.wikiwand.com/en/Program-specific_information
		// https://fossies.org/linux/MediaInfo_CLI/MediaInfoLib/Source/MediaInfo/Multiple/File_Mpeg_Psi.cpp
		
		case 0x80: return"MPEG Video / PCM";
		case 0x81: return"AC-3 (ATSC)";
		case 0x82: return"SCTE-27 subtitling / DTS 6 ch";
		case 0x83: return"Isochronous Data (SCTE) / AC-3";
		case 0x84: return"E-AC-3 up to 16 ch.";
		case 0x85: return"Program Identifier / DTS 8 ch.";
		case 0x86: return"SCTE-35 splice_info_section / DTS 8 ch.";
		case 0x87: return"E-AC-3 (ATSC)";

		case 0x88: return"VC-1";

		case 0x90: return"Time Slicing - MPE-FEC (DVB) / PGS subtitling)";
		case 0x91: return"PGS subtitling";
		case 0x92: return"Subtitle text (TEXTST)";

		case 0x95: return"Data Service Table, Network Resources Table";

		case 0xA1: return"AC-3";
		case 0xA2: return"DTS";
		
		case 0xC0: return"DigiCipher II text";
		
		case 0xC1: return"AC-3 with AES-128-CBC data encryption";
		case 0xC2: return"DSM CC synchronous data / Dolby Digital Plus with AES-128-CBC data encryption";
		case 0xCF: return"ISO/IEC 13818-7 ADTS AAC with AES-128-CBC frame encryption";
		case 0xD1: return"Dirac video";
		case 0xDB: return"H.264 and ISO/IEC 14496-10 with AES-128-CBC slice encryption";
		case 0xEA: return"VC-1";
		
		

		default:
			
			if((0x27<=tag)&&(tag<=0x7e)){
				return "ISO/IEC 13818-1 Reserved";
			}

			if((0x80<=tag)&&(tag<=0xFF)){
				return "User Private";
			}

			return "illegal value";
		}
	}


	/**
	 * ETR 162 and EN 300 472 and EN 301 775
	 * based on dvb_str.c
	 * @param dataId
	 * @return
	 */
	public static String getDataIDString(final int dataId){

		if((0x00<=dataId)&&(dataId<=0x0f)){
			return "Reserved";
		}
		if((0x10<=dataId)&&(dataId<=0x1f)){
			return "EBU data EN 300 472/ EN 301 775 (teletext, VPS, WSS)";
		}
		if((0x23<=dataId)&&(dataId<=0x7f)){
			return "Reserved";
		}
		if((0x80<=dataId)&&(dataId<=0x98)){
			return "user defined";
		}
		if((0x99<=dataId)&&(dataId<=0x9b)){
			return "EBU teletext/VPS/WSS/closed caption/VBI sample data";
		}
		if((0x9c<=dataId)&&(dataId<=0xff)){
			return "user defined";
		}

		switch (dataId) {
		case 0x20: return"DVB subtitling EN 300 743";
		case 0x21: return"DVB synchronous data stream";
		case 0x22: return"DVB synchronized data stream";

		default:
			return "illegal value";
		}
	}


	/**
	 * Add a list of DefaultMutableTreeNodes to parent. if modus != simple all items are put below an intermediate node with label 'label'. If modus is simple all items are added directly to parent
	 * @param parent
	 * @param itemList
	 * @param modus
	 * @param label
	 */
	public static <U extends TreeNode>void addListJTree(final DefaultMutableTreeNode parent,final Collection<U> itemList, final int modus, final String label) {
		if((itemList!=null)&&(itemList.size()!=0)){
			if(simpleModus(modus)){ // simple layout
				addToList(parent, itemList, modus);
			}else{
				final DefaultMutableTreeNode descriptorListNode = new DefaultMutableTreeNode(new KVP(label +": "+ itemList.size()+" entries"));
				addToList(descriptorListNode, itemList, modus);
				parent.add(descriptorListNode);
			}
		}
	}



	public static <U> void addToList(final DefaultMutableTreeNode parent,
			final Collection<U> itemList, final int modus) {
		if(countListModus(modus)){
			int count = 0;
			for (final U u : itemList) {
				final DefaultMutableTreeNode node = ((TreeNode) u).getJTreeNode(modus);
				final Object userObject = node.getUserObject();
				if (userObject instanceof KVP) {
					final KVP kvp = (KVP)userObject;
					kvp.appendLabel(" ["+ count +"]");
					count++;
				}
				parent.add(node);
			}
		}else{
			for (final U u : itemList) {
				parent.add(((TreeNode) u).getJTreeNode(modus));
			}
		}
	}

	/**
	 * convert array of bytes into a String, ascii only. replace non ascii character with a dot '.'.
	 * @param block
	 * @return
	 */
	public static String toSafeString(final byte[] block) {
		final StringBuilder buf = new StringBuilder();
		if(block==null){
			return buf.toString();
		}

		for (byte b : block) {
			if ((32 <= toUnsignedInt(b)) && (toUnsignedInt(b) <= 127)) {
				buf.append((char) b);
			} else {
				buf.append('.');
			}
		}
		return buf.toString();
	}

	/**
	 * convert array of bytes into a String, ascii only. replace non ascii character with a dot '.'.
	 * @param block
	 * @param offset
	 * @param len
	 * @return
	 */
	public static String toSafeString(final byte[] block, final int offset, final int len) {
		final StringBuilder buf = new StringBuilder();
		if(block==null){
			return buf.toString();
		}
		final int end = Math.min(block.length, offset+len);
		for (int i = offset; i < end; i++) {
			final byte b=block[i];
			if((32<=toUnsignedInt(b))&&(toUnsignedInt(b)<127)){
				buf.append((char)(b));
			}else{
				buf.append('.');
			}
		}
		return buf.toString();
	}

	/**
	 * create a copy of a part of a byte[], use Arrays.copyOfRange instead
	 * @param original
	 * @param from
	 * @param to
	 * @return
	 */
	@Deprecated
	public static byte[] copyOfRange(final byte[] original, final int from, final int to) {
		final int newLength = to - from;
		if (newLength < 0) {
			throw new IllegalArgumentException(from + " > " + to);
		}
		final byte[] copy = new byte[newLength];
		System.arraycopy(original, from, copy, 0,
				Math.min(original.length - from, newLength));
		return copy;
	}


	/**
	 * replace 'html'characters in the string with their html-entity
	 * Output is now safe for use in HTML fragments
	 *
	 * @param s
	 * @return
	 */
	public static String escapeSimpleHTML(final String s){
		final StringBuilder sb = new StringBuilder();
		if(s==null){
			return "";
		}
		final int n = s.length();
		for (int i = 0; i < n; i++) {
			final char c = s.charAt(i);
			switch (c) {
			case '<': sb.append("&lt;"); break;
			case '>': sb.append("&gt;"); break;
			case '&': sb.append("&amp;"); break;
			default:  sb.append(c); break;
			}
		}
		return sb.toString();
	}

	/**
	 * replace all 'html'characters in the string with their html-entity
	 * Output is now safe for use in HTML fragments
	 *
	 * @param s
	 * @return
	 */
	public static String escapeHTML(final String s){
		final StringBuilder sb = new StringBuilder();
		if(s==null){
			return "&nbsp;";
		}
		final int n = s.length();
		for (int i = 0; i < n; i++) {
			final char c = s.charAt(i);
			switch (c) {
			case ' ': sb.append("&nbsp;"); break;
			case '<': sb.append("&lt;"); break;
			case '>': sb.append("&gt;"); break;
			case '&': sb.append("&amp;"); break;
			case '"': sb.append("&quot;"); break;
			case '€':
				sb.append("&euro;");
				break;
			case '™':
				sb.append("&trade;");
				break;

				// extra chars based on http://www.w3schools.com/tags/ref_entities.asp

			case '¡':
				sb.append("&iexcl;");
				break; // 	inverted exclamation mark
			case '¢':
				sb.append("&cent;");
				break; // 	cent
			case '£':
				sb.append("&pound;");
				break; // 	pound
			case '¤':
				sb.append("&curren;");
				break; // 	currency
			case '¥':
				sb.append("&yen;");
				break; // 	yen
			case '¦':
				sb.append("&brvbar;");
				break; // 	broken vertical bar
			case '§':
				sb.append("&sect;");
				break; // 	section
			case '¨':
				sb.append("&uml;");
				break; // 	spacing diaeresis
			case '©':
				sb.append("&copy;");
				break; // 	copyright
			case 'ª':
				sb.append("&ordf;");
				break; // 	feminine ordinal indicator
			case '«':
				sb.append("&laquo;");
				break; // 	angle quotation mark (left)
			case '¬':
				sb.append("&not;");
				break; // 	negation
			case '\u00AD': sb.append("&shy;");break; // 	soft hyphen
			case '®':
				sb.append("&reg;");
				break; // 	registered trademark
			case '¯':
				sb.append("&macr;");
				break; // 	spacing macron
			case '°':
				sb.append("&deg;");
				break; // 	degree
			case '±':
				sb.append("&plusmn;");
				break; // 	plus-or-minus
			case '²':
				sb.append("&sup2;");
				break; // 	superscript 2
			case '³':
				sb.append("&sup3;");
				break; // 	superscript 3
			case '´':
				sb.append("&acute;");
				break; // 	spacing acute
			case 'µ':
				sb.append("&micro;");
				break; // 	micro
			case '¶':
				sb.append("&para;");
				break; // 	paragraph
			case '·':
				sb.append("&middot;");
				break; // 	middle dot
			case '¸':
				sb.append("&cedil;");
				break; // 	spacing cedilla
			case '¹':
				sb.append("&sup1;");
				break; // 	superscript 1
			case 'º':
				sb.append("&ordm;");
				break; // 	masculine ordinal indicator
			case '»':
				sb.append("&raquo;");
				break; // 	angle quotation mark (right)
			case '¼':
				sb.append("&frac14;");
				break; // 	fraction 1/4
			case '½':
				sb.append("&frac12;");
				break; // 	fraction 1/2
			case '¾':
				sb.append("&frac34;");
				break; // 	fraction 3/4
			case '¿':
				sb.append("&iquest;");
				break; // 	inverted question mark
			case '×':
				sb.append("&times;");
				break; // 	multiplication
			case '÷':
				sb.append("&divide;");
				break; // 	division

			case 'À':
				sb.append("&Agrave;");
				break; // 	capital a, grave accent
			case 'Á':
				sb.append("&Aacute;");
				break; // 	capital a, acute accent
			case 'Â':
				sb.append("&Acirc;");
				break; // 	capital a, circumflex accent
			case 'Ã':
				sb.append("&Atilde;");
				break; // 	capital a, tilde
			case 'Ä':
				sb.append("&Auml;");
				break; // 	capital a, umlaut mark
			case 'Å':
				sb.append("&Aring;");
				break; // 	capital a, ring
			case 'Æ':
				sb.append("&AElig;");
				break; // 	capital ae
			case 'Ç':
				sb.append("&Ccedil;");
				break; // 	capital c, cedilla
			case 'È':
				sb.append("&Egrave;");
				break; // 	capital e, grave accent
			case 'É':
				sb.append("&Eacute;");
				break; // 	capital e, acute accent
			case 'Ê':
				sb.append("&Ecirc;");
				break; // 	capital e, circumflex accent
			case 'Ë':
				sb.append("&Euml;");
				break; // 	capital e, umlaut mark
			case 'Ì':
				sb.append("&Igrave;");
				break; // 	capital i, grave accent
			case 'Í':
				sb.append("&Iacute;");
				break; // 	capital i, acute accent
			case 'Î':
				sb.append("&Icirc;");
				break; // 	capital i, circumflex accent
			case 'Ï':
				sb.append("&Iuml;");
				break; // 	capital i, umlaut mark
			case 'Ð':
				sb.append("&ETH;");
				break; // 	capital eth, Icelandic
			case 'Ñ':
				sb.append("&Ntilde;");
				break; // 	capital n, tilde
			case 'Ò':
				sb.append("&Ograve;");
				break; // 	capital o, grave accent
			case 'Ó':
				sb.append("&Oacute;");
				break; // 	capital o, acute accent
			case 'Ô':
				sb.append("&Ocirc;");
				break; // 	capital o, circumflex accent
			case 'Õ':
				sb.append("&Otilde;");
				break; // 	capital o, tilde
			case 'Ö':
				sb.append("&Ouml;");
				break; // 	capital o, umlaut mark
			case 'Ø':
				sb.append("&Oslash;");
				break; // 	capital o, slash
			case 'Ù':
				sb.append("&Ugrave;");
				break; // 	capital u, grave accent
			case 'Ú':
				sb.append("&Uacute;");
				break; // 	capital u, acute accent
			case 'Û':
				sb.append("&Ucirc;");
				break; // 	capital u, circumflex accent
			case 'Ü':
				sb.append("&Uuml;");
				break; // 	capital u, umlaut mark
			case 'Ý':
				sb.append("&Yacute;");
				break; // 	capital y, acute accent
			case 'Þ':
				sb.append("&THORN;");
				break; // 	capital THORN, Icelandic
			case 'ß':
				sb.append("&szlig;");
				break; // 	small sharp s, German
			case 'à':
				sb.append("&agrave;");
				break; // 	small a, grave accent
			case 'á':
				sb.append("&aacute;");
				break; // 	small a, acute accent
			case 'â':
				sb.append("&acirc;");
				break; // 	small a, circumflex accent
			case 'ã':
				sb.append("&atilde;");
				break; // 	small a, tilde
			case 'ä':
				sb.append("&auml;");
				break; // 	small a, umlaut mark
			case 'å':
				sb.append("&aring;");
				break; // 	small a, ring
			case 'æ':
				sb.append("&aelig;");
				break; // 	small ae
			case 'ç':
				sb.append("&ccedil;");
				break; // 	small c, cedilla
			case 'è':
				sb.append("&egrave;");
				break; // 	small e, grave accent
			case 'é':
				sb.append("&eacute;");
				break; // 	small e, acute accent
			case 'ê':
				sb.append("&ecirc;");
				break; // 	small e, circumflex accent
			case 'ë':
				sb.append("&euml;");
				break; // 	small e, umlaut mark
			case 'ì':
				sb.append("&igrave;");
				break; // 	small i, grave accent
			case 'í':
				sb.append("&iacute;");
				break; // 	small i, acute accent
			case 'î':
				sb.append("&icirc;");
				break; // 	small i, circumflex accent
			case 'ï':
				sb.append("&iuml;");
				break; // 	small i, umlaut mark
			case 'ð':
				sb.append("&eth;");
				break; // 	small eth, Icelandic
			case 'ñ':
				sb.append("&ntilde;");
				break; // 	small n, tilde
			case 'ò':
				sb.append("&ograve;");
				break; // 	small o, grave accent
			case 'ó':
				sb.append("&oacute;");
				break; // 	small o, acute accent
			case 'ô':
				sb.append("&ocirc;");
				break; // 	small o, circumflex accent
			case 'õ':
				sb.append("&otilde;");
				break; // 	small o, tilde
			case 'ö':
				sb.append("&ouml;");
				break; // 	small o, umlaut mark
			case 'ø':
				sb.append("&oslash;");
				break; // 	small o, slash
			case 'ù':
				sb.append("&ugrave;");
				break; // 	small u, grave accent
			case 'ú':
				sb.append("&uacute;");
				break; // 	small u, acute accent
			case 'û':
				sb.append("&ucirc;");
				break; // 	small u, circumflex accent
			case 'ü':
				sb.append("&uuml;");
				break; // 	small u, umlaut mark
			case 'ý':
				sb.append("&yacute;");
				break; // 	small y, acute accent
			case 'þ':
				sb.append("&thorn;");
				break; // 	small thorn, Icelandic
			case 'ÿ':
				sb.append("&yuml;");
				break; // 	small y, umlaut mark
			default:  sb.append(c); break;
			}
		}
		return sb.toString();
	}


	public static boolean simpleModus(final int m){
		return ((m & DVBtree.SIMPLE_MODUS )!=0);
	}

	public static boolean psiOnlyModus(final int m){
		return ((m&DVBtree.PSI_ONLY_MODUS)!=0);
	}

	public static boolean packetModus(final int m){
		return ((m&DVBtree.PACKET_MODUS)!=0);
	}

	public static boolean countListModus(final int m){
		return ((m&DVBtree.COUNT_LIST_ITEMS_MODUS)!=0);
	}

	public static boolean showPtsModus(final int m){
		return ((m&DVBtree.SHOW_PTS_MODUS)!=0);
	}


	public static boolean showVersionModus(final int m){
		return ((m&DVBtree.SHOW_VERSION_MODUS)!=0);
	}

	public static String stripLeadingZeros(final String s) {
		final int len = s.length()-1; // leave at least one zero if that is the only char
		int st = 0;

		while ((st < len) && (s.charAt(st)=='0')&&(s.charAt(st+1)!='.')) {
			st++;
		}

		return s.substring(st);
	}

	/**
	 * Format a byte[] a an formatted IP number for display (decimal, separated with dots)
	 * @param ip byte[] of any length, so ready for IP6 (or 8 or ...) can not be <code>null</code>
	 * @return
	 */
	public static String formatIPNumber(final byte[] ip){
		final StringBuilder r = new StringBuilder();
		if(ip.length>0){
			r.append(toUnsignedInt(ip[0]));
		}
		for(int i=1;i<ip.length;i++){
			r.append('.').append(toUnsignedInt(ip[i]));
		}
		return r.toString();
	}

	/**
	 * Convert PCR into human readable String (24 hour clock based)
	 * based on DVBsnoop helper.c, which is based on "dvbtextsubs  Dave Chapman"
	 *
	 * @param program_clock_reference
	 * @return
	 */
	public static String printPCRTime(final long program_clock_reference) {

		long  h,m,s;
		long  u;
		final long  p = program_clock_reference/27;
		final long  fa = 1000000;

		final long allSecs = p / fa;

		//  following lines basically taken from "dvbtextsubs  Dave Chapman"
		h=(p/(fa*60*60));
		m=(p/(fa*60))-(h*60);
		s=(p/fa)-(h*3600)-(m*60);
		u=p-(h*fa*60*60)-(m*fa*60)-(s*fa);

		if(PreferencesManager.isEnableSecondsTimestamp()) {
			return allSecs+"."+f6.format(u);
		}
		return h+":"+f2.format(m)+":"+f2.format(s)+"."+f6.format(u);
	}

	/**
	 * based on DVBsnoop helper.c, which is based on "dvbtextsubs  Dave Chapman"
	 *
	 * @param ts
	 * @return
	 *
	 */
	public static String printTimebase90kHz(final long ts) {

		long  h,m,s;
		long  u;
		final long  p = ts/9;

		final long allSecs = ts / 90_000L;
		
		//  following lines basically taken from "dvbtextsubs  Dave Chapman"
		h=(p/(10000L*60*60));
		m=(p/(10000L*60))-(h*60);
		s=(p/10000L)-(h*3600)-(m*60);
		u=p-(h*10000L*60*60)-(m*10000L*60)-(s*10000L);

		if(PreferencesManager.isEnableSecondsTimestamp()) {
			return allSecs+"."+f6.format(u);
		}

		return h+":"+f2.format(m)+":"+f2.format(s)+"."+f4.format(u);
	}

	public static int getHammingReverseByte(final byte b){
		final int t= toUnsignedInt(b);
		int r = (t & 0x40)>>6;
		r |= (t & 0x10)>>3;
		r |= (t & 0x04);
		r |= (t & 0x01)<<3;
		return r;

	}

	public static int getHammingByte(final byte b){
		final int t= toUnsignedInt(b);
		int r = (t & 0x40)>>3;
		r |= (t & 0x10)>>2;
			r |= (t & 0x04)>>1;
			r |= (t & 0x01);
			return r;

	}


	public static int getHamming24_8Byte(final byte[] b,final int offset){

		final int lsb = ((b[offset] & 0x20)<<2)|((b[offset] & 0x0e)<<3)|((b[offset+1] & 0xF0)>>4); // 8 lowest bits
		final int msb = ((b[offset+1] & 0x0E)<<4)|((b[offset+2] & 0xF8)>>3); // 8 medium bits
		final int hsb = ((b[offset+2] & 0x06)<<5); // 2 highest bits, shift to left, after inv they are to the right.

		return (65536*invtab[hsb])+ (256*invtab[msb])+ invtab[lsb];

	}


	/**
	 * Helper byte[] to reverse order of bits in a byte
	 */
	public static final int[] invtab = {
		0x00, 0x80, 0x40, 0xc0, 0x20, 0xa0, 0x60, 0xe0,
		0x10, 0x90, 0x50, 0xd0, 0x30, 0xb0, 0x70, 0xf0,
		0x08, 0x88, 0x48, 0xc8, 0x28, 0xa8, 0x68, 0xe8,
		0x18, 0x98, 0x58, 0xd8, 0x38, 0xb8, 0x78, 0xf8,
		0x04, 0x84, 0x44, 0xc4, 0x24, 0xa4, 0x64, 0xe4,
		0x14, 0x94, 0x54, 0xd4, 0x34, 0xb4, 0x74, 0xf4,
		0x0c, 0x8c, 0x4c, 0xcc, 0x2c, 0xac, 0x6c, 0xec,
		0x1c, 0x9c, 0x5c, 0xdc, 0x3c, 0xbc, 0x7c, 0xfc,
		0x02, 0x82, 0x42, 0xc2, 0x22, 0xa2, 0x62, 0xe2,
		0x12, 0x92, 0x52, 0xd2, 0x32, 0xb2, 0x72, 0xf2,
		0x0a, 0x8a, 0x4a, 0xca, 0x2a, 0xaa, 0x6a, 0xea,
		0x1a, 0x9a, 0x5a, 0xda, 0x3a, 0xba, 0x7a, 0xfa,
		0x06, 0x86, 0x46, 0xc6, 0x26, 0xa6, 0x66, 0xe6,
		0x16, 0x96, 0x56, 0xd6, 0x36, 0xb6, 0x76, 0xf6,
		0x0e, 0x8e, 0x4e, 0xce, 0x2e, 0xae, 0x6e, 0xee,
		0x1e, 0x9e, 0x5e, 0xde, 0x3e, 0xbe, 0x7e, 0xfe,
		0x01, 0x81, 0x41, 0xc1, 0x21, 0xa1, 0x61, 0xe1,
		0x11, 0x91, 0x51, 0xd1, 0x31, 0xb1, 0x71, 0xf1,
		0x09, 0x89, 0x49, 0xc9, 0x29, 0xa9, 0x69, 0xe9,
		0x19, 0x99, 0x59, 0xd9, 0x39, 0xb9, 0x79, 0xf9,
		0x05, 0x85, 0x45, 0xc5, 0x25, 0xa5, 0x65, 0xe5,
		0x15, 0x95, 0x55, 0xd5, 0x35, 0xb5, 0x75, 0xf5,
		0x0d, 0x8d, 0x4d, 0xcd, 0x2d, 0xad, 0x6d, 0xed,
		0x1d, 0x9d, 0x5d, 0xdd, 0x3d, 0xbd, 0x7d, 0xfd,
		0x03, 0x83, 0x43, 0xc3, 0x23, 0xa3, 0x63, 0xe3,
		0x13, 0x93, 0x53, 0xd3, 0x33, 0xb3, 0x73, 0xf3,
		0x0b, 0x8b, 0x4b, 0xcb, 0x2b, 0xab, 0x6b, 0xeb,
		0x1b, 0x9b, 0x5b, 0xdb, 0x3b, 0xbb, 0x7b, 0xfb,
		0x07, 0x87, 0x47, 0xc7, 0x27, 0xa7, 0x67, 0xe7,
		0x17, 0x97, 0x57, 0xd7, 0x37, 0xb7, 0x77, 0xf7,
		0x0f, 0x8f, 0x4f, 0xcf, 0x2f, 0xaf, 0x6f, 0xef,
		0x1f, 0x9f, 0x5f, 0xdf, 0x3f, 0xbf, 0x7f, 0xff,
	};


	/**
	 * Find byte sequence target in source, starting search from fromIndex
	 *
	 * @param source array to be searched
	 * @param target sequence to be found
	 * @param fromIndex index to start search from
	 * @return -1 if not found, else starting position
	 */
	public static int indexOf(final byte[] source,  final byte[]target, final int fromIndex){
		if (fromIndex >= source.length) {
			return  -1;
		}
		final byte first  = target[0];
		final int max = source.length - target.length;

		for (int i = fromIndex; i < max; i++) {
			/* Look for first byte. */
			if (source[i] != first) {
				//noinspection StatementWithEmptyBody
				while ((++i <= max) && (source[i] != first)){ // NOPMD by Eric on 22-5-12 14:46
					// EMPTY body
				}
			}

			/* Found first byte, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				final int end = (j + target.length) - 1;
				for (int k =  1; (j < end) && (source[j] ==
						target[k]); j++, k++){
					// EMPTY BLOCK
				}

				if (j == end) {
					/* Found whole string. */
					return i ;
				}
			}
		}
		return -1;


	}


	public static final DecimalFormat df2pos = new DecimalFormat("00");


	public static DecimalFormat df3pos = new DecimalFormat("000");


	/**
	 * Compare two byte[] with each other
	 * @param data_block
	 * @param offset
	 * @param len
	 * @param data_block2
	 * @param offset2
	 * @param len2
	 * @return
	 */
	public static boolean equals(final byte[] data_block, final int offset, final int len, final byte[] data_block2, final int offset2, final int len2) {

		if ((data_block==null) || (data_block2==null)) {
			return false;
		}

		if (len != len2) {
			return false;
		}

		for (int i=0; i<len; i++){
			if (data_block[i+offset] != data_block2[i+offset2]){
				return false;
			}
		}

		return true;

	}



	public static String getAspectRatioInformationString(final int s) {

		switch (s) {
		case 0: return "forbidden";
		case 1: return "1,0 (Square Sample)";
		case 2:
			return "3÷4";
		case 3:
			return "9÷16";
		case 4:
			return "1÷2,21";

		default:
			return "reserved";
		}
	}


	/**
	 * Generate a HTML hexView (both hex and character) of byteValue, with 16 bytes on each line
	 * @param byteValue
	 * @param offset
	 * @param len
	 * @return
	 */
	public static String getHTMLHexviewColored(final byte [] byteValue, final int offset, final int len, final RangeHashMap<Integer, Color> coloring) {

		final StringBuilder b= new StringBuilder();
		b.append("<pre>");
		// header line
		b.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0001 0203 0405 0607 0809 0A0B 0C0D 0E0F 0123456789ABCDEF<br>");
		final int lines=1+((len-1)/16);
		for (int l = 0; l < lines; l++) {
			final int start=l*16;
			b.append(Utils.toHexString(start,6));
			b.append("&nbsp;");
			b.append("<span>");
			final int lineLen=(l==(lines-1))?(len-(l*16)):16; // if last line calculate bytes left, else 16

			Color currentColor = coloring.find(l*16);
			if(currentColor!=null){
				b.append("<span style=\"color:").append(Utils.toHexString(currentColor)).append("\">");
			}
			// show byte as hex
			for (int i = 0; i < 16; i++) {
				if(i<lineLen){
					b.append(Utils.toHexString(byteValue,  offset+(l*16)+i, 1));
				}else{
					b.append("&nbsp;&nbsp;");
				}
				final Color nextColor=coloring.find((l*16)+i+1);
				// disadvantage, at end of line maybe ampty span.
				if((currentColor!=null)&&!currentColor.equals(nextColor)){ // color change
					b.append("</span>"); //always close current
				}
				if((nextColor!=null)&&!nextColor.equals(currentColor)){
					b.append("<span style=\"color:").append(Utils.toHexString(nextColor)).append("\">");
				}
				currentColor=nextColor;
				// after every second byte insert space
				if((i%2)!=0){
					b.append("&nbsp;");
				}
			}
			if(currentColor!=null){
				b.append("</span>"); //close current at end of line
			}

			// string representation at end of line
			b.append(Utils.escapeHTML(Utils.toSafeString(byteValue, offset+(l*16), lineLen))).append("</span><br>");
		}

		b.append("</pre>");
		return b.toString();
	}



	public static int findMPEG2VideoPid(final PMTsection pmt) {
		int videoPID=0;
		for(final Component component :pmt.getComponentenList()){
			if(component.getStreamtype()==0x02){ //

				videoPID= component.getElementaryPID();
				break;
			}
		}
		return videoPID;
	}



	/**
	 * @param date
	 * @return
	 */
	public static Date roundHourUp(final Date date) {
		final Calendar c2 = new GregorianCalendar();
		c2.setTime(date);
		if((c2.get(Calendar.SECOND)!=0) || (c2.get(Calendar.MINUTE)!=0)){ //  no need to round if xx:00:00
			c2.set(Calendar.SECOND, 0);
			c2.set(Calendar.MINUTE, 0);
			c2.add(Calendar.HOUR, 1);
		}


		return c2.getTime();
	}



	/**
	 * @param date
	 * @return
	 */
	public static Date roundHourDown(final Date date) {
		final Calendar c = new GregorianCalendar();
		c.setTime(date);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);

		return c.getTime();
	}



	/**
	 *replace all 'html'characters in the string with their html-entity
	 * Output is now safe for use in HTML fragments.
	 *
	 * Same as escapeHTML, except for the purpose of displaying in a tooltiptext,
	 * the text is broken down into lines of 80 chars or less, separated by &lt;br&gt;
	 * @param t
	 * @return
	 */
	public static String escapeHtmlBreakLines(final String t) {
		final StringTokenizer st = new StringTokenizer(t);
		int len = 0;
		final StringBuilder res = new StringBuilder();
		while (st.hasMoreTokens()) {
			final String s = st.nextToken();
			if((len+s.length())>80){
				res.append("<br>").append(escapeHTML(s));
				len=s.length();
			}else{
				res.append(' ').append(escapeHTML(s));
				len+=1+s.length();
			}
		}
		return res.toString();
	}

	public static String getHexAndDecimalFormattedString(final int intValue){
		final StringBuilder b = new StringBuilder();
		b.append("0x").append(Integer.toHexString(intValue).toUpperCase()).append(" (").append(intValue)
		.append(")");
		return b.toString();
	}

	public static String getHexAndDecimalFormattedString(final long longValue){
		final StringBuilder b = new StringBuilder();
		b.append("0x").append(Long.toHexString(longValue).toUpperCase()).append(" (").append(longValue)
		.append(")");
		return b.toString();
	}

	public static String getHexAndDecimalFormattedString(final BigInteger bigIntValue){
		final StringBuilder b = new StringBuilder();
		b.append("0x").append(bigIntValue.toString(16).toUpperCase()).append(" (").append(bigIntValue.toString())
		.append(")");
		return b.toString();
	}

	/**
	 * Get single bit as boolean from byte
	 * numberings starts from high order bit, starts at 1.
	 *
	 * @param b singel byte
	 * @param i position of bit in byte, start from 1 up to 8
	 * @return boolen true if bit is set
	 */
	public static boolean getBitAsBoolean(final byte b, final int i) {
		return (( b & (0x80 >> (i-1))) != 0);
	}



	/**
	 * @param b
	 * @return
	 */
	public static int getBooleanAsInt(final boolean b) {
		return b?1:0;
	}



	/**
	 * Generate a HTML hexView (both hex and character) of byteValue, with 16 bytes on each line
	 * @param byteValue
	 * @param offset
	 * @param len
	 * @return
	 */
	public static String getHTMLHexview(final byte [] byteValue, final int offset, final int len) {

		final StringBuilder b= new StringBuilder();
		b.append("<pre>");
		// header line
		b.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;0001 0203 0405 0607 0809 0A0B 0C0D 0E0F 0123456789ABCDEF<br>");
		final int lines=1+((len-1)/16);
		for (int l = 0; l < lines; l++) {
			final int start=l*16;
			b.append(Utils.toHexString(start,6));
			b.append("&nbsp;");
			b.append("<span style=\"background-color: white\">");
			final int lineLen=(l==(lines-1))?(len-(l*16)):16; // if last line calculate bytes left, else 16

			// show byte as hex
			for (int i = 0; i < 16; i++) {
				if(i<lineLen){
					b.append(Utils.toHexString(byteValue,  offset+(l*16)+i, 1));
				}else{
					b.append("&nbsp;&nbsp;");
				}
				// after every second byte insert space
				if((i%2)!=0){
					b.append("&nbsp;");
				}
			}

			// string representation at end of line
			b.append(Utils.escapeHTML(Utils.toSafeString(byteValue, offset+(l*16), lineLen))).append("</span><br>");
		}

		b.append("</pre>");
		return b.toString();
	}


	public static String toHexString ( final Color c ){

		String s = Integer.toHexString( c.getRGB() & 0xffffff );

		if ( s.length() < 6 ){
			s = "000000".substring( 0, 6 - s.length() ) + s;
		}
		return '#' + s;

	}

	public static StringBuilder getChildrenAsHTML(final DefaultMutableTreeNode dmtn) {
		final String lineSep = "<br>";
		final StringBuilder res = new StringBuilder();
		@SuppressWarnings("rawtypes")
		final Enumeration children = dmtn.children();
		while(children.hasMoreElements()){
			final Object next = children.nextElement();
			if(next instanceof DefaultMutableTreeNode){
				final DefaultMutableTreeNode child = (DefaultMutableTreeNode)next;
				final KVP chKVP = (KVP)child.getUserObject();
				res.append(chKVP.toString(KVP.STRING_DISPLAY_HTML_FRAGMENTS, KVP.NUMBER_DISPLAY_BOTH)).append(lineSep);
				if(!child.isLeaf()){
					res.append(getChildrenAsHTML(child));
				}
			}
		}
		return res;
	}
	
	
	public static String formatDuration(String duration){
		if((duration==null)||(duration.length()!=6)){
			return duration;
		}
		StringBuilder res= new StringBuilder(duration.substring(0, 2)).append('h');
		res.append(duration, 2, 4).append('m').append(duration, 4, 6);
		return res.toString();
	}

	/**
	 * extract plain text from HTML,  replacing &lt;br&gt; and &lt;/p&gt; with newlines.
	 * @param htmlString
	 * @return
	 */
	public static String extractTextFromHTML(String htmlString) {
		Reader reader = new StringReader(htmlString);
	    final ArrayList<String> list = new ArrayList<>();
	
	    HTMLEditorKit.ParserCallback parserCallback = new HTMLEditorKit.ParserCallback() {
	        public void handleText(final char[] data, final int pos) {
	            list.add(new String(data));
	        }
	
	        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attribute, int pos) {
	        	// ignore
	        }
	
	        public void handleEndTag(HTML.Tag t, final int pos) {
	        	if (t.equals(HTML.Tag.P)) {
	                list.add("\n");
	            }
	        }
	
	        public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, final int pos) {
	            if (t.equals(HTML.Tag.BR)) {
	                list.add("\n");
	            }
	        }
	
	        public void handleComment(final char[] data, final int pos) {
	        	// ignore
	        }
	
	        public void handleError(final String errMsg, final int pos) {
	        	// ignore
	        }
	    };
	    try {
	        new ParserDelegator().parse(reader, parserCallback, true);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    StringBuilder result = new StringBuilder();
	    for (String s : list) {
	    	result.append(s);
	    }
	    return result.toString();
	}
	
	
	/**
	 * @param list
	 * @param pay
	 * @return
	 */
	public static boolean listContainsByteArray(List<byte[]> list, byte[] pay) {
		for (byte[] inList : list) {
			if (Arrays.equals(inList, pay)) {
				return true;
			}
		}
		return false;
	}

	public static byte[] bytesListToArray(List<Byte> payLoad) {
		byte[] res = new byte[payLoad.size()];
		for(int i=0; i<payLoad.size(); i++) {
			res[i] = payLoad.get(i);
		}
		return res;
	}
	
	
}

