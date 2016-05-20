package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.im.*;
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
import java.awt.datatransfer.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

import com.quollwriter.text.*;
import com.quollwriter.text.rules.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ScrollablePanel;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.renderers.*;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;

public class QuollEditorPanel extends AbstractEditableEditorPanel implements ChapterItemViewer
{

    public static final String SHOW_WORD_CLOUD_ACTION_NAME = "show-word-cloud";
    public static final String SHOW_EDITORS_ACTION_NAME = "show-editors";
    public static final String CHAPTER_INFO_ACTION_NAME = "chapter-info";
    public static final String PROBLEM_FINDER_ACTION_NAME = "problem-finder";
    public static final String SPLIT_CHAPTER_ACTION_NAME = "split-chapter";
    public static final String SET_EDIT_COMPLETE_ACTION_NAME = "set-edit-complete";
    public static final String REMOVE_EDIT_POINT_ACTION_NAME = "remove-edit-point";

    public static final String NEW_CHAPTER_ACTION_NAME = "new" + Chapter.OBJECT_TYPE;
    public static final String NEW_SCENE_ACTION_NAME = "new" + Scene.OBJECT_TYPE;
    public static final String NEW_OUTLINE_ITEM_ACTION_NAME = "new" + OutlineItem.OBJECT_TYPE;
    public static final String NEW_NOTE_ACTION_NAME = "new" + Note.OBJECT_TYPE;
    public static final String NEW_EDIT_NEEDED_NOTE_ACTION_NAME = "new-edit-needed-" + Note.OBJECT_TYPE;
    
    public static final String TAB = String.valueOf ('\t');

    public static final String SECTION_BREAK_FIND = "***";
    public static final String SECTION_BREAK = "*" + TAB + TAB + "*" + TAB + TAB + "*";

    private static final CutNPasteTransferHandler cnpTransferHandler = new CutNPasteTransferHandler ();
    
    // private QTextEditor editor = null;
    private IconColumn              iconColumn = null;
    protected ProjectViewer         projectViewer = null;
    private Box                     problemFinderPanel = null;
    private ProblemFinder           problemFinder = null;
    //private ProblemFinderRuleConfig problemFinderRuleConfig = null;
    private JLabel                  ignoredProblemsLabel = null;
    private int                     lastCaret = -1;
    private ChapterItemTransferHandler chItemTransferHandler = null;
    private BlockPainter highlight = null;    

    public QuollEditorPanel(ProjectViewer pv,
                            Chapter       c)
                     throws GeneralException
    {

        super (pv,
               c);
        
        this.projectViewer = pv;

        final QuollEditorPanel _this = this;

        DefaultIconProvider iconProv = new DefaultIconProvider ()
        {

            @Override
            public ImageIcon getIcon (DataObject d,
                                      int        type)
            {
               
               if (d instanceof Note)
               {
                  
                  Note n = (Note) d;
                  
                  if (n.isEditNeeded ())
                  {
                     
                     return super.getIcon (Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                           type);
                     
                  }
                  
               }
               
               return super.getIcon (d,
                                     type);
               
            }

         
        };
        
        this.highlight = new BlockPainter (Environment.getHighlightColor ());                
        
        this.iconColumn = new IconColumn (this,
                                          this.projectViewer.getIconProvider (),
                                          this.projectViewer.getChapterItemViewPopupProvider ());

        this.iconColumn.addMouseListener (this);

        this.iconColumn.addMouseListener (new MouseAdapter ()
        {

            public void mouseClicked (MouseEvent ev)
            {
               
               if (ev.getClickCount () == 2)
               {

                  JPopupMenu popup = new JPopupMenu ();
                  
                  // Convert the mouse position to a point in the text.
                  
                  String pref = "Shortcut: Ctrl+Shift+";
                  
                  JMenuItem mi = null;

                  mi = UIUtils.createMenuItem (Environment.getObjectTypeName (Scene.OBJECT_TYPE),
                                               Scene.OBJECT_TYPE,
                                               _this.getActionListenerForTextPosition (NEW_SCENE_ACTION_NAME,
                                                                                       ev.getPoint ()),
                                               null,
                                               null);
                                             
                  popup.add (mi);
                      
                  char fc = Character.toUpperCase (Environment.getObjectTypeName (Scene.OBJECT_TYPE).charAt (0));
              
                  mi.setMnemonic (fc);
                  mi.setToolTipText (pref + fc);
              
                  mi = UIUtils.createMenuItem (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE),
                                               OutlineItem.OBJECT_TYPE,
                                               _this.getActionListenerForTextPosition (NEW_OUTLINE_ITEM_ACTION_NAME,
                                                                                       ev.getPoint ()),
                                               null,
                                               null);
          
                  popup.add (mi);
          
                  fc = Character.toUpperCase ("O".charAt (0));
          
                  mi.setMnemonic (fc);
                  mi.setToolTipText (pref + fc);
          
                  mi = UIUtils.createMenuItem (Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                               Note.OBJECT_TYPE,
                                               _this.getActionListenerForTextPosition (NEW_NOTE_ACTION_NAME,
                                                                                       ev.getPoint ()),
                                               null,
                                               null);
      
                  popup.add (mi);
                                            
                  fc = Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0));
          
                  mi.setMnemonic (fc);
                  mi.setToolTipText (pref + fc);
      
                  mi = UIUtils.createMenuItem (Note.EDIT_NEEDED_NOTE_TYPE + " " + Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                               Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                               _this.getActionListenerForTextPosition (NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                                                                       ev.getPoint ()),
                                               null,
                                               null);
              
                  popup.add (mi);
              
                  fc = 'E'; // Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0));
          
                  mi.setMnemonic (fc);
                  mi.setToolTipText (pref + fc);
                        
                  popup.show ((Component) ev.getSource (),
                              ev.getPoint ().x,
                              ev.getPoint ().y);
                  
               }
               
            }
         
        });
        
        this.chItemTransferHandler = new ChapterItemTransferHandler (this.getIconColumn ());
        
        this.setTransferHandler (this.chItemTransferHandler);

        this.actions.put (SPLIT_CHAPTER_ACTION_NAME,
                          new ActionAdapter ()
                          {
                           
                              @Override
                              public void actionPerformed (ActionEvent ev)
                              {
                                 
                                 
                                 
                                 new SplitChapterActionHandler (_this.chapter,
                                                                _this.projectViewer).actionPerformed (ev);
        
                              }
                              
                          });
        
        this.actions.put (REMOVE_EDIT_POINT_ACTION_NAME,
                          new ActionAdapter ()
                          {
                           
                             public void actionPerformed (ActionEvent ev)
                             {
                               
                                 try
                                 {
                               
                                    _this.projectViewer.removeChapterEditPosition (_this.chapter);
                                    
                                 } catch (Exception e) {
                                    
                                    Environment.logError ("Unable to remove edit position for chapter: " +
                                                          _this.chapter,
                                                          e);
                                    
                                    UIUtils.showErrorMessage (_this.projectViewer,
                                                              "Unable to remove edit position.");
                                    
                                                                               
                                 }

                             }
                           
                          });
        
        this.actions.put (SET_EDIT_COMPLETE_ACTION_NAME,
                          new ActionAdapter ()
                          {
                           
                             public void actionPerformed (ActionEvent ev)
                             {
                               
                                 try
                                 {
                                    
                                    _this.projectViewer.setChapterEditComplete (_this.chapter,
                                                                                true);

                                 } catch (Exception e) {
                                    
                                    Environment.logError ("Unable to set chapter edit complete: " +
                                                          _this.chapter,
                                                          e);
                                    
                                    UIUtils.showErrorMessage (_this.projectViewer,
                                                              "Unable to set {chapter} as edit complete.");
                                                                               
                                 }
                                                                               
                             }
                           
                          });
        
        this.actions.put (NEW_CHAPTER_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       NEW_CHAPTER_ACTION_NAME,
                                                       -1);

                              }

                          });
        
        this.actions.put (NEW_SCENE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       NEW_SCENE_ACTION_NAME,
                                                       -1);

                              }

                          });

        this.actions.put (NEW_OUTLINE_ITEM_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       NEW_OUTLINE_ITEM_ACTION_NAME,
                                                       -1);

                              }

                          });

        this.actions.put (NEW_NOTE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       NEW_NOTE_ACTION_NAME,
                                                       -1);

                              }

                          });

        this.actions.put (NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.performAction (ev,
                                                       NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                                       -1);

                              }

                          });

        this.actions.put (CHAPTER_INFO_ACTION_NAME,
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
                          
        this.actions.put (SHOW_EDITORS_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.projectViewer.viewEditors ();

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to show editors",
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                Environment.replaceObjectNames ("Unable to show editors."));

                                  }

                              }

                          });

        this.actions.put (SHOW_WORD_CLOUD_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  try
                                  {

                                      _this.projectViewer.viewWordCloud ();

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to show word cloud",
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                Environment.replaceObjectNames ("Unable to show word cloud."));

                                  }

                              }

                          });

        this.actions.put (PROBLEM_FINDER_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.showProblemFinder ();

                              }

                          });
                          
        InputMap im = this.editor.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Remove ctrl+shift+O from the when_focused set since it conflicts.
        this.editor.getInputMap (JComponent.WHEN_FOCUSED).put (KeyStroke.getKeyStroke ("ctrl shift O"),
                                                               "none");
        
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (Scene.OBJECT_TYPE).charAt (0))),
                NEW_SCENE_ACTION_NAME);

        im.put (KeyStroke.getKeyStroke ("ctrl shift O"),
                NEW_OUTLINE_ITEM_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0))),
                NEW_NOTE_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke ("ctrl shift E"),
                NEW_EDIT_NEEDED_NOTE_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke ("ctrl shift P"),
                PROBLEM_FINDER_ACTION_NAME);

        this.editor.setTransferHandler (QuollEditorPanel.cnpTransferHandler);
                
    }
   
    public int getTextPositionForMousePosition (Point p)
    {
      
       Point pp = p;
      
       if (this.iconColumn.getMousePosition () != null)
       {
         
          pp = new Point (0,
                          p.y);
         
       }

       return this.editor.viewToModel (pp);
      
    }
    
    public ActionListener getActionListenerForTextPosition (final String actionName,
                                                            final Point  p)
    {
      
         final QuollEditorPanel _this = this;

         final int pos = this.getTextPositionForMousePosition (p);
      
         return new ActionAdapter ()
         {
            
            public void actionPerformed (ActionEvent ev)
            {
                              
               _this.performAction (ev,
                                    actionName,
                                    pos);
               
            }
            
         };      
      
    }
    

    public ChapterItemTransferHandler getChapterItemTransferHandler ()
    {
        
        return this.chItemTransferHandler;
        
    }
    
    public JComponent getEditorWrapper (QTextEditor q)
    {

        Box b = new /*Box*/ com.quollwriter.ui.components.ScrollableBox (BoxLayout.X_AXIS);
        b.add (this.iconColumn);
        b.add (q);
        q.setMaximumSize (new Dimension (Integer.MAX_VALUE, Integer.MAX_VALUE));

        q.setMinimumSize (new Dimension (200, 200));
        q.setAlignmentY (Component.TOP_ALIGNMENT);
        q.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.iconColumn.setAlignmentY (Component.TOP_ALIGNMENT);
        this.iconColumn.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.iconColumn.setMinimumSize (new Dimension (50, 200));
        this.iconColumn.setPreferredSize (new Dimension (50, 200));
        this.iconColumn.setMaximumSize (new Dimension (50, Integer.MAX_VALUE));
        
        JPanel p = new ScrollablePanel (new BorderLayout ());
        p.add (b);

        return p;
        
    }
    
    public void showProblemFinderRuleConfig ()
    {

         this.projectViewer.showProblemFinderRuleConfig ();
    /*
        if (this.problemFinderRuleConfig == null)
        {

            this.problemFinderRuleConfig = new ProblemFinderRuleConfig (this.projectViewer);

            this.problemFinderRuleConfig.init ();

        }

        this.problemFinderRuleConfig.setVisible (true);
*/
    }

    public void doFillToolsPopupMenu (ActionEvent ev,
                                      JPopupMenu  p)
    {

         // Need a more elegant way of handling this, maybe via cue.language?
         if (this.projectViewer.isLanguageEnglish ())
         {
    
            p.add (this.createMenuItem ("Find Problems",
                                        Constants.PROBLEM_FINDER_ICON_NAME,
                                        PROBLEM_FINDER_ACTION_NAME,
                                         KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                                                 ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK)));

         }
/*        
        p.add (this.createMenuItem ("Editors",
                                    Constants.EDITORS_ICON_NAME,
                                    SHOW_EDITORS_ACTION_NAME,
                                    null));
*/
/*
        p.add (this.createMenuItem ("Word Cloud",
                                    Constants.EDITORS_ICON_NAME,
                                    SHOW_WORD_CLOUD_ACTION_NAME,
                                    null));
  */                                  
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
                                                       "Click to add a new {Outlineitem}, {Character}, {Note}, {Object} etc.",
                                                       "new",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu m = new JPopupMenu ();

                _this.addNewItemsForPopupMenu (m,
                                               b,
                                               -1,
                                               false);

                Component c = (Component) ev.getSource ();

                m.show (c,
                        10,
                        10);

            }

        };

        b.addActionListener (ab);

        acts.add (b);
        acts.add (this.createToolbarButton (Constants.INFO_ICON_NAME,
                                            "Click to view/edit the {chapter} information",
                                            CHAPTER_INFO_ACTION_NAME));

    }

    private void performAction (ActionEvent ev,
                                String      c,
                                int         pos)
    {

        if (c == null)
        {

            return;

        }

        if (c.equals (NEW_CHAPTER_ACTION_NAME))
        {

            Action a = this.projectViewer.getAction (ProjectViewer.NEW_CHAPTER_ACTION,
                                                     this.chapter);
        
            if (a != null)
            {
               
               a.actionPerformed (ev);
               
            }
        
            return;

        }
        
        if (c.equals (NEW_SCENE_ACTION_NAME))
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

        if (c.equals (NEW_OUTLINE_ITEM_ACTION_NAME))
        {

            AbstractActionHandler aah = new OutlineItemChapterActionHandler (this.chapter,
                                                                             this,
                                                                             pos);

            aah.actionPerformed (ev);

            return;

        }

        if (c.equals (NEW_NOTE_ACTION_NAME))
        {

            AbstractActionHandler aah = new NoteActionHandler (this.chapter,
                                                               this,
                                                               pos);

            aah.actionPerformed (ev);

            return;

        }

        if (c.equals (NEW_EDIT_NEEDED_NOTE_ACTION_NAME))
        {

            AbstractActionHandler aah = new NoteActionHandler (this.chapter,
                                                               this,
                                                               Note.EDIT_NEEDED_NOTE_TYPE,
                                                               pos);

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
/*
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
*/
    private void addNewItemsForPopupMenu (final JComponent popup,
                                                Component  showAt,
                                                int        pos,
                                                boolean    compress)
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

        if (compress)
        {
        
            List<JComponent> buts = new ArrayList ();

            buts.add (this.createButton (Scene.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         String.format ("Add a new {%s}", Scene.OBJECT_TYPE),
                                         NEW_SCENE_ACTION_NAME,
                                         aa));

            buts.add (this.createButton (OutlineItem.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         String.format ("Add a new {%s}", OutlineItem.OBJECT_TYPE),
                                         NEW_OUTLINE_ITEM_ACTION_NAME,
                                         aa));

            buts.add (this.createButton (Note.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         String.format ("Add a new {%s}", Note.OBJECT_TYPE),
                                         NEW_NOTE_ACTION_NAME,
                                         aa));

            buts.add (this.createButton ("edit-needed-note",
                                         Constants.ICON_MENU,
                                         String.format ("Add a new Edit Needed Note"),
                                         NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                         aa));
                                  
            buts.add (this.createButton (Chapter.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         String.format ("Add a new {%s}", Chapter.OBJECT_TYPE),
                                         NEW_CHAPTER_ACTION_NAME,
                                         aa));

            if (popup instanceof JPopupMenu)
            {                                            
            
               JPopupMenu pm = (JPopupMenu) popup;
            
               pm.addSeparator ();
                                                           
               popup.add (UIUtils.createPopupMenuButtonBar ("New",
                                                            pm,
                                                            buts));
        
               UIUtils.addNewAssetItemsAsToolbarToPopupMenu (pm,
                                                             null,
                                                             (ProjectViewer) this.projectViewer,
                                                             null,
                                                             null);
                    
               pm.addSeparator ();
               
            }
        
        } else {
        
            String pref = "Shortcut: Ctrl+Shift+";
            
            JMenuItem mi = null;
            
            mi = this.createMenuItem (Environment.getObjectTypeName (Scene.OBJECT_TYPE),
                                      Scene.OBJECT_TYPE,
                                      NEW_SCENE_ACTION_NAME,
                                      null,
                                      aa);
                
            popup.add (mi);
                
            char fc = Character.toUpperCase (Environment.getObjectTypeName (Scene.OBJECT_TYPE).charAt (0));
        
            mi.setMnemonic (fc);
            mi.setToolTipText (pref + fc);
        
            mi = this.createMenuItem (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE),
                                      OutlineItem.OBJECT_TYPE,
                                      NEW_OUTLINE_ITEM_ACTION_NAME,
                                      null,
                                      aa);
    
            popup.add (mi);
    
            fc = Character.toUpperCase (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE).charAt (0));
    
            mi.setMnemonic (fc);
            mi.setToolTipText (pref + fc);
    
            mi = this.createMenuItem (Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                      Note.OBJECT_TYPE,
                                      NEW_NOTE_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);
                                      
            fc = Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0));
    
            mi.setMnemonic (fc);
            mi.setToolTipText (pref + fc);

            mi = this.createMenuItem (Note.EDIT_NEEDED_NOTE_TYPE + " " + Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                      Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                      NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                      null,
                                      aa);
        
            popup.add (mi);
        
            mi = this.createMenuItem (Environment.getObjectTypeName (Chapter.OBJECT_TYPE),
                                      Chapter.OBJECT_TYPE,
                                      NEW_CHAPTER_ACTION_NAME,
                                      null,
                                      aa);
        
            popup.add (mi);
        
            fc = 'E'; // Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).charAt (0));
    
            mi.setMnemonic (fc);
            mi.setToolTipText (pref + fc);
    
            UIUtils.addNewAssetItemsToPopupMenu (popup,
                                                 showAt,
                                                 this.projectViewer,
                                                 null,
                                                 null);

        }
                                                 
    }
    
    public void showIconColumn (boolean v)
    {
      
         this.iconColumn.setVisible (v);
         
         this.validate ();
         this.repaint ();
      
    }
    /*
    public void setEditPosition (int textPos)
    {

        try
        {

            int tl = Utils.stripEnd (this.editor.getText ()).length ();
        
            if (textPos > tl)
            {
                
                textPos = tl;
                
            }
        
            this.iconColumn.setEditPosition (textPos);
            
            // See if we are on the last line (it may be that the user is in the icon
            // column).
            Rectangle pp = this.editor.modelToView (textPos);
            
            if (Environment.getUserProperties ().getPropertyAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME))
            {
            
                if (textPos < tl)
                {
                    
                    Rectangle ep = this.editor.modelToView (tl);

                    boolean complete = false;
                    
                    if (ep.y == pp.y)                    
                    {
                        
                        complete = true;
                        
                    }
                        
                    // Last line.
                    this.chapter.setEditComplete (complete);
                                                                
                }

            }

            ((ProjectViewer) this.projectViewer).reloadChapterTree ();
            
            this.projectViewer.saveObject (this.chapter,
                                           false);
                                                                
        } catch (Exception e) {
            
            Environment.logError ("Unable to set edit position for chapter: " +
                                  this.chapter,
                                  e);
                                                            
        }
        
    }
    */
    public void setEditPosition (Point mouseP)
                          throws Exception
    {

        this.projectViewer.setChapterEditPosition (this.chapter,
                                                   this.editor.viewToModel (mouseP));
        
    }
    /*
    public void removeEditPosition ()
    {
        
        try
        {
            
            this.iconColumn.setEditPosition (-1);

            this.setEditComplete (false);
            
            this.projectViewer.saveObject (this.chapter,
                                           false);

            ((ProjectViewer) this.projectViewer).reloadChapterTree ();
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to set edit position for chapter: " +
                                  this.chapter,
                                  e);
                                                            
        }
        
    }
    */
    /*
    public void setEditComplete (boolean v)
    {
        
        try
        {
            
            this.chapter.setEditComplete (v);
            
            if (v)
            {
            
                int tl = Utils.stripEnd (this.editor.getText ()).length ();
            
                this.iconColumn.setEditPosition (tl);
                                
            }
            
            this.projectViewer.saveObject (this.chapter,
                                           false);

            this.iconColumn.init ();

            if (this.projectViewer instanceof ProjectViewer)
            {
                
                ProjectViewer pv = (ProjectViewer) this.projectViewer;
                
                pv.reloadChapterTree ();
                                                                        
            }
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to set edit complete for chapter: " +
                                  this.chapter,
                                  e);
                                                            
        }        
        
    }
    */
    public void doFillPopupMenu (final MouseEvent ev,
                                 final JPopupMenu popup,
                                       boolean    compress)
    {

        final QuollEditorPanel _this = this;

        // Get the mouse position, don't get it later since the mouse could have moved.
        Point mP = this.editor.getMousePosition ();        
        
        if (mP == null)
        {
            
            mP = this.iconColumn.getMousePosition ();
            
        }
        
        final Point mouseP = mP;
        
        int pos = this.getTextPositionForMousePosition (ev.getPoint ());

        // This is needed to move to the correct character, the call above seems to get the character
        // before what was clicked on.
        // pos++;
        
        JMenuItem mi = null;
                                 
        if (compress)
        {
                         
            List<JComponent> buts = new ArrayList ();
            
            buts.add (this.createButton (Constants.SAVE_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Save {Chapter}",
                                         SAVE_ACTION_NAME));
                                                                                                
            if ((this.editor.getCaret ().getDot () > 0)
                ||
                (this.editor.getSelectionStart () > 0)
               )
            {

               if (this.editor.getCaret ().getDot () < this.editor.getTextWithMarkup ().getText ().length ())
               {
            
                  buts.add (this.createButton (Constants.CHAPTER_SPLIT_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Split {Chapter}",
                                               SPLIT_CHAPTER_ACTION_NAME));

               }
                                            
            }
                        
            buts.add (this.createButton (Constants.PROBLEM_FINDER_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Find Problems",
                                         PROBLEM_FINDER_ACTION_NAME));
                                            
            buts.add (this.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_MENU,
                                         "Find",
                                         Constants.SHOW_FIND_ACTION));
                                            
            popup.add (UIUtils.createPopupMenuButtonBar (Environment.replaceObjectNames ("{Chapter}"),
                                                         popup,
                                                         buts));

            buts = new ArrayList ();
                                                                     
            buts.add (UIUtils.createButton (Constants.EDIT_IN_PROGRESS_ICON_NAME,
                                            Constants.ICON_MENU,
                                            "Set Edit Point",
                                            new ActionAdapter ()
                                            {
                                               
                                               public void actionPerformed (ActionEvent ev)
                                               {
                                                   
                                                   try
                                                   {
                                                   
                                                      _this.setEditPosition (mouseP);
                                                      
                                                   } catch (Exception e) {
                                                      
                                                      Environment.logError ("Unable to set edit position for chapter: " +
                                                                            _this.chapter,
                                                                            e);
                                                      
                                                      UIUtils.showErrorMessage (_this.projectViewer,
                                                                                "Unable to set edit position.");
                                                                                                 
                                                   }
                                                   
                                               }
                                               
                                            }));

            if (this.chapter.getEditPosition () > 0)
            {
    
                buts.add (this.createButton (Constants.REMOVE_EDIT_POINT_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Remove Edit Point",
                                             REMOVE_EDIT_POINT_ACTION_NAME));
                   
            }

            if (!this.chapter.isEditComplete ())
            {
                
                buts.add (this.createButton (Constants.EDIT_COMPLETE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Set as Edit Complete",
                                             SET_EDIT_COMPLETE_ACTION_NAME));
                    
            } 

            popup.add (UIUtils.createPopupMenuButtonBar (null,
                                                         popup,
                                                         buts));

            this.addNewItemsForPopupMenu (popup,
                                          this,
                                          pos,
                                          compress);        
                         
        } else {

            // Save.
            
            mi = this.createMenuItem ("Save {Chapter}",
                                      Constants.SAVE_ICON_NAME,
                                      SAVE_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_S);

            popup.add (mi);
                
            JMenu m = new JMenu (Environment.replaceObjectNames ("{Chapter} Edit"));
            m.setIcon (Environment.getIcon (Constants.EDIT_ICON_NAME,
                                            Constants.ICON_MENU));
    
            popup.add (m);
            
            if ((this.editor.getCaret ().getDot () > 0)
                ||
                (this.editor.getSelectionStart () > 0)
               )
            {
    
                m.add (this.createMenuItem ("Split {Chapter}",
                                            Constants.CHAPTER_SPLIT_ICON_NAME,
                                            SPLIT_CHAPTER_ACTION_NAME,
                                            null));
            
            }
            
            mi = UIUtils.createMenuItem ("Set Edit Point",
                                         Constants.EDIT_IN_PROGRESS_ICON_NAME,
                                         new ActionAdapter ()
                                         {
                                            
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                try
                                                {
                                                
                                                   _this.setEditPosition (mouseP);
                                                   
                                                } catch (Exception e) {
                                                   
                                                   Environment.logError ("Unable to set edit position for chapter: " +
                                                                         _this.chapter,
                                                                         e);
                                                   
                                                   UIUtils.showErrorMessage (_this.projectViewer,
                                                                             "Unable to set edit position.");
                                                                                              
                                                }
                                                                                                
                                            }
                                            
                                         });
                
            m.add (mi);
    
            if (this.chapter.getEditPosition () > 0)
            {
    
                m.add (this.createMenuItem ("Remove Edit Point",
                                            Constants.REMOVE_EDIT_POINT_ICON_NAME,
                                            REMOVE_EDIT_POINT_ACTION_NAME,
                                            null));
    
            }
    
            if (!this.chapter.isEditComplete ())
            {
                
                m.add (this.createMenuItem ("Set as Edit Complete",
                                            Constants.EDIT_COMPLETE_ICON_NAME,
                                            SET_EDIT_COMPLETE_ACTION_NAME,
                                            null));
    
            } 
            
            popup.add (this.createMenuItem ("Find Problems",
                                            Constants.PROBLEM_FINDER_ICON_NAME,
                                            PROBLEM_FINDER_ACTION_NAME,
                                            null));

            JMenu nm = new JMenu ("New");
            nm.setIcon (Environment.getIcon (Constants.NEW_ICON_NAME,
                                             Constants.ICON_MENU));

            popup.add (nm);

            this.addNewItemsForPopupMenu (nm,
                                          this,
                                          pos,
                                          compress);        
                                            
        }
                    
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

        this.iconColumn.addItem (c);

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

    public void editNote (final Note n)
                   throws GeneralException
    {

      final QuollEditorPanel _this = this;

      UIUtils.doLater (new ActionListener ()
      {
         
         public void actionPerformed (ActionEvent ev)
         {
            
            try
            {
               
    
               _this.scrollToNote (n);
       
               new NoteActionHandler (n,
                                     _this).actionPerformed (new ActionEvent (_this,
                                                                               0,
                                                                               "edit"));
                                           
            } catch (Exception e) {
               
               Environment.logError ("Unable to edit item: " +
                                     n,
                                     e);
               
               UIUtils.showErrorMessage (_this,
                                         "Unable to edit item, please contact Quoll Writer support for assistance.");

            }
                                       
         }
            
      });

    }

   public void showNote (final  Note n)
                         throws GeneralException
   {

      final QuollEditorPanel _this = this;

      UIUtils.doLater (new ActionListener ()
      {
                                 
         public void actionPerformed (ActionEvent ev)
         {
         
            try
            {
               
               _this.scrollToNote (n);
       
               _this.iconColumn.showItem (n);
                                           
            } catch (Exception e) {
               
               Environment.logError ("Unable to show item: " +
                                     n,
                                     e);
               
               UIUtils.showErrorMessage (_this,
                                         "Unable to show item, please contact Quoll Writer support for assistance.");

            }
                                       
         }
            
      });

    }

   public void editScene (final  Scene s)
                          throws GeneralException
   {

      final QuollEditorPanel _this = this;
   
      UIUtils.doLater (new ActionListener ()
      {
         
         public void actionPerformed (ActionEvent ev)
         {
            
            try
            {
               
               _this.scrollToItem (s);

               new ChapterItemActionHandler (s,
                                             _this,
                                             AbstractActionHandler.EDIT,
                                             s.getPosition ()).actionPerformed (new ActionEvent (_this,
                                                                                                 0,
                                                                                                 "edit"));
            } catch (Exception e) {
               
               Environment.logError ("Unable to edit item: " +
                                     s,
                                     e);
               
               UIUtils.showErrorMessage (_this,
                                         "Unable to edit item, please contact Quoll Writer support for assistance.");

            }            
            
         }
         
      });
   
    }

   public void showScene (final  Scene s)
                          throws GeneralException
    {

      final QuollEditorPanel _this = this;

      UIUtils.doLater (new ActionListener ()
      {
         
         public void actionPerformed (ActionEvent ev)
         {
            
            try
            {
               
               _this.scrollToItem (s);
       
               _this.iconColumn.showItem (s);
                                           
            } catch (Exception e) {
               
               Environment.logError ("Unable to show item: " +
                                     s,
                                     e);
               
               UIUtils.showErrorMessage (_this,
                                         "Unable to show item, please contact Quoll Writer support for assistance.");

            }
                                       
         }
            
      });
    
    }

    public void editOutlineItem (final  OutlineItem n)
                                 throws GeneralException
    {

      final QuollEditorPanel _this = this;

      UIUtils.doLater (new ActionListener ()
      {
         
         public void actionPerformed (ActionEvent ev)
         {
            
            try
            {

               _this.scrollToOutlineItem (n);
       
               new ChapterItemActionHandler (n,
                                             _this,
                                             AbstractActionHandler.EDIT,
                                             n.getPosition ()).actionPerformed (new ActionEvent (this,
                                                                                                 0,
                                                                                                 "edit"));
            } catch (Exception e) {
               
               Environment.logError ("Unable to show item: " +
                                     n,
                                     e);
               
               UIUtils.showErrorMessage (_this,
                                         "Unable to show item, please contact Quoll Writer support for assistance.");

            }

         }
         
      });
            
    }

   public void showOutlineItem (final  OutlineItem n)
   {

      final QuollEditorPanel _this = this;

      UIUtils.doLater (new ActionListener ()
      {
                                 
         public void actionPerformed (ActionEvent ev)
         {
         
            try
            {
               
               _this.scrollToOutlineItem (n);
       
               _this.iconColumn.showItem (n);
                                           
            } catch (Exception e) {
               
               Environment.logError ("Unable to show item: " +
                                     n,
                                     e);
               
               UIUtils.showErrorMessage (_this,
                                         "Unable to show item, please contact Quoll Writer support for assistance.");

            }
                                       
         }
            
      });

    }

    public void scrollToNote (Note n)
                       throws GeneralException
    {

        this.scrollToPosition (n.getPosition ());

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

    public void reinitIconColumn ()
                                  throws GeneralException
    {

        try
        {
    
            this.iconColumn.init ();
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to init icon column",
                                        e);
            
        }
        
    }
    

   @Override    
   public void init ()
              throws GeneralException
   {

         super.init ();
   
        final QuollEditorPanel _this = this;

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

         ActionAdapter finishAction = new ActionAdapter ()
         {
             
             public void actionPerformed (ActionEvent ev)
             {

                 _this.problemFinderPanel.setVisible (false);

                 _this.problemFinder.reset ();
                                               
                 _this.editor.setHighlightWritingLine (_this.projectViewer.isHighlightWritingLine ());
                                                    
                  _this.editor.grabFocus ();
                                                                 
             }
             
         };
                                        
        hbuts.add (UIUtils.createButton ("cancel",
                                        Constants.ICON_MENU,
                                        "Click to stop looking for problems.",
                                        finishAction));

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

        JButton cbut = null;
        /*
        UIUtils.createButton ("config",
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
*/
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

                    Environment.logError ("Unable to goto previous.",
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to go to previous.");

                }

            }

        });

        prev.setToolTipText ("Go back to the problem(s)");

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

        next.setToolTipText ("Find the next problem(s)");
        next.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.add (next);

        buts.add (Box.createHorizontalStrut (5));

        JButton finish = UIUtils.createButton ("Finish",
                                               null);

        finish.addActionListener (finishAction);

        finish.setToolTipText ("Stop looking for problems");
        finish.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.add (finish);
                
        buts.add (Box.createHorizontalGlue ());

        this.ignoredProblemsLabel = UIUtils.createClickableLabel ("",
                                                                  Environment.getIcon ("warning",
                                                                                       Constants.ICON_MENU)); 
        this.ignoredProblemsLabel.setVisible (false);

        this.updateIgnoredProblemsLabel ();
        
        this.ignoredProblemsLabel.addMouseListener (new MouseEventHandler ()
        {
        
            @Override
            public void handlePress (MouseEvent ev)
            {
                
               int s = _this.problemFinder.getIgnoredIssues ().size ();

               String pl = (s > 1 ? "s" :"");
  
               UIUtils.createQuestionPopup (_this.projectViewer,
                                            "Un-ignore " + s + " problem" + pl,
                                            Constants.PROBLEM_FINDER_ICON_NAME,
                                            "Please confirm you wish to un-ignore the " +
                                            s +
                                            " problem" + pl + "?",
                                            "Yes, un-ignore " + (s == 1 ? "it" : "them"),
                                            null,
                                            new ActionListener ()
                                            {
                                               
                                               public void actionPerformed (ActionEvent ev)
                                               {

                                                  _this.problemFinder.removeAllIgnores ();
                                                  
                                                  _this.updateIgnoredProblemsLabel ();

                                               }
                                               
                                            },
                                            null,
                                            null,
                                            null);
                                
            }
            
        });
        
        buts.add (this.ignoredProblemsLabel);

        buts.setBorder (new EmptyBorder (10,
                                         10,
                                         5,
                                         10));

        this.problemFinderPanel.add (buts);

        this.add (this.problemFinderPanel);

        this.reinitIconColumn ();

        this.setReadyForUse (true);        
        
    }

   public void showProblemFinder ()
   {

      if (!this.projectViewer.isLanguageFunctionAvailable ())
      {

         return;
      
      }
        
      // Disable typewriter scrolling when the problem finder is active.
      this.setUseTypewriterScrolling (false);
      this.editor.setHighlightWritingLine (false);

      this.problemFinderPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                              this.problemFinderPanel.getPreferredSize ().height));
                                                              
      this.problemFinderPanel.setVisible (true);

      this.projectViewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                           ProjectEvent.SHOW);
      
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

      /*
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
        */
    }
    
   public int getIconColumnXOffset (ChapterItem i)
   {
     
      int xOffset = 36;

      if (i instanceof OutlineItem)
      {

          xOffset = 22;

      }
      
      return xOffset;
     
   }
    
   public void removeItemHighlightTextFromEditor (ChapterItem it)
   {
      
      this.editor.removeAllHighlights (this.highlight);      
    
   }
   
   public void highlightItemTextInEditor (ChapterItem it)
   {
      
      this.editor.removeAllHighlights (this.highlight);
      this.editor.addHighlight (it.getStartPosition (),
                                it.getEndPosition (),
                                this.highlight,
                                false);                    
      
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

   @Override
   public void close ()
   {

       super.close ();

       this.problemFinder.saveIgnores ();

   }
/*
    public ProblemFinderRuleConfig getProblemFinderRuleConfig ()
    {

        return this.problemFinderRuleConfig;

    }
*/
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

        if ((c.equals (Color.white))
            ||
            (c.equals (UIUtils.getComponentColor ()))
           )
        {

            this.iconColumn.setBackground (IconColumn.defaultBGColor);

        } else
        {

            this.iconColumn.setBackground (c);

        }

    }

   private static class CutNPasteTransferHandler extends TransferHandler 
   {
        public void exportToClipboard(JComponent comp, Clipboard clipboard,
                                      int action)
        throws IllegalStateException
        {
         
            if (comp instanceof JTextComponent)
            {
               
                JTextComponent text = (JTextComponent)comp;
                int p0 = text.getSelectionStart();
                int p1 = text.getSelectionEnd();
                if (p0 != p1) {
                    try {
                        Document doc = text.getDocument();
                        String srcData = doc.getText(p0, p1 - p0);
         
                        StringSelection contents =new StringSelection(srcData);

                        // this may throw an IllegalStateException,
                        // but it will be caught and handled in the
                        // action that invoked this method
                        clipboard.setContents(contents, null);

                        if (action == TransferHandler.MOVE) {
                            doc.remove(p0, p1 - p0);
                        }
                    } catch (BadLocationException ble) {}
                }
            }
        }
        
        public boolean importData(JComponent comp, Transferable t)
        {
        
            if (comp instanceof JTextComponent) {
                DataFlavor flavor = getFlavor(t.getTransferDataFlavors());

                if (flavor != null) {
                    InputContext ic = comp.getInputContext();
                    if (ic != null) {
                        ic.endComposition();
                    }
                    try {
                        String data = (String)t.getTransferData(flavor);

                        ((JTextComponent)comp).replaceSelection(data);
                        return true;
                    } catch (UnsupportedFlavorException ufe) {
                    } catch (IOException ioe) {
                    }
                }
            }
            return false;
        }
        
        public boolean canImport(JComponent comp,
                                 DataFlavor[] transferFlavors)
        {
         
            JTextComponent c = (JTextComponent)comp;
            if (!(c.isEditable() && c.isEnabled())) {
                return false;
            }
            return (getFlavor(transferFlavors) != null);
        }
        
        public int getSourceActions(JComponent c)
        {
        
            return NONE;
        
        }
        
        private DataFlavor getFlavor(DataFlavor[] flavors)
        {
            if (flavors != null) {
                for (int counter = 0; counter < flavors.length; counter++) {
                    if (flavors[counter].equals(DataFlavor.stringFlavor)) {
                        return flavors[counter];
                    }
                }
            }
            return null;
        }
    }    

    
}
