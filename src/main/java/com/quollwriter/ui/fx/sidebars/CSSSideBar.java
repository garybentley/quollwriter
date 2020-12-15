package com.quollwriter.ui.fx.sidebars;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;

public class CSSSideBar extends SideBarContent<CSSViewer>
{

    public static final String SIDEBAR_ID = "css";

    private Node node = null;
    private VBox content = null;
    private TreeItem<Node> tree = null;
    private QuollTreeView<Node> treeView = null;

    public CSSSideBar (CSSViewer v)
    {

        super (v);

        this.content = new VBox ();
        this.treeView = new QuollTreeView ();

        this.viewer.currentPanelProperty ().addListener ((pr, oldv, newv) ->
        {

            CSSNodePanel p = (CSSNodePanel) newv.getContent ();

            this.treeView.select (p.getNode ());

            UIUtils.forceRunLater (() ->
            {

                UIUtils.scrollIntoView (this.treeView.getCellForObject (p.getNode ()),
                                        VPos.CENTER);

            });

        });

        Function<TreeItem<Node>, Node> cellProvider = (treeItem) ->
        {

            Node n = treeItem.getValue ();

            String cn = n.getClass ().getName ();

            QuollLabel l = QuollLabel.builder ()
                .styleClassName ("node")
                .label (CSSViewer.getNodeLabelName (n))
                .tooltip (new SimpleStringProperty (cn))
                .build ();

            l.addEventHandler (MouseEvent.MOUSE_RELEASED,
                               ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                try
                {

                    this.viewer.showNode (n);

                } catch (Exception e) {

                    Environment.logError ("Unable to show node: " + n,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     "Unable to show node.");

                }
                //this.viewer.viewObject (n);

            });

            return l;

        };

        QScrollPane sp = new QScrollPane (this.treeView);

        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        this.treeView.setCellProvider (cellProvider);
        this.treeView.setShowRoot (true);
        this.content.getChildren ().add (sp);
        this.getChildren ().add (this.content);

    }

    public void setNode (Node n)
    {

        this.node = n;


        this.tree = this.createTree ();

        this.treeView.setRoot (this.tree);
        this.treeView.expandPathToRoot (this.treeView.getTreeItemForObject (n));
        this.requestLayout ();

    }

    private TreeItem<Node> createTree ()
    {

        Scene s = this.node.getScene ();

        Parent sr = s.getRoot ();
System.out.println ("ROOT: " + sr);
        TreeItem<Node> root = new TreeItem<> (sr);
System.out.println ("ADD: " + root.getValue ());
        this.addChildren (sr,
                          root);

        return root;

    }

    private void addChildren (Parent         p,
                              TreeItem<Node> pn)
    {

        for (Node n : p.getChildrenUnmodifiable ())
        {

            TreeItem<Node> nn = new TreeItem<> (n);

            pn.getChildren ().add (nn);

            if (n instanceof Parent)
            {

                this.addChildren ((Parent) n,
                                  nn);

            }

        }

    }

    @Override
    public SideBar createSideBar ()
    {

        StringProperty title = new SimpleStringProperty ("CSS");
        //getUILanguageStringProperty (project,LanguageStrings.sidebar,LanguageStrings.title);

        return SideBar.builder ()
            //.title (title)
            .activeTitle (title)
            //.contextMenu ()?
            .styleClassName (StyleClassNames.CSS)
            .styleSheet (StyleClassNames.CSS)
            .withScrollPane (false)
            .canClose (false)
            //.headerControls ()?
            .withViewer (this.viewer)
            .content (this)
            .sideBarId (SIDEBAR_ID)
            .build ();

    }

    @Override
    public void init (State s)
    {

        super.init (s);

    }

}
