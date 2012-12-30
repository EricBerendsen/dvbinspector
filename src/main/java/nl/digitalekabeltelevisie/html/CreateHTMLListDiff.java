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

public class CreateHTMLListDiff implements Runnable{

	private TransportStream newTransportStream;
	private TransportStream oldTransportStream;
	private static String bgColorCSS="background-color:yellow;";


	public CreateHTMLListDiff() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final CreateHTMLListDiff inspector = new CreateHTMLListDiff();
		inspector.run();
	}

	public void run() {



		newTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 12-19 08-51-59.ts");
		oldTransportStream  = new TransportStream("d:\\ts\\Ziggo Oost 369000 12-12 08-02-42.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 11-21 15-52-01.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 11-14 06-47-48.ts");
		//newTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-29 15-01-50.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-25 11-33-33.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-25 11-33-33.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-21 22-47-10.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-02 14-31-15.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 09-03 14-16-44.ts");

		// oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 07-02 20-42-06.ts");
		// newTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 09-03 14-16-44.ts");
		// oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 07-02 20-42-06.ts");
		// newTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 09-03 14-16-44.ts");
		//		oldTransportStream = new TransportStream("d:\\ts\\upc20120805\\UPC 164000 08-05 17-15-22partial.ts");
		//		oldTransportStream = new TransportStream("d:\\ts\\upc20120805\\UPC 164000 08-05 17-15-22partial.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 06-17 11-09-46.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 05-15 19-10-43.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 05-03 08-49-10.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 04-03 11-06-14.ts");
		try {
			newTransportStream.parseStream();
			oldTransportStream.parseStream();
		} catch (final IOException e) {

			e.printStackTrace();
		}

		//newTransportStream.namePIDs();
		System.out.println(newTransportStream);
		writeHTML(newTransportStream,oldTransportStream);


		if( !java.awt.Desktop.isDesktopSupported() ) {

			System.err.println( "Desktop is not supported (fatal)" );
			System.exit( 1 );
		}

		final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

		if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {

			System.err.println( "Desktop doesn't support the browse action (fatal)" );
			System.exit( 1 );
		}

		String filename="";
		filename = "file:///d:/eric/diff"+ newTransportStream.getFile().getName().replaceAll(" ","%20") +".html";

		try {

			final java.net.URI uri = new java.net.URI( filename );
			desktop.browse( uri );
		}
		catch ( final Exception e ) {

			System.err.println( e.getMessage() );
		}		System.out.println("Klaar!!");


	}

	private void writeHTML(final TransportStream newStream,final TransportStream oldStream) {

		try {
			final String filename=newStream.getFile().getName();

			final FileWriter fstream = new FileWriter("d:\\eric\\diff"+filename+".html");
			final BufferedWriter out = new BufferedWriter(fstream);

			out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head><script src=\"sorttable.js\"></script></head>\n");
			out.write("<body>\n");
			out.write("\n");

			out.write("<table class=\"sortable\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\">\n");
			out.write("<thead><tr><th>Transport Stream</th><th>Zender</th><th>Type (TV/Radio) - (RAW)</th><th>Service-ID</th><th>logical channel</th></tr></thead>\n");
			out.write("<tbody>\n");

			final SDT sdt = newStream.getPsi().getSdt();
			final SDT oldSDT = oldStream.getPsi().getSdt();

			final Map<Integer, SDTsection[]> streams = sdt.getTransportStreams();
			final TreeSet<Integer> s = new TreeSet<Integer>(streams.keySet());

			for(final Integer transportStreamID: s){
				//out.write("<tr><th colspan=\"4\">"+transportStreamID+"</th></tr>\n");
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
					final Service oldService=oldSDT.getService(sid);

					final int lcn = newStream.getPsi().getNit().getLCN(1000, transportStreamID, sid);
					//final int lcn = newStream.getPsi().getNit().getLCN(43136, transportStreamID, sid);
					String lcnString = (lcn > 0) ? Integer.toString(lcn) : "-";

					final int hdLCN = newStream.getPsi().getNit().getHDSimulcastLCN(1000, transportStreamID, sid);
					final String hdlcnString = (hdLCN > 0) ? (" / " + Integer.toString(hdLCN)) : "";
					lcnString += hdlcnString;

					final String newName= sdt.getServiceName(sid);
					final String safeName= Utils.escapeHTML(newName);
					String style="";
					if(oldService==null){ //service is new, mark whole line
						style="style=\""+bgColorCSS+"\"";
					}
					out.write("<tr "+style+">");
					style="";
					if((oldService!=null)&&(transportStreamID!=oldSDT.getTransportStreamID(sid))){
						style=bgColorCSS;
					}
					out.write("<td style=\"text-align: left;" +style+"\">\n"+transportStreamID+"</td>");

					style="";
					if ((oldService != null) && (newName != null) && (!newName.equals(oldSDT.getServiceName(sid)))) {
						style=bgColorCSS;
					}

					out.write("<td style=\"text-align: left;" +style+"\">\n"+safeName+"</td>\n");
					style="";
					if((oldService!=null)&&(sdt.getServiceType(sid)!=oldSDT.getServiceType(sid))){
						style=bgColorCSS;
					}
					out.write("<td style=\"text-align: left;" +style+"\">"+Descriptor.getServiceTypeString(sdt.getServiceType(sid))+" ("+sdt.getServiceType(sid)+") </td>\n");
					style="";
					final int oldLCN = oldStream.getPsi().getNit().getLCN(1000, oldSDT.getTransportStreamID(sid), sid);
					String old = "";
					if ((oldService != null)
 && (lcn != oldLCN)) {
						style="style=\""+bgColorCSS+"\"";
						old = " [" + oldLCN + "] ";

					}
					out.write("<td style=\"text-align: left;\">" + sid + " </td><td " + style + ">" + lcnString + old
							+ "</td></tr>\n");

				}


			}


			out.write("	\n");
			out.write("<br><br>	\n");
			out.write("	\n");
			out.write("	</tbody></table>\n");
			out.write("	<p><br></p><p>Onderstaande lijst bevat alle frequenties zoals ze gebruikt worden per regionaal netwerk.</p>\n");
			out.write("	<p><br></p>\n");
			out.write("	<table class=\"content\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\"><thead><tr><th>Transportstream-ID</th><th>Frequentie</th><th>Modulatie</th></tr></thead><tbody>\n");

			final NIT nit = newStream.getPsi().getNit();
			final NIT oldNIT = oldStream.getPsi().getNit();

			final Map<Integer, NITsection []> networks = nit.getNetworks();
			final Map<Integer, NITsection []> oldNetworks = oldNIT.getNetworks();

			final TreeSet<Integer> t = new TreeSet<Integer>(networks.keySet());

			final Iterator<Integer> j = t.iterator();
			while(j.hasNext()){
				final int networkNo=j.next();
				out.write("<tr><th colspan=\"3\">"+networkNo+" : "+nit.getNetworkName(networkNo)+"</th></tr>\n");
				final ArrayList<nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream> newStreams = new ArrayList<nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream>();
				final NITsection [] sections = networks.get(networkNo);
				oldNetworks.get(networkNo);

				for(final NITsection section: sections){
					if(section!= null){
						newStreams.addAll(section.getTransportStreamList());
					}
				}

				Collections.sort(newStreams, new Comparator<nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream>(){
					public int compare(final nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream s1, final nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream s2){
						return(s1.getTransportStreamID()-s2.getTransportStreamID());
					}
				});

				for (final nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream stream: newStreams) {
					final int streamID=stream.getTransportStreamID();
					final Iterator<Descriptor> descs=stream.getDescriptorList().iterator();
					String freq="";
					int mod=-1;
					String modulation = "";

					CableDeliverySystemDescriptor newDelivery = null;
					while(descs.hasNext()){
						final Descriptor d=descs.next();
						if(d instanceof CableDeliverySystemDescriptor) {
							newDelivery = (CableDeliverySystemDescriptor)d;
							freq =  Descriptor.formatCableFrequencyList(((CableDeliverySystemDescriptor)d).getFrequency());
							mod = ((CableDeliverySystemDescriptor)d).getModulation();
							modulation = CableDeliverySystemDescriptor.getModulationString(mod);
						}
					}
					String style="";
					final nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream oldNITStream = oldNIT.getTransportStream(networkNo, streamID);
					if(oldNITStream==null){ // new transportstream
						style="style=\""+bgColorCSS+"\"";
					}

					out.write("<tr " +style+">\n");
					out.write("<td>"+streamID+"</td>\n");

					style="";
					CableDeliverySystemDescriptor oldDelivery = null;
					if(oldNITStream!=null){
						// find deliveryDescriptor for old stream
						final Iterator<Descriptor> oldDescs=oldNITStream.getDescriptorList().iterator();
						while(oldDescs.hasNext()){
							final Descriptor d=oldDescs.next();
							if(d instanceof CableDeliverySystemDescriptor) {
								oldDelivery = (CableDeliverySystemDescriptor)d;
							}
						}
						if((oldDelivery!=null)&&(!oldDelivery.getFrequency().equals(newDelivery.getFrequency()))){
							style="style=\""+bgColorCSS+"\"";
						}
					}
					out.write("<td " + style+">"+freq+" </td>");

					style="";
					if((oldNITStream!=null)&&(oldDelivery!=null)&&(oldDelivery.getModulation()!=newDelivery.getModulation())){
						style="style=\""+bgColorCSS+"\"";
					}
					out.write("<td " + style+">"+modulation+ "</td></tr>\n");

				}

			}


			out.write("\n");
			out.write("	</tbody></table>\n");

			out.write("\n");
			out.write("</body></html>");

			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}


	public static String stripLeadingZero(final String s){
		String f = s;
		while((f.length()>1)&&(f.charAt(0)=='0')&&(f.charAt(1)!='.')){
			f=f.substring(1);
		}
		return f;
	}

}
