package nl.digitalekabeltelevisie.gui.xmleditorkit;

// code from http://java-sl.com/xml_editor_kit.html by Stanislav Lapitsky.
// code free to use/modify, provided reference to Stanislav Lapitsky is given in the used code snippets.

import javax.swing.text.*;

class XMLViewFactory implements ViewFactory {
    public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new LabelView(elem);
            }
            else if (kind.equals(XMLDocument.TAG_ELEMENT)) {
                return new TagView(elem);
            }
            else if (kind.equals(XMLDocument.TAG_ROW_START_ELEMENT) ||
                    kind.equals(XMLDocument.TAG_ROW_END_ELEMENT)) {
                return new BoxView(elem, View.X_AXIS) {
                    public float getAlignment(int axis) {
                        return 0;
                    }
                    public float getMaximumSpan(int axis) {
                        return getPreferredSpan(axis);
                    }
                };
            }
            else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            }
            else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            }
            else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }

        // default to text display
        return new LabelView(elem);
    }
}
