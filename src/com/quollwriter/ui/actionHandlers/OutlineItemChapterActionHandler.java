package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.renderers.*;


public class OutlineItemChapterActionHandler extends ProjectViewerActionHandler
{

    private JTextArea      descField = UIUtils.createTextArea (-1);
    private JCheckBox      addToChapter = new JCheckBox ();
    private Chapter        chapter = null;
    private int            showAt = -1;
    private List<FormItem> formFields = new ArrayList ();

    public OutlineItemChapterActionHandler(OutlineItem         item,
                                           AbstractEditorPanel qep)
    {

        super (item,
               qep,
               AbstractActionHandler.EDIT,
               true);

        this.chapter = item.getChapter ();
        this.showAt = item.getPosition ();

        this.setPopupOver (qep); // pv.getEditorForChapter (this.chapter));

        this.initFormItems ();

    }

    public OutlineItemChapterActionHandler(Chapter             c,
                                           AbstractEditorPanel qep,
                                           int                 showAt)
    {

        super (new OutlineItem (showAt,
                                c),
               qep,
               AbstractActionHandler.ADD,
               true);

        this.chapter = c;

        this.setPopupOver (qep);

        this.initFormItems ();

        this.showAt = showAt;

    }

    public int getShowAtPosition ()
    {

        return this.showAt;

    }

    public JTextArea getFocussedField ()
    {

        return this.descField;

    }

    public String getIcon (int mode)
    {

        return OutlineItem.OBJECT_TYPE;

    }

    public String getTitle (int mode)
    {

        if (mode == AbstractActionHandler.EDIT)
        {

            return "Edit Plot Outline Item";

        }

        return "Add New Plot Outline Item";

    }

    private void initFormItems ()
    {

        this.descField.setRows (5);
        this.descField.setLineWrap (true);
        this.descField.setWrapStyleWord (true);

        this.addToChapter.setText (Environment.replaceObjectNames ("Add the description to the {Chapter}"));

        boolean sel = true;

        if (this.projectViewer.hasTempOption ("addToChapter"))
        {
            
            sel = this.projectViewer.isTempOption ("addToChapter");

        }

        this.addToChapter.setSelected (sel);

        final OutlineItemChapterActionHandler _this = this;

        this.descField.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) &&
                        (ev.getKeyCode () == KeyEvent.VK_ENTER))
                    {

                        // This is the same as save for the form.
                        _this.submitForm ();

                    }

                }

            });

    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

        OutlineItem it = (OutlineItem) obj;

        List<FormItem> f = new ArrayList ();

        f.add (new FormItem ("Description",
                             new JScrollPane (this.descField)));

        if (mode == AbstractActionHandler.ADD)
        {

            f.add (new FormItem ("",
                                 this.addToChapter));

            if ((selectedText != null)
                &&
                (selectedText.trim ().length () > 0)
               )
            {

                this.addToChapter.setSelected (false);

                this.descField.setText (selectedText);

            }

        } else
        {

            this.descField.setText (it.getDescription ());

        }

        return f;

    }

    public boolean handleSave (int mode)
    {

        if (this.descField.getText ().trim ().equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a description.");

            return false;

        }

        OutlineItem it = (OutlineItem) this.dataObject;

        // Fill up the outline item.
        it.setDescription (this.descField.getText ().trim ());

        // QuollEditorPanel qep = this.projectViewer.getEditorForChapter (this.chapter);

        this.projectViewer.setTempOption ("addToChapter",
                                          this.addToChapter.isSelected ());

        QTextEditor editor = this.editorPanel.getEditor ();

        if (this.mode == AbstractActionHandler.ADD)
        {

            try
            {

                if (this.addToChapter.isSelected ())
                {

                    String d = it.getDescription ();

                    if ((d != null) &&
                        (d.trim ().equals ("")))
                    {

                        d = null;

                        it.setDescription (null);

                    } else
                    {

                        String toAdd = it.getDescription () + "\n";

                        // We need to append the text.
                        editor.insertText (it.getPosition (),
                                           toAdd);

                        // Need to update the text position because it will have moved.
                        it.setTextPosition (editor.getDocument ().createPosition (it.getPosition () - toAdd.length ()));

                    }

                }

                Scene s = this.chapter.getLastScene (it.getPosition ());

                if (s != null)
                {

                    s.addOutlineItem (it);

                } else
                {

                    this.chapter.addOutlineItem (it);

                }

                this.projectViewer.saveObject (it,
                                               true);

                //this.projectViewer.addChapterItemToChapterTree (it);

                this.projectViewer.fireProjectEvent (it.getObjectType (),
                                                     ProjectEvent.NEW,
                                                     it);

            } catch (Exception e)
            {

                Environment.logError ("Unable to add new plot outline item",
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to add new plot outline item.");

                return false;

            }

        } else
        {
        
            try
            {

                this.projectViewer.saveObject (it,
                                               true);

                this.projectViewer.fireProjectEvent (it.getObjectType (),
                                                     ProjectEvent.EDIT,
                                                     it);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save plot outline item",
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to save " + Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE).toLowerCase ());

                return false;

            }

        }

        if (it.getChapter () != null)
        {

            editor.grabFocus ();

        }

        // Need to repaint so that tree doesn't truncate the field for name extensions.
        this.projectViewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

        editor.grabFocus ();

        return true;

    }

}
