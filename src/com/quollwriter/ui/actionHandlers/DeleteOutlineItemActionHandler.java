package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.*;


public class DeleteOutlineItemActionHandler extends ActionAdapter
{

    private ProjectViewer projectViewer = null;
    private OutlineItem   item = null;

    public DeleteOutlineItemActionHandler(OutlineItem   item,
                                          ProjectViewer pv)
    {

        this.item = item;
        this.projectViewer = pv;

    }

    public void actionPerformed (ActionEvent ev)
    {

        if (JOptionPane.showConfirmDialog (this.projectViewer,
                                           "Please confirm you wish to delete Plot Outline Item: " +
                                           this.item.getSummary (),
                                           "Confirm deletion of Plot Outline Item: " + this.item.getSummary (),
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
        {

            try
            {

                this.projectViewer.deleteOutlineItem (this.item,
                                                      true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save item: " +
                                      this.item,
                                      e);

                com.quollwriter.ui.UIUtils.showErrorMessage (this.projectViewer,
                                                             "Unable to delete.");

            }


        }

    }

}
