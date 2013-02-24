package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.*;


public class DeleteCharacterActionHandler extends ActionAdapter
{

    private ProjectViewer projectViewer = null;
    private QCharacter    c = null;

    public DeleteCharacterActionHandler(QCharacter    c,
                                        ProjectViewer pv)
    {

        this.c = c;
        this.projectViewer = pv;

    }

    public void actionPerformed (ActionEvent ev)
    {

        if (JOptionPane.showConfirmDialog (this.projectViewer,
                                           "Please confirm you wish to delete Character: " +
                                           this.c.getName (),
                                           "Confirm deletion of Character: " + this.c.getName (),
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
        {

            this.projectViewer.deleteAsset (this.c);

        }

    }

}
