package com.quollwriter.ui;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

import java.util.concurrent.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.event.*;
import javax.swing.*;
import javax.swing.text.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.events.*;

public abstract class LanguageStringsIdBox<E extends Value, T extends Object> extends Box
{

    protected LanguageStringsIdsPanel panel = null;
    protected E baseValue = null;
    protected E stringsValue = null;
    private Box errorsWrapper = null;
    private JLabel errorsLabel = null;
    private JTextPane errors = null;

    public LanguageStringsIdBox (final E                       baseValue,
                                 final E                       stringsValue,
                                 final LanguageStringsIdsPanel panel)
    {

        super (BoxLayout.Y_AXIS);

        final LanguageStringsIdBox _this = this;

        this.panel = panel;
        this.baseValue = baseValue;
        this.stringsValue = stringsValue;

    }

    public void init ()
    {

        final String fid = BaseStrings.toId (this.baseValue.getId ());

        final LanguageStringsIdBox _this = this;

        // Needed to prevent the performance hit
        this.errorsWrapper = new Box (BoxLayout.Y_AXIS);

        this.errorsLabel = UIUtils.createErrorLabel ("Errors");
        this.errorsLabel.setBorder (UIUtils.createPadding (0, 0, 0, 0));
        this.errorsLabel.setVisible (false);
        this.errorsLabel.setIcon (null);
        this.errorsLabel.setFocusable (false);

        final JTextField h = new JTextField (fid)
        {

            @Override
            public void copy ()
            {

                String sel = this.getSelectedText ();

                if ((sel == null)
                    ||
                    (sel.equals (""))
                   )
                {

                    sel = fid;

                }

                if (sel.equals (fid))
                {

                    sel = BaseStrings.ID_REF_START + fid + BaseStrings.ID_REF_END;

                }

                StringSelection stringSelection = new StringSelection (sel);
                Clipboard clipboard = Toolkit.getDefaultToolkit ().getSystemClipboard ();
                clipboard.setContents (stringSelection, null);

            }

        };

        h.setEditable (false);
        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
        h.setForeground (UIUtils.getTitleColor ());
        h.setBackground (null);
        h.setBackground (UIUtils.getComponentColor ());
        h.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));

        h.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {

                JMenuItem mi = this.createMenuItem ("Find all references",
                                                    Constants.FIND_ICON_NAME,
                                                    "find");

                mi.addActionListener (new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.getEditor ().showFind (BaseStrings.toId (_this.baseValue.getId ()));

                    }

                });

                m.add (mi);

                mi = this.createMenuItem ("Report error about this Id",
                                          Constants.BUG_ICON_NAME,
                                          "bug");

                mi.addActionListener (new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.getEditor ().showReportProblemForId (BaseStrings.toId (_this.baseValue.getId ()));

                    }

                });

                m.add (mi);

                mi = this.createMenuItem ("Copy Id",
                                          Constants.COPY_ICON_NAME,
                                          "copy");

                mi.addActionListener (new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        h.copy ();

                    }

                });

                m.add (mi);

            }

        });

        this.add (h);

        String comment = this.baseValue.getComment ();

        String rows = (comment != null ? "top:p, 6px" : "");

        Set<FormItem> items = this.getFormItems ();

        for (FormItem i : items)
        {

            if (rows.length () > 0)
            {

                rows += ",";

            }

            rows += "top:p,6px";

        }

        rows += ",top:p";

        Box b = new Box (BoxLayout.Y_AXIS);
        FormLayout   fl = new FormLayout ("right:60px, 5px, min(150px;p):grow",
                                          rows);
                                          //(comment != null ? "top:p, 6px," : "") + "top:p, 6px, top:p:grow, 6px, top:p, top:p, top:p");

        fl.setHonorsVisibility (true);
        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int r = 1;

        if (comment != null)
        {

            pb.addLabel ("<html><i>Comment</i></html>",
                         cc.xy (1, r));

            String c = "";

            int sc = this.getSCount ();

            if (sc > 0)
            {

                for (int i = 0; i < sc; i++)
                {

                    if (c.length () > 0)
                    {

                        c += ", ";

                    }

                    c += "%" + (i + 1) + "$s";

                }

                c = "<br /><i>Requires values: " + c + " to be present in your value.</i>";

            }

            final JLabel cl = UIUtils.createLabel (comment + c);
            //cl.setFocusable (false);
/*
            cl.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void mouseEntered (MouseEvent ev)
                {

                    TextIterator iter = new TextIterator (this.editor.getText ());

                    final Word w = iter.getWordAt (this.editor.viewToModel (ev.getPoint ()));


                }

            });
*/
            pb.add (cl,
                    cc.xy (3, r));

            r += 2;

        }

        for (FormItem i : items)
        {

            Object l = i.getLabel ();

            if (l instanceof String)
            {

                pb.addLabel (String.format ("<html><i>%s</i></html>",
                                            l.toString ()),
                             cc.xy (1,
                                    r));

            } else {

                if (l instanceof JComponent)
                {

                    pb.add ((JComponent) l,
                            cc.xy (1,
                                   r));

                }

            }

            pb.add (i.getComponent (),
                    cc.xy (3, r));

            r += 2;

        }

        //r++;

        pb.add (this.errorsLabel,
                cc.xy (1, r));

        pb.add (this.errorsWrapper,
                cc.xy (3, r));

        JPanel p = pb.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        p.setBorder (UIUtils.createPadding (5, 5, 0, 0));

        this.add (p);

        this.setBorder (UIUtils.createPadding (0, 10, 20, 10));
        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setAlignmentY (Component.TOP_ALIGNMENT);

    }

    private int getSCount ()
    {

        if (this.baseValue instanceof TextValue)
        {

            return ((TextValue) this.baseValue).getSCount ();

        }

        return -1;

    }

    public void updatePreviews ()
    {

        this.panel.getEditor ().updatePreviews ();

    }

    public void updateSideBar (Node n)
    {

        this.panel.getEditor ().updateSideBar (this.panel.getParentNode ());

    }

    public AbstractLanguageStringsEditor getEditor ()
    {

        return this.panel.getEditor ();

    }

    public abstract Component getFocusableComponent ();

    public abstract T getUserValue ();

    public abstract Set<FormItem> getFormItems ();

    public abstract void saveValue ()
                             throws GeneralException;

/*
    public void saveValue ()
                    throws GeneralException
    {

        String uv = this.getUserValue ();

        if (uv != null)
        {

            if (this.stringsValue != null)
            {

                this.stringsValue.setRawText (uv);

            } else {

                this.stringsValue = this.editor.userStrings.insertTextValue (this.baseValue.getId ());

                //this.stringsValue.setSCount (this.baseValue.getSCount ());
                this.stringsValue.setRawText (uv);

            }

        } else {

            this.editor.userStrings.removeNode (this.baseValue.getId ());

        }

    }
*/
/*
    public Id getIdAtOffset (int offset)
    {

        return BaseStrings.getId (this.userValue.getEditor ().getText (),
                                  offset);

    }
*/
/*
    public Id getIdAtCaret ()
    {

        return this.getIdAtOffset (this.userValue.getEditor ().getCaretPosition ());

    }
*/
    public String getId ()
    {

        return BaseStrings.toId (this.baseValue.getId ());

    }

    public boolean hasUserValue ()
    {

        return this.getUserValue () != null;

    }

/*
    public String getUserValue ()
    {

        StringWithMarkup sm = this.userValue.getTextWithMarkup ();

        if (sm != null)
        {

            if (!sm.hasText ())
            {

                return null;

            }

            return sm.getMarkedUpText ();

        }

        return null;

    }
*/
/*
    public void useEnglishValue ()
    {

        this.userValue.updateText (this.baseValue.getRawText ());
        this.showPreview ();
        this.validate ();
        this.repaint ();

    }
*/

    public abstract boolean hasErrors ();
/*
    public boolean hasErrors ()
    {

        String s = this.getUserValue ();

        if (s == null)
        {

            return false;

        }

        return BaseStrings.getErrors (s,
                                      BaseStrings.toId (this.baseValue.getId ()),
                                      this.baseValue.getSCount (),
                                      this.editor).size () > 0;


    }
*/

    public abstract boolean showErrors (boolean requireUserValue);

    public void hideErrors ()
    {

        this.errorsLabel.setVisible (false);
        this.errorsWrapper.setVisible (false);

    }

    public boolean showErrors (Set<String> errs)
    {

        this.hideErrors ();

        if ((errs == null)
            ||
            (errs.size () == 0)
           )
        {

            return false;

        }

        if (this.errors == null)
        {

            this.errors = UIUtils.createHelpTextPane ("",
                                                      this.getEditor ());
            this.errors.setBorder (UIUtils.createPadding (0, 0, 0, 0));
            this.errors.setFocusable (false);
            this.errorsWrapper.add (this.errors);

        }

        StringBuilder b = new StringBuilder ();

        for (String e : errs)
        {

            if (b.length () > 0)
            {

                b.append ("<br />");

            }

            b.append ("* " + e);

        }

        this.errors.setText ("<span class='error'>" + b.toString () + "</span>");
        this.errorsLabel.setVisible (true);
        this.errorsWrapper.setVisible (true);

        this.getEditor ().updateSideBar (this.baseValue);

        return true;

    }

    public abstract void showPreview ();
    /*
    public void showPreview ()
    {

        if (this.showErrors (false))
        {

            return;

        }

        String s = this.getUserValue ();

        if (s == null)
        {

            if (this.preview != null)
            {

                this.preview.setText ("");

            }

            this.previewWrapper.setVisible (false);
            this.previewLabel.setVisible (false);

            this.editor.updateSideBar (this.baseValue);

            return;

        }

        if (this.preview == null)
        {

            this.preview = UIUtils.createHelpTextPane ("",
                                                       this.editor);
            this.preview.setBorder (UIUtils.createPadding (6, 0, 0, 0));
            this.preview.setFocusable (false);

            this.previewWrapper.add (this.preview);

        }

        String t = this.editor.getPreviewText (s);

        this.previewLabel.setVisible (true);
        this.preview.setText (t);
        this.previewWrapper.setVisible (true);

        this.editor.updateSideBar (this.baseValue);

        this.validate ();
        this.repaint ();

    }
*/
/*
    private void fillMatch ()
    {

        QTextEditor editor = this.userValue.getEditor ();

        int c = editor.getCaretPosition ();

        Id id = BaseStrings.getId (editor.getText (),
                                   c);

        if (id == null)
        {

            return;

        }

        String m = this.selections.getSelectedValue ();

        //m = id.getNewFullId (m);

        Id.Part part = id.getPart (c);

        if (part != null)
        {

            editor.replaceText (part.start, part.end, m);
            editor.setCaretPosition (part.start + m.length ());

        } else {

            // Is the previous character a . if so append.
            if ((editor.getText ().substring (c - 1, c).equals ("."))
                ||
                (c == id.getEnd ())
               )
            {

                editor.replaceText (c, c, m);
                editor.setCaretPosition (c + m.length ());

            } else {

                part = id.getLastPart ();

                editor.replaceText (part.start, part.start + part.end, m);
                editor.setCaretPosition (part.start + m.length ());

            }

        }

        c = editor.getCaretPosition ();

        id = this.getIdAtCaret ();

        // We may be inserting into the middle of an id, check to see if it's valid.

        // Check to see if the id maps to a string.
        if (this.editor.baseStrings.getString (id.getId ()) != null)
        {

            if (id.isPartial ())
            {

                editor.replaceText (id.getEnd (), id.getEnd (), "}");
                editor.setCaretPosition (c + 1);

            }

            this.hideSelector ();

            return;

        }

        // Check to see if there are more matches further down the tree.
        String nid = id.getId () + ".";

        Set<String> matches = this.editor.baseStrings.getIdMatches (nid);

        if (matches.size () > 0)
        {

            c = editor.getCaretPosition ();

            editor.replaceText (c, c, ".");
            editor.setCaretPosition (c + 1);

            try
            {

                this.showSelectionPopup (matches,
                                         editor.modelToView (id.getPart (c).start).getLocation ());

            } catch (Exception e) {

                e.printStackTrace ();

            }

            return;

        } else {

            c = editor.getCaretPosition ();

            editor.replaceText (c, c, "}");
            editor.setCaretPosition (c + 1);

            this.hideSelector ();

            return;

        }

    }
*/
/*
    private String updateSelectedMatch (int incr)
    {

        int i = this.selections.getSelectedIndex ();

        i += incr;

        int s = this.selections.getModel ().getSize ();

        if (i < 0)
        {

            i = s + i;

        }

        if (i > s - 1)
        {

            i -= s;

        }

        this.selections.setSelectedIndex (i);

        return this.selections.getSelectedValue ();

    }
*/
/*
    public boolean isSelectorVisible ()
    {

        if (this.selector != null)
        {

            return this.selector.isVisible ();

        }

        return false;

    }
*/
/*
    public void hideSelector ()
    {

        if (this.selector != null)
        {

            this.selector.setVisible (false);

        }

        this.userValue.getEditor ().setFocusTraversalKeysEnabled (true);

    }
*/
/*
    public void showSelectionPopup (Set<String> matches,
                                    Point       point)
    {

        if (this.selector == null)
        {

            this.selector = new Box (BoxLayout.Y_AXIS);

            this.selector.setOpaque (true);
            this.selector.setBackground (UIUtils.getComponentColor ());
            this.selector.setBorder (UIUtils.createLineBorder ());

        }

        if ((matches == null)
            ||
            (matches.size () == 0)
           )
        {

            this.hideSelector ();

            return;

        }

        this.userValue.getEditor ().setFocusTraversalKeysEnabled (false);

        this.selector.removeAll ();

        DefaultListModel<String> m = new DefaultListModel<> ();

        for (String o : matches)
        {

            m.addElement (o);

        }

        this.selections = new JList<String> ();
        final JList<String> l = this.selections;
        l.setModel (m);
        l.setLayoutOrientation (JList.VERTICAL);
        l.setVisibleRowCount (0);
        l.setOpaque (true);
        l.setBackground (UIUtils.getComponentColor ());
        l.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
        UIUtils.setAsButton (l);

        l.setCellRenderer (new DefaultListCellRenderer ()
        {

            public Component getListCellRendererComponent (JList   list,
                                                           Object  value,
                                                           int     index,
                                                           boolean isSelected,
                                                           boolean cellHasFocus)
            {

                String obj = (String) value;

                JLabel l = (JLabel) super.getListCellRendererComponent (list,
                                                                        value,
                                                                        index,
                                                                        isSelected,
                                                                        cellHasFocus);

                l.setText (obj);//.getName ());

                l.setFont (l.getFont ().deriveFont (UIUtils.getScaledFontSize (10)).deriveFont (Font.PLAIN));
                l.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
                l.setPreferredSize (new Dimension (l.getPreferredSize ().width, 29));

                return l;

            }

        });

        l.setSelectedIndex (0);

        int rowHeight = 30;

        l.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        JScrollPane sp = new JScrollPane (l);

        sp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar ().setUnitIncrement (rowHeight);
        sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        sp.setOpaque (false);
        sp.setBorder (null);

        this.selector.add (sp);

        l.addListSelectionListener (new ListSelectionListener ()
        {

            @Override
            public void valueChanged (ListSelectionEvent ev)
            {

            }

        });

        this.selector.setPreferredSize (new Dimension (300,
                                                       rowHeight * (matches.size () > 10 ? 10 : matches.size ())));

        this.editor.showPopupAt (this.selector,
                                 SwingUtilities.convertPoint (this.userValue,
                                                              point,
                                                              this.editor),
                                 false);

    }
*/
    @Override
    public Dimension getMaximumSize ()
    {

        return new Dimension (Short.MAX_VALUE,
                              this.getPreferredSize ().height);

    }
/*
    public class Id
    {

        private int _start = -1;
        private String fullId = null;
        private boolean hasClosingBrace = false;
        private java.util.List<Part> parts = new ArrayList<> ();
        public boolean hasErrors = false;

        public Id (String text,
                   int    offset)
        {

            if (text.length () < 3)
            {

                return;

            }

            String idstart = LanguageStrings.ID_REF_START;

            int ind = text.lastIndexOf (idstart, offset);

            if (ind > -1)
            {

                ind += idstart.length ();
                this._start = ind;

                int idendind = text.indexOf (LanguageStrings.ID_REF_END, ind);

                if (idendind > -1)
                {

                    if (idendind < offset)
                    {

                        return;

                    }

                    // We have an end, see if it's on the same line.
                    int leind = text.indexOf ("\n", ind);

                    if (leind < 0)
                    {

                        leind = text.length ();

                    }

                    if (idendind < leind)
                    {

                        this.hasClosingBrace = true;
                        this.fullId = text.substring (ind, leind - 1);

                    }

                } else {
System.out.println ("ELSE");
                    StringBuilder b = new StringBuilder ();

                    for (int i = ind; i < text.length (); i++)
                    {

                        char c = text.charAt (i);

                        if (Character.isWhitespace (c))
                        {

                            break;

                        }

                        b.append (c);

                    }

                    this.fullId = b.toString ();

                }
System.out.println ("ID: " + this.fullId);
                if (this.fullId.equals (""))
                {

                    this.fullId = null;

                }

                int start = ind;

                java.util.List<String> parts = Utils.splitString (this.fullId,
                                                                  ".");

                int cind = start;

                Part prevp = null;

                for (int i = 0; i < parts.size (); i++)
                {

                    if (i > 0)
                    {

                        cind++;

                    }

                    String ps = parts.get (i);

                    if (ps.trim ().length () != ps.length ())
                    {

                        this.hasErrors = true;

                    }

                    Part p = new Part (this,
                                       cind,
                                       ps,
                                       prevp);

                    prevp = p;
                    cind += ps.length ();

                    this.parts.add (p);

                }

            }

        }

        public int getEnd ()
        {

            if (this.parts.size () == 0)
            {

                return this._start;

            }

            return this.parts.get (this.parts.size () - 1).end;

        }

        public Part getPart (int offset)
        {

            for (int i = 0; i < this.parts.size (); i++)
            {

                Part p = this.parts.get (i);

                if ((offset >= p.start)
                    &&
                    (offset <= p.end)
                   )
                {

                    return p;

                }

            }

            return null;

        }

        public String getFullId ()
        {

            return this.fullId;

        }

        public boolean isIdValid (LanguageStrings baseStrings)
        {

            if (this.fullId == null)
            {

                return false;

            }

            return baseStrings.isIdValid (this.fullId);

        }

        public boolean hasErrors ()
        {

            return this.hasErrors;

        }

        public String getNewFullId (String suffix)
        {

            if (this.fullId.endsWith ("."))
            {

                return this.fullId + suffix;

            }

            String pref = this.getIdPrefix ();

            if (pref == null)
            {

                return suffix;

            }

            return pref + "." + suffix;

        }

        public String getIdPrefix ()
        {

            StringBuilder b = new StringBuilder ();

            for (int i = 0; i < this.parts.size () - 1; i++)
            {

                if (i > 0)
                {

                    b.append (".");

                }

                b.append (this.parts.get (i).part);

            }

            if (b.length () == 0)
            {

                return null;

            }

            return b.toString ();

        }

        public Part getLastPart ()
        {

            if (this.parts.size () == 0)
            {

                return null;

            }

            return this.parts.get (this.parts.size () - 1);

        }

        public Set<String> getPartMatches (int             offset,
                                           LanguageStrings baseStrings)
        {

            Part p = this.getPart (offset);

            if (p != null)
            {

                return baseStrings.getIdMatches (p.getFullId ());

            }

            return this.getMatches (baseStrings);

        }

        public Set<String> getMatches (LanguageStrings baseStrings)
        {

            return baseStrings.getIdMatches (this.fullId);

        }

        public class Part
        {

            public int start = -1;
            public int end = -1;
            public String part = null;
            public Id parent = null;
            public Part previous = null;

            public Part (Id     parent,
                         int    start,
                         String part,
                         Part   prev)
            {

                this.start = start;
                this.end = this.start + part.length ();
                this.parent = parent;
                this.part = part;
                this.previous = prev;

            }

            public String getFullId ()
            {

                StringBuilder b = new StringBuilder (this.part);

                Part prev = this.previous;

                while (prev != null)
                {

                    b.insert (0, prev.part + ".");
                    prev = prev.previous;

                }

                return b.toString ();

            }

        }

    }
*/

}
