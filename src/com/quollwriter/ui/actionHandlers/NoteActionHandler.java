package com.quollwriter.ui.actionHandlers;

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
import com.quollwriter.text.*;
import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.renderers.*;

public class NoteActionHandler extends ProjectViewerActionHandler
{

    private JTextField summaryField = null;
    private TextArea descField = null;    
    protected Chapter    chapter = null;
    protected int        showAt = -1;
    private JComboBox  types = null;

    public NoteActionHandler (final Note                n,
                              final AbstractEditorPanel qep)
    {

        super (n,
               qep,
               AbstractActionHandler.EDIT,
               true);

        this.chapter = n.getChapter ();
        this.showAt = n.getPosition ();

        this.initFormItems ();

        this.setPopupOver (qep); 
        
        final QTextEditor editor = qep.getEditor ();        
        
        final int origSelStart = editor.getSelectionStart ();
        
        final BlockPainter highlight = new BlockPainter (Environment.getHighlightColor ());                
        
        final Caret origCaret = editor.getCaret ();
        
        this.setOnShowAction (new ActionListener ()
        {
                      
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                            
                editor.setCaret (new DefaultCaret ()
                {
                   
                    private boolean isVis = false;
                   
                    @Override
                    public void setSelectionVisible (boolean vis)
                    {
                        
                        editor.removeAllHighlights (highlight);
                        
                        if (vis != this.isVis) {
                            this.isVis = vis;
                            super.setSelectionVisible(false);
                            super.setSelectionVisible(true);
                        }                        
                        
                    }
                    
                });                        
                                
                if (n.getEndPosition () > n.getStartPosition ())
                {
                    
                    editor.addHighlight (n.getStartPosition (),
                                         n.getEndPosition (),
                                         highlight,
                                         false);
                    
                }                        

            }
            
        });
        
        this.setOnHideAction (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
            
                editor.removeAllHighlights (highlight);
                
                editor.setCaret (origCaret);

                editor.setSelectionStart (origSelStart);
                
                editor.grabFocus ();
                
            }
            
        });
        
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

        this.summaryField = UIUtils.createTextField ();
        
        this.descField = UIUtils.createTextArea (this.projectViewer,
                                                 null,
                                                 5,
                                                 -1);

        this.descField.setCanFormat (true);
        this.descField.setAutoGrabFocus (false);
        
        try
        {
        
            this.descField.setSynonymProvider (this.projectViewer.getSynonymProvider ());
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to set synonym provider for note edit: " +
                                  this.dataObject,
                                  e);
            
        }
        
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
        
        this.types = new JComboBox (new Vector (Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).getTypes ()));
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
                             this.descField));

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

                Paragraph p = new Paragraph (selectedText,
                                             0);
                
                this.summaryField.setText (p.getFirstSentence ().getText ());

                if (p.getSentenceCount () > 1)
                {

                    this.descField.setTextWithMarkup (new StringWithMarkup (selectedText.substring (p.getFirstSentence ().getNext ().getAllTextStartOffset ()).trim ()));

                }

            }

        } else
        {

            Note n = (Note) obj;

            this.summaryField.setText (n.getSummary ());
            this.descField.setTextWithMarkup (n.getDescription ());

        }

        return f;

    }

    public boolean handleSave (Form f,
                               int  mode)
    {

        Note n = (Note) this.dataObject;

        if (n.isEditNeeded ())
        {

            String text = this.descField.getText ();

            if ((text == null)
                ||
                (text.trim ().length () == 0)
               )
            {
                
                f.showError ("Please enter a description.");
                
                return false;
                
            }
            
            Paragraph p = new Paragraph (text,
                                         0);
            
            n.setSummary (p.getFirstSentence ().getText ());

        } else
        {

            if (this.summaryField.getText ().trim ().equals (""))
            {

                f.showError ("Please enter a summary.");

                return false;

            }

            // Fill up the note.
            n.setSummary (this.summaryField.getText ().trim ());

        }

        n.setDescription (this.descField.getTextWithMarkup ());

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

        // See if we are adding at the end of the chapter.
        if (this.editorPanel.getEditor ().isPositionAtTextEnd (n.getPosition ()))
        {
            
            try
            {

                // Add a newline to the end of the chapter.
                this.editorPanel.getEditor ().insertText (n.getPosition (),
                                                          "\n");

                n.setTextPosition (this.editorPanel.getEditor ().getDocument ().createPosition (n.getPosition () - 1));
                                                          
            } catch (Exception e) {
                
                Environment.logError ("Unable to insert newline at end of chapter",
                                      e);
                
            }
            
        }
        
        try
        {

            if (this.mode == AbstractActionHandler.ADD)
            {
    
                // Add the item to the chapter.
                this.chapter.addNote (n);
    
            }

            this.projectViewer.saveObject (n,
                                           true);

        } catch (Exception e)
        {

            this.chapter.removeNote (n);
        
            Environment.logError ("Unable to save/add note: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to " + ((this.mode == AbstractActionHandler.ADD) ? "add new " : "save") + Environment.getObjectTypeName (Note.OBJECT_TYPE).toLowerCase () + ".");

            return false;

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

        // Need to reindex the chapter to ensure that things are in the right order.    
        n.getChapter ().reindex ();
        
        Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).addType (type,
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
