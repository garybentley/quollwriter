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
import com.quollwriter.ui.components.FormItem;


public class AddChapterActionHandler extends AbstractActionHandler
{

    private JTextField    nameField = UIUtils.createTextField ();
    private Form          f = null;
    private ProjectViewer projectViewer = null;
    private Chapter       addAfter = null;
    private Book          book = null;

    public AddChapterActionHandler(Book          b,
                                   Chapter       addAfter,
                                   ProjectViewer pv)
    {

        super (new Chapter (b,
                            null),
               pv,
               AbstractActionHandler.ADD,
               true);

        this.projectViewer = pv;
        this.book = b;
        this.addAfter = addAfter;

        final AddChapterActionHandler _this = this;

        this.nameField.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                    {

                        _this.submitForm ();

                    }

                }

            });

    }

    public void handleCancel (int mode)
    {

        // Nothing to do.

    }

    public boolean handleSave (int mode)
    {

        String n = this.nameField.getText ().trim ();

        if (n.equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            return false;

        }

        try
        {

            this.dataObject = this.book.createChapterAfter (this.addAfter,
                                                            this.nameField.getText ());

            this.projectViewer.saveObject (this.dataObject,
                                           true);

            this.projectViewer.fireProjectEvent (this.dataObject.getObjectType (),
                                                 ProjectEvent.NEW,
                                                 this.dataObject);
                                                                                      
        } catch (Exception e)
        {

            Environment.logError ("Unable to add new chapter with name: " +
                                  this.nameField.getText (),
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to add new " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE) + ".");

            return false;

        }

        Chapter nc = (Chapter) this.dataObject;

        try
        {

            this.projectViewer.editChapter (nc);

            this.projectViewer.addChapterToTreeAfter (nc,
                                                      this.addAfter);

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  nc,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to edit " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE) + ".");

            return false;

        }

        return true;

    }

    public String getTitle (int mode)
    {

        return "Add New {Chapter}";

    }

    public String getIcon (int mode)
    {

        return Chapter.OBJECT_TYPE + "-add";

    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

        List<FormItem> items = new ArrayList ();
        items.add (new FormItem ("Name",
                                 this.nameField));

        return items;

    }

    public JComponent getFocussedField ()
    {

        return this.nameField;

    }

    public int getShowAtPosition ()
    {

        return -1;

    }

}
