package nl.digitalekabeltelevisie.data.mpeg.pes.ttml;
/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_32BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_40BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.CRCcheck;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.gui.XMLSource;
import nl.digitalekabeltelevisie.util.LookUpList;

// EN 303 560 V1.1.1 (2018-05)
public class TtmlPesDataField extends PesPacketData {
	
	
	private static final Logger logger = Logger.getLogger(TtmlPesDataField.class.getName());
	
	private final long segment_mediatime;
	private final int num_of_segments;
	private final List<Segment> segmentList = new ArrayList<>();
	
	private final long crc_32;
	private boolean crc32Failed;

	public static class Segment implements TreeNode, XMLSource{
		
		final LookUpList segmentTypeList = new LookUpList.Builder().
				add(0x00,"reserved for future use").
				add(0x01, "uncompressed TTML document").
				add(0x02, "gzip compressed TTML document").
				add(0x03,0xFF, "reserved for future use").
				build();
				


		private final int segment_type;
		private final int segment_length;
		private final byte[] segment_data_field;
		private String xml;

		public Segment(int segment_type, int segment_length, byte[] segment_data_field) {
			super();
			this.segment_type = segment_type;
			this.segment_length = segment_length;
			this.segment_data_field = segment_data_field;
			if(segment_type==1) {
				xml = new String(segment_data_field, StandardCharsets.UTF_8);
			}else if(segment_type==2) { // compressed 
				try {
					GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(segment_data_field));
					ByteArrayOutputStream byteout = new ByteArrayOutputStream();
	
					int res = 0;
					byte buf[] = new byte[1024];
					while (res >= 0) {
						res = gzin.read(buf, 0, buf.length);
					    if (res > 0) {
					        byteout.write(buf, 0, res);
					    }
					}
					byte uncompressed[] = byteout.toByteArray();
					xml = new String(uncompressed, StandardCharsets.UTF_8);
				} catch (IOException e) {
					xml = "gunzip error";
					e.printStackTrace();
				}

			}else {
				xml = "error";
			}
		}

		
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("Ttml Segment"));
			s.add(new DefaultMutableTreeNode(new KVP("segment_type",segment_type,segmentTypeList.get(segment_type))));
			s.add(new DefaultMutableTreeNode(new KVP("segment_length",segment_length,null)));
			s.add(new DefaultMutableTreeNode(new KVP("segment_data_field",segment_data_field,null)));
			KVP xmlKvp = new KVP("xml",xml,null);
			xmlKvp.setXmlSource(this);
			s.add(new DefaultMutableTreeNode(xmlKvp));
			return s;
			
		}



		@Override
		public String getXML() {
			return xml;
		}
	}
	
	protected TtmlPesDataField(PesPacketData pesPacket) {
		super(pesPacket);
		int offset = pesDataStart;

		segment_mediatime = getLong(data, offset, 6, MASK_40BITS);
		offset += 6;
		num_of_segments = getInt(data, offset++, 1, MASK_8BITS);
		for (int i = 0; i < num_of_segments; i++) {
			int segment_type = getInt(data, offset++, 1, MASK_8BITS);
			int segment_length = getInt(data, offset, 2, MASK_16BITS);
			offset += 2;
			byte[] segment_data_field = Arrays.copyOfRange(data, offset, offset+segment_length);
			offset += segment_length;
			Segment segment = new Segment(segment_type, segment_length, segment_data_field);
			segmentList.add(segment);
		}
		
		crc_32  = getLong(data, offset, 4, MASK_32BITS);
		
		final long checkRes = CRCcheck.crc32(data,pesDataStart, offset+4 - pesDataStart);
		if(checkRes!=0) {
			logger.warning("crc32 check failed, checkRes="+checkRes);
			crc32Failed=true;
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode s = super.getJTreeNode(modus,new KVP("Ttml Subtitling PES Packet"));
		s.add(new DefaultMutableTreeNode(new KVP("segment_mediatime",segment_mediatime,"(* 100 microseconds")));
		s.add(new DefaultMutableTreeNode(new KVP("num_of_segments",num_of_segments,null)));
		addListJTree(s,segmentList,modus,"Segments");
		s.add(new DefaultMutableTreeNode(new KVP("crc_32",crc_32,crc32Failed?"error in CRC Check":null)));


		return s;
	}


}
