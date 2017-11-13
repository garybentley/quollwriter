package com.quollwriter.editors.ui;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.dnd.*;
import java.awt.event.*;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.db.*;

import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.*;

import com.quollwriter.ui.components.TabHeader;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class EditorProjectViewer extends AbstractProjectViewer
{

    public static final String TAB_OBJECT_TYPE = "tab";
    public static final int    NEW_COMMENT_ACTION = 3002; // "newComment";
    public static final int    VIEW_EDITOR_CHAPTER_INFO_ACTION = 105; // "viewEditorChapterInfo";

    public static final int DELETE_COMMENT_ACTION = 3001; // "deleteComment";
    public static final int EDIT_COMMENT_ACTION = 3000;
    public static final int COMPLETE_EDITING_ACTION = 3003;

    private Date            sessionStart = new Date ();
    private EditorProjectSideBar  sideBar = null;
    private DefaultChapterItemViewPopupProvider chapterItemViewPopupProvider = null;
    private IconProvider iconProvider = null;

    public EditorProjectViewer()
    {

        final EditorProjectViewer _this = this;
/*
        ObjectProvider<Note> noteProvider = new GeneralObjectProvider<Note> ()
        {

            public Set<Note> getAll ()
            {

                return (Set<Note>) _this.getAllNotes ();

            }

            public Note getByKey (Long key)
            {

                throw new UnsupportedOperationException ("Not supported");

            }

            public void save (Note   obj)
                              throws GeneralException
            {

                _this.saveObject (obj,
                                  true);

            }

            public void saveAll (java.util.List<Note> objs)
                                 throws    GeneralException
            {

                _this.saveObjects (objs,
                                   false);

            }

        };
        */
        this.chapterItemViewPopupProvider = new DefaultChapterItemViewPopupProvider ()
        {

            @Override
            public boolean canEdit (ChapterItem it)
            {

                if (it instanceof Note)
                {

                    Note n = (Note) it;

                    return !n.isDealtWith ();

                }

                return true;

            }

            @Override
            public boolean canDelete (ChapterItem it)
            {

                if (it instanceof Note)
                {

                    Note n = (Note) it;

                    return !n.isDealtWith ();

                }

                return true;

            }

        };

        this.chapterItemViewPopupProvider.setShowLinks (false);
        this.chapterItemViewPopupProvider.setFormatDetails (Note.OBJECT_TYPE,
                                                            new NoteFormatDetails<EditorProjectViewer> ()
                                                            {

                                                                @Override
                                                                public String getTitle (Note item)
                                                                {

                                                                    return "{Comment}";

                                                                }

                                                                @Override
                                                                public String getIcon (Note item)
                                                                {

                                                                    return Constants.COMMENT_ICON_NAME;

                                                                }

                                                                @Override
                                                                public String getItemDescription (Note item)
                                                                {

                                                                    return item.getDescription ().getMarkedUpText ();

                                                                }

                                                                @Override
                                                                public ActionListener getEditItemActionHandler (Note                                   item,
                                                                                                                ChapterItemViewer<EditorProjectViewer> ep)
                                                                {

                                                                    return new CommentActionHandler (item,
                                                                                                     ep);

                                                                }

                                                                @Override
                                                                public ActionListener getDeleteItemActionHandler (Note                                   item,
                                                                                                                  ChapterItemViewer<EditorProjectViewer> ep,
                                                                                                                  boolean                                showAtItem)
                                                                {

                                                                    // Should really add generics for this.
                                                                    return new DeleteCommentActionHandler (item,
                                                                                                           ep.getViewer (),
                                                                                                           showAtItem);

                                                                }

                                                            });

        this.iconProvider = new DefaultIconProvider ()
        {

            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {

                name = Constants.COMMENT_ICON_NAME;

                return super.getIcon (name,
                                      type);

            }

        };

        this.sideBar = new EditorProjectSideBar (this);

    }

    public IconProvider getIconProvider ()
    {

        return this.iconProvider;

    }

    public ChapterItemViewPopupProvider getChapterItemViewPopupProvider ()
    {

        return this.chapterItemViewPopupProvider;

    }

    public void initActionMappings (ActionMap am)
    {

        super.initActionMappings (am);

    }

    public void initKeyMappings (InputMap im)
    {

        super.initKeyMappings (im);

    }

    public void showObjectInTree (String      treeObjType,
                                  NamedObject obj)
    {

        this.sideBar.showObjectInTree (treeObjType,
                                       obj);

    }

    public void reloadTreeForObjectType (String objType)
    {

        this.sideBar.reloadTreeForObjectType (objType);

    }

    public void reloadTreeForObjectType (NamedObject obj)
    {

        this.sideBar.reloadTreeForObjectType (obj.getObjectType ());

    }

    public AbstractSideBar getMainSideBar ()
    {

        return this.sideBar;

    }

    public void fillFullScreenTitleToolbar (JToolBar toolbar)
    {

        this.fillTitleToolbar (toolbar);

        WordCountTimerBox b = new WordCountTimerBox (this.getFullScreenFrame (),
                                                     Constants.ICON_FULL_SCREEN_ACTION,
                                                     this.getWordCountTimer ());

        b.setBarHeight (20);

        toolbar.add (b);

    }

    public void fillTitleToolbar (JToolBar toolbar)
    {

    }

    public void fillSettingsPopup (JPopupMenu titlePopup)
    {

        final EditorProjectViewer _this = this;

        JMenuItem mi = null;

        // Open project.
        titlePopup.add (this.createMenuItem ("Open {Project}",
                                             Constants.OPEN_PROJECT_ICON_NAME,
                                             EditorProjectViewer.OPEN_PROJECT_ACTION));

        // Close Project
        titlePopup.add (this.createMenuItem ("Close {Project}",
                                             Constants.CLOSE_ICON_NAME,
                                             EditorProjectViewer.CLOSE_PROJECT_ACTION));

        // Delete Project
        titlePopup.add (this.createMenuItem ("Delete {Project}",
                                             Constants.DELETE_ICON_NAME,
                                             EditorProjectViewer.COMPLETE_EDITING_ACTION));

    }

    public void switchToProjectVersion (ProjectVersion pv)
    {

        if (pv == null)
        {

            throw new IllegalArgumentException ("Expected a project version");

        }

        Set<Chapter> chaps = null;

        try
        {

            chaps = ((ChapterDataHandler) this.getObjectManager ().getHandler (Chapter.class)).getChaptersForVersion (pv,
                                                                                                                      null,
                                                                                                                      null,
                                                                                                                      true);

        } catch (Exception e) {

            Environment.logError ("Unable to get project at version: " +
                                  pv,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to open project at that version, please contact Quoll Writer support for assistance.");

            return;

        }

        // Close all the current panels and save the state.
        this.closeAllTabs (true);

        // Remove all the chapters from the book.
        this.proj.getBook (0).removeAllChapters ();

        for (Chapter c : chaps)
        {

            this.proj.getBook (0).addChapter (c);

        }

        this.proj.setProjectVersion (pv);

        this.setViewerTitle (this.getViewerTitle ());

        EditorProjectSideBar epb = null;

        try
        {

            epb = new EditorProjectSideBar (this);

            epb.init (null);

        } catch (Exception e) {

            Environment.logError ("Unable to init new editor project side bar",
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to open project at that version, please contact Quoll Writer support for assistance.");

            // Need to close and reopen the project?

            return;

        }

        this.sideBar = epb;

        this.setMainSideBar (this.sideBar);

        this.restoreTabs ();

    }

/*
    public boolean viewEditors ()
                         throws GeneralException
    {

        // See if the user has an account or has already registered, if so show the sidebar
        // otherwise show the register.
        if (!EditorsEnvironment.hasRegistered ())
        {

            EditorsUIUtils.showRegister (this);

            return true;

        }

        EditorsSideBar sb = new EditorsSideBar (this);

        this.addSideBar ("editors",
                         sb);

        this.showSideBar ("editors");

        return true;

    }
  */

    private void showCompleteEditing ()
    {

        final EditorProjectViewer _this = this;

        final Project _proj = this.proj;

        // Check for unsent comments.

        final ActionListener deleteProj = new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                final QPopup qp = UIUtils.createClosablePopup ("Delete {Project}?",
                                                               Environment.getIcon (Constants.DELETE_ICON_NAME,
                                                                                    Constants.ICON_POPUP),
                                                               null);

                Box content = new Box (BoxLayout.Y_AXIS);

                JTextPane desc = UIUtils.createHelpTextPane (String.format ("To delete {Project} <b>%s</b> please enter the word <b>Yes</b> into the box below.<br /><br />Warning!  All information/{comments} associated with the {project} will be deleted.<br /><br />A message will also be sent to <b>%s</b> telling them you are no longer editing the {project}.",
                                                                            _this.proj.getName (),
                                                                            _this.proj.getForEditor ().getShortName ()),
                                                             _this);

                content.add (desc);
                desc.setBorder (null);
                desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                             desc.getPreferredSize ().height));

                content.add (Box.createVerticalStrut (10));

                final JLabel error = UIUtils.createErrorLabel (getUIString (form,errors,affirmativevalue));
                //"Please enter the word Yes.");

                error.setVisible (false);
                error.setBorder (UIUtils.createPadding (0,
                                                  0,
                                                  5,
                                                  0));

                final JTextField text = UIUtils.createTextField ();

                text.setMinimumSize (new Dimension (300,
                                                    text.getPreferredSize ().height));
                text.setPreferredSize (new Dimension (300,
                                                      text.getPreferredSize ().height));
                text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                    text.getPreferredSize ().height));
                text.setAlignmentX (Component.LEFT_ALIGNMENT);

                error.setAlignmentX (Component.LEFT_ALIGNMENT);

                content.add (error);
                content.add (text);

                content.add (Box.createVerticalStrut (10));

                // Blue pill/red pill?
                ActionListener confirmAction = new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        if (!text.getText ().trim ().equalsIgnoreCase (getUIString (form,affirmativevalue)))
                        {

                            error.setVisible (true);

                            qp.resize ();

                            return;

                        }

                        qp.removeFromParent ();

                        EditorsEnvironment.sendProjectEditStopMessage (_proj,
                        new ActionListener ()
                        {

                            public void actionPerformed (ActionEvent ev)
                            {

                                _this.close (true,
                                             new ActionListener ()
                                {

                                    public void actionPerformed (ActionEvent ev)
                                    {

                                        Environment.deleteProject (_proj,
                                        new ActionListener ()
                                        {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                // TODO: Really must sort this mess out.
                                                UIUtils.showMessage ((Component) null,
                                                                     "{Project} deleted",
                                                                     String.format ("The {project} has been deleted and a message has been sent to <b>%s</b> to let them know.",
                                                                                    _proj.getForEditor ().getShortName ()),
                                                                     null,
                                                                     new ActionListener ()
                                                                     {

                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {

                                                                            Environment.showLandingIfNoOpenProjects ();

                                                                        }

                                                                     });

                                            }

                                         });

                                    }

                                });

                            }

                        });

                    }

                };

                JButton confirm = UIUtils.createButton ("Yes, delete it",
                                                        confirmAction);

                UIUtils.addDoActionOnReturnPressed (text,
                                                    confirmAction);

                JButton cancel = UIUtils.createButton (Constants.CANCEL_BUTTON_LABEL_ID,
                                                       new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        qp.removeFromParent ();

                    }

                });

                JButton[] buts = new JButton[] { confirm, cancel };

                JComponent bs = UIUtils.createButtonBar2 (buts,
                                                          Component.LEFT_ALIGNMENT);
                bs.setAlignmentX (Component.LEFT_ALIGNMENT);
                content.add (bs);
                content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
                qp.setContent (content);

                content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                         content.getPreferredSize ().height));

                _this.showPopupAt (qp,
                                   UIUtils.getCenterShowPosition (_this,
                                                                  qp),
                                   false);

                qp.setDraggable (_this);

                text.grabFocus ();

            }

        };

        // Send project edit complete message.

        // Delete project.

        int count = 0;

        try
        {

            count = this.getUnsentComments ().size ();

        } catch (Exception e) {

            Environment.logError ("Unable to get unsent comments for project: " +
                                  this.proj,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to check for unsent comments, please contact Quoll Writer support for assistance.");

            // Let things through.

        }

        if (count > 0)
        {

            String s = "are %s comments";

            if (count == 1)
            {

                UIUtils.createQuestionPopup (this,
                                             "Unsent comment",
                                             Constants.ERROR_ICON_NAME,
                                             String.format ("There is <b>1</b> {comment} that you have not yet sent to <b>%s</b>.<br /><br />Do you wish to send it now?",
                                                            this.getProject ().getForEditor ().getShortName ()),
                                             "Yes, I'll send it now",
                                             "No, don't send it",
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    EditorsUIUtils.showSendUnsentComments (_this,
                                                                                           deleteProj);

                                                }

                                             },
                                             deleteProj,
                                             null,
                                             null);

            } else {

                UIUtils.createQuestionPopup (this,
                                             "Unsent comments",
                                             Constants.ERROR_ICON_NAME,
                                             String.format ("There are <b>%s</b> comments that you have not yet sent to <b>%s</b>.<br /><br />Do you wish to send them now?",
                                                            count,
                                                            this.getProject ().getForEditor ().getShortName ()),
                                             "Yes, I'll send them now",
                                             "No, don't send them",
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    EditorsUIUtils.showSendUnsentComments (_this,
                                                                                           deleteProj);

                                                }

                                             },
                                             deleteProj,
                                             null,
                                             null);

            }

            return;

        }

        deleteProj.actionPerformed (new ActionEvent ("call", 1, "call"));

    }

    @Override
    public Action getAction (int               name,
                             final NamedObject other)
    {

        final EditorProjectViewer pv = this;

        if (name == COMPLETE_EDITING_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.showCompleteEditing ();

                }

            };

        }

        if (name == EditorProjectViewer.DELETE_PROJECT_ACTION)
        {

            return null;

        }

        Action a = super.getAction (name,
                                    other);

        if (a != null)
        {

            return a;

        }

        if (name == EditorProjectViewer.VIEW_EDITOR_CHAPTER_INFO_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.viewEditorChapterInformation ((Chapter) other);

                }

            };

        }

        if (name == EditorProjectViewer.EDIT_COMMENT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (final ActionEvent ev)
                {

                    final Chapter c = ((Note) other).getChapter ();

                    pv.viewObject (c);

                    UIUtils.doLater (new ActionListener ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            try
                            {

                                EditorChapterPanel qep = (EditorChapterPanel) pv.getEditorForChapter (c);

                                qep.editNote ((Note) other);

                            } catch (Exception e)
                            {

                                Environment.logError ("Unable to edit comment: " +
                                                      other,
                                                      e);

                                UIUtils.showErrorMessage (pv,
                                                          "Unable to edit {Comment}");

                            }

                        }

                    });

                }

            };

        }

        if (name == EditorProjectViewer.DELETE_COMMENT_ACTION)
        {

            return new DeleteCommentActionHandler ((Note) other,
                                                   this,
                                                   false);

        }

        if (name == EditorProjectViewer.NEW_COMMENT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Chapter c = (Chapter) other;

                    EditorChapterPanel qep = pv.getEditorForChapter (c);

                    String text = qep.getEditor ().getText ();

                    int pos = 0;

                    if (text != null)
                    {

                        pos = text.length ();

                    }

                    pv.scrollTo (c,
                                 pos);

                    new CommentActionHandler<EditorProjectViewer> (c,
                                                                   qep,
                                                                   pos).actionPerformed (ev);

                }

            };

        }

        throw new IllegalArgumentException ("Action: " +
                                            name +
                                            " not known.");

    }

    @Override
    public void handleNewProject ()
    {

        Book b = this.proj.getBooks ().get (0);

        Chapter c = b.getFirstChapter ();

        // Create a new chapter for the book.
        if (c == null)
        {

            throw new IllegalArgumentException ("No chapter found.");

        }

        // Refresh the chapter tree.
        this.reloadTreeForObjectType (c.getObjectType ());

        this.handleOpenProject ();

        this.editChapter (c,
                          null);

    }

    @Override
    public String getViewerIcon ()
    {

        return "editors";//this.proj.getObjectType ();

    }

    @Override
    public String getViewerTitle ()
    {

        ProjectVersion pv = this.proj.getProjectVersion ();

        String suff = "";

        if ((pv != null)
            &&
            (pv.getName () != null)
           )
        {

            suff = String.format (" (%s)",
                                  pv.getName ());

        }

        return String.format ("Editing%s: %s",
                              suff,
                              this.proj.getName ());

    }

    @Override
    public void handleHTMLPanelAction (String v)
    {

        StringTokenizer t = new StringTokenizer (v,
                                                 ",;");

        if (t.countTokens () > 1)
        {

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                this.handleHTMLPanelAction (tok);

            }

            return;

        }

        if (v.equals ("find"))
        {

            this.showFind (null);

            return;

        }

        super.handleHTMLPanelAction (v);

    }

    @Override
    public void handleOpenProject ()
    {

        // TODO: Add achievements later.
        Environment.removeFromAchievementsManager (this);

        // See if we have any state, if not then this is probably the first time we've opened the project
        // then open the first chapter.

        if (this.getOpenTabsProperty () == null)
        {

            // No state, open the first chapter.
            Book b = this.proj.getBooks ().get (0);

            Chapter c = b.getFirstChapter ();

            // Create a new chapter for the book.
            if (c != null)
            {

                this.editChapter (c,
                                  null);

            }

        }

        final EditorProjectViewer _this = this;

        if (this.getProject ().getForEditor ().isPrevious ())
        {

            UIUtils.doLater (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    UIUtils.showMessage (_this,
                                         "Note: this is a {project} for a previous {contact}.  You can no longer send {comments}.");

                }

            });

        }

    }

    public void expandNoteTypeInNoteTree (String type)
    {

        TreeParentNode tpn = new TreeParentNode (Note.OBJECT_TYPE,
                                                 type);

        DefaultTreeModel dtm = (DefaultTreeModel) this.getNoteTree ().getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        this.getNoteTree ().expandPath (UIUtils.getTreePathForUserObject (root,
                                                                          tpn));

    }
/*
    private void initNoteTree (boolean restoreSavedOpenTypes)
    {

        if (restoreSavedOpenTypes)
        {

            String openTypes = this.proj.getProperty (Constants.NOTE_TREE_OPEN_TYPES_PROPERTY_NAME);

            if (openTypes != null)
            {

                DefaultTreeModel dtm = (DefaultTreeModel) this.getNoteTree ().getModel ();

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

                // Split on :
                StringTokenizer t = new StringTokenizer (openTypes,
                                                         "|");

                while (t.hasMoreTokens ())
                {

                    String tok = t.nextToken ().trim ();

                    TreeParentNode tpn = new TreeParentNode (Note.OBJECT_TYPE,
                                                             tok);

                    this.getNoteTree ().expandPath (UIUtils.getTreePathForUserObject (root,
                                                                                      tpn));

                }

            }

        }

    }
*/
    public void handleItemChangedEvent (ItemChangedEvent ev)
    {

        if (ev.getChangedObject () instanceof Chapter)
        {

            this.sideBar.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

        }

        if (ev.getChangedObject () instanceof Note)
        {

            this.sideBar.reloadTreeForObjectType (Note.OBJECT_TYPE);

        }

    }

    public void reloadNoteTree ()
    {

        this.sideBar.reloadTreeForObjectType (Note.OBJECT_TYPE);

    }

    public void reloadChapterTree ()
    {

        this.sideBar.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

    }

    @Override
    public void doSaveState ()
    {

    }

    public EditorChapterPanel getEditorForChapter (Chapter c)
    {

        for (QuollPanel qp : this.getAllQuollPanelsForObject (c))
        {

            if (qp instanceof FullScreenQuollPanel)
            {

                qp = ((FullScreenQuollPanel) qp).getChild ();

            }

            if (qp instanceof EditorChapterPanel)
            {

                return (EditorChapterPanel) qp;

            }

        }

        return null;

    }

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter has been opened for editing.
     */
    public boolean editChapter (Chapter c,
                                ActionListener doAfterView)
    {

        // Check our tabs to see if we are already editing this chapter, if so then just switch to it instead.
        EditorChapterPanel qep = (EditorChapterPanel) this.getQuollPanelForObject (c);

        if (qep != null)
        {

            this.setPanelVisible (qep);

            this.getEditorForChapter (c).getEditor ().grabFocus ();

            if (doAfterView != null)
            {

                UIUtils.doActionWhenPanelIsReady (qep,
                                                  doAfterView,
                                                  c,
                                                  "afterview");

            }

            return true;

        }

        final EditorProjectViewer _this = this;

        try
        {

            qep = new EditorChapterPanel (this,
                                          c);

            qep.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (_this,
                                      "Unable to edit {chapter}: " +
                                      c.getName ());

            return false;

        }

        final TabHeader th = this.addPanel (qep);

        qep.addActionListener (new ActionAdapter ()
        {


            public void actionPerformed (ActionEvent ev)
            {

                if (ev.getID () == QuollPanel.UNSAVED_CHANGES_ACTION_EVENT)
                {

                    th.setComponentChanged (true);

                }

            }

        });

        this.addNameChangeListener (c,
                                    qep);

        // Open the tab :)
        return this.editChapter (c,
                                 doAfterView);

    }

    public boolean viewObject (DataObject     d,
                               ActionListener doAfterView)
    {

        if (d instanceof Note)
        {

            final Note n = (Note) d;

            if (n.getObject () != null)
            {

                this.viewNote (n,
                               doAfterView);

            }

            return true;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            return this.editChapter (c,
                                     doAfterView);

        }

        // Record the error, then ignore.
        Environment.logError ("Unable to open object: " + d);

        return false;

    }

    public boolean viewObject (DataObject d)
    {

        return this.viewObject (d,
                                null);

    }

    public void viewNote (final Note           n,
                          final ActionListener doAfterView)
    {

        try
        {

            // Need to change this.
            if (n.getObject () instanceof Chapter)
            {

                final Chapter c = (Chapter) n.getObject ();

                final EditorProjectViewer _this = this;

                this.editChapter (c,
                                  new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        EditorChapterPanel qep = _this.getEditorForChapter (c);

                        try
                        {

                            qep.showNote (n,
                                          doAfterView);

                        } catch (Exception e) {

                            Environment.logError ("Unable to show note: " +
                                                  n,
                                                  e);

                            UIUtils.showErrorMessage (_this,
                                                      "Unable to show {comment}, please contact Quoll Writer support for assistance.");

                        }

                    }

                });

                return;

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to show note: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to show " + Environment.getObjectTypeName (n).toLowerCase () + ".");

        }

    }

    public boolean openPanel (String id)
    {

        return false;

    }

    protected void addNameChangeListener (final NamedObject             n,
                                          final ProjectObjectQuollPanel qp)
    {

        final EditorProjectViewer _this = this;

        qp.addObjectPropertyChangedListener (new PropertyChangedListener ()
        {

            @Override
            public void propertyChanged (PropertyChangedEvent ev)
            {

                if (ev.getChangeType ().equals (NamedObject.NAME))
                {

                    _this.setTabHeaderTitle (qp,
                                             qp.getTitle ());

                    _this.informTreeOfNodeChange (n,
                                                  _this.getTreeForObjectType (n.getObjectType ()));

                }

            }

        });

    }

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter information is viewed.
     */
    public boolean viewEditorChapterInformation (final Chapter c)
    {
/*
        ChapterInformationSideBar cb = new ChapterInformationSideBar (this,
                                                                      c);

        this.addSideBar ("chapterinfo-" + c.getKey (),
                         cb);

        this.showSideBar ("chapterinfo-" + c.getKey ());
  */
        return true;

    }

    public JTree getTreeForObjectType (String objType)
    {

        return this.sideBar.getTreeForObjectType (objType);

    }

    public void addChapterToTreeAfter (Chapter newChapter,
                                       Chapter addAfter)
    {

        DefaultTreeModel model = (DefaultTreeModel) this.getChapterTree ().getModel ();

        DefaultMutableTreeNode cNode = new DefaultMutableTreeNode (newChapter);

        if (addAfter == null)
        {

            // Get the book node.
            TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                            newChapter.getBook ());

            if (tp != null)
            {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                model.insertNodeInto (cNode,
                                      (MutableTreeNode) node,
                                      0);

            } else
            {

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot ();

                model.insertNodeInto (cNode,
                                      root,
                                      root.getChildCount ());

            }

        } else
        {

            // Get the "addAfter" node.
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                     addAfter).getLastPathComponent ();

            model.insertNodeInto (cNode,
                                  (MutableTreeNode) node.getParent (),
                                  node.getParent ().getIndex (node) + 1);

        }

        this.getChapterTree ().setSelectionPath (new TreePath (cNode.getPath ()));

    }

    public JTree getNoteTree ()
    {

        return this.getTreeForObjectType (Note.OBJECT_TYPE);

    }

    public JTree getChapterTree ()
    {

        return this.getTreeForObjectType (Chapter.OBJECT_TYPE);

    }

    public void deleteObject (NamedObject o,
                              boolean     deleteChildObjects)
                       throws GeneralException
    {

        if (o instanceof ChapterItem)
        {

            this.deleteChapterItem ((ChapterItem) o,
                                    deleteChildObjects,
                                    true);

            return;

        }

        this.deleteObject (o);

    }

    public void deleteObject (NamedObject o)
                              throws      GeneralException
    {

        if (o instanceof Chapter)
        {

            this.deleteChapter ((Chapter) o);

        }

        if (o instanceof ChapterItem)
        {

            this.deleteChapterItem ((ChapterItem) o,
                                    false,
                                    false);

        }

    }

    public void deleteChapterItem (ChapterItem ci,
                                   boolean     deleteChildObjects,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        if (ci.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.deleteNote ((Note) ci,
                             doInTransaction);

        }

    }

    public void deleteNote (Note    n,
                            boolean doInTransaction)
                     throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = n.getOtherObjectsInLinks ();

        NamedObject obj = n.getObject ();

        // Need to get the links, they may not be setup.
        this.setLinks (n);

        this.dBMan.deleteObject (n,
                                 false,
                                 null);

        obj.removeNote (n);

        this.fireProjectEvent (n.getObjectType (),
                               ProjectEvent.DELETE,
                               n);

        this.refreshObjectPanels (otherObjects);

        if (obj instanceof Chapter)
        {

            EditorChapterPanel qep = this.getEditorForChapter ((Chapter) obj);

            if (qep != null)
            {

                qep.removeItem (n);

            }

        }

        this.reloadNoteTree ();

        this.reloadChapterTree ();

    }

    public void chapterTreeChanged (DataObject d)
    {

        DefaultTreeModel model = (DefaultTreeModel) this.getChapterTree ().getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                 d).getLastPathComponent ();

        model.nodeStructureChanged (node);

    }

    public void scrollTo (final Chapter c,
                          final int     pos)
    {

        final EditorProjectViewer _this = this;

        this.editChapter (c,
                          new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    _this.getEditorForChapter (c).scrollToPosition (pos);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to show snippet at position: " +
                                          pos,
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to show snippet in context");

                }

            }

        });

    }

    public Set<Note> getNotesForType (String t)
    {

        Set<Note> notes = this.getAllNotes ();

        Set<Note> ret = new TreeSet (new ChapterItemSorter ());

        for (Note n : notes)
        {

            if (n.getType ().equals (t))
            {

                ret.add (n);

            }

        }

        return ret;

    }

    public Set<Note> getAllNotes ()
    {

        Set<Note> notes = new HashSet ();

        Book b = this.proj.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

        for (Chapter c : chapters)
        {

            notes.addAll (c.getNotes ());

        }

        return notes;

    }

    public TypesHandler getObjectTypesHandler (String objType)
    {

        return null;

    }

    public void updateChapterIndexes (Book b)
                               throws GeneralException
    {

        this.dBMan.updateChapterIndexes (b);

    }

    public String getChapterObjectName ()
    {

        return Environment.getObjectTypeName (Chapter.OBJECT_TYPE);

    }

    public void deleteChapter (Chapter c)
    {

        throw new UnsupportedOperationException ("Not supported");

    }

    public Set<FindResultsBox> findText (String t)
    {

        Set<FindResultsBox> res = new LinkedHashSet ();

        // Get the snippets.
        Map<Chapter, java.util.List<Segment>> snippets = this.getTextSnippets (t);

        if (snippets.size () > 0)
        {

            res.add (new ChapterFindResultsBox (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE),
                                                Chapter.OBJECT_TYPE,
                                                Chapter.OBJECT_TYPE,
                                                this,
                                                snippets));

        }

        Set<Note> notes = this.proj.getNotesContaining (t);

        if (notes.size () > 0)
        {

            res.add (new NamedObjectFindResultsBox<Note> ("{Comments}",
                                                          Constants.COMMENT_ICON_NAME,
                                                          Note.OBJECT_TYPE,
                                                          this,
                                                          notes));

        }

        return res;

    }

    public Set<Note> getUnsentComments (ProjectVersion pv)
                                 throws GeneralException
    {

        return this.getDealtWithNotes (pv,
                                       false);

    }

    public Set<Note> getUnsentComments ()
                                 throws GeneralException
    {

        return this.getUnsentComments (this.proj.getProjectVersion ());

    }

    @Override
    public Set<String> getTitleHeaderControlIds ()
	{

		Set<String> ids = new LinkedHashSet ();

		ids.add (CONTACTS_HEADER_CONTROL_ID);
		ids.add (FIND_HEADER_CONTROL_ID);
		ids.add (FULL_SCREEN_HEADER_CONTROL_ID);
        ids.add (SETTINGS_HEADER_CONTROL_ID);

		return ids;

	}

}
