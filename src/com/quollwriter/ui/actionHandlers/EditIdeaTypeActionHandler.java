package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.events.*;


public class EditIdeaTypeActionHandler extends TextInputActionHandler
{

    private IdeaBoard  ideaBoard = null;
    private IdeaType   ideaType = null;

    public EditIdeaTypeActionHandler (IdeaType  it,
                                      IdeaBoard ib)
    {

        super (ib.getProjectViewer ());
    
        this.ideaBoard = ib;
        this.ideaType = it;

    }

    public String getIcon ()
    {
        
        return Constants.EDIT_ICON_NAME;
        
    }
    
    public String getTitle ()
    {
        
        return "Edit Idea Type";
        
    }
    
    public String getHelp ()
    {
        
        return "Enter the new name of the Idea type below.";
        
    }
    
    public String getConfirmButtonLabel ()
    {
        
        return "Change";
        
    }
    
    public String getInitialValue ()
    {
        
        return this.ideaType.getName ();
        
    }
    
    public String isValid (String v)
    {
        
        if ((v == null)
            ||
            (v.trim ().length () == 0)
           )
        {
            
            return "Please enter a name.";
            
        }
        
        List<IdeaType> its = this.projectViewer.getProject ().getIdeaTypes ();

        for (IdeaType it : its)
        {
            
            if ((it.getName ().equalsIgnoreCase (v))
                &&
                (it != this.ideaType)
               )
            {
                
                return "Already have an Idea Type called: " + it.getName ();
                
            }
            
        }
        
        return null;
    
    }
    
    public boolean onConfirm (String v)
                              throws Exception
    {
        
        this.ideaType.setName (v.trim ());

        try
        {

            this.ideaBoard.updateIdeaType (this.ideaType);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to add update idea type with name: " +
                                        v,
                                        e);

        }
        
        return true;
        
    }
    
    public boolean onCancel ()
                             throws Exception
    {
        
        return true;
        
    }
    
    public Point getShowAt ()
    {
        
        return null;
        
    }
    /*
    private void initForm ()
    {

        if (this.f == null)
        {

            List items = new ArrayList ();
            items.add (new FormItem ("Name",
                                     this.nameField));

            this.f = new Form ("Edit Idea type name",
                               Environment.getIcon ("idea",
                                                    Constants.ICON_POPUP),
                               items,
                               this.ideaBoard,
                               Form.SAVE_CANCEL_BUTTONS);

            f.addFormListener (this);

            final Form form = this.f;

            this.nameField.addKeyListener (new KeyAdapter ()
                {

                    public void keyPressed (KeyEvent ev)
                    {

                        if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                        {

                            // This is the same as save for the form.
                            form.fireFormEvent (FormEvent.SAVE,
                                                FormEvent.SAVE_ACTION_NAME);

                        }

                    }

                });

        }

        this.nameField.setText (this.ideaType.getName ());

    }

    public void actionPerformed (ActionEvent ev)
    {

        this.initForm ();

        this.ideaBoard.showPopupAt (this.f,
                                    new Point (300,
                                               100));

        this.nameField.grabFocus ();

        this.nameField.selectAll ();

    }

    public void actionPerformed (FormEvent ev)
    {

        if (ev.getID () != FormEvent.SAVE)
        {

            return;

        }

        String n = this.nameField.getText ().trim ();

        if (n.equals (""))
        {

            UIUtils.showErrorMessage (this.ideaBoard,
                                      "Please select a name.");

            return;

        }

        this.ideaType.setName (n);

        try
        {

            this.ideaBoard.updateIdeaType (this.ideaType);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add update idea type with name: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this.ideaBoard,
                                      "An internal error has occurred.\n\nUnable to update Idea Type.");

        }

        this.f.hideForm ();

    }
*/
}
