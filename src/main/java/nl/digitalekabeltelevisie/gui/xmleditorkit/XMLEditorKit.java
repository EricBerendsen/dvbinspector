package nl.digitalekabeltelevisie.gui.xmleditorkit;

//code from http://java-sl.com/xml_editor_kit.html by Stanislav Lapitsky.
//code free to use/modify, provided reference to Stanislav Lapitsky is given in the used code snippets.

import javax.swing.text.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;

public class XMLEditorKit extends StyledEditorKit {

	ViewFactory defaultFactory = new XMLViewFactory();

	public ViewFactory getViewFactory() {
		return defaultFactory;
	}

    public Document createDefaultDocument() {
        return new XMLDocument();
    }

    public String getContentType() {
        return "text/xml";
    }

    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        BufferedReader br=new BufferedReader(in);
        String s=br.readLine();
        StringBuffer buff=new StringBuffer();
        while (s!=null) {
            buff.append(s);
            s=br.readLine();
        }

        int p=getInsertPosition(pos, doc);
        XMLReader.getInstance().read(new ByteArrayInputStream(buff.toString().getBytes()), doc, p);
    }
    
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        int p=getInsertPosition(pos, doc);
        XMLReader.getInstance().read(in, doc, p);
    }

    public void write(OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException {
        int[] sel=new int[2];
        sel[0]=pos;
        sel[1]=pos+len;
        correctSelectionBounds(sel, doc);
        pos=sel[0];
        len=sel[1]-pos;
        super.write(out, doc, pos, len);
    }

    public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
        int[] sel=new int[2];
        sel[0]=pos;
        sel[1]=pos+len;
        correctSelectionBounds(sel, doc);
        pos=sel[0];
        len=sel[1]-pos;
        super.write(out, doc, pos, len);
    }

    public static void correctSelectionBounds(int[] selection, Document d) {
        if (d instanceof XMLDocument && d.getLength()>0) {
            XMLDocument doc=(XMLDocument)d;
            int start=selection[0];
            Element root=doc.getDefaultRootElement();
            int i=root.getElementIndex(start);
            while (i>=0 && root.getElement(i).getName().equals(XMLDocument.TAG_ELEMENT)) {
                root=root.getElement(i);
                i=root.getElementIndex(start);
            }

            Element startTag=root;

            int end=selection[0];
            root=doc.getDefaultRootElement();
            i=root.getElementIndex(end);
            while (i>=0 && root.getElement(i).getName().equals(XMLDocument.TAG_ELEMENT)) {
                root=root.getElement(i);
                i=root.getElementIndex(end);
            }

            Element endTag=root;
            Element commonParent=startTag;
            while (commonParent!=null &&
                    !(commonParent.getStartOffset()<=endTag.getStartOffset() &&
                     commonParent.getEndOffset()>=endTag.getEndOffset()) ) {
                commonParent=commonParent.getParentElement();
            }

            if (commonParent!=null) {
                selection[0]=commonParent.getStartOffset();
                selection[1]=commonParent.getEndOffset();
            }
        }
    }

    protected int getInsertPosition(int pos, Document d) {
        if (d instanceof XMLDocument && d.getLength()>0) {
            XMLDocument doc=(XMLDocument)d;
            Element root=doc.getDefaultRootElement();
            int i=root.getElementIndex(pos);
            while (i>=0 && root.getElement(i).getName().equals(XMLDocument.TAG_ELEMENT)) {
                root=root.getElement(i);
                i=root.getElementIndex(pos);
            }

            while (root.getElementCount()<3) {
                root=root.getParentElement();
            }
            return root.getElement(0).getEndOffset();
        }

        return pos;
    }

    MouseListener lstCollapse=new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            JEditorPane src=(JEditorPane)e.getSource();

            int pos=src.viewToModel(e.getPoint());
            View v=src.getUI().getRootView(src);
            while (v!=null && !(v instanceof TagView)) {
                int i=v.getViewIndex(pos, Position.Bias.Forward);
                v=v.getView(i);
            }
            TagView deepest=(TagView)v;
            while (v!=null && v instanceof TagView) {
                deepest=(TagView)v;
                int i=v.getViewIndex(pos, Position.Bias.Forward);
                v=v.getView(i);
            }

            if (deepest!=null) {
                Shape a=getAllocation(deepest, src);
                if (a!=null) {
                    Rectangle r=a instanceof Rectangle ? (Rectangle)a : a.getBounds();
                    r.y+=TagView.AREA_SHIFT/2;
                    r.width=TagView.AREA_SHIFT;
                    r.height=TagView.AREA_SHIFT;

                    if (r.contains(e.getPoint())) {
                        deepest.setExpanded(!deepest.isExpanded());

                        XMLDocument doc= (XMLDocument)src.getDocument();
                        try {
                            doc.setUserChanges(false);
                            pos++;
                            doc.insertString(pos, "\n", new SimpleAttributeSet());
                            doc.remove(pos,1);
                            doc.setUserChanges(true);
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    Cursor oldCursor;
    MouseMotionListener lstMoveCollapse=new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
            JEditorPane src=(JEditorPane)e.getSource();
            if (oldCursor==null) {
                oldCursor=src.getCursor();
            }

            int pos=src.viewToModel(e.getPoint());
            View v=src.getUI().getRootView(src);
            while (v!=null && !(v instanceof TagView)) {
                int i=v.getViewIndex(pos, Position.Bias.Forward);
                v=v.getView(i);
            }
            TagView deepest=(TagView)v;
            while (v!=null && v instanceof TagView) {
                deepest=(TagView)v;
                int i=v.getViewIndex(pos, Position.Bias.Forward);
                v=v.getView(i);
            }

            if (deepest!=null) {
                Shape a=getAllocation(deepest, src);
                if (a!=null) {
                    Rectangle r=a instanceof Rectangle ? (Rectangle)a : a.getBounds();
                    r.y+=TagView.AREA_SHIFT/2;
                    r.width=TagView.AREA_SHIFT;
                    r.height=TagView.AREA_SHIFT;

                    if (r.contains(e.getPoint())) {
                        src.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
            }

            src.setCursor(oldCursor);
        }
    };

    public void install(JEditorPane c) {
        super.install(c);
        c.addMouseListener(lstCollapse);
        c.addMouseMotionListener(lstMoveCollapse);
    }
    
    public void deinstall(JEditorPane c) {
        c.removeMouseListener(lstCollapse);
        c.removeMouseMotionListener(lstMoveCollapse);
        super.deinstall(c);
    }

    protected static Shape getAllocation(View v, JEditorPane edit) {
        Insets ins=edit.getInsets();
        View vParent=v.getParent();
        int x=ins.left;
        int y=ins.top;
        while(vParent!=null) {
            int i=vParent.getViewIndex(v.getStartOffset(), Position.Bias.Forward);
            Shape alloc=vParent.getChildAllocation(i, new Rectangle(0,0, Short.MAX_VALUE, Short.MAX_VALUE));
            x+=alloc.getBounds().x;
            y+=alloc.getBounds().y;

            vParent=vParent.getParent();
        }

        if (v instanceof BoxView) {
            int ind=v.getParent().getViewIndex(v.getStartOffset(), Position.Bias.Forward);
            Rectangle r2=v.getParent().getChildAllocation(ind, new Rectangle(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE)).getBounds();

            return new Rectangle(x,y, r2.width, r2.height);
        }

        return new Rectangle(x,y, (int)v.getPreferredSpan(View.X_AXIS), (int)v.getPreferredSpan(View.Y_AXIS));
    }

    public Action[] getActions() {
        Action[] res=super.getActions();
        for (int i=0; i<res.length; i++) {
            if (res[i] instanceof CopyAction) {
                res[i]=new XMLCopyAction();
            }
        }
        return res;
    }

    public class XMLCopyAction extends TextAction {

        /** Create this object with the appropriate identifier. */
        public XMLCopyAction() {
            super(copyAction);
        }

        /**
         * The operation to perform when this action is triggered.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
            JTextComponent target = getTextComponent(e);
            if (target != null) {
                //adapt selection
                int start=target.getSelectionStart();
                int end=target.getSelectionEnd();
                if (start!=end) {
                    int[] sel=new int[2];
                    sel[0]=start;
                    sel[1]=end;
                    XMLEditorKit.correctSelectionBounds(sel, target.getDocument());
                    target.setSelectionStart(sel[0]);
                    target.setSelectionEnd(sel[1]);
                }
                target.copy();
            }
        }
    }
}
