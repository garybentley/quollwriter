package com.quollwriter.ui;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

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

public class LanguageStringsIdBox extends Box
{

    private TextArea userValue = null;
    private AbstractLanguageStringsEditor editor = null;
    private Box selector = null;
    private JList<String> selections = null;
    private TextValue baseValue = null;
    private TextValue stringsValue = null;

    private JTextPane preview = null;
    private Box previewWrapper = null;
    private JLabel previewLabel = null;
    private JTextPane errors = null;
    private Box errorsWrapper = null;
    private JLabel errorsLabel = null;

    public LanguageStringsIdBox (final TextValue                     baseValue,
                                 final TextValue                     stringsValue,
                                 final AbstractLanguageStringsEditor editor)
    {

        super (BoxLayout.Y_AXIS);

        final LanguageStringsIdBox _this = this;

        this.editor = editor;
        this.baseValue = baseValue;
        this.stringsValue = stringsValue;

        Header h = UIUtils.createHeader (BaseStrings.toId (this.baseValue.getId ()),
                                         Constants.SUB_PANEL_TITLE);

        h.setBorder (UIUtils.createBottomLineWithPadding (0, 0, 3, 0));
        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (h);

        String comment = this.baseValue.getComment ();

        Box b = new Box (BoxLayout.Y_AXIS);
        FormLayout   fl = new FormLayout ("right:60px, 5px, min(150px;p):grow",
                                          (comment != null ? "top:p, 6px," : "") + "top:p, 6px, top:p:grow, 6px, top:p, top:p, top:p");
        fl.setHonorsVisibility (true);
        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int r = 1;

        if (comment != null)
        {

            pb.addLabel ("<html><i>Comment</i></html>",
                         cc.xy (1, r));

            String c = "";

            if (this.baseValue.getSCount () > 0)
            {

                for (int i = 0; i < this.baseValue.getSCount (); i++)
                {

                    if (c.length () > 0)
                    {

                        c += ", ";

                    }

                    c += "%" + (i + 1) + "$s";

                }

                c = "<br /><i>Requires values: " + c + " to be present in your value.</i>";

            }

            pb.addLabel ("<html>" + comment + c + "</html>",
                         cc.xy (3, r));

            r += 2;

        }

        pb.addLabel ("<html><i>English</i></html>",
                     cc.xy (1,
                            r));

        JTextArea l = new JTextArea (baseValue.getRawText ()); //defValue);
        l.setLineWrap (true);
        l.setWrapStyleWord (true);
        l.setEditable (false);
        l.setBackground (UIUtils.getComponentColor ());
        l.setAlignmentX (Component.LEFT_ALIGNMENT);

        //l.setMinimumSize (new Dimension (200, 20));

        pb.add (l,
                cc.xy (3, r));

        r += 2;

        pb.addLabel ("<html><i>Your Value</i></html>",
                     cc.xy (1, r));

        this.userValue = new TextArea (null,
                                       3,
                                       -1,
                                       false)
        {

            @Override
            public void fillPopupMenuForExtraItems (MouseEvent ev,
                                                    JPopupMenu popup,
                                                    boolean    compress)
            {

                if (compress)
                {

                    java.util.List<JComponent> buts = new java.util.ArrayList ();

                    buts.add (UIUtils.createButton (Constants.COPY_ICON_NAME,
                                                    Constants.ICON_MENU,
                                                    "Use the English value",
                                                    new ActionListener ()
                                                    {

                                                         public void actionPerformed (ActionEvent ev)
                                                         {

                                                             _this.useEnglishValue ();

                                                         }

                                                    }));

                    popup.add (UIUtils.createPopupMenuButtonBar ("Manage",
                                                                 //"Edit",
                                                                 popup,
                                                                 buts));

                } else {

                    JMenuItem mi = null;

                    mi = UIUtils.createMenuItem ("Use the English value",
                                                 Constants.COPY_ICON_NAME,
                                                 new ActionListener ()
                                                 {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.useEnglishValue ();

                                                    }

                                                 });
                    popup.add (mi);

                }

            }

        };

        this.userValue.addKeyListener (new KeyAdapter ()
        {

            private ScheduledFuture task = null;

            private void update ()
            {

                if (this.task != null)
                {

                    this.task.cancel (false);

                }

                this.task = _this.editor.schedule (new Runnable ()
                {

                    @Override
                    public void run ()
                    {

                        UIUtils.doLater (new ActionListener ()
                        {

                            @Override
                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.showPreview ();

                            }

                        });

                    }

                },
                750,
                0);

            }

            @Override
            public void keyPressed (KeyEvent ev)
            {

                this.update ();

            }

            @Override
            public void keyReleased (KeyEvent ev)
            {

                this.update ();

            }

        });

        //this.userValue.setBorder (UIUtils.createLineBorder ());

        try
        {

            this.userValue.setDictionaryProvider (new UserDictionaryProvider (UserProperties.get (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME))
            {

                @Override
                public SpellChecker getSpellChecker ()
                {

                    final SpellChecker sp = super.getSpellChecker ();

                    return new SpellChecker ()
                    {

                        @Override
                        public boolean isCorrect (Word word)
                        {

                            int offset = word.getAllTextStartOffset ();

                            Id id = _this.getIdAtOffset (offset);

                            if (id != null)
                            {

                                return _this.editor.baseStrings.isIdValid (id.getId ());

                            }

                            return sp.isCorrect (word);

                        }

                        @Override
                        public boolean isIgnored (Word word)
                        {

                            return false;

                        }

                        @Override
                        public java.util.List<String> getSuggestions (Word word)
                        {

                            return sp.getSuggestions (word);

                        }

                    };

                }

            });

            this.userValue.setSpellCheckEnabled (true);

        } catch (Exception e) {

            e.printStackTrace ();

        }

        final Action defSelect = this.userValue.getEditor ().getActionMap ().get (DefaultEditorKit.selectWordAction);

        this.userValue.getEditor ().getActionMap ().put (DefaultEditorKit.selectWordAction,
                                                         new TextAction (DefaultEditorKit.selectWordAction)
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                int c = _this.userValue.getEditor ().getCaretPosition ();

                Id id = _this.getIdAtCaret ();

                if (id != null)
                {

                    _this.userValue.getEditor ().setSelectionStart (id.getPart (c).start);
                    _this.userValue.getEditor ().setSelectionEnd (id.getPart (c).end);

                } else {

                    defSelect.actionPerformed (ev);

                }

            }

        });

        if (stringsValue != null)
        {

            this.userValue.setText (stringsValue.getRawText ());

        }

        this.userValue.setAutoGrabFocus (false);

        InputMap im = this.userValue.getInputMap (JComponent.WHEN_FOCUSED);
        ActionMap am = this.userValue.getActionMap ();

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                        InputEvent.CTRL_MASK),
                "preview");

        am.put ("preview",
                new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.showPreview ();

            }

        });

        this.userValue.addKeyListener (new KeyAdapter ()
        {

            @Override
            public void keyPressed (KeyEvent ev)
            {

                if ((ev.getKeyCode () == KeyEvent.VK_ENTER)
                    ||
                    (ev.getKeyCode () == KeyEvent.VK_UP)
                    ||
                    (ev.getKeyCode () == KeyEvent.VK_DOWN)
                   )
                {

                    if (_this.isSelectorVisible ())
                    {

                        ev.consume ();

                        return;

                    }

                }

                if (ev.getKeyCode () == KeyEvent.VK_TAB)
                {

                    ev.consume ();

/*
                    if (id.hasErrors ())
                    {

                        _this.hideSelector ();

                        return;

                    }
*/
                    _this.fillMatch ();//m);

                }

            }

            @Override
            public void keyReleased (KeyEvent ev)
            {

                if ((ev.getKeyCode () == KeyEvent.VK_CLOSE_BRACKET)
                    &&
                    (ev.isShiftDown ())
                   )
                {

                    _this.hideSelector ();

                    return;

                }

                if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                {

                    if (_this.isSelectorVisible ())
                    {

                        ev.consume ();

                        _this.fillMatch ();

                        return;

                    }

                }

                if (ev.getKeyCode () == KeyEvent.VK_UP)
                {

                    if (_this.isSelectorVisible ())
                    {

                        ev.consume ();

                        _this.updateSelectedMatch (-1);

                        return;

                    }

                }

                if (ev.getKeyCode () == KeyEvent.VK_DOWN)
                {

                    if (_this.isSelectorVisible ())
                    {

                        ev.consume ();

                        _this.updateSelectedMatch (1);

                        return;

                    }

                }

                if (ev.getKeyCode () == KeyEvent.VK_ESCAPE)
                {

                    ev.consume ();

                    _this.hideSelector ();

                    return;

                }

                int c = _this.userValue.getEditor ().getCaretPosition ();

                String t = _this.userValue.getEditor ().getText ();

                Id id = BaseStrings.getId (t, c);

                if (id == null)
                {

                    _this.hideSelector ();

                    return;

                }

                Set<String> matches = id.getPartMatches (c,
                                                         _this.editor.baseStrings.getStrings ());

                if ((matches == null)
                    ||
                    (matches.size () == 0)
                   )
                {

                    _this.hideSelector ();

                    return;

                }

                if (matches.size () == 1)
                {

                    Id.Part p = id.getPart (c);

                    if (p == null)
                    {

                        p = id.getLastPart ();

                    }

                    if (p.part.equals (matches.iterator ().next ()))
                    {

                        _this.hideSelector ();
                        return;

                    }

                }

                try
                {

                    int ind = c;

                    Id.Part pa = id.getPart (c);

                    if (pa != null)
                    {

                        ind = pa.start;

                    }

                    Rectangle r = _this.userValue.getEditor ().modelToView (ind);

                    //Point p = r.getLocation ();
                    //p.y -= 10;
                    //p.x -= 10;
                    //p.y += r.height;.

                    _this.showSelectionPopup (matches,
                                              r.getLocation ());

                } catch (Exception e) {

                    e.printStackTrace ();

                }

            }

        });

        pb.add (this.userValue,
                cc.xy (3, r));

        r += 2;

        // Needed to prevent the performance hit
        this.errorsWrapper = new Box (BoxLayout.Y_AXIS);

        this.errorsLabel = UIUtils.createErrorLabel ("Errors");
        this.errorsLabel.setBorder (UIUtils.createPadding (6, 0, 0, 0));
        this.errorsLabel.setVisible (false);
        this.errorsLabel.setIcon (null);
        this.errorsLabel.setFocusable (false);

        pb.add (this.errorsLabel,
                cc.xy (1, r));
        pb.add (this.errorsWrapper,
                cc.xy (3, r));

        r += 1;

        // Needed to prevent the performance hit
        this.previewWrapper = new Box (BoxLayout.Y_AXIS);

        this.previewLabel = UIUtils.createInformationLabel ("Preview");
        this.previewLabel.setBorder (UIUtils.createPadding (6, 0, 0, 0));
        this.previewLabel.setVisible (false);

        pb.add (this.previewLabel,
                cc.xy (1, r));
        pb.add (this.previewWrapper,
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

    public Id getIdAtOffset (int offset)
    {

        return BaseStrings.getId (this.userValue.getEditor ().getText (),
                                  offset);
/*
        Id id = new Id (this.userValue.getEditor ().getText (),
                        offset);

        if (id.fullId == null)
        {

            return null;

        }

        return id;
*/
    }

    public Id getIdAtCaret ()
    {

        return this.getIdAtOffset (this.userValue.getEditor ().getCaretPosition ());

    }

    public String getId ()
    {

        return BaseStrings.toId (this.baseValue.getId ());

    }

    public boolean hasUserValue ()
    {

        return this.getUserValue () != null;

    }

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

    public void useEnglishValue ()
    {

        this.userValue.updateText (this.baseValue.getRawText ());
        this.showPreview ();
        this.validate ();
        this.repaint ();

    }

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

    public boolean showErrors (boolean requireUserValue)
    {

        String s = this.getUserValue ();

        if ((s == null)
            &&
            (!requireUserValue)
           )
        {

            this.errorsLabel.setVisible (false);
            this.errorsWrapper.setVisible (false);

            return false;

        }

        Set<String> errs = null;

        if (s == null)
        {

            errs = new LinkedHashSet<> ();

            errs.add ("Cannot show a preview, no value provided.");

        } else {

            errs = BaseStrings.getErrors (s,
                                          BaseStrings.toId (this.baseValue.getId ()),
                                          this.baseValue.getSCount (),
                                          this.editor);

        }

        Node root = this.baseValue.getRoot ();

        this.editor.updateSideBar (this.baseValue);

        if (errs.size () > 0)
        {

            if (this.errors == null)
            {

                this.errors = UIUtils.createHelpTextPane ("",
                                                          this.editor);
                this.errors.setBorder (UIUtils.createPadding (6, 0, 0, 0));
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

                b.append ("- " + e);

            }

            this.errors.setText ("<span class='error'>" + b.toString () + "</span>");
            this.errorsLabel.setVisible (true);
            this.errorsWrapper.setVisible (true);

            this.editor.updateSideBar (this.baseValue);

            return true;

        } else {

            this.errorsLabel.setVisible (false);
            this.errorsWrapper.setVisible (false);

        }

        return false;

    }

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
/*
        if (id.hasErrors ())
        {

            this.hideSelector ();

            // Update the view to "spell check" the ids.
            return;

        }
*/
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

    public boolean isSelectorVisible ()
    {

        if (this.selector != null)
        {

            return this.selector.isVisible ();

        }

        return false;

    }

    public void hideSelector ()
    {

        if (this.selector != null)
        {

            this.selector.setVisible (false);

        }

        this.userValue.getEditor ().setFocusTraversalKeysEnabled (true);

    }

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
/*
                l.setIcon (Environment.getObjectIcon (obj,
                                                      Constants.ICON_NOTIFICATION));
*/
                l.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));
                l.setPreferredSize (new Dimension (l.getPreferredSize ().width, 29));
/*
                if (cellHasFocus)
                {

                    l.setBackground (Environment.getHighlightColor ());

                }
*/
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
/*
        sp.getViewport ().setPreferredSize (new Dimension (300,
                                                           rowHeight * (matches.size () > 10 ? 10 : matches.size ())));
*/
        sp.setBorder (null);

        this.selector.add (sp);

        l.addListSelectionListener (new ListSelectionListener ()
        {

            @Override
            public void valueChanged (ListSelectionEvent ev)
            {
/*
                if (onSelect != null)
                {

                    NamedObject obj = (NamedObject) l.getSelectedValue ();

                    onSelect.actionPerformed (new ActionEvent (l,
                                                               0,
                                                               obj.getObjectReference ().asString ()));

                    if (closeOnSelect)
                    {

                        ep.removeFromParent ();

                    }

                }
*/
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
