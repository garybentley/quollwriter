package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.beans.property.*;

import com.quollwriter.data.Scene;
import com.quollwriter.data.OutlineItem;
import com.quollwriter.data.Project;
import com.quollwriter.data.Chapter;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.Note;
import com.quollwriter.data.ChapterItem;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ChaptersSidebarItem extends ProjectObjectsSidebarItem<ProjectViewer>
{

    private QuollTreeView<NamedObject> tree = null;
    private IntegerProperty countProp = null;

    public ChaptersSidebarItem (ProjectViewer pv)
    {

        super (pv);

        // TODO Have better handling of the changes.
        pv.getProject ().getBooks ().get (0).getChapters ().addListener ((ListChangeListener<Chapter>) ev -> this.reloadTree ());

        this.countProp = new SimpleIntegerProperty (0);

        this.viewer.currentPanelProperty ().addListener ((pr, oldv, newv) ->
        {

            if (newv != null)
            {

                if (newv.getContent () instanceof NamedObjectPanelContent)
                {

                    NamedObjectPanelContent nc = (NamedObjectPanelContent) newv.getContent ();

                    if (nc.getObject () instanceof Chapter)
                    {

                        this.tree.select ((Chapter) nc.getObject ());

                    }

                }

            } else {

                this.tree.clearSelection ();

            }

        });

        this.tree = new QuollTreeView<> ();
        this.tree.setShowRoot (false);
        this.tree.setCellProvider (treeItem ->
        {

            NamedObject n = treeItem.getValue ();

            if (n instanceof Project)
            {

                return new Label ();

            }

            QuollLabel l = QuollLabel.builder ()
                .label (n.nameProperty ())
                .styleClassName (n.getObjectType ())
                .build ();

            l.setOnMouseClicked (ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                this.viewer.viewObject (n);

            });

            List<String> prefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,items);

            ContextMenu m = new ContextMenu ();

            if (n instanceof Note)
            {

                java.util.List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,notes,items);

                final Note note = (Note) n;

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                    .styleClassName (StyleClassNames.VIEW)
                    .onAction (ev ->
                    {

                        this.viewer.viewObject (n);

                    })
                    .build ());

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                    .styleClassName (StyleClassNames.EDIT)
                    .onAction (ev ->
                    {

                        this.viewer.editObject (n);

                    })
                    .build ());

                Menu tm = UIUtils.createTagsMenu (n,
                                                  this.viewer);

                if (tm != null)
                {

                    m.getItems ().add (tm);

                }

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        this.viewer.editObject (n);

                    })
                    .build ());

            }

            if (n instanceof Chapter)
            {

                final Chapter c = (Chapter) n;

                l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, (!c.isEditComplete () && c.getEditPosition () > 0));
                l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, c.isEditComplete ());

                c.editPositionProperty ().addListener ((pr, oldv, newv) ->
                {

                    if (!c.isEditComplete ())
                    {

                        l.pseudoClassStateChanged (StyleClassNames.EDITPOSITION_PSEUDO_CLASS, newv.intValue () > 0);

                    }

                });

                c.editCompleteProperty ().addListener ((pr, oldv, newv) ->
                {

                    l.pseudoClassStateChanged (StyleClassNames.EDITCOMPLETE_PSEUDO_CLASS, newv);

                });

                final String chapterObjTypeName = Environment.getObjectTypeName (c).getValue ();

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,edit)))
                    .styleClassName (StyleClassNames.EDIT)
                    .onAction (ev ->
                    {

                        this.viewer.editObject (n);

                    })
                    .build ());

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,view)))
                    .styleClassName (StyleClassNames.INFO)
                    .onAction (ev ->
                    {

                        this.viewer.viewChapterInformation (c);

                    })
                    .build ());

                if (!c.isEditComplete ())
                {

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,seteditcomplete)))
                        .styleClassName (StyleClassNames.EDITCOMPLETE)
                        .onAction (ev ->
                        {

                            this.viewer.setChapterEditComplete (c,
                                                                true);

                        })
                        .build ());

                } else {

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,seteditneeded)))
                        .styleClassName (StyleClassNames.EDITNEEDED)
                        .onAction (ev ->
                        {

                            this.viewer.setChapterEditComplete (c,
                                                                false);

                        })
                        .build ());

                }

                if (c.getEditPosition () > 0)
                {

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,removeeditposition)))
                        .styleClassName (StyleClassNames.REMOVEEDITPOSITION)
                        .onAction (ev ->
                        {

                            this.viewer.removeChapterEditPosition (c);

                        })
                        .build ());

                }

                Menu tm = UIUtils.createTagsMenu (n,
                                                  this.viewer);

                if (tm != null)
                {

                    m.getItems ().add (tm);

                }

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,newbelow)))
                    .styleClassName (StyleClassNames.ADD)
                    .onAction (ev ->
                    {

                        this.viewer.addNewChapterBelow (c);

                    })
                    .build ());

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,rename)))
                    .styleClassName (StyleClassNames.RENAME)
                    .onAction (ev ->
                    {

                        this.viewer.renameChapter (c);

                    })
                    .build ());

                if (this.viewer.isEditing (c))
                {

                    m.getItems ().add (QuollMenuItem.builder ()
                        .label (getUILanguageStringProperty (Utils.newList (prefix,close)))
                        .styleClassName (StyleClassNames.CLOSE)
                        .onAction (ev ->
                        {

                            this.viewer.closePanel (c,
                                                    null);

                        })
                        .build ());

                }

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (prefix,delete)))
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        this.viewer.deleteChapter (c);

                    })
                    .build ());

            }

            if (n instanceof OutlineItem)
            {

                List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,outlineitems,items);

                final OutlineItem oi = (OutlineItem) n;

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (nprefix,view)))
                    .styleClassName (StyleClassNames.VIEW)
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.viewoutlineitem,
                                                oi);

                    })
                    .build ());

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (nprefix,edit)))
                    .styleClassName (StyleClassNames.EDIT)
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.editoutlineitem,
                                                oi);

                    })
                    .build ());

                Menu tm = UIUtils.createTagsMenu (n,
                                                  this.viewer);

                if (tm != null)
                {

                    m.getItems ().add (tm);

                }

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (nprefix,delete)))
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.deleteoutlineitem,
                                                oi);

                    })
                    .build ());

            }

            if (n instanceof Scene)
            {

                List<String> nprefix = Arrays.asList (project,sidebar,chapters,treepopupmenu,scenes,items);

                final Scene s = (Scene) n;

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (nprefix,view)))
                    .styleClassName (StyleClassNames.VIEW)
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.viewscene,
                                                s);

                    })
                    .build ());

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (nprefix,edit)))
                    .styleClassName (StyleClassNames.EDIT)
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.editscene,
                                                s);

                    })
                    .build ());

                Menu tm = UIUtils.createTagsMenu (n,
                                                  this.viewer);

                if (tm != null)
                {

                    m.getItems ().add (tm);

                }

                m.getItems ().add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (Utils.newList (nprefix,delete)))
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        this.viewer.runCommand (ProjectViewer.CommandId.deletescene,
                                                s);

                    })
                    .build ());

            }

            l.setContextMenu (m);

            return l;

        });

    }

    private void selectChapter (Chapter c)
    {

        this.tree.select (c);


    }

    @Override
    public Node getContent ()
    {

        return this.tree;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.CHAPTER;

    }

    @Override
    public StringProperty getTitle ()
    {

        return Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE);

    }

    @Override
    public String getId ()
    {

        return Chapter.OBJECT_TYPE;

    }

    @Override
    public void init (State ss)
    {

        super.init (ss);

        this.reloadTree ();

    }

    @Override
    public BooleanProperty showItemCountOnHeader ()
    {

        // TODO Make a style?
        return new SimpleBooleanProperty (true);

    }

    @Override
    public State getState ()
    {

        State ss = super.getState ();

        if (ss == null)
        {

            ss = new State ();

        }

        return ss;

    }

    @Override
    public Supplier<Set<MenuItem>> getHeaderContextMenuItemSupplier ()
    {

        List<String> prefix = Arrays.asList (project,sidebar,chapters,headerpopupmenu,items);

        return () ->
        {

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings._new)))
                .styleClassName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    this.viewer.addNewChapter ();

                })
                .build ());

            return items;

        };

    }

    @Override
    public void reloadTree ()
    {

        // Set the root of the tree.
        this.tree.setRoot (this.createTree ());

    }

    private TreeItem<NamedObject> createTree ()
    {

        List<Chapter> objs = new ArrayList<> (this.viewer.getProject ().getBooks ().get (0).getChapters ());

        TreeItem<NamedObject> root = new TreeItem<> (this.viewer.getProject ());

        for (Chapter c : objs)
        {

            TreeItem<NamedObject> ci = new TreeItem<> (c);

            root.getChildren ().add (ci);

            List<ChapterItem> items = new ArrayList<> (c.getScenes ());

            items.addAll (c.getOutlineItems ());

            Collections.sort (items,
                              new ChapterItemSorter ());

            for (ChapterItem citem : items)
            {

                TreeItem<NamedObject> cii = new TreeItem<> (citem);

                ci.getChildren ().add (cii);

                if (citem instanceof Scene)
                {

                    Scene s = (Scene) citem;

                    for (OutlineItem oitem : s.getOutlineItems ())
                    {

                        TreeItem<NamedObject> oii = new TreeItem<> (oitem);

                        cii.getChildren ().add (oii);

                    }

                }

            }

        }

        this.countProp.setValue (objs.size ());

        return root;

    }

    @Override
    public IntegerProperty getItemCount ()
    {

        return this.countProp;

    }

    @Override
    public boolean isDragEnabled ()
    {

        return true;

    }

/*
    @Override
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv)
    {

        final AssetAccordionItem _this = this;

        return new DragActionHandler<Asset> ()
        {

            @Override
            public boolean canImportForeignObject (NamedObject obj)
            {

                // Allow support of addition of same object type.
                return false;

            }

            @Override
            public boolean importForeignObject (NamedObject obj,
                                                int         insertRow)
            {

                return false;

            }

            @Override
            public boolean handleMove (int   fromRow,
                                       int   toRow,
                                       Asset object)
                                throws GeneralException
            {

                _this.sortField = null;

                QuollPanel p = _this.viewer.getCurrentlyVisibleTab ();

                if (p != null)
                {

                    if (p instanceof ProjectObjectQuollPanel)
                    {

                        ProjectObjectQuollPanel pp = (ProjectObjectQuollPanel) p;

                        if (pp.getForObject ().equals (object))
                        {

                            return true;

                        }

                    }

                }

                _this.getTree ().removeSelectionPath (UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) _this.getTree ().getModel ().getRoot (),
                                                      object));

                return true;

            }

            @Override
            public boolean performAction (int         removeRow,
                                          NamedObject removeObject,
                                          int         insertRow,
                                          NamedObject insertObject)
                                   throws GeneralException
            {

                return true;

            }

        };

    }
*/

}
