package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ActionAdapter;


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

        final DeleteOutlineItemActionHandler _this = this;
    
        UIUtils.createQuestionPopup (this.projectViewer,
                                     "Delete " + Environment.getObjectTypeName (this.item),
                                     Constants.DELETE_ICON_NAME,
                                     "Please confirm you wish to delete " +
                                     Environment.getObjectTypeName (this.item) +
                                     " <b>" +
                                     this.item.getSummary () +
                                     "</b>?",
                                     "Yes, delete it",
                                     null,
                                     new ActionListener ()
                                     {
                                        
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            try
                                            {
                                
                                                _this.projectViewer.deleteOutlineItem (_this.item,
                                                                                       true);
                                
                                            } catch (Exception e)
                                            {
                                
                                                Environment.logError ("Unable to save item: " +
                                                                      _this.item,
                                                                      e);
                                
                                                com.quollwriter.ui.UIUtils.showErrorMessage (_this.projectViewer,
                                                                                             "Unable to delete.");
                                
                                            }
                                        
                                        }
                                        
                                     },
                                     null,
                                     null,
                                     null);

    }

}
