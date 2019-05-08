package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.util.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollComboBox2<T> extends Button
{

    private List<T> items = null;
    private int selInd = -1;
    private Function<T, Node> cellProvider = null;
    private AbstractViewer viewer = null;

    public QuollComboBox2 (AbstractViewer viewer)
    {

        this.viewer = viewer;
        this.getStyleClass ().add ("qcombobox");
/*
        this.obsItems.addListener (ev ->
        {

            this.rebuild ();

        });
*/
        this.setOnAction (ev ->
        {

            this.showPopup ();

        });

    }

    public QuollComboBox2 (AbstractViewer viewer, Set<T> items)
    {

        this (viewer);
        this.setItems (items);

    }

    public void setItems (Set<T> items)
    {

        this.selInd = 0;
        this.items = new ArrayList<> (items);
        //this.obbsItems = FXCollections.observableList (this.items);
        this.rebuild ();

    }

    private void rebuild ()
    {

        this.setGraphic (this.getCellProvider ().apply (this.getSelectedItem ()));

    }

    private void addMousePressedHandler (Node  n,
                                         PopupControl p,
                                         int   i)
    {

        n.setOnMousePressed (ev ->
        {

            p.hide ();
            this.setSelectedIndex (i);

        });

    }

    private void showPopup ()
    {

        if (this.items == null)
        {

            return;

        }

        PopupControl p = new PopupControl ();
        //p.setAutoHide (true);
        //p.setHideOnEscape (true);

        VBox vb = new VBox ();
        vb.getStyleClass ().add ("qcombobox");
        vb.getStyleClass ().add (StyleClassNames.ITEMS);

        for (int i = 0; i < this.items.size (); i++)
        {

            T it = this.items.get (i);

            Node n = this.getCellProvider ().apply (it);
            n.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, i == this.getSelectedIndex ());

            vb.getChildren ().add (n);

            this.addMousePressedHandler (n,
                                         p,
                                         i);

        }

        ScrollPane sp = new ScrollPane (vb);
        p.getScene ().setRoot (sp);
        p.getScene ().getStylesheets ().add (UserProperties.getUserStyleSheetURL ().toExternalForm ());
        sp.setPrefWidth (300);
        sp.setPrefHeight (300);
        p.sizeToScene ();
        p.setX (100);
        p.setY (100);
System.out.println ("SP: " + sp.getPrefWidth ());
System.out.println ("SP: " + sp.getPrefHeight ());
        //p.getContent ().add (sp);

        p.show (this.viewer.getViewer ());
UIUtils.runLater (() ->
{
    System.out.println ("P: " + p.getX () + ", " + p.getY ());
});
    }

    public void setCellProvider (Function<T, Node> cellProvider)
    {

        this.cellProvider = cellProvider;

    }

    public Function<T, Node> getCellProvider ()
    {

        if (this.cellProvider == null)
        {

            this.cellProvider = it ->
            {

                QuollLabel l = QuollLabel.builder ()
                    .build ();
                l.setText (it.toString ());
                return l;

            };

        }

        return this.cellProvider;

    }

    public void setSelectedIndex (int ind)
    {

        this.selInd = ind;
        this.rebuild ();

    }

    public int getSelectedIndex ()
    {

        return this.selInd;

    }

    public void setSelectedItem (T it)
    {

        this.selInd = this.items.indexOf (it);
        this.rebuild ();

    }

    public T getSelectedItem ()
    {

        if (this.selInd < 0)
        {

            return null;

        }

        return this.items.get (this.selInd);

    }

}
