package com.quollwriter.ui.actionHandlers;

import java.awt.event.*;
import java.awt.font.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.renderers.*;

public class NoteActionHandler extends ChapterItemActionHandler<Note> 
{

    private TextFormItem summaryField = null;
    private MultiLineTextFormItem descField = null;
    private JComboBox  types = null;

    public NoteActionHandler (final Note              n,
                              final ChapterItemViewer viewer)
    {
    
        super (n,
               viewer,
               EDIT,
               n.getPosition ());        
        
        final QTextEditor editor = this.itemViewer.getEditor ();        
        
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

    public NoteActionHandler(Chapter           c,
                             ChapterItemViewer itemViewer,
                             int               showAt)
    {

        this (c,
              itemViewer,
              null,
              showAt);

    }

    public NoteActionHandler(Chapter           c,
                             ChapterItemViewer itemViewer,
                             String            noteType,
                             int               showAt)
    {

        super (new Note (0,
                         c),
               itemViewer,
               ADD,
               showAt);

        this.chapter = c;

        if (noteType != null)
        {

            Note n = this.object;
            n.setType (noteType);

        }

    }

    @Override
    public JComponent getFocussedField ()
    {

        if (this.object.isEditNeeded ())
        {

            return this.descField;

        }

        return this.summaryField;

    }

    @Override
    public Icon getIcon (int iconSizeType)
    {

        String t = Note.OBJECT_TYPE;
    
        if (this.object.isEditNeeded ())
        {

            t = "edit-needed-note";

        }

        return Environment.getIcon (t,
                                    iconSizeType);

    }

    @Override
    public String getTitle ()
    {

        if (this.mode == AbstractActionHandler.EDIT)
        {

            return "Edit Note";

        }

        boolean editNeeded = this.object.isEditNeeded ();

        if (editNeeded)
        {

            return "Add New " + Note.EDIT_NEEDED_NOTE_TYPE + " Note";

        }

        return "Add New Note";

    }

    @Override
    public Set<FormItem> getFormItems (String selectedText)
    {

        Set<FormItem> f = new LinkedHashSet ();

        final NoteActionHandler _this = this;

        this.summaryField = new TextFormItem ("Summary",
                                              null);
        
        this.descField = new MultiLineTextFormItem ("Description",
                                                    this.viewer,
                                                    5);
        
        this.descField.setCanFormat (true);
        
        UIUtils.addDoActionOnReturnPressed (this.summaryField.getTextField (),
                                            this.getSaveAction ());
        UIUtils.addDoActionOnReturnPressed (this.descField.getTextArea (),
                                            this.getSaveAction ());
        
        this.types = new JComboBox (new Vector (Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).getTypes ()));
        this.types.setEditable (true);

        this.types.setMaximumSize (this.types.getPreferredSize ());
        this.types.setToolTipText ("Add a new Type by entering a value in the field.");

        if (this.object.getType () != null)
        {

            this.types.setSelectedItem (this.object.getType ());

        }        
        
        boolean editNeeded = this.object.isEditNeeded ();

        if (!editNeeded)
        {

            f.add (this.summaryField);

        }

        f.add (this.descField);

        if (!editNeeded)
        {

            Box tb = new Box (BoxLayout.X_AXIS);
            tb.add (this.types);
            tb.add (Box.createHorizontalGlue ());

            DefaultComboBoxModel m = (DefaultComboBoxModel) this.types.getModel ();
            
            m.removeElement (Note.EDIT_NEEDED_NOTE_TYPE);

            f.add (new AnyFormItem ("Type",
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

                    this.descField.setText (new StringWithMarkup (selectedText.substring (p.getFirstSentence ().getNext ().getAllTextStartOffset ()).trim ()));

                }

            }

        } else
        {

            this.summaryField.setText (this.object.getSummary ());
            this.descField.setText (this.object.getDescription ());

        }

        return f;

    }

    @Override
    public Set<String> getFormErrors ()
    {
        
        Set<String> errs = new LinkedHashSet ();
        
        if (this.object.isEditNeeded ())
        {

            String text = this.descField.getText ();

            if ((text == null)
                ||
                (text.trim ().length () == 0)
               )
            {
                
                errs.add ("Please enter a description.");
                                
            }
            
        } else
        {

            if (this.summaryField.getText () == null)
            {

                errs.add ("Please enter a summary.");

            }

        }
        
        return errs;
        
    }
    
    @Override
    public boolean handleSave ()
    {

        if (this.object.isEditNeeded ())
        {

            String text = this.descField.getText ();
            
            Paragraph p = new Paragraph (text,
                                         0);
            
            this.object.setSummary (p.getFirstSentence ().getText ());

        } else
        {

            // Fill up the note.
            this.object.setSummary (this.summaryField.getText ());

        }

        this.object.setDescription (this.descField.getValue ());

        String type = null;

        if (!this.object.isEditNeeded ())
        {

            type = this.types.getSelectedItem ().toString ().trim ();

            this.object.setType (type);

        } else
        {

            type = Note.EDIT_NEEDED_NOTE_TYPE;

        }

        // If the type is "edit needed" then get any selectd text indices.
        if (this.object.isEditNeeded ())
        {

            int s = this.itemViewer.getEditor ().getSelectionStart ();
            int e = this.itemViewer.getEditor ().getSelectionEnd ();
        
            if ((this.mode == AbstractActionHandler.EDIT)
                &&
                (s != e)
                &&
                (e > s)
               )
            {
        
                this.object.setPosition (s);
                this.object.setEndPosition (e);

            }
            
            if (this.mode == AbstractActionHandler.ADD)
            {
                
                this.object.setPosition (s);
                this.object.setEndPosition (e);                
                
            }
            
        }

        // See if we are adding at the end of the chapter.
        if (this.itemViewer.getEditor ().isPositionAtTextEnd (this.object.getPosition ()))
        {
            
            try
            {

                // Add a newline to the end of the chapter.
                this.itemViewer.getEditor ().insertText (this.object.getPosition (),
                                                         "\n");

                this.object.setTextPosition (this.itemViewer.getEditor ().getDocument ().createPosition (this.object.getPosition () - 1));
                                                          
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
                this.chapter.addNote (this.object);
    
            }

            this.viewer.saveObject (this.object,
                                    true);

            this.viewer.fireProjectEvent (this.object.getObjectType (),
                                          (this.mode == AbstractActionHandler.ADD ? ProjectEvent.NEW : ProjectEvent.EDIT),
                                          this.object);
                                           
                                           
        } catch (Exception e)
        {

            this.chapter.removeNote (this.object);
        
            Environment.logError ("Unable to save/add note: " +
                                  this.object,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to " + ((this.mode == AbstractActionHandler.ADD) ? "add new " : "save") + Environment.getObjectTypeName (Note.OBJECT_TYPE).toLowerCase () + ".");

            return false;

        }
        
        if (this.object.isEditNeeded ())
        {
            
            try
            {

                Position pos = this.itemViewer.getEditor ().getDocument ().createPosition (this.object.getPosition ());
    
                this.object.setTextPosition (pos);            

                if (this.object.getEndPosition () > -1)
                {
    
                    this.object.setEndTextPosition (this.itemViewer.getEditor ().getDocument ().createPosition (this.object.getEndPosition ()));
    
                }                
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to set text position",
                                      e);
                
            }
            
        }

        if (this.object.getChapter () != null)
        {

            QTextEditor editor = this.itemViewer.getEditor ();

            editor.grabFocus ();

        }

        // Need to reindex the chapter to ensure that things are in the right order.    
        this.object.getChapter ().reindex ();
        
        Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).addType (type,
                                                                                         true);

        // Expand the note type.
        this.viewer.showObjectInTree (Note.OBJECT_TYPE,
                                      new TreeParentNode (Note.OBJECT_TYPE,
                                                          type));
                                             
        this.viewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

        this.viewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
        
        return true;

    }
    
}
