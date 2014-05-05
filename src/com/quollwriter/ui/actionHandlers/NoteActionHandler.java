package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.renderers.*;


public class NoteActionHandler extends ProjectViewerActionHandler
{

    private JTextField summaryField = UIUtils.createTextField ();
    private JTextArea  descField = UIUtils.createTextArea (-1);
    private Chapter    chapter = null;
    private int        showAt = -1;
    private JComboBox  types = null;

    public NoteActionHandler(Note                n,
                             AbstractEditorPanel qep)
    {

        super (n,
               qep,
               AbstractActionHandler.EDIT,
               true);

        this.chapter = n.getChapter ();
        this.showAt = n.getPosition ();

        this.initFormItems ();

        this.setPopupOver (qep); // pv.getEditorForChapter (this.chapter));

    }

    public NoteActionHandler(Chapter             c,
                             AbstractEditorPanel qep,
                             int                 showAt)
    {

        this (c,
              qep,
              null,
              showAt);

        this.setPopupOver (qep); // pv.getEditorForChapter (c));

    }

    public NoteActionHandler(Chapter             c,
                             AbstractEditorPanel qep,
                             String              noteType,
                             int                 showAt)
    {

        super (new Note (0,
                         c),
               qep,
               AbstractActionHandler.ADD,
               true);

        this.showAt = showAt;
               
        this.chapter = c;

        this.setPopupOver (qep); // pv.getEditorForChapter (c));

        if (noteType != null)
        {

            Note n = (Note) this.dataObject;
            n.setType (noteType);

        }

        this.initFormItems ();

    }

    public int getShowAtPosition ()
    {

        return this.showAt;

    }

    public JComponent getFocussedField ()
    {

        if (((Note) this.dataObject).isEditNeeded ())
        {

            return this.descField;

        }

        return this.summaryField;

    }

    public String getIcon (int mode)
    {

        if (((Note) this.dataObject).isEditNeeded ())
        {

            return "edit-needed-note";

        }

        return Note.OBJECT_TYPE;

    }

    public String getTitle (int mode)
    {

        if (mode == AbstractActionHandler.EDIT)
        {

            return "Edit Note";

        }

        boolean editNeeded = ((Note) this.dataObject).isEditNeeded ();

        if (editNeeded)
        {

            return "Add New " + Note.EDIT_NEEDED_NOTE_TYPE + " Note";

        }

        return "Add New Note";

    }

    private void initFormItems ()
    {

        final NoteActionHandler _this = this;

        ActionListener doSave = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.submitForm ();
                
            }
            
        };
        
        UIUtils.addDoActionOnReturnPressed (this.summaryField,
                                            doSave);
        UIUtils.addDoActionOnReturnPressed (this.descField,
                                            doSave);
        
        this.descField.setRows (5);

        this.types = new JComboBox (new Vector (this.projectViewer.getObjectTypesHandler (Note.OBJECT_TYPE).getTypes ()));
        this.types.setEditable (true);

        this.types.setMaximumSize (this.types.getPreferredSize ());
        this.types.setToolTipText ("Add a new Type by entering a value in the field.");

        Note n = (Note) this.dataObject;

        if (n.getType () != null)
        {

            this.types.setSelectedItem (n.getType ());

        }

    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

        List<FormItem> f = new ArrayList ();

        boolean editNeeded = ((Note) obj).isEditNeeded ();

        if (!editNeeded)
        {

            f.add (new FormItem ("Summary",
                                 this.summaryField));

        }

        f.add (new FormItem ("Description",
                             new JScrollPane (this.descField)));

        if (!editNeeded)
        {

            Box tb = new Box (BoxLayout.X_AXIS);
            tb.add (this.types);
            tb.add (Box.createHorizontalGlue ());

            DefaultComboBoxModel m = (DefaultComboBoxModel) this.types.getModel ();
            
            m.removeElement (Note.EDIT_NEEDED_NOTE_TYPE);

            f.add (new FormItem ("Type",
                                 tb));

        }

        if (mode == AbstractActionHandler.ADD)
        {

            if ((selectedText != null) &&
                (selectedText.trim ().length () > 0) &&
                (!editNeeded))
            {

                BreakIterator bi = BreakIterator.getSentenceInstance ();
                bi.setText (selectedText);

                int s = bi.first ();
                int e = bi.next ();

                this.summaryField.setText (selectedText.substring (s,
                                                                   e).trim ());

                if (e < selectedText.length ())
                {

                    this.descField.setText (selectedText.substring (e).trim ());

                }

            }

        } else
        {

            Note n = (Note) obj;

            this.summaryField.setText (n.getSummary ());
            this.descField.setText (n.getDescription ());

        }

        return f;

    }

    public boolean handleSave (int mode)
    {

        Note n = (Note) this.dataObject;

        if (n.isEditNeeded ())
        {

            String text = this.descField.getText ().trim ();

            // Use the first line of the description as the summary.
            BreakIterator bi = BreakIterator.getSentenceInstance ();
            bi.setText (text);

            int s = bi.first ();
            int e = bi.next ();

            n.setSummary (text.substring (s,
                                          e));

        } else
        {

            if (this.summaryField.getText ().trim ().equals (""))
            {

                UIUtils.showErrorMessage (this.projectViewer,
                                          "Please enter a summary.");

                return false;

            }

            // Fill up the note.
            n.setSummary (this.summaryField.getText ().trim ());

        }

        n.setDescription (this.descField.getText ().trim ());

        String type = null;

        if (!n.isEditNeeded ())
        {

            type = this.types.getSelectedItem ().toString ().trim ();

            n.setType (type);

        } else
        {

            type = Note.EDIT_NEEDED_NOTE_TYPE;

        }

        // If the type is "edit needed" then get any selectd text indices.
        if (n.isEditNeeded ())
        {

            int s = this.editorPanel.getEditor ().getSelectionStart ();
            int e = this.editorPanel.getEditor ().getSelectionEnd ();
        
            if ((mode == AbstractActionHandler.EDIT)
                &&
                (s != e)
                &&
                (e > s)
               )
            {
        
                n.setPosition (s);
                n.setEndPosition (e);

            }
            
            if (mode == AbstractActionHandler.ADD)
            {
                
                n.setPosition (s);
                n.setEndPosition (e);                
                
            }
            
        }

        try
        {

            this.projectViewer.saveObject (n,
                                           true);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save/add note: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to " + ((this.mode == AbstractActionHandler.ADD) ? "add new " : "save") + Environment.getObjectTypeName (Note.OBJECT_TYPE).toLowerCase () + ".");

            return false;

        }

        if (this.mode == AbstractActionHandler.ADD)
        {

            // Add the item to the chapter.
            this.chapter.addNote (n);

        }
        
        if (n.isEditNeeded ())
        {
            
            try
            {

                Position pos = this.editorPanel.getEditor ().getDocument ().createPosition (n.getPosition ());
    
                n.setTextPosition (pos);            

                if (n.getEndPosition () > -1)
                {
    
                    n.setEndTextPosition (this.editorPanel.getEditor ().getDocument ().createPosition (n.getEndPosition ()));
    
                }                
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to set text position",
                                      e);
                
            }
            
        }

        if (n.getChapter () != null)
        {

            QTextEditor editor = this.editorPanel.getEditor ();

            editor.grabFocus ();

        }

        this.projectViewer.getObjectTypesHandler (Note.OBJECT_TYPE).addType (type,
                                                                             true);

        // Expand the note type.
        this.projectViewer.showObjectInTree (Note.OBJECT_TYPE,
                                             new TreeParentNode (Note.OBJECT_TYPE,
                                                                 type));
                                             
        this.projectViewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

        this.projectViewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
        
        return true;

    }

}
