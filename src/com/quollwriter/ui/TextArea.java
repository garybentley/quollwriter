package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.Markup;
import com.quollwriter.ui.components.ActionAdapter;

import com.quollwriter.DictionaryProvider;
import com.quollwriter.synonyms.*;
import com.quollwriter.text.*;

import com.quollwriter.ui.components.ScrollableBox;

public class TextArea extends ScrollableBox
{

    protected QTextEditor text = null;
    protected JScrollPane scrollPane = null;
    private JLabel maxText = null;
    private String placeholder = null;
    private int maxChars = -1;
    private boolean autoGrabFocus = true;
    private boolean spellCheckEnabled = false;
    private int rows = -1;

    public TextArea ()
    {

        this (null,
              -1,
              -1);

    }

    public TextArea (String placeholder,
                     int    rows,
                     int    maxChars)
    {

        this (placeholder,
              rows,
              maxChars,
              true);

    }

    public TextArea (String  placeholder,
                     int     rows,
                     int     maxChars,
                     boolean wrapInScrollPane)
    {

        super (BoxLayout.Y_AXIS);

        this.text = new QTextEditor (null,
                                     false);

        this.text.setCanFormat (false);

        JLabel l = new JLabel ();

        this.text.setFontColor (l.getForeground ());
        this.text.setFontFamily (l.getFont ().getName ());
        this.text.setFontSize (l.getFont ().getSize ());
        this.rows = rows;

        if (rows > 0)
        {

            //this.text.setPreferredSize (new Dimension (0, (int) (this.text.getLineHeight () * (rows + 0.8))));

            this.text.setMinimumSize (new Dimension (200,
                                                     this.text.getLineHeight () * (int) (rows + 0.8)));

        } else {

            this.text.setMinimumSize (new Dimension (300, this.text.getLineHeight () * 1));

        }

        if (placeholder != null)
        {

            Color c = this.text.getForeground ();

            this.placeholder = Environment.replaceObjectNames (placeholder);
            this.text.setTextWithMarkup (new StringWithMarkup (this.placeholder));
            this.text.setFontColor (UIUtils.getHintTextColor ());
            this.text.getCaret ().setDot (0);
            this.text.setCaretColor (c);
            this.spellCheckEnabled = false;

        }

        this.text.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
        this.text.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

        final TextArea _this = this;

        this.addCaretListener (new CaretListener ()
        {

            @Override
            public void caretUpdate (CaretEvent ev)
            {

                if (_this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
                {

                    return;

                }

                _this.updateMax ();

            }

        });

        final ActionMap am = this.text.getActionMap ();

        am.put (QTextEditor.CUT_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.text.cut ();

                        _this.updateMax ();

                    }

                });

        am.put (QTextEditor.PASTE_ACTION_NAME,
                new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        if (_this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
                        {

                            _this.text.setTextWithMarkup (new StringWithMarkup (""));

                        }

                        _this.text.setFontColor (new JLabel ().getForeground ());
                        _this.text.paste ();

                        _this.setSpellCheckEnabled (_this.spellCheckEnabled);

                        _this.updateMax ();

                    }

                });


        this.maxChars = maxChars;

        this.maxText = new JLabel (String.format (Environment.getUIString (LanguageStrings.textarea,
                                                                           LanguageStrings.maxchars),
                                                  Environment.formatNumber (maxChars)));
        this.maxText.setForeground (UIUtils.getHintTextColor ());

        this.text.addKeyListener (new KeyAdapter ()
        {

            public void keyPressed (KeyEvent ev)
            {

                if (_this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
                {

                    _this.text.setTextWithMarkup (new StringWithMarkup (""));
                    _this.setTextColorToNormal ();
                    _this.setSpellCheckEnabled (_this.spellCheckEnabled);

                }

                _this.updateMax ();

            }

            public void keyReleased (KeyEvent ev)
            {

                if (_this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
                {

                    _this.text.setTextWithMarkup (new StringWithMarkup (""));
                    _this.setTextColorToNormal ();
                    _this.setSpellCheckEnabled (_this.spellCheckEnabled);

                }

                _this.updateMax ();

            }

        });

        this.text.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void performAction (ActionEvent ev)
            {

                Action aa = (Action) am.get (ev.getActionCommand ());

                if (aa != null)
                {

                    aa.actionPerformed (ev);

                }

            }

            @Override
            public void fillPopup (JPopupMenu popup,
                                   MouseEvent ev)
            {

                _this.fillPopupMenu (ev,
                                     popup);

            }

            @Override
            public void mouseEntered (MouseEvent ev)
            {

                if ((!_this.text.getText ().equals (""))
                    &&
                    (_this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
                   )
                {

                    _this.text.getCaret ().setDot (0);

                }

                if (_this.isAutoGrabFocus ())
                {

                    _this.text.grabFocus ();

                }

            }

            @Override
            public void handlePress (MouseEvent ev)
            {

                Color c = new JLabel ().getForeground ();

                if ((!_this.text.getText ().equals (""))
                    &&
                    (_this.text.getFontColor ().equals (c))
                   )
                {

                    return;

                }

                if (_this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
                {

                    _this.text.getCaret ().setDot (0);

                    return;

                }

                _this.text.setTextWithMarkup (new StringWithMarkup (""));
                _this.setTextColorToNormal ();
                _this.setSpellCheckEnabled (_this.spellCheckEnabled);

                _this.updateMax ();

            }

            @Override
            public void mousePressed (MouseEvent ev)
            {

                _this.updateMax ();

            }

        });

        if (wrapInScrollPane)
        {

            this.text.setPreferredSize (new Dimension (200,
                                                     this.text.getLineHeight () * (int) (rows + 0.8)));

            this.scrollPane = UIUtils.createScrollPane (this.text);
            this.add (this.scrollPane);
            this.scrollPane.setMinimumSize (this.getMinimumSize ());

        } else {

            this.add (this.text);
            this.setBorder (UIUtils.createLineBorderWithPadding (3, 3, 3, 3));

        }

        this.add (this.maxText);

        if (this.maxChars <= 0)
        {

            this.maxText.setVisible (false);

        }

    }

    @Override
    public void setToolTipText (String t)
    {

        this.text.setToolTipText (t);

    }

    @Override
    public Dimension getMaximumSize ()
    {

        Dimension p = this.getPreferredSize ();

        p.width = Short.MAX_VALUE;

        return p;

    }

    @Override
    public Dimension getMinimumSize ()
    {

        Dimension p = this.getPreferredSize ();

        p.width = 250;

        if (this.rows > 0)
        {

            int ph = this.text.getLineHeight () * (int) (this.rows + 0.8);

            p.height = ph;

        }

        return p;

    }

    public void fillPopupMenuForSpellCheck (final MouseEvent ev,
                                            final JPopupMenu popup)
    {

        final TextArea _this = this;

        Point p = ev.getPoint ();

        JMenuItem mi = null;

        if (p != null)
        {

            ActionAdapter addToDict = new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.text.addWordToDictionary (ev.getActionCommand ());

                    Environment.fireUserProjectEvent (ProjectEvent.PERSONAL_DICTIONARY,
                                                      ProjectEvent.ADD_WORD,
                                                      ev.getActionCommand ());

                }

            };

            TextIterator iter = new TextIterator (this.text.getText ());

            final Word w = iter.getWordAt (this.text.viewToModel (p));

            if (w != null)
            {

                final String word = w.getText ();

                final int loc = w.getAllTextStartOffset ();

                java.util.List l = this.text.getSpellCheckSuggestions (w);

                if (l != null)
                {

                    java.util.List<String> prefix = new ArrayList ();
                    prefix.add (LanguageStrings.dictionary);
                    prefix.add (LanguageStrings.spellcheck);
                    prefix.add (LanguageStrings.popupmenu);
                    prefix.add (LanguageStrings.items);

                    if (l.size () == 0)
                    {

                        mi = new JMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings.add));
                                            //"Add to Dictionary");
                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                        mi.setActionCommand (word);
                        mi.addActionListener (addToDict);

                        popup.add (mi,
                                   0);

                        mi = new JMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings.nosuggestions));
                                            //"(No Spelling Suggestions)");
                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                        mi.setEnabled (false);

                        popup.add (mi,
                                   0);

                    } else
                    {

                        JMenu more = new JMenu (Environment.getUIString (prefix,
                                                                         LanguageStrings.more));
                        //"More Suggestions");

                        int i = 0;

                        for (i = 0; i < l.size (); i++)
                        {

                            if (i == 5)
                            {

                                popup.add (more,
                                           5);

                            }

                            final String suggestion = (String) l.get (i);
                            //final String suggestion = ((com.swabunga.spell.engine.Word) l.get (i)).getWord ();

                            mi = new JMenuItem (suggestion);
                            mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                            mi.setActionCommand (mi.getText ());
                            mi.addActionListener (new ActionAdapter ()
                            {

                                public void actionPerformed (ActionEvent ev)
                                {

                                    String repWord = ev.getActionCommand ();

                                    _this.text.replaceText (loc,
                                                            loc + word.length (),
                                                            repWord);

                                    Environment.fireUserProjectEvent (ProjectEvent.SPELL_CHECK,
                                                                      ProjectEvent.REPLACE,
                                                                      ev.getActionCommand ());

                                }

                            });

                            if (i < 5)
                            {

                                popup.add (mi);
                                           //0);

                            } else
                            {

                                more.add (mi);

                            }

                        }

                        if (i > 5)
                        {

                            i = 6;

                        }

                        mi = new JMenuItem (Environment.getUIString (prefix,
                                                                     LanguageStrings.add));
                        //"Add to Dictionary");
                        mi.setActionCommand (word);
                        mi.addActionListener (addToDict);

                        popup.add (mi,
                                   i);

                    }

                } else {

                    if (this.text.getSynonymProvider () != null)
                    {

                        if ((word != null) &&
                            (word.length () > 0))
                        {

                            //String mt = "No synonyms found for: " + word;

                            try
                            {

                                // See if there are any synonyms.
                                if (this.text.getSynonymProvider ().hasSynonym (word))
                                {

                                    mi = new JMenuItem (String.format (Environment.getUIString (LanguageStrings.synonyms,
                                                                                                LanguageStrings.popupmenu,
                                                                                                LanguageStrings.items,
                                                                                                LanguageStrings.find),
                                                                       word));
                                    //"Find synonyms for: " + word);

                                    mi.setIcon (Environment.getIcon ("find",
                                                                     Constants.ICON_MENU));
                                    mi.addActionListener (new ActionListener ()
                                    {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            try
                                            {

                                                new FindSynonymsActionHandler (w,
                                                                               _this.text).showItem ();

                                            } catch (Exception e) {

                                                Environment.logError ("Unable to show synonyms selector for word: " +
                                                                      w,
                                                                      e);

                                                UIUtils.showErrorMessage (_this,
                                                                          Environment.getUIString (LanguageStrings.synonyms,
                                                                                                   LanguageStrings.show,
                                                                                                   LanguageStrings.actionerror));

                                            }

                                        }

                                    });

                                } else {

                                    mi = new JMenuItem (String.format (Environment.getUIString (LanguageStrings.synonyms,
                                                                                                LanguageStrings.popupmenu,
                                                                                                LanguageStrings.items,
                                                                                                LanguageStrings.nosynonyms),
                                                                       word));
                                                        //"(No synonyms for: " + word + ")");
                                    mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                                    mi.setEnabled (false);

                                }

                                popup.add (mi);

                            } catch (Exception e) {

                                Environment.logError ("Unable to determine whether word: " +
                                                      word +
                                                      " has synonyms.",
                                                      e);

                            }

                        }

                    }

                }

            }

        }

    }

    private void performAction (ActionEvent ev)
    {

        Action aa = (Action) this.text.getActionMap ().get (ev.getActionCommand ());

        if (aa != null)
        {

            aa.actionPerformed (ev);

        }

    }

    private JMenuItem createMenuItem (String label,
                                      String icon,
                                      String actionCommand)
    {

        final TextArea _this = this;

        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               new ActionListener ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.performAction (ev);

                                                    }

                                               });

        mi.setActionCommand (actionCommand);

        return mi;

    }

    private JMenuItem createMenuItem (String label,
                                      String icon,
                                      String         actionCommand,
                                      KeyStroke      accel)
    {

        JMenuItem mi = this.createMenuItem (label,
                                            icon,
                                            actionCommand);

        mi.setAccelerator (accel);

        return mi;


    }

    private JButton createButton (String         icon,
                                  int            iconType,
                                  String         toolTipText,
                                  String actionCommand)
    {

        final TextArea _this = this;

        JButton but = UIUtils.createButton (icon,
                                            iconType,
                                            toolTipText,
                                            new ActionListener ()
                                            {

                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     _this.performAction (ev);

                                                 }

                                            });

        but.setActionCommand (actionCommand);

        return but;

    }

    public void fillPopupMenu (final MouseEvent ev,
                               final JPopupMenu popup)
    {

        this.fillPopupMenuForSpellCheck (ev,
                                         popup);

        final TextArea _this = this;

        JMenuItem mi = null;

        String sel = this.text.getSelectedText ();

        boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);

        if (this.text.isCanFormat ())
        {

            if (!sel.equals (""))
            {

                if (popup.getComponentCount () > 0)
                {

                    popup.addSeparator ();

                }

                java.util.List<String> prefix = new ArrayList ();
                prefix.add (LanguageStrings.formatting);
                prefix.add (LanguageStrings.format);
                prefix.add (LanguageStrings.popupmenu);
                prefix.add (LanguageStrings.items);

                if (compress)
                {

                    java.util.List<JComponent> buts = new java.util.ArrayList ();
                    buts.add (this.createButton (Constants.BOLD_ICON_NAME,
                                                 Constants.ICON_MENU,
                                                 Environment.getUIString (prefix,
                                                                          LanguageStrings.bold,
                                                                          LanguageStrings.tooltip),
                                                 //"Bold the selected text",
                                                 QTextEditor.BOLD_ACTION_NAME));
                    buts.add (this.createButton (Constants.ITALIC_ICON_NAME,
                                                 Constants.ICON_MENU,
                                                 Environment.getUIString (prefix,
                                                                          LanguageStrings.italic,
                                                                          LanguageStrings.tooltip),
                                                 //"Italic the selected text",
                                                 QTextEditor.ITALIC_ACTION_NAME));
                    buts.add (this.createButton (Constants.UNDERLINE_ICON_NAME,
                                                 Constants.ICON_MENU,
                                                 Environment.getUIString (prefix,
                                                                          LanguageStrings.underline,
                                                                          LanguageStrings.tooltip),
                                                 //"Underline the selected text",
                                                 QTextEditor.UNDERLINE_ACTION_NAME));

                    popup.add (UIUtils.createPopupMenuButtonBar (Environment.getUIString (LanguageStrings.formatting,
                                                                                          LanguageStrings.format,
                                                                                          LanguageStrings.popupmenu,
                                                                                          LanguageStrings.title),
                                                                 //"Format",
                                                                 popup,
                                                                 buts));

                } else {

                    // Add the bold/italic/underline.
                    mi = this.createMenuItem (Environment.getUIString (prefix,
                                                                       LanguageStrings.bold,
                                                                       LanguageStrings.text),
                                            //"Bold",
                                              Constants.BOLD_ICON_NAME,
                                              QTextEditor.BOLD_ACTION_NAME,
                                              KeyStroke.getKeyStroke (KeyEvent.VK_B,
                                                                      ActionEvent.CTRL_MASK));
                    mi.setMnemonic (KeyEvent.VK_B);
                    mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                    popup.add (mi);

                    mi = this.createMenuItem (Environment.getUIString (prefix,
                                                                       LanguageStrings.italic,
                                                                       LanguageStrings.text),
                                              //"Italic",
                                              Constants.ITALIC_ICON_NAME,
                                              QTextEditor.ITALIC_ACTION_NAME,
                                              KeyStroke.getKeyStroke (KeyEvent.VK_I,
                                                                      ActionEvent.CTRL_MASK));
                    mi.setMnemonic (KeyEvent.VK_I);
                    mi.setFont (mi.getFont ().deriveFont (Font.ITALIC));
                    popup.add (mi);

                    mi = this.createMenuItem (Environment.getUIString (prefix,
                                                                       LanguageStrings.underline,
                                                                       LanguageStrings.text),
                                              //"Underline",
                                              Constants.UNDERLINE_ICON_NAME,
                                              QTextEditor.UNDERLINE_ACTION_NAME,
                                              KeyStroke.getKeyStroke (KeyEvent.VK_U,
                                                                      ActionEvent.CTRL_MASK));
                    mi.setMnemonic (KeyEvent.VK_U);
                    popup.add (mi);

                    java.util.Map attrs = mi.getFont ().getAttributes ();
                    attrs.put (TextAttribute.UNDERLINE,
                               TextAttribute.UNDERLINE_LOW_ONE_PIXEL);

                    mi.setFont (mi.getFont ().deriveFont (attrs));

                }

            }

        }

        if (popup.getComponentCount () > 0)
        {

            popup.addSeparator ();

        }

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.formatting);
        prefix.add (LanguageStrings.edit);
        prefix.add (LanguageStrings.popupmenu);
        prefix.add (LanguageStrings.items);

        if (compress)
        {

            java.util.List<JComponent> buts = new java.util.ArrayList ();

            // Only add if there is something to cut.
            if (!sel.equals (""))
            {

                buts.add (this.createButton (Constants.CUT_ICON_NAME,
                                             Constants.ICON_MENU,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.cut,
                                                                      LanguageStrings.tooltip),
                                             //"Cut the selected text",
                                             QTextEditor.CUT_ACTION_NAME));
                buts.add (this.createButton (Constants.COPY_ICON_NAME,
                                             Constants.ICON_MENU,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.copy,
                                                                      LanguageStrings.tooltip),
                                             //"Copy the selected text",
                                             QTextEditor.COPY_ACTION_NAME));

            }

            if (UIUtils.clipboardHasContent ())
            {

                buts.add (this.createButton (Constants.PASTE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.paste,
                                                                      LanguageStrings.tooltip),
                                             //"Paste",
                                             QTextEditor.PASTE_ACTION_NAME));

            }

            // Only add if there is an undo available.
            buts.add (this.createButton (Constants.UNDO_ICON_NAME,
                                         Constants.ICON_MENU,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.undo,
                                                                  LanguageStrings.tooltip),
                                         //"Undo",
                                         QTextEditor.UNDO_ACTION_NAME));
            buts.add (this.createButton (Constants.REDO_ICON_NAME,
                                         Constants.ICON_MENU,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.redo,
                                                                  LanguageStrings.tooltip),
                                         //"Redo",
                                         QTextEditor.REDO_ACTION_NAME));

            popup.add (UIUtils.createPopupMenuButtonBar (Environment.getUIString (LanguageStrings.formatting,
                                                                                  LanguageStrings.edit,
                                                                                  LanguageStrings.popupmenu,
                                                                                  LanguageStrings.title),
                                                         //"Edit",
                                                         popup,
                                                         buts));

        } else {

            if (!sel.equals (""))
            {

                mi = this.createMenuItem (Environment.getUIString (prefix,
                                                                   LanguageStrings.cut,
                                                                   LanguageStrings.text),
                                          //"Cut",
                                          Constants.CUT_ICON_NAME,
                                          QTextEditor.CUT_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_X,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_X);
                popup.add (mi);

                mi = this.createMenuItem (Environment.getUIString (prefix,
                                                                   LanguageStrings.copy,
                                                                   LanguageStrings.text),
                                          //"Copy",
                                          Constants.COPY_ICON_NAME,
                                          QTextEditor.COPY_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_C,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_C);
                popup.add (mi);

            }

            // Only show if there is something in the clipboard.
            if (UIUtils.clipboardHasContent ())
            {

                mi = this.createMenuItem (Environment.getUIString (prefix,
                                                                   LanguageStrings.paste,
                                                                   LanguageStrings.text),
                                          //"Paste",
                                          Constants.PASTE_ICON_NAME,
                                          QTextEditor.PASTE_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_V,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_V);

                popup.add (mi);

            }

            mi = this.createMenuItem (Environment.getUIString (prefix,
                                                               LanguageStrings.undo,
                                                               LanguageStrings.text),
                                      //"Undo",
                                      Constants.UNDO_ICON_NAME,
                                      QTextEditor.UNDO_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_Z,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_Z);
            popup.add (mi);

            mi = this.createMenuItem (Environment.getUIString (prefix,
                                                               LanguageStrings.redo,
                                                               LanguageStrings.text),
                                      //"Redo",
                                      Constants.REDO_ICON_NAME,
                                      QTextEditor.REDO_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_Y,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_Y);
            popup.add (mi);

        }

        this.fillPopupMenuForExtraItems (ev,
                                         popup,
                                         compress);

    }

    public void fillPopupMenuForExtraItems (MouseEvent ev,
                                            JPopupMenu popup,
                                            boolean    compress)
    {

        // Do nothing, just allows sub-classes to add extra items.

    }

    public void setSynonymProvider (SynonymProvider sp)
    {

        this.text.setSynonymProvider (sp);

    }

    public void setDictionaryProvider (DictionaryProvider2 dp)
    {

        this.text.setDictionaryProvider (dp);

    }

    public void setSpellCheckEnabled (boolean v)
    {

        this.spellCheckEnabled = v;

        if (this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
        {

            return;

        }

        this.text.setSpellCheckEnabled (v);

    }

    public void setCanFormat (boolean v)
    {

        this.text.setCanFormat (v);

    }

    private void setTextColorToNormal ()
    {

        Color c = new JLabel ().getForeground ();
        this.text.setFontColor (c);

    }

    private void updateMax ()
    {

        if (this.text.getFontColor ().equals (UIUtils.getHintTextColor ()))
        {

            return;

        }

        if (this.maxChars <= 0)
        {

            return;

        }

        this.maxText.setForeground (UIUtils.getHintTextColor ());

        int l = this.text.getText ().trim ().length ();

        String t = String.format (Environment.getUIString (LanguageStrings.textarea,
                                                           LanguageStrings.maxchars),
                                  Environment.formatNumber (this.maxChars));

        if (l > 0)
        {

            if (l > this.maxChars)
            {

                t += String.format (Environment.getUIString (LanguageStrings.textarea,
                                                             LanguageStrings.charsover),
                                    Environment.formatNumber (l - this.maxChars));
                this.maxText.setForeground (Color.RED);

            } else {

                t += String.format (Environment.getUIString (LanguageStrings.textarea,
                                                             LanguageStrings.charsremaining),
                                    Environment.formatNumber ((this.maxChars - l)));

            }

        }

        this.maxText.setText (t);

        this.validate ();
        this.repaint ();

    }

    public QTextEditor getEditor ()
    {

        return this.text;

    }

    @Override
    public void grabFocus ()
    {

        this.text.grabFocus ();

    }

    public void addDoActionOnCtrlReturnPressed (final ActionListener l)
    {

        if (l == null)
        {

            return;

        }

        this.text.addKeyListener (new KeyAdapter ()
        {

            public void keyPressed (KeyEvent ev)
            {

                if (((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) &&
                    (ev.getKeyCode () == KeyEvent.VK_ENTER))
                {

                    l.actionPerformed (new ActionEvent (text,
                                                             1,
                                                             "ctrl-return-pressed"));

                }

            }

        });

    }

    public boolean isAutoGrabFocus ()
    {

        return this.autoGrabFocus;

    }

    public void setAutoGrabFocus (boolean v)
    {

        this.autoGrabFocus = v;

    }

    public JScrollPane getScrollPane ()
    {

        return this.scrollPane;

    }

    public String getText ()
    {

        if (this.placeholder != null)
        {

            if (this.text.getText ().equals (this.placeholder))
            {

                return "";

            }

        }

        return this.text.getText ();

    }

    public StringWithMarkup getTextWithMarkup ()
    {

        if (this.placeholder != null)
        {

            if (this.text.getText ().equals (this.placeholder))
            {

                return null;

            }

        }

        return this.text.getTextWithMarkup ();

    }

    public void clearText ()
    {

        this.setText ((StringWithMarkup) null);

    }

    public void setText (String  t)
    {

        this.setText (new StringWithMarkup (t));

    }

    public void setText (StringWithMarkup t)
    {

        this.text.setTextWithMarkup (t);

        this.text.setFontColor (new JLabel ().getForeground ());

        this.setSpellCheckEnabled (this.spellCheckEnabled);

    }

    public void updateText (String t)
    {

        this.updateText (new StringWithMarkup (t));

    }

    public void updateText (StringWithMarkup t)
    {

        this.text.replaceText (t.getText ());

    }

    /**
     * Pass through to the underlying QTextEditor.
     *
     * @param l The listener.
     */
    @Override
    public void addKeyListener (KeyListener l)
    {

        this.text.addKeyListener (l);

    }

    /**
     * Pass through to the underlying QTextEditor.
     *
     * @param l The listener.
     */
    @Override
    public void removeKeyListener (KeyListener l)
    {

        this.text.removeKeyListener (l);

    }

    /**
     * Pass through to the underlying QTextEditor.
     *
     * @param l The listener.
     */
    @Override
    public void addMouseListener (MouseListener l)
    {

        this.text.addMouseListener (l);

    }

    /**
     * Pass through to the underlying QTextEditor.
     *
     * @param l The listener.
     */
    @Override
    public void removeMouseListener (MouseListener l)
    {

        this.text.removeMouseListener (l);

    }

    public void removeCaretListener (CaretListener l)
    {

        this.text.removeCaretListener (l);

    }

    public void addCaretListener (CaretListener l)
    {

        this.text.addCaretListener (l);

    }

    @Override
    public void setBorder (Border b)
    {

        if (this.scrollPane != null)
        {

            this.scrollPane.setBorder (b);

        } else {

            this.text.setBorder (b);

        }

    }

}
