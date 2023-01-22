package com.quollwriter.ui.fx;

import java.util.concurrent.*;

import java.util.*;
import java.util.stream.*;

import javafx.scene.control.*;
import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;

public abstract class LanguageStringsIdBox<E extends Value, T extends Object> extends VBox implements org.fxmisc.flowless.Cell<Value, LanguageStringsIdBox<E, T>>
{

    protected LanguageStringsIdsPanel panel = null;
    protected E baseValue = null;
    protected E stringsValue = null;
    private Id id = null;
    private Form form = null;

    public LanguageStringsIdBox (final E                       baseValue,
                                 final E                       stringsValue,
                                 final LanguageStringsIdsPanel panel)
    {

        final LanguageStringsIdBox _this = this;

        this.panel = panel;
        this.baseValue = baseValue;
        this.stringsValue = stringsValue;

        this.id = new Id (0,
                          BaseStrings.toId (this.baseValue.getId ()),
                          false);

    }

    public E getBaseValue ()
    {

        return this.baseValue;

    }

    @Override
    public boolean isReusable ()
    {

        return false;

    }

    @Override
    public void updateItem (Value v)
    {



    }

    @Override
    public LanguageStringsIdBox getNode ()
    {

        //this.init ();

        return this;

    }

    public void init ()
    {

        final String fid = BaseStrings.toId (this.baseValue.getId ());

        final LanguageStringsIdBox _this = this;

        Set<MenuItem> items = new LinkedHashSet<> ();
        items.add (QuollMenuItem.builder ()
            .label (new SimpleStringProperty ("Find all references"))
            .iconName (StyleClassNames.FIND)
            .onAction (ev ->
            {

                _this.panel.getEditor ().showFind (BaseStrings.toId (_this.baseValue.getId ()));

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (new SimpleStringProperty ("Report error about this Id"))
            .iconName (StyleClassNames.BUG)
            .onAction (ev ->
            {

                this.getEditor ().showReportProblemForId (BaseStrings.toId (this.baseValue.getId ()));

            })
            .build ());

        items.add (QuollMenuItem.builder ()
            .label (new SimpleStringProperty ("Copy Id"))
            .iconName (StyleClassNames.COPY)
            .onAction (ev ->
            {

                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString (BaseStrings.ID_REF_START + BaseStrings.toId (this.baseValue.getId ()) + BaseStrings.ID_REF_END);
                clipboard.setContent (content);

            })
            .build ());

        Header h = Header.builder ()
            .title (new SimpleStringProperty (BaseStrings.toId (this.baseValue.getId ())))
            .contextMenu (items)
            .build ();

        this.getStyleClass ().add (this.getStyleClassName ());

        this.getChildren ().add (h);

        String comment = this.baseValue.getComment ();

        String scount = "";

        int sc = this.getSCount ();

        if (sc > 0)
        {

            StringBuilder c = new StringBuilder ();

            for (int i = 0; i < sc; i++)
            {

                if (c.length () > 0)
                {

                    c.append (", ");

                }

                c.append ("%" + (i + 1) + "$s");

            }

        }

        if (comment == null)
        {

            comment = "";

        }

        if (scount.length () > 0)
        {

            scount = String.format ("Requires values: %1$s to be present in your value.",
                                    scount);

            if (comment.length () > 0)
            {

                comment += "\n";

            }

            comment += scount;

        }

        Form.Builder fb = Form.builder ()
            .description (new SimpleStringProperty (comment));

        for (Form.Item i : this.getFormItems ())
        {

            fb.item (i);

        }

        this.form = fb.build ();

        this.getChildren ().add (this.form);

        this.showErrors (false);

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

    public void updateSideBar (Node    n)
    {

        this.panel.getEditor ().updateSideBar (this.panel.getParentNode ());

    }

    public AbstractLanguageStringsEditor getEditor ()
    {

        return this.panel.getEditor ();

    }

    public abstract String getStyleClassName ();

    public abstract T getUserValue ();

    public abstract Set<Form.Item> getFormItems ();

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
    public Id getForId ()
    {

        return this.id;

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

    public abstract boolean showErrors (boolean requireUserValue);

    public boolean showErrors (Set<String> errs)
    {

        if ((errs == null)
            ||
            (errs.size () == 0)
           )
        {

            this.form.hideError ();

            return false;

        }

        this.form.showErrors (new LinkedHashSet<> (errs.stream ()
            .map (e -> new SimpleStringProperty (e))
            .collect (Collectors.toList ())));

        this.updateSideBar (this.baseValue);

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
