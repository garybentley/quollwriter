package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;
import com.quollwriter.ui.components.FormItem;


public class AddNewNoteTypeActionHandler extends FormAdapter
{

    private JTextField            nameField = UIUtils.createTextField ();
    private Form                  f = null;
    private Project               project = null;
    private ProjectViewer projectViewer = null;

    public AddNewNoteTypeActionHandler (ProjectViewer pv)
    {

        this.project = pv.getProject ();
        this.projectViewer = pv;

    }

    private void initForm ()
    {

        List items = new ArrayList ();
        items.add (new FormItem ("Name",
                                 this.nameField));

        this.f = new Form ("Add New Note Type",
                           Environment.getIcon ("add",
                                                Constants.ICON_POPUP),
                           items,
                           this.projectViewer,
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

    public void actionPerformed (ActionEvent ev)
    {

        this.initForm ();

        this.f.setBounds (200,
                          200,
                          this.f.getPreferredSize ().width,
                          this.f.getPreferredSize ().height);

        this.projectViewer.addPopup (this.f);

        this.f.setVisible (true);

        this.nameField.grabFocus ();

        this.nameField.selectAll ();

    }

    public void actionPerformed (FormEvent ev)
    {

        if (ev.getID () != FormEvent.SAVE)
        {

            return;

        }

        String newName = this.nameField.getText ().trim ();

        if (newName.equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            return;

        }

        this.projectViewer.getNoteTypeHandler ().addType (newName,
                                                          false);

        this.f.hideForm ();

    }

}
