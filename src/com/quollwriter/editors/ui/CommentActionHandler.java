package com.quollwriter.editors.ui;

//import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

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
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.renderers.*;

import com.quollwriter.editors.ui.panels.*;

/**
 * A comment action handler is really just a tweaked note action handler.  A comment is a note but for
 * the editor project viewer behaves a little differently.
 */
public class CommentActionHandler extends ProjectViewerActionHandler
{

    private TextArea  comment = null;
    protected Chapter    chapter = null;
    protected int        showAt = -1;

    public CommentActionHandler (final Note                n,
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
                        
                ((EditorChapterPanel) qep).setChapterItemEditVisible (true);
                            
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
                
                ((EditorChapterPanel) qep).setChapterItemEditVisible (false);                
                
                editor.removeAllHighlights (highlight);
                
                editor.setCaret (origCaret);

                editor.setSelectionStart (origSelStart);
                
                editor.grabFocus ();
                
            }
            
        });

    }

    public CommentActionHandler (Chapter             c,
                                 AbstractEditorPanel qep,
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
               
        ((Note) this.dataObject).setType (Note.EDIT_NEEDED_NOTE_TYPE);
        
        this.initFormItems ();
        
    }
    
    @Override
    public int getShowAtPosition ()
    {

        return this.showAt;

    }    
    
    @Override
    public JComponent getFocussedField ()
    {

        return this.comment;

    }

    @Override
    public String getIcon (int mode)
    {

        return Constants.COMMENT_ICON_NAME;

    }

    @Override
    public String getTitle (int mode)
    {

        if (mode == AbstractActionHandler.EDIT)
        {

            return "Edit Comment";

        }

        return "Add New Comment";

    }

    private void initFormItems ()
    {

        final CommentActionHandler _this = this;

        ActionListener doSave = new ActionAdapter ()
        {
          
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.submitForm ();
                
            }
            
        };
                
        this.comment = new TextArea ("Enter your comment here...",
                                     5,
                                     -1);
        
        UIUtils.addDoActionOnReturnPressed (this.comment,
                                            doSave);
        
    }

    @Override
    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

        List<FormItem> f = new ArrayList ();

        f.add (new FormItem (null,
                             this.comment));

        if (mode == AbstractActionHandler.ADD)
        {

        } else
        {

            Note n = (Note) obj;
            
            this.comment.setTextWithMarkup (n.getDescription ());

        }

        return f;

    }

    @Override
    public boolean handleSave (Form f,
                               int  mode)
    {

        Note n = (Note) this.dataObject;

        String c = this.comment.getText ();
        
        n.setDescription (this.comment.getTextWithMarkup ());

        // Use the first line of the description as the summary.
        Paragraph p = new Paragraph (c,
                                     0);

        n.setSummary (p.getFirstSentence ().getText ());
        
        String type = Note.EDIT_NEEDED_NOTE_TYPE;

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
            
        try
        {

            this.projectViewer.saveObject (n,
                                           true);

            if (this.mode == AbstractActionHandler.ADD)
            {
    
                // Add the item to the chapter.
                this.chapter.addNote (n);
    
            }
                                           
            this.projectViewer.fireProjectEvent (n.getObjectType (),
                                                 (this.mode == AbstractActionHandler.ADD ? ProjectEvent.NEW : ProjectEvent.EDIT),
                                                 n);
                                           
        } catch (Exception ex)
        {

            Environment.logError ("Unable to save/add comment: " +
                                  n,
                                  ex);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to " + ((this.mode == AbstractActionHandler.ADD) ? "add new " : "save") + " comment.");

            return false;

        }
        
        try
        {

            Position pos = this.editorPanel.getEditor ().getDocument ().createPosition (n.getPosition ());

            n.setTextPosition (pos);            

            if (n.getEndPosition () > -1)
            {

                n.setEndTextPosition (this.editorPanel.getEditor ().getDocument ().createPosition (n.getEndPosition ()));

            }                
            
        } catch (Exception ex) {
            
            Environment.logError ("Unable to set text position",
                                  ex);
            
        }

        if (n.getChapter () != null)
        {

            QTextEditor editor = this.editorPanel.getEditor ();

            editor.grabFocus ();

        }
        
        // Need to reindex the chapter to ensure that things are in the right order.    
        n.getChapter ().reindex ();
            
        // Expand the note type.
        this.projectViewer.showObjectInTree (Note.OBJECT_TYPE,
                                             new TreeParentNode (Note.OBJECT_TYPE,
                                                                 type));
                                             
        this.projectViewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

        this.projectViewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
        
        if (e > s)
        {
            
            this.editorPanel.getEditor ().setSelectionEnd (s);
            
        }
        
        return true;

    }

}
