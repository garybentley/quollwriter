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


public class NewIdeaTypeActionHandler extends FormAdapter
{

    private JTextField nameField = UIUtils.createTextField ();
    private Form       f = null;
    private IdeaBoard  ideaBoard = null;

    public NewIdeaTypeActionHandler(IdeaBoard ib)
    {

        this.ideaBoard = ib;

    }

    private void initForm ()
    {

        if (this.f == null)
        {

            List items = new ArrayList ();
            items.add (new FormItem ("Name",
                                     this.nameField));

            this.f = new Form ("Add New Idea Type",
                               Environment.getIcon ("idea",
                                                    Constants.ICON_POPUP),
                               items,
                               this.ideaBoard,
                               Form.SAVE_CANCEL_BUTTONS,
                               true);

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

        } else
        {

            this.nameField.setText ("");

        }

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

        if (this.ideaBoard.hasTypeWithName (n))
        {

            UIUtils.showErrorMessage (this.ideaBoard,
                                      "A type called: " +
                                      n +
                                      " already exists.");

            return;

        }

        try
        {

            this.ideaBoard.addNewType (n,
                                       null,
                                       true);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add new idea type with name: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this.ideaBoard,
                                      "An internal error has occurred.\n\nUnable to add new idea type.");

        }

        this.f.hideForm ();

    }

}
