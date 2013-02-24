package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;

public class DeleteNoteActionHandler extends ActionAdapter
{

    private ProjectViewer    projectViewer = null;
    private QuollEditorPanel quollEditorPanel = null;
    private Note             note = null;

    public DeleteNoteActionHandler(Note             n,
                                   QuollEditorPanel qep)
    {

        this.note = n;
        this.quollEditorPanel = qep;
        this.projectViewer = (ProjectViewer) this.quollEditorPanel.getProjectViewer ();

    }

    public void actionPerformed (ActionEvent ev)
    {

        if (JOptionPane.showConfirmDialog (this.projectViewer,
                                           "Please confirm you wish to delete Note: " +
                                           this.note.getSummary (),
                                           "Confirm deletion of Note: " + this.note.getSummary (),
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
        {

            try
            {

                this.projectViewer.deleteNote (this.note,
                                               true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to delete note: " +
                                      this.note,
                                      e);

                com.quollwriter.ui.UIUtils.showErrorMessage (this.projectViewer,
                                                             "Unable to delete.");

            }

        }

    }

}
