package com.quollwriter.editors.ui;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.*;
import java.util.function.*;

import javafx.scene.control.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.beans.property.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.db.*;

import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;

import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorProjectViewer extends AbstractProjectViewer
{

    public interface CommandId extends AbstractProjectViewer.CommandId
    {

        String completeediting = "completeediting";

    }

    public static final String TAB_OBJECT_TYPE = "tab";
    public static final int    NEW_COMMENT_ACTION = 3002; // "newComment";
    public static final int    VIEW_EDITOR_CHAPTER_INFO_ACTION = 105; // "viewEditorChapterInfo";

    public static final int DELETE_COMMENT_ACTION = 3001; // "deleteComment";
    public static final int EDIT_COMMENT_ACTION = 3000;
    public static final int COMPLETE_EDITING_ACTION = 3003;

    private Date            sessionStart = new Date ();
    private EditorProjectSideBar  sideBar = null;

    public EditorProjectViewer ()
                         //throws GeneralException
    {

        final EditorProjectViewer _this = this;
        this.sideBar = new EditorProjectSideBar (this);

        this.initActionMappings ();

    }
/*
    @Override
    public SideBar getMainSideBar ()
    {

        return this.sideBar.getSideBar ();

    }
*/
/*
TODO
    public void fillFullScreenTitleToolbar (JToolBar toolbar)
    {

        this.fillTitleToolbar (toolbar);

        WordCountTimerBox b = new WordCountTimerBox (this.getFullScreenFrame (),
                                                     Constants.ICON_FULL_SCREEN_ACTION,
                                                     this.getWordCountTimer ());

        b.setBarHeight (20);

        toolbar.add (b);

    }
*/
    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        final EditorProjectViewer _this = this;

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            List<String> prefix = Arrays.asList (editors,LanguageStrings.project,settingsmenu,LanguageStrings.items);

            items.add (QuollMenuItem.builder ()
                .label (Utils.newList (prefix,openproject))
                .iconName (StyleClassNames.OPEN)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.openproject);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (Utils.newList (prefix,closeproject))
                .iconName (StyleClassNames.CLOSE)
                .onAction (ev ->
                {

                    _this.runCommand (CommandId.closeproject);

                })
                .build ());

            items.add (QuollMenuItem.builder ()
                .label (Utils.newList (prefix,deleteproject))
                .iconName (StyleClassNames.DELETE)
                .onAction (ev ->
                {

                    this.showCompleteEditing ();

                })
                .build ());

            return items;

        };

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

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (editors,LanguageStrings.project,actions,switchtoversion,actionerror));
                                      //"Unable to open project at that version, please contact Quoll Writer support for assistance.");

            return;

        }

        // Close all the current panels and save the state.
        this.closeAllTabs ();

        // Remove all the chapters from the book.
        this.project.getBook (0).removeAllChapters ();

        for (Chapter c : chaps)
        {

            this.project.getBook (0).addChapter (c);

        }

        this.project.setProjectVersion (pv);

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

  @Override
  public boolean addToAchievementsManager ()
  {

      return false;

  }

    private void showCompleteEditing ()
    {

        final EditorProjectViewer _this = this;

        final Project _proj = this.project;

        Runnable deleteProj = () ->
        {

            // Check for unsent comments.
            List<String> prefix = Arrays.asList (editors,LanguageStrings.project,actions,deleteproject,popup);

            QuollPopup.yesConfirmTextEntryBuilder ()
                .title (Utils.newList (prefix,title))
                .styleClassName (StyleClassNames.DELETE)
                //.styleSheet ("deleteeditorproject")
                .inViewer (this)
                .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           this.project.getName (),
                                                           this.project.getForEditor ().getMainName ()))
                .confirmButtonLabel (Utils.newList (prefix,buttons,confirm))
                .cancelButtonLabel (Utils.newList (prefix,buttons,cancel))
                .onConfirm (ev ->
                {

                    EditorsEnvironment.sendProjectEditStopMessage (this.project,
                                                                   () ->
                    {

                        UIUtils.runLater (() ->
                        {

                            Environment.setCloseDownAllowed (false);

                            _this.close (true,
                                         () ->
                            {

                                Environment.setCloseDownAllowed (true);

                                Environment.deleteProject (_proj,
                                                           () ->
                                {

                                    List<String> prefx = Arrays.asList (editors,LanguageStrings.project,actions,deleteproject,confirmpopup);

                                    UIUtils.runLater (() ->
                                    {

                                        QuollPopup.messageBuilder ()
                                            .title (Utils.newList (prefx,title))
                                            .message (getUILanguageStringProperty (Utils.newList (prefx,text),
                                                                                   _proj.getForEditor ().mainNameProperty ()))
                                            .closeButton (null,
                                                          () ->
                                                          {

                                                              Environment.showAllProjectsViewerIfNoOpenProjects ();

                                                          })
                                            .build ();

                                    });

                                 });

                            });

                        });

                    });

                })
                .build ();

        };

        int count = 0;

        try
        {

            count = this.getUnsentComments ().size ();

        } catch (Exception e) {

            Environment.logError ("Unable to get unsent comments for project: " +
                                  this.project,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (editors,LanguageStrings.project,actions,unsentcomments,actionerror));
                                      //"Unable to check for unsent comments, please contact Quoll Writer support for assistance.");

            // Let things through.

        }

        if (count > 0)
        {

            List<String> prefix2 = Arrays.asList (editors,LanguageStrings.project,actions,deleteproject,unsentcommentspopup);

            QuollPopup.questionBuilder ()
                .title (Utils.newList (prefix2,title))
                .inViewer (this)
                .styleClassName (StyleClassNames.COMMENTS)
                .styleSheet (StyleClassNames.COMMENTS)
                .message (getUILanguageStringProperty (Utils.newList (prefix2,text),
                                                       count,
                                                       this.getProject ().getForEditor ().mainNameProperty ()))
                .confirmButtonLabel (Utils.newList (prefix2,buttons,confirm))
                .cancelButtonLabel (Utils.newList (prefix2,buttons,cancel))
                .onConfirm (ev ->
                {

                    EditorsUIUtils.showSendUnsentComments (this,
                                                           deleteProj);

                })
                .onCancel (ev ->
                {

                    UIUtils.runLater (deleteProj);

                })
                .build ();

            return;

        }

        UIUtils.runLater (deleteProj);

    }

    private void initActionMappings ()
    {

        this.addActionMapping (() ->
        {

            this.showCompleteEditing ();

        },
        CommandId.completeediting);

        this.addActionMapping (new CommandWithArgs<DataObject> (objs ->
        {

            DataObject o = null;

            if ((objs != null)
                &&
                (objs.length > 0)
               )
            {

                o = objs[0];

            }

            if (o == null)
            {

                throw new IllegalArgumentException ("No object provided.");

            }

            this.editObject (o);

        },
        CommandId.editobject));

    }

    public void editObject (DataObject d)
    {

        if (d instanceof Chapter)
        {

            this.editChapter ((Chapter) d,
                              null);
            return;

        }

        if (d instanceof Note)
        {

            this.editComment ((Note) d);

        }

    }

    public void showAddNewComment (Chapter c,
                                   int     pos)
    {

        EditorChapterPanel qep = this.getEditorForChapter (c);

        qep.scrollToTextPosition (pos,
                                  () ->
        {

            qep.showAddNewComment (pos);

        });

    }

    public void editComment (Note n)
    {

        Chapter c = n.getChapter ();

        this.editChapter (c,
                          () ->
        {

            try
            {

                EditorChapterPanel editor = this.getEditorForChapter (c);
                editor.editComment (n);

            } catch (Exception e) {

                Environment.logError ("Unable to edit comment: " +
                                      n,
                                      e);
                ComponentUtils.showErrorMessage (this,
                                                 getUILanguageStringProperty (editors,LanguageStrings.project,actions,editcomment,actionerror));

            }

        });

    }

    @Override
    public void handleNewProject ()
    {

        Book b = this.project.getBooks ().get (0);

        Chapter c = b.getFirstChapter ();

        // Create a new chapter for the book.
        if (c == null)
        {

            throw new IllegalArgumentException ("No chapter found.");

        }

        this.handleOpenProject ();

        this.editChapter (c,
                          null);

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.EDITOR;

    }

    @Override
    public void handleOpenProject ()
    {

        // TODO: Add achievements later.
        // TODO Environment.getAchievementsManager ().removeViewer (this);

        // See if we have any state, if not then this is probably the first time we've opened the project
        // then open the first chapter.

        if (this.getOpenTabsProperty () == null)
        {

            // No state, open the first chapter.
            Book b = this.project.getBooks ().get (0);

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

            UIUtils.forceRunLater (() ->
            {

                QuollPopup.messageBuilder ()
                    .inViewer (this)
                    .closeButton ()
                    .headerIconClassName (StyleClassNames.INFORMATION)
                    .message (editors,LanguageStrings.project,actions,openproject,openerrors,previouscontact)
                    .build ();

            });

        }

    }

    public EditorChapterPanel getEditorForChapter (Chapter c)
    {

        ChapterEditorPanelContent cp = super.getEditorForChapter (c);

        return (EditorChapterPanel) cp;

    }

    public void editChapter (final Chapter  c,
                             final Runnable doAfterView)
    {

        String pid = EditorChapterPanel.getPanelIdForChapter (c);

        if (this.showPanel (pid))
        {

            UIUtils.runLater (doAfterView);

            return;

        }

        try
        {

            EditorChapterPanel p = new EditorChapterPanel (this,
                                                           c);

            this.addPanel (p);

            this.editChapter (c,
                              doAfterView);

        } catch (Exception e) {

            Environment.logError ("Unable to edit chapter: " +
                                  c,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,editchapter,actionerror),
                                                                          c.getName ()));

        }

    }

    @Override
    public void viewObject (DataObject d,
                            Runnable   doAfterView)
    {

        if (d instanceof Note)
        {

            final Note n = (Note) d;

            if (n.getObject () != null)
            {

                this.viewComment (n,
                                  doAfterView);

            }

            return;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            this.editChapter (c,
                              doAfterView);

            return;

        }

        // Record the error, then ignore.
        Environment.logError ("Unable to open object: " + d);

    }

    public void viewObject (DataObject d)
    {

        this.viewObject (d,
                         null);

    }

    public void viewComment (final Note     n,
                             final Runnable doAfterView)
    {

        try
        {

            final Chapter c = n.getChapter ();

            this.editChapter (c,
                              () ->
            {

                EditorChapterPanel pc = this.getEditorForChapter (c);

                if (pc.isReadyForUse ())
                {

                    UIUtils.forceRunLater (() ->
                    {

                        pc.showItem (n,
                                     false);

                        UIUtils.runLater (doAfterView);

                   });

               } else {

                   pc.readyForUseProperty ().addListener ((pr, oldv, nev) ->
                   {

                       UIUtils.forceRunLater (() ->
                       {

                           pc.showItem (n,
                                        false);

                           UIUtils.runLater (doAfterView);

                       });

                   });

               }

            });

        } catch (Exception e)
        {

            Environment.logError ("Unable to show note: " +
                                  n,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (editors,LanguageStrings.project,actions,viewcomment,actionerror));

        }

    }

    public void showDeleteComment (Note n)
    {

        EditorChapterPanel pc = this.getEditorForChapter (n.getChapter ());

        pc.showDeleteCommentPopup (n,
                                   pc.getNodeForChapterItem (n));

    }

/*
TODO remove
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
*/
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
/*
TODO Remove
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
*/
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

        Set<NamedObject> otherObjects = n.getOtherObjectsInLinks ();

        NamedObject obj = n.getObject ();

        // Need to get the links, they may not be setup.

        this.dBMan.deleteObject (n,
                                 false,
                                 null);

        obj.removeNote (n);

        this.fireProjectEvent (ProjectEvent.Type.note,
                               ProjectEvent.Action.delete,
                               n);

    }

/*
TODO?
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

                }

            }

        });

    }
*/
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

        Book b = this.project.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

        for (Chapter c : chapters)
        {

            notes.addAll (c.getNotes ());

        }

        return notes;

    }

    public void deleteChapter (Chapter c)
    {

        throw new UnsupportedOperationException ("Not supported");

    }

    @Override
    public Set<FindResultsBox> findText (String t)
    {

        Set<FindResultsBox> res = new LinkedHashSet<> ();

        FindResultsBox chres = this.findTextInChapters (t);

        if (chres != null)
        {

            res.add (chres);

        }

        Set<Note> notes = this.project.getNotesContaining (t);

        if (notes.size () > 0)
        {

            res.add (new NamedObjectFindResultsBox (getUILanguageStringProperty (objectnames,plural,comment),
                                                    StyleClassNames.COMMENTS,
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

        return this.getUnsentComments (this.project.getProjectVersion ());

    }

    public void setChapterEditComplete (Chapter chapter,
                                        boolean editComplete)
    {

        try
        {

            chapter.setEditComplete (editComplete);

            EditorChapterPanel p = this.getEditorForChapter (chapter);

            int pos = -1;

            if (editComplete)
            {

                if (p != null)
                {

                    pos = Utils.stripEnd (p.getEditor ().getText ()).length ();

                } else {

                    String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

                    pos = Utils.stripEnd (t).length ();

                }

            }

            chapter.setEditPosition (pos);

            this.saveObject (chapter,
                             false);

            p.recreateVisibleParagraphs ();

        } catch (Exception e) {

            Environment.logError ("Unable to set chapter edit complete: " +
                                  chapter,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.project,editorpanel,actions,seteditcomplete,actionerror));

        }

    }

    public void setChapterEditPosition (Chapter chapter,
                                        int     textPos)
    {

        EditorChapterPanel p = this.getEditorForChapter (chapter);

        try
        {

            int _textPos = 0;
            int l = 0;

            if (p != null)
            {

                l = Utils.stripEnd (p.getEditor ().getText ()).length ();

                _textPos = Math.min (textPos, l);

                // See if we are on the last line (it may be that the user is in the icon
                // column).
/*
TODO
                Rectangle2D pp = p.getEditor ().modelToView2D (_textPos);

                if (UserProperties.getAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME))
                {

                    if (_textPos <= l)
                    {

                        Rectangle2D ep = p.getEditor ().modelToView2D (l);

                        chapter.setEditComplete ((Math.round (ep.getY ()) == Math.round (pp.getY ())));

                    }

                }
*/
            } else {

                String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

                l = Utils.stripEnd (t).length ();

            }

            _textPos = Math.min (_textPos, l);

            chapter.setEditComplete (false);
            chapter.setEditPosition (_textPos);

            this.saveObject (chapter,
                             false);

        } catch (Exception e) {

            Environment.logError ("Unable to set edit position for: " + chapter,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.project,editorpanel,actions,seteditposition,actionerror));

            return;

        }

    }

    public void removeChapterEditPosition (Chapter chapter)
    {

        try
        {

            chapter.setEditComplete (false);
            chapter.setEditPosition (-1);

            this.saveObject (chapter,
                             false);

        } catch (Exception e) {

            Environment.logError ("Unable to remove edit position for chapter: " +
                                  chapter,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.project,editorpanel,actions,removeeditposition,actionerror));

        }

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        this.setMainSideBar (this.sideBar);

        this.titleProperty ().unbind ();
        this.titleProperty ().bind (UILanguageStringsManager.createStringPropertyWithBinding (() ->
        {

            ProjectVersion pv = this.project.getProjectVersion ();

            StringProperty suff = new SimpleStringProperty ("");

            if ((pv != null)
                &&
                (pv.getName () != null)
               )
            {

                suff = getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.project,viewertitleversionwrapper),
                                    //" (%s)",
                                                    pv.getName ());

            }

            return getUILanguageStringProperty (Arrays.asList (editors,LanguageStrings.project,viewertitle),
                                        //"Editing%s: %s",
                                                suff,
                                                this.project.getName ()).getValue ();

        },
        this.project.projectVersionProperty ()));

    }

}
