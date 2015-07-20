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

public abstract class AbstractEditorPanel extends QuollPanel implements SpellCheckSupported, TextStylable
{

    public static final String TOGGLE_SPELLCHECK_ACTION_NAME = "toggle-spellcheck";
    public static final String TOGGLE_WORDCOUNTS_ACTION_NAME = "toggle-wordcounts";
    public static final String EDIT_TEXT_PROPERTIES_ACTION_NAME = "edit-text-properties";

    protected QTextEditor    editor = null;
    protected ActionMap      actions = null;
    protected Chapter        chapter = null;
    //protected Timer          autoSave = new Timer (true);
    //protected javax.swing.Timer          chapterInfo = null;
    //private   long chapterInfoTimerLastRun = -1;
    private ChapterCounts  chapterCounts = null;
    //protected java.util.List actionListeners = new ArrayList ();
    protected Point             lastMousePosition = null;
    protected JScrollPane    scrollPane = null;
    private boolean          ignoreDocumentChange = false;
    private boolean          useTypewriterScrolling = false;
    private ReadabilityIndices readability = null;
    //private int a4PageCount = 0;
    private ActionListener performAction = null;
    private int scrollOffset = 0;
    private Insets origEditorMargin = null;    
    private int softCaret = -1;
    
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

        SynonymProvider sp = null;

        try
        {

            sp = pv.getSynonymProvider ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to get synonym provider.",
                                        e);

        }

        this.editor = new QTextEditor (dp,
                                       pv.isSpellCheckingEnabled ());

        this.editor.setSynonymProvider (sp);//Environment.getSynonymProvider ());
        
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

        this.actions.put (TOGGLE_SPELLCHECK_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.projectViewer.setSpellCheckingEnabled (!_this.editor.isSpellCheckEnabled ());

                              }

                          });

        this.actions.put (TOGGLE_WORDCOUNTS_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.projectViewer.viewWordCounts ();

                              }

                          });

        this.actions.put (EDIT_TEXT_PROPERTIES_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {
        
                                    try
                                    {
        
                                        _this.showTextProperties ();
                                        
                                    } catch (Exception e) {
                                        
                                        Environment.logError ("Unable to show text properties",
                                                              e);
                                        
                                        UIUtils.showErrorMessage (_this,
                                                                  "Unable to show text properties.");
                                        
                                    }
        
                              }

                          });
                                          
        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                        Event.CTRL_MASK),
                EDIT_TEXT_PROPERTIES_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_W,
                                        Event.CTRL_MASK),
                TOGGLE_WORDCOUNTS_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_L,
                                        Event.CTRL_MASK),
                TOGGLE_SPELLCHECK_ACTION_NAME);

        im = this.editor.getInputMap (JComponent.WHEN_FOCUSED);

        this.addComponentListener (new ComponentAdapter ()
        {
            
            public void componentResized (ComponentEvent ev)
            {
                
                _this.scrollCaretIntoView ();
                
            }
            
                
        });
        
        this.performAction = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };
        
    }

    public abstract JComponent getEditorWrapper (QTextEditor editor);    
    
    public void setWritingLineColor (Color c)
    {
        
        this.editor.setWritingLineColor (c);
        
    }

    @Override
    public void close ()
    {
        
        
    }
    
    public void setHighlightWritingLine (boolean v)
    {
        
        this.editor.setHighlightWritingLine (v);
        
    }
    
    public void showTextProperties ()
                             throws GeneralException
    {
        
        this.projectViewer.showTextProperties ();
        
    }
    
    public JButton createToolbarButton (String icon,
                                        String toolTipText,
                                        String actionCommand)
    {
        
        return this.createButton (icon,
                                  Constants.ICON_TOOLBAR,
                                  toolTipText,
                                  actionCommand);
        
    }
    
    public JButton createButton (String         icon,
                                 int            iconType,
                                 String         toolTipText,
                                 String actionCommand)
    {
        
        JButton but = UIUtils.createButton (icon,
                                            iconType,
                                            toolTipText,
                                            this.performAction);
        
        but.setActionCommand (actionCommand);

        return but;
        
    }

    public JButton createButton (String         icon,
                                 int            iconType,
                                 String         toolTipText,
                                 String         actionCommand,
                                 ActionListener list)
    {
        
        JButton but = UIUtils.createButton (icon,
                                            iconType,
                                            toolTipText,
                                            list);
        
        but.setActionCommand (actionCommand);
        
        return but;
        
    }

    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String         actionCommand,
                                     KeyStroke      accel,
                                     ActionListener list)
    {
        
        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               list);
                                     
        mi.setActionCommand (actionCommand);

        mi.setAccelerator (accel);
        
        return mi;

        
    }
    
    public JMenuItem createMenuItem (String label,
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

    public JMenuItem createMenuItem (String label,
                                     String icon,
                                     String actionCommand)
    {
        
        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               this.performAction);
                                     
        mi.setActionCommand (actionCommand);
                                                       
        return mi;
        
    }        
    
    public void addPerformActionListener (AbstractButton b)
    {
        
        b.addActionListener (this.performAction);
        
    }
    
    public ActionListener getPerformActionListener ()
    {
        
        return this.performAction;
        
    }
    
/*
    public int getA4PageCount ()
    {
        
        return this.a4PageCount;
        
    }
  */  
    protected void setChapterCounts (ChapterCounts c)
    {
        
        this.chapterCounts = c;
        
    }
    
    protected void setReadabilityIndices (ReadabilityIndices r)
    {
        
        this.readability = r;
        
    }
    
    public ReadabilityIndices getReadabilityIndices ()
    {
        
        return this.readability;
        
    }
    
    public ChapterCounts getChapterCounts ()
    {
        
        return this.chapterCounts;
        
    }

    public void setIgnoreDocumentChanges (boolean v)
    {
        
        this.ignoreDocumentChange = v;
        
    }
    
    public boolean isIgnoreDocumentChanges ()
    {
        
        return this.ignoreDocumentChange;
        
    }
    
    public void init ()
               throws GeneralException
    {

        final AbstractEditorPanel _this = this;

        final DefaultStyledDocument doc = (DefaultStyledDocument) this.editor.getDocument ();

        this.editor.setText (this.chapter.getText (),
                             this.chapter.getMarkup ());
    
        this.editor.addStyleChangeListener (new StyleChangeAdapter ()
        {
            
            public void styleChanged (StyleChangeEvent ev)
            {

                _this.initScrollBar ();
                        
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
        this.scrollPane.getViewport ().setOpaque (true);
        
        this.scrollPane.getVerticalScrollBar ().setUnitIncrement (20);
                                
        this.origEditorMargin = this.editor.getMargin ();
                                         
        this.add (this.scrollPane);
        
        this.scrollPane.addMouseWheelListener (new MouseWheelListener ()
        {
            
            public void mouseWheelMoved (MouseWheelEvent ev)
            {
                
                if (_this.useTypewriterScrolling)
                {
                
                    try
                    {
                    
                        int c = ev.getWheelRotation ();
        
                        Rectangle r = _this.editor.modelToView (_this.editor.getCaret ().getDot ());                
                        
                        int ny = r.y + (c * r.height);
                        
                        int d = _this.editor.viewToModel (new Point (r.x,
                                                                     ny));
                        
                        _this.editor.getCaret ().setDot (d);
                        
                    } catch (Exception e) {
                        
                        // Just ignore
                        
                    }

                }
                
            }
            
        });
        
        this.editor.addStyleChangeListener (new StyleChangeAdapter ()
        {
            
            public void styleChanged (StyleChangeEvent ev)
            {

                _this.initScrollBar ();
                        
                _this.projectViewer.fireProjectEvent (Chapter.OBJECT_TYPE,
                                                      ev.getType (),
                                                      ev);
                                
            }
            
        });
        
        this.editor.addCaretListener (new CaretListener ()
        {
                   
                      
            public void caretUpdate (final CaretEvent ev)
            {
                                
                UIUtils.doLater (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {
                    
                        if (_this.useTypewriterScrolling)
                        {
                        
                            _this.updateViewportPositionForTypewriterScrolling ();                              

                        }

                    }
                            
                });
                                        
            }
            
        });
                
        this.initEditor ();
                
        /*
         *Experimental... not quite right for page-up.
        final ActionListener origPageDown = this.editor.getActionMap ().get ("page-down");
        final ActionListener origPageUp = this.editor.getActionMap ().get ("page-up");

        this.editor.getActionMap ().put ("page-up",
                                         new TextAction ("here")
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                try
                {
                
                    int c = _this.editor.getCaret ().getDot ();
                    
                    int l = _this.editor.getText ().length ();
                    
                    c--;
                    
                    if (c <= 0)
                    {
                        
                        return;
                        
                    }
                    
                    // Find the first non-whitespace.
                    for (int i = c; i > -1; i--)
                    {
                        
                        if (!Character.isWhitespace (_this.editor.getText ().charAt (i)))
                        {
                         
                            c = i;

                            break;
                        }
                        
                    }
                    
                    // Find the first newline.
                    for (int i = c; i > -1; i--)
                    {

                        if (_this.editor.getText ().charAt (i) == '\n')
                        {

                            _this.editor.getCaret ().setDot (i + 1);

                            _this.editor.scrollRectToVisible (_this.editor.modelToView (i + 1));
                            
                            break;
                            
                        }
                        
                    }
                    
                } catch (Exception e) {
                    
                    e.printStackTrace ();
                    
                                
                }
            }
            
        });
        
        this.editor.getActionMap ().put ("page-down",
                                         new TextAction ("here")
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                try
                {
                
                    int c = _this.editor.getCaret ().getDot ();
                    
                    int l = _this.editor.getText ().length ();
                    
                    if (c >= l)
                    { 
                        
                        return;
                        
                    }
                    
                    int n = _this.editor.getText ().indexOf ('\n', c);
                    
                    if (n > c)
                    {
                        
                        // Find the first non-whitespace.
                        for (int i = n; i < _this.editor.getText ().length (); i++)
                        {
                            
                            if (!Character.isWhitespace (_this.editor.getText ().charAt (i)))
                            {
                                
                                _this.editor.getCaret ().setDot (i);
    
                                _this.editor.scrollRectToVisible (_this.editor.modelToView (i));
                    
                                break;
                                
                            }
                            
                        }
                        
                    }
                    
                } catch (Exception e) {
                    
                    e.printStackTrace ();
                    
                                
                }
            }
            
        });
        */
    }
        
    public void setSoftCaret (int c)
    {
        
        this.softCaret = c;
        
        this.updateViewportPositionForTypewriterScrolling ();
        
    }
    
    public void updateViewportPositionForTypewriterScrolling ()
    {

        if (!this.useTypewriterScrolling)
        {
            
            return;
            
        }
    
        final AbstractEditorPanel _this = this;
    
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                int dot = (_this.softCaret > -1 ? _this.softCaret : _this.editor.getCaret ().getDot ());
                        
                Rectangle r = null;
                
                try
                {
                    
                    r = _this.editor.modelToView (dot);
                    
                } catch (Exception e) {
                    
                    // Ignore.
                    return;
                    
                }

                _this.softCaret = -1;
                
                if (r == null)
                {
        
                    return;
                    
                }
                
                Insets i = _this.editor.getMargin ();
                int hh = _this.scrollPane.getSize ().height / 2;
                int y = r.y;
        
                if (i != null)
                {
                    
                    y -= i.top;
                    
                }
                
                if ((y - hh) < 0)
                {
                                
                    _this.editor.setMargin (new Insets (-1 * (y - hh),
                                                              _this.origEditorMargin.left,
                                                              _this.origEditorMargin.bottom,
                                                              _this.origEditorMargin.right));
                                                             
                    _this.scrollPane.getViewport ().setViewPosition (new Point (0, 0));
        
                } else {

                    if (y > (_this.editor.getSize ().height - hh - i.bottom - (r.height / 2)))
                    {
        
                        _this.editor.setMargin (new Insets (_this.origEditorMargin.top,
                                                            _this.origEditorMargin.left,
                                                            hh - (_this.editor.getSize ().height - y - i.bottom - (r.height / 2)),
                                                            _this.origEditorMargin.right));
                                                                                                                 
                    } else {
                    
                        _this.editor.setMargin (new Insets (_this.origEditorMargin.top,
                                                            _this.origEditorMargin.left,
                                                            _this.origEditorMargin.bottom,
                                                            _this.origEditorMargin.right));
        
                    }
                                                                 
                    Point p = new Point (0, y - hh + (r.height /2));
                    
                    _this.scrollPane.getViewport ().setViewPosition (p);
        
                }

                _this.validate ();
                _this.repaint ();
                
            }
            
        });
        
    }
    
    public void setUseTypewriterScrolling (boolean v)
    {
        
        this.useTypewriterScrolling = v;

        if (!v)
        {
            
            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            // Reset the margin.            
            this.editor.setMargin (this.origEditorMargin);
                        
        } else {
            
            this.scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            
        }
        
        this.scrollCaretIntoView ();
        
        this.scrollPane.getViewport ().setViewSize (this.editor.getPreferredSize ());

        this.editor.grabFocus ();
        
    }
/*
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
*/
/*
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
*/
    public Chapter getChapter ()
    {

        return this.chapter;

    }
/*
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
*/
  /*  
    public abstract void fillToolBar (JToolBar acts,
                             final boolean  fullScreen)
    {

        final AbstractEditorPanel _this = this;

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
*/
  /*
    public void saveObject ()
                     throws Exception
    {

        this.chapter.setText (this.editor.getText ());
        this.chapter.setMarkup (this.editor.getMarkup ().toString ());

        super.saveObject ();

    }
*/
  /*
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
    */
  /*
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
    */
  /*
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
*/
    public QTextEditor getEditor ()
    {

        return this.editor;

    }
        
    public int getScrollOffset ()
    {
        
        return this.scrollPane.getVerticalScrollBar ().getValue ();
        
    }
    
    public void incrementScrollPositionBy (int p)
    {
        
        this.scrollPane.getVerticalScrollBar ().setValue (this.scrollPane.getVerticalScrollBar ().getValue () + p);
        
    }
    
    public void scrollToPosition (int p)
                           throws GeneralException
    {

        if (this.useTypewriterScrolling)
        {

            // Not compatible with typewriter scrolling.
            return;
            
        }
    
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
        
        if (y < 0)
        {
            
            y = 0;
            
        }

        this.scrollPane.getVerticalScrollBar ().setValue (y);

    }

    private void initScrollBar ()
    {
        
        int l = this.editor.getLineHeight ();

        JScrollBar sc = this.scrollPane.getVerticalScrollBar ();
        
        int o = sc.getValue ();
        
        sc.setUnitIncrement (l);
        
        sc.setValue ((int) (Math.floor (o / l) * l));

        sc.setBlockIncrement (l);        
        
    }
    
    public void setState (final Map<String, String> s,
                          boolean                   hasFocus)
    {

        final AbstractEditorPanel _this = this;
        
        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {

                try
                {
        
                    JScrollBar sc = _this.scrollPane.getVerticalScrollBar ();
                    
                    int o = Integer.parseInt (s.get (Constants.LAST_EDITOR_SCROLL_POSITION_PROPERTY_NAME));
        
                    sc.setValue (o);
        
                    int v = Integer.parseInt (s.get (Constants.LAST_EDITOR_CARET_POSITION_PROPERTY_NAME));
        
                    _this.editor.setSelectionStart (v);
                    _this.editor.setSelectionEnd (v);
                    _this.editor.getCaret ().setDot (v);
                        
                } catch (Exception e)
                {
        
                    // Ignore it.
        
                } finally {
                    
                    _this.initScrollBar ();
                                            
                }

            }
            
        });
                
        //this.scrollCaretIntoView ();

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
        this.setFirstLineIndent (props.getFirstLineIndent ());

        this.setBackgroundColor (props.getBackgroundColor ());
        this.setTextColor (props.getTextColor ());
        this.setWritingLineColor (props.getWritingLineColor ());
        this.setHighlightWritingLine (props.isHighlightWritingLine ());
        
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
        this.setWritingLineColor (UIUtils.getColor (props.getProperty (Constants.EDITOR_WRITING_LINE_COLOR_PROPERTY_NAME)));
        this.setHighlightWritingLine (props.getPropertyAsBoolean (Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME));
        
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

    public void setSynonymProvider (SynonymProvider sp)
    {
        
        this.editor.setSynonymProvider (sp);
        
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

    public void setCaretPosition (int dot)
    {
        
        this.editor.getCaret ().setDot (dot);
        
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
        
        this.scrollCaretIntoView (null);
        
    }
        
    public void scrollCaretIntoView (final Runnable runAfterScroll)
    {

        final AbstractEditorPanel _this = this;

        UIUtils.doLater (new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                try
                {

                    int c = _this.editor.getCaret ().getDot ();

                    if (c > -1)
                    {

                        _this.scrollToPosition (c);

                    }

                    _this.updateViewportPositionForTypewriterScrolling ();
                    
                    if (runAfterScroll != null)
                    {
                        
                        SwingUtilities.invokeLater (runAfterScroll);
                        
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

        return new ProjectTextProperties (this.projectViewer);

    }
    
    public void setFontSize (int v)
    {

        this.ignoreDocumentChange = true;
        
        this.editor.setFontSize (UIUtils.getEditorFontSize (v));
        
        this.ignoreDocumentChange = false;

        this.scrollCaretIntoView ();
        
    }

    public void setFontFamily (String name)
    {

        this.ignoreDocumentChange = true;
        
        this.editor.setFontFamily (name);
        
        this.ignoreDocumentChange = false;

        this.scrollCaretIntoView ();
        
    }

    public void setAlignment (String v)
    {
        
        this.ignoreDocumentChange = true;
        
        this.editor.setAlignment (v);
        
        this.ignoreDocumentChange = false;        
        
        this.scrollCaretIntoView ();
        
    }

    public void setFirstLineIndent (boolean v)
    {
        
        this.ignoreDocumentChange = true;
        
        this.editor.setFirstLineIndent (v);
        
        this.ignoreDocumentChange = false;        
        
        this.scrollCaretIntoView ();
        
    }

    public void setLineSpacing (float v)
    {
        
        this.ignoreDocumentChange = true;
        
        this.editor.setLineSpacing (v);
        
        this.ignoreDocumentChange = false;        
        
        this.scrollCaretIntoView ();
        
    }

    public Point getLastMousePosition ()
    {
        
        return this.lastMousePosition;
        
    }
    
}
