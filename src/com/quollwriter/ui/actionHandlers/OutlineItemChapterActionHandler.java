package com.quollwriter.ui.actionHandlers;

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
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.renderers.*;


public class OutlineItemChapterActionHandler extends ProjectViewerActionHandler
{

    private TextArea descField = null;    
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

    public JComponent getFocussedField ()
    {

        return this.descField;

    }

    public String getIcon (int mode)
    {

        return OutlineItem.OBJECT_TYPE;

    }

    public String getTitle (int mode)
    {

        String t = "Add New {%s}";
    
        if (mode == AbstractActionHandler.EDIT)
        {

            t = "Edit {%s}";

        }
        
        return String.format (t,
                              OutlineItem.OBJECT_TYPE);

    }

    private void initFormItems ()
    {

        this.descField = UIUtils.createTextArea (this.projectViewer,
                                                 "Describe the item here",
                                                 5,
                                                 -1);
        this.descField.setCanFormat (true);
                                       
        this.addToChapter.setText (Environment.replaceObjectNames ("Add the description to the {Chapter}"));

        boolean sel = true;

        if (this.projectViewer.hasTempOption ("addToChapter"))
        {
            
            sel = this.projectViewer.isTempOption ("addToChapter");

        }

        this.addToChapter.setSelected (sel);

        final OutlineItemChapterActionHandler _this = this;

        ActionListener doSave = new ActionListener ()
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

        OutlineItem it = (OutlineItem) obj;

        List<FormItem> f = new ArrayList ();

        f.add (new FormItem ("Description",
                             this.descField));

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

                this.descField.setTextWithMarkup (new StringWithMarkup (selectedText));

            }

        } else
        {

            this.descField.setTextWithMarkup (it.getDescription ());

        }

        return f;

    }

    @Override
    public boolean handleSave (Form f,
                               int  mode)
    {

        if (this.descField.getText ().trim ().equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a description.");

            return false;

        }

        OutlineItem it = (OutlineItem) this.dataObject;

        // Fill up the outline item.
        it.setDescription (this.descField.getTextWithMarkup ());

        // QuollEditorPanel qep = this.projectViewer.getEditorForChapter (this.chapter);

        this.projectViewer.setTempOption ("addToChapter",
                                          this.addToChapter.isSelected ());

        QTextEditor editor = this.editorPanel.getEditor ();

        if (this.mode == AbstractActionHandler.ADD)
        {

            try
            {

                String d = it.getDescriptionText ();
            
                if (this.addToChapter.isSelected ())
                {

                    if ((d != null) &&
                        (d.trim ().equals ("")))
                    {

                        d = null;

                        it.setDescription (null);

                    } else
                    {

                        if ((d != null)
                            &&
                            (!d.trim ().equals (""))
                           )
                        {
                    
                            String toAdd = d + "\n";

                            // We need to append the text.
                            editor.insertText (it.getPosition (),
                                               toAdd);
    
                            // Need to update the text position because it will have moved.
                            it.setTextPosition (editor.getDocument ().createPosition (it.getPosition () - toAdd.length ()));

                        }
                            
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
                                          "An internal error has occurred.\n\nUnable to add new {outlineitem}.");

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
