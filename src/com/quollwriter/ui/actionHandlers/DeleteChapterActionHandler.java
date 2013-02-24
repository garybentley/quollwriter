package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.AbstractProjectViewer;
import com.quollwriter.ui.components.*;


public class DeleteChapterActionHandler extends ActionAdapter
{

    private AbstractProjectViewer projectViewer = null;
    private Chapter               chapter = null;

    public DeleteChapterActionHandler(Chapter               c,
                                      AbstractProjectViewer pv)
    {

        this.chapter = c;
        this.projectViewer = pv;

    }

    private String showDialog ()
    {

        String objName = this.projectViewer.getChapterObjectName ();

        Object o = JOptionPane.showInputDialog (this.projectViewer,
                                                "Please confirm you wish to delete " + objName + ": " +
                                                this.chapter.getName () +
                                                "\nby entering the word \"Yes\" in the box below." +
                                                "\n\nWarning!  All information/text associated with the " + objName + "\nwill be deleted.  Once deleted a " + objName + " cannot be restored.\n",
                                                "Confirm deletion of " + objName + ": " + this.chapter.getName (),
                                                JOptionPane.WARNING_MESSAGE);

        if (o == null)
        {

            return "cancel";

        }

        if (!o.toString ().trim ().toLowerCase ().equals ("yes"))
        {

            return "error";

        }

        return "ok";

    }

    public void actionPerformed (ActionEvent ev)
    {

        String res = "";

        while ((res = this.showDialog ()).equals ("error"))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please enter the word \"Yes\" to confirm deletion of " +
                                      this.chapter.getName ());

        }

        if (res.equals ("cancel"))
        {

            return;

        }

        this.projectViewer.deleteChapter (this.chapter);

    }

}
