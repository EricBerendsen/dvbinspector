package nl.digitalekabeltelevisie.html;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.CableDeliverySystemDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.NIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.NITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDT;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDTsection.Service;
import nl.digitalekabeltelevisie.util.Utils;

public class CreateHTMLListCaiw implements Runnable{

	private TransportStream transportStream;


	public CreateHTMLListCaiw() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final CreateHTMLListCaiw inspector = new CreateHTMLListCaiw();
		inspector.run();
	}

	public void run() {

		// transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\multikabel\\2008-8-29-21-22-514000-6875-C.ts");
		// transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\onsnet\\2008-11-19-13-20-303000-6956-C.ts");
		// transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\casema\\2008-6-15-21-45-356000-6875-C.ts");
		// transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\upc\\upc_f284Mhz.ts");
		// transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\417_2009_02_25_18_30_02.DAT");
		// transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\417_2009_02_24_20_44_58.DAT");
		//transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\369_2009_04_30_19_49_34.DAT");
		transportStream = new TransportStream("d:\\ts\\dvb-c\\Caiway 554000 23-8-2013-6900.ts");
		//transportStream = new TransportStream("d:\\ts\\caiway\\2013-7-4-20-19-538000-6900-C.ts");
		//transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\369_2009_07_03_09_33_12.DAT");
		//transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\818_2009_03_05_17_59_31.DAT");
		//transportStream = new TransportStream("C:\\eric\\mpeg\\ts\\618_2009_03_09_19_59_44.DAT");
		//TSReader tsReader = new TSReader(transportStream);
		try {
			transportStream.parseStream();
		} catch (final IOException e) {

			e.printStackTrace();
		}

		transportStream.namePIDs();
		System.out.println(transportStream);
		writeHTML(transportStream);
		System.out.println("Klaar!!");


	}

	private void writeHTML(final TransportStream tStream) {

		try {
			final FileWriter fstream = new FileWriter("d:\\eric\\caiw20130823.html");
			final BufferedWriter out = new BufferedWriter(fstream);

			out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head><script src=\"sorttable.js\"></script></head>\n");
			out.write("<body>\n");

			out.write("	<table class=\"content\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\">\n");

			final SDT sdt = tStream.getPsi().getSdt();

			final NIT nit = tStream.getPsi().getNit();
			final Map<Integer, NITsection []> networks = nit.getNetworks();
			int numberNITS = networks.size();


			out.write("<thead><tr><th rowspan=\"2\">Zender</th><th rowspan=\"2\">Type (TV/Radio)<br> - (RAW)</th><th rowspan=\"2\">Service-ID</th><th colspan=\""+numberNITS+"\">Logical Channel Number per Netwerk ID</th></tr>\n");
			out.write("<tr>");

			final TreeSet<Integer> networksSet = new TreeSet<Integer>(networks.keySet());
			for(final Integer nid: networksSet){
				out.write("<th>"+nid +"</th>");

			}
			out.write("</tr></thead>\n");


			out.write("<tbody>\n");

			final Map<Integer, SDTsection[]> streams = sdt.getTransportStreams();
			final TreeSet<Integer> s = new TreeSet<Integer>(streams.keySet());

			for(final Integer transportStreamID: s){
				out.write("<tr><th colspan=\""+(3+numberNITS)+"\">"+transportStreamID+"</th></tr>\n");
				final SDTsection [] sections = streams.get(transportStreamID);
				final ArrayList<Service> serviceList = new ArrayList<Service>();

				for (final SDTsection section: sections) {
					if(section!= null){
						serviceList.addAll(section.getServiceList());
					}
				}

				Collections.sort(serviceList, new Comparator<Service>(){
					public int compare(final Service s1, final Service s2){
						return(s1.getServiceID()-s2.getServiceID());
					}
				});
				for (final Service element: serviceList) {
					final int sid=element.getServiceID();



					//String safeName= sdt.getServiceName(sid).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
					final String safeName= Utils.escapeHTML(sdt.getServiceName(sid));
					out.write("<tr><td style=\"text-align: left;\">\n"+safeName+"</td>\n");
					out.write("<td style=\"text-align: left;\">"+Descriptor.getServiceTypeStringShort(sdt.getServiceType(sid))+" ("+sdt.getServiceType(sid)+") </td>\n");
					out.write("<td style=\"text-align: left;\">"+sid+" </td>\n");
					for(final Integer nid: networksSet){

						final int lcn = tStream.getPsi().getNit().getLCN(nid, transportStreamID, sid);
						final String lcnString = (lcn>0)?Integer.toString(lcn):"-";
						out.write("<td>"+lcnString +"</td>");

					}
					out.write("</tr>\n");
				}


			}

			out.write("	\n");
			out.write("<br><br>	\n");
			out.write("	\n");
			out.write("	</tbody></table>\n");
			out.write("	<p><br></p><p>Onderstaande lijst bevat alle frequenties zoals ze gebruikt worden per regionaal netwerk.</p>\n");
			out.write("	<p><br></p>\n");
			out.write("	<table class=\"content\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th>Transportstream-ID</th><th>Frequentie</th><th>Modulatie</th><th>Symbol Rate</th></tr></thead><tbody>\n");


			final Iterator<Integer> j = networksSet.iterator();
			while(j.hasNext()){
				final int networkNo=j.next();

				out.write("<tr><th colspan=\"4\">"+networkNo+" : "+nit.getNetworkName(networkNo)+"</th></tr>\n");
				final ArrayList<nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream> tstreams = new ArrayList<nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream>();
				final NITsection [] sections = networks.get(networkNo);

				for(final NITsection section: sections){
					if(section!= null){
						tstreams.addAll(section.getTransportStreamList());
					}
				}

				Collections.sort(tstreams, new Comparator<nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream>(){
					public int compare(final nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream s1, final nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream s2){
						return(s1.getTransportStreamID()-s2.getTransportStreamID());
					}
				});

				for (final nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream stream: tstreams) {
					final int sid=stream.getTransportStreamID();
					final Iterator<Descriptor> descs=stream.getDescriptorList().iterator();
					String freq="";
					int mod=-1;
					String modulation = "";
					String symbolrate = "";

					while(descs.hasNext()){
						final Descriptor d=descs.next();
						if(d instanceof CableDeliverySystemDescriptor) {
							freq = Descriptor.formatCableFrequency(((CableDeliverySystemDescriptor)d).getFrequency());
							mod = ((CableDeliverySystemDescriptor)d).getModulation();
							modulation = CableDeliverySystemDescriptor.getModulationString(mod);
							symbolrate = Descriptor.formatSymbolRate(((CableDeliverySystemDescriptor)d).getSymbol_rate());

						}
					}

					out.write("<tr><td>"+sid+"</td>\n");
					out.write("<td>"+freq+" </td><td>"+modulation+ "</td><td>"+symbolrate+ "</td></tr>\n");

				}
			}

			out.write("	\n");
			out.write("	</tbody></table>\n");

			out.write("");
			out.write("</body></html>");

			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
