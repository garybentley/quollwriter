package com.quollwriter.ui.components;

import java.awt.*;

import javax.swing.event.*;
import javax.swing.text.*;

// Taken from: http://stackoverflow.com/questions/11000220/strange-text-wrapping-with-styled-text-in-jtextpane-with-java-7
// It is designed to solve the reflow bug that breaks words up incorrectly when the width of the
// text pane changes.
public class QStyledEditorKit extends StyledEditorKit
{

    private MyFactory factory;

    public ViewFactory getViewFactory() {
        if (factory == null) {
            factory = new MyFactory();
        }
        return factory;
    }

    class MyFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new MyLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new MyParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
    
            // default to text display
            return new LabelView(elem);
        }
    }
    
    class MyParagraphView extends ParagraphView {
    
        public MyParagraphView(Element elem) {
            super(elem);
        }
    public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.removeUpdate(e, a, f);
        resetBreakSpots();
    }
    public void insertUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.insertUpdate(e, a, f);
        resetBreakSpots();
    }
    
    private void resetBreakSpots() {
        for (int i=0; i<layoutPool.getViewCount(); i++) {
            View v=layoutPool.getView(i);
            if (v instanceof MyLabelView) {
                ((MyLabelView)v).resetBreakSpots();
            }
        }
    }
    
    }
    
    class MyLabelView extends LabelView {
    
        boolean isResetBreakSpots=false;
    
        public MyLabelView(Element elem) {
            super(elem);
        }
        public View breakView(int axis, int p0, float pos, float len) {
            if (axis == View.X_AXIS) {
                resetBreakSpots();
            }
            return super.breakView(axis, p0, pos, len);
        }
    
        public void resetBreakSpots() {
            isResetBreakSpots=true;
            removeUpdate(null, null, null);
            isResetBreakSpots=false;
       }
    
        public void removeUpdate(DocumentEvent e, Shape a, ViewFactory f) {
            super.removeUpdate(e, a, f);
        }
    
        public void preferenceChanged(View child, boolean width, boolean height) {
            if (!isResetBreakSpots) {
                super.preferenceChanged(child, width, height);
            }
        }
    }

}