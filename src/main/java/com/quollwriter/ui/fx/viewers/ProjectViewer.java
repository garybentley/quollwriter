package com.quollwriter.ui.fx.viewers;

import java.awt.geom.Rectangle2D;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.control.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.charts.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.swing.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ProjectViewer extends AbstractProjectViewer
{

    public static final String VIEWER_STATE_ID = "project.state";

    private ProjectSideBar sidebar = null;
    private SetChangeListener<Tag> tagsListener = null;

    public interface CommandId extends AbstractProjectViewer.CommandId
    {

        String editscene = "editscene";
        String deletescene = "deletescene";
        String newoutlineitem = "newoutlineitem";
        String editoutlineitem = "editoutlineitem";
        String deleteoutlineitem = "deleteoutlineitem";
        String newasset = "newasset";
        String editasset = "editasset";
        String deleteasset = "deleteasset";
        String ideaboard = "ideaboard";
        String togglespellchecking = "togglespellchecking";
        String newchapter = "newchapter";
        String deletechapter = "deletechapter";
        String renamechapter = "renamechapter";
        String showchapterinfo = "showchapterinfo";
        String newnote = "newnote";
        String neweditneedednote = "neweditneedednote";
        String createbackup = "createbackup";
        String exportproject = "exportproject";
        String deleteproject = "deleteproject";
        String renameproject = "renameproject";

    }

    public ProjectViewer ()
    {

        this.sidebar = new ProjectSideBar (this);

        this.initActionMappings ();

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

            UIUtils.showDeleteObjectPopup (getUILanguageStringProperty (LanguageStrings.project,actions,deletechapter,deletetype),
                                           c.nameProperty (),
                                           StyleClassNames.DELETE,
                                           getUILanguageStringProperty (LanguageStrings.project,actions,deletechapter,warning),
                                           ev ->
                                           {

                                               this.deleteChapter (_c);

                                           },
                                           null,
                                           this);

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

            this.showAddNewChapter (addBelow);

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

        this.addActionMapping (() ->
        {

            this.toggleSpellChecking ();

        },
        CommandId.togglespellchecking);

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

        ProjectChapterEditorPanelContent editor = (ProjectChapterEditorPanelContent) this.getEditorForChapter (c);

        editor.showAddNewScene (pos);

    }

    public void createNewOutlineItem (Chapter c,
                                      int     pos)
    {

        ProjectChapterEditorPanelContent editor = (ProjectChapterEditorPanelContent) this.getEditorForChapter (c);

        editor.showAddNewOutlineItem (pos);

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

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

        // We do this last because the sidebars will be restored by the super.
        super.init (s);

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
                    .styleClassName (StyleClassNames.OPEN)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.openproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,newproject))
                    .styleClassName (StyleClassNames.NEW)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.newproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,renameproject))
                    .styleClassName (StyleClassNames.RENAME)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.renameproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,statistics))
                    .styleClassName (StyleClassNames.STATISTICS)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.statistics);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,targets))
                    .styleClassName (StyleClassNames.TARGETS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.targets);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,createbackup))
                    .styleClassName (StyleClassNames.CREATEBACKUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.createbackup);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,closeproject))
                    .styleClassName (StyleClassNames.CLOSE)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.closeproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,deleteproject))
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.deleteproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,ideaboard))
                    .styleClassName (StyleClassNames.IDEABOARD)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.ideaboard);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,dowarmup))
                    .styleClassName (StyleClassNames.WARMUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.warmup);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,importfileorproject))
                    .styleClassName (StyleClassNames.IMPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.importfile);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,exportproject))
                    .styleClassName (StyleClassNames.EXPORT)
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
                .styleClassName (StyleClassNames.IDEABOARD)
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

    // TODO
    public void editObject (DataObject d)
    {

        // TODO

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

    public boolean isEditing (Chapter c)
    {

        return this.getEditorForChapter (c) != null;

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
                             () -> _this.getEditorForChapter (ci.getChapter ()).showItem (ci,
                                                                                          false));

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

            this.setPanelVisible (p);

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

            if (doAfterView != null)
            {

                // We add the listener to the panel itself so its deregistered when the panel is removed.
                avp.getBinder ().addChangeListener (avp.readyForUseProperty (),
                                                    (pv, oldv, newv) -> UIUtils.runLater (doAfterView));

            }

            // TODO Add state handling...
            avp.init (new State ());

            this.addPanel (avp.getPanel ());

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
    public SideBar getMainSideBar ()
    {

        return this.sidebar.getSideBar ();

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.PROJECT;

    }

    public void addChapterToTreeAfter (Chapter newChapter,
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

    public void openObjectSection (Asset a)
    {

        // TODO this.sideBar.setObjectsOpen (a.getUserConfigurableObjectType ().getObjectTypeId ());

    }

    public void openObjectSection (String objType)
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
            Set<NamedObject> objs = this.project.getAllObjectsWithTag (tag);

            for (NamedObject o : objs)
            {

                o.removeTag (tag);

            }

            this.saveObjects (new ArrayList (objs),
                              true);

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
        this.setLinks (n);

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

            this.fireProjectEvent (ProjectEvent.Type.asset,
                                   ProjectEvent.Action.delete,
                                   a);

        } catch (Exception e)
        {

            Environment.logError ("Unable to remove links: " +
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

            ProjectChapterEditorPanelContent p = (ProjectChapterEditorPanelContent) this.getEditorForChapter (chapter);

            int pos = 0;

            if (p != null)
            {

                pos = Utils.stripEnd (p.getEditor ().getText ()).length ();

            } else {

                String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

                pos = Utils.stripEnd (t).length ();

            }

            chapter.setEditPosition (pos);

            this.saveObject (chapter,
                             false);

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

        ProjectChapterEditorPanelContent p = (ProjectChapterEditorPanelContent) this.getEditorForChapter (chapter);

        SwingUIUtils.doLater (() ->
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

    public void showAddNewChapter (Chapter addBelow)
    {

        new NewChapterPopup (null,
                             this,
                             addBelow,
                             null).show ();

    }

    @Override
    public State getState ()
    {

        return super.getState ();

    }

    public void showAddNewAsset (Asset a)
    {

        // TODOwwa dw

    }

}
