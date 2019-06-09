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

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;

public class QuollEditorPanel extends AbstractEditableEditorPanel implements ChapterItemViewer, ProjectEventListener
{

    public static final String SHOW_WORD_CLOUD_ACTION_NAME = "show-word-cloud";
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

    private IconColumn<ProjectViewer> iconColumn = null;
    protected ProjectViewer         projectViewer = null;
    private Box                     problemFinderPanel = null;
    private ProblemFinder           problemFinder = null;
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

        this.viewer.addProjectEventListener (this);

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

        this.highlight = new BlockPainter (UIUtils.getHighlightColor ());

        this.iconColumn = new IconColumn<ProjectViewer> (this,
                                                         c,
                                                         this.projectViewer.getIconProvider (),
                                                         this.projectViewer.getChapterItemViewPopupProvider ());

        //this.iconColumn.addMouseListener (this);

        this.iconColumn.addMouseListener (new MouseEventHandler ()
        {

            public void handleDoublePress (MouseEvent ev)
            {

                java.util.List<String> prefix = new ArrayList<> ();
                prefix.add (LanguageStrings.iconcolumn);
                prefix.add (LanguageStrings.doubleclickmenu);
                prefix.add (LanguageStrings.items);

                JPopupMenu popup = new JPopupMenu ();

                // Convert the mouse position to a point in the text.

                String pref = getUIString (LanguageStrings.general,LanguageStrings.shortcutprefix);

                JMenuItem mi = null;

                mi = UIUtils.createMenuItem (getUIString (prefix,
                                                                      Scene.OBJECT_TYPE),
                                             Scene.OBJECT_TYPE,
                                             _this.getActionListenerForTextPosition (NEW_SCENE_ACTION_NAME,
                                                                                     ev.getPoint ()),
                                             null,
                                             null);

                popup.add (mi);

                // TODO: Abstract this.
                mi.setMnemonic (Character.toUpperCase (Environment.getObjectTypeName (Scene.OBJECT_TYPE).getValue ().charAt (0)));
                mi.setToolTipText (pref + "S");

                mi = UIUtils.createMenuItem (getUIString (prefix,
                                                                      OutlineItem.OBJECT_TYPE),
                                             OutlineItem.OBJECT_TYPE,
                                             _this.getActionListenerForTextPosition (NEW_OUTLINE_ITEM_ACTION_NAME,
                                                                                     ev.getPoint ()),
                                             null,
                                             null);

                popup.add (mi);

                mi.setMnemonic (Character.toUpperCase (Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE).getValue ().charAt (0)));
                mi.setToolTipText (pref + "O");

                mi = UIUtils.createMenuItem (getUIString (prefix,
                                                                      Note.OBJECT_TYPE),
                                             Note.OBJECT_TYPE,
                                             _this.getActionListenerForTextPosition (NEW_NOTE_ACTION_NAME,
                                                                                     ev.getPoint ()),
                                             null,
                                             null);

                popup.add (mi);

                mi.setMnemonic (Character.toUpperCase (Environment.getObjectTypeName (Note.OBJECT_TYPE).getValue ().charAt (0)));
                mi.setToolTipText (pref + "N");

                mi = UIUtils.createMenuItem (getUIString (prefix,
                                                                      Note.EDIT_NEEDED_OBJECT_TYPE),
                                             Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                             _this.getActionListenerForTextPosition (NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                                                                     ev.getPoint ()),
                                             null,
                                             null);

                popup.add (mi);

                mi.setMnemonic ('E');
                mi.setToolTipText (pref + "E");

                popup.show ((Component) ev.getSource (),
                            ev.getPoint ().x,
                            ev.getPoint ().y);

            }

        });

        final java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.actions);

        this.chItemTransferHandler = new ChapterItemTransferHandler (this.getIconColumn ());

        this.setTransferHandler (this.chItemTransferHandler);

        this.actions.put (SPLIT_CHAPTER_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              @Override
                              public void actionPerformed (ActionEvent ev)
                              {

                                 new SplitChapterActionHandler (_this.obj,
                                                                _this.projectViewer).actionPerformed (ev);

                              }

                          });

        this.actions.put (REMOVE_EDIT_POINT_ACTION_NAME,
                          new ActionAdapter ()
                          {

                             public void actionPerformed (ActionEvent ev)
                             {

                                 _this.projectViewer.removeChapterEditPosition (_this.obj);

                             }

                          });

        this.actions.put (SET_EDIT_COMPLETE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                             public void actionPerformed (ActionEvent ev)
                             {

                                _this.projectViewer.setChapterEditComplete (_this.obj,
                                                                            true);

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

                                      _this.projectViewer.viewChapterInformation (_this.obj);

                                  } catch (Exception e)
                                  {

                                      Environment.logError ("Unable to show chapter information for: " +
                                                            _this.obj,
                                                            e);

                                      UIUtils.showErrorMessage (_this,
                                                                getUIString (LanguageStrings.project,
                                                                                         LanguageStrings.actions,
                                                                                         LanguageStrings.showchapterinfo,
                                                                                         LanguageStrings.actionerror));
                                                                //Environment.replaceObjectNames ("Unable to show {chapter}."));

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

        im.put (KeyStroke.getKeyStroke ("ctrl shift S"),
                NEW_SCENE_ACTION_NAME);

        im.put (KeyStroke.getKeyStroke ("ctrl shift O"),
                NEW_OUTLINE_ITEM_ACTION_NAME);
        im.put (KeyStroke.getKeyStroke ("ctrl shift N"),
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
        p.setOpaque (false);

        p.add (this.iconColumn,
               BorderLayout.WEST);
        p.add (q,
               BorderLayout.CENTER);

        return p;

    }

    public void showProblemFinderRuleConfig ()
    {

         this.projectViewer.showProblemFinderRuleConfig ();

    }

    @Override
    public void fillToolBar (JToolBar acts,
                             final boolean  fullScreen)
    {

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.toolbar);

        final QuollEditorPanel _this = this;

        acts.add (this.createToolbarButton (Constants.SAVE_ICON_NAME,
                                            getUIString (prefix,
                                                                     LanguageStrings.save,
                                                                     LanguageStrings.tooltip),
                                            //"Click to save the {Chapter} text",
                                            SAVE_ACTION_NAME));

        this.doFillToolBar (acts);

        acts.add (this.createToolbarButton (Constants.WORDCOUNT_ICON_NAME,
                                            getUIString (prefix,
                                                                     LanguageStrings.wordcount,
                                                                     LanguageStrings.tooltip),
                                            //"Click to view the word counts and readability indices",
                                            TOGGLE_WORDCOUNTS_ACTION_NAME));

        if (this.projectViewer.isSpellCheckingEnabled ())
        {

            acts.add (this.createToolbarButton ("spellchecker-turn-off",
                                                getUIString (prefix,
                                                                         LanguageStrings.spellcheckoff,
                                                                         LanguageStrings.tooltip),
                                                //"Click to turn the spell checker " + type,
                                                TOGGLE_SPELLCHECK_ACTION_NAME));

        } else {

            acts.add (this.createToolbarButton ("spellchecker-turn-on",
                                                getUIString (prefix,
                                                                         LanguageStrings.spellcheckon,
                                                                         LanguageStrings.tooltip),
                                                //"Click to turn the spell checker " + type,
                                                TOGGLE_SPELLCHECK_ACTION_NAME));

        }

        acts.add (this.createToolbarButton (Constants.DELETE_ICON_NAME,
                                            getUIString (prefix,
                                                                     LanguageStrings.delete,
                                                                     LanguageStrings.tooltip),
                                            //"Click to delete this {Chapter}",
                                            DELETE_CHAPTER_ACTION_NAME));

        // Add a tools menu.
        final JButton b = UIUtils.createToolBarButton ("tools",
                                                       getUIString (prefix,
                                                                                LanguageStrings.tools,
                                                                                LanguageStrings.tooltip),
                                                       //"Click to view the tools such as Print, Find Problems and Edit the text properties",
                                                       "tools",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                java.util.List<String> prefix = new ArrayList<> ();
                prefix.add (LanguageStrings.project);
                prefix.add (LanguageStrings.editorpanel);
                prefix.add (LanguageStrings.tools);

                JPopupMenu m = new JPopupMenu ();
/*
                _this.doFillToolsPopupMenu (ev,
                                            m);
*/
                JMenuItem mi = null;

                // Need a more elegant way of handling this, maybe via cue.language?
                if (_this.viewer.isLanguageEnglish ())
                {

                   m.add (_this.createMenuItem (getUIString (prefix,
                                                                         LanguageStrings.problemfinder,
                                                                         LanguageStrings.text),
                                                                        //"Find Problems",
                                                Constants.PROBLEM_FINDER_ICON_NAME,
                                                PROBLEM_FINDER_ACTION_NAME,
                                                KeyStroke.getKeyStroke (KeyEvent.VK_P,
                                                                        ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK)));

                }

                m.add (_this.createMenuItem (getUIString (prefix,
                                                                      LanguageStrings.textproperties,
                                                                      LanguageStrings.text),
                                                                      //"Edit Text Properties",
                                             Constants.EDIT_PROPERTIES_ICON_NAME,
                                             EDIT_TEXT_PROPERTIES_ACTION_NAME,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                                                     ActionEvent.CTRL_MASK)));

                m.add (_this.createMenuItem (getUIString (prefix,
                                                                      LanguageStrings.find,
                                                                      LanguageStrings.text),
                                                                      //"Find",
                                             Constants.FIND_ICON_NAME,
                                             Constants.SHOW_FIND_ACTION,
                                             KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                                     ActionEvent.CTRL_MASK)));

                m.add (_this.createMenuItem (getUIString (prefix,
                                                                      LanguageStrings.print,
                                                                      LanguageStrings.text),
                                                                      //"Print {Chapter}",
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

    private void doFillToolBar (JToolBar acts)
    {

        final QuollEditorPanel _this = this;
/*
        ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.performAction (ev);

            }

        };
*/
        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.toolbar);

        final JButton b = UIUtils.createToolBarButton (Constants.NEW_ICON_NAME,
                                                       getUIString (prefix,
                                                                                LanguageStrings._new,
                                                                                LanguageStrings.tooltip),
                                                       //"Click to add a new {Outlineitem}, {Character}, {Note}, {Object} etc.",
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
                                            getUIString (prefix,
                                                                     LanguageStrings.showchapterinfo,
                                                                     LanguageStrings.tooltip),
                                            //"Click to view/edit the {chapter} information",
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
                                                     this.obj);

            if (a != null)
            {

               a.actionPerformed (ev);

            }

            return;

        }

        if (c.equals (NEW_SCENE_ACTION_NAME))
        {

            Scene s = new Scene (-1,
                                 this.obj);

            new ChapterItemActionHandler<Scene> (s,
                                                 this,
                                                 AbstractFormPopup.ADD,
                                                 pos).actionPerformed (ev);

            return;

        }

        if (c.equals (NEW_OUTLINE_ITEM_ACTION_NAME))
        {

            OutlineItem o = new OutlineItem (-1,
                                             this.obj);

            new ChapterItemActionHandler<OutlineItem> (o,
                                                       this,
                                                       AbstractFormPopup.ADD,
                                                       pos).actionPerformed (ev);

            return;

        }

        if (c.equals (NEW_NOTE_ACTION_NAME))
        {

            new NoteActionHandler (this.obj,
                                   this,
                                   pos).actionPerformed (ev);

            return;

        }

        if (c.equals (NEW_EDIT_NEEDED_NOTE_ACTION_NAME))
        {

            Note n = new Note (0, this.obj);
            n.setType (Note.EDIT_NEEDED_NOTE_TYPE);

            new NoteActionHandler (this.obj,
                                   this,
                                   n,
                                   pos).actionPerformed (ev);

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

        java.util.List<String> prefix = new ArrayList<> ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.popupmenu);
        prefix.add (LanguageStrings._new);
        prefix.add (LanguageStrings.items);

        if (compress)
        {

            List<JComponent> buts = new ArrayList<> ();

            buts.add (this.createButton (Scene.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  Scene.OBJECT_TYPE,
                                                                  LanguageStrings.tooltip),
                                         //String.format ("Add a new {%s}", Scene.OBJECT_TYPE),
                                         NEW_SCENE_ACTION_NAME,
                                         aa));

            buts.add (this.createButton (OutlineItem.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  OutlineItem.OBJECT_TYPE,
                                                                  LanguageStrings.tooltip),
                                         //String.format ("Add a new {%s}", OutlineItem.OBJECT_TYPE),
                                         NEW_OUTLINE_ITEM_ACTION_NAME,
                                         aa));

            buts.add (this.createButton (Note.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  Note.OBJECT_TYPE,
                                                                  LanguageStrings.tooltip),
                                         //String.format ("Add a new {%s}", Note.OBJECT_TYPE),
                                         NEW_NOTE_ACTION_NAME,
                                         aa));

            buts.add (this.createButton ("edit-needed-note",
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  Note.EDIT_NEEDED_OBJECT_TYPE,
                                                                  LanguageStrings.tooltip),
                                         //String.format ("Add a new Edit Needed Note"),
                                         NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                         aa));

            buts.add (this.createButton (Chapter.OBJECT_TYPE,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  Chapter.OBJECT_TYPE,
                                                                  LanguageStrings.tooltip),
                                         //String.format ("Add a new {%s}", Chapter.OBJECT_TYPE),
                                         NEW_CHAPTER_ACTION_NAME,
                                         aa));

            if (popup instanceof JPopupMenu)
            {

               JPopupMenu pm = (JPopupMenu) popup;

               pm.addSeparator ();

               popup.add (UIUtils.createPopupMenuButtonBar (getUIString (LanguageStrings.project,
                                                                                     LanguageStrings.editorpanel,
                                                                                     LanguageStrings.popupmenu,
                                                                                     LanguageStrings._new,
                                                                                     LanguageStrings.text),
                                                                                     //"New",
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

            String pref = getUIString (LanguageStrings.general,LanguageStrings.shortcutprefix);

            //"Shortcut: Ctrl+Shift+";

            JMenuItem mi = null;

            mi = this.createMenuItem (getUIString (prefix,
                                                               Scene.OBJECT_TYPE,
                                                               LanguageStrings.text),
//Environment.getObjectTypeName (Scene.OBJECT_TYPE),
                                      Scene.OBJECT_TYPE,
                                      NEW_SCENE_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('S');
            mi.setToolTipText (pref + "S");

            mi = this.createMenuItem (getUIString (prefix,
                                                               OutlineItem.OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE),
                                      OutlineItem.OBJECT_TYPE,
                                      NEW_OUTLINE_ITEM_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('O');
            mi.setToolTipText (pref + "O");

            mi = this.createMenuItem (getUIString (prefix,
                                                               Note.OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                      Note.OBJECT_TYPE,
                                      NEW_NOTE_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('N');
            mi.setToolTipText (pref + "N");

            mi = this.createMenuItem (getUIString (prefix,
                                                               Note.EDIT_NEEDED_OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Note.EDIT_NEEDED_NOTE_TYPE + " " + Environment.getObjectTypeName (Note.OBJECT_TYPE),
                                      Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                      NEW_EDIT_NEEDED_NOTE_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi = this.createMenuItem (getUIString (prefix,
                                                               Chapter.OBJECT_TYPE,
                                                               LanguageStrings.text),
                                                               //Environment.getObjectTypeName (Chapter.OBJECT_TYPE),
                                      Chapter.OBJECT_TYPE,
                                      NEW_CHAPTER_ACTION_NAME,
                                      null,
                                      aa);

            popup.add (mi);

            mi.setMnemonic ('E');
            mi.setToolTipText (pref + "E");

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

    public void setEditPosition (Point mouseP)
    {

        try
        {

            this.projectViewer.setChapterEditPosition (this.obj,
                                                       this.editor.viewToModel (mouseP));

        } catch (Exception e) {

           Environment.logError ("Unable to set edit position for chapter: " +
                                 this.obj,
                                 e);

           UIUtils.showErrorMessage (this.projectViewer,
                                     getUIString (LanguageStrings.project,
                                                              LanguageStrings.editorpanel,
                                                              LanguageStrings.actions,
                                                              LanguageStrings.seteditposition,
                                                              LanguageStrings.actionerror));
                                     //"Unable to set edit position.");

        }

    }

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

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.editorpanel);
        prefix.add (LanguageStrings.popupmenu);
        prefix.add (Chapter.OBJECT_TYPE);
        prefix.add (LanguageStrings.items);

        JMenuItem mi = null;

        if (compress)
        {

            List<JComponent> buts = new ArrayList ();

            buts.add (this.createButton (Constants.SAVE_ICON_NAME,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  LanguageStrings.save,
                                                                  LanguageStrings.tooltip),
                                         //"Save {Chapter}",
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
                                               getUIString (prefix,
                                                                        LanguageStrings.splitchapter,
                                                                        LanguageStrings.tooltip),
                                               //"Split {Chapter}",
                                               SPLIT_CHAPTER_ACTION_NAME));

               }

            }

            buts.add (this.createButton (Constants.PROBLEM_FINDER_ICON_NAME,
                                         Constants.ICON_MENU,
                                         //"Find Problems",
                                         getUIString (prefix,
                                                                  LanguageStrings.problemfinder,
                                                                  LanguageStrings.tooltip),
                                         PROBLEM_FINDER_ACTION_NAME));

            buts.add (this.createButton (Constants.EDIT_PROPERTIES_ICON_NAME,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  LanguageStrings.textproperties,
                                                                  LanguageStrings.tooltip),
                                         //"Edit Text Properties",
                                         EDIT_TEXT_PROPERTIES_ACTION_NAME));

            buts.add (this.createButton (Constants.FIND_ICON_NAME,
                                         Constants.ICON_MENU,
                                         getUIString (prefix,
                                                                  LanguageStrings.find,
                                                                  LanguageStrings.tooltip),
                                         //"Find",
                                         Constants.SHOW_FIND_ACTION));

            popup.add (UIUtils.createPopupMenuButtonBar (getUIString (LanguageStrings.project,
                                                                                  LanguageStrings.editorpanel,
                                                                                  LanguageStrings.popupmenu,
                                                                                  Chapter.OBJECT_TYPE,
                                                                                  LanguageStrings.compresstext),
            //Environment.replaceObjectNames ("{Chapter}"),
                                                         popup,
                                                         buts));

            buts = new ArrayList ();

            buts.add (UIUtils.createButton (Constants.EDIT_IN_PROGRESS_ICON_NAME,
                                            Constants.ICON_MENU,
                                            getUIString (prefix,
                                                                     LanguageStrings.seteditposition,
                                                                     LanguageStrings.tooltip),
                                            //"Set Edit Point",
                                            new ActionAdapter ()
                                            {

                                               public void actionPerformed (ActionEvent ev)
                                               {

                                                   _this.setEditPosition (mouseP);

                                               }

                                            }));

            if (this.obj.getEditPosition () > 0)
            {

                buts.add (this.createButton (Constants.REMOVE_EDIT_POINT_ICON_NAME,
                                             Constants.ICON_MENU,
                                             getUIString (prefix,
                                                                      LanguageStrings.removeeditposition,
                                                                      LanguageStrings.tooltip),
                                             //"Remove Edit Point",
                                             REMOVE_EDIT_POINT_ACTION_NAME));

            }

            if (!this.obj.isEditComplete ())
            {

                buts.add (this.createButton (Constants.EDIT_COMPLETE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             getUIString (prefix,
                                                                      LanguageStrings.seteditcomplete,
                                                                      LanguageStrings.tooltip),
                                             //"Set as Edit Complete",
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

            mi = this.createMenuItem (getUIString (prefix,
                                                               LanguageStrings.save,
                                                               LanguageStrings.text),
                                                                //"Save {Chapter}",
                                      Constants.SAVE_ICON_NAME,
                                      SAVE_ACTION_NAME,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_S);

            popup.add (mi);

            JMenu m = new JMenu (getUIString (LanguageStrings.project,
                                                          LanguageStrings.editorpanel,
                                                          LanguageStrings.popupmenu,
                                                          Chapter.OBJECT_TYPE,
                                                          LanguageStrings.text));
            //Environment.replaceObjectNames ("{Chapter} Edit"));
            m.setIcon (Environment.getIcon (Constants.EDIT_ICON_NAME,
                                            Constants.ICON_MENU));

            popup.add (m);

            if ((this.editor.getCaret ().getDot () > 0)
                ||
                (this.editor.getSelectionStart () > 0)
               )
            {

               if (this.editor.getCaret ().getDot () < this.editor.getTextWithMarkup ().getText ().length ())
               {

                  m.add (this.createMenuItem (getUIString (prefix,
                                                                       LanguageStrings.splitchapter,
                                                                       LanguageStrings.text),
                                                                     //"Split {Chapter}",
                                              Constants.CHAPTER_SPLIT_ICON_NAME,
                                              SPLIT_CHAPTER_ACTION_NAME,
                                              null));

               }

            }

            mi = UIUtils.createMenuItem (getUIString (prefix,
                                                                  LanguageStrings.seteditposition,
                                                                  LanguageStrings.text),
                                                               //"Set Edit Point",
                                         Constants.EDIT_IN_PROGRESS_ICON_NAME,
                                         new ActionAdapter ()
                                         {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.setEditPosition (mouseP);

                                            }

                                         });

            m.add (mi);

            if (this.obj.getEditPosition () > 0)
            {

                m.add (this.createMenuItem (getUIString (prefix,
                                                                   LanguageStrings.removeeditposition,
                                                                   LanguageStrings.text),
                                                                   //"Remove Edit Point",
                                            Constants.REMOVE_EDIT_POINT_ICON_NAME,
                                            REMOVE_EDIT_POINT_ACTION_NAME,
                                            null));

            }

            if (!this.obj.isEditComplete ())
            {

                m.add (this.createMenuItem (getUIString (prefix,
                                                                     LanguageStrings.seteditcomplete,
                                                                     LanguageStrings.text),
                                                                   //"Set as Edit Complete",
                                            Constants.EDIT_COMPLETE_ICON_NAME,
                                            SET_EDIT_COMPLETE_ACTION_NAME,
                                            null));

            }

            popup.add (this.createMenuItem (getUIString (prefix,
                                                                     LanguageStrings.problemfinder,
                                                                     LanguageStrings.text),
                                                               //"Find Problems",
                                            Constants.PROBLEM_FINDER_ICON_NAME,
                                            PROBLEM_FINDER_ACTION_NAME,
                                            null));

            JMenu nm = new JMenu (getUIString (LanguageStrings.project,
                                                          LanguageStrings.editorpanel,
                                                          LanguageStrings.popupmenu,
                                                          LanguageStrings._new,
                                                          LanguageStrings.text));
                                                          //"New");
            nm.setIcon (Environment.getIcon (Constants.NEW_ICON_NAME,
                                             Constants.ICON_MENU));

            popup.add (nm);

            this.addNewItemsForPopupMenu (nm,
                                          this,
                                          pos,
                                          compress);

            popup.add (this.createMenuItem (getUIString (prefix,
                                                                     LanguageStrings.textproperties,
                                                                     LanguageStrings.text),
                                                                     //"Edit Text Properties",
                                            Constants.EDIT_PROPERTIES_ICON_NAME,
                                            EDIT_TEXT_PROPERTIES_ACTION_NAME,
                                            null));

            mi = this.createMenuItem (getUIString (prefix,
                                                               LanguageStrings.find,
                                                               LanguageStrings.text),
                                                                     //"Find",
                                      Constants.FIND_ICON_NAME,
                                      Constants.SHOW_FIND_ACTION,
                                      KeyStroke.getKeyStroke (KeyEvent.VK_F,
                                                              ActionEvent.CTRL_MASK));
            mi.setMnemonic (KeyEvent.VK_F);

            popup.add (mi);

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

    public void scrollToItem (ChapterItem i)
                       throws GeneralException
    {

        this.scrollToPosition (i.getPosition ());

    }

    public void editNote (final Note n)
    {

      final QuollEditorPanel _this = this;

      UIUtils.doLater (new ActionListener ()
      {

         public void actionPerformed (ActionEvent ev)
         {

            try
            {


               _this.scrollToItem (n);

               new NoteActionHandler (n,
                                     _this).actionPerformed (new ActionEvent (_this,
                                                                               0,
                                                                               "edit"));

            } catch (Exception e) {

               Environment.logError ("Unable to edit item: " +
                                     n,
                                     e);

               UIUtils.showErrorMessage (_this,
                                         String.format (getUIString (LanguageStrings.edititem,
                                                                                 LanguageStrings.actionerror),
                                                        Environment.getObjectTypeName (n)));

            }

         }

      });

    }

   public void editScene (final  Scene s)
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
                                             AbstractFormPopup.EDIT,
                                             s.getPosition ()).actionPerformed (new ActionEvent (_this,
                                                                                                 0,
                                                                                                 "edit"));
            } catch (Exception e) {

               Environment.logError ("Unable to edit item: " +
                                     s,
                                     e);

               UIUtils.showErrorMessage (_this,
                                         String.format (getUIString (LanguageStrings.edititem,
                                                                                 LanguageStrings.actionerror),
                                                        Environment.getObjectTypeName (s)));

            }

         }

      });

    }

    public void editOutlineItem (final  OutlineItem n)
    {

      final QuollEditorPanel _this = this;

      UIUtils.doLater (new ActionListener ()
      {

         public void actionPerformed (ActionEvent ev)
         {

            try
            {

               _this.scrollToItem (n);

               new ChapterItemActionHandler (n,
                                             _this,
                                             AbstractFormPopup.EDIT,
                                             n.getPosition ()).actionPerformed (new ActionEvent (this,
                                                                                                 0,
                                                                                                 "edit"));
            } catch (Exception e) {

               Environment.logError ("Unable to show item: " +
                                     n,
                                     e);

               UIUtils.showErrorMessage (_this,
                                         String.format (getUIString (LanguageStrings.edititem,
                                                                                 LanguageStrings.actionerror),
                                                        Environment.getObjectTypeName (n)));
/*
               UIUtils.showErrorMessage (_this,
                                         "Unable to show item, please contact Quoll Writer support for assistance.");
*/
            }

         }

      });

    }

    public void showItem (final  ChapterItem n)
    {

       final QuollEditorPanel _this = this;

       UIUtils.doLater (new ActionListener ()
       {

          public void actionPerformed (ActionEvent ev)
          {

             try
             {

                _this.scrollToItem (n);

                _this.iconColumn.showItem (n);

             } catch (Exception e) {

                Environment.logError ("Unable to show item: " +
                                      n,
                                      e);

                UIUtils.showErrorMessage (_this,
                                          String.format (getUIString (LanguageStrings.viewitem,
                                                                                  LanguageStrings.actionerror),
                                                         Environment.getObjectTypeName (n)));
                                          //"Unable to show item, please contact Quoll Writer support for assistance.");

             }

          }

       });

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

        String tooltip = getUIString (LanguageStrings.dictionary,
                                                  LanguageStrings.spellcheck,
                                                  LanguageStrings.buttons,
                                                  (v ? "off" : "on"),
                                                  LanguageStrings.tooltip);

        this.setToolBarButtonIcon ("toggle-spellcheck",
                                   tooltip,
                                   //"Click turn the spell checker " + type,
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

         final java.util.List<String> prefix = new ArrayList<> ();
         prefix.add (LanguageStrings.project);
         prefix.add (LanguageStrings.editorpanel);
         prefix.add (LanguageStrings.actions);
         prefix.add (LanguageStrings.problemfinder);

        final QuollEditorPanel _this = this;

        this.problemFinderPanel = new Box (BoxLayout.Y_AXIS);
        this.problemFinderPanel.setOpaque (true);
        this.problemFinderPanel.setBackground (UIUtils.getComponentColor ());
        this.problemFinderPanel.setBorder (new MatteBorder (1,
                                                            0,
                                                            0,
                                                            0,
                                                            UIUtils.getBorderColor ()));

        java.util.List<JButton> hbuts = new ArrayList ();

        hbuts.add (UIUtils.createButton ("config",
                                        Constants.ICON_MENU,
                                        getUIString (prefix,
                                                                 LanguageStrings.headercontrols,
                                                                 LanguageStrings.items,
                                                                 LanguageStrings.config,
                                                                 LanguageStrings.tooltip),
                                        //"Click to configure the text rules.",
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

        hbuts.add (UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                        Constants.ICON_MENU,
                                        getUIString (prefix,
                                                                 LanguageStrings.headercontrols,
                                                                 LanguageStrings.items,
                                                                 LanguageStrings.cancel,
                                                                 LanguageStrings.tooltip),
                                        //"Click to stop looking for problems.",
                                        finishAction));

        hbuts.add (UIUtils.createHelpPageButton ("chapters/problem-finder",
                                                Constants.ICON_MENU,
                                                null));

        JToolBar tb = UIUtils.createButtonBar (hbuts);

        tb.setBorder (UIUtils.createPadding (0, 0, 0, 5));

        Header h = UIUtils.createHeader (getUIString (prefix,
                                                                  LanguageStrings.title),
                                                                  //"Find Problems",
                                         Constants.SUB_PANEL_TITLE,
                                         Constants.PROBLEM_FINDER_ICON_NAME,
                                         tb);

        this.problemFinderPanel.add (h);
        this.problemFinderPanel.setVisible (false);
        this.problemFinderPanel.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.problemFinder = new ProblemFinder (this,
                                                // This is a duplicate reference that overlaps with parent viewer reference.
                                                // TODO: Resolve the duplication, make generic and fix this class with ProjectViewer.
                                                this.projectViewer);
        this.problemFinder.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.problemFinderPanel.add (this.problemFinder);

        Box buts = new Box (BoxLayout.X_AXIS);

        buts.setAlignmentX (Component.LEFT_ALIGNMENT);

        JButton cbut = null;

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
                                              getUIString (prefix,
                                                                       LanguageStrings.previouserror));
                                              //"Unable to go to previous.");

                }

            }

        });

        prev.setToolTipText (getUIString (prefix,
                                                      LanguageStrings.buttons,
                                                      LanguageStrings.previous,
                                                      LanguageStrings.tooltip));
                                 //"Go back to the problem(s)");

        buts.add (prev);
        buts.add (Box.createHorizontalStrut (5));

        JButton next = UIUtils.createButton (getUIString (prefix,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.next,
                                                                      LanguageStrings.text),
                                                                      //"Next",
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

                        Environment.logError ("Unable to goto next problem",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  getUIString (prefix,
                                                                           LanguageStrings.nexterror));
                                                  //"Unable to go to next sentence.");

                    }

                }

            });

        next.setToolTipText (getUIString (prefix,
                                                      LanguageStrings.buttons,
                                                      LanguageStrings.next,
                                                      LanguageStrings.tooltip));
                                                                      //"Find the next problem(s)");
        next.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.add (next);

        buts.add (Box.createHorizontalStrut (5));

        JButton finish = UIUtils.createButton (getUIString (prefix,
                                                                        LanguageStrings.buttons,
                                                                        LanguageStrings.finish,
                                                                        LanguageStrings.text),
                                                      //"Finish",
                                               finishAction);

        finish.setToolTipText (getUIString (prefix,
                                                        LanguageStrings.buttons,
                                                        LanguageStrings.finish,
                                                        LanguageStrings.tooltip));
                                                      //"Stop looking for problems");
        finish.setHorizontalTextPosition (SwingConstants.LEFT);

        buts.add (finish);

        this.ignoredProblemsLabel = UIUtils.createClickableLabel ("",
                                                                  Environment.getIcon ("warning",
                                                                                       Constants.ICON_MENU));
        this.ignoredProblemsLabel.setVisible (false);
        this.ignoredProblemsLabel.setBorder (UIUtils.createPadding (0, 10, 0, 0));

        this.updateIgnoredProblemsLabel ();

        this.ignoredProblemsLabel.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

               int s = _this.problemFinder.getIgnoredIssues ().size ();

               String pl = (s > 1 ? "s" :"");

               UIUtils.createQuestionPopup (_this.projectViewer,
                                            getUIString (prefix,
                                                                     LanguageStrings.unignore,
                                                                     LanguageStrings.confirmpopup,
                                                                     LanguageStrings.title),
                                            //"Un-ignore " + s + " problem" + pl,
                                            Constants.PROBLEM_FINDER_ICON_NAME,
                                            String.format (getUIString (prefix,
                                                                                    LanguageStrings.unignore,
                                                                                    LanguageStrings.confirmpopup,
                                                                                    LanguageStrings.text),
                                                           Environment.formatNumber (s)),
                                            //"Please confirm you wish to un-ignore the " +
                                            //s +
                                            //" problem" + pl + "?",
                                            getUIString (prefix,
                                                                     LanguageStrings.unignore,
                                                                     LanguageStrings.confirmpopup,
                                                                     LanguageStrings.confirm),
                                            //"Yes, un-ignore " + (s == 1 ? "it" : "them"),
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
        buts.add (Box.createHorizontalGlue ());

        buts.setBorder (UIUtils.createPadding (10, 10, 7, 10));

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
                                   getUIString (LanguageStrings.project,
                                                            LanguageStrings.editorpanel,
                                                            LanguageStrings.actions,
                                                            LanguageStrings.problemfinder,
                                                            LanguageStrings.actionerror));
                                   //"Unable to open problem finding panel");

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

        if (this.ignoredProblemsLabel == null)
        {

            return;

        }

        int s = this.problemFinder.getIgnoredIssues ().size ();

        if (s > 0)
        {

            this.ignoredProblemsLabel.setVisible (true);

            java.util.List<String> prefix = new ArrayList<> ();
            prefix.add (LanguageStrings.project);
            prefix.add (LanguageStrings.editorpanel);
            prefix.add (LanguageStrings.actions);
            prefix.add (LanguageStrings.problemfinder);
            prefix.add (LanguageStrings.ignored);

            String t = null;

            if (s == 1)
            {

                t = getUIString (prefix,
                                             LanguageStrings.single);

            } else {

                t = String.format (getUIString (prefix,
                                                            LanguageStrings.plural),
                                   Environment.formatNumber (s));

            }

            this.ignoredProblemsLabel.setText (t);
            //Environment.replaceObjectNames (s + " problem" + (s > 1 ? "s" : "") + " currently ignored in this {chapter}, click to un-ignore."));

        } else {

            this.ignoredProblemsLabel.setVisible (false);

        }

        this.validate ();
        this.repaint ();

    }

    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        if (ProjectEvent.PROBLEM_FINDER.equals (ev.getType ()))
        {

            this.updateIgnoredProblemsLabel ();

        }

    }

   @Override
   public void close ()
   {

       super.close ();

       this.problemFinder.saveIgnores ();

       this.viewer.removeProjectEventListener (this);

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
