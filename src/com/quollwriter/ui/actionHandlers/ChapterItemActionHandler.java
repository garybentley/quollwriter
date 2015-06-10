package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.panels.*;

public class ChapterItemActionHandler extends ProjectViewerActionHandler
{

    private JTextArea      descField = UIUtils.createTextArea (-1);
    private JCheckBox      addToChapter = new JCheckBox ();
    private Chapter        chapter = null;
    private int            showAt = -1;
    private List<FormItem> formFields = new ArrayList ();

    public ChapterItemActionHandler(ChapterItem         item,
                                    AbstractEditorPanel qep,
                                    int                 mode,
                                    int                 showAt)
    {

        super (item,
               qep,
               mode,
               true);

        this.chapter = item.getChapter ();

        if (mode == AbstractActionHandler.EDIT)
        {

            this.showAt = item.getPosition ();

        } else
        {

            this.showAt = showAt;

        }

        this.setPopupOver (qep); // pv.getEditorForChapter (this.chapter));

        this.initFormItems ();

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

        return this.dataObject.getObjectType ();

    }

    public String getTitle (int mode)
    {

        if (mode == AbstractActionHandler.EDIT)
        {

            return "Edit " + Environment.getObjectTypeName (this.dataObject.getObjectType ());

        }

        return "Add New " + Environment.getObjectTypeName (this.dataObject.getObjectType ());

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

        final ChapterItemActionHandler _this = this;

        ActionListener doSave = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.submitForm ();
                
            }
            
        };

        UIUtils.addDoActionOnReturnPressed (this.descField,
                                            doSave);
        
    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

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

                this.descField.setText (selectedText);
                this.addToChapter.setSelected (false);
                
            }

        } else
        {

            ChapterItem it = (ChapterItem) obj;

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

        ChapterItem it = (ChapterItem) this.dataObject;

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
                        it.setTextPosition (editor.getDocument ().createPosition (it.getPosition () - toAdd.length () + 1));

                    }

                }

                // See if we are adding at the end of the chapter.
                if (editor.isPositionAtTextEnd (it.getPosition ()))
                {
                    
                    // Add a newline to the end of the chapter.
                    editor.insertText (it.getPosition (),
                                       "\n");

                    it.setTextPosition (editor.getDocument ().createPosition (it.getPosition () - 1));
                    
                }
                
                if (it.getObjectType ().equals (Scene.OBJECT_TYPE))
                {

                    Set<OutlineItem> oitems = this.chapter.getItemsFromPositionToNextScene (it.getPosition ());

                    Scene s = (Scene) it;

                    // Change the scene for each item.
                    for (OutlineItem i : oitems)
                    {

                        s.addOutlineItem (i);

                    }

                }

                // Add the item to the chapter.
                this.chapter.addChapterItem (it);

                // Force a save of the chapter.
                this.projectViewer.saveObject (this.chapter,
                                               true);
                                               
                this.projectViewer.fireProjectEvent (it.getObjectType (),
                                                     ProjectEvent.NEW,
                                                     it);                                               

            } catch (Exception e)
            {

                Environment.logError ("Unable to add new chapter item",
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to add new " + Environment.getObjectTypeName (this.dataObject.getObjectType ()) + ".");

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

                Environment.logError ("Unable to save item: " +
                                      it,
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to save " + Environment.getObjectTypeName (this.dataObject.getObjectType ()).toLowerCase () + ".");

                return false;

            }

        }

        // Reload the entire tree.
        this.projectViewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
        
        editor.grabFocus ();

        return true;

    }

}
