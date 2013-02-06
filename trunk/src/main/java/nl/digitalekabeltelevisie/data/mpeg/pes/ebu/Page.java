package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.toHexString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

/**
 * Page holds a listof subPages. For normal teletext (non subtitle) this is a hashmap. If only one subpage exitst with subPageNo==0, it is treated specia+
 * For subtitle pages all versions are stored in an ArrayList.
 */
public class Page implements TreeNode{

	/**
	 *
	 */
	private final Magazine	magazineHandler;
	private final Map<Integer, SubPage>subPages = new HashMap<Integer, SubPage>();
	private final List<SubPage> subtitles = new ArrayList<SubPage>();

	private int pageNo=-1;
	private int currentSubPageNo=-1;
	private SubPage currentSubPage=null;

	//private TxtDataField[][] linesList = new TxtDataField[1][32]; //subPages, lines
	/**
	 * @param currentPageNo
	 * @param magazine
	 */
	public Page(final Magazine magazine, final int currentPageNo) {
		magazineHandler = magazine;
		pageNo = currentPageNo;
	}
	/**
	 * @param txtDataField
	 */
	public void addLine(final TxtDataField txtDataField) {
		if(currentSubPage!=null){
			currentSubPage.addLine(txtDataField);
		}
	}
	/**
	 * @param txtDataField
	 */
	public void setHeader(final TxtDataField txtDataField) {
		currentSubPageNo = txtDataField.getSubPage();
		if((txtDataField.getDataUnitId()==0x02)|| (txtDataField.getDataUnitId()== 0xc0)){ //EBU Teletext non-subtitle data , or inverted
			currentSubPage=getSubPages().get(currentSubPageNo);
			if(currentSubPage==null){
				currentSubPage=new SubPage(this, currentSubPageNo);
				getSubPages().put(currentSubPageNo, currentSubPage);
			}
			currentSubPage.setHeader(txtDataField);
		}else if(txtDataField.getDataUnitId()==03){ //EBU Teletext subtitle data // TODO ??check also for subtitle flag, because spanish TVE uses DataUnitId()==03 on all pages.... && txtDataField.isSubtitle()
			// But then normal pages of TVE text are not shown at all...
			currentSubPage=new SubPage(this, currentSubPageNo);
			subtitles.add(currentSubPage);
			currentSubPage.setHeader(txtDataField);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		DefaultMutableTreeNode s;
		if((getSubPages().size()==1)&&(getSubPages().keySet().iterator().next()==0)){ //only one subPage, and its number is 0000
			final SubPage p = getSubPages().values().iterator().next();
			s = p.getJTreeNode(modus);
			s.setUserObject(new KVP(getPageNumberLabel(),p));
		}else if(getSubPages().size()>0){ // normal list of subpages
			s=new DefaultMutableTreeNode(new KVP(getPageNumberLabel()));
			final SortedSet<Integer> sortedSubPages = new TreeSet<Integer>(getSubPages().keySet());
			for (Integer integer : sortedSubPages) {
				final int subPage= integer;
				final SubPage p = getSubPages().get(subPage);
				s.add(p.getJTreeNode(modus));
			}
		}else if(subtitles.size()>0){
			s=new DefaultMutableTreeNode(new KVP(getPageNumberLabel()));
			for(final SubPage subPage: subtitles){
				if(subPage!=null){
					final DefaultMutableTreeNode t = subPage.getJTreeNode(modus);
					final StringBuilder titel = new StringBuilder("subtitle ");
					for (int i = 1; i < subPage.getLinesList().length; i++) { // skip the header
						final TxtDataField line = subPage.getLinesList()[i];
						if(line!=null){
							titel.append(line.getTeletextPlain().trim()).append(' ');
						}
					}

					t.setUserObject(new KVP(titel.toString().trim(),subPage));
					s.add(t);
				}
			}
		}else{
			s=new DefaultMutableTreeNode(new KVP(getPageNumberLabel()));
		}
		return s;
	}

	private String getPageNumberLabel(){
		final StringBuilder b = new StringBuilder("Page ");
		b.append(toHexString(pageNo,2));
		if((pageNo==0xBE)&&(currentSubPage.linesList[0]!=null)&&(currentSubPage.linesList[0].getMagazineNo()==1)){
			b.append(" [Automatic Channel Installation (ACI)]");
		}
		if((pageNo==0xDF)&&(currentSubPage.linesList[0]!=null)&&(currentSubPage.linesList[0].getMagazineNo()==1)){
			b.append(" [Data broadcasting page carrying EPG data as defined in ETSI EN 300 707]");
		}

		if((pageNo==0xE7)&&(currentSubPage.linesList[0]!=null)&&(currentSubPage.linesList[0].getMagazineNo()==1)){
			b.append(" [Page carrying trigger messages as defined and coded in IEC/PAS 62297 Edition 1.0 (2002-01): Proposal for introducing a trigger mechanism into TV transmissions]");
		}

		if((pageNo>=0xF1)&&(pageNo<=0xF4)&&(currentSubPage.linesList[0]!=null)&&(currentSubPage.linesList[0].getMagazineNo()==1)){
			b.append(" [TOP data pages: Additional Information Table (AIT), Multi-Page Table (MPT) and Multi-Page Extension Table (MPT-EX)]");
		}

		if(isBasicTOPTable()){
			b.append(" [Basic TOP Table (BTT)]");
		}
		if(pageNo==0xFD){
			b.append(" [Magazine Inventory Page (MIP)]");
		}
		if(isMOTTable()){
			b.append(" [Magazine Organization Table (MOT)]");
		}
		if(pageNo==0xFF){
			b.append(" [time filling and terminator]");
		}
		return b.toString();
	}
	/**
	 * @return
	 */
	public boolean isMOTTable() {
		return pageNo==0xFE;
	}
	/**
	 * @return
	 */
	private boolean isBasicTOPTable() {
		return (pageNo==0xF0)&&(currentSubPage.linesList[0]!=null)&&(currentSubPage.linesList[0].getMagazineNo()==1);
	}
	/**
	 * @return the pageNo
	 */
	public int getPageNo() {
		return pageNo;
	}
	/**
	 * @param pageNo the pageNo to set
	 */
	public void setPageNo(final int pageNo) {
		this.pageNo = pageNo;
	}
	/**
	 * @return
	 */
	public SubPage getMOTPage() {
		final Page p = magazineHandler.getPage(0xFE);
		if((p!=null)&&!p.getSubPages().isEmpty()){// always hashMap, maybe no entries ?
			return p.getSubPages().values().iterator().next();
		}
		return null;
	}

	public int getMagazineNo() {
		return magazineHandler.getMagazineNo();
	}

	/**
	 * @return the magazineHandler
	 */
	public Magazine getMagazine() {
		return magazineHandler;
	}
	/**
	 * @return the magazineHandler
	 */
	public Magazine getMagazine(final int m) {
		return magazineHandler.getTxtService().getMagazine(m);
	}
	/**
	 * @param subPageS1
	 * @return
	 */
	public SubPage getSubPageByS1(final int subPageS1) {
		final Set<Integer> subPageNos = getSubPages().keySet();
		for(final Integer subNo:subPageNos){
			if(subPageS1==(subNo&0xF)){ // last hex digit matches
				return getSubPages().get(subNo);
			}
		}
		return null;
	}

	@Override
	public String toString(){
		return "Mag:"+getMagazineNo()+", pageNo"+getPageNo();
	}

	public Map<Integer, SubPage> getSubPages() {
		return subPages;
	}


}