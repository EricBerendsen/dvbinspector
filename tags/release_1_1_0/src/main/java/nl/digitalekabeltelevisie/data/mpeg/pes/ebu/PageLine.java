package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import nl.digitalekabeltelevisie.controller.TreeNode;

/**
 * PageLine is a TxtDataField in the context of a page, so it can access stuff like National Option Character Subset, CLUTs, etc. from the page,magazine or service.
 * a TxtDataField can live inside a single PESpacket,
 */
public class PageLine extends TxtDataField implements TreeNode{


	/**
	 * 
	 */
	private final SubPage	pageHandler;


	public PageLine(final SubPage page, final TxtDataField l) {
		super(l.getData_block(), l.getOffset(), l.getLen(), l.getPts());
		pageHandler = page;

	}

	@Override
	protected String getTeletextHTML(final byte[] b) {
		String bg="black";
		String fg="white";
		final StringBuilder buf = new StringBuilder("<code><b><span style=\"background-color: black; color: white; \">");
		for (int i = 0; i < b.length; i++) {
			final byte ch = b[i];
			if(ch==0x20){ //nbsp
				buf.append("&nbsp;");
			}else if(ch==0x3c){ //<<
				buf.append("&lt;");
			}else if(ch==0x26){ //&
				buf.append("&amp;");
			}else if((ch>32)&&(ch<127)){  // national option charset subset
				final int nocs= pageHandler.getNationalOptionCharSubset();
				// all chars 0x20..7F
				switch (ch)  // special national characters
				{
				case 0x23:
					buf.append((char)TxtTriplet.national_subsets[nocs][0]);
					break;
				case 0x24:
					buf.append((char)TxtTriplet.national_subsets[nocs][1]);
					break;
				case 0x40:
					buf.append((char)TxtTriplet.national_subsets[nocs][2]);
					break;
				case 0x5b:
					buf.append((char)TxtTriplet.national_subsets[nocs][3]);
					break;
				case 0x5c:
					buf.append((char)TxtTriplet.national_subsets[nocs][4]);
					break;
				case 0x5d:
					buf.append((char)TxtTriplet.national_subsets[nocs][5]);
					break;
				case 0x5e:
					buf.append((char)TxtTriplet.national_subsets[nocs][6]);
					break;
				case 0x5f:
					buf.append((char)TxtTriplet.national_subsets[nocs][7]);
					break;
				case 0x60:
					buf.append((char)TxtTriplet.national_subsets[nocs][8]);
					break;
				case 0x7b:
					buf.append((char)TxtTriplet.national_subsets[nocs][9]);
					break;
				case 0x7c:
					buf.append((char)TxtTriplet.national_subsets[nocs][10]);
					break;
				case 0x7d:
					buf.append((char)TxtTriplet.national_subsets[nocs][11]);
					break;
				case 0x7e:
					buf.append((char)TxtTriplet.national_subsets[nocs][12]);
					break;
				case 0x7f:
					buf.append(0x25a0);
					break;
				default:
					buf.append((char)ch);
				}

			}else if((ch>=0)&&(ch<=7)){ //12.2 Spacing attributes
				fg=getHTMLColorString(ch);
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else if((ch>=0x10)&&(ch<=0x17)){ //12.2 Spacing attributes Mosaic Colour Codes ("Set-After")
				fg=getHTMLColorString(ch-0x10);
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else if(ch==0x1c){ //Black Background ("Set-At")
				bg=getHTMLColorString(0);
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else if(ch==0x1d){ //1New Background ("Set-At")
				bg=fg;
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else{  // not implemented, empty space
				buf.append("&nbsp;");
			}
		}
		buf.append("</span></b></code>");
		return buf.toString();
	}
}