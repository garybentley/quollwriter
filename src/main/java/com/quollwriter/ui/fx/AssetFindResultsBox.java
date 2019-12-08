package com.quollwriter.ui.fx;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import com.quollwriter.uistrings.UILanguageStringsManager;
import static com.quollwriter.LanguageStrings.*;

public class AssetFindResultsBox extends FindResultsBox<AbstractProjectViewer>
{

    private Set<Asset> objs = null;
    private QuollTreeView<NamedObject> tree = null;
    private AccordionItem acc = null;
    private UserConfigurableObjectType type = null;

    public AssetFindResultsBox (UserConfigurableObjectType type,
                                AbstractProjectViewer      viewer,
                                Set<Asset>                 objs)
    {

        super (viewer,
               null);

        this.objs = objs;
        this.type = type;

    }

    @Override
    public void dispose ()
    {

    }

    public QuollTreeView<NamedObject> getTree ()
    {

        if (this.tree == null)
        {

            this.tree = this.createTree ();

        }

        return this.tree;

    }

    @Override
    public Node getContent ()
    {

        if (this.tree == null)
        {

            this.tree = this.getTree ();

        }

        StringProperty tProp = new SimpleStringProperty ();
        tProp.bind (UILanguageStringsManager.createStringBinding (() ->
        {

            return String.format ("%1$s (%2$s)",
                                  this.type.getObjectTypeNamePlural (),
                                  Environment.formatNumber (this.objs.size ()));

        }));

        this.acc = AccordionItem.builder ()
            .title (tProp)
            .styleClassName (StyleClassNames.ASSET)
            .openContent (this.tree)
            .build ();

        this.acc.getHeader ().getIcon ().imageProperty ().bind (this.type.icon16x16Property ());

        return this.acc;

    }

    private QuollTreeView<NamedObject> createTree ()
    {

        Function<TreeItem<NamedObject>, Node> cellProvider = (treeItem) ->
        {

            NamedObject n = treeItem.getValue ();

            if (n instanceof Project)
            {

                return new Label ();

            }

            QuollLabel l = QuollLabel.builder ()
                .styleClassName (n.getObjectType ())
                .label (n.getName ())
                .build ();

            l.addEventHandler (MouseEvent.MOUSE_RELEASED,
                               ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                this.viewer.viewObject (n);

            });

            return l;

        };

        // Create the model.
        TreeItem<NamedObject> root = new TreeItem<> ();
        root.setValue (this.viewer.getProject ());

        for (Asset o : this.objs)
        {

            TreeItem<NamedObject> ci = new TreeItem<> ();
            ci.setValue (o);
            root.getChildren ().add (ci);

        }

        QuollTreeView<NamedObject> tree = new QuollTreeView<> ();
        tree.setShowRoot (false);
        tree.setCellProvider (cellProvider);
        tree.setRoot (root);

        return tree;

    }

}
