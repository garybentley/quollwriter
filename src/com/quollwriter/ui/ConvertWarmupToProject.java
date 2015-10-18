package com.quollwriter.ui;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;

/**
 * TODO: Remove, no longer used.
 */
public class ConvertWarmupToProject extends PopupWindow
{

    private String          text = null;
    private Warmup          warmup = null;
    private NewProjectPanel newProjectPanel = null;
    private AbstractEditorPanel panel = null;

    public ConvertWarmupToProject(AbstractProjectViewer pv,
                                  Warmup                w,
                                  AbstractEditorPanel   pan)
    {

        super (pv);

        this.panel = pan;
        //this.text = text;
        this.warmup = w;

        final ConvertWarmupToProject _this = this;
        
        this.newProjectPanel = new NewProjectPanel ()
        {
          
            public boolean createProject (Component parent)
            {
                
                if (!_this.newProjectPanel.checkForm (_this))
                {

                    return false;

                }

                ProjectViewer pj = new ProjectViewer ();

                Project p = new Project (_this.newProjectPanel.getName ());

                Book b = new Book (p,
                                   p.getName ());

                // Create a new chapter for the book.
                Chapter c = new Chapter (b,
                                         Environment.getProperty (Constants.DEFAULT_CHAPTER_NAME_PROPERTY_NAME));

                b.addChapter (c);

                c.setName (_this.warmup.getChapter ().getName ());
                c.setText (_this.panel.getEditor ().getTextWithMarkup ());

                p.addBook (b);

                String pwd = _this.newProjectPanel.getPassword ();

                try
                {

                    pj.newProject (_this.newProjectPanel.getSaveDirectory (),
                                   p,
                                   pwd);

                    pj.createActionLogEntry (p,
                                             "Project created from warmup, prompt id: " +
                                             _this.warmup.getPrompt ().getId () +
                                             " and chapter: " +
                                             _this.warmup.getChapter ().getName () +
                                             " (" +
                                             _this.warmup.getChapter ().getKey () +
                                             ")");

                } catch (Exception e)
                {

                    Environment.logError ("Unable to create new project: " +
                                          p,
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to create new project: " + p.getName ());

                    return false;

                }
                
                return true;
                
            }
            
        };

    }

    public String getWindowTitle ()
    {

        return "Convert Warm-up to Project";

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return Project.OBJECT_TYPE;

    }

    public String getHelpText ()
    {

        return "To create the new {Project} enter the name below, select the directory it should be saved to and press the Create button.  Your warm-up text will be added as a {chapter} to the {Project}.";

    }

    public JComponent getContentPanel ()
    {

        final ConvertWarmupToProject _this = this;    
    
        return this.newProjectPanel.createPanel (this,
                                                 null,
                                                 true,
                                                 this.getCloseAction (),
                                                 true);

    }

    public JButton[] getButtons ()
    {

        return null;

    }

}
