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

public class NamedObjectFindResultsBox<E extends NamedObject> extends FindResultsBox<AbstractProjectViewer>
{

    private Set<E> objs = null;
    private QuollTreeView<NamedObject> tree = null;
    private AccordionItem acc = null;
    private String objType = null;

    public NamedObjectFindResultsBox (String                objType,
                                      AbstractProjectViewer viewer,
                                      Set<E>                objs)
    {

        super (viewer,
               null);

        this.objs = objs;
        this.objType = objType;

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
                                  getUILanguageStringProperty (objectnames,plural,this.objType).getValue (),
                                  Environment.formatNumber (this.objs.size ()));

        }));

        this.acc = AccordionItem.builder ()
            .title (tProp)
            .styleClassName (this.objType)
            .openContent (this.tree)
            .build ();

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

        for (NamedObject o : this.objs)
        {

            TreeItem<NamedObject> ci = new TreeItem<> ();
            ci.setValue (o);
            root.getChildren ().add (ci);

        }

        QuollTreeView<NamedObject> tree = new QuollTreeView<> ();
        tree.setShowRoot (false);
        tree.getStyleClass ().add (this.objType);
        tree.setCellProvider (cellProvider);
        tree.setRoot (root);

        return tree;

    }

}
