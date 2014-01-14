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

public class CreateHTMLListVeendamRekam implements Runnable{

	private TransportStream transportStream;


	public CreateHTMLListVeendamRekam() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final CreateHTMLListVeendamRekam inspector = new CreateHTMLListVeendamRekam();
		inspector.run();
	}

	public void run() {

		transportStream = new TransportStream("d:\\ts\\rekam\\2012-5-5-15-42-163000-6875-C.ts");
		try {
			transportStream.parseStream();
		} catch (final IOException e) {

			e.printStackTrace();
		}

		System.out.println(transportStream);
		writeHTML(transportStream);
		System.out.println("Klaar!!");


	}

	private void writeHTML(final TransportStream tStream) {

		try {
			final FileWriter fstream = new FileWriter("d:\\eric\\rekam20120505.html");
			final BufferedWriter out = new BufferedWriter(fstream);

			out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head><script src=\"sorttable.js\"></script></head>\n");
			out.write("<body>\n");

			out.write("<table class=\"content\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\">\n");

			final SDT sdt = tStream.getPsi().getSdt();

			final NIT nit = tStream.getPsi().getNit();
			final Map<Integer, NITsection []> networks = nit.getNetworks();


			out.write("<thead><tr><th>Zender</th><th>Type (TV/Radio)<br> - (RAW)</th><th>Service-ID</th><th>Logical Channel Number</th>\n");

			final TreeSet<Integer> networksSet = new TreeSet<Integer>(networks.keySet());
			out.write("</tr></thead>\n");


			out.write("<tbody>\n");

			final Map<Integer, SDTsection[]> streams = sdt.getTransportStreams();
			final TreeSet<Integer> s = new TreeSet<Integer>(streams.keySet());

			for(final Integer transportStreamID: s){
				out.write("<tr><th colspan=\"4\">"+transportStreamID+"</th></tr>\n");
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
					//for(Integer nid: networksSet){

					final int lcn = tStream.getPsi().getNit().getLCN(777, transportStreamID, sid);
					final String lcnString = (lcn>0)?Integer.toString(lcn):"-";
					out.write("<td>"+lcnString +"</td>");

					//}
					out.write("</tr>\n");
				}


			}

			out.write("	\n");
			out.write("<br><br>	\n");
			out.write("	\n");
			out.write("	</tbody></table>\n");
			out.write("	<p><br></p><p>Onderstaande lijst bevat alle frequenties van de transport streams.</p>\n");
			out.write("	<p><br></p>\n");
			out.write("	<table class=\"content\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th>Transportstream-ID</th><th>Frequentie</th><th>Modulatie</th></tr></thead><tbody>\n");


			final Iterator<Integer> j = networksSet.iterator();
			while(j.hasNext()){
				final int networkNo=j.next();

				//out.write("<tr><th colspan=\"3\">"+networkNo+" : "+nit.getNetworkName(networkNo)+"</th></tr>\n");
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
					String freq="-";
					int mod=-1;
					String modulation = "-";
					while(descs.hasNext()){
						final Descriptor d=descs.next();
						if(d instanceof CableDeliverySystemDescriptor) {
							freq = Descriptor.formatCableFrequency(((CableDeliverySystemDescriptor)d).getFrequency());
							mod = ((CableDeliverySystemDescriptor)d).getModulation();
							modulation = CableDeliverySystemDescriptor.getModulationString(mod);
							((CableDeliverySystemDescriptor)d).getSymbol_rate();

						}
					}

					out.write("<tr><td>"+sid+"</td>\n");
					out.write("<td>"+freq+" </td><td>"+modulation+ "</td></tr>\n");

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
