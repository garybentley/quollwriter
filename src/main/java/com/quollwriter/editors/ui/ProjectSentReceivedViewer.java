package com.quollwriter.editors.ui;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;

//import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;

import com.quollwriter.db.*;

import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.uistrings.UILanguageStringsManager;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public abstract class ProjectSentReceivedViewer<E extends EditorMessage> extends AbstractProjectViewer
{

    //private DefaultChapterItemViewPopupProvider chapterItemViewPopupProvider = null;
    private EditorEditor editor = null;
    protected E message = null;
    //private ProjectSentReceivedSideBar<E, ProjectSentReceivedViewer<E>> sideBar = null;

    public ProjectSentReceivedViewer (Project proj,
                                      E       message)
    {

        this.project = proj;
        this.message = message;

        // TODO Set icon of header.
/*
        this.chapterItemViewPopupProvider = new DefaultChapterItemViewPopupProvider ()
        {

            @Override
            public boolean canEdit (ChapterItem it)
            {

                return false;

            }

            @Override
            public boolean canDelete (ChapterItem it)
            {

                return false;

            }

        };

        this.chapterItemViewPopupProvider.setShowLinks (false);
        this.chapterItemViewPopupProvider.setFormatDetails (Note.OBJECT_TYPE,
                                                            new NoteFormatDetails<ProjectSentReceivedViewer> ()
                                                            {

                                                                @Override
                                                                public String getTitle (Note item)
                                                                {

                                                                    return getUILanguageStringProperty (editors,projectcomments,(_this.message.isSentByMe () ? sent : received),comment,view,title);
                                                                    //)"{Comment}";

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
                                                                public ActionListener getEditItemActionHandler (Note                                         item,
                                                                                                                ChapterItemViewer<ProjectSentReceivedViewer> ep)
                                                                {

                                                                    throw new UnsupportedOperationException ("Not supported for project comments.");

                                                                }

                                                                @Override
                                                                public ActionListener getDeleteItemActionHandler (Note                                         item,
                                                                                                                  ChapterItemViewer<ProjectSentReceivedViewer> ep,
                                                                                                                  boolean                                      showAtItem)
                                                                {

                                                                    throw new UnsupportedOperationException ("Not supported for project comments.");

                                                                }

                                                                @Override
                                                                public Set<JComponent> getTools (Note                                         item,
                                                                                                 ChapterItemViewer<ProjectSentReceivedViewer> ep)
                                                                {

                                                                    if (_this.message.isSentByMe ())
                                                                    {

                                                                        return null;

                                                                    }

                                                                    Set<JComponent> buts = new LinkedHashSet ();

                                                                    final JButton but = UIUtils.createButton ((item.isDealtWith () ? Constants.SET_UNDEALT_WITH_ICON_NAME : Constants.SET_DEALT_WITH_ICON_NAME),
                                                                                                              Constants.ICON_MENU,
                                                                                                              getUIString (editors,projectcomments,received,comment,tools, (item.isDealtWith () ? undealtwith : dealtwith),tooltip),
                                                                                                              //"Click to mark the {comment} as %s with",
                                                                                                              //"undealt" : "dealt")),
                                                                                                              null);

                                                                    ActionListener aa = new ActionListener ()
                                                                    {

                                                                        @Override
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {

                                                                            Date d = null;

                                                                            if (!item.isDealtWith ())
                                                                            {

                                                                                d = new Date ();

                                                                            }

                                                                            item.setDealtWith (d);

                                                                            but.setToolTipText (getUIString (editors,projectcomments,received,comment,tools, (item.isDealtWith () ? undealtwith : dealtwith),tooltip));
                                                                            //String.format ("Click to mark the {comment} as %s with",
                                                                            //                                   (item.isDealtWith () ? "undealt" : "dealt")));
                                                                            but.setIcon (Environment.getIcon ((item.isDealtWith () ? Constants.SET_UNDEALT_WITH_ICON_NAME : Constants.SET_DEALT_WITH_ICON_NAME),
                                                                                                              Constants.ICON_MENU));

                                                                            // Inform the sidebar of the change.
                                                                            _this.sideBar.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

                                                                        }

                                                                    };

                                                                    but.addActionListener (aa);

                                                                    buts.add (but);

                                                                    return buts;

                                                                }

                                                            });
*/
/*
TODO Remove/change?
        this.iconProvider = new DefaultIconProvider ()
        {

            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {

                if (name.equals (Note.OBJECT_TYPE))
                {

                    name = Constants.COMMENT_ICON_NAME;

                }

                return super.getIcon (name,
                                      type);

            }

        };
*/

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        this.viewObject (this.project.getBook (0).getChapters ().get (0));

    }

    @Override
    public void close (Runnable afterClose)
    {

        this.project = null;
        super.close (afterClose);

    }

    public E getMessage ()
    {

        return this.message;

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
	{

        return () ->
        {

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (this.getTitleHeaderControl (HeaderControl.contacts));
            controls.add (this.getTitleHeaderControl (HeaderControl.find));
            controls.add (this.getTitleHeaderControl (HeaderControl.close));

            return controls;

        };

	}

    @Override
    public void handleNewProject ()
    {

        throw new UnsupportedOperationException ("Not supported for viewing project comments.");

    }

    @Override
    public StringProperty titleProperty ()
    {

        return UILanguageStringsManager.createStringPropertyWithBinding (() ->
        {

            return getUILanguageStringProperty (Arrays.asList (editors,projectsent,viewertitle),
                                                this.project.getName ()).getValue ();

        },
        this.project.nameProperty ());

    }

    @Override
    public void handleOpenProject ()
    {

        throw new UnsupportedOperationException ("Not supported for viewing sent/received information.");

    }

	@Override
	public void saveProject ()
	{

        // Do nothing.

	}
/*
    public ChapterCommentsPanel getEditorForChapter (Chapter c)
    {

        for (QuollPanel qp : this.getAllQuollPanelsForObject (c))
        {

            if (qp instanceof FullScreenQuollPanel)
            {

                qp = ((FullScreenQuollPanel) qp).getChild ();

            }

            if (qp instanceof ChapterCommentsPanel)
            {

                return (ChapterCommentsPanel) qp;

            }

        }

        return null;

    }
*/
    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter has been opened for editing.
     */
     public void editChapter (final Chapter  c,
                              final Runnable doAfterView)
     {

         NamedObjectPanelContent p = this.getPanelForObject (c);

         if (p != null)
         {

             this.showPanel (p.getPanel ().getPanelId ());

             UIUtils.runLater (doAfterView);

             return;

         }

         try
         {

             p = new ChapterCommentsPanel (this,
                                           c);

             this.addPanel (p);

             this.editChapter (c,
                               doAfterView);

         } catch (Exception e) {

             Environment.logError ("Unable to edit chapter: " +
                                   c,
                                   e);

             ComponentUtils.showErrorMessage (this,
                                              getUILanguageStringProperty (editors,projectsent,actions,viewchapter,actionerror));

         }

     }

    @Override
    public void viewObject (final DataObject d,
                            final Runnable   doAfterView)
    {

        if (d instanceof Note)
        {

            final Note n = (Note) d;

            this.viewObject (n.getChapter (),
                             () ->
                             {

                                 ChapterCommentsPanel pc = (ChapterCommentsPanel) this.getEditorForChapter (n.getChapter ());

                                 if (pc.isReadyForUse ())
                                 {

                                     UIUtils.forceRunLater (() ->
                                     {

                                         pc.showItem (n,
                                                      false);

                                     });

                                } else {

                                    pc.readyForUseProperty ().addListener ((pr, oldv, nev) ->
                                    {

                                        UIUtils.forceRunLater (() ->
                                        {

                                            pc.showItem (n,
                                                         false);

                                        });

                                    });

                                }

                            });

            return;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            this.editChapter (c,
                              doAfterView);

        }

        // Record the error, then ignore.
        Environment.logError ("Unable to open object: " + d);

    }

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

            res.add (new NamedObjectFindResultsBox<Note> (getUILanguageStringProperty (objectnames,plural,Note.OBJECT_TYPE),
                                                          StyleClassNames.COMMENTS,
                                                          this,
                                                          notes));

        }

        return res;

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        final ProjectSentReceivedViewer _this = this;

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

            return items;

        };

    }

    @Override
    public void createActionLogEntry (NamedObject n,
                                      String      m)
    {

        // Do nothing.

    }

}
