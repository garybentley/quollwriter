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

import com.quollwriter.synonyms.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.DocumentAdapter;
import com.quollwriter.ui.components.StyleChangeAdapter;
import com.quollwriter.ui.components.StyleChangeEvent;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.Runner;
import com.quollwriter.ui.components.TextStylable;
import com.quollwriter.ui.components.TextProperties;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;


public abstract class AbstractEditorPanel extends QuollPanel implements SpellCheckSupported, TextStylable
{

    public static final String TAB = String.valueOf ('\t');

    public static final String SECTION_BREAK_FIND = "***";
    public static final String SECTION_BREAK = "*" + TAB + TAB + "*" + TAB + TAB + "*";

    protected QTextEditor    editor = null;
    protected ActionMap      actions = null;
    protected Chapter        chapter = null;
    protected Timer          autoSave = new Timer (true);
    protected javax.swing.Timer          chapterInfo = null;
    private   long chapterInfoTimerLastRun = -1;
    protected ChapterCounts  chapterCounts = null;
    protected java.util.List actionListeners = new ArrayList ();
    public Point             lastMousePosition = null;
    protected JScrollPane    scrollPane = null;
    private boolean          ignoreDocumentChange = false;
    private ReadabilityIndices readability = null;
    private int a4PageCount = 0;

    public AbstractEditorPanel(AbstractProjectViewer pv,
                               Chapter               c)
                        throws GeneralException
    {

        super (pv,
               c);

        this.chapter = c;

        final AbstractEditorPanel _this = this;

        DictionaryProvider dp = null;

        try
        {

            dp = pv.getDictionaryProvider ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get dictionary provider.",
                                        e);

        }

        this.editor = new QTextEditor (dp, // Environment.dictionaryProvider,
                                       pv.isSpellCheckingEnabled (),
                                       SECTION_BREAK);

        this.editor.setSynonymProvider (Environment.getSynonymProvider ());
        
        // This ensures that the viewport is always in sync with the text area size.
        this.editor.addKeyListener (new KeyAdapter ()
        {

            public void keyPressed (KeyEvent ev)
            {

                _this.projectViewer.playKeyStrokeSound ();

                // Get the caret.
                if (_this.editor.getCaretPosition () >= _this.editor.getText ().length ())
                {

                    Dimension d = _this.editor.getSize ();

                    _this.getScrollPane ().getViewport ().setViewSize (new Dimension (d.width,
                                                                                      d.height + 200));

                }

            }
        });

        this.editor.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.editor.addMouseListener (this);
        //this.initEditor ();

        this.actions = this.editor.getActionMap ();

        super.setActionMap (this.actions);

        this.actions.put (Constants.SHOW_FIND_ACTION,
                          new ActionAdapter ()
                          {
                            
                              public void actionPerformed (ActionEvent ev)
                              {
                                
                                  _this.projectViewer.showFind (null);
                                
                              }
                            
                          });
        
        this.actions.put ("save-chapter",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.saveChapter ();

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to save chapter: " +
                                                            _this.getChapter (),
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                "Unable to save chapter");

                                  }

                              }

                          });

        this.actions.put ("__delete",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.getEditor ().cut ();

                              }

                          });

        this.actions.put ("__paste",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.getEditor ().paste ();

                              }

                          });

        this.actions.put ("__copy",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.getEditor ().copy ();

                              }

                          });

        this.actions.put ("__cut",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.getEditor ().cut ();

                              }

                          });

        this.actions.put ("__undo",
                          this.getEditor ().getUndoManager ().getUndoAction ());

        this.actions.put ("__redo",
                          this.getEditor ().getUndoManager ().getRedoAction ());

        this.actions.put ("toggle-spellcheck",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.projectViewer.setSpellCheckingEnabled (!_this.editor.isSpellCheckEnabled ());

                              }

                          });

        this.actions.put ("toggle-wordcounts",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.projectViewer.viewWordCounts ();

                              }

                          });

        this.actions.put ("save",
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

        this.actions.put ("edit-text-properties",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {
        
                                  _this.showTextProperties ();                                
        
                              }

                          });
                     
        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                        Event.CTRL_MASK),
                "edit-text-properties");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                        Event.CTRL_MASK),
                "save");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_W,
                                        Event.CTRL_MASK),
                "toggle-wordcounts");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_R,
                                        Event.CTRL_MASK),
                "toggle-readability");
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_L,
                                        Event.CTRL_MASK),
                "toggle-spellcheck");

        im = this.editor.getInputMap (JComponent.WHEN_FOCUSED);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER,
                                        Event.CTRL_MASK),
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

    }

    public void showTextProperties ()
    {

        this.projectViewer.addSideBar ("textproperties",
                                       new TextPropertiesSideBar (this.projectViewer,
                                                                  this.projectViewer,
                                                                  this.getTextProperties ()));
        
        this.projectViewer.showSideBar ("textproperties");
        
    }

    public void close ()
    {
        
        if (this.chapterInfo != null)
        {
        
            this.chapterInfo.stop ();
            
        }
        
        this.chapterInfo = null;
        this.autoSave.cancel ();
        this.autoSave = null;
        
    }

    public int getA4PageCount ()
    {
        
        return this.a4PageCount;
        
    }
    
    public ReadabilityIndices getReadabilityIndices ()
    {
        
        return this.readability;
        
    }
    
    public ChapterCounts getChapterCounts ()
    {
        
        return this.chapterCounts;
        
    }

    public abstract JComponent getEditorWrapper (QTextEditor editor);

    public abstract void doFillToolBar (JToolBar b);

    public abstract void doFillPopupMenu (MouseEvent eve,
                                          JPopupMenu p);

    public abstract void doFillToolsPopupMenu (ActionEvent eve,
                                               JPopupMenu  p);

    public abstract void doInit ()
                          throws GeneralException;

    public void setIgnoreDocumentChanges (boolean v)
    {
        
        this.ignoreDocumentChange = v;
        
    }

    public void init ()
               throws GeneralException
    {

        final AbstractEditorPanel _this = this;

        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        this.editor.setText (this.chapter.getText (),
                             this.chapter.getMarkup ());

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
                
                            _this.chapterCounts = UIUtils.getChapterCounts (_this.editor.getText ());
                            _this.readability = UIUtils.getReadabilityIndices (_this.editor.getText ());
                            
                            _this.chapterInfoTimerLastRun = System.currentTimeMillis ();
                            
                            _this.chapterCounts.a4PageCount = UIUtils.getA4PageCountForChapter (_this.chapter,
                                                                                                _this.editor.getText ());
                            
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

        Position pos = null;
        
        try
        {
            pos = doc.createPosition (0);
            
        }catch(Exception e) {
            
        }
    
        final Position ppos = pos;
    
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

                                String te = doc.getText (offset - SECTION_BREAK_FIND.length (),
                                                         SECTION_BREAK_FIND.length ());

                                if (te.equals (SECTION_BREAK_FIND))
                                {

                                    add = true;

                                }

                                if (doc.getLogicalStyle (offset) == _this.editor.sectionBreakStyle)
                                {

                                    SwingUtilities.invokeLater (new Runner ()
                                        {

                                            public void run ()
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

                        SwingUtilities.invokeLater (new Runner ()
                            {

                                public void run ()
                                {

                                    try
                                    {

                                        String ins = String.valueOf ('\n') + String.valueOf ('\n') + SECTION_BREAK + String.valueOf ('\n') + String.valueOf ('\n');

                                        doc.replace (offset - SECTION_BREAK_FIND.length (),
                                                     SECTION_BREAK_FIND.length () + 1,
                                                     ins,
                                                     _this.editor.sectionBreakStyle);

                                        doc.setParagraphAttributes (offset + 2,
                                                                    SECTION_BREAK.length (),
                                                                    _this.editor.sectionBreakStyle,
                                                                    false);

                                        doc.setLogicalStyle (offset + 2,
                                                             _this.editor.sectionBreakStyle);

                                        Style ls = doc.addStyle (null,
                                                                 null);
                                        StyleConstants.setAlignment (ls,
                                                                     StyleConstants.ALIGN_LEFT);

                                        doc.setParagraphAttributes (offset + SECTION_BREAK.length (),
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

                        SwingUtilities.invokeLater (new Runner ()
                            {

                                public void run ()
                                {

                                    try
                                    {

                                        doc.replace (offset - SECTION_BREAK_FIND.length (),
                                                     SECTION_BREAK_FIND.length (),
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

        this.scheduleAutoSave ();

        this.editor.getDocument ().addDocumentListener (new DocumentAdapter ()
        {

            public void changedUpdate (DocumentEvent ev)
            {

                if (_this.ignoreDocumentChange)
                {
                    
                    return;
                    
                }

                _this.setHasUnsavedChanges (true);

            }

            public void insertUpdate (DocumentEvent ev)
            {

                _this.setHasUnsavedChanges (true);

            }

            public void removeUpdate (DocumentEvent ev)
            {

                _this.setHasUnsavedChanges (true);

            }

        });

        this.editor.addStyleChangeListener (new StyleChangeAdapter ()
        {
            
            public void styleChanged (StyleChangeEvent ev)
            {

                _this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                      ev.getType (),
                                                      ev);
                                
            }
            
        });

        JComponent p = this.getEditorWrapper (this.editor);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.scrollPane = new JScrollPane (p);
        this.scrollPane.setBorder (null);
        this.scrollPane.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.scrollPane.getVerticalScrollBar ().setUnitIncrement (20);

        this.add (this.scrollPane);

        this.doInit ();
        
        this.initEditor ();
        
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

    public void scheduleAutoSave ()
    {

        this.autoSave.cancel ();

        this.autoSave = new Timer (true);

        if (this.chapter.getPropertyAsBoolean (Constants.CHAPTER_AUTO_SAVE_ENABLED_PROPERTY_NAME))
        {
        
            long autoSaveInt = Utils.getTimeAsMillis (this.chapter.getProperty (Constants.CHAPTER_AUTO_SAVE_INTERVAL_PROPERTY_NAME));
    
            if (autoSaveInt > 0)
            {
    
                final AbstractEditorPanel _this = this;
    
                // Create our auto save
                this.autoSave.schedule (new BlankTimerTask ()
                {

                    public void run ()
                    {

                        if (!_this.hasUnsavedChanges ())
                        {

                            return;

                        }

                        try
                        {

                            _this.saveChapter ();

                        } catch (Exception e)
                        {

                            Environment.logError ("Unable to auto save chapter: " +
                                                  _this.getChapter (),
                                                  e);

                        }

                    }

                },
                (autoSaveInt + (new Random ().nextInt (10) * 1000)),
                autoSaveInt);
    
            }

        }
                
    }

    public void insertSectionBreak ()
    {

        final AbstractEditorPanel _this = this;

        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        final int offset = this.editor.getCaret ().getDot ();

        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    try
                    {

                        _this.editor.startCompoundEdit ();

                        String ins = String.valueOf ('\n') + String.valueOf ('\n') + SECTION_BREAK + String.valueOf ('\n') + String.valueOf ('\n');

                        doc.insertString (offset,
                                          ins,
                                          _this.editor.sectionBreakStyle);

                        doc.setParagraphAttributes (offset + 2,
                                                    SECTION_BREAK.length (),
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

    public Chapter getChapter ()
    {

        return this.chapter;

    }

    public void stopAutoSave ()
    {

        this.autoSave.cancel ();

    }

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

        final AbstractEditorPanel _this = this;

        final ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };

        acts.add (UIUtils.createToolBarButton ("save",
                                               "Click to save the Chapter text",
                                               "save-chapter",
                                               aa));

        this.doFillToolBar (acts);

        acts.add (UIUtils.createToolBarButton ("wordcount",
                                               "Click to view the word counts and readability indices",
                                               "word-count",
                                               new ActionAdapter ()
                                               {

                                                   public void actionPerformed (ActionEvent ev)
                                                   {

                                                       _this.projectViewer.viewWordCounts ();

                                                   }

                                               }));

        String type = (this.projectViewer.isSpellCheckingEnabled () ? "off" : "on");

        acts.add (UIUtils.createToolBarButton ("spellchecker-turn-" + type,
                                               "Click to turn the spell checker " + type,
                                               "toggle-spellcheck",
                                               aa));

        acts.add (UIUtils.createToolBarButton ("delete",
                                               "Click to delete this Chapter",
                                               "delete",
                                               new DeleteChapterActionHandler ((Chapter) this.obj,
                                                                               this.projectViewer)));

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

                if (!_this.projectViewer.isInFullScreen ())
                {                                            
                
                    mi = new JMenuItem ("Edit Text Properties",
                                        Environment.getIcon ("edit-properties",
                                                             Constants.ICON_MENU));
                    mi.setActionCommand ("edit-text-properties");
                    mi.addActionListener (aa);
                    mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                                               ActionEvent.CTRL_MASK));
    
                    m.add (mi);

                }
                    
                mi = new JMenuItem ("Find",
                                    Environment.getIcon ("find",
                                                         Constants.ICON_MENU));
                mi.setActionCommand (Constants.SHOW_FIND_ACTION);
                mi.addActionListener (aa);
                mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                           ActionEvent.CTRL_MASK));

                m.add (mi);

                mi = new JMenuItem ("Print Chapter",
                                    Environment.getIcon ("print",
                                                         Constants.ICON_MENU));
                mi.setActionCommand ("print");
                mi.addActionListener (aa);
                mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                                           ActionEvent.CTRL_MASK));
                m.add (mi);

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

        this.chapter.setText (this.editor.getText ());
        this.chapter.setMarkup (this.editor.getMarkup ().toString ());

        super.saveObject ();

    }

    public void fillPopupMenu (final MouseEvent ev,
                               final JPopupMenu popup)
    {

        final QTextEditor         editor = this.editor;
        final AbstractEditorPanel _this = this;

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

            java.util.List l = this.editor.getSpellCheckSuggestions (p);

            if (l != null)
            {

                final DocumentWordTokenizer wordsTok = new DocumentWordTokenizer (this.editor.getDocument ());
                wordsTok.posStartFullWordFrom (this.editor.viewToModel (p));

                String word = wordsTok.nextWord ();

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

                        mi = new JMenuItem (((com.swabunga.spell.engine.Word) l.get (i)).getWord ());
                        mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
                        mi.setActionCommand (mi.getText ());
                        mi.addActionListener (new ActionAdapter ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.getEditor ().startCompoundEdit ();

                                wordsTok.replaceWord (ev.getActionCommand ());

                                _this.getEditor ().endCompoundEdit ();

                                _this.projectViewer.fireProjectEvent (ProjectEvent.SPELL_CHECK,
                                                                      ProjectEvent.REPLACE,
                                                                      ev.getActionCommand ());

                            }

                        });

                        if (i < 5)
                        {

                            popup.add (mi,
                                       0);

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

                if ((Environment.synonymLookupsSupported ()) &&
                    (ev.getSource () == this.editor))
                {

                    DocumentWordTokenizer wordsTok = null;

                    try
                    {

                        wordsTok = new DocumentWordTokenizer (this.editor.getDocument ());

                    } catch (Exception e)
                    {

                        Environment.logError ("HERE",
                                              e);

                    }

                    // Handle selected text...

                    int c = this.editor.viewToModel (p);
                    
                    String word = null;

                    if (wordsTok != null)
                    {

                        try
                        {
    
                            wordsTok.posStartFullWordFrom (c);
    
                            word = wordsTok.nextWord ();
    
                            word = word.trim ();
    
                            c = wordsTok.getCurrentWordPosition ();
    
                        } catch (Exception e)
                        {
    
                            // Ignore.
    
                        }

                    }

                    if ((word != null) &&
                        (word.length () > 0))
                    {

                        String mt = "No synonyms found for: " + word;

                        try
                        {

                            // See if there are any synonyms.
                            if (Environment.hasSynonym (word))
                            {
    
                                mi = new JMenuItem ("Find synonyms for: " + word);
    
                                mi.setIcon (Environment.getIcon ("find",
                                                                 Constants.ICON_MENU));
                                mi.addActionListener (new FindSynonymsActionHandler (word,
                                                                                     c,
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

        // Save.
        mi = new JMenuItem ("Save " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE),
                            Environment.getIcon ("save",
                                                 Constants.ICON_MENU));
        mi.setMnemonic (KeyEvent.VK_S);
        mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                                   ActionEvent.CTRL_MASK));
        mi.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        _this.saveChapter ();

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save chapter: " +
                                              _this.getChapter (),
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to save chapter");

                    }

                }

            });

        popup.add (mi);

        this.doFillPopupMenu (ev,
                              popup);

        String sel = this.editor.getSelectedText ();

        if (!sel.equals (""))
        {

            popup.addSeparator ();

            // Add the bold/italic/underline.
            mi = new JMenuItem ("Bold",
                                Environment.getIcon ("bold",
                                                     Constants.ICON_MENU));
            mi.setMnemonic (KeyEvent.VK_B);
            mi.setFont (mi.getFont ().deriveFont (Font.BOLD));
            mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_B,
                                                       ActionEvent.CTRL_MASK));
            mi.addActionListener (this.actions.get ("bold"));
            popup.add (mi);

            mi = new JMenuItem ("Italic",
                                Environment.getIcon ("italic",
                                                     Constants.ICON_MENU));
            mi.setMnemonic (KeyEvent.VK_I);
            mi.setFont (mi.getFont ().deriveFont (Font.ITALIC));
            mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_I,
                                                       ActionEvent.CTRL_MASK));
            mi.addActionListener (this.actions.get ("italic"));
            popup.add (mi);

            mi = new JMenuItem ("Underline",
                                Environment.getIcon ("underline",
                                                     Constants.ICON_MENU));
            mi.setMnemonic (KeyEvent.VK_U);

            Map attrs = mi.getFont ().getAttributes ();
            attrs.put (TextAttribute.UNDERLINE,
                       TextAttribute.UNDERLINE_LOW_ONE_PIXEL);

            mi.setFont (mi.getFont ().deriveFont (attrs));
            mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_U,
                                                       ActionEvent.CTRL_MASK));
            mi.addActionListener (this.actions.get ("underline"));
            popup.add (mi);

        }

        // Text menu.
        popup.addSeparator ();

        mi = new JMenuItem ("Find",
                            Environment.getIcon ("find",
                                                 Constants.ICON_MENU));
        mi.setMnemonic (KeyEvent.VK_F);
        mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                   ActionEvent.CTRL_MASK));
        mi.addActionListener (this.actions.get (Constants.SHOW_FIND_ACTION));
        popup.add (mi);

        // Only add if there is something to cut.
        if (!sel.equals (""))
        {

            mi = new JMenuItem ("Cut",
                                Environment.getIcon ("cut",
                                                     Constants.ICON_MENU));
            mi.setMnemonic (KeyEvent.VK_X);
            mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_X,
                                                       ActionEvent.CTRL_MASK));
            mi.addActionListener (this.actions.get ("__cut"));
            popup.add (mi);

            mi = new JMenuItem ("Copy",
                                Environment.getIcon ("copy",
                                                     Constants.ICON_MENU));
            mi.setMnemonic (KeyEvent.VK_C);
            mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_C,
                                                       ActionEvent.CTRL_MASK));
            mi.addActionListener (this.actions.get ("__copy"));
            popup.add (mi);

        }

        // Only show if there is something in the clipboard.
        if (UIUtils.clipboardHasContent ())
        {

            mi = new JMenuItem ("Paste",
                                Environment.getIcon ("paste",
                                                     Constants.ICON_MENU));
            mi.setMnemonic (KeyEvent.VK_V);
            mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_V,
                                                       ActionEvent.CTRL_MASK));
            mi.addActionListener (this.actions.get ("__paste"));
            popup.add (mi);

        }

        if (!sel.equals (""))
        {

            mi = new JMenuItem ("Delete",
                                Environment.getIcon ("delete",
                                                     Constants.ICON_MENU));
            mi.addActionListener (this.actions.get ("__delete"));
            popup.add (mi);

        }

        // Only add if there is an undo available.

        mi = new JMenuItem ("Undo",
                            Environment.getIcon ("undo",
                                                 Constants.ICON_MENU));
        mi.setMnemonic (KeyEvent.VK_Z);
        mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Z,
                                                   ActionEvent.CTRL_MASK));
        mi.addActionListener (this.actions.get ("__undo"));
        popup.add (mi);

        mi = new JMenuItem ("Redo",
                            Environment.getIcon ("redo",
                                                 Constants.ICON_MENU));
        mi.setMnemonic (KeyEvent.VK_Y);
        mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Y,
                                                   ActionEvent.CTRL_MASK));
        mi.addActionListener (this.actions.get ("__redo"));
        popup.add (mi);

    }

    public QTextEditor getEditor ()
    {

        return this.editor;

    }

    public void scrollToPosition (int p)
                           throws GeneralException
    {

        Rectangle r = null;

        try
        {

            r = this.editor.modelToView (p);

        } catch (Exception e)
        {

            // BadLocationException!
            throw new GeneralException ("Position: " +
                                        p +
                                        " is not valid.",
                                        e);

        }

        if (r == null)
        {

            throw new GeneralException ("Position: " +
                                        p +
                                        " is not valid.");

        }

        int y = r.y - r.height;

        this.scrollPane.getVerticalScrollBar ().setValue (y);

    }

    public void setState (final Map<String, String> s,
                          boolean                   hasFocus)
    {

        try
        {

            int v = Integer.parseInt (s.get (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME));

            this.editor.setSelectionStart (v);
            this.editor.setSelectionEnd (v);
            this.editor.getCaret ().setDot (v);

        } catch (Exception e)
        {

        }

        try
        {

            // this.scrollToPosition (Integer.parseInt (s.get (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME)));

        } catch (Exception e)
        {

            // Ignore it.

        }

        this.scrollCaretIntoView ();

        if (hasFocus)
        {

            this.editor.grabFocus ();

        }

    }

    public void getState (Map<String, Object> m)
    {

        m.put (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME,
               this.scrollPane.getVerticalScrollBar ().getValue ());
        m.put (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME,
               this.editor.getSelectionStart ());

    }

    public void initEditor (TextProperties props)
    {
        
        Project proj = this.projectViewer.getProject ();

        this.ignoreDocumentChange = true;

        this.setLineSpacing (props.getLineSpacing ());
        this.setFontSize (props.getFontSize ());
        this.setFontFamily (props.getFontFamily ());
        this.setAlignment (props.getAlignment ());
        this.setFirstLineIndent (proj.getPropertyAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME));

        this.setBackgroundColor (props.getBackgroundColor ());
        this.setTextColor (props.getTextColor ());

        this.ignoreDocumentChange = false;
        
    }
    
    public void initEditor ()
    {

        this.ignoreDocumentChange = true;

        com.gentlyweb.properties.Properties props = Environment.getUserProperties ();        
        
        this.editor.setLineSpacing (props.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME));
        
        this.setFontSize (props.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME));
        this.setFontFamily (props.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME));
        this.setAlignment (props.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME));
        this.setFirstLineIndent (props.getPropertyAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME));

        this.restoreBackgroundColor ();
        this.restoreFontColor ();
        
        this.ignoreDocumentChange = false;
        
    }

    public List<Component> getTopLevelComponents ()
    {

        List<Component> l = new ArrayList ();
        l.add (this.editor);

        return l;

    }

    public void refresh (NamedObject n)
    {

        // No need to do anything.

    }

    public void setDictionaryProvider (DictionaryProvider dp)
    {

        this.editor.setDictionaryProvider (dp);

    }

    public void checkSpelling ()
    {

        this.editor.checkSpelling ();

    }

    public void setSpellCheckingEnabled (boolean v)
    {

        this.editor.setSpellCheckEnabled (v);

        String type = (v ? "off" : "on");

        this.setToolBarButtonIcon ("toggle-spellcheck",
                                   "Click to turn the spell checker " + type,
                                   "spellchecker-turn-" + type);

    }

    public String getTitle ()
    {
        
        return this.obj.getName ();
        
    }
    
    public String getIconType ()
    {

        return this.obj.getObjectType ();

    }

    public void setFontColor (Color c)
    {

        this.ignoreDocumentChange = true;

        this.editor.setFontColor (c);

        this.editor.setCaretColor (c);

        this.ignoreDocumentChange = false;

    }

    public void setTextColor (Color c)
    {
        
        this.setFontColor (c);
        
    }
    
    public void restoreFontColor ()
    {

        this.ignoreDocumentChange = true;
        
        this.editor.setFontColor (Color.black);
        this.editor.setCaretColor (Color.black);

        this.ignoreDocumentChange = false;
        
    }

    public void restoreBackgroundColor ()
    {

        this.editor.setBackground (Color.white);

    }

    public void setBackgroundColor (Color c)
    {

        this.editor.setBackground (c);

    }

    public Color getBackgroundColor ()
    {
        
        return this.editor.getBackground ();
        
    }

    public JScrollPane getScrollPane ()
    {

        return this.scrollPane;

    }

    public void scrollCaretIntoView ()
    {

        final AbstractEditorPanel _this = this;

        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    try
                    {

                        int c = _this.editor.getCaret ().getDot ();

                        if (c > -1)
                        {

                            _this.scrollToPosition (c);

                        }

                    } catch (Exception e)
                    {

                        // Ignore it.

                    }

                }

            });

    }

    public TextProperties getTextProperties ()
    {

        //Project proj = this.projectViewer.getProject ();
    
        return new ProjectTextProperties (this.projectViewer);
    /*
        TextProperties props = new TextProperties (this,
                                                   proj.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME),
                                                   proj.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME),
                                                   proj.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME),
                                                   proj.getPropertyAsBoolean (Constants.EDITOR_INDENT_FIRST_LINE_PROPERTY_NAME),
                                                   proj.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME),
                                                   this.editor.getTextColor (),
                                                   this.getBackgroundColor ());
        
        return props;
      */  
    }
    
    public void setFontSize (int v)
    {

        this.ignoreDocumentChange = true;
        
        this.editor.setFontSize (UIUtils.getEditorFontSize (v));
        
        this.ignoreDocumentChange = false;

    }

    public void setFontFamily (String name)
    {

        this.ignoreDocumentChange = true;
        
        this.editor.setFontFamily (name);
        
        this.ignoreDocumentChange = false;

    }

    public void setAlignment (String v)
    {
        
        this.ignoreDocumentChange = true;
        
        this.editor.setAlignment (v);
        
        this.ignoreDocumentChange = false;        
        
    }

    public void setFirstLineIndent (boolean v)
    {
        
        this.ignoreDocumentChange = true;
        
        this.editor.setFirstLineIndent (v);
        
        this.ignoreDocumentChange = false;        
        
    }

    public void setLineSpacing (float v)
    {
        
        this.ignoreDocumentChange = true;
        
        this.editor.setLineSpacing (v);
        
        this.ignoreDocumentChange = false;        
        
    }

}
