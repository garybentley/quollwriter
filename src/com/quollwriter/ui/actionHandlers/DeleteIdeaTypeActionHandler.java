package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;

public class DeleteIdeaTypeActionHandler extends ActionAdapter
{

    private IdeaType  ideaType = null;
    private IdeaBoard ideaBoard = null;

    public DeleteIdeaTypeActionHandler(IdeaType  it,
                                       IdeaBoard ib)
    {

        this.ideaType = it;
        this.ideaBoard = ib;

    }

    private String showDialog ()
    {

        Object o = JOptionPane.showInputDialog (this.ideaBoard,
                                                "Please confirm you wish to delete Idea Type: " +
                                                this.ideaType.getName () +
                                                "\nby entering the word \"Yes\" in the box below." +
                                                "\n\nWarning!  All ideas associated with the type\nwill be deleted.  Once deleted a type and ideas cannot be restored.\n",
                                                "Confirm deletion of Idea Type: " + this.ideaType.getName (),
                                                JOptionPane.WARNING_MESSAGE);

        if (o == null)
        {

            return "cancel";

        }

        if (!o.toString ().toLowerCase ().equals ("yes"))
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

            UIUtils.showErrorMessage (this.ideaBoard,
                                      "Please enter the word \"Yes\" to confirm deletion of " +
                                      this.ideaType.getName ());

        }

        if (res.equals ("cancel"))
        {

            return;

        }

        this.ideaBoard.deleteIdeaType (this.ideaType);

    }

}
