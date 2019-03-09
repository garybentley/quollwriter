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

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.forms.*;

public class LanguageStringsTextIdBox extends LanguageStringsIdBox<TextValue, String>
{

    private TextArea userValue = null;
    private Box selector = null;
    private JList<String> selections = null;

    private JTextPane preview = null;
    private Box previewWrapper = null;
    private JLabel previewLabel = null;

    public LanguageStringsTextIdBox (final TextValue               baseValue,
                                     final TextValue               stringsValue,
                                     final LanguageStringsIdsPanel panel)
    {

        super (baseValue,
               stringsValue,
               panel);

    }

    public Component getFocusableComponent ()
    {

        return this.userValue.getEditor ();

    }

    public Set<FormItem> getFormItems ()
    {

        final LanguageStringsTextIdBox _this = this;

        // Needed to prevent the performance hit
        this.previewWrapper = new Box (BoxLayout.Y_AXIS);

        this.previewLabel = UIUtils.createInformationLabel ("Preview");
        this.previewLabel.setBorder (UIUtils.createPadding (0, 0, 0, 0));
        this.previewLabel.setVisible (false);

        Set<FormItem> items = new LinkedHashSet<> ();

        JTextArea l = new JTextArea (this.baseValue.getRawText ());
        l.setLineWrap (true);
        l.setWrapStyleWord (true);
        l.setEditable (false);
        l.setBackground (UIUtils.getComponentColor ());
        l.setAlignmentX (Component.LEFT_ALIGNMENT);
        //l.setFocusable (false);

        AnyFormItem enValue = new AnyFormItem ("<html><i>English</i></html>",
                                               l);

        items.add (enValue);

        this.userValue = new TextArea (null,
                                       3,
                                       -1,
                                       false)
        {

            @Override
            public void onCut ()
            {

                _this.updatePreviews ();
_this.showPreview ();
            }

            @Override
            public void onPaste ()
            {

                _this.updatePreviews ();
_this.showPreview ();
            }

            @Override
            public void fillPopupMenuForExtraItems (MouseEvent ev,
                                                    JPopupMenu popup,
                                                    boolean    compress)
            {

                if (compress)
                {

                    java.util.List<JComponent> buts = new java.util.ArrayList<> ();

                    final Id id = _this.getIdAtPoint (ev.getPoint ());

                    if (id != null)
                    {

                        buts.add (UIUtils.createButton (Constants.VIEW_ICON_NAME,
                                                        Constants.ICON_MENU,
                                                        "Go to Id definition",
                                                        new ActionListener ()
                                                        {

                                                             public void actionPerformed (ActionEvent ev)
                                                             {

                                                                 _this.getEditor ().showId (id.getId ());

                                                             }

                                                        }));

                    }


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

                    final Id id = _this.getIdAtPoint (ev.getPoint ());

                    if (id != null)
                    {

                        popup.add (UIUtils.createMenuItem ("Go to Id definition",
                                                           Constants.VIEW_ICON_NAME,
                                                           new ActionListener ()
                                                           {

                                                               public void actionPerformed (ActionEvent ev)
                                                               {

                                                                   _this.getEditor ().showId (id.getId ());

                                                               }

                                                           }));

                    }

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

        this.userValue.addMouseMotionListener (new MouseAdapter ()
        {

            @Override
            public void mouseMoved (MouseEvent ev)
            {

                _this.userValue.setToolTipText (null);

                Id id = _this.getIdAtPoint (ev.getPoint ());

                if (id != null)
                {

                    _this.userValue.setToolTipText (_this.getEditor ().getString (id.getId ()));

                }

            }

        });

        this.userValue.getEditor ().getDocument ().addDocumentListener (new DocumentListener ()
        {

            private ScheduledFuture task = null;

            private void update ()
            {

                if (this.task != null)
                {

                    this.task.cancel (false);

                }

                this.task = _this.getEditor ().schedule (new Runnable ()
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
            public void insertUpdate (DocumentEvent ev)
            {

                _this.updatePreviews ();
_this.showPreview ();
            }

            @Override
            public void changedUpdate (DocumentEvent ev)
            {

                _this.updatePreviews ();
_this.showPreview ();
            }

            @Override
            public void removeUpdate (DocumentEvent ev)
            {

                _this.updatePreviews ();
_this.showPreview ();
            }

        });

        this.userValue.addKeyListener (new KeyAdapter ()
        {

            private ScheduledFuture task = null;

            private void update ()
            {

                if (this.task != null)
                {

                    this.task.cancel (false);

                }

                this.task = _this.getEditor ().schedule (new Runnable ()
                {

                    @Override
                    public void run ()
                    {

                        UIUtils.doLater (new ActionListener ()
                        {

                            @Override
                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.updatePreviews ();

                                //_this.showPreview ();

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

                _this.updatePreviews ();
_this.showPreview ();
            }

            @Override
            public void keyReleased (KeyEvent ev)
            {

                _this.updatePreviews ();
_this.showPreview ();
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

                                return _this.getEditor ().baseStrings.isIdValid (id.getId ());

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
                                                         _this.getEditor ().baseStrings.getStrings ());

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

                    Environment.logError ("Unable show selection popup",
                                          e);

                }

            }

        });

        AnyFormItem yourValue = new AnyFormItem ("<html><i>Your Value</i></html>",
                                                 this.userValue);

        items.add (yourValue);

        items.add (new AnyFormItem (this.previewLabel,
                                    this.previewWrapper));

        return items;

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

                this.stringsValue = this.getEditor ().userStrings.insertTextValue (this.baseValue.getId ());

                //this.stringsValue.setSCount (this.baseValue.getSCount ());
                this.stringsValue.setRawText (uv);

            }

        } else {

            this.getEditor ().userStrings.removeNode (this.baseValue.getId ());

        }

    }

    public Id getIdAtPoint (Point p)
    {

        try
        {

            return BaseStrings.getId (this.userValue.getEditor ().getText (),
                                      this.userValue.getEditor ().viewToModel (p));

        } catch (Exception e) {

            // Can ignore.

        }

        return null;

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

    @Override
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
                                      this.getEditor ()).size () > 0;


    }

    public boolean showErrors (boolean requireUserValue)
    {

        String s = this.getUserValue ();

        this.hideErrors ();

        if ((s == null)
            &&
            (!requireUserValue)
           )
        {

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
                                          this.getEditor ());

        }

        if (errs.size () > 0)
        {

            return this.showErrors (errs);

        }

        return false;

/*
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
*/

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

            this.updateSideBar (this.baseValue);

            return;

        }

        if (this.preview == null)
        {

            this.preview = UIUtils.createHelpTextPane ("",
                                                       this.getEditor ());
            this.preview.setBorder (UIUtils.createPadding (6, 0, 0, 0));
            this.preview.setFocusable (false);

            this.previewWrapper.add (this.preview);

            UIUtils.addHyperLinkListener (this.preview,
                                          this.getEditor ());

        }

        String t = this.getEditor ().getPreviewText (s);

        t = StringUtils.replaceString (t,
                                       "[",
                                       "&#91;");

        t = StringUtils.replaceString (t,
                                       "]",
                                       "&#93;");

        this.previewLabel.setVisible (true);
        this.preview.setText (t);
        this.previewWrapper.setVisible (true);

        this.updateSideBar (this.baseValue);

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
        if (this.getEditor ().baseStrings.getString (id.getId ()) != null)
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

        Set<String> matches = this.getEditor ().baseStrings.getIdMatches (nid);

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

        this.selector.setPreferredSize (new Dimension (300,
                                                       rowHeight * (matches.size () > 10 ? 10 : matches.size ())));

        this.getEditor ().showPopupAt (this.selector,
                                       SwingUtilities.convertPoint (this.userValue,
                                                                    point,
                                                                    this.getEditor ()),
                                       false);

    }

    @Override
    public Dimension getMaximumSize ()
    {

        return new Dimension (Short.MAX_VALUE,
                              this.getPreferredSize ().height);

    }

}
