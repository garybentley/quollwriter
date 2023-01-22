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

        //super (BoxLayout.Y_AXIS);

        this.sideBar = sb;

        //Box b = new ScrollableBox (BoxLayout.Y_AXIS);

        EditorProject proj = null;//this.sideBar.getProjectViewer ().getProject ().getEditorProject ();
/*
        Set<String> selectedGenres = null;

        if (proj != null)
        {

            selectedGenres = proj.getGenres ();

        }

        Box crit = new Box (BoxLayout.Y_AXIS);

        TextFormItem nameF = new TextFormItem ("Name",
                                               false,
                                               1,
                                               null,
                                               -1,
                                               null,
                                               false,
                                               null);
        nameF.init ();
        crit.add (nameF);

        Vector<String> gitems = new Vector (EditorsEnvironment.getWritingGenres ());

        SelectFormItem genreF = new SelectFormItem ("Genre",
                                           gitems,
                                           5,
                                           selectedGenres,
                                           -1,
                                           false,
                                           null);
        genreF.init ();
        crit.add (genreF);

        AccordionItem ai = new AccordionItem ("Find Editors by",
                                              null,
                                              crit);

        b.add (ai);
        ai.init ();
        ai.setAlignmentX (Component.LEFT_ALIGNMENT);
        //this.add (ai);

        b.add (Box.createVerticalStrut (5));

        final EditorFindPanel _this = this;

        JButton find = UIUtils.createButton (Constants.FIND_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Click to find editors",
                                             new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.doSearch ();

            }

        });

        find.setText ("Find Editors");

        b.add (find);

        b.add (Box.createVerticalStrut (10));

        Box matches = new Box (BoxLayout.Y_AXIS);

        this.loading = new JLabel (Environment.getLoadingIcon ());
        this.loading.setText ("Searching...");
        this.loading.setBorder (new EmptyBorder (5, 10, 5, 5));
        matches.add (this.loading);
        this.matchesBox = new AccordionItem ("Matches",
                                             null,
                                             matches);

        b.add (this.matchesBox);
        this.matchesBox.init ();
        this.matchesBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.matchesBox.setVisible (false);

        b.add (UIUtils.createOpaqueGlue (BoxLayout.Y_AXIS));

        JScrollPane sp = new JScrollPane (b);

        sp.setOpaque (false);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (new EmptyBorder (0, 0, 0, 0));
        sp.getViewport ().setOpaque (false);
        sp.getVerticalScrollBar ().setUnitIncrement (20);

        this.add (sp);
        */
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
