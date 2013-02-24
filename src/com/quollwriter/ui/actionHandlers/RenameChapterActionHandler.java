package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

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
import com.quollwriter.ui.events.*;


public class RenameChapterActionHandler extends FormAdapter
{

    private JTextField            nameField = UIUtils.createTextField ();
    private Form                  f = null;
    private Chapter               chapter = null;
    private AbstractProjectViewer projectViewer = null;

    public RenameChapterActionHandler(Chapter               c,
                                      AbstractProjectViewer pv)
    {

        this.chapter = c;
        this.projectViewer = pv;

    }

    private void initForm ()
    {

        List items = new ArrayList ();
        items.add (new FormItem ("Name",
                                 this.nameField));

        this.f = new Form ("Rename Chapter",
                           Environment.getIcon (Chapter.OBJECT_TYPE + "-edit",
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

        Point p = this.projectViewer.getMousePosition ();
        SwingUtilities.convertPointToScreen (p,
                                             this.projectViewer);

        this.f.setBounds (p.x,
                          p.y,
                          this.f.getPreferredSize ().width,
                          this.f.getPreferredSize ().height);

        this.projectViewer.addPopup (this.f);

        this.f.setVisible (true);

        this.nameField.setText (this.chapter.getName ());

        this.nameField.grabFocus ();

        this.nameField.selectAll ();

    }

    public void actionPerformed (FormEvent ev)
    {

        if (ev.getID () != FormEvent.SAVE)
        {

            return;

        }

        if (this.nameField.getText ().trim ().equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            return;

        }

        try
        {

            this.chapter.setName (this.nameField.getText ());

            this.projectViewer.saveObject (this.chapter,
                                           true);

        } catch (Exception e)
        {

            Environment.logError ("Unable to change name of chapter: " +
                                  this.chapter +
                                  " to: " +
                                  this.nameField.getText (),
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to change name of chapter.");

        }

        // Inform the chapter tree that something has changed.
        this.projectViewer.handleItemChangedEvent (new ItemChangedEvent (this,
                                                                         this.chapter,
                                                                         AbstractProjectViewer.NAME_CHANGED));

        this.f.hideForm ();

    }

}
