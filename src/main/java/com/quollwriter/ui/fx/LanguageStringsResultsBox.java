package com.quollwriter.ui.fx;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

public class LanguageStringsResultsBox extends FindResultsBox<AbstractLanguageStringsEditor>
{

    private QuollTreeView<Node> tree = null;
    private Map<Section, Map<Node, List<Value>>> matches = new HashMap<> ();

    public LanguageStringsResultsBox (AbstractLanguageStringsEditor        viewer,
                                      Map<Section, Map<Node, List<Value>>> matches)
    {

        super (viewer,
               null);

        this.matches = matches;

    }

    @Override
    public void dispose ()
    {

    }

    public QuollTreeView<Node> getTree ()
    {

        if (this.tree == null)
        {

            this.tree = this.createTree ();

        }

        return this.tree;

    }

    @Override
    public javafx.scene.Node getContent ()
    {

        if (this.tree == null)
        {

            this.tree = this.getTree ();

        }

        return this.tree;

    }

    private QuollTreeView<Node> createTree ()
    {

        TreeItem<Node> root = new TreeItem<> ();
        root.setValue (new Node ("root", null));

        Function<TreeItem<Node>, javafx.scene.Node> cellProvider = (treeItem) ->
        {

            Node n = treeItem.getValue ();

            boolean parent = treeItem.getParent () == root;

            int c = treeItem.getChildren ().size ();

            String title = (n.getTitle () != null ? n.getTitle () : BaseStrings.toId (n.getId ()));

            if (parent)
            {

                String name = String.format ("%s (%s)",
                                             title,
                                             Environment.formatNumber (c));

                QuollLabel l = QuollLabel.builder ()
                    .label (new SimpleStringProperty (name))
                    .styleClassName (this.viewer.getBaseStrings ().getSection (n.getSection ()).icon)
                    .build ();

                l.getStyleClass ().add ("section");

                l.setOnMousePressed (ev ->
                {

                    this.viewer.showIds (new Id (BaseStrings.toId (n.getId ())));

                });

                return l;

            } else {

                String name = String.format ("%s",
                                             title,
                                             Environment.formatNumber (c));

                QuollLabel l = QuollLabel.builder ()
                    .label (new SimpleStringProperty (name))
                    .build ();

                l.setOnMousePressed (ev ->
                {

                    this.viewer.showIds (new Id (BaseStrings.toId (n.getParent ().getId ())));

                });

                return l;

            }

        };

        // Create the model.

        for (Section s : this.matches.keySet ())
        {

            Map<Node, List<Value>> items = this.matches.get (s);

            for (Node n : items.keySet ())
            {

                TreeItem<Node> ci = new TreeItem<> ();
                ci.setValue (n);
                root.getChildren ().add (ci);

                List<Value> vals = items.get (n);

                for (Value v : vals)
                {

                    TreeItem<Node> ii = new TreeItem<> ();
                    ii.setValue (v);
                    ci.getChildren ().add (ii);

                }

            }

        }

        QuollTreeView tree = new QuollTreeView<> ();
        tree.setShowRoot (false);
        tree.getStyleClass ().add (StyleClassNames.LANGUAGESTRINGS);
        tree.setCellProvider (cellProvider);
        tree.setRoot (root);

        UIUtils.addStyleSheet (tree,
                               Constants.COMPONENT_STYLESHEET_TYPE,
                               "languagestringsresultsbox");

        return tree;

    }

}
