package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;

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
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.renderers.*;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;


public class QuollEditorPanel extends AbstractEditorPanel
{

    public static final String TAB = String.valueOf ('\t');

    public static final String SECTION_BREAK_FIND = "***";
    public static final String SECTION_BREAK = "*" + TAB + TAB + "*" + TAB + TAB + "*";

    // private QTextEditor editor = null;
    private IconColumn              iconColumn = null;
    protected ProjectViewer         projectViewer = null;
    private Box                     problemFinderPanel = null;
    private ProblemFinder           problemFinder = null;
    private ProblemFinderRuleConfig problemFinderRuleConfig = null;
    private JLabel                  ignoredProblemsLabel = null;
    private int                     lastCaret = -1;

    public QuollEditorPanel(ProjectViewer pv,
                            Chapter       c)
                     throws GeneralException
    {

        super (pv,
               c);

        this.projectViewer = pv;

        final QuollEditorPanel _this = this;

        this.iconColumn = new IconColumn (this,
                                          this.projectViewer);

        this.iconColumn.addMouseListener (this);

        this.actions.put ("chapter-info",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.projectViewer.viewChapterInformation (_this.chapter);

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to show chapter information for: " +
                                                            _this.chapter,
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                Environment.replaceObjectNames ("Unable to show {chapter}."));

                                  }

                              }

                          });

        this.actions.put ("new" + Scene.OBJECT_TYPE,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       "new" + Scene.OBJECT_TYPE,
                                                       -1);

                              }

                          });

        this.actions.put ("new" + OutlineItem.OBJECT_TYPE,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       "new" + OutlineItem.OBJECT_TYPE,
                                                       -1);

                              }

                          });

        this.actions.put ("new" + Note.OBJECT_TYPE,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       "new" + Note.OBJECT_TYPE,
                                                       -1);

                              }

                          });

        this.actions.put ("new-edit-needed" + Note.OBJECT_TYPE,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       "new-edit-needed" + Note.OBJECT_TYPE,
                                                       -1);

                              }

                          });

        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (Scene.OBJECT_TYPE).charAt (0))),
                "new" + Scene.OBJECT_TYPE);
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE).charAt (0))),
                "new" + OutlineItem.OBJECT_TYPE);
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0))),
                "new" + Note.OBJECT_TYPE);
        im.put (KeyStroke.getKeyStroke ("ctrl shift P"),
                "find-problems");

    }

    public JComponent getEditorWrapper (QTextEditor q)
    {

        FormLayout fl = new FormLayout ("50px, fill:200px:grow",
                                        "fill:p:grow");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.add (this.iconColumn,
                     cc.xy (1,
                            1));
        builder.add (q,
                     cc.xy (2,
                            1));

        JPanel p = builder.getPanel ();
        p.setBorder (null);

        return p;

    }

    public void showProblemFinderRuleConfig ()
    {

        if (this.problemFinderRuleConfig == null)
        {

            this.problemFinderRuleConfig = new ProblemFinderRuleConfig (this.projectViewer);

            this.problemFinderRuleConfig.init ();

        }

        this.problemFinderRuleConfig.setVisible (true);

    }

    public void doFillToolsPopupMenu (ActionEvent ev,
                                      JPopupMenu  p)
    {

        final QuollEditorPanel _this = this;

        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };

        JMenuItem mi = new JMenuItem ("Find Problems",
                                      Environment.getIcon ("eye",
                                                           Constants.ICON_MENU));
        mi.setActionCommand ("find-problems");
        mi.addActionListener (aa);
        mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                                   ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        p.add (mi);

    }

    public void doFillToolBar (JToolBar acts)
    {

        final QuollEditorPanel _this = this;

        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };

        final JButton b = UIUtils.createToolBarButton ("new",
                                                       Environment.replaceObjectNames ("Click to add a new {Outlineitem}, {Character}, {Note}, {Object} etc."),
                                                       "new",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu m = new JPopupMenu ();

                _this.addNewItemsForPopupMenu (m,
                                               b,
                                               -1);

                Component c = (Component) ev.getSource ();

                m.show (c,
                        10,
                        10);

            }

        };

        b.addActionListener (ab);

        acts.add (b);
/*
        acts.add (UIUtils.createToolBarButton ("timeline",
                                               "Click to view the Timeline",
                                               "timeline",
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.projectViewer.viewTimeline ();

                                                    }

                                                }));
*/
        acts.add (UIUtils.createToolBarButton ("information",
                                               Environment.replaceObjectNames ("Click to view/edit the {chapter} information"),
                                               "chapter-info",
                                               aa));

    }

    private void performAction (ActionEvent ev,
                                String      c,
                                int         pos)
    {

        if (c == null)
        {

            return;

        }

        if (c.equals ("new" + Scene.OBJECT_TYPE))
        {

            Scene s = new Scene (-1,
                                 this.chapter);

            AbstractActionHandler aah = new ChapterItemActionHandler (s,
                                                                      this,
                                                                      AbstractActionHandler.ADD,
                                                                      pos);

            aah.actionPerformed (ev);

            return;

        }

        if (c.equals ("new" + OutlineItem.OBJECT_TYPE))
        {

            AbstractActionHandler aah = new OutlineItemChapterActionHandler (this.chapter,
                                                                             this,
                                                                             pos);

            aah.actionPerformed (ev);

            return;

        }

        if (c.equals ("new" + Note.OBJECT_TYPE))
        {

            AbstractActionHandler aah = new NoteActionHandler (this.chapter,
                                                               this,
                                                               null);

            aah.actionPerformed (ev);

            return;

        }

        if (c.equals ("new-edit-needed" + Note.OBJECT_TYPE))
        {

            AbstractActionHandler aah = new NoteActionHandler (this.chapter,
                                                               this,
                                                               Note.EDIT_NEEDED_NOTE_TYPE);

            aah.actionPerformed (ev);

            return;

        }

    }

    private void performAction (ActionEvent ev,
                                int         pos)
    {

        String c = ev.getActionCommand ();

        this.performAction (ev,
                            c,
                            pos);

    }

    private void addNewItemsForPopupMenu (final JComponent popup,
                                          Component        showAt,
                                          int              pos)
    {

        final QuollEditorPanel _this = this;

        final PositionActionAdapter aa = new PositionActionAdapter (pos)
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev,
                                     this.pos);

            }

        };

        JMenuItem mi = new JMenuItem (Environment.getObjectTypeName (Scene.OBJECT_TYPE),
                                      Environment.getIcon (Scene.OBJECT_TYPE,
                                                           Constants.ICON_MENU));

        char fc = Character.toUpperCase (Environment.getObjectTypeName (Scene.OBJECT_TYPE).charAt (0));

        String pref = "Shortcut: Ctrl+Shift+";

        mi.setMnemonic (fc);
        mi.setToolTipText (pref + fc);
        // mi.setAccelerator (KeyStroke.getKeyStroke ("ctrl shift " + fc));

        mi.addActionListener (aa);

        mi.setActionCommand ("new" + Scene.OBJECT_TYPE);

        popup.add (mi);

        mi = new JMenuItem (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE),
                            Environment.getIcon (OutlineItem.OBJECT_TYPE,
                                                 Constants.ICON_MENU));

        fc = Character.toUpperCase (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE).charAt (0));

        mi.setMnemonic (fc);
        mi.setToolTipText (pref + fc);
        mi.setActionCommand ("new" + OutlineItem.OBJECT_TYPE);

        mi.addActionListener (aa);
        popup.add (mi);

        mi = new JMenuItem (Environment.getObjectTypeName (Note.OBJECT_TYPE),
                            Environment.getIcon (Note.OBJECT_TYPE,
                                                 Constants.ICON_MENU));
        fc = Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0));

        mi.setMnemonic (fc);
        mi.setToolTipText (pref + fc);
        mi.setActionCommand ("new" + Note.OBJECT_TYPE);

        mi.addActionListener (aa);

        popup.add (mi);

        mi = new JMenuItem (Note.EDIT_NEEDED_NOTE_TYPE + " " + Environment.getObjectTypeName (Note.OBJECT_TYPE),
                            Environment.getIcon ("edit-needed-note",
                                                 Constants.ICON_MENU));
        fc = 'E'; // Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0));

        mi.setMnemonic (fc);
        mi.setToolTipText (pref + fc);
        mi.setActionCommand ("new-edit-needed" + Note.OBJECT_TYPE);

        mi.addActionListener (aa);

        popup.add (mi);

        UIUtils.addNewAssetItemsToPopupMenu (popup,
                                             showAt,
                                             this.projectViewer,
                                             null,
                                             null);

    }

    public void doFillPopupMenu (final MouseEvent ev,
                                 final JPopupMenu popup)
    {

        final QuollEditorPanel _this = this;

        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };

        JMenu nm = new JMenu ("New");
        nm.setIcon (Environment.getIcon ("new",
                                         Constants.ICON_MENU));

        popup.add (nm);

        int pos = this.editor.viewToModel (new Point (ev.getPoint ().x, // 0,
                                                      ev.getPoint ().y));

        // This is needed to move to the correct character, the call above seems to get the character
        // before what was clicked on.
        // pos++;

        this.addNewItemsForPopupMenu (nm,
                                      this,
                                      pos);

/*
        JMenuItem mi = new JMenuItem (Environment.getObjectTypeName (Scene.OBJECT_TYPE),
                                      Environment.getIcon (Scene.OBJECT_TYPE,
                                                           false));

        Scene s = new Scene (-1,
                             this.chapter);

        AbstractActionHandler aah = new ChapterItemActionHandler (s,
                                                                  this,
                                                                  AbstractActionHandler.ADD,
                                                                  pos);

        mi.addActionListener (aah);
        nm.add (mi);

        mi = new JMenuItem (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE),
                            Environment.getIcon (OutlineItem.OBJECT_TYPE,
                                                 false));

        aah = new OutlineItemChapterActionHandler (this.chapter,
                                                   this,
                                                   pos);

        mi.addActionListener (aah);
        nm.add (mi);

        mi = new JMenuItem (Environment.getObjectTypeName (Note.OBJECT_TYPE),
                            Environment.getIcon (Note.OBJECT_TYPE,
                                                 false));

        mi.addActionListener (new NoteActionHandler (this.chapter,
                                                     this,
                                                     pos));
        nm.add (mi);
*/
/*
        UIUtils.addNewAssetItemsToPopupMenu (nm,
                                             this,
                                             this.projectViewer);
*/
        JMenuItem mi = new JMenuItem ("Chapter Below",
                                      Environment.getIcon (Chapter.OBJECT_TYPE,
                                                           Constants.ICON_MENU));
        AddChapterActionHandler ac = new AddChapterActionHandler (this.chapter.getBook (),
                                                                  this.chapter,
                                                                  this.projectViewer);
        ac.setPopupOver (this);
        mi.addActionListener (ac);
        nm.add (mi);

        mi = new JMenuItem ("Find Problems",
                            Environment.getIcon ("eye",
                                                 Constants.ICON_MENU));

        mi.setActionCommand ("find-problems");

        mi.addActionListener (aa);

        popup.add (mi);

    }

    public IconColumn getIconColumn ()
    {

        return this.iconColumn;

    }

    public QTextEditor getEditor ()
    {

        return this.editor;

    }

    public void removeItem (ChapterItem c)
    {

        this.iconColumn.removeItem (c);

    }

    public void addItem (ChapterItem c)
                  throws GeneralException
    {

        this.iconColumn.addItem (c,
                                 this);

    }

    public void scrollToOutlineItem (OutlineItem oi)
                              throws GeneralException
    {

        this.scrollToPosition (oi.getPosition ());

    }

    public void scrollToItem (ChapterItem i)
                       throws GeneralException
    {

        this.scrollToPosition (i.getPosition ());

    }

    public void editNote (Note n)
                   throws GeneralException
    {

        this.scrollToNote (n);

        new NoteActionHandler (n,
                               this).actionPerformed (new ActionEvent (this,
                                                                       0,
                                                                       "edit"));

    }

    public void showNote (Note n)
                   throws GeneralException
    {

        this.scrollToNote (n);

        new ShowNoteActionHandler (n,
                                   this).showItem ();

    }

    public void editScene (Scene s)
                    throws GeneralException
    {

        this.scrollToItem (s);

        new ChapterItemActionHandler (s,
                                      this,
                                      AbstractActionHandler.EDIT,
                                      s.getPosition ()).actionPerformed (new ActionEvent (this,
                                                                                          0,
                                                                                          "edit"));

    }

    public void showScene (Scene s)
                    throws GeneralException
    {

        this.scrollToItem (s);

        new ShowSceneActionHandler (s,
                                    this).showItem ();

    }

    public void editOutlineItem (OutlineItem n)
                          throws GeneralException
    {

        this.scrollToOutlineItem (n);

        new ChapterItemActionHandler (n,
                                      this,
                                      AbstractActionHandler.EDIT,
                                      n.getPosition ()).actionPerformed (new ActionEvent (this,
                                                                                          0,
                                                                                          "edit"));

    }

    public void showOutlineItem (OutlineItem n)
                          throws GeneralException
    {

        this.scrollToOutlineItem (n);

        new ShowOutlineItemActionHandler (n,
                                          this).showItem ();

    }

    public void scrollToNote (Note n)
                       throws GeneralException
    {

        this.scrollToPosition (n.getPosition ());

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

    public List<Component> getTopLevelComponents ()
    {

        List<Component> l = new ArrayList ();
        l.add (this.iconColumn);
        l.add (this.editor);

        return l;

    }

    public void refresh (NamedObject n)
    {

        // No need to do anything.

    }

    public void setSpellCheckingEnabled (boolean v)
    {

        this.editor.setSpellCheckEnabled (v);

        String type = (v ? "off" : "on");

        this.setToolBarButtonIcon ("toggle-spellcheck",
                                   "Click turn the spell checker " + type,
                                   "spellchecker-turn-" + type);            

    }

    public void doInit ()
                 throws GeneralException
    {

        final QuollEditorPanel _this = this;

        ProjectViewer.addAssetActionMappings (this,
                                              this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW),
                                              this.getActionMap ());
        
        this.problemFinderPanel = new Box (BoxLayout.Y_AXIS);
        this.problemFinderPanel.setOpaque (true);
        this.problemFinderPanel.setBackground (UIUtils.getComponentColor ());
        this.problemFinderPanel.setBorder (new MatteBorder (1,
                                                            0,
                                                            0,
                                                            0,
                                                            Environment.getBorderColor ()));
        
        java.util.List<JButton> hbuts = new ArrayList ();
        
        hbuts.add (UIUtils.createButton ("config",
                                        Constants.ICON_MENU,
                                        "Click to configure the text rules.",
                                        new ActionAdapter ()
                                        {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                _this.showProblemFinderRuleConfig ();
                                                
                                            }
                                            
                                        }));

        hbuts.add (UIUtils.createButton ("cancel",
                                        Constants.ICON_MENU,
                                        "Click to stop looking for problems.",
                                        new ActionAdapter ()
                                        {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.problemFinderPanel.setVisible (false);
                            
                                                _this.problemFinder.reset ();
                                                                                                
                                            }
                                            
                                        }));

        hbuts.add (UIUtils.createHelpPageButton ("chapters/problem-finder",
                                                Constants.ICON_MENU,
                                                null));
        
        JToolBar tb = UIUtils.createButtonBar (hbuts);

        Header h = UIUtils.createHeader ("Find Problems",
                                         Constants.SUB_PANEL_TITLE,
                                         "eye",
                                         tb);
        /*
        Header h = new Header ("Find Problems",
                               Environment.getIcon ("eye",
                                                    Constants.ICON_SUB_PANEL_MAIN),
                               tb);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (14d)).deriveFont (Font.PLAIN));
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setPadding (new Insets (5, 5, 0, 5));
        */
        this.problemFinderPanel.add (h);
        this.problemFinderPanel.setVisible (false);
        this.problemFinderPanel.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.problemFinder = new ProblemFinder (this);
        this.problemFinder.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.problemFinderPanel.add (this.problemFinder);

        Box buts = new Box (BoxLayout.X_AXIS);

        buts.setAlignmentX (Component.LEFT_ALIGNMENT);

        JButton cbut = UIUtils.createButton ("config",
                                             Constants.ICON_MENU,
                                             "Click to configure the text rules.",
                                             new ActionAdapter ()
                                             {

                                                 public void actionPerformed (ActionEvent ev)
                                                 {
                                                
                                                     _this.showProblemFinderRuleConfig ();
                                                
                                                 }
                                            
                                             });
        UIUtils.setDefaultCursor (cbut);
        buts.add (cbut);

        buts.add (Box.createHorizontalStrut (5));

        JButton prev = new JButton (Environment.getIcon ("previous",
                                                         Constants.ICON_MENU));
        prev.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    _this.problemFinder.previous ();

                    _this.updateIgnoredProblemsLabel ();

                } catch (Exception e)
                {

                    Environment.logError ("Unable to goto previous sentence",
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to go to previous sentence.");

                }

            }

        });

        prev.setToolTipText ("Go back to the previous sentence with problems");

        buts.add (prev);
        buts.add (Box.createHorizontalStrut (5));

        JButton next = UIUtils.createButton ("Next",
                                             "next");

        next.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        _this.problemFinder.next ();

                        _this.updateIgnoredProblemsLabel ();

                        _this.problemFinderPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                                _this.problemFinderPanel.getPreferredSize ().height));

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to goto next sentence",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to go to next sentence.");

                    }

                }

            });

        next.setToolTipText ("Go to the next sentence with problems");
        next.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.add (next);

        buts.add (Box.createHorizontalStrut (5));

        JButton cancel = UIUtils.createButton ("Finish",
                                               null);

        cancel.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.problemFinderPanel.setVisible (false);

                    _this.problemFinder.reset ();

                }

            });

        cancel.setToolTipText ("Stop looking for problems");
        cancel.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.add (cancel);
                
        buts.add (Box.createHorizontalGlue ());

        this.ignoredProblemsLabel = UIUtils.createClickableLabel ("",
                                                                  Environment.getIcon ("warning",
                                                                                       Constants.ICON_MENU)); 
        this.ignoredProblemsLabel.setVisible (false);

        this.updateIgnoredProblemsLabel ();
        
        this.ignoredProblemsLabel.addMouseListener (new MouseAdapter ()
        {
        
            public void mousePressed (MouseEvent ev)
            {
                
                int s = _this.problemFinder.getIgnoredIssues ().size ();
                
                if (JOptionPane.showConfirmDialog (_this.projectViewer,
                                                   "Please confirm you wish to un-ignore the " + s + " problem" + (s > 1 ? "s" :"") + "?",
                                                   UIUtils.getFrameTitle ("Un-ignore problems"),
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                
                    _this.problemFinder.removeAllIgnores ();
                    
                    _this.updateIgnoredProblemsLabel ();

                }
                
            }
            
        });
        
        buts.add (this.ignoredProblemsLabel);

        buts.setBorder (new EmptyBorder (10,
                                         10,
                                         5,
                                         10));

        this.problemFinderPanel.add (buts);

        this.add (this.problemFinderPanel);

        this.actions.put ("chapter-info",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.projectViewer.viewChapterInformation (_this.chapter);

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to show chapter information for: " +
                                                            _this.chapter,
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                Environment.replaceObjectNames ("Unable to show {chapter}."));

                                  }

                              }

                          });

        this.actions.put ("find-problems",
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.showProblemFinder ();

                              }

                          });

        this.iconColumn.setOutlineItems (this.chapter.getOutlineItems (),
                                         this);

        this.iconColumn.setNotes (this.chapter.getNotes (),
                                  this);

        this.iconColumn.setScenes (this.chapter.getScenes (),
                                   this);

    }

    public void showProblemFinder ()
    {
        
        try
        {
              
            this.problemFinder.start ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to start problem finding",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to open problem finding panel");

            return;

        }

        this.problemFinderPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                                this.problemFinderPanel.getPreferredSize ().height));

        this.problemFinderPanel.setVisible (true);

        this.projectViewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                             ProjectEvent.SHOW);
        
        try
        {
        
            this.problemFinder.next ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to start problem finding",
                                  e);
            
            UIUtils.showErrorMessage (this,
                                      "Unable to start problem finding");
            
            return;
                        
        }
        
    }

    private void updateIgnoredProblemsLabel ()
    {
        
        int s = this.problemFinder.getIgnoredIssues ().size ();
        
        if (s > 0)
        {
            
            this.ignoredProblemsLabel.setText (Environment.replaceObjectNames (s + " problem" + (s > 1 ? "s" : "") + " currently ignored in this {chapter}, click to un-ignore."));
            this.ignoredProblemsLabel.setVisible (true);
        
        } else {
            
            this.ignoredProblemsLabel.setVisible (false);
            
        }
        
        this.validate ();
        this.repaint ();
        
    }

    public void close ()
    {

        super.close ();

        this.problemFinder.saveIgnores ();

    }

    public ProblemFinderRuleConfig getProblemFinderRuleConfig ()
    {

        return this.problemFinderRuleConfig;

    }

    public void removeIgnoreCheckboxesForRule (Rule r)
    {

        if (this.problemFinder != null)
        {

            this.problemFinder.removeCheckboxesForRule (r);

        }

    }

    public void restoreBackgroundColor ()
    {

        super.restoreBackgroundColor ();

        this.iconColumn.setBackground (IconColumn.defaultBGColor);

    }

    public void setBackgroundColor (Color c)
    {

        super.setBackgroundColor (c);

        if (c.equals (Color.white))
        {

            this.iconColumn.setBackground (IconColumn.defaultBGColor);

        } else
        {

            this.iconColumn.setBackground (c);

        }

    }

}
