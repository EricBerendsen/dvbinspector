/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.*;
import java.awt.image.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.imageio.ImageIO;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.util.*;

public class SubPage implements TreeNode, ImageSource, TextConstants{

	/**
	 *
	 */
	private final Page pageHandler;
	private final int subPageNo;
	protected PageLine[] linesList = new PageLine[26]; // visible lines within cntext of a page

	protected TxtDataField[] packetx_26 = new TxtDataField[16]; // 9.4.1 Packet X/26, Designation code values 0000 to 1111 allow up to 16 packets with Y = 26 to be associated with a given page.

	protected TxtDataField[] packetx_27 = new TxtDataField[16]; // 9.4.1 Packet X/27, Designation code values 0000 to 1111 allow up to 16 packets with Y = 27 to be associated with a given page.
	protected TxtDataField[] packetx_28 = new TxtDataField[16]; // 9.4.1 Packet X/28, Designation code values 0000 to 1111 allow up to 16 packets with Y = 28 to be associated with a given page.

	private static final int charWidth = 15;
	private static final int charHeight = 19;

	private static final int textColumns = 40;
	private static final int textRows = 25;

	private final int width = textColumns * charWidth;
	private final int height = textRows * charHeight;

	public static final int NO_OBJECT_TYPE = 0;
	public static final int ACTIVE_OBJECT_TYPE = 1;
	public static final int ADAPTIVE_OBJECT_TYPE = 2;
	public static final int PASSIVE_OBJECT_TYPE = 3;
	
	private static final ClassLoader classLoader = SubPage.class.getClassLoader();
	/**
	 * contains java Unicode char for each visible character. Depending on effect[][] it might be a block graphics (that is not a unicode char)
	 * which is drawn differently
	 */
	private char[][]					txt			= new char[textRows][textColumns];
	/**
	 * contains index into CLUT for color to be used for this char backGround
	 */
	private int[][]						bgColor		= new int[textRows][textColumns];
	/**
	 * contains index into CLUT for color to be used for this char foreGround
	 */
	private int[][]						fgColor		= new int[textRows][textColumns];

	/**
	 *  bits used to indicate special effects (like blockgraphics,hidden,flashing, double heigth,double size) to be applied to this char. Contstructed by combining the values defined in TextConstants.java
	 */
	private int[][]						effect		= new int[textRows][textColumns];

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 *
	 */

	private static final Logger logger = Logger.getLogger(SubPage.class.getName());

	private static BufferedImage g3CharsImage;

	/**
	 *
	 */

	static {
		try {
			final InputStream fileInputStream = classLoader.getResourceAsStream("g3_charset.gif");
			g3CharsImage = ImageIO.read(fileInputStream);

		} catch (final Exception e) {
			logger.log(Level.WARNING, "error reading image g3_charset.gif:", e);
		}

	}

	public int getNationalOptionCharSubset() {
		int r = 0; // sane default
		
		/* check for page enhancement data first. if X/28/0 is available, use
		 * that, otherwise check for M/29/0 which contains the same data, but
		 * magazine-wide. */
		TxtDataField pageEnhancement = (packetx_28[0] != null)
				? packetx_28[0]
				: getMagazine().getPageEnhanceMentDataPackes(0);
		if(pageEnhancement != null)
		{
			/* the national option selection data is contained in bits 14-8 of
			 * triplet 1. */

			final int tripletVal = pageEnhancement.getTripletList().get(0).getVal();

			/* EB very dirty hack, the order of the last 3 bits is reversed from the order used in getNationalOptionCharacterSubset()
			 * Table 32 in ETSI EN 300 706 V1.2.1 (2003-04) suggest the order from triplet 1 is correct. 
			 * However we use look up tables from ProjectX, and these also have the 'wrong' order.  
			 *  
			 */

			
			// G0 character set part
			final int g0CharacterSet = (tripletVal & 0x3c00) >>> 7;
			
			// National Option subset part, reverse bits with a lookup table
			final int  nationalOptionSubset =Utils.invNationalOptionSet[(tripletVal & 0x0380) >>> 7];
			
			r = g0CharacterSet | nationalOptionSubset;
		}
		else if(linesList[0] != null) {
			/* 
			 * ETSI EN 300 706 V1.2.1 (2003-04) 15.2 
			 * Designation of default G0 and G2 sets and national option sub-sets
			 * "in the absence of a packet X/28/0 Format 1, X/28/4, M/29/0 or M/29/4, the
			 * default sets are established by a local Code of Practice"
			 * 
			 *  get user specified default G0 and G2 sets, to be used in the absence of a packet X/28/0 Format 1, X/28/4, M/29/0 or M/29/4
			 *   
			 */
			
			int defaultG0CharacterSet = pageHandler.getMagazine().getTxtService().getTransportStream().getDefaultG0CharacterSet();
			/* if we have no page enhancement data, revert to the data available
			 * in the page's header. this only gives us the last 3 bits of the
			 * selection bits. Add the user specified default G0 and G2 set to this 
			 */
			r = (defaultG0CharacterSet << 3)|linesList[0].getNationalOptionCharacterSubset();
		}
		return r;
	}

	/**
	 *
	 */
	public SubPage(final Page page, final int s) {
		pageHandler = page;
		subPageNo = s;
	}

	/**
	 * @param txtDataField
	 */
	public void setHeader(final TxtDataField txtDataField) {
		if (txtDataField.getSubPage() != subPageNo) {
			logger.log(Level.INFO, "txtDataField.getSubPage()!=subPageNo," + txtDataField.getSubPage() + "!=" + subPageNo);
		}
		if (txtDataField.isErasePage()) {
			linesList = new PageLine[26];
			packetx_26	= new TxtDataField[16];			// 9.4.1 Packet X/26, Designation code values 0000 to 1111 allow up to 16 packets with Y = 26 to be associated with a given page.
			packetx_27	= new TxtDataField[16];			// 9.4.1 Packet X/27, Designation code values 0000 to 1111 allow up to 16 packets with Y = 27 to be associated with a given page.
			packetx_28	= new TxtDataField[16];			// 9.4.1 Packet X/28, Designation code values 0000 to 1111 allow up to 16 packets with Y = 28 to be associated with a given page.
		}
		linesList[0] = new PageLine(this, txtDataField);

	}

	public void addLine(final TxtDataField txtDataField) {
		final int l = txtDataField.getPacketNo();
		if (txtDataField.getPacketNo() == 26) {
			packetx_26[txtDataField.getDesignationCode()] = txtDataField;
		} else if (txtDataField.getPacketNo() == 27) {
			packetx_27[txtDataField.getDesignationCode()] = txtDataField;
		} else if (txtDataField.getPacketNo() == 28) {
			packetx_28[txtDataField.getDesignationCode()] = txtDataField;
		} else {
			linesList[l] = new PageLine(this, txtDataField);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("SubPage " + toHexString(subPageNo, 4), this));

		for (int i = 0; i < 26; i++) { // 24 or 25 lines, what is line 25 used for anyaway ??? normal is 0 (header) + 1 to 24
			final PageLine pageLine = linesList[i];
			if (pageLine != null) {
				s.add(pageLine.getHTMLJTreeNode(modus));
			} else {
				s.add(new DefaultMutableTreeNode(new KVP(TxtDataField.BLACK_HTML_LINE, "")));
			}
		}
		for (final TxtDataField txtDatafield : packetx_26) {
			if (txtDatafield != null) {
				s.add(txtDatafield.getJTreeNode(modus));
			}
		}
		for (final TxtDataField txtDatafield : packetx_27) {
			if (txtDatafield != null) {
				s.add(txtDatafield.getJTreeNode(modus));
			}
		}

		for (final TxtDataField txtDatafield : packetx_28) {
			if (txtDatafield != null) {
				s.add(txtDatafield.getJTreeNode(modus));
			}
		}
		if ((linesList[0] != null) && (linesList[0].getPageNumber() == 0xFD)) { // MIP Page
			addMIPPageDetailsToJTree(modus, s);
		}
		if (isMOTpage()) {
			addMOTPageDetailsToJTree(modus, s);
		}
		if (isDRCSDownloadingpage()) {
			addDRCSDownloadPageDetailsToJTree(modus, s);
		}
		if (isObjectDefinitionpage()) {
			addObjectDefinitionPageDetailsToJTree(modus, s);
		}
		if(isBTTpage()){
			addBTTPageDetailsToJTree(modus, s);
		}
		if(isAITpage()){
			addAITPageDetailsToJTree(modus, s);
		}
		if(isMPTpage()){
			addMPTPageDetailsToJTree(modus, s);
		}
		return s;
	}

	/**
	 * @param modus
	 * @param s
	 */
	private void addMPTPageDetailsToJTree(final int modus, final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode mptPage = new DefaultMutableTreeNode(new KVP("Multipage Table (MPT) Page"));
		s.add(mptPage);

		for (int i = 1; i <= 23; i++) {
			final TxtDataField txtDataField = linesList[i];
			if(txtDataField!=null){
				final StringBuilder b = new StringBuilder();
				for(int j= 0; j < 39; j++){
					final int value = getHammingReverseByte(txtDataField.getRawByte(j));
					b.append(Integer.toHexString(value));
				}
				final DefaultMutableTreeNode p = new DefaultMutableTreeNode(new KVP("Row "+i,b.toString(),null));
				mptPage.add(p);
			}
		}

		for (int i = 1; i <= 20; i++) {
			final TxtDataField txtDataField = linesList[i];
			if(txtDataField!=null){
				for(int j= 0; j < 39; j++){
					final int value = getHammingReverseByte(txtDataField.getRawByte(j));
					final int pageNo= 100 + ((i-1)*40)+j;
					final DefaultMutableTreeNode p = new DefaultMutableTreeNode(new KVP("page "+pageNo,value,null));
					mptPage.add(p);

				}

			}
		}


	}

	/**
	 * @param modus
	 * @param s
	 */
	private void addAITPageDetailsToJTree(final int modus, final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode aitPage = new DefaultMutableTreeNode(new KVP("Additional Information Tables (AIT) Page"));
		s.add(aitPage);

		for (int i = 1; i <= 22; i++) {
			final TxtDataField txtDataField = linesList[i];
			if(txtDataField!=null){
				for(int j= 0; j < 2; j++){
					final StringBuilder b = new StringBuilder();
					for(int k= 0; k < 8; k++){
						final int v = getHammingReverseByte(txtDataField.getRawByte((j*20)+k));
						b.append(Integer.toHexString(v));
					}
					final StringBuilder buf = new StringBuilder();
					for(int k= 8; k < 20; k++){
						final byte ch = txtDataField.getPageDataByte((j*20)+k);
						if((ch>=32)&&(ch<127)){
							buf.append((char)ch);
						}else{  // empty space
							buf.append(' ');
						}

					}
					final DefaultMutableTreeNode p = new DefaultMutableTreeNode(new KVP("Title "+(((i-1)*2)+j),b.toString()+" "+buf.toString(),null));
					aitPage.add(p);
				}
			}
		}
	}

	/**
	 * @param modus
	 * @param s
	 */
	private void addBTTPageDetailsToJTree(final int modus, final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode bttPage = new DefaultMutableTreeNode(new KVP("Basic Top Table (BTT) Page"));
		s.add(bttPage);
		for (int i = 1; i <= 20; i++) {
			final TxtDataField txtDataField = linesList[i];
			if(txtDataField!=null){
				for(int j= 0; j < 39; j++){
					final int value = getHammingReverseByte(txtDataField.getRawByte(j));
					final int pageNo= 100 + ((i-1)*40)+j;
					final DefaultMutableTreeNode p = new DefaultMutableTreeNode(new KVP("page "+pageNo,value,getPageDescription(value)));
					bttPage.add(p);

				}

			}
		}
		for (int i = 21; i <= 23; i++) {
			final TxtDataField txtDataField = linesList[i];
			if(txtDataField!=null){
				final StringBuilder b = new StringBuilder();
				for(int j= 0; j < 39; j++){
					final int value = getHammingReverseByte(txtDataField.getRawByte(j));
					b.append(Integer.toHexString(value));
				}
				final DefaultMutableTreeNode p = new DefaultMutableTreeNode(new KVP("Row "+i,b.toString(),null));
				bttPage.add(p);
			}
		}
	}

	/**
	 * @param value
	 * @return
	 */
	private static String getPageDescription(final int value) {
		switch (value) {
		case 0:
			return "Page not in transmission cycle";
		case 1:
			return "Text Page, extra information";
		case 2:
			return "Header Page, main group page of TV programs, extra information";
		case 3:
			return "Header Page, main group page of TV programs, extra information, multiple page";
		case 4:
			return "Main Group Page, extra information";
		case 5:
			return "Main Group Page, extra information, multiple page";
		case 6:
			return "Group Page, extra information";
		case 7:
			return "Group Page, extra information, multiple page";
		case 8:
			return "Normal Page";
		case 9:
			return "Normal Page, extra information";
		case 10:
			return "Normal Page, multiple page";
		case 11:
			return "Normal Page, extra information, multiple page";

		default:
			return "Reserved";
		}
	}

	/**
	 * @param modus
	 * @param s
	 */
	private void addObjectDefinitionPageDetailsToJTree(final int modus, final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode objPage = new DefaultMutableTreeNode(new KVP("Object Definition Page"));
		s.add(objPage);
		for (int i = 1; i <= 4; i++) {
			// possible pointer rows
			final TxtDataField txtDataField = linesList[i];
			if (txtDataField != null) {
				final int functionByte = getHammingReverseByte(txtDataField.getRawByte(0)); // start counting from payload,this is actual byte 6
				if ((functionByte & MASK_1BIT) != 0) {
					final DefaultMutableTreeNode pointerLineTreeNode = new DefaultMutableTreeNode(new KVP(
							"Pointer Table line " + i));
					pointerLineTreeNode
					.add(new DefaultMutableTreeNode(new KVP("functionByte", functionByte, null)));
					objPage.add(pointerLineTreeNode);
					final List<Triplet> tripletList = txtDataField.getTripletList();
					for (int j = 0; j <= 3; j++) { // skip triplet 0,reserved
						final Triplet tr1 = tripletList.get(1 + (j * 3));
						final Triplet tr2 = tripletList.get(1 + (j * 3) + 1);
						final Triplet tr3 = tripletList.get(1 + (j * 3) + 2);
						final int activeObjectEven = (tr1.getVal() & MASK_9BITS);
						final int activeObjectOdd = (tr1.getVal() & 0x3FE00) >> 9;
					final int adaptiveObjectEven = (tr2.getVal() & MASK_9BITS);
					final int adaptiveObjectOdd = (tr2.getVal() & 0x3FE00) >> 9;
				final int pasiveObjectEven = (tr3.getVal() & MASK_9BITS);
				final int pasiveObjectOdd = (tr3.getVal() & 0x3FE00) >> 9;
				addObjectPointer(((i - 1) * 8) + (j * 2), pointerLineTreeNode, activeObjectEven,
						"Active Object",modus);
				addObjectPointer(((i - 1) * 8) + (j * 2), pointerLineTreeNode, adaptiveObjectEven,
						"Adaptive Object",modus);
				addObjectPointer(((i - 1) * 8) + (j * 2), pointerLineTreeNode, pasiveObjectEven,
						"Passive Object",modus);
				addObjectPointer(((i - 1) * 8) + (j * 2) + 1, pointerLineTreeNode, activeObjectOdd,
						"Active Object",modus);
				addObjectPointer(((i - 1) * 8) + (j * 2) + 1, pointerLineTreeNode, adaptiveObjectOdd,
						"Adaptive Object",modus);
				addObjectPointer(((i - 1) * 8) + (j * 2) + 1, pointerLineTreeNode, pasiveObjectOdd,
						"Passive Object",modus);
					}
				}
			}
		}

		// line 3 up
		for (int i = 3; i <= 41; i++) {
			// possible pointer rows
			final TxtDataField txtDataField = getLine(i);
			if (txtDataField != null) {
				final int functionByte = getHammingReverseByte(txtDataField.getRawByte(0)); // start counting from payload,this is actual byte 6
				if ((functionByte & MASK_1BIT) == 0) {
					final List<TxtTriplet> tripletList = txtDataField.getTxtTripletList();
					addListJTree(objPage, tripletList, modus, "triplet_list line " + i);
				}
			}
		}
	}

	/**
	 * @param i
	 * @param t
	 * @param j
	 * @param activeObjectEven
	 */
	private void addObjectPointer(final int i, final DefaultMutableTreeNode t, final int objPointer, final String label,final int modus) {
		if (objPointer != 0x1FF) {
			final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP(label + " " + i, objPointer, null));
			t.add(s);

			final int lineNo = 3 + (objPointer/13);
			final int tripletNo = objPointer%13;
			final List<TxtTriplet> objectDefinition = getObjectDefinition(this, lineNo, tripletNo);
			Utils.addListJTree(s, objectDefinition, modus, "Object Definition");

		}
	}

	/**
	 * @param modus
	 * @param s
	 */
	private void addDRCSDownloadPageDetailsToJTree(final int modus, final DefaultMutableTreeNode s) {
		TxtDataField drcsDatafield = null;
		for (final TxtDataField txtDatafield : packetx_28) {
			if ((txtDatafield != null) && (txtDatafield.getDesignationCode() == 3)) {
				drcsDatafield = txtDatafield;
			}
		}
		if (drcsDatafield != null) {
			final List<Triplet> tripletList = drcsDatafield.getTripletList();

			final BitString bs = new BitString();
			// bits 11-18 of triplet 2
			for (int i = 2; i <= 11; i++) {
				bs.addIntBitsReverse(tripletList.get(i - 1).getVal(), 18);
			}
			// bits 1-12 of triplet 12
			bs.addIntBitsReverse(tripletList.get(11).getVal() & 0xFFF, 12);

			final DefaultMutableTreeNode drcs = new DefaultMutableTreeNode(new KVP("DRCS Downloading Mode Invocation"));

			for (int i = 0; i < 48; i++) {
				final int drcsMode = bs.getIntBitsReverse(4); // 0..15

				final DRCSCharacter drcsChar = new DRCSCharacter(drcsMode, this, i);
				drcs.add(drcsChar.getJTreeNode(modus));
			}
			s.add(drcs);
		}
	}

	private boolean isDRCSDownloadingpage() {
		for (final TxtDataField txtDatafield : packetx_28) {
			if ((txtDatafield != null) && (txtDatafield.getDesignationCode() == 3)) {
				final List<Triplet> tripletList = txtDatafield.getTripletList();
				final Triplet tr1 = tripletList.get(0);
				if ((tr1.getPageFunction() == 0x04) || (tr1.getPageFunction() == 0x05)) {
					return true;
				}
			}
		}
		// look in MOT
		final SubPage motPage = getMOTPage();
		if(motPage!=null){
			final List<DRCSLink> drcsLinks = motPage.getDRCSLinksLevel25();
			for(final DRCSLink link :drcsLinks){
				if((link.getMagazine()==getMagazineNo())&&(link.getPageNo()==getPageNo())&&(link.getPageNo()!=0xFF)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	private SubPage getMOTPage() {
		return pageHandler.getMOTPage();
	}


	private SubPage getBTTPage() {
		if(getMagazine(1)!=null){
			final Page p= getMagazine(1).getPage(0xF0);
			if((p!=null)&&!p.getSubPages().isEmpty()){
				return p.getSubPages().values().iterator().next();
			}
		}
		return null;

	}

	private boolean isObjectDefinitionpage() {
		for (final TxtDataField txtDatafield : packetx_28) {
			if ((txtDatafield != null) && (txtDatafield.getDesignationCode() == 0)) {
				final List<Triplet> tripletList = txtDatafield.getTripletList();
				final Triplet tr1 = tripletList.get(0);
				if ((tr1.getPageFunction() == 0x02) || (tr1.getPageFunction() == 0x03)) {
					return true;
				}
			}
		}
		// look in MOT,just in case line 28 does not indicate POP
		final SubPage motPage = getMOTPage();
		if(motPage!=null){
			final List<ObjectLink> objectLinks = motPage.getObjectLinksLevel25();
			for(final ObjectLink link :objectLinks){
				if((link.getMagazine()==getMagazineNo())&&(link.getPageNo()==getPageNo())&&(link.getPageNo()!=0xFF)){
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * @return
	 */
	private static List<ObjectLink> getObjectLinks(final TxtDataField txtDataField1, final TxtDataField txtDataField2) {
		final List<ObjectLink> objects=new ArrayList<ObjectLink>();
		if(txtDataField1!=null){
			final ObjectLink gpop = new ObjectLink(txtDataField1.data_block, 6 + txtDataField1.offset);
			objects.add(gpop);
			for (int i = 1; i < 4; i++) {
				final ObjectLink pop = new ObjectLink(txtDataField1.data_block, 6 + (10 * i) + txtDataField1.offset);
				objects.add(pop);
			}
		}
		if(txtDataField2!=null){
			for (int i = 4; i < 8; i++) {
				final ObjectLink pop = new ObjectLink(txtDataField2.data_block, 6 + (10 * (i-4)) + txtDataField2.offset);
				objects.add(pop);
			}
		}

		return objects;
	}


	/**
	 * @return
	 */
	private List<ObjectLink> getObjectLinksLevel25() {
		return getObjectLinks(linesList[19],linesList[20]);
	}

	/**
	 * @param modus
	 * @param s
	 */
	private void addMOTPageDetailsToJTree(final int modus, final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode mot = new DefaultMutableTreeNode(new KVP("MOT Structure"));
		s.add(mot);

		for (int i = 1; i <= 8; i++) { // lines with links for normal pages without last hexnumber
			if (linesList[i] != null) {
				for (int j = 0; j < 2; j++) { // two sections per line
					for (int k = 0; k <= 9; k++) {
						final int objectPageAssociation = getHammingReverseByte(linesList[i].getRawByte((j * 20) + (k * 2)));
						final int drcsPageAssociation = getHammingReverseByte(linesList[i].getRawByte((j * 20) + 1 + (k * 2)));
						final int associationPageNo = (16 * (((i - 1) * 2) + j)) + k;
						addPageAssociationsToTree(mot, objectPageAssociation, drcsPageAssociation, associationPageNo);
					}
				}
			}
		}
		// lines 9 to 14, hexPages
		for (int i = 9; i <= 14; i++) { // lines with links for normal pages without last hexnumber
			if (linesList[i] != null) {
				for (int j = 0; j < 3; j++) { // 3 sections per line
					for (int k = 0; k <= 5; k++) {
						if ((i < 14) || (j == 0)) { // line 8-13 completely,line 14 only first 6 elements (0xFA - 0xFF)
							final int objectPageAssociation = getHammingReverseByte(linesList[i].getRawByte((j * 12)
									+ (k * 2)));
							final int drcsPageAssociation = getHammingReverseByte(linesList[i].getRawByte((j * 12) + 1
									+ (k * 2)));
							final int associationPageNo = (16 * (((i - 9) * 3) + j)) + k + 10;
							addPageAssociationsToTree(mot, objectPageAssociation, drcsPageAssociation,
									associationPageNo);
						}
					}
				}
			}
		}

		String level = "Level 2.5";
		addObjectLinkLine1ToJTree(modus, mot, linesList[19], level);
		addObjectLinkLine2ToJTree(modus, mot, linesList[20], level);
		final List<ObjectLink> obs=getObjectLinksLevel25();
		Utils.addListJTree(mot, obs, modus, "Objects list");

		addDRCSLinkLineToJTree(modus, mot, linesList[21], level);

		level = "Level 3.5";
		addObjectLinkLine1ToJTree(modus, mot, linesList[22], level);
		addObjectLinkLine2ToJTree(modus, mot, linesList[23], level);
		addDRCSLinkLineToJTree(modus, mot, linesList[24], level);
	}

	/**
	 * @param mot
	 * @param objectPageAssociation
	 * @param drcsPageAssociation
	 * @param associationPageNo
	 */
	private static void addPageAssociationsToTree(final DefaultMutableTreeNode mot, final int objectPageAssociation,
			final int drcsPageAssociation, final int associationPageNo) {
		if (objectPageAssociation != 0) {
			String exp = ((objectPageAssociation & 0x08) != 0) ? "global objects required"
					: "no global objects required";
			exp += ", "
					+ (((objectPageAssociation & 0x07) == 0) ? "no public objects required"
							: ("POP link " + (objectPageAssociation & 0x07)));
			mot.add(new DefaultMutableTreeNode(new KVP("Page " + toHexString(associationPageNo, 2)
					+ " Object Page Association", objectPageAssociation, exp)));
		}
		if (drcsPageAssociation != 0) {
			String exp = ((drcsPageAssociation & 0x08) != 0) ? "global DRCS required" : "no global DRCS required";
			exp += ", "
					+ (((drcsPageAssociation & 0x07) == 0) ? "no public DRCS required"
							: ("DRCS link " + (drcsPageAssociation & 0x07)));
			mot.add(new DefaultMutableTreeNode(new KVP("Page " + toHexString(associationPageNo, 2)
					+ " DRCS Page Association", drcsPageAssociation, exp)));
		}
	}

	/**
	 * @param modus
	 * @param s
	 */

	private void addMIPPageDetailsToJTree(final int modus, final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode mot = new DefaultMutableTreeNode(new KVP("MIP Structure"));
		s.add(mot);

		for (int i = 1; i <= 8; i++) { // lines with links for normal pages without last hexnumber
			if (linesList[i] != null) {
				for (int j = 0; j <= 1; j++) { // two sections per line
					for (int k = 0; k <= 9; k++) {
						final int lowerNibble = getHammingReverseByte(linesList[i].getRawByte((j * 20) + (k * 2)));
						final int upperNibble = getHammingReverseByte(linesList[i].getRawByte((j * 20) + 1 + (k * 2)));
						final int mipPageNo = (16 * (((i - 1) * 2) + j)) + k;
						addMIPPageDetailsToTree(mot, lowerNibble, upperNibble, mipPageNo);
					}
				}
			}
		}
		// lines 9 to 14, hexPages
		for (int i = 9; i <= 14; i++) { // lines with links for normal pages without last hexnumber
			if (linesList[i] != null) {
				for (int j = 0; j <= 3; j++) { // 3 sections per line
					for (int k = 0; k <= 5; k++) {
						if ((i < 14) || (j == 0)) { // line 8-13 completely,line 14 only first 6 elements (0xFA - 0xFF)
							final int lowerNibble = getHammingReverseByte(linesList[i].getRawByte((j * 12) + (k * 2)));
							final int upperNibble = getHammingReverseByte(linesList[i].getRawByte((j * 12) + 1 + (k * 2)));
							final int mipPageNo = (16 * (((i - 9) * 3) + j)) + k + 10;
							addMIPPageDetailsToTree(mot, lowerNibble, upperNibble, mipPageNo);
						}
					}
				}
			}
		}

		// TODO lines 15-24 subpages
	}

	/**
	 * @param mot
	 * @param lowerNibble
	 * @param upperNibble
	 * @param mipPageNo
	 */
	private static void addMIPPageDetailsToTree(final DefaultMutableTreeNode mot, final int lowerNibble, final int upperNibble, final int mipPageNo) {
		final int pagecode = (16 * upperNibble) + lowerNibble;
		if (pagecode != 0) {
			mot.add(new DefaultMutableTreeNode(new KVP("Page " + toHexString(mipPageNo, 2), pagecode, Utils
					.getMIPPageFunctionString(pagecode))));
		}
	}

	/**
	 * @param modus
	 * @param mot
	 * @param txtDataField
	 * @param level
	 */
	private static void addObjectLinkLine1ToJTree(final int modus, final DefaultMutableTreeNode mot, final TxtDataField txtDataField, final String level) {
		if (txtDataField != null) {
			final ObjectLink gpop = new ObjectLink(txtDataField.data_block, 6 + txtDataField.offset);
			mot.add(gpop.getJTreeNode(modus, level + " GPOP "));
			for (int i = 1; i < 4; i++) {
				final ObjectLink pop = new ObjectLink(txtDataField.data_block, 6 + (10 * i) + txtDataField.offset);
				mot.add(pop.getJTreeNode(modus, level + " POP " + i + " "));
			}
		}
	}

	/**
	 * @param modus
	 * @param mot
	 * @param txtDataField
	 * @param level
	 */
	private static void addObjectLinkLine2ToJTree(final int modus, final DefaultMutableTreeNode mot, final TxtDataField txtDataField, final String level) {
		if (txtDataField != null) {
			for (int i = 4; i < 8; i++) {
				final ObjectLink pop = new ObjectLink(txtDataField.data_block, 6 + (10 * (i - 4)) + txtDataField.offset);
				mot.add(pop.getJTreeNode(modus, level + " POP " + i + " "));
			}
		}
	}

	/**
	 * @param modus
	 * @param mot
	 * @param txtDataField
	 * @param level
	 */
	private static void addDRCSLinkLineToJTree(final int modus, final DefaultMutableTreeNode mot, final TxtDataField txtDataField, final String level) {
		if (txtDataField != null) {
			final DRCSLink gpop = new DRCSLink(txtDataField.data_block, 6 + txtDataField.offset);
			mot.add(gpop.getJTreeNode(modus, level + " GDRCS "));
			for (int i = 1; i < 8; i++) {
				final DRCSLink pop = new DRCSLink(txtDataField.data_block, 6 + (4 * i) + txtDataField.offset);
				mot.add(pop.getJTreeNode(modus, level + " DRCS " + i + " "));
			}

			// ETSI EN 300 706 § 10.6.6 Number of Enhancement Pages defines next byte numbers with respect to entire line.
			// getRawByte uses only bytes from the data part, so the offset is off by 6.
			final int mag1 = getHammingReverseByte(txtDataField.getRawByte(34)); // byte 40 in ETSI EN 300 706 § 10.6.6
			final int mag2 = getHammingReverseByte(txtDataField.getRawByte(35));

			final DefaultMutableTreeNode noPagesNode = new DefaultMutableTreeNode(new KVP("Number of Enhancement Pages"));

			noPagesNode.add(new DefaultMutableTreeNode(new KVP("Magazine 3,2,1,8", mag1, null)));
			noPagesNode.add(new DefaultMutableTreeNode(new KVP("Magazine 7,6,5,4", mag2, null)));
			final int objectPages = getHammingReverseByte(txtDataField.getRawByte(36)) + (16
					* getHammingReverseByte(txtDataField.getRawByte(37)));
			final int drcsPages = getHammingReverseByte(txtDataField.getRawByte(38)) + (16
					* getHammingReverseByte(txtDataField.getRawByte(39)));
			noPagesNode.add(new DefaultMutableTreeNode(new KVP("Total number of object pages", objectPages, null)));
			noPagesNode.add(new DefaultMutableTreeNode(new KVP("Total number of DRCS pages", drcsPages, null)));

			mot.add(noPagesNode);
		}
	}



	private static List<DRCSLink> getDRCSLinks(final TxtDataField txtDataField) {
		final ArrayList<DRCSLink> res = new ArrayList<DRCSLink>();
		if (txtDataField != null) {
			final DRCSLink gpop = new DRCSLink(txtDataField.data_block, 6 + txtDataField.offset);
			res.add(gpop);
			for (int i = 1; i < 8; i++) {
				final DRCSLink pop = new DRCSLink(txtDataField.data_block, 6 + (4 * i) + txtDataField.offset);
				res.add(pop);
			}

		}
		return res;
	}

	private List<DRCSLink> getDRCSLinksLevel25() {
		return getDRCSLinks(linesList[21]);

	}

	/**
	 * @return the linesList
	 */
	public TxtDataField[] getLinesList() {
		return linesList;
	}

	/**
	 * @return the packetx_26
	 */
	public TxtDataField[] getPacketx_26() {
		return packetx_26;
	}

	/**
	 * @return the packetx_27
	 */
	public TxtDataField[] getPacketx_27() {
		return packetx_27;
	}

	/**
	 * @return the packetx_28
	 */
	public TxtDataField[] getPacketx_28() {
		return packetx_28;
	}

	/**
	 * @return the subPageNo
	 */
	public int getSubPageNo() {
		return subPageNo;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	public BufferedImage getImage() {

		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gd = img.createGraphics();
		gd.setColor(Color.BLACK);
		gd.fillRect(0, 0, width, height);

		final BufferedImage charImg = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D charGD = charImg.createGraphics();
		final Font font = new Font("Monospaced", Font.BOLD, 18);
		charGD.setFont(font);
		Image targetChar = null;


		fillLevel1();
		processX26Enhancements(); // level 1.5 accented chars, and Object invocations
		processDefaultObjects(); // object invoked through MOT

		// now use txt[][], fgColor[][], bgColor[][] and effect[][] to draw on image
		// TODO do we need original lines to lookup CLUT?

		final List<DRCSCharacter> globalDrcsChars = getDRCSChars(false); // global
		final List<DRCSCharacter> localDrcsChars = getDRCSChars(true);

		for (int i = 24; i >=0 ; i--) {
			for (int j = 0; j < 40; j++) {
				boolean doubleWidthUsed = false;
				// first draw the char on a tmp Image
				if ((effect[i][j] & DRCS_CHAR) != 0) {
					// DRCS Char
					targetChar = drawDRCSChar(globalDrcsChars,localDrcsChars, i, j);
				}else if ((effect[i][j] & G3_CHAR) != 0) {
					targetChar = drawG3Char(i, j);
				}else{
					drawChar(charGD, i, j);
					targetChar = charImg;
				}

				// now scale this char image if neccesarry
				int h = charHeight;
				int w = charWidth;
				if ((effect[i][j] & DOUBLE_HEIGHT) != 0) {
					h = charHeight * 2;
				} else if ((effect[i][j] & DOUBLE_WIDTH) != 0) {
					w = charWidth * 2;
					doubleWidthUsed = true; //  skip charafter a double width char

				} else if ((effect[i][j] & DOUBLE_SIZE) != 0) {
					h = charHeight * 2;
					w = charWidth * 2;
					doubleWidthUsed = true; //  skip charafter a double width char
				}

				gd.drawImage(targetChar, j * charWidth, i * charHeight, w, h, null);
				if (doubleWidthUsed) { // this char was double width (or size), so skip next pos
					j++;
					doubleWidthUsed = false; //reset
				}
			}
		}

		return img;
	}

	/**
	 *
	 */
	private void fillLevel1() {
		byte[] rowData = null;
		// level 1.5 page
		boolean doubleHeightUsed1 = false;
		for (int row = 0; row < 25; row++) {
			if(doubleHeightUsed1){
				// last line contained some double heigth chars, this line should contain only level 1 spaces, with same bgcolor as last line
				for(int k=0;k<40;k++){
					txt[row][k]=' ';
					bgColor[row][k]=bgColor[row-1][k];
				}
				doubleHeightUsed1 = false;
			}else{
				if(row==0){
					rowData = new byte[40];
					Arrays.fill(rowData, (byte) 32);
					System.arraycopy(linesList[0].getHeaderDataBytes(), 0, rowData, 8, 32);
				}else if(linesList[row]!=null){
					rowData = linesList[row].getPageDataBytes();
				}else{
					rowData = new byte[40];
					Arrays.fill(rowData, (byte) 32);
				}
				int bg = 0;
				int fg = 7;
				int effectFlags = 0;
				// define outside loop for Hold Mosaics
				char targetChar1;
				char heldMosaicCharacter = ' ';

				for (int column = 0; column < rowData.length; column++) {

					// initial set foreground,background color and effect flags to the 'old' values, before interpreting this char ch
					// if they are updated most of the time they are "Set-After"
					// if they are "Set-At" we will explicitly set the value for this
					fgColor[row][column] = fg;
					bgColor[row][column] = bg;
					effect[row][column] = effectFlags;

					final byte ch = rowData[column];
					if (ch >= 32) { // national option charset subset
						final int nocs = getNationalOptionCharSubset();
						// all chars 0x20..7F
						if ((effectFlags & (MOSAIC_GRAPHICS)) == 0) { // not in block graphics
							targetChar1 = TxtTriplet.getNationalOptionChar(ch, nocs);
						} else { // block graphices, not translated for special national characters
							targetChar1 = (char) ch;
							if ((ch & 0x20) != 0) {
								heldMosaicCharacter = (char) ch;
							}
						}
					} else if ((ch >= 0) && (ch <= 7)) { //12.2 Spacing attributes Alpha Colour Codes ("Set-After")
						fg = ch;
						// clear graphics flags and conceal
						effectFlags = effectFlags & (~MOSAIC_GRAPHICS) & (~CONCEAL);
						heldMosaicCharacter = ' ';
						targetChar1 = ' ';
					} else if (ch == 0x08) { //Flash ("Set-After")
						effectFlags = effectFlags | FLASH;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x09) { //Steady ("Set-At")
						effectFlags = effectFlags & ~ FLASH;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x0A) { //End Box ("Set-After")
						effectFlags = effectFlags & ~ BOX;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x0B) { //Start Box ("Set-After")
						effectFlags = effectFlags | BOX;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}

					} else if (ch == 0x0c) { //Normal Size ("Set-At")
						//  clear DOUBLE_HEIGHT DOUBLE_SIZE,DOUBLE_WIDTH
						final int oldEffectFlags=effectFlags;
						effectFlags = effectFlags & ~(DOUBLE_HEIGHT | DOUBLE_SIZE | DOUBLE_WIDTH);
						if(oldEffectFlags!=effectFlags){
							heldMosaicCharacter = ' ';
						}
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x0d) { //Double Height ("Set-After")
						// set DOUBLE_HEIGHT, clear DOUBLE_SIZE,DOUBLE_WIDTH
						final int oldEffectFlags=effectFlags;
						effectFlags = effectFlags | DOUBLE_HEIGHT;
						effectFlags = effectFlags & ~(DOUBLE_SIZE | DOUBLE_WIDTH);
						if(oldEffectFlags!=effectFlags){
							heldMosaicCharacter = ' ';
						}
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
						doubleHeightUsed1= true; // next line should not be drawn

					} else if (ch == 0x0e) { //Double Width ("Set-After")
						// set DOUBLE_WIDTH, clear DOUBLE_SIZE,DOUBLE_HEIGHT
						effectFlags = effectFlags | DOUBLE_WIDTH;
						effectFlags = effectFlags & ~(DOUBLE_SIZE | DOUBLE_HEIGHT);
						targetChar1 = ' ';
						heldMosaicCharacter = ' ';
					} else if (ch == 0x0f) { //Double Size ("Set-After")
						// set DOUBLE_SIZE, clear DOUBLE_HEIGHT,DOUBLE_WIDTH
						effectFlags = effectFlags | DOUBLE_SIZE;
						effectFlags = effectFlags & ~(DOUBLE_HEIGHT | DOUBLE_WIDTH);
						targetChar1 = ' ';
						heldMosaicCharacter = ' ';
						doubleHeightUsed1= true; // next line should not be drawn
					} else if ((ch >= 0x10) && (ch <= 0x17)) { //12.2 Spacing attributes Mosaic Colour Codes ("Set-After")
						fg = (ch - 0x10);
						effectFlags |= MOSAIC_GRAPHICS;
						// clear conceal
						effectFlags &=  ~CONCEAL;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x18) { //Conceal ("Set-At")
						effectFlags = effectFlags | CONCEAL;
						effect[row][column] = effectFlags;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x19) { //Contiguous Mosaic Graphics ("Set-At") - Start-of-row default condition
						effectFlags = effectFlags & ~(SEPARATED_MOSAIC_GRAPHICS);
						effect[row][column] = effectFlags;
						targetChar1 = ' ';
					} else if (ch == 0x1a) { //Separated Mosaic Graphics ("Set-At")
						effectFlags = effectFlags | SEPARATED_MOSAIC_GRAPHICS;
						effect[row][column] = effectFlags;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x1c) { //Black Background ("Set-At")
						bg = 0;
						bgColor[row][column] = bg;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x1d) { //1New Background ("Set-At")
						bg = fg;
						bgColor[row][column] = bg;
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
					} else if (ch == 0x1e) { //Hold Mosaics ("Set-At")
						effectFlags = effectFlags | HOLD_MOSAIC;
						effect[row][column] = effectFlags;
						targetChar1 = heldMosaicCharacter;
					} else if (ch == 0x1f) { //Release Mosaics ("Set-After")
						if (holdMosaicActive(effectFlags)) {
							targetChar1 = heldMosaicCharacter;
						} else {
							targetChar1 = ' ';
						}
						effectFlags = effectFlags & ~(HOLD_MOSAIC);
					} else { // not implemented, empty space
						logger.warning("Teletext: found not implemented character:"+ch+" at magazine:"+pageHandler.getMagazineNo()+",page:"+pageHandler.getPageNo()+",subpage:"+subPageNo+",row:"+row+", column:"+column);
						targetChar1 = ' ';
					}
					txt[row][column] = targetChar1;
				}
			}
		}
	}

	/**
	 * @param i
	 * @param j
	 * @return
	 */
	private Image drawG3Char(final int i, final int j) {
		final BufferedImage b = new BufferedImage(12,10,BufferedImage.TYPE_BYTE_BINARY);
		final Graphics2D gd = b.createGraphics();
		Image target;
		final int bgC = getColorInt(bgColor[i][j]);
		final int fgC = getColorInt(fgColor[i][j]);
		final IndexColorModel blackAndWhite = new IndexColorModel(
				1, // One bit per pixel
				2,new int[]{fgC,bgC},0,false,-1,DataBuffer.TYPE_BYTE);
		gd.drawImage(g3CharsImage, 0, 0, 12, 10, ((txt[i][j])-32)*12, 0, ((txt[i][j])-31)*12, 10, null);
		final DataBuffer buf = b.getData().getDataBuffer();
		final WritableRaster wr =  Raster.createPackedRaster(buf, 12, 10, 1, null);
		target = new BufferedImage(blackAndWhite, wr, true, null);

		return target;
	}

	/**
	 * @param drcsChars
	 * @param i
	 * @param j
	 * @return
	 */
	private Image drawDRCSChar(final List<DRCSCharacter> globalDrcsChars,final List<DRCSCharacter> localDdrcsChars, final int i, final int j) {
		Image target=null;
		final int bgC = getColorInt(bgColor[i][j]);
		final int fgC = getColorInt(fgColor[i][j]);
		final IndexColorModel blackAndWhite = new IndexColorModel(
				1, // One bit per pixel
				2,new int[]{bgC,fgC},0,false,-1,DataBuffer.TYPE_BYTE);
		if(txt[i][j]<48){ // global
			target = new BufferedImage(blackAndWhite, globalDrcsChars.get(txt[i][j]).getWritableRaster(), true, null);
		}else{
			target = new BufferedImage(blackAndWhite, localDdrcsChars.get(txt[i][j]-64).getWritableRaster(), true, null);
		}

		return target;
	}

	/**
	 * @return
	 */
	private List<DRCSCharacter> getDRCSChars(final boolean local) {
		final SubPage mot = getMOTPage();
		if(mot!=null){
			final int pageNo=pageHandler.getPageNo();
			final int association = mot.getDRCSPageAssociation(pageNo);
			if(association==0){
				return null;
			}
			final List<DRCSLink> drcsLinks= mot.getDRCSLinksLevel25();
			DRCSLink drcsLink =null;
			if(local){
				drcsLink = drcsLinks.get(association%8); // GDRCS == 8, mask that bit out. (COuld also use association & 0x7,
			}else{
				drcsLink = drcsLinks.get(0); // GDRCS == 0,
			}
			final int drcsPageNo = drcsLink.getPageNo();
			final int drcsMagazine = drcsLink.getMagazine();
			// DRCS can have multiple sub pages, If the object data does not fit within one page, additional sub-pages can be used 10.5.1.1 Page Format
			final Page drcsDefPage = getMagazine(drcsMagazine).getPage(drcsPageNo); // now we have the page that defines the correct drcs chars.
			// TODO just ass-u-me a single sub page
			final SubPage drcsDefSubPage = drcsDefPage.getSubPages().values().iterator().next();

			TxtDataField drcsDatafield = null;
			for (final TxtDataField txtDatafield : drcsDefSubPage.packetx_28) {
				if ((txtDatafield != null) && (txtDatafield.getDesignationCode() == 3)) {
					drcsDatafield = txtDatafield;
				}
			}
			if (drcsDatafield != null) {
				final List<Triplet> tripletList = drcsDatafield.getTripletList();

				final BitString bs = new BitString();
				// bits 11-18 of triplet 2
				for (int i = 2; i <= 11; i++) {
					bs.addIntBitsReverse(tripletList.get(i - 1).getVal(), 18);
				}
				// bits 1-12 of triplet 12
				bs.addIntBitsReverse(tripletList.get(11).getVal() & 0xFFF, 12);
				final List<DRCSCharacter> drcsChars = new ArrayList<DRCSCharacter>();

				for (int i = 0; i < 48; i++) {
					final int drcsMode = bs.getIntBitsReverse(4); // 0..15
					final DRCSCharacter drcsChar = new DRCSCharacter(drcsMode, drcsDefSubPage, i);
					drcsChars.add(drcsChar);
				}
				return drcsChars;
			}
		}
		return null;
	}

	/**
	 * @return
	 */
	private void processDefaultObjects() {

		final SubPage mot = getMOTPage();
		if(mot!=null){
			final int pageNo=pageHandler.getPageNo();
			final int association = mot.getObjectPageAssociation(pageNo);
			if(association==0){
				return;
			}
			final List<ObjectLink> objectLinks= mot.getObjectLinksLevel25();
			// can have 2 links for each page, GPOPLink, and POPLink. Both can have 2 default objects.
			if(association>=8){ // has GPOP
				final ObjectLink objectLink = objectLinks.get(0); // GPOP == 0
				processDefaultObjectsLink(objectLink);
			}
			if((association&0x7)!=0){ // has POP
				final ObjectLink objectLink = objectLinks.get((association&0x7)); // POP != 0
				processDefaultObjectsLink(objectLink);
			}
		}
	}

	/**
	 * @param objectLink
	 */
	private void processDefaultObjectsLink(final ObjectLink objectLink) {
		final int objectPageNo = objectLink.getPageNo();
		final int magazine = objectLink.getMagazine();
		if(objectLink.getDefaultObject1Type()!=0){  // default object
			final int object1type = objectLink.getDefaultObject1Type();
			// (G)POP can have multiple sub pages, If the object data does not fit within one page, additional sub-pages can be used 10.5.1.1 Page Format
			final Page objectDefPage = getMagazine(magazine).getPage(objectPageNo); // now we have the page that defines the correct default object.
			final int subPageS1=objectLink.getDefaultObject1SubPageS1();
			final SubPage objectDefSubPage = objectDefPage.getSubPageByS1(subPageS1);
			final int ptrLocation=objectLink.getDefaultObject1PointerLocation();
			final int tripletOffset=objectLink.getDefaultObject1TripletNoOffset();
			final int ptrPosition = objectLink.getDefaultObject1PointerPosition();
			findProcessObjectDefinition(objectDefSubPage, object1type, ptrLocation, tripletOffset, ptrPosition,0,0,0,0);
		}
		if(objectLink.getDefaultObject2Type()!=0){  // default object
			final int object2type = objectLink.getDefaultObject2Type();
			// object 2
			// (G)POP can have multiple sub pages, If the object data does not fit within one page, additional sub-pages can be used 10.5.1.1 Page Format
			final Page objectDefPage = getMagazine(magazine).getPage(objectPageNo); // now we have the page that defines the correct default object.
			final int subPageS1=objectLink.getDefaultObject2SubPageS1();
			final SubPage objectDefSubPage = objectDefPage.getSubPageByS1(subPageS1);
			final int ptrLocation=objectLink.getDefaultObject2PointerLocation();
			final int tripletOffset=objectLink.getDefaultObject2TripletNoOffset();
			final int ptrPosition = objectLink.getDefaultObject2PointerPosition();
			findProcessObjectDefinition(objectDefSubPage, object2type, ptrLocation, tripletOffset, ptrPosition,0,0,0,0);
		}
	}

	/**
	 * @param objectDefSubPage
	 * @param objectType
	 * @param ptrLocation
	 * @param tripletOffset
	 * @param ptrPosition
	 */
	private void findProcessObjectDefinition(final SubPage objectDefSubPage, final int objectType, final int ptrLocation, final int tripletOffset, final int ptrPosition,final int actRow,final int actCol,final int rowOffset, final int colOffset) {
		final TxtDataField txtDataField = objectDefSubPage.linesList[1+ptrLocation]; // ptrLine
		if (txtDataField != null) {
			final int functionByte = getHammingReverseByte(txtDataField.getRawByte(0)); // start counting from payload,this is actual byte 6
			if ((functionByte & MASK_1BIT) != 0) { // PTRLine
				final List<Triplet> tripletList = txtDataField.getTripletList();
				final Triplet tr1 = tripletList.get(objectType + (tripletOffset * 3));
				int tripletStart = 0;
				if((ptrPosition==0)){
					tripletStart = (tr1.getVal() & MASK_9BITS);
				}else{
					tripletStart = (tr1.getVal() & 0x3FE00) >> 9;
				}
				// now we have the  9-bit pointer that gives the absolute triplet number of the definition triplet for the object, starting from line 3
				if(tripletStart!=511){
					final int lineNo = 3 + (tripletStart/13);
					final int tripletNo = tripletStart%13;
					final List<TxtTriplet> objectDefinition = getObjectDefinition(objectDefSubPage, lineNo, tripletNo);
					processTripletList(objectDefinition,objectType,actRow,actCol,rowOffset,colOffset);
				}


			}
		}
	}

	/**
	 * @param objectDefSubPage
	 * @param lineNo
	 * @param tripletOffset
	 * @return
	 */
	private static List<TxtTriplet> getObjectDefinition(final SubPage objectDefSubPage, final int lineNo, final int tripletOffset) {
		
		int lineNumber = lineNo;
		int offset = tripletOffset;
		final List<TxtTriplet> objectDefinition = new ArrayList<TxtTriplet>();

		TxtDataField line = objectDefSubPage.getLine(lineNumber);
		if(line!=null){
			List<TxtTriplet> lineTriplets = line.getTxtTripletList();
			TxtTriplet triplet = lineTriplets.get(offset++);


			do{
				objectDefinition.add(triplet);
				if(offset==13){ // next line
					offset = 0;
					lineNumber++;
					line = objectDefSubPage.getLine(lineNumber);
					if(line!=null){
						lineTriplets = line.getTxtTripletList();
					}
				}
				triplet = lineTriplets.get(offset++);


			}while((triplet!=null)&&(!triplet.isTerminationMarker())&&(!triplet.isObjectDefinition())&&(line!=null));
		}
		return objectDefinition;
	}

	/**
	 * Used toretrieve lines for Object definition page. numbering 1-25 as normal, 26-41 map to line 26, designation code 0-15
	 * @param startLine
	 * @return
	 */
	private TxtDataField getLine(final int lineNo) {
		if(lineNo<=25){
			return linesList[lineNo];
		}else if(lineNo<41){
			return packetx_26[lineNo-26];
		}
		return null;
	}

	/**
	 * @param pageNo
	 * @return
	 */
	private int getObjectPageAssociation(final int pageNo) {
		// assumes this is a MOT page.
		int objectPageAssociation = 0;
		if((pageNo&0x0F)<10){
			final int row=1+(((pageNo&0xF0)>>4) / 2);
			final int col = 2 * (pageNo&0x0F);
			if(linesList[row]!=null){
				objectPageAssociation = getHammingReverseByte(linesList[row].getRawByte(col));
			}

		}else{
			final int row=10+ (((pageNo&0xF0)>>4) / 3);
			final int col = 3 * ((pageNo&0x0F)-10);
			if(linesList[row]!=null){
				objectPageAssociation = getHammingReverseByte(linesList[row].getRawByte(col));
			}
		}
		return objectPageAssociation;
	}

	/**
	 * @param pageNo
	 * @return
	 */
	private int getDRCSPageAssociation(final int pageNo) {
		// assumes this is a MOT page.
		int drcsPageAssociation = 0;
		if((pageNo&0x0F)<10){
			final int row=1+(((pageNo&0xF0)>>4) / 2);
			final int col = 1+(2 * (pageNo&0x0F));
			if(linesList[row]!=null){
				drcsPageAssociation = getHammingReverseByte(linesList[row].getRawByte(col));
			}

		}else{
			final int row=10+ (((pageNo&0xF0)>>4) / 3);
			final int col = 1+ (3 * ((pageNo&0x0F)-10));
			if(linesList[row]!=null){
				drcsPageAssociation = getHammingReverseByte(linesList[row].getRawByte(col));
			}
		}
		return drcsPageAssociation;
	}

	/**
	 * @param charGD
	 * @param i
	 * @param j
	 */

	private void drawChar(final Graphics2D charGD, final int i, final int j) {
		final FontMetrics metrics = charGD.getFontMetrics();
		final int descent = metrics.getDescent();

		charGD.setColor(new Color(getColorInt(bgColor[i][j])));
		charGD.fillRect(0, 0, charWidth, charHeight);
		charGD.setColor(new Color(getColorInt(fgColor[i][j])));
		final int ch = txt[i][j];
		final int characterEffect = effect[i][j];
		if (isMosaicGraphicsMode(characterEffect) && 
			isValidMosaicCharacter(ch)) {
			final int blockH = charHeight / 3;
			final int blockW = charWidth / 2;

			if ((ch & 0x01) != 0) { // top left
				charGD.fillRect(0, 0, blockW, blockH);
			}
			if ((ch & 0x02) != 0) { // top right
				charGD.fillRect(blockW, 0, charWidth - blockW, blockH);
			}
			if ((ch & 0x04) != 0) { // middle left
				charGD.fillRect(0, blockH, blockW, blockH);
			}
			if ((ch & 0x08) != 0) { // middle right
				charGD.fillRect(blockW, blockH, charWidth - blockW, blockH);
			}
			if ((ch & 0x10) != 0) { // bottom left
				charGD.fillRect(0, 2 * blockH, blockW, charHeight - (2 * blockH));
			}
			if ((ch & 0x40) != 0) { // bottom right
				charGD.fillRect(blockW, 2 * blockH, charWidth - blockW, charHeight - (2 * blockH));
			}
			if (((characterEffect & SEPARATED_MOSAIC_GRAPHICS) != 0)) {
				charGD.setColor(new Color(getColorInt(bgColor[i][j])));
				charGD.drawRect(0, 0, charWidth, charHeight);
				charGD.drawLine(0, blockH, charWidth - 1, blockH);
				charGD.drawLine(0, 2 * blockH, charWidth - 1, 2 * blockH);
				charGD.drawLine(blockW, 0, blockW, charHeight - 1);
			}

		} else {
			charGD.drawChars(txt[i], j, 1, 1, charHeight - descent);
		}
	}

	private static boolean isValidMosaicCharacter(final int ch) {
		return (ch < 0x40) || ((ch >= 0x60)&&(ch <= 0x7f));
	}

	private static boolean isMosaicGraphicsMode(final int ef) {
		return (ef & MOSAIC_GRAPHICS) != 0;
	}

	/**
	 * @param i
	 * @return
	 */
	private int getColorInt(final int i) {
		// first look at page level for line 28/0,
		if(i>15){
			if(packetx_28[0]!=null){		// first look at page level for line 28/0,
				return packetx_28[0].getColor(i);
			}else{// then at magazine level line 29/0
				final Magazine mag = pageHandler.getMagazine();
				final TxtDataField line = mag.getPageEnhanceMentDataPackes(0);
				if(line!=null){
					return line.getColor(i);
				}
			}
		}

		if(i==0){
			if(packetx_28[0]!=null){		// first look at page level for line 28/0,
				final TxtDataField line = packetx_28[0];
				if((line.getPageFunction()==0)&&(line.isBlackBackGroundColorSubstitution())){
					return line.getColor(line.getDefaultRowColour());
				}
			}else{// then at magazine level line 29/0
				final Magazine mag = pageHandler.getMagazine();
				final TxtDataField line = mag.getPageEnhanceMentDataPackes(0);
				if((line!=null)&&(line.getPageFunction()==0)&&(line.isBlackBackGroundColorSubstitution())){
					return line.getColor(line.getDefaultRowColour());
				}
			}
		}
		//last return default
		return TxtDataField.getColorInt(i);
	}

	/**
	 *
	 */
	private void processX26Enhancements() {
		final List<TxtTriplet> tripletList = new ArrayList<TxtTriplet>();
		for (final TxtDataField x26 : packetx_26) {
			if((x26!=null)&&(x26.getTxtTripletList()!=null)){
				tripletList.addAll(x26.getTxtTripletList());
			}
		}
		processTripletList(tripletList,NO_OBJECT_TYPE,0,0,0,0);
	}




	/**
	 * @param tripletList
	 * @param objectType
	 * @param row
	 * @param col
	 * @param rowOffset
	 * @param colOffset
	 */
	private void processTripletList(final List<TxtTriplet> tripletList,final int objectType,final int row, final int col,final int rowOffset, final int colOffset) {

		int originModifierRowOffset = 0;
		int originModifierColOffset = 0;
		int actRow = row+rowOffset;
		int actCol = col+colOffset;
		// used for passive objects
		int bgCol=0;
		int fgCol=7;
		int effct = -1;

		if (tripletList != null) {
			for (final TxtTriplet triplet : tripletList) {
				final int address = triplet.getAddress();
				final int mode = triplet.getMode();
				final int data = triplet.getData();
				if(address >= 40){
					if (mode == 0x04) { // Set Active Position
						actRow = rowOffset + ((address == 40) ? 24 : address - 40);
						actCol = colOffset + data;
						if(objectType==ADAPTIVE_OBJECT_TYPE){ // reset colors for new posistion
							fgCol = -1;
							bgCol = -1;
							effct = -1;
						}
					}else if (mode == 0x10) { //Origin Modifier
						originModifierRowOffset = address-40;
						originModifierColOffset = data;
					}else if ((mode >= 0x11)&&(mode <= 0x13)){ // Object Invocation
						final int objectSource = (address&0x18)>>3;
						final int calledObjectType = (mode&0x3);
						final int subPageS1 =data&0xF;
						if((objectSource==3)||(objectSource==2)){ // GPOP or POP
							final SubPage mot = getMOTPage();
							if(mot!=null){
								final int pageNo=pageHandler.getPageNo();
								// only needed for POP
								final int association = mot.getObjectPageAssociation(pageNo);
								if(association==0){
									return;
								}
								final List<ObjectLink> objectLinks= mot.getObjectLinksLevel25();
								ObjectLink objectLink = null;
								if(objectSource==3){
									objectLink = objectLinks.get(0); // GPOP == ,
								}else{ //POP
									objectLink = objectLinks.get(association);
								}

								final int objectPageNo = objectLink.getPageNo();
								final int magazine = objectLink.getMagazine();
								final Page objectDefPage = pageHandler.getMagazine(magazine).getPage(objectPageNo); // now we have the page that defines the correct default object.
								final SubPage objectDefSubPage = objectDefPage.getSubPageByS1(subPageS1);
								final int ptrLocation = address &0x3;
								final int tripletOffset = (data & 0x60)>>5;
								final int ptrPosition = (data&0x10)>>4;
								findProcessObjectDefinition(objectDefSubPage, calledObjectType, ptrLocation, tripletOffset, ptrPosition,actRow,actCol,originModifierRowOffset,originModifierColOffset);
								originModifierRowOffset = 0;
								originModifierColOffset = 0;
							}
						}
					}else if ((mode >= 0x15)&&(mode <= 0x17)){ // Object Definition
						// EMPTY function, only error checking
						if((mode&0x3)!=objectType){
							logger.log(Level.INFO,"Object definition type :"+(mode&0x3)+" does not match called expected type "+objectType);
						}
					}else if (mode == 0x1F) { //Termination Marker
						break;
					}else{
						logger.log(Level.FINE,"not implemented triplet:"+triplet);
					}
				}else{	// (address < 40) AKA Column Address Triplets
					if(objectType==ADAPTIVE_OBJECT_TYPE){
						//color from last pos until new
						for (int i = actCol; i <= (colOffset + address); i++) {
							if(fgCol!=-1){
								fgColor[actRow][i]=fgCol;
							}
							if(bgCol!=-1){
								bgColor[actRow][i]=bgCol;
							}
							if(effct!=-1){
								setEffect(effect[actRow][i],effct);
							}
						}
					}
					actCol = colOffset + address;
					if (mode == 0) { //Foreground Colour
						// this is implementation for active, TODO passive and adaptive
						if(data<32){ // When data field bits D6 and D5 are both set to '0',
							if(objectType==ACTIVE_OBJECT_TYPE){
								fgColor[actRow][actCol] = data;
								for(int i = actCol+1; ((i<40)
										&&(getPageDataByte(actRow, i)>7) // alpha codes 0-7, so anything above is fine
										&& !((getPageDataByte(actRow, i)>=0x10)&&(getPageDataByte(actRow, i)<=0x17)) // except Mosaic Colour Codes
										)
										; i++){
									fgColor[actRow][i] = data;
								}
							}else if (objectType==ADAPTIVE_OBJECT_TYPE){
								fgCol=data;
								fgColor[actRow][actCol] = data;
							}else if (objectType==PASSIVE_OBJECT_TYPE){
								fgCol=data;
							}
						}
					}else if (mode == 0x1) { //Block Mosaic Character from the G1 Set
						txt[actRow][actCol] = (char)data;
						effect[actRow][actCol]|=MOSAIC_GRAPHICS;
						if(objectType==PASSIVE_OBJECT_TYPE){
							fgColor[actRow][actCol]=fgCol;
							bgColor[actRow][actCol]=bgCol;
						}
					}else if (mode == 0x3) { //Background Colour
						if(data<32){ // When data field bits D6 and D5 are both set to '0',
							if(objectType==ACTIVE_OBJECT_TYPE){
								bgColor[actRow][actCol] = data;
								for(int i = actCol+1; ((i<40) &&(getPageDataByte(actRow, i)!=0x1C)&&(getPageDataByte(actRow, i)!=0x1D)); i++){ // 1/C Black Background , 1/D New Background
									bgColor[actRow][i] = data;
								}
							}else if (objectType==ADAPTIVE_OBJECT_TYPE){
								bgCol=data;
								bgColor[actRow][actCol] = data;
							}else if (objectType==PASSIVE_OBJECT_TYPE){
								bgCol=data;
							}
						}
					}else if (mode == 0x9) { //Character from the G0 Set at Levels 2.5 and 3.5
						txt[actRow][actCol] = (char) TxtTriplet.G0_sets[0][data];
						if(objectType==PASSIVE_OBJECT_TYPE){
							fgColor[actRow][actCol]=fgCol;
							bgColor[actRow][actCol]=bgCol;
						}
					}else if (mode == 0xB) { //Line Drawing and Smoothed Mosaic Character from the G3 Set at Levels 2.5 and 3.5
						txt[actRow][actCol] = (char)data;
						effect[actRow][actCol]|=G3_CHAR;
						if(objectType==PASSIVE_OBJECT_TYPE){
							fgColor[actRow][actCol]=fgCol;
							bgColor[actRow][actCol]=bgCol;
						}
					}else if (mode == 0xD) { //DRCS Character Invocation
						txt[actRow][actCol] = (char)data;
						effect[actRow][actCol]|=DRCS_CHAR;
						effect[actRow][actCol]&= ~(DOUBLE_HEIGHT | DOUBLE_WIDTH | DOUBLE_SIZE); // no double heigth, this is not mentioned explicit in EN300706, need this to make SWR text work.
						if(objectType==PASSIVE_OBJECT_TYPE){
							fgColor[actRow][actCol]=fgCol;
							bgColor[actRow][actCol]=bgCol;
						}
					}else if (mode == 0xF) { //Character from the G2 Supplementary Set
						txt[actRow][actCol] = (char) TxtTriplet.G2_sets[0][data];
						if(objectType==PASSIVE_OBJECT_TYPE){
							fgColor[actRow][actCol]=fgCol;
							bgColor[actRow][actCol]=bgCol;
						}
					}else if (mode == 16) {
						//NOTE 3: The @ symbol replaces the * symbol at position 2/A when the table is accessed via a packet X/26 Column
						// Address triplet with Mode Description = 10 000 and Data = 0101010. See clause 12.2.4.
						txt[actRow][actCol] = (char) TxtTriplet.G0_sets[0][data];
						if(objectType==PASSIVE_OBJECT_TYPE){
							fgColor[actRow][actCol]=fgCol;
							bgColor[actRow][actCol]=bgCol;
						}
					}else if(mode > 16){// Characters Including Diacritical Marks
						txt[actRow][actCol] = (char) TxtTriplet.getCombinedCharacter(data, mode & 0xF);
						if(objectType==PASSIVE_OBJECT_TYPE){
							fgColor[actRow][actCol]=fgCol;
							bgColor[actRow][actCol]=bgCol;
						}
					}else{
						logger.log(Level.FINE,"not implemented triplet:"+triplet);
					}
				}
			}
		}
	}

	/**
	 * @param i
	 * @param effct
	 */
	private void setEffect(final int i, final int effct) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param actRow
	 * @param i
	 * @return
	 */
	private byte getPageDataByte(final int actRow, final int i) {
		if(linesList[actRow]!=null){
			return linesList[actRow].getPageDataByte(i);
		}
		return (byte)0x20; // line does not exits, same as line full of spaces
	}

	/**
	 * @param effectFlags
	 * @return
	 */
	private static boolean holdMosaicActive(final int effectFlags) {
		return ((effectFlags & HOLD_MOSAIC) != 0);
	}

	private boolean isMOTpage() {
		return (getPageNo() == 0xFE);
	}

	@Override
	public String toString(){
		return "Mag:"+pageHandler.getMagazineNo()+", pageNo"+pageHandler.getPageNo()+". sub:"+subPageNo;
	}

	private boolean isBTTpage() {
		return ((getPageNo() == 0xF0)&&(getMagazineNo()==1));
	}

	private boolean isAITpage() {
		return isTOPpage(2);
	}

	private boolean isMPTpage() {
		return isTOPpage(1);
	}

	private boolean isTOPpage(final int type) {
		final SubPage btt= getBTTPage();
		if(btt!=null){
			for(int i=21;i<23;i++){
				final TxtDataField txtDataField=btt.getLine(i);
				if(txtDataField!=null){
					for (int j = 0; j < 5; j++) {
						final int magNo=getHammingReverseByte(txtDataField.getRawByte(j*8));
						final int pagNo= (getHammingReverseByte(txtDataField.getRawByte((j*8)+1))*16) + getHammingReverseByte(txtDataField.getRawByte((j*8)+2));
						final int t = getHammingReverseByte(txtDataField.getRawByte((j*8)+7));
						if((t==type)&&(pagNo==getPageNo())&&(magNo==getMagazineNo())){
							return true;
						}

					}
				}
			}

		}

		return false;
	}

	/**
	 * @return
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.ebu.Page#getMagazine()
	 */
	public Magazine getMagazine() {
		return pageHandler.getMagazine();
	}

	/**
	 * @param m
	 * @return
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.ebu.Page#getMagazine(int)
	 */
	public Magazine getMagazine(final int m) {
		return pageHandler.getMagazine(m);
	}

	/**
	 * @return
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.ebu.Page#getMagazineNo()
	 */
	public int getMagazineNo() {
		return pageHandler.getMagazineNo();
	}

	/**
	 * @return
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.ebu.Page#getPageNo()
	 */
	public int getPageNo() {
		return pageHandler.getPageNo();
	}

}