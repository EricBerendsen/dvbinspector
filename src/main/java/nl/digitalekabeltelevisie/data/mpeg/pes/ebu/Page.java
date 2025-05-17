package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.toHexString;

import java.util.*;

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

	private int pageNo;
	private SubPage currentSubPage;

	/**
	 * @param currentPageNo
	 * @param magazine
	 */
	public Page(Magazine magazine, int currentPageNo) {
		magazineHandler = magazine;
		pageNo = currentPageNo;
	}
	/**
	 * @param txtDataField
	 */
	public void addLine(TxtDataField txtDataField) {
		if(currentSubPage!=null){
			currentSubPage.addLine(txtDataField);
		}
	}
	/**
	 * @param txtDataField
	 */
	public void setHeader(TxtDataField txtDataField) {
		int currentSubPageNo = txtDataField.getSubPage();
		if((txtDataField.getDataUnitId()==0x02)|| // EBU Teletext non-subtitle data
				(txtDataField.getDataUnitId()==0x03 && !txtDataField.isSubtitle()) || // EBU Teletext subtitle data AND not subtitle :-)
				(txtDataField.getDataUnitId()== 0xc0)){ //Inverted Teletext
            currentSubPage = subPages.computeIfAbsent(currentSubPageNo, n -> new SubPage(this, n));
            currentSubPage.setHeader(txtDataField);
		}else if(txtDataField.getDataUnitId()==0x03 && txtDataField.isSubtitle()){ //EBU Teletext subtitle data AND isSubtitle()
			currentSubPage=new SubPage(this, currentSubPageNo);
			subtitles.add(currentSubPage);
			currentSubPage.setHeader(txtDataField);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public KVP getJTreeNode(int modus) {

		if((subPages.size()==1)&&(subPages.get(0) != null)){ //only one subPage, and its number is 0000
			return subPages.get(0).getJTreeNode(modus).setLabel(getPageNumberLabel());
		}
		if(!subPages.isEmpty()) { // normal list of subpages
			KVP kvp = new KVP(getPageNumberLabel());
			for (SubPage subPage : subPages.values()) {
				kvp.add(subPage.getJTreeNode(modus));
			}
			return kvp;
		}
		if(!subtitles.isEmpty()){
			KVP treeNode = new KVP(getPageNumberLabel());
			for(SubPage subPage: subtitles){
				if(subPage!=null){
					KVP t = subPage.getJTreeNode(modus);
					StringBuilder titel = new StringBuilder("subtitle ");
					// skip the header
					TxtDataField[] linesList = Arrays.copyOfRange(subPage.getLinesList(), 1, subPage.getLinesList().length);
					for (TxtDataField line: linesList) {
						if(line!=null){
							titel.append(line.getTeletextPlain().trim()).append(' ');
						}
					}
					String subTitle = titel.toString().trim();
					t.setLabel(subTitle);
					//t.addImageSource(subPage, subTitle);
					treeNode.add(t);
				}
			}
			return treeNode;
		}
		return new KVP(getPageNumberLabel());
	}

	private String getPageNumberLabel(){
		StringBuilder b = new StringBuilder("Page ");
		b.append(toHexString(pageNo, 2));
		if ((pageNo == 0xBE) && isMagazine1()) {
			b.append(" [Automatic Channel Installation (ACI)]");
		}
		if ((pageNo == 0xDF) && isMagazine1()) {
			b.append(" [Data broadcasting page carrying EPG data as defined in ETSI EN 300 707]");
		}

		if ((pageNo == 0xE7) && isMagazine1()) {
			b.append(
					" [Page carrying trigger messages as defined and coded in IEC/PAS 62297 Edition 1.0 (2002-01): Proposal for introducing a trigger mechanism into TV transmissions]");
		}

		if ((pageNo >= 0xF1) && (pageNo <= 0xF4) && isMagazine1()) {
			b.append(" [TOP data pages: Additional Information Table (AIT), Multi-Page Table (MPT) and Multi-Page Extension Table (MPT-EX)]");
		}

		if (isBasicTOPTable()) {
			b.append(" [Basic TOP Table (BTT)]");
		}
		if (pageNo == 0xFD) {
			b.append(" [Magazine Inventory Page (MIP)]");
		}
		if (isMOTTable()) {
			b.append(" [Magazine Organization Table (MOT)]");
		}
		if (pageNo == 0xFF) {
			b.append(" [time filling and terminator]");
		}
		return b.toString();
	}
	
	private boolean isMagazine1() {
		return (currentSubPage.linesList[0] != null) && (currentSubPage.linesList[0].getMagazineNo() == 1);
	}

	/**
	 * @return
	 */
	public boolean isMOTTable() {
		return pageNo == 0xFE;
	}

	/**
	 * @return
	 */
	private boolean isBasicTOPTable() {
		return (pageNo == 0xF0) && isMagazine1();
	}

	/**
	 * @return the pageNo
	 */
	public int getPageNo() {
		return pageNo;
	}

	/**
	 * @return
	 */
	public SubPage getMOTPage() {
		Page p = magazineHandler.getPage(0xFE);
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
	public Magazine getMagazine(int m) {
		return magazineHandler.getTxtService().getMagazine(m);
	}
	/**
	 * @param subPageS1
	 * @return
	 */
	public SubPage getSubPageByS1(int subPageS1) {
        for(Map.Entry<Integer, SubPage> entry : subPages.entrySet()){
			if(subPageS1==(entry.getKey() &0xF)){ // last hex digit matches
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public String toString(){
		return "Mag:"+getMagazineNo()+", pageNo"+ pageNo;
	}

	public SortedMap<Integer, SubPage> getSubPages() {
		return subPages;
	}


}