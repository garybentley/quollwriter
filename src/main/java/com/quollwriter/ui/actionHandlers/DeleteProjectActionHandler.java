package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.editors.*;

public class DeleteProjectActionHandler extends YesDeleteConfirmTextInputActionHandler<AbstractViewer>
{

    private ProjectInfo projInfo = null;
    private ActionListener onDelete = null;
    
    public DeleteProjectActionHandler (AbstractViewer viewer,
                                       Project        proj,
                                       ActionListener onDelete)
    {

        this (viewer,
              Environment.getProjectInfo (proj),
              onDelete);
               
    }

    public DeleteProjectActionHandler (AbstractViewer viewer,
                                       ProjectInfo    pi,
                                       ActionListener onDelete)
    {

        super (viewer,
               pi);

        this.projInfo = pi;
        this.onDelete = onDelete;
               
    }

    public String getDeleteType ()
    {
        
        return Environment.getUIString (LanguageStrings.project,
                                        LanguageStrings.actions,
                                        LanguageStrings.deleteproject,
                                        LanguageStrings.deletetype);
        //"{Project}";
        
    }
        
    public String getWarning ()
    {
        
        String m = Environment.getUIString (LanguageStrings.project,
                                            LanguageStrings.actions,
                                            LanguageStrings.deleteproject,
                                            LanguageStrings.warning,
                                            LanguageStrings.normal);
        //"Warning!  All information/chapters associated with the {project} will be deleted. Once deleted a {project} cannot be restored.";

        if (this.projInfo.isEditorProject ())
        {
            
            m += String.format (Environment.getUIString (LanguageStrings.project,
                                                         LanguageStrings.actions,
                                                         LanguageStrings.deleteproject,
                                                         LanguageStrings.warning,
                                                         LanguageStrings.editor),
                                //"<br /><br />A message will also be sent to <b>%s</b> telling them you are no longer editing the {project}.",
                                this.projInfo.getForEditor ().getShortName ());
        
        }
        
        return m;
        
    }
            
    @Override
    public boolean onConfirm (String v)
                              throws Exception
    {

        final DeleteProjectActionHandler _this = this;
        
        if (this.projInfo.isEditorProject ())
        {
        
            EditorsEnvironment.sendProjectEditStopMessage (this.projInfo,
                                                           new ActionListener ()
            {
              
                @Override
                public void actionPerformed (ActionEvent ev)
                {
        
                    Environment.deleteProject (_this.projInfo,
                                               _this.onDelete);

                    UIUtils.showMessage ((Component) null,
                                         Environment.getUIString (LanguageStrings.project,
                                                                  LanguageStrings.actions,
                                                                  LanguageStrings.deleteproject,
                                                                  LanguageStrings.editorproject,
                                                                  LanguageStrings.confirmpopup,
                                                                  LanguageStrings.title),
                                         String.format (Environment.getUIString (LanguageStrings.project,
                                                                                 LanguageStrings.actions,
                                                                                 LanguageStrings.deleteproject,
                                                                                 LanguageStrings.editorproject,
                                                                                 LanguageStrings.confirmpopup,
                                                                                 LanguageStrings.text),
                                                        _this.projInfo.getForEditor ().getShortName ()),
                                         null,
                                         null);
    
                }
                
            });

        } else {
            
            Environment.deleteProject (this.projInfo,
                                       this.onDelete);            
            
        }
        
        return true;
        
    }
    
}
