package nl.digitalekabeltelevisie.gui.xmleditorkit;

//code from http://java-sl.com/xml_editor_kit.html by Stanislav Lapitsky.
//code free to use/modify, provided reference to Stanislav Lapitsky is given in the used code snippets.

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class XMLDocument extends DefaultStyledDocument {
    public static String TAG_ELEMENT="tag_element";
    public static String TAG_ROW_START_ELEMENT="tag_row_start_element";
    public static String TAG_ROW_END_ELEMENT="tag_row_end_element";

    public static SimpleAttributeSet BRACKET_ATTRIBUTES=new SimpleAttributeSet();
    public static SimpleAttributeSet TAGNAME_ATTRIBUTES=new SimpleAttributeSet();
    public static SimpleAttributeSet ATTRIBUTENAME_ATTRIBUTES=new SimpleAttributeSet();
    public static SimpleAttributeSet ATTRIBUTEVALUE_ATTRIBUTES=new SimpleAttributeSet();
    public static SimpleAttributeSet PLAIN_ATTRIBUTES=new SimpleAttributeSet();
    public static SimpleAttributeSet COMMENT_ATTRIBUTES=new SimpleAttributeSet();
    static {
        StyleConstants.setBold(TAGNAME_ATTRIBUTES, true);
        StyleConstants.setForeground(TAGNAME_ATTRIBUTES, Color.GREEN.darker());

        StyleConstants.setBold(ATTRIBUTENAME_ATTRIBUTES, true);

        StyleConstants.setItalic(ATTRIBUTEVALUE_ATTRIBUTES, true);
        StyleConstants.setForeground(ATTRIBUTEVALUE_ATTRIBUTES, Color.BLUE);

        StyleConstants.setFontSize(PLAIN_ATTRIBUTES, StyleConstants.getFontSize(PLAIN_ATTRIBUTES)-1);
        StyleConstants.setForeground(PLAIN_ATTRIBUTES, Color.DARK_GRAY);

        StyleConstants.setFontSize(COMMENT_ATTRIBUTES, StyleConstants.getFontSize(COMMENT_ATTRIBUTES)-1);
        StyleConstants.setForeground(COMMENT_ATTRIBUTES, Color.GRAY);
        StyleConstants.setItalic(COMMENT_ATTRIBUTES, true);
    }
    
    private boolean isUserChanges=true;
    
    public XMLDocument() {

    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (!isUserChanges()) {
            super.insertString(offs, str, a);
        }
    }

    public void remove(int offs, int len) throws BadLocationException {
        if (!isUserChanges()) {
            super.remove(offs, len);
        }
    }

    public boolean isUserChanges() {
        return isUserChanges;
    }

    public void setUserChanges(boolean userChanges) {
        isUserChanges = userChanges;
    }
    
    protected void insert(int offset, ElementSpec[] data) throws BadLocationException {
        super.insert(offset, data);
    }

}
