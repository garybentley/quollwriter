package com.quollwriter.ui.fx.viewers;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.scene.input.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.text.Rule;
import com.quollwriter.text.Issue;
import com.quollwriter.text.Sentence;
import com.quollwriter.text.Paragraph;
import com.quollwriter.text.TextBlockIterator;
import com.quollwriter.text.TextBlock;
import com.quollwriter.text.SentenceMatches;
import com.quollwriter.text.rules.RuleFactory;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.charts.*;
import com.quollwriter.ui.fx.popups.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ProjectViewer extends AbstractProjectViewer
{

    public static final String VIEWER_STATE_ID = "project.state";

    private ProjectSideBar sidebar = null;
    private SetChangeListener<Tag> tagsListener = null;
    private ProblemFinderRuleConfigPopup problemFinderRuleConfigPopup = null;

    public interface HeaderControlButtonIds extends AbstractProjectViewer.HeaderControlButtonIds
    {

        String ideaboard = "ideaboard";

    }

    public interface CommandId extends AbstractProjectViewer.CommandId
    {

        String editscene = "editscene";
        String deletescene = "deletescene";
        String editoutlineitem = "editoutlineitem";
        String deleteoutlineitem = "deleteoutlineitem";
        String newasset = "newasset";
        String deleteasset = "deleteasset";
        String ideaboard = "ideaboard";
        String newchapter = "newchapter";
        String deletechapter = "deletechapter";
        String renamechapter = "renamechapter";
        String showchapterinfo = "showchapterinfo";
        String exportproject = "exportproject";

    }

    public ProjectViewer ()
    {

        this.sidebar = new ProjectSideBar (this);

        this.initActionMappings ();

        this.setOnDragDropped (ev ->
        {

            try
            {

                Dragboard db = ev.getDragboard ();

                if (db.hasFiles ())
                {

                    if ((db.getFiles () != null)
                        &&
                        (db.getFiles ().size () > 0)
                       )
                    {

                        File f = db.getFiles ().get (0);

                        if (f.getName ().toLowerCase ().endsWith (".docx"))
                        {

                            ImportPopup p = new ImportPopup (this);

                            p.setFilePathToImport (f.toPath ());
                            p.show ();
                            ev.setDropCompleted (true);
                            ev.consume ();

                        }

                    }

                }

            } catch (Exception e) {

                Environment.logError ("Unable to import",
                                      e);

            }

        });

        this.setOnDragOver (ev ->
        {

            Dragboard db = ev.getDragboard ();

            if (db.hasFiles ())
            {

                if ((db.getFiles () != null)
                    &&
                    (db.getFiles ().size () > 0)
                   )
                {

                    File f = db.getFiles ().get (0);

                    if (f.getName ().toLowerCase ().endsWith (".docx"))

                    {

                        ev.acceptTransferModes (TransferMode.MOVE);

                    }

                }

            }

        });

    }

    private void initActionMappings ()
    {

        this.addActionMapping (new CommandWithArgs<Chapter> (objs ->
        {

            Chapter c = null;

            if ((objs != null)
                &&
                (objs.length > 0)
               )
            {

                c = objs[0];

            }

            if (c == null)
            {

                throw new IllegalArgumentException ("No chapter provided.");

            }

            /*
            TODO Needs more thought...
            // See if the chapter is empty.
            if (c.isEmpty ())
            {

                this.deleteObject (c);
                return;

            }
            */

            Chapter _c = c;

            String pid = "chapterdelete" + c.getObjectReference ().asString ();

            if (this.getPopupById (pid) != null)
            {

                return;

            }

            QuollPopup qp = UIUtils.showDeleteObjectPopup (getUILanguageStringProperty (LanguageStrings.project,actions,deletechapter,deletetype),
                                           c.nameProperty (),
                                           StyleClassNames.DELETE,
                                           getUILanguageStringProperty (LanguageStrings.project,actions,deletechapter,warning),
                                           ev ->
                                           {

                                               this.deleteChapter (_c);

                                           },
                                           null,
                                           this);

            qp.setPopupId (pid);

        },
        CommandId.deletechapter));

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

            this.viewObject (o);

        },
        CommandId.viewobject));

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

            if (!(o instanceof NamedObject))
            {

                throw new IllegalArgumentException ("A named object must be provided.");

            }

            try
            {

                this.deleteObject ((NamedObject) o);

            } catch (Exception e) {

                Environment.logError ("Unable to delete object: " + o,
                                      e);

            }

        },
        CommandId.deleteobject));

        this.addActionMapping (new CommandWithArgs (objs ->
        {

            Chapter addBelow = null;

            if ((objs != null)
                &&
                (objs.length > 0)
               )
            {

                addBelow = (Chapter) objs[0];

            }

            this.showAddNewChapter (null,
                                    addBelow);

        },
        CommandId.newchapter));

        this.addActionMapping (() ->
        {

            this.showIdeaBoard ();

        },
        CommandId.ideaboard);

        this.addActionMapping (() ->
        {

            new ImportPopup (this).show ();

        },
        CommandId.importfile);

        this.addActionMapping (() ->
        {

            new ExportProjectPopup (this).show ();

        },
        CommandId.exportproject);

        this.addActionMapping (() ->
        {

            UIUtils.showDeleteProjectPopup (this.project,
                                            null,
                                            this);

        },
        CommandId.deleteproject);

        this.addActionMapping (() ->
        {

            BackupsManager.showCreateBackup (this.project,
                                             this.project.getFilePassword (),
                                             this);

        },
        CommandId.createbackup);

        this.addActionMapping (() ->
        {

            QuollPopup qp = this.getPopupById (RenameProjectPopup.POPUP_ID);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            new RenameProjectPopup (this,
                                    this.getProject ()).show ();

        },
        CommandId.renameproject);

        this.addActionMapping (new CommandWithArgs<NamedObject> (objs ->
        {

            if ((objs == null)
                ||
                (objs.length == 0)
               )
            {

                throw new IllegalArgumentException ("No object provided.");

            }

            NamedObject o = (NamedObject) objs[0];

            // TODO
            if (o instanceof Chapter)
            {

                this.printChapter ((Chapter) o);

            }

        },
        CommandId.print));

        this.addActionMapping (new CommandWithArgs<Chapter> (objs ->
        {

            if ((objs == null)
                ||
                (objs.length == 0)
               )
            {

                throw new IllegalArgumentException ("No chapter provided.");

            }

            Chapter c = (Chapter) objs[0];

            this.renameChapter (c);

        },
        CommandId.renamechapter));

        this.addActionMapping (new CommandWithArgs (objs ->
        {

            if ((objs == null)
                ||
                (objs.length == 0)
               )
            {

                throw new IllegalArgumentException ("No chapter provided.");

            }

            Chapter c = (Chapter) objs[0];

            SideBar sb = this.getSideBarById (ChapterInformationSideBar.getSideBarIdForChapter (c));

            if (sb != null)
            {

                this.showSideBar (sb);

                return;

            }

            ChapterInformationSideBar csb = new ChapterInformationSideBar (this,
                                                                           c);

            this.addSideBar (csb);

            this.showSideBar (csb.getSideBar ());

        },
        CommandId.showchapterinfo));

        this.addActionMapping (new CommandWithArgs (objs ->
        {

            if ((objs == null)
                ||
                (objs.length == 0)
               )
            {

                throw new IllegalArgumentException ("No chapter item provided.");

            }

            ChapterItem ci = (ChapterItem) objs[0];

            this.editChapter (ci.getChapter (),
                              () ->
                              {

                                  this.getEditorForChapter (ci.getChapter ()).editItem (ci);

                              });

        },
        CommandId.editscene,
        CommandId.editoutlineitem));

    }
/*
    public void runCommand (String        id,
                            Runnable      runAfter,
                            DataObject... context)
    {

        Command c = this.getActionMapping (id);

        if (c == null)
        {

            throw new IllegalArgumentException ("Unable to find command with id: " + id);

        }

        if (c instanceof ProjectViewerCommand)
        {

            ProjectViewerCommand pvc = (ProjectViewerCommand) c;

            pvc.run (context);

            return;

        }

        super.<DataObject>runCommand (id,
                          runAfter,
                          context);

    }
*/

    public void createNewScene (Chapter c,
                                int     pos)
    {

        ProjectChapterEditorPanelContent editor = this.getEditorForChapter (c);

        editor.showAddNewScene (pos);

    }

    public void createNewOutlineItem (Chapter c,
                                      int     pos)
    {

        ProjectChapterEditorPanelContent editor = this.getEditorForChapter (c);

        editor.showAddNewOutlineItem (pos);

    }

    public void createNewNote (Chapter c,
                               int     pos)
    {

        ProjectChapterEditorPanelContent editor = this.getEditorForChapter (c);

        editor.showAddNewNote (pos,
                               false);

    }

    public void createNewEditNeededNote (Chapter c,
                                         int     pos)
    {

        ProjectChapterEditorPanelContent editor = this.getEditorForChapter (c);

        editor.showAddNewNote (pos,
                               true);

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        // We do this last because the sidebars will be restored by the super.
        super.init (s);

        this.setMainSideBar (this.sidebar);

        this.addSetChangeListener (Environment.getAllTags (),
                                   ev ->
        {

            Tag t = ev.getElementRemoved ();

            if (t != null)
            {

                this.removeTag (t);

            }

        });

        this.addMapChangeListener (this.getProject ().getAssets (),
                                   ch ->
        {

            if (ch.wasRemoved ())
            {

                this.deleteAllAssetsOfType (ch.getKey ());

            }

        });

        this.getWindowedContent ().getHeader ().getControls ().setVisibleItems (UserProperties.projectViewerHeaderControlButtonIds ());

        this.getWindowedContent ().getHeader ().getControls ().setOnConfigurePopupClosed (ev ->
        {

            UserProperties.setProjectViewerHeaderControlButtonIds (this.getWindowedContent ().getHeader ().getControls ().getVisibleItemIds ());

        });

        this.getBinder ().addSetChangeListener (UserProperties.projectViewerHeaderControlButtonIds (),
                                                ev ->
        {

            this.getWindowedContent ().getHeader ().getControls ().setVisibleItems (UserProperties.projectViewerHeaderControlButtonIds ());

        });

        this.addKeyMapping (CommandId.newchapter,
                            KeyCode.H, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

    }

    @Override
    public void openPanelForId (String id)
                         throws GeneralException
    {

        super.openPanelForId (id);

        if (id.equals (IdeaBoard.PANEL_ID))
        {

            this.showIdeaBoard ();
            return;

        }

    }

    @Override
    public void handleNewProject ()
                           throws Exception
    {

        Book b = this.project.getBooks ().get (0);

        Chapter c = b.getFirstChapter ();

        // Create a new chapter for the book.
        if (c == null)
        {

            c = new Chapter (b,
                             Environment.getDefaultChapterName ());

            b.addChapter (c);

        }

        this.saveObject (c,
                         true);

        // Refresh the chapter tree.
        // TODO Needed? this.reloadTreeForObjectType (c.getObjectType ());

        this.handleOpenProject ();

        this.editChapter (c);

    }

    @Override
    public void handleOpenProject ()
    {

        //this.initProjectItemBoxes ();

		final ProjectViewer _this = this;

		// Called whenever a note type is changed.
        /*
         TODO
		this.noteTypePropChangedListener = new PropertyChangedListener ()
		{

			@Override
			public void propertyChanged (PropertyChangedEvent ev)
			{

				if (ev.getChangeType ().equals (UserPropertyHandler.VALUE_CHANGED))
				{

					java.util.List<Note> toSave = new ArrayList ();

					Set<Note> objs = _this.getAllNotes ();

					for (Note o : objs)
					{

						if (o.getType ().equals ((String) ev.getOldValue ()))
						{

							o.setType ((String) ev.getNewValue ());

							toSave.add (o);

						}

						if (toSave.size () > 0)
						{

							try
							{

								_this.saveObjects (toSave,
												   true);

							} catch (Exception e)
							{

								Environment.logError ("Unable to save notes: " +
													  toSave +
													  " with new type: " +
													  ev.getNewValue (),
													  e);
// TODO: Language string
								UIUtils.showErrorMessage (_this,
														  "Unable to change type");

							}

						}

					}

					_this.reloadTreeForObjectType (Note.OBJECT_TYPE);

				}

			}

		};
*/
		// TODO Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).addPropertyChangedListener (this.noteTypePropChangedListener);

		// TODO this.scheduleUpdateAppearsInChaptersTree ();

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        final ProjectViewer _this = this;

        return new Supplier<> ()
        {

            @Override
            public Set<MenuItem> get ()
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                java.util.List<String> prefix = Arrays.asList (LanguageStrings.project,settingsmenu,LanguageStrings.items);

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,openproject))
                    .iconName (StyleClassNames.OPEN)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.openproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,newproject))
                    .iconName (StyleClassNames.NEW)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.newproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,renameproject))
                    .iconName (StyleClassNames.RENAME)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.renameproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,statistics))
                    .iconName (StyleClassNames.STATISTICS)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.statistics);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,targets))
                    .iconName (StyleClassNames.TARGETS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.targets);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,createbackup))
                    .iconName (StyleClassNames.CREATEBACKUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.createbackup);

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

                        _this.runCommand (CommandId.deleteproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,ideaboard))
                    .iconName (StyleClassNames.IDEABOARD)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.ideaboard);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,dowarmup))
                    .iconName (StyleClassNames.WARMUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.warmup);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,importfileorproject))
                    .iconName (StyleClassNames.IMPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.importfile);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,exportproject))
                    .iconName (StyleClassNames.EXPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.exportproject);

                    })
                    .build ());

                return items;

            }

        };

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        Set<Node> pcons = super.getTitleHeaderControlsSupplier ().get ();

        return () ->
        {

            List<String> prefix = Arrays.asList (LanguageStrings.project, LanguageStrings.title,toolbar,buttons);

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (QuollButton.builder ()
                .tooltip (prefix,ideaboard,tooltip)
                .iconName (StyleClassNames.IDEABOARD)
                .buttonId (HeaderControlButtonIds.ideaboard)
                .onAction (ev ->
                {

                    this.runCommand (CommandId.ideaboard);

                })
                .build ());

            controls.addAll (pcons);

            return controls;

        };

    }

    public void viewObject (DataObject d)
    {

        if (d == null)
        {

            return;

        }

        this.viewObject (d,
                         null);

    }

    public void editObject (DataObject d)
    {

        if (d instanceof Chapter)
        {

            this.editChapter ((Chapter) d);
            return;

        }

        if (d instanceof Asset)
        {

            Asset a = (Asset) d;

            this.viewAsset (a,
                            () ->
            {

                AssetViewPanel avp = this.getAssetViewPanel (a);
                avp.showEdit ();

            });

        }

        if (d instanceof ChapterItem)
        {

            ChapterItem ci = (ChapterItem) d;

            this.editChapter (ci.getChapter (),
                              () ->
            {

                ProjectChapterEditorPanelContent editor = this.getEditorForChapter (ci.getChapter ());
                editor.editItem (ci);

            });

        }

    }

    public void renameChapter (Chapter c)
    {

        QuollPopup qp = this.getPopupById (RenameChapterPopup.getPopupIdForChapter (c));

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        new RenameChapterPopup (this,
                                c).show ();

    }

    public AssetViewPanel getAssetViewPanel (Asset a)
    {

        NamedObjectPanelContent p = this.getPanelForObject (a);

        if (p instanceof AssetViewPanel)
        {

            return (AssetViewPanel) p;

        }

        return null;

    }

    @Override
    public ProjectChapterEditorPanelContent getEditorForChapter (Chapter c)
    {

        NamedObjectPanelContent p = this.getPanelForObject (c);

        if (p instanceof ProjectChapterEditorPanelContent)
        {

            return (ProjectChapterEditorPanelContent) p;

        }

        return null;

    }

    public void viewObject (final DataObject d,
                            final Runnable   doAfterView)
    {

        final ProjectViewer _this = this;

        if (d instanceof ChapterItem)
        {

            final ChapterItem ci = (ChapterItem) d;

            this.viewObject (ci.getChapter (),
                             () ->
                             {

                                 ProjectChapterEditorPanelContent pc = _this.getEditorForChapter (ci.getChapter ());

                                 if (pc.isReadyForUse ())
                                 {

                                     UIUtils.forceRunLater (() ->
                                     {

                                         pc.showItem (ci,
                                                      false);

                                         UIUtils.runLater (doAfterView);

                                    });

                                } else {

                                    pc.readyForUseProperty ().addListener ((pr, oldv, nev) ->
                                    {

                                        UIUtils.forceRunLater (() ->
                                        {

                                            pc.showItem (ci,
                                                         false);

                                            UIUtils.runLater (doAfterView);

                                        });

                                    });

                                }

                            });

            return;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            if (d.getObjectType ().equals (Chapter.INFORMATION_OBJECT_TYPE))
            {

                try
                {

                    this.viewChapterInformation (c,
                                                 doAfterView);

                } catch (Exception e) {

                    Environment.logError ("Unable to view chapter information for chapter: " +
                                          c,
                                          e);

                    ComponentUtils.showErrorMessage (_this,
                                                     getUILanguageStringProperty (LanguageStrings.project,actions,viewchapterinformation,actionerror));
                                              //"Unable to show chapter information.");

                }

            } else
            {

                this.editChapter (c,
                                  doAfterView);

            }

            return;

        }

        if (d instanceof Asset)
        {

            this.viewAsset ((Asset) d,
                            doAfterView);

        }
/*
        if (d instanceof Note)
        {

            this.viewNote ((Note) d);

            return true;

        }
        */
/*
        if (d instanceof OutlineItem)
        {

            this.viewOutlineItem ((OutlineItem) d);

            return true;

        }
*/
        // Record the error, then ignore.
        // TODO throw new GeneralException ("Unable to open object");

    }

    public void editChapter (final Chapter  c)
    {

        this.editChapter (c,
                          null);

    }

    public void editChapter (final Chapter  c,
                             final Runnable doAfterView)
    {

        String pid = ProjectChapterEditorPanelContent.getPanelIdForChapter (c);

        if (this.showPanel (pid))
        {

            UIUtils.runLater (doAfterView);

            return;

        }

        try
        {

            ProjectChapterEditorPanelContent p = new ProjectChapterEditorPanelContent (this,
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

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter information is viewed.
     */
    public void viewChapterInformation (final Chapter c,
                                        final Runnable doAfterView)
                                 throws GeneralException
    {

        String sbid = ChapterInformationSideBar.getSideBarIdForChapter (c);
        SideBar sb = this.getSideBarById (sbid);

        if (sb == null)
        {

            ChapterInformationSideBar cb = new ChapterInformationSideBar (this,
                                                                          c);

            this.addSideBar (cb);

        }

        this.showSideBar (sbid,
                          doAfterView);

    }

    public void viewAsset (final Asset    a,
                           final Runnable doAfterView)
    {

        NamedObjectPanelContent p = this.getPanelForObject (a);

        if (p != null)
        {

            this.showPanel (p.getPanelId ());

            if (doAfterView != null)
            {

                UIUtils.runLater (doAfterView);

            }

            return;

        }

        final ProjectViewer _this = this;

        AssetViewPanel avp = null;

        try
        {

            avp = new AssetViewPanel (this,
                                      a);

            this.addPanel (avp);

            if (doAfterView != null)
            {

                if (avp.isReadyForUse ())
                {

                    UIUtils.runLater (doAfterView);

                } else {

                    // We add the listener to the panel itself so its deregistered when the panel is removed.
                    avp.getBinder ().addChangeListener (avp.readyForUseProperty (),
                                                        (pv, oldv, newv) -> UIUtils.runLater (doAfterView));

                }

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to view asset: " +
                                  a,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (assets,view,actionerror),
                                                                          a.getObjectTypeName (),
                                                                          a.getName ()));

            return;

        }

        // Open the tab :)
        this.viewAsset (a,
                        null);

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.PROJECT;

    }

    public void addChapterToTreeAfterX (Chapter newChapter,
                                       Chapter addAfter)
    {
/*
TODO
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
*/
    }

    public void openObjectSectionX (Asset a)
    {

        // TODO this.sideBar.setObjectsOpen (a.getUserConfigurableObjectType ().getObjectTypeId ());

    }

    public void openObjectSectionX (String objType)
    {

        // TODO this.sideBar.setObjectsOpen (objType);

    }

    public void showIdeaBoard ()
    {

        if (this.showPanel (IdeaBoard.PANEL_ID))
        {

            return;

        }

        try
        {

            IdeaBoard i = new IdeaBoard (this);

            this.addPanel (i);
            this.showPanel (i.getPanelId ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to view idea board",
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (ideaboard,actionerror));
                                      //"Unable to view idea board");

        }

    }

    /**
     * Remove the specified tag from all objects in this project.
     *
     * @param tag The tag.
     */
    public void removeTag (Tag tag)
    {

        try
        {

            // Get all objects with the tag, remove the tag.
            Set<NamedObject> objs = new HashSet<> (this.project.getAllObjectsWithTag (tag));

            for (NamedObject o : objs)
            {

                o.removeTag (tag);

            }

            this.saveObjects (new ArrayList (objs),
                              true);

            this.sidebar.removeTagSection (tag);

        } catch (Exception e) {

            Environment.logError ("Unable to remove tag: " +
                                  tag,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.project,actions,removetag,actionerror));
                                      //"Unable to remove tag.");

        }

    }

    public void removeTagFromObject (NamedObject n,
                                     Tag         t)
                              throws GeneralException
    {

        n.removeTag (t);

        this.saveObject (n,
                         true);

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
                       throws GeneralException
    {

        if (o instanceof Asset)
        {

            this.deleteAsset ((Asset) o);

        }

        if (o instanceof Chapter)
        {

            this.deleteChapter ((Chapter) o);

        }

        if (o instanceof ChapterItem)
        {

            this.deleteChapterItem ((ChapterItem) o,
                                    true,
                                    true);

        }

    }

    public void showDeleteAsset (Asset a)
    {

        this.showDeleteAsset (a,
                              null);

    }

    public void showDeleteAsset (Asset a,
                                 Node  showAt)
    {

        String pid = a.getObjectReference ().asString () + "deleteasset";

        if (this.getPopupById (pid) != null)
        {

            return;

        }

        QuollPopup.questionBuilder ()
            .withViewer (this)
            .popupId (pid)
            .styleClassName (StyleClassNames.DELETE)
            .title (getUILanguageStringProperty (Arrays.asList (assets,delete,confirmpopup,title),
                                                 a.getUserConfigurableObjectType ().nameProperty ()))
            .message (getUILanguageStringProperty (Arrays.asList (assets,delete,confirmpopup,text),
                                                       a.getUserConfigurableObjectType ().nameProperty (),
                                                       a.nameProperty ()))
            .confirmButtonLabel (assets,delete,confirmpopup,buttons,confirm)
            .cancelButtonLabel (assets,delete,confirmpopup,buttons,cancel)
            .onConfirm (ev ->
            {

                this.deleteAsset (a);
                this.getPopupById (pid).close ();

            })
            .onCancel (ev ->
            {

                this.getPopupById (pid).close ();

            })
            .build ()
            .show (showAt,
                   Side.BOTTOM);

    }

    public void showDeleteChapterItemPopup (ChapterItem item,
                                            Node        showAt)
    {

        String pid = "delete" + item.getObjectReference ().asString ();

        QuollPopup qp = this.getPopupById (pid);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        qp = QuollPopup.questionBuilder ()
            .popupId (pid)
            .title (getUILanguageStringProperty (Arrays.asList (chapteritems,delete,title),
                                                 Environment.getObjectTypeName (item)))
            .message (getUILanguageStringProperty (Arrays.asList (chapteritems,delete,text),
                                                   Environment.getObjectTypeName (item),
                                                   item.getSummary ()))
            .confirmButtonLabel (chapteritems,delete,buttons,confirm)
            .cancelButtonLabel (chapteritems,delete,buttons,cancel)
            .onConfirm (ev ->
            {

                // If the item is a scene AND has outline items ask if they want to keep them.
                if (item instanceof Scene)
                {

                    Scene s = (Scene) item;

                    if (s.getOutlineItems ().size () > 0)
                    {

                        String pid2 = "deleteoutlineitems" + item.getObjectReference ().asString ();

                        QuollPopup qp2 = QuollPopup.questionBuilder ()
                            .popupId (pid2)
                            .title (getUILanguageStringProperty (Arrays.asList (chapteritems,deletesceneoutlineitems,title),
                                                                 Environment.formatNumber (s.getOutlineItems ().size ())))
                            .message (getUILanguageStringProperty (Arrays.asList (chapteritems,deletesceneoutlineitems,text),
                                                                   Environment.formatNumber (s.getOutlineItems ().size ())))
                            .confirmButtonLabel (chapteritems,deletesceneoutlineitems,buttons,confirm)
                            .cancelButtonLabel (chapteritems,deletesceneoutlineitems,buttons,cancel)
                            .onConfirm (ev2 ->
                            {

                                try
                                {

                                    this.deleteChapterItem (item,
                                                            true,
                                                            true);

                                } catch (Exception e) {

                                    ComponentUtils.showErrorMessage (this,
                                                                     getUILanguageStringProperty (chapteritems,deletesceneoutlineitems,actionerror));

                                    Environment.logError ("Unable to delete: " + item,
                                                          e);

                                }

                                this.getPopupById (pid2).close ();

                            })
                            .onCancel (ev2 ->
                            {

                                try
                                {

                                    this.deleteChapterItem (item,
                                                            false,
                                                            true);

                                } catch (Exception e) {

                                    ComponentUtils.showErrorMessage (this,
                                                                     getUILanguageStringProperty (chapteritems,deletesceneoutlineitems,actionerror));

                                    Environment.logError ("Unable to delete: " + item,
                                                          e);

                                }

                                //this.getPopupById (pid2).close ();

                            })
                            .withViewer (this)
                            .styleClassName (StyleClassNames.DELETE)
                            .build ();

                        this.showPopup (qp2,
                                        showAt,
                                        Side.BOTTOM);

                        this.getPopupById (pid).close ();

                        return;

                    }

                }

                try
                {

                    this.deleteChapterItem (item,
                                            false,
                                            true);

                } catch (Exception e) {

                    ComponentUtils.showErrorMessage (this,
                                                     getUILanguageStringProperty (chapteritems,delete,actionerror));

                    Environment.logError ("Unable to delete: " + item,
                                          e);

                }

                this.getPopupById (pid).close ();

            })
            .onCancel (ev -> {})
            .withViewer (this)
            .styleClassName (StyleClassNames.DELETE)
            .build ();

        this.showPopup (qp,
                        showAt,
                        Side.BOTTOM);

    }

    public void deleteChapterItem (ChapterItem ci,
                                   boolean     deleteChildObjects,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        if (ci.getObjectType ().equals (Scene.OBJECT_TYPE))
        {

            this.deleteScene ((Scene) ci,
                              deleteChildObjects,
                              doInTransaction);

        }

        if (ci.getObjectType ().equals (OutlineItem.OBJECT_TYPE))
        {

            this.deleteOutlineItem ((OutlineItem) ci,
                                    doInTransaction);

        }

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
        // TODO Remove this.setLinks (n);

        // Remove the tags.
        this.project.removeObject (n);

        this.dBMan.deleteObject (n,
                                 false,
                                 null);

        obj.removeNote (n);

        this.fireProjectEvent (ProjectEvent.Type.note,
                               ProjectEvent.Action.delete,
                               n);

        // TODO this.refreshObjectPanels (otherObjects);

        if (obj instanceof Chapter)
        {

            ProjectChapterEditorPanelContent qep = this.getEditorForChapter ((Chapter) obj);

            if (qep != null)
            {

                // TODO qep.removeItem (n);

            }

        }

        // TODO this.reloadNoteTree ();

        // TODO this.reloadChapterTree ();

    }

    public void deleteScene (Scene   s,
                             boolean deleteOutlineItems,
                             boolean doInTransaction)
                      throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = s.getOtherObjectsInLinks ();

        java.util.List<OutlineItem> outlineItems = new ArrayList<> (s.getOutlineItems ());

        // Get the editor panel for the item.
        Chapter c = s.getChapter ();

        // Remove the tags.
        this.project.removeObject (s);

        this.dBMan.deleteObject (s,
                                 deleteOutlineItems,
                                 null);

        c.removeScene (s);

        this.fireProjectEvent (ProjectEvent.Type.scene,
                               ProjectEvent.Action.delete,
                               s);

        // TODO this.refreshObjectPanels (otherObjects);

        ProjectChapterEditorPanelContent qep = this.getEditorForChapter (c);

        if (qep != null)
        {

            for (OutlineItem oi : outlineItems)
            {

                if (deleteOutlineItems)
                {

                    // TODO qep.removeItem (oi);

                } else {

                    // Add the item back into the chapter.
                    c.addChapterItem (oi);

                }

            }

            // TODO qep.removeItem (s);

        }

        // TODO ? this.reloadChapterTree ();

    }

    public void deleteOutlineItem (OutlineItem it,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = it.getOtherObjectsInLinks ();

        // Get the editor panel for the item.
        Chapter c = it.getChapter ();

        // Remove the tags.
        this.project.removeObject (it);

        this.dBMan.deleteObject (it,
                                 false,
                                 null);

        c.removeOutlineItem (it);

        this.fireProjectEvent (ProjectEvent.Type.outlineitem,
                               ProjectEvent.Action.delete,
                               it);

        if (it.getScene () != null)
        {

            it.getScene ().removeOutlineItem (it);

        }

        // TODO this.refreshObjectPanels (otherObjects);

        ProjectChapterEditorPanelContent qep = this.getEditorForChapter (c);

        if (qep != null)
        {

            // TODO qep.removeItem (it);

        }

        // TODO ? this.reloadChapterTree ();

    }

    @Override
    public void deleteAllObjectsForType (UserConfigurableObjectType type)
    {

        this.deleteAllAssetsOfType (type);

    }

    public void deleteAllAssetsOfType (UserConfigurableObjectType type)
    {

        // Due to the async nature of removing types it may be that the type has already been removed
        // from the project but there may be tabs open for assets of the type.
        for (Panel p : this.getPanels ().values ())
        {

            if (p.getContent () instanceof AssetViewPanel)
            {

                AssetViewPanel avp = (AssetViewPanel) p.getContent ();

                if (avp.getObject ().getUserConfigurableObjectType ().equals (type))
                {

                    this.removePanel (p,
                                      null);

                }

            }

        }

        Set<Asset> assets = this.project.getAssets (type);

        if (assets == null)
        {

            return;

        }

        Set<Asset> nassets = new LinkedHashSet<> (assets);

        for (Asset a : nassets)
        {

            this.deleteAsset (a);

        }

    }

    public void deleteAsset (Asset a)
    {

        // Remove the links.
        try
        {

            // Capture a list of all the object objects in the links, we then need to message
            // the linked to panel of any of those.
            Set<NamedObject> otherObjects = a.getOtherObjectsInLinks ();

            this.dBMan.deleteObject (a,
                                     false,
                                     null);

            this.project.removeObject (a);

            this.removeWordFromDictionary (a.getName ());
                                           //"project");
            //this.removeWordFromDictionary (a.getName () + "'s",
            //                               "project");

            // TODO this.refreshObjectPanels (otherObjects);

            for (NamedObject n : otherObjects)
            {

                n.removeLinkFor (a);

            }

            this.fireProjectEvent (ProjectEvent.Type.asset,
                                   ProjectEvent.Action.delete,
                                   a);

        } catch (Exception e)
        {

            Environment.logError ("Unable to remove asset: " +
                                  a,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (assets,delete,actionerror),
                                                                          a.getObjectTypeName (),
                                                                          a.getName ()));
                                      //"Unable to remove " + Environment.getObjectTypeName (a));

            return;

        }

        // TODO this.reloadTreeForObjectType (a.getObjectType ());

		this.removeAllSideBarsForObject (a);

        this.removePanel (a);

    }

    /**
     * Actually delete a chapter from the project.
     * To get user interfaction for the delete call runCommand(CommandId.deletechapter)
     *
     * @param c The chapter.
     */
    public void deleteChapter (Chapter c)
    {

        try
        {

            // Remove the chapter from the book.
            java.util.Set<NamedObject> otherObjects = c.getOtherObjectsInLinks ();

            this.dBMan.deleteObject (c,
                                     false,
                                     null);

            Book b = c.getBook ();

            b.removeChapter (c);

            // See if there is a chapter information sidebar.
            this.removeSideBar (ChapterInformationSideBar.getSideBarIdForChapter (c));

            this.fireProjectEvent (ProjectEvent.Type.chapter,
                                   ProjectEvent.Action.delete,
                                   c);

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete chapter: " + c,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,deletechapter,actionerror),
                                                                          c.getName ()));
                                      //"Unable to delete " +
                                      //Environment.getObjectTypeName (c));

            return;

        }

		this.removeAllSideBarsForObject (c);

        // Remove the tab (if present).
        this.removeAllPanelsForObject (c);

        // Notify the note tree about the change.
        // We get a copy of the notes here to allow iteration.
        Set<Note> _notes = new LinkedHashSet (c.getNotes ());
        for (Note n : _notes)
        {

            try
            {

                this.deleteNote (n,
                                 false);

            } catch (Exception e)
            {

                Environment.logError ("Unable to delete note: " + n,
                                      e);

            }

        }

    }

    public void setChapterEditComplete (Chapter chapter,
                                        boolean editComplete)
    {

        try
        {

            chapter.setEditComplete (editComplete);

            ProjectChapterEditorPanelContent p = this.getEditorForChapter (chapter);

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

            if (p != null)
            {

                p.recreateVisibleParagraphs ();

            }

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

        final ProjectViewer _this = this;

        ProjectChapterEditorPanelContent p = this.getEditorForChapter (chapter);

        UIUtils.runLater (() ->
        {

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
                    //Rectangle2D pp = p.getEditor ().modelToView2D (_textPos);

                } else {

                    String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

                    l = Utils.stripEnd (t).length ();

                }

                _textPos = Math.min (_textPos, l);

                if (_textPos <= l)
                {

                    chapter.setEditComplete (UserProperties.getAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME));

                } else {

                    chapter.setEditComplete (false);

                }

                chapter.setEditPosition (_textPos);

                this.saveObject (chapter,
                                 false);

            } catch (Exception e) {

                Environment.logError ("Unable to set edit position for: " + chapter,
                                      e);

                ComponentUtils.showErrorMessage (_this,
                                                 getUILanguageStringProperty (LanguageStrings.project,editorpanel,actions,seteditposition,actionerror));

                return;

            }

        });

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

    public void showAddNewChapter (Chapter newChapter,
                                   Chapter addBelow)
    {

        new NewChapterPopup (newChapter,
                             this,
                             addBelow,
                             null).show ();

    }

    public void showAddNewChapterBelow (Chapter addBelow)
    {

        this.showAddNewChapter (null,
                                addBelow);

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        Set<String> headerIds = this.getWindowedContent ().getHeader ().getControls ().getVisibleItemIds ();

        String ids = headerIds.stream ()
            .collect (Collectors.joining (","));

        s.set ("headerControlButtonIds",
               ids);

        return s;

    }

    public void showAddNewAsset (UserConfigurableObjectType forAssetType)
    {

        Asset asset = new Asset (forAssetType);

        this.showAddNewAsset (asset);

    }

    public void showAddNewAssetInTab (Asset asset)
    {

        try
        {

            NewAssetPanel avp = new NewAssetPanel (this,
                                                   asset);

            this.addPanel (avp);

            this.showPanel (avp.getPanelId ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to view asset: " +
                                  asset,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                          asset.getObjectTypeName ()));

            return;

        }

    }

    public void showAddNewAsset (Asset asset)
    {

        String addAsset = UserProperties.get (Constants.ADD_ASSETS_PROPERTY_NAME);

        if (((addAsset.equals (Constants.ADD_ASSETS_TRY_POPUP))
             &&
             (asset.getUserConfigurableObjectType ().getNonCoreFieldCount () == 0)
            )
            ||
            (addAsset.equals (Constants.ADD_ASSETS_POPUP))
           )
        {

            new NewAssetPopup (asset,
                               this).show ();
            return;

        }

        this.showAddNewAssetInTab (asset);

    }

    public ProblemFinderRuleConfigPopup getProblemFinderRuleConfig ()
    {

        if (this.problemFinderRuleConfigPopup == null)
        {

            this.problemFinderRuleConfigPopup = new ProblemFinderRuleConfigPopup (this);

        }

        return this.problemFinderRuleConfigPopup;

    }

    public void showProblemFinderRuleConfig ()
    {

        this.showProblemFinderRuleConfig (null);

    }

    public void showProblemFinderRuleConfig (Consumer<ProblemFinderRuleConfigPopup> onShow)
    {

        this.showPopup (this.getProblemFinderRuleConfig ().getPopup ());

        if (onShow != null)
        {

            UIUtils.runLater (() ->
            {

                onShow.accept (this.getProblemFinderRuleConfig ());

            });

        }

        this.fireProjectEvent (ProjectEvent.Type.problemfinderruleconfig,
                               ProjectEvent.Action.show);

    }

    public void showProblemFinderRuleSideBar (Rule    r)
    {

        // Switch off the problem finder for all chapters.
        this.getPanels ().values ().stream ()
            .forEach (p ->
            {

                if (p.getContent () instanceof ProjectChapterEditorPanelContent)
                {

                    ProjectChapterEditorPanelContent pp = (ProjectChapterEditorPanelContent) p.getContent ();

                    pp.closeProblemFinder ();

                }

            });

        String id = ProblemFinderSideBar.getSideBarId (r);

        SideBar sb = this.getSideBarById (id);

        if (sb == null)
        {

            ProblemFinderSideBar psb = new ProblemFinderSideBar (this,
                                                                 r);
            this.addSideBar (psb);

            sb = psb.getSideBar ();

        }

        this.showSideBar (sb);

    }

    public void saveProblemFinderIgnores (Chapter    c)
                                   throws GeneralException
    {

        ChapterDataHandler dh = (ChapterDataHandler) this.getDataHandler (Chapter.class);

        dh.saveProblemFinderIgnores (c,
                                     null);

    }

    public Set<Issue> getProblemFinderIgnores (Rule r)
                                        throws GeneralException
    {

        Set<Issue> ignores = new HashSet ();

        for (Chapter c : this.getProject ().getBook (0).getChapters ())
        {

            Set<Issue> ignored = c.getProblemFinderIgnores ();

            for (Issue i : ignored)
            {

                if (i.getRuleId ().equals (r.getId ()))
                {

                    ignores.add (i);

                }

            }

        }

        return ignores;

    }

    public Map<Chapter, Set<Issue>> getProblemsForAllChapters ()
    {

        return this.getProblemsForAllChapters (null);

    }

    public Map<Chapter, Set<Issue>> getProblemsForAllChapters (Rule limitToRule)
    {

        Map<Chapter, Set<Issue>> probs = new LinkedHashMap ();

        if (this.getProject () == null)
        {

            // Closing down.
            return probs;

        }

        for (Book book : this.getProject ().getBooks ())
        {

            for (Chapter c : book.getChapters ())
            {

                Set<Issue> issues = null;

                if (limitToRule != null)
                {

                    issues = this.getProblems (c,
                                               limitToRule);

                } else {

                    issues = this.getProblems (c);

                }

                if (issues.size () > 0)
                {

                    probs.put (c, issues);

                }

            }

        }

        return probs;

    }

    public Set<Issue> getProblems (Chapter c,
                                   Rule    r)
    {

        Set<Issue> ret = new LinkedHashSet<> ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            TextBlockIterator ti = new TextBlockIterator (ct);

            TextBlock b = null;

            while ((b = ti.next ()) != null)
            {

                List<Issue> issues = RuleFactory.getIssues (b,
                                                            r,
                                                            this.project.getProperties ());

                for (Issue i : issues)
                {

                    ret.add (i);

                    i.setChapter (c);

                }

            }

        }

        return ret;

    }

    public Set<Issue> getProblems (Chapter c)
    {

        Set<Issue> ret = new LinkedHashSet ();

        String ct = this.getCurrentChapterText (c);

        if (ct != null)
        {

            TextBlockIterator ti = new TextBlockIterator (ct);

            TextBlock b = null;

            while ((b = ti.next ()) != null)
            {

                if (b instanceof Paragraph)
                {

                    ret.addAll (RuleFactory.getParagraphIssues ((Paragraph) b,
                                                                this.project.getProperties ()));

                }

                if (b instanceof Sentence)
                {

                    ret.addAll (RuleFactory.getSentenceIssues ((Sentence) b,
                                                                this.project.getProperties ()));

                }

            }

        }

        return ret;

    }

    public void openObjectSection (Asset a)
    {

        // TODO

    }

    public void openObjectSection (String objType)
    {

        // TODO

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

        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType type : types)
        {

            Set<Asset> objs = this.project.getAssetsContaining (t,
                                                                type);

            if (objs.size () > 0)
            {

                res.add (new AssetFindResultsBox (type,
                                                  this,
                                                  objs));

            }

        }

        Set<Note> notes = this.project.getNotesContaining (t);

        if (notes.size () > 0)
        {

            res.add (new NamedObjectFindResultsBox (getUILanguageStringProperty (objectnames,plural,Note.OBJECT_TYPE),
                                                    Note.OBJECT_TYPE,
                                                    this,
                                                    notes));

        }

        Set<Scene> scenes = this.project.getScenesContaining (t);

        if (scenes.size () > 0)
        {

            res.add (new NamedObjectFindResultsBox (getUILanguageStringProperty (objectnames,plural,Scene.OBJECT_TYPE),
                                                    Scene.OBJECT_TYPE,
                                                    this,
                                                    scenes));

        }

        Set<OutlineItem> oitems = this.project.getOutlineItemsContaining (t);

        if (oitems.size () > 0)
        {

            res.add (new NamedObjectFindResultsBox (getUILanguageStringProperty (objectnames,plural,OutlineItem.OBJECT_TYPE),
                                                    OutlineItem.OBJECT_TYPE,
                                                    this,
                                                    oitems));

        }

        return res;

    }

    public void addNewIdeaType (IdeaType it)
                         throws GeneralException
    {

        this.dBMan.saveObject (it,
                               null);

        this.project.addIdeaType (it);

        this.fireProjectEvent (ProjectEvent.Type.ideatype,
                               ProjectEvent.Action._new,
                               it);

    }

    public void addNewIdea (Idea i)
                     throws GeneralException
    {

        this.dBMan.saveObject (i,
                               null);

        i.getType ().addIdea (i);

        this.fireProjectEvent (ProjectEvent.Type.idea,
                               ProjectEvent.Action._new,
                               i);

    }

    public void updateIdeaType (IdeaType it)
                         throws GeneralException
    {

        this.dBMan.saveObject (it,
                               null);

        this.fireProjectEvent (ProjectEvent.Type.ideatype,
                               ProjectEvent.Action.edit,
                               it);

    }

    public void deleteIdeaType (IdeaType it)
                    throws GeneralException
    {

        try
        {

            this.dBMan.deleteObject (it,
                                     false,
                                     null);

            this.project.removeIdeaType (it);

            this.fireProjectEvent (ProjectEvent.Type.ideatype,
                                   ProjectEvent.Action.delete,
                                   it);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to delete idea type: " + it,
                                        e);

        }

    }

    public void updateIdea (Idea i)
                     throws GeneralException
    {

        try
        {

            this.dBMan.saveObject (i);

            this.fireProjectEvent (ProjectEvent.Type.idea,
                                   ProjectEvent.Action.changed,
                                   i);

        } catch (Exception e) {

            throw new GeneralException ("Unable to update idea: " + i,
                                        e);

        }

    }

    public void deleteIdea (Idea i)
                     throws GeneralException
    {

        try
        {

            this.dBMan.deleteObject (i,
                                     false,
                                     null);

            i.getType ().removeIdea (i);

            this.fireProjectEvent (ProjectEvent.Type.idea,
                                   ProjectEvent.Action.delete,
                                   i);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to delete idea: " + i,
                                        e);

        }

    }

    public void showAppearsInChaptersSideBarForAsset (Asset asset)
    {

        SideBar osb = this.getSideBarById (AppearsInChaptersSideBar.getSideBarIdForObject (asset));

        if (osb != null)
        {

            this.showSideBar (osb);

            return;

        }

        AppearsInChaptersSideBar sb = new AppearsInChaptersSideBar (this,
                                                                    asset);

        this.addSideBar (sb);

        this.showSideBar (sb.getSideBar ());

    }

}
