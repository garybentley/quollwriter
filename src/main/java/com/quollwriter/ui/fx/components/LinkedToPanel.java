package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

public class LinkedToPanel extends StackPane
{

    private QuollTreeView<NamedObject> editTree = null;
    private QuollTreeView<NamedObject> viewTree = null;
    private NamedObject obj = null;
    private AbstractProjectViewer viewer = null;

    public LinkedToPanel (NamedObject           obj,
                          IPropertyBinder       binder,
                          AbstractProjectViewer viewer)
    {

        this.obj = obj;
        this.viewer = viewer;

        this.getStyleClass ().add (StyleClassNames.LINKEDTO);

        binder.addSetChangeListener (obj.getLinks (),
                                     ev ->
        {
/*
TODO The add is really complex... need to find the parent and the right position to add...
            if (ev.wasAdded ())
            {

                this.viewTree. (ev.getElementAdded ().getOtherObject (obj));
                this.

            }

            if (ev.wasRemoved ())
            {

                this.viewTree.removeObject (ev.getElementRemoved ().getOtherObject (obj));
                this.editTree.removeObject (ev.getElementRemoved ().getOtherObject (obj));

            }
*/

            this.viewTree.setRoot (this.createViewTree ());
            this.viewTree.requestLayout ();

        });

        Function<TreeItem<NamedObject>, Node> cellProvider = (treeItem) ->
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

            if (n instanceof UserConfigurableObjectType)
            {

                l.getStyleClass ().add (StyleClassNames.HEADER);
                UserConfigurableObjectType type = (UserConfigurableObjectType) n;
                ImageView iv = new ImageView ();
                iv.imageProperty ().bind (type.icon16x16Property ());

                l.setGraphic (iv);

            }

            l.setOnMouseClicked (ev ->
            {

                if ((this.editTree.isVisible ())
                    ||
                    (n instanceof UserConfigurableObjectType)
                    ||
                    (n instanceof BlankNamedObject)
                   )
                {

                    if (treeItem.getChildren ().size () > 0)
                    {

                        treeItem.setExpanded (!treeItem.isExpanded ());

                    }

                } else {

                    viewer.viewObject (n);

                }

            });

            if (n instanceof BlankNamedObject)
            {

                l.getStyleClass ().add (StyleClassNames.HEADER);

            }

            return l;

        };

        this.editTree = new QuollTreeView<> ();
        this.editTree.getStyleClass ().add (StyleClassNames.EDIT);
        this.editTree.setShowRoot (false);
        this.editTree.setCellProvider (cellProvider);
        this.editTree.setRoot (this.createEditTree ());
        this.editTree.managedProperty ().bind (this.editTree.visibleProperty ());
        this.editTree.setVisible (false);

        this.viewTree = new QuollTreeView<> ();
        this.viewTree.getStyleClass ().add (StyleClassNames.VIEW);
        this.viewTree.setShowRoot (false);
        this.viewTree.setCellProvider (cellProvider);
        this.viewTree.setRoot (this.createViewTree ());
        this.viewTree.managedProperty ().bind (this.viewTree.visibleProperty ());

        this.getChildren ().addAll (this.editTree, this.viewTree);

    }

    private TreeItem<NamedObject> createEditTree ()
    {

        Set<NamedObject> exclude = new HashSet<> ();
        exclude.add (this.obj);

        // Painful but just about the only way.
        // TODO Needed?
        //viewer.setLinks (this.obj);

        Set<NamedObject> links = this.obj.getLinks ().stream ()
            .map (l -> l.getOtherObject (this.obj))
            .collect (Collectors.toSet ());

        Project proj = this.viewer.getProject ();

        TreeItem<NamedObject> root = new TreeItem<> (proj);

        UIUtils.createChaptersSelectableTree (root,
                                              proj,
                                              links,
                                              exclude);

        Set<UserConfigurableObjectType> assetTypes = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {

            Set<Asset> as = proj.getAssets (t);

            if ((as == null)
                ||
                (as.size () == 0)
               )
            {

                continue;

            }

            UIUtils.createAssetsSelectableTree (root,
                                                t,
                                                as,
                                                links,
                                                exclude);

        }

        UIUtils.createNotesSelectableTree (root,
                                           proj,
                                           links,
                                           exclude);

        return root;

    }

    private TreeItem<NamedObject> createViewTree ()
    {

        // Painful but just about the only way.
        // TODO Needed?
        //viewer.setLinks (this.obj);

        Set<NamedObject> links = this.obj.getLinks ().stream ()
            .map (l -> l.getOtherObject (this.obj))
            .collect (Collectors.toSet ());

        links.remove (this.obj);

        Project proj = this.viewer.getProject ();

        TreeItem<NamedObject> root = new TreeItem<> (proj);

        UIUtils.createChaptersTree (root,
                                    proj,
                                    links);

        Set<UserConfigurableObjectType> assetTypes = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {

            Set<Asset> as = proj.getAssets (t);

            if ((as == null)
                ||
                (as.size () == 0)
               )
            {

                continue;

            }

            UIUtils.createAssetsTree (root,
                                      t,
                                      as,
                                      links);

        }

        UIUtils.createNotesTree (root,
                                 proj,
                                 links);

        return root;

    }

    public Set<NamedObject> getSelected ()
    {

        Set<NamedObject> ret = new LinkedHashSet<> ();

        this.walkAndCollectSelected (this.editTree.getRoot (),
                                     ret);

        return ret;

    }

    private void walkAndCollectSelected (TreeItem<NamedObject> ti,
                                         Set<NamedObject>      addTo)
    {

        if (ti instanceof CheckBoxTreeItem)
        {

            CheckBoxTreeItem<NamedObject> ci = (CheckBoxTreeItem<NamedObject>) ti;

            if (ci.isSelected ())
            {

                NamedObject o = ci.getValue ();

                if ((o.getKey () != null)
                    &&
                    (!(o instanceof UserConfigurableObjectType))
                   )
                {

                    addTo.add (o);

                }

            }

        }

        for (TreeItem<NamedObject> c : ti.getChildren ())
        {

            this.walkAndCollectSelected (c,
                                         addTo);

        }

    }

    public void showEdit ()
    {

        this.editTree.setRoot (this.createEditTree ());
        this.editTree.walkTree (i ->
        {

            if (i instanceof CheckBoxTreeItem)
            {

                CheckBoxTreeItem ci = (CheckBoxTreeItem) i;

                if (ci.isSelected ())
                {

                    this.editTree.expandPathToRoot (ci);

                }

            }

        });
        this.viewTree.setVisible (false);

        Set<NamedObject> otherObjs = obj.getOtherObjectsInLinks ();

        this.editTree.walkTree (ti ->
        {

            if (ti instanceof CheckBoxTreeItem)
            {

                CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) ti;

                cti.setSelected (otherObjs.contains (cti.getValue ()));

            }

        });
        this.editTree.setVisible (true);

    }

    public boolean isEditVisible ()
    {

        return this.editTree.isVisible ();

    }

    public void showView ()
    {

        this.viewTree.setRoot (this.createViewTree ());
        this.viewTree.setVisible (true);
        this.editTree.setVisible (false);

    }

}
