package nl.digitalekabeltelevisie.html;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import nl.digitalekabeltelevisie.data.mpeg.psi.TDT;
import nl.digitalekabeltelevisie.data.mpeg.psi.TDTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.TOT;
import nl.digitalekabeltelevisie.data.mpeg.psi.TOTsection;
import nl.digitalekabeltelevisie.util.Utils;

public class CreateHTMLListDiff implements Runnable{

	private TransportStream newTransportStream;
	private TransportStream oldTransportStream;
	private static String bgColorCSS="background-color:yellow;";
	private static String strikeCSS="text-decoration: line-through; background-color: orange;";

	//private static String outputDir="d:/88/";
	private static String outputDir="C:/Users/Eric/workspace/dktv/WebContent/techniek/";

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





		newTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-10 09-21-35.ts");
		oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-09 08-47-06.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-04 11-15-18.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-02 08-15-22.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 10-01 10-42-17.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 09-30 07-21-16.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 09-25 08-05-48.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 09-11 08-29-21.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 09-10 11-27-43.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 08-09 11-01-22.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 08-02 08-19-13.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 07-31 16-29-16.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 07-24 08-54-40.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 06-12 12-17-48.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 06-08 09-43-28.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 06-01 08-19-38.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 04-16 21-46-57.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 04-13 10-21-40.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 04-10 11-06-59.ts");
		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 04-10 11-06-59.ts");

		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 04-02 21-57-13.ts");

		//oldTransportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 03-15 09-28-41.ts");
		//oldTransportStream  = new TransportStream("d:\\ts\\Ziggo Oost 369000 12-12 08-02-42.ts");
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


		String tvhome_prefix_1="<!-- tpl:insert page=\"/newstemplate.htpl\" -->\n<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head>\n<meta http-equiv=\"content-language\" content=\"nl\">\n<meta name=\"owner\" content=\"info@digitalekabeltelevisie.nl\">\n<meta name=\"author\" content=\"Eric Berendsen\">\n<!-- tpl:put name=\"description\" -->\n\n<script src=\"/theme/sorttable.js\" type=\"text/javascript\"></script>\n<meta name=\"description\"\ncontent=\"Overzicht gebruikte kanalen en frequenties bij Ziggo Oost (voormalig @home gebied).\">\n\n\n\n\n<!-- /tpl:put -->\n<!-- tpl:put name=\"keywords\" -->\n<meta name=\"keywords\"\ncontent=\"digitale televisie, DVB-C, NIT, SDT, MPEG, frequenties\">\n<!-- /tpl:put -->\n\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">\n<link href=\"/theme/Master.css\" rel=\"stylesheet\" type=\"text/css\">\n<!-- tpl:put name=\"headarea\" -->\n<title>Zenderindeling Ziggo/@Home</title>\n<!-- /tpl:put -->\n</head>\n<body>\n<table class=\"lay\" align=\"center\" border=\"0\" cellpadding=\"0\"\ncellspacing=\"0\">\n<tbody>\n<tr>\n<td colspan=\"2\" class=\"kop\"\nonclick=\"location.href=\'http://www.digitalekabeltelevisie.nl/\';\"\nstyle=\"cursor: pointer;\"><h1 class=\"kop\">digitale\nkabeltelevisie</h1></td>\n</tr>\n<tr>\n<td class=\"links\">\n<!-- tpl:put name=\"linksboven\" --> <!-- /tpl:put --> <!--#exec cgi=\"/cgi-bin/menu.pl\" -->\n<!-- tpl:put name=\"linksmidden\" --> <!-- /tpl:put --> <!--#include virtual=\"/nieuws/laatste.shtml\" -->\n<!-- tpl:put name=\"linksbeneden\" --> <!-- /tpl:put -->\n\n</td>\n<td class=\"main\">\n<h1>\n<!-- tpl:put name=\"titel\" -->\nZenderindeling Ziggo/@Home\n<!-- /tpl:put -->\n</h1> <!-- tpl:put name=\"bodyarea\" -->\n\n<p>\nDe indeling van kanalen zoals geldig in het voormalig @Home gebied (Enschede) van Ziggo op ";

		String tvhome_prefix_2=". Wijzigingen/toevoegingen\nt.o.v. de vorige versie zijn in <span\nstyle=\"background-color: yellow;\">geel</span> aangegeven,\nverwijderde zenders zijn <span\nstyle=\"text-decoration: line-through; background-color: orange;\">oranje\nen doorgestreept</span>. (Soms geeft Ziggo zenders een ander\nservice ID, terwijl ze alleen maar naar een andere stream\nverplaatst zijn. Dan lijkt het in dit overzicht alsof de zender\nverwijderd EN tegelijk nieuw is.)\n</p>\n\n<p>Ziggo gebruikt nog twee gescheiden netwerken, \u00e9\u00e9n voor het\nvoormalige @Home-gebied, en \u00e9\u00e9n voor het gebied van\nCasema/Multikabel. Voor verschillende regios worden 32 streams op\nverschillende frequenties gebruikt. Eerst een lijst met de\nindeling in 32 streams (de nummering is die zoals door Ziggo\ngebruikt). Daarna een lijst met de frequenties van de streams voor\nde verschillende regio\'s.</p>\n<p>Bij type staat het soort zender aangegeven.</p>\n<ul>\n<li>\"digital television service (1)\" is een gewoon standaard\ndefinition (SD) TV kanaal.</li>\n<li>\"digital radio sound service (2)\" is een radiokanaal.</li>\n<li>\"user defined (128)\" is in de DVB specificatie\nvrijgelaten, en wordt gebruikt voor software updates voor de\ndecoders.</li>\n<li>\"reserved for future use (17)\" is HDTV in MPEG2\n(eigenlijk \"MPEG-2 HD digital television service\", in de zomer\nvan 2006 gebruikt voor het WK, en tot mei 2010 voor Discovery HD\nmet een datarate van 20 Mbps voor het beeld. Op dit moment niet\ngebruikt op het Ziggo Netwerk.</li>\n<li>\"reserved for future use (25)\" is eigenlijk \"advanced\ncodec HD digital television service\", HDTV in MPEG4. Nu in\ngebruik voor Sport1 HD, National Geographic Channel HD en Film1\nHD. Deze hebben ongeveer een datarate van 14 Mbps voor het beeld.</li>\n</ul>\n\n<p>\nKlik <a href=\"tvhome";

		String tvhome_prefix_3=".shtml\">hier voor de vorige\nindeling</a>.\n</p>\n<p>\n<script type=\"text/javascript\">\ndocument\n\t.write(\"\\t\\t\\tOnderstaande tabel kan je sorteren door te klikken op de naam van de kolom die je wilt sorteren.\");\n</script>\n</p> <!-- /tpl:put -->\n\n";

		String tvhome_suffix="<p class=\"adsense_align\">\n<script type=\"text/javascript\">\n<!--\ngoogle_ad_client = \"pub-3413460302732065\";\ngoogle_ad_slot = \"0646833741\";\ngoogle_ad_width = 468;\ngoogle_ad_height = 60;\n//-->\n</script>\n<script type=\"text/javascript\"\nsrc=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n\n</script>\n</p> <!--#config timefmt=\"%e/%m/%Y\" -->\n<p class=\"updated\">\nDeze pagina is het laatst aangepast op\n<!--#echo var=\"LAST_MODIFIED\" -->\n</p>\n</td>\n\n</tr>\n</tbody>\n</table>\n</body>\n</html>\n<!-- /tpl:insert -->\n";



		String date_prefix_1="<!-- tpl:insert page=\"/newstemplate.htpl\" -->\n<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head>\n<meta http-equiv=\"content-language\" content=\"nl\">\n<meta name=\"owner\" content=\"info@digitalekabeltelevisie.nl\">\n<meta name=\"author\" content=\"Eric Berendsen\">\n<!-- tpl:put name=\"description\" -->\n\n<script src=\"/theme/sorttable.js\" type=\"text/javascript\"></script>\n<meta name=\"description\"\ncontent=\"Overzicht gebruikte kanalen en frequenties bij Ziggo Oost (voormalig @home gebied).\">\n\n\n\n\n<!-- /tpl:put -->\n<!-- tpl:put name=\"keywords\" -->\n<meta name=\"keywords\"\ncontent=\"digitale televisie, DVB-C, NIT, SDT, MPEG, frequenties\">\n<!-- /tpl:put -->\n\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n<meta http-equiv=\"Content-Style-Type\" content=\"text/css\">\n<link href=\"/theme/Master.css\" rel=\"stylesheet\" type=\"text/css\">\n<!-- tpl:put name=\"headarea\" -->\n<title>Zenderindeling Ziggo/@Home</title>\n<!-- /tpl:put -->\n</head>\n<body>\n<table class=\"lay\" align=\"center\" border=\"0\" cellpadding=\"0\"\ncellspacing=\"0\">\n<tbody>\n<tr>\n<td colspan=\"2\" class=\"kop\"\nonclick=\"location.href=\'http://www.digitalekabeltelevisie.nl/\';\"\nstyle=\"cursor: pointer;\"><h1 class=\"kop\">digitale\nkabeltelevisie</h1></td>\n</tr>\n<tr>\n<td class=\"links\">\n<!-- tpl:put name=\"linksboven\" --> <!-- /tpl:put --> <!--#exec cgi=\"/cgi-bin/menu.pl\" -->\n<!-- tpl:put name=\"linksmidden\" --> <!-- /tpl:put --> <!--#include virtual=\"/nieuws/laatste.shtml\" -->\n<!-- tpl:put name=\"linksbeneden\" --> <!-- /tpl:put -->\n\n</td>\n<td class=\"main\">\n<h1>\n<!-- tpl:put name=\"titel\" -->\nZenderindeling Ziggo/@Home\n<!-- /tpl:put -->\n</h1> <!-- tpl:put name=\"bodyarea\" -->\n\n<p>\nDe indeling van kanalen zoals geldig in het voormalig @Home gebied\n(Enschede) van Ziggo op ";
		String date_prefix_2=". Wijzigingen/toevoegingen t.o.v. de vorige versie\nzijn in <span style=\"background-color: yellow;\">geel</span>\naangegeven, verwijderde zenders zijn <span\nstyle=\"text-decoration: line-through; background-color: orange;\">oranje\nen doorgestreept</span>. (Soms geeft Ziggo zenders onnodig een ander\nservice ID, terwijl ze alleen maar naar een andere stream\nverplaatst zijn. Dan lijkt het in dit overzicht alsof de zender\nverwijderd EN tegelijk nieuw is.)\n</p>\n\n\n<p>\nKlik <a href=\"tvhome";
		String date_prefix_3=".shtml\">hier voor de vorige\nindeling</a>.\n</p>\n<p>\n<script type=\"text/javascript\">\ndocument.write(\"\\t\\t\\tOnderstaande tabel kan je sorteren door te klikken op de naam van de kolom die je wilt sorteren.\");\n</script>\n</p> <!-- /tpl:put -->";
		String date_sufffix="\n<p class=\"adsense_align\">\n<script type=\"text/javascript\">\n<!--\ngoogle_ad_client = \"pub-3413460302732065\";\ngoogle_ad_slot = \"0646833741\";\ngoogle_ad_width = 468;\ngoogle_ad_height = 60;\n//-->\n</script>\n<script type=\"text/javascript\"\nsrc=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n\n</script>\n</p> <!--#config timefmt=\"%e/%m/%Y\" -->\n<p class=\"updated\">\nDeze pagina is het laatst aangepast op\n<!--#echo var=\"LAST_MODIFIED\" -->\n</p>\n</td>\n\n</tr>\n</tbody>\n</table>\n</body>\n</html>\n<!-- /tpl:insert -->\n";


		//find date
		Date newDate= getDate(newTransportStream);
		Date oldDate= getDate(oldTransportStream);

		//newTransportStream.namePIDs();
		System.out.println(newTransportStream);
		String tables = getHTMLTables(newTransportStream,oldTransportStream);

		// actual file
		String filename1=outputDir+"tvhome.shtml";
		FileWriter fstream;
		try {
			fstream = new FileWriter(filename1);
			fstream.write(tvhome_prefix_1);
			fstream.write(new SimpleDateFormat("YYYY-MM-dd").format(newDate));
			fstream.write(tvhome_prefix_2);
			fstream.write(new SimpleDateFormat("YYYYMMdd").format(oldDate));
			fstream.write(tvhome_prefix_3);

			fstream.write(tables);
			fstream.write(tvhome_suffix);

			fstream.close();


		} catch (IOException e1) {

			e1.printStackTrace();
		}


		// old file for archive, same contents but shorter intro

		final String filename="tvhome" +new SimpleDateFormat("YYYYMMdd").format(newDate)+".shtml";
		try {
			fstream = new FileWriter(outputDir+filename);
			fstream.write(date_prefix_1);
			fstream.write(new SimpleDateFormat("YYYY-MM-dd").format(newDate));
			fstream.write(date_prefix_2);
			fstream.write(new SimpleDateFormat("YYYYMMdd").format(oldDate));
			fstream.write(date_prefix_3);

			fstream.write(tables);
			fstream.write(date_sufffix);

			fstream.close();


		} catch (IOException e1) {

			e1.printStackTrace();
		}


		if( !java.awt.Desktop.isDesktopSupported() ) {

			System.err.println( "Desktop is not supported (fatal)" );
			System.exit( 1 );
		}

		final java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

		if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) ) {

			System.err.println( "Desktop doesn't support the browse action (fatal)" );
			System.exit( 1 );
		}


		filename1 = "file:///"+filename1.replaceAll(" ","%20");

		try {

			final java.net.URI uri = new java.net.URI( filename1 );
			desktop.browse( uri );
		}
		catch ( final Exception e ) {

			System.err.println( e.getMessage() );
		}		System.out.println("Klaar!!");


	}

	private String getHTMLTables(final TransportStream newStream,final TransportStream oldStream) {

		final StringWriter out = new StringWriter();
		try {
			//final String filename=newStream.getFile().getName();

			// final FileWriter fstream = new FileWriter("d:\\eric\\diff"+filename+".html");



			out.write("<table class=\"sortable\" border=\"1\" cellpadding=\"0\" cellspacing=\"0\">\n");
			out.write("<thead><tr><th>Transport Stream</th><th>Zender</th><th>Type (TV/Radio) - (RAW)</th><th>Service-ID</th><th>logical channel</th></tr></thead>\n");
			out.write("<tbody>\n");

			final SDT sdt = newStream.getPsi().getSdt();
			final SDT oldSDT = oldStream.getPsi().getSdt();

			final Map<Integer, SDTsection[]> streams = sdt.getTransportStreams();
			final Map<Integer, SDTsection[]> oldStreams = oldSDT.getTransportStreams();
			final TreeSet<Integer> s = new TreeSet<Integer>(streams.keySet());

			for(final Integer transportStreamID: s){
				//out.write("<tr><th colspan=\"4\">"+transportStreamID+"</th></tr>\n");
				final SDTsection [] sections = streams.get(transportStreamID);
				final SDTsection [] oldSDTsections = oldStreams.get(transportStreamID);
				final ArrayList<Service> sdtServiceList = getSortedSdtServices(sections);
				final ArrayList<Service> oldSdtServiceList = getSortedSdtServices(oldSDTsections);


				int currentServiceIndex = 0;
				int oldServiceIndex = 0;
				//while ((currentService<sdtServiceList.size())&&(oldService<oldSdtServiceList.size()))
				for (currentServiceIndex=0; currentServiceIndex < sdtServiceList.size(); currentServiceIndex++) {
					Service element = sdtServiceList.get(currentServiceIndex);
					final int sid=element.getServiceID();
					while((oldServiceIndex<oldSdtServiceList.size())&&(oldSdtServiceList.get(oldServiceIndex).getServiceID()<sid)){
						// oldService does not exist in new SDT in this stream, maybe somewhere else
						Service oldService = oldSdtServiceList.get(oldServiceIndex);
						// is this service somewhere else in the new SDT? If not, it was removed
						if(sdt.getService(oldService.getServiceID())==null){ // it was removed, so print it with strike

							writeService(oldStream, out, oldSDT, transportStreamID, oldService);


						}
						oldServiceIndex++;
					}
					// OLD SERVICE IN SAME STREAM AS new one
					if((oldServiceIndex<oldSdtServiceList.size())&&(oldSdtServiceList.get(oldServiceIndex).getServiceID()==sid)){
						oldServiceIndex++;
					}

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
					// if service moved to other stream only highlight stream ID
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
					// LCN changed ?
					final int oldLCN = oldStream.getPsi().getNit().getLCN(1000, oldSDT.getTransportStreamID(sid), sid);
					String old = "";
					if ((oldService != null) && (lcn != oldLCN)&&(oldLCN!=-1)) {
						style="style=\""+bgColorCSS+"\"";
						old = " [" + oldLCN + "] ";

					}
					out.write("<td style=\"text-align: left;\">" + sid + " </td><td " + style + ">" + lcnString + old
							+ "</td></tr>\n");

				}
				// new Services processed, maybe some old ones with higher sid left
				while(oldServiceIndex<oldSdtServiceList.size()){
					// oldService does not exist in new SDT in this stream, maybe somewhere else
					Service oldService = oldSdtServiceList.get(oldServiceIndex);
					// is this service somewhere else in the new SDT? If not, it was removed
					if(sdt.getService(oldService.getServiceID())==null){ // it was removed, so print it with strike

						writeService(oldStream, out, oldSDT, transportStreamID, oldService);


					}
					oldServiceIndex++;
				}


			}


			out.write("	\n");
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


		} catch (final IOException e) {
			e.printStackTrace();
		}
		return out.toString();
	}

	/**
	 * @param oldStream
	 * @param out
	 * @param oldSDT
	 * @param transportStreamID
	 * @param oldService
	 * @throws IOException
	 */
	private String writeService(final TransportStream oldStream, final StringWriter out, final SDT oldSDT,
			final Integer transportStreamID, Service oldService) throws IOException {


		out.write("<tr style=\""+strikeCSS+"\">");

		out.write("<td style=\"text-align: left;\">\n"+transportStreamID+"</td>");
		final String safeName= Utils.escapeHTML(oldSDT.getServiceName(oldService.getServiceID()));


		out.write("<td style=\"text-align: left;\">\n"+safeName+"</td>\n");
		out.write("<td style=\"text-align: left;\">"+Descriptor.getServiceTypeString(oldSDT.getServiceType(oldService.getServiceID()))+" ("+oldSDT.getServiceType(oldService.getServiceID())+") </td>\n");

		final int lcn = oldStream.getPsi().getNit().getLCN(1000, transportStreamID, oldService.getServiceID());
		//final int lcn = newStream.getPsi().getNit().getLCN(43136, transportStreamID, sid);
		String lcnString = (lcn > 0) ? Integer.toString(lcn) : "-";

		final int hdLCN = oldStream.getPsi().getNit().getHDSimulcastLCN(1000, transportStreamID, oldService.getServiceID());
		final String hdlcnString = (hdLCN > 0) ? (" / " + Integer.toString(hdLCN)) : "";
		lcnString += hdlcnString;

		out.write("<td style=\"text-align: left;\">" + oldService.getServiceID() + " </td><td>" + lcnString
				+ "</td></tr>\n");

		return out.toString();
	}

	/**
	 * @param sections
	 * @return
	 */
	private ArrayList<Service> getSortedSdtServices(final SDTsection[] sections) {
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
		return serviceList;
	}


	public static Date getDate(final TransportStream transportStream){

		Date newDate=null;
		TDT tdt = transportStream.getPsi().getTdt();
		if(tdt!=null){
			List<TDTsection> l = tdt.getTdtSectionList();
			if(!l.isEmpty()){
				TDTsection e = l.get(0);
				newDate = Utils.getUTCDate(e.getUTC_time());
			}
		}
		// try tot
		if(newDate==null){
			TOT tot = transportStream.getPsi().getTot();
			if(tot!=null){
				List<TOTsection> l = tot.getTotSectionList();
				if(!l.isEmpty()){
					TOTsection e = l.get(0);
					newDate = Utils.getUTCDate(e.getUTC_time());
				}
			}
		}
		// try file date
		if(newDate==null){
			newDate = new Date(transportStream.getFile().lastModified());
		}
		return newDate;
	}


	public static String stripLeadingZero(final String s){
		String f = s;
		while((f.length()>1)&&(f.charAt(0)=='0')&&(f.charAt(1)!='.')){
			f=f.substring(1);
		}
		return f;
	}




}


