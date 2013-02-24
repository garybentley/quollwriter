package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;

public class DeleteChapterItemActionHandler extends ActionAdapter
{

    private QuollEditorPanel quollEditorPanel = null;
    private ProjectViewer    projectViewer = null;
    private ChapterItem      item = null;

    public DeleteChapterItemActionHandler(ChapterItem      item,
                                          QuollEditorPanel qep)
    {

        this.item = item;
        this.quollEditorPanel = qep;
        this.projectViewer = (ProjectViewer) this.quollEditorPanel.getProjectViewer ();

    }

    public void actionPerformed (ActionEvent ev)
    {

        if (JOptionPane.showConfirmDialog (this.quollEditorPanel,
                                           "Please confirm you wish to delete " + Environment.getObjectTypeName (this.item.getObjectType ()) + ": " +
                                           this.item.getSummary (),
                                           "Confirm deletion of " + Environment.getObjectTypeName (this.item.getObjectType ()) + ": " + this.item.getSummary (),
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
        {

            try
            {

                if (this.item instanceof Scene)
                {
                    
                    Scene s = (Scene) this.item;
                    
                    int c = s.getOutlineItems ().size ();
                    
                    if (c > 0)
                    {
                        
                        String it = Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE) + (c > 1 ? "s" : "");
                        
                        if (JOptionPane.showConfirmDialog (this.quollEditorPanel,
                                                           "The " + Environment.getObjectTypeName (this.item.getObjectType ()) + " has " + c + " " + it + " associated with it.  Would you like to also remove the " + it + "?",
                                                           "Also remove " + it,
                                                           JOptionPane.YES_NO_OPTION,
                                                           JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
                        {

                            this.projectViewer.deleteChapterItem (this.item,
                                                                  true,
                                                                  true);

                            return;

                        } 
                        
                    } 
                                            
                }

                this.projectViewer.deleteChapterItem (this.item,
                                                      false,
                                                      true);

            } catch (Exception e)
            {

                Environment.logError ("Unable to delete item: " +
                                      this.item,
                                      e);

                com.quollwriter.ui.UIUtils.showErrorMessage (this.projectViewer,
                                                             "Unable to delete.");

            }

        }

    }

}
