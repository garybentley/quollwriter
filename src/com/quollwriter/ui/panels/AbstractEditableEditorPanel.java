package com.quollwriter.ui.panels;

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
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.DocumentAdapter;
import com.quollwriter.ui.components.StyleChangeAdapter;
import com.quollwriter.ui.components.StyleChangeEvent;
import com.quollwriter.ui.components.StyleChangeListener;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.Runner;
import com.quollwriter.ui.components.TextStylable;
import com.quollwriter.ui.components.TextProperties;

public abstract class AbstractEditableEditorPanel extends AbstractEditorPanel
{

    public static final String SAVE_ACTION_NAME = "save";
    public static final String INSERT_SECTION_BREAK_ACTION_NAME = "insert-section-break";
    public static final String DELETE_CHAPTER_ACTION_NAME = "delete-chapter";
    
    protected javax.swing.Timer          chapterInfo = null;
    private   long chapterInfoTimerLastRun = -1;
    //protected java.util.List actionListeners = new ArrayList ();
    
    public AbstractEditableEditorPanel (AbstractProjectViewer pv,
                                        Chapter               c)
                                 throws GeneralException
    {

        super (pv,
               c);

        this.chapter = c;

        final AbstractEditableEditorPanel _this = this;
        
        this.editor.setSectionBreak (Constants.SECTION_BREAK);
        
        this.actions.put (DELETE_CHAPTER_ACTION_NAME,
                          new DeleteChapterActionHandler ((Chapter) this.obj,
                                                                    this.projectViewer));

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

                                      UIUtils.showErrorMessage (_this,
                                                                "Unable to save chapter.");

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
                                        Event.CTRL_MASK),
                SAVE_ACTION_NAME);

        im = this.editor.getInputMap (JComponent.WHEN_FOCUSED);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER,
                                        Event.CTRL_MASK),
                INSERT_SECTION_BREAK_ACTION_NAME);
                                
    }
        
    public abstract void doFillToolBar (JToolBar b);

    public abstract void doFillPopupMenu (MouseEvent eve,
                                          JPopupMenu p,
                                          boolean    compress);

    public abstract void doFillToolsPopupMenu (ActionEvent eve,
                                               JPopupMenu  p);            
        
    @Override
    public void close ()
    {
        
        super.close ();
        
        if (this.chapterInfo != null)
        {
        
            this.chapterInfo.stop ();
            
        }
                                
    }
    
    @Override
    public void init ()
               throws GeneralException
    {

        super.init ();
    
        final AbstractEditableEditorPanel _this = this;

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
                            t.setName ("Chapter counts for: " + _this.chapter);
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
        
        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();
        
        doc.addDocumentListener (new DocumentAdapter ()
        {

            public void insertUpdate (DocumentEvent ev)
            {

                final int offset = ev.getOffset ();

                if (ev.getLength () > 0)
                {
                    
                    _this.restartChapterInfoTimer ();
    
                    _this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                          ProjectEvent.EDIT,
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

                                UIUtils.doLater (new ActionListener ()
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

                    UIUtils.doLater (new ActionListener ()
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
                    
                    _this.restartChapterInfoTimer ();
    
                    _this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                          ProjectEvent.EDIT,
                                                          _this.chapter);
                    
                }                    

                final int offset = ev.getOffset ();

                if (doc.getLogicalStyle (offset) == _this.editor.sectionBreakStyle)
                {

                    UIUtils.doLater (new ActionListener ()
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
        
    private void restartChapterInfoTimer ()
    {
        
        if ((System.currentTimeMillis () - this.chapterInfoTimerLastRun) > 10000)
        {
            
            // Been more than 10s, so force run.
            this.chapterInfo.start ();
            
            return;
            
        } else {
            
            this.chapterInfo.stop ();
            this.chapterInfo.start ();
            
        }
        
    }

    public void insertSectionBreak ()
    {

        final AbstractEditorPanel _this = this;

        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        final int offset = this.editor.getCaret ().getDot ();

        UIUtils.doLater (new ActionListener ()
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
    
    public void fillToolBar (JToolBar acts,
                             final boolean  fullScreen)
    {

        final AbstractEditableEditorPanel _this = this;

        acts.add (this.createToolbarButton (Constants.SAVE_ICON_NAME,
                                            "Click to save the {Chapter} text",
                                            SAVE_ACTION_NAME));

        this.doFillToolBar (acts);

        acts.add (this.createToolbarButton (Constants.WORDCOUNT_ICON_NAME,
                                            "Click to view the word counts and readability indices",
                                            TOGGLE_WORDCOUNTS_ACTION_NAME));

        String type = (this.projectViewer.isSpellCheckingEnabled () ? "off" : "on");

        acts.add (this.createToolbarButton ("spellchecker-turn-" + type,
                                            "Click to turn the spell checker " + type,
                                            TOGGLE_SPELLCHECK_ACTION_NAME));

        acts.add (this.createToolbarButton (Constants.DELETE_ICON_NAME,
                                            "Click to delete this {Chapter}",
                                            DELETE_CHAPTER_ACTION_NAME));

        // Add a tools menu.
        final JButton b = UIUtils.createToolBarButton ("tools",
                                                       "Click to view the tools such as Print, Find Problems and Edit the text properties",
                                                       "tools",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu m = new JPopupMenu ();

                _this.doFillToolsPopupMenu (ev,
                                            m);

                JMenuItem mi = null;

                m.add (_this.createMenuItem ("Edit Text Properties",
                                             Constants.EDIT_PROPERTIES_ICON_NAME,
                                             EDIT_TEXT_PROPERTIES_ACTION_NAME,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                                                     ActionEvent.CTRL_MASK)));
                        
                m.add (_this.createMenuItem ("Find",
                                             Constants.FIND_ICON_NAME,
                                             Constants.SHOW_FIND_ACTION,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                                     ActionEvent.CTRL_MASK)));

                m.add (_this.createMenuItem ("Print {Chapter}",
                                             Constants.PRINT_ICON_NAME,
                                             QTextEditor.PRINT_ACTION_NAME,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                                                     ActionEvent.CTRL_MASK)));
                                                                       
                m.show (b,
                        10,
                        10);

            }

        };

        b.addActionListener (ab);

        acts.add (b);

    }

    public void saveObject ()
                     throws Exception
    {

        this.chapter.setText (this.editor.getTextWithMarkup ());

        super.saveObject ();

    }

    private void addFormatItemsToPopupMenu (JPopupMenu popup,
                                            boolean    compress)
    {
        
        String sel = this.editor.getSelectedText ();

        if (!sel.equals (""))
        {

            if (compress)
            {
            
                List<JComponent> buts = new ArrayList ();
                buts.add (this.createButton (Constants.BOLD_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Bold the selected text",
                                             QTextEditor.BOLD_ACTION_NAME));
                buts.add (this.createButton (Constants.ITALIC_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Italic the selected text",
                                             QTextEditor.ITALIC_ACTION_NAME));
                buts.add (this.createButton (Constants.UNDERLINE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Underline the selected text",
                                             QTextEditor.UNDERLINE_ACTION_NAME));
                            
                popup.add (UIUtils.createPopupMenuButtonBar ("Format",
                                                             popup,
                                                             buts));

            } else {

                JMenuItem mi = null;
            
                popup.addSeparator ();
            
                // Add the bold/italic/underline.
                mi = this.createMenuItem ("Bold",
                                          Constants.BOLD_ICON_NAME,
                                          QTextEditor.BOLD_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_B,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_B);
                mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                popup.add (mi);
    
                mi = this.createMenuItem ("Italic",
                                          Constants.ITALIC_ICON_NAME,
                                          QTextEditor.ITALIC_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_I,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_I);
                mi.setFont (mi.getFont ().deriveFont (Font.ITALIC));
                popup.add (mi);
    
                mi = this.createMenuItem ("Underline",
                                          Constants.UNDERLINE_ICON_NAME,
                                          QTextEditor.UNDERLINE_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_U,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_U);
                popup.add (mi);                                      
            
                Map attrs = mi.getFont ().getAttributes ();
                attrs.put (TextAttribute.UNDERLINE,
                           TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
    
                mi.setFont (mi.getFont ().deriveFont (attrs));

            }
            
        }
        
    }
    
    private void addEditItemsToPopupMenu (JPopupMenu popup,
                                          boolean    compress)
    {

        JMenuItem mi = null;
    
        String sel = this.editor.getSelectedText ();
        
        if (compress)
        {
        
            List<JComponent> buts = new ArrayList ();
    
            // Only add if there is something to cut.
            if (!sel.equals (""))
            {
    
                buts.add (this.createButton (Constants.CUT_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Cut the selected text",
                                             QTextEditor.CUT_ACTION_NAME));
                buts.add (this.createButton (Constants.COPY_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Copy the selected text",
                                             QTextEditor.COPY_ACTION_NAME));
        
            }

            if (UIUtils.clipboardHasContent ())
            {
    
                buts.add (this.createButton (Constants.PASTE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Paste",
                                             QTextEditor.PASTE_ACTION_NAME));        
        
            }
            
            // Only add if there is an undo available.
            buts.add (this.createButton (Constants.UNDO_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Undo",
                                         QTextEditor.UNDO_ACTION_NAME));
            buts.add (this.createButton (Constants.REDO_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Redo",
                                         QTextEditor.REDO_ACTION_NAME));
            
            popup.add (UIUtils.createPopupMenuButtonBar ("Edit",
                                                         popup,
                                                         buts));

        } else {           

            popup.addSeparator ();
        
            mi = this.createMenuItem ("Find",
                                      Constants.FIND_ICON_NAME,
                                      Constants.SHOW_FIND_ACTION,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_F);
            popup.add (mi);                                      
        
            if (!sel.equals (""))
            {

                mi = this.createMenuItem ("Cut",
                                          Constants.CUT_ICON_NAME,
                                          QTextEditor.CUT_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_X,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_X);
                popup.add (mi);
                
                mi = this.createMenuItem ("Copy",
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

                mi = this.createMenuItem ("Paste",
                                          Constants.PASTE_ICON_NAME,
                                          QTextEditor.PASTE_ACTION_NAME,
                                          KeyStroke.getKeyStroke (KeyEvent.VK_V,
                                                                  ActionEvent.CTRL_MASK));
                mi.setMnemonic (KeyEvent.VK_V);

                popup.add (mi);
    
            }
    
            mi = this.createMenuItem ("Undo",
                                      Constants.UNDO_ICON_NAME,
                                      QTextEditor.UNDO_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_Z,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_Z);
            popup.add (mi);
            
            mi = this.createMenuItem ("Redo",
                                      Constants.REDO_ICON_NAME,
                                      QTextEditor.REDO_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_Y,
                                                              ActionEvent.CTRL_MASK));
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

                    _this.projectViewer.fireProjectEvent (ProjectEvent.PERSONAL_DICTIONARY,
                                                          ProjectEvent.ADD_WORD,
                                                          ev.getActionCommand ());

                }

            };

            TextIterator iter = new TextIterator (this.editor.getText ());
            
            final Word w = iter.getWordAt (this.editor.viewToModel (p));
            
            if (w != null)
            {
                
                final String word = w.getText ();
            
                final int loc = w.getAllTextStartOffset ();
    
                java.util.List l = this.editor.getSpellCheckSuggestions (word);
    
                if (l != null)
                {
    
                    if (l.size () == 0)
                    {
    
                        mi = new JMenuItem ("Add to Dictionary");
                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                        mi.setActionCommand (word);
                        mi.addActionListener (addToDict);
    
                        popup.add (mi,
                                   0);
    
                        mi = new JMenuItem ("(No Spelling Suggestions)");
                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                        mi.setEnabled (false);
    
                        popup.add (mi,
                                   0);
    
                    } else
                    {
    
                        JMenu more = new JMenu ("More Suggestions");
    
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
                                
                                    _this.projectViewer.fireProjectEvent (ProjectEvent.SPELL_CHECK,
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
    
                        mi = new JMenuItem ("Add to Dictionary");
                        mi.setActionCommand (word);
                        mi.addActionListener (addToDict);
    
                        popup.add (mi,
                                   i);
    
                    }
    
                    popup.addSeparator ();
    
                } else
                {
    
                    if ((this.projectViewer.synonymLookupsSupported ()) &&
                        (ev.getSource () == this.editor))
                    {
    
                        if (Environment.isEnglish (_this.projectViewer.getSpellCheckLanguage ()))
                        {
                        
                            if ((word != null) &&
                                (word.length () > 0))
                            {
        
                                String mt = "No synonyms found for: " + word;
        
                                try
                                {
        
                                    // See if there are any synonyms.
                                    if (this.editor.getSynonymProvider ().hasSynonym (word))
                                    {
            
                                        mi = new JMenuItem ("Find synonyms for: " + word);
            
                                        mi.setIcon (Environment.getIcon ("find",
                                                                         Constants.ICON_MENU));
                                        mi.addActionListener (new FindSynonymsActionHandler (word,
                                                                                             loc, // c
                                                                                             this.getChapter (),
                                                                                             _this));
                                        
                                    } else {
                                        
                                        mi = new JMenuItem ("(No synonyms for: " + word + ")");
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

        boolean compress = Environment.getUserProperties ().getPropertyAsBoolean (Constants.COMPRESS_CHAPTER_CONTEXT_MENU_PROPERTY_NAME);
                                
        this.doFillPopupMenu (ev,
                              popup,
                              compress);

        this.addFormatItemsToPopupMenu (popup,
                                        compress);
                              
        this.addEditItemsToPopupMenu (popup,
                                      compress);        
        
    }
        
}
