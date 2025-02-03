package com.quollwriter.editors.ui.sidebars;

import java.util.*;

import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.editors.*;

public class EditorFindPanel extends VBox
{

    private EditorsSideBar sideBar = null;
    private AccordionItem matchesBox = null;
    //private JLabel loading = null;

    public EditorFindPanel (EditorsSideBar sb)
    {

        this.sideBar = sb;

        EditorProject proj = null;//this.sideBar.getProjectViewer ().getProject ().getEditorProject ();

    }

    private void doSearch ()
    {

        try
        {

            java.util.List<EditorEditor> eds = EditorsEnvironment.getEditorsWebServiceHandler ().findEditors ();

            //this.matchesBox.setContent (this.sideBar.createEditorsFindList (eds));

            //this.matchesBox.setVisible (true);

            //this.loading.setVisible (false);

        } catch (Exception e) {

            e.printStackTrace ();

        }

    }

    public void init ()
    {


    }

}
