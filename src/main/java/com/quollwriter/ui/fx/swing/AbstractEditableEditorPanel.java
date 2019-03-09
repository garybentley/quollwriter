package com.quollwriter.ui.fx.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.text.*;
import com.quollwriter.synonyms.*;
/*
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.DocumentAdapter;
import com.quollwriter.ui.components.StyleChangeAdapter;
import com.quollwriter.ui.components.StyleChangeEvent;
import com.quollwriter.ui.components.StyleChangeListener;
*/
import com.quollwriter.ui.fx.State;
import com.quollwriter.ui.fx.ProjectEvent;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

public abstract class AbstractEditableEditorPanel<E extends AbstractProjectViewer> extends AbstractEditorPanel<E>
{

    public static final String SAVE_ACTION_NAME = "save";
    public static final String INSERT_SECTION_BREAK_ACTION_NAME = "insert-section-break";
    public static final String DELETE_CHAPTER_ACTION_NAME = "delete-chapter";

    public AbstractEditableEditorPanel (E       pv,
                                        Chapter c)
                                 throws GeneralException
    {

        super (pv,
               c);

        final AbstractEditableEditorPanel _this = this;

        this.editor.setSectionBreak (Constants.SECTION_BREAK);

/*
 TODO
        this.actions.put (DELETE_CHAPTER_ACTION_NAME,
                          new DeleteChapterActionHandler ((Chapter) this.obj,
                                                                    this.viewer));
*/
        this.actions.put (SAVE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.saveChapter ();

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to save chapter",
                                                            e);

                                  }

                              }

                          });

        this.actions.put (INSERT_SECTION_BREAK_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.insertSectionBreak ();

                                  } catch (Exception e)
                                  {

                                      // Ignore.

                                  }

                              }

                          });

        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                        InputEvent.CTRL_DOWN_MASK),
                SAVE_ACTION_NAME);

        im = this.editor.getInputMap (JComponent.WHEN_FOCUSED);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER,
                                        InputEvent.CTRL_DOWN_MASK),
                INSERT_SECTION_BREAK_ACTION_NAME);

    }

    public abstract void doFillPopupMenu (MouseEvent eve,
                                          JPopupMenu p,
                                          boolean    compress);

    @Override
    public void close ()
    {

        super.close ();

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        final AbstractEditableEditorPanel _this = this;
        /* TODO: Remove merged.
<<<<<<< HEAD
=======

        this.chapterInfo = new javax.swing.Timer (2000,
                                      new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                new Thread (new Runnable ()
                {

                    public void run ()
                    {

                        try
                        {

                            Thread t = Thread.currentThread ();
                            t.setName ("Chapter counts for: " + _this.chapter.getName ());
                            t.setPriority (Thread.MIN_PRIORITY);

                        } catch (Exception e) {

                            // Ignore.

                        }

                        try
                        {

                            ChapterCounts cc = new ChapterCounts (_this.editor.getText ());
                            cc.a4PageCount = UIUtils.getA4PageCountForChapter (_this.chapter,
                                                                               _this.editor.getText ());

                            _this.setChapterCounts (cc);

                            _this.setReadabilityIndices (_this.projectViewer.getReadabilityIndices (_this.editor.getText ()));

                            _this.chapterInfoTimerLastRun = System.currentTimeMillis ();

                        } catch (Exception e) {

                            Environment.logError ("Unable to get chapter counts/readability for chapter: " +
                                                  _this.chapter,
                                                  e);

                        }

                    }

                }).start ();

            }

        });

        this.chapterInfo.setRepeats (false);
        this.chapterInfo.start ();
>>>>>>> refs/remotes/origin/master
        */
        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        doc.addDocumentListener (new DocumentAdapter ()
        {

            public void insertUpdate (DocumentEvent ev)
            {

                final int offset = ev.getOffset ();

                if (ev.getLength () > 0)
                {

                    // TODO Check that any dependent achievements still work.
                    _this.viewer.fireProjectEvent (ProjectEvent.Type.projectobject,
                                                   ProjectEvent.Action.edit,
                                                   _this.chapter);

                }

                boolean add = false;

                try
                {

                    if (ev.getLength () == 1)
                    {

                        if (offset == 0)
                        {

                            Set<OutlineItem> its = _this.chapter.getOutlineItemsAt (0);

                            for (OutlineItem it : its)
                            {

                                it.setTextPosition (editor.getDocument ().createPosition (it.getPosition () + 1));

                            }

                            Set<Scene> ss = _this.chapter.getScenesAt (0);

                            for (Scene s : ss)
                            {

                                s.setTextPosition (editor.getDocument ().createPosition (s.getPosition () + 1));

                            }

                            Set<Note> nn = _this.chapter.getNotesAt (0);

                            for (Note n : nn)
                            {

                                n.setTextPosition (editor.getDocument ().createPosition (n.getPosition () + 1));

                            }

                        }

                        String t = doc.getText (offset,
                                                ev.getLength ());

                        String nl = String.valueOf ('\n');

                        if (t.equals (nl))
                        {

                            String te = doc.getText (offset - Constants.SECTION_BREAK_FIND.length (),
                                                     Constants.SECTION_BREAK_FIND.length ());

                            if (te.equals (Constants.SECTION_BREAK_FIND))
                            {

                                add = true;

                            }

                            if (doc.getLogicalStyle (offset) == _this.editor.sectionBreakStyle)
                            {

                                SwingUIUtils.doLater (new ActionListener ()
                                {

                                    public void actionPerformed (ActionEvent ev)
                                    {

                                        Style ls = doc.addStyle (null,
                                                                 null);
                                        StyleConstants.setAlignment (ls,
                                                                     StyleConstants.ALIGN_LEFT);

                                        doc.setParagraphAttributes (offset + 1,
                                                                    1,
                                                                    ls,
                                                                    false);

                                    }

                                });

                            }

                        }

                    }

                } catch (Exception e)
                {

                    // Ignore.

                }

                if (add)
                {

                    SwingUIUtils.doLater (new ActionListener ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            try
                            {

                                String ins = String.valueOf ('\n') + String.valueOf ('\n') + Constants.SECTION_BREAK + String.valueOf ('\n') + String.valueOf ('\n');

                                doc.replace (offset - Constants.SECTION_BREAK_FIND.length (),
                                             Constants.SECTION_BREAK_FIND.length () + 1,
                                             ins,
                                             _this.editor.sectionBreakStyle);

                                doc.setParagraphAttributes (offset + 2,
                                                            Constants.SECTION_BREAK.length (),
                                                            _this.editor.sectionBreakStyle,
                                                            false);

                                doc.setLogicalStyle (offset + 2,
                                                     _this.editor.sectionBreakStyle);

                                Style ls = doc.addStyle (null,
                                                         null);
                                StyleConstants.setAlignment (ls,
                                                             StyleConstants.ALIGN_LEFT);

                                doc.setParagraphAttributes (offset + Constants.SECTION_BREAK.length (),
                                                            2,
                                                            ls,
                                                            false);

                            } catch (Exception e)
                            {

                                Environment.logError ("Unable to add section breaks",
                                                      e);

                            }

                        }

                    });

                }

            }

            public void removeUpdate (DocumentEvent ev)
            {

                if (ev.getLength () > 0)
                {

                    _this.viewer.fireProjectEvent (ProjectEvent.Type.projectobject,
                                                   ProjectEvent.Action.edit,
                                                   _this.chapter);

                }

                final int offset = ev.getOffset ();

                if (doc.getLogicalStyle (offset) == _this.editor.sectionBreakStyle)
                {

                    SwingUIUtils.doLater (new ActionListener ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            try
                            {

                                doc.replace (offset - Constants.SECTION_BREAK_FIND.length (),
                                             Constants.SECTION_BREAK_FIND.length (),
                                             null,
                                             null);

                            } catch (Exception e)
                            {

                            }

                        }

                    });

                }

            }

        });

        this.editor.getDocument ().addDocumentListener (new DocumentAdapter ()
        {

            public void changedUpdate (DocumentEvent ev)
            {

                if (_this.isIgnoreDocumentChanges ())
                {

                    return;

                }

                _this.setHasUnsavedChanges (true);

            }

            public void insertUpdate (DocumentEvent ev)
            {

                if (_this.isIgnoreDocumentChanges ())
                {

                    return;

                }

                _this.setHasUnsavedChanges (true);

            }

            public void removeUpdate (DocumentEvent ev)
            {

                if (_this.isIgnoreDocumentChanges ())
                {

                    return;

                }

                _this.setHasUnsavedChanges (true);

            }

        });

    }

    public void insertSectionBreak ()
    {

        final AbstractEditorPanel _this = this;

        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        final int offset = this.editor.getCaret ().getDot ();

        SwingUIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    _this.editor.startCompoundEdit ();

                    String ins = String.valueOf ('\n') + String.valueOf ('\n') + Constants.SECTION_BREAK + String.valueOf ('\n') + String.valueOf ('\n');

                    doc.insertString (offset,
                                      ins,
                                      _this.editor.sectionBreakStyle);

                    doc.setParagraphAttributes (offset + 2,
                                                Constants.SECTION_BREAK.length (),
                                                _this.editor.sectionBreakStyle,
                                                false);

                    doc.setLogicalStyle (offset + 2,
                                         _this.editor.sectionBreakStyle);

                    _this.editor.endCompoundEdit ();

                } catch (Exception e)
                {

                }

            }

        });

    }

    @Override
    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        this.saveObject ();

        return true;

    }

    public void saveChapter ()
                      throws Exception
    {

        this.saveObject ();

    }

    public void saveObject ()
                     throws Exception
    {

        this.chapter.setText (this.editor.getTextWithMarkup ());

        super.saveObject ();

    }

    // TODO: Merge with TextArea.fillPopupMenu
    private void addFormatItemsToPopupMenu (JPopupMenu popup,
                                            boolean    compress)
    {

        String sel = this.editor.getSelectedText ();

        if (!sel.equals (""))
        {

            java.util.List<String> prefix = new ArrayList<> ();
            prefix.add (LanguageStrings.formatting);
            prefix.add (LanguageStrings.format);
            prefix.add (LanguageStrings.popupmenu);
            prefix.add (LanguageStrings.items);

            if (compress)
            {

                List<JComponent> buts = new ArrayList<> ();
                buts.add (this.createButton (Constants.BOLD_ICON_NAME,
                                             Constants.ICON_MENU,
                                             getUIString (prefix,
                                                                      LanguageStrings.bold,
                                                                      LanguageStrings.tooltip),
                                             //"Bold the selected text",
                                             QTextEditor.BOLD_ACTION_NAME));
                buts.add (this.createButton (Constants.ITALIC_ICON_NAME,
                                             Constants.ICON_MENU,
                                             getUIString (prefix,
                                                                      LanguageStrings.italic,
                                                                      LanguageStrings.tooltip),
                                             //"Italic the selected text",
                                             QTextEditor.ITALIC_ACTION_NAME));
                buts.add (this.createButton (Constants.UNDERLINE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             getUIString (prefix,
                                                                      LanguageStrings.underline,                                                                          LanguageStrings.tooltip),
                                             //"Underline the selected text",
                                             QTextEditor.UNDERLINE_ACTION_NAME));

                popup.add (SwingUIUtils.createPopupMenuButtonBar (getUIString (LanguageStrings.formatting,
                                                                                      LanguageStrings.format,
                                                                                      LanguageStrings.popupmenu,
                                                                                      LanguageStrings.title),
                                                             //"Format",
                                                             popup,
                                                             buts));

            } else {

                JMenuItem mi = null;

                popup.addSeparator ();

                // Add the bold/italic/underline.
                mi = this.createMenuItem (getUIString (prefix,
                                                                   LanguageStrings.bold,
                                                                   LanguageStrings.text),
                                          //"Bold",
                                          Constants.BOLD_ICON_NAME,
                                          QTextEditor.BOLD_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_B,
                                                                  InputEvent.CTRL_DOWN_MASK));
                mi.setMnemonic (KeyEvent.VK_B);
                mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                popup.add (mi);

                mi = this.createMenuItem (getUIString (prefix,
                                                                   LanguageStrings.italic,
                                                                   LanguageStrings.text),
                                          //"Italic",
                                          Constants.ITALIC_ICON_NAME,
                                          QTextEditor.ITALIC_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_I,
                                                                  InputEvent.CTRL_DOWN_MASK));
                mi.setMnemonic (KeyEvent.VK_I);
                mi.setFont (mi.getFont ().deriveFont (Font.ITALIC));
                popup.add (mi);

                mi = this.createMenuItem (getUIString (prefix,
                                                                   LanguageStrings.underline,
                                                                   LanguageStrings.text),
                                          //"Underline",
                                          Constants.UNDERLINE_ICON_NAME,
                                          QTextEditor.UNDERLINE_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_U,
                                                                  InputEvent.CTRL_DOWN_MASK));
                mi.setMnemonic (KeyEvent.VK_U);
                popup.add (mi);

                Map attrs = mi.getFont ().getAttributes ();
                attrs.put (TextAttribute.UNDERLINE,
                           TextAttribute.UNDERLINE_LOW_ONE_PIXEL);

                mi.setFont (mi.getFont ().deriveFont (attrs));

            }

        }

    }

    // TODO: Merge with TextArea.fillPopupMenu.
    private void addEditItemsToPopupMenu (JPopupMenu popup,
                                          boolean    compress)
    {

        JMenuItem mi = null;

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.formatting);
        prefix.add (LanguageStrings.edit);
        prefix.add (LanguageStrings.popupmenu);
        prefix.add (LanguageStrings.items);

        String sel = this.editor.getSelectedText ();

        if (compress)
        {

            List<JComponent> buts = new ArrayList<> ();

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

            if (SwingUIUtils.clipboardHasContent ())
            {

                buts.add (this.createButton (Constants.PASTE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             getUIString (prefix,
                                                                      LanguageStrings.paste,
                                                                      LanguageStrings.tooltip),
                                             //"Paste",
                                             QTextEditor.PASTE_ACTION_NAME));

            }

            // Only add if there is an undo available.
            buts.add (this.createButton (Constants.UNDO_ICON_NAME,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  LanguageStrings.undo,
                                                                  LanguageStrings.tooltip),
                                         //"Undo",
                                         QTextEditor.UNDO_ACTION_NAME));
            buts.add (this.createButton (Constants.REDO_ICON_NAME,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  LanguageStrings.redo,
                                                                  LanguageStrings.tooltip),
                                         //"Redo",
                                         QTextEditor.REDO_ACTION_NAME));

            popup.add (SwingUIUtils.createPopupMenuButtonBar (getUIString (LanguageStrings.formatting,
                                                                                  LanguageStrings.edit,
                                                                                  LanguageStrings.popupmenu,
                                                                                  LanguageStrings.title),
                                                         //"Edit",
                                                         popup,
                                                         buts));

        } else {

            popup.addSeparator ();
/*
            mi = this.createMenuItem ("Find",
                                      Constants.FIND_ICON_NAME,
                                      Constants.SHOW_FIND_ACTION,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                              ActionEvent.CTRL_DOWN_MASK));
            mi.setMnemonic (KeyEvent.VK_F);
            popup.add (mi);
*/
            if (!sel.equals (""))
            {

                mi = this.createMenuItem (getUIString (prefix,
                                                                   LanguageStrings.cut,
                                                                   LanguageStrings.text),
                                          //"Cut",
                                          Constants.CUT_ICON_NAME,
                                          QTextEditor.CUT_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_X,
                                                                  InputEvent.CTRL_DOWN_MASK));
                mi.setMnemonic (KeyEvent.VK_X);
                popup.add (mi);

                mi = this.createMenuItem (getUIString (prefix,
                                                                   LanguageStrings.copy,
                                                                   LanguageStrings.text),
                                          //"Copy",
                                          Constants.COPY_ICON_NAME,
                                          QTextEditor.COPY_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_C,
                                                                  InputEvent.CTRL_DOWN_MASK));
                mi.setMnemonic (KeyEvent.VK_C);
                popup.add (mi);

            }

            // Only show if there is something in the clipboard.
            if (SwingUIUtils.clipboardHasContent ())
            {

                mi = this.createMenuItem (getUIString (prefix,
                                                                   LanguageStrings.paste,
                                                                   LanguageStrings.text),
                                          //"Paste",
                                          Constants.PASTE_ICON_NAME,
                                          QTextEditor.PASTE_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_V,
                                                                  InputEvent.CTRL_DOWN_MASK));
                mi.setMnemonic (KeyEvent.VK_V);

                popup.add (mi);

            }

            mi = this.createMenuItem (getUIString (prefix,
                                                               LanguageStrings.undo,
                                                               LanguageStrings.text),
                                      //"Undo",
                                      Constants.UNDO_ICON_NAME,
                                      QTextEditor.UNDO_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_Z,
                                                              InputEvent.CTRL_DOWN_MASK));
            mi.setMnemonic (KeyEvent.VK_Z);
            popup.add (mi);

            mi = this.createMenuItem (getUIString (prefix,
                                                               LanguageStrings.redo,
                                                               LanguageStrings.text),
                                      //"Redo",
                                      Constants.REDO_ICON_NAME,
                                      QTextEditor.REDO_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_Y,
                                                              InputEvent.CTRL_DOWN_MASK));
            mi.setMnemonic (KeyEvent.VK_Y);
            popup.add (mi);

        }

    }

    public void fillPopupMenu (final MouseEvent ev,
                               final JPopupMenu popup)
    {

        final QTextEditor         editor = this.editor;
        final AbstractEditableEditorPanel _this = this;

        Point p = this.editor.getMousePosition ();

        this.lastMousePosition = p;

        JMenuItem mi = null;

        if (p != null)
        {

            ActionAdapter addToDict = new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    editor.addWordToDictionary (ev.getActionCommand ());

                    _this.viewer.fireProjectEvent (ProjectEvent.Type.personaldictionary,
                                                   ProjectEvent.Action.addword,
                                                   ev.getActionCommand ());

                }

            };

            TextIterator iter = new TextIterator (this.editor.getText ());

            final Word w = iter.getWordAt (this.editor.viewToModel2D (p));

            if (w != null)
            {

                final String word = w.getText ();

                final int loc = w.getAllTextStartOffset ();

                java.util.List l = this.editor.getSpellCheckSuggestions (w);

                if (l != null)
                {

                    java.util.List<String> prefix = new ArrayList<> ();
                    prefix.add (LanguageStrings.dictionary);
                    prefix.add (LanguageStrings.spellcheck);
                    prefix.add (LanguageStrings.popupmenu);
                    prefix.add (LanguageStrings.items);

                    if (l.size () == 0)
                    {

                        mi = new JMenuItem (getUIString (prefix,
                                                                     LanguageStrings.add));
                        //"Add to Dictionary");
                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                        mi.setActionCommand (word);
                        mi.addActionListener (addToDict);

                        popup.add (mi,
                                   0);

                        mi = new JMenuItem (getUIString (prefix,
                                                                     LanguageStrings.nosuggestions));
                        //"(No Spelling Suggestions)");
                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                        mi.setEnabled (false);

                        popup.add (mi,
                                   0);

                    } else
                    {

                        JMenu more = new JMenu (getUIString (prefix,
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

                                    _this.editor.replaceText (loc,
                                                              loc + word.length (),
                                                              repWord);

                                    _this.viewer.fireProjectEvent (ProjectEvent.Type.spellcheck,
                                                                   ProjectEvent.Action.replace,
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

                    popup.addSeparator ();

                } else
                {

                    if ((this.viewer.synonymLookupsSupported ()) &&
                        (ev.getSource () == this.editor))
                    {

                        // TODO Check this...
                        if (_this.viewer.isLanguageFunctionAvailable ())
                        {

                            if ((word != null) &&
                                (word.length () > 0))
                            {

                                //String mt = "No synonyms found for: " + word;

                                try
                                {

                                    // See if there are any synonyms.
                                    if (this.editor.getSynonymProvider ().hasSynonym (word))
                                    {

                                        mi = new JMenuItem (String.format (Environment.getUIString (LanguageStrings.synonyms,
                                                                                                    LanguageStrings.popupmenu,
                                                                                                    LanguageStrings.items,
                                                                                                    LanguageStrings.find),
                                                                           word));
                                        //mi = new JMenuItem ("Find synonyms for: " + word);

                                        mi.setIcon (Environment.getIcon ("find",
                                                                         Constants.ICON_MENU));

/*
TODO
                                        mi.addActionListener (new FindSynonymsActionHandler (w, _this.editor));
                                        */
                                        /*
                                        word,
                                                                                             loc, // c
                                                                                             this.getChapter (),
                                                                                             _this));
*/
                                    } else {

                                        mi = new JMenuItem (String.format (Environment.getUIString (LanguageStrings.synonyms,
                                                                                                    LanguageStrings.popupmenu,
                                                                                                    LanguageStrings.items,
                                                                                                    LanguageStrings.nosynonyms),
                                                                           word));
                                        //mi = new JMenuItem ("(No synonyms for: " + word + ")");
                                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                                        mi.setEnabled (false);

                                    }

                                    popup.add (mi);

                                    popup.addSeparator ();

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

        boolean compress = UserProperties.getAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);

        this.doFillPopupMenu (ev,
                              popup,
                              compress);

        this.addFormatItemsToPopupMenu (popup,
                                        compress);

        this.addEditItemsToPopupMenu (popup,
                                      compress);

    }

}
