package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.toHexString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

/**
 * Page holds a list of subPages. For normal teletext (non subtitle) this is a treemap. 
 * If only one subpage exists with subPageNo==0, it is treated special
 * For subtitle pages all versions are stored in an ArrayList.
 */
public class Page implements TreeNode{

	/**
	 *
	 */
	private final Magazine	magazineHandler;
	private final TreeMap<Integer, SubPage>subPages = new TreeMap<>();
	private final List<SubPage> subtitles = new ArrayList<>();

	private int pageNo=-1;
	private SubPage currentSubPage=null;
	
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
		int currentSubPageNo = txtDataField.getSubPage();
		if((txtDataField.getDataUnitId()==0x02)|| (txtDataField.getDataUnitId()== 0xc0)){ //EBU Teletext non-subtitle data , or inverted
			currentSubPage=subPages.get(currentSubPageNo);
			if(currentSubPage==null){
				currentSubPage=new SubPage(this, currentSubPageNo);
				subPages.put(currentSubPageNo, currentSubPage);
			}
			currentSubPage.setHeader(txtDataField);
		}else if(txtDataField.getDataUnitId()==0x03){ //EBU Teletext subtitle data // TODO ??check also for subtitle flag, because spanish TVE uses DataUnitId()==03 on all pages.... && txtDataField.isSubtitle()
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
		DefaultMutableTreeNode treeNode;
		if((subPages.size()==1)&&(subPages.get(0) != null)){ //only one subPage, and its number is 0000
			treeNode = subPages.get(0).getJTreeNode(modus);
			((KVP) treeNode.getUserObject()).setLabel(getPageNumberLabel());
		}else if(subPages.size()>0){ // normal list of subpages
			treeNode=new DefaultMutableTreeNode(new KVP(getPageNumberLabel()));
			for (SubPage subPage : subPages.values()) {
				treeNode.add(subPage.getJTreeNode(modus));
			}
		}else if(!subtitles.isEmpty()){
			treeNode=new DefaultMutableTreeNode(new KVP(getPageNumberLabel()));
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

					t.setUserObject(new KVP(titel.toString().trim()).addImageSource(subPage, titel.toString().trim()));
					treeNode.add(t);
				}
			}
		}else{
			treeNode=new DefaultMutableTreeNode(new KVP(getPageNumberLabel()));
		}
		return treeNode;
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
		if((p!=null)&&!p.subPages.isEmpty()){// always hashMap, maybe no entries ?
			return p.subPages.values().iterator().next();
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
		final Set<Integer> subPageNos = subPages.keySet();
		for(final Integer subNo:subPageNos){
			if(subPageS1==(subNo&0xF)){ // last hex digit matches
				return subPages.get(subNo);
			}
		}
		return null;
	}

	@Override
	public String toString(){
		return "Mag:"+getMagazineNo()+", pageNo"+getPageNo();
	}

	public SortedMap<Integer, SubPage> getSubPages() {
		return subPages;
	}


}