package com.quollwriter.editors.ui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.font.*;

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
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.BlockPainter;
import com.quollwriter.ui.renderers.*;

import com.quollwriter.editors.ui.panels.*;

/**
 * A comment action handler is really just a tweaked note action handler.  A comment is a note but for
 * the editor project viewer behaves a little differently.
 *
 * TODO: Merge/extend from NoteActionHandler?
 */
public class CommentActionHandler<V extends AbstractProjectViewer> extends AbstractFormPopup<AbstractProjectViewer, Note> //ProjectViewerActionHandler<AbstractProjectViewer, Note>
{

    private MultiLineTextFormItem comment = null;
    protected Chapter    chapter = null;
    protected int        showAt = -1;
    private QTextEditor editor = null;
    private ChapterItemViewer<V> itemViewer = null;
    
    public CommentActionHandler (final Note                 n,
                                 final ChapterItemViewer<V> itemViewer)
    {
    
        super (n,
               itemViewer.getViewer (),
               EDIT);

        this.chapter = n.getChapter ();
        this.showAt = n.getPosition ();

        this.itemViewer = itemViewer;
        
        final EditorChapterPanel qep = (EditorChapterPanel) this.viewer.getEditorForChapter (this.chapter);
        
        this.setPopupOver (qep);
        
        this.editor = itemViewer.getEditor ();
        
        final int origSelStart = this.editor.getSelectionStart ();
        
        final BlockPainter highlight = new BlockPainter (Environment.getHighlightColor ());                
        
        final Caret origCaret = editor.getCaret ();
        
        final CommentActionHandler _this = this;
        
        this.setOnShowAction (new ActionListener ()
        {
                      
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                        
                qep.setChapterItemEditVisible (true);
                            
                editor.setCaret (new DefaultCaret ()
                {
                   
                    private boolean isVis = false;
                   
                    @Override
                    public void setSelectionVisible (boolean vis)
                    {
                        
                        _this.editor.removeAllHighlights (highlight);
                        
                        if (vis != this.isVis) {
                            this.isVis = vis;
                            super.setSelectionVisible(false);
                            super.setSelectionVisible(true);
                        }                        
                        
                    }
                    
                });                        
                                
                if (n.getEndPosition () > n.getStartPosition ())
                {
                    
                    _this.editor.addHighlight (n.getStartPosition (),
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
                
                qep.setChapterItemEditVisible (false);                
                
                _this.editor.removeAllHighlights (highlight);
                
                _this.editor.setCaret (origCaret);

                _this.editor.setSelectionStart (origSelStart);
                
                _this.editor.grabFocus ();
                
            }
            
        });

    }

    public CommentActionHandler (Chapter              c,
                                 ChapterItemViewer<V> itemViewer,
                                 int                  showAt)
    {

        super (new Note (0,
                         c),
               itemViewer.getViewer (),
               ADD);
    
        this.showAt = showAt;
               
        this.chapter = c;

        this.itemViewer = itemViewer;
        
        this.editor = itemViewer.getEditor ();
        
        final EditorChapterPanel qep = (EditorChapterPanel) this.viewer.getEditorForChapter (this.chapter);
        
        this.setPopupOver (qep); 
               
        this.object.setType (Note.EDIT_NEEDED_NOTE_TYPE);

        final CommentActionHandler _this = this;
        
        this.setOnShowAction (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                try
                {
    
                    _this.itemViewer.addItem (_this.getChapterItem ());
            
                } catch (Exception e)
                {
    
                    Environment.logError ("Unable to add item: " +
                                          _this.object +
                                          " to editor panel",
                                          e);
    
                }
                
            }
            
        });
                            
    }
    
    @Override
    public Point getShowAtPosition ()
    {

        int y = 0;

        Point lastMousePosition = this.itemViewer.getLastMousePosition ();
        
        int at = this.showAt;

        QTextEditor editor = this.itemViewer.getEditor ();

        if (at == -1)
        {

            // Calculate where it should be displayed.
            String sel = editor.getSelectedText ();

            if ((sel != null) &&
                (!sel.trim ().equals ("")))
            {

                // We have some text so use "at"...
                at = editor.getSelectionStart ();

            } else
            {

                int c = editor.getCaret ().getDot ();

                if (c >= 0)
                {

                    at = c;

                } else
                {

                    if (lastMousePosition != null)
                    {

                        at = editor.viewToModel (new Point (lastMousePosition.x,
                                                            lastMousePosition.y));

                    } else
                    {

                        at = editor.getText ().length () - 1;

                    }

                }

            }

        }
            
        this.object.setPosition (at);

        Rectangle r = null;

        try
        {

            r = editor.modelToView (at);

        } catch (Exception e)
        {

            Environment.logError ("Position: " +
                                  at +
                                  " is not valid.",
                                  e);

            return super.getShowAtPosition ();

        }

        JScrollPane scrollPane = this.itemViewer.getScrollPane ();

        y = r.y + 22 - scrollPane.getVerticalScrollBar ().getValue ();

        if ((y < 0) ||
            (y > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ())))
        {

            // Recalculate y since we have moved the scroll position.
            y = r.y + 22; // - scrollPane.getVerticalScrollBar ().getValue ();

        }
/*
        // Adjust the bounds so that the form is fully visible.
        if ((y + this.f.getPreferredSize ().height) > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ()))
        {

            y = y - 22 - this.f.getPreferredSize ().height;

        }

        y -= this.f.getPreferredSize ().height;
  */      
        int xOffset = this.itemViewer.getIconColumnXOffset (this.object);
            
        Point p = new Point (this.itemViewer.getIconColumn ().getWidth () - xOffset,
                             y);

        return p;
        
    }
    
    public Note getChapterItem ()
    {
        
        return this.object;
        
    }
    
    @Override
    public JComponent getFocussedField ()
    {

        return this.comment;

    }

    @Override
    public Icon getIcon (int iconTypeSize)
    {

        return Environment.getIcon (Constants.COMMENT_ICON_NAME,
                                    iconTypeSize);

    }

    @Override
    public String getTitle ()
    {

        if (mode == CommentActionHandler.EDIT)
        {

            return "Edit Comment";

        }

        return "Add New Comment";

    }

    @Override
    public Set<FormItem> getFormItems (String  selectedText)
    {

        Set<FormItem> items = new LinkedHashSet ();
        
        this.comment = new MultiLineTextFormItem (null,
                                                  this.viewer,
                                                  "Enter your comment here...",
                                                  5,
                                                  -1,
                                                  false,
                                                  null);

        UIUtils.addDoActionOnReturnPressed (this.comment.getTextArea (),
                                            this.getSaveAction ());

        if (mode == CommentActionHandler.ADD)
        {

        } else
        {
            
            this.comment.setText (this.object.getDescription ());

        }

        items.add (this.comment);
        
        return items;

    }
    
    @Override
    public Set<String> getFormErrors ()
    {
        
        Set<String> errs = new LinkedHashSet ();
        
        if (this.comment.getValue () == null)
        {
            
            errs.add ("Please enter a comment.");
            
        }
        
        return errs;
        
    }
    
    @Override
    public void handleCancel ()
    {
             
        if (this.mode == ADD)
        {
            
            this.itemViewer.removeItem (this.object);
             
        }             
                
    }
    
    @Override
    public boolean handleSave ()
    {

        String c = this.comment.getText ();
        
        this.object.setDescription (this.comment.getValue ());

        // Use the first line of the description as the summary.
        Paragraph p = new Paragraph (c,
                                     0);

        this.object.setSummary (p.getFirstSentence ().getText ());
        
        String type = Note.EDIT_NEEDED_NOTE_TYPE;        
        
        int s = this.editor.getSelectionStart ();
        int e = this.editor.getSelectionEnd ();
    
        if ((mode == CommentActionHandler.EDIT)
            &&
            (s != e)
            &&
            (e > s)
           )
        {
    
            this.object.setPosition (s);
            this.object.setEndPosition (e);

        }
            
        if (mode == CommentActionHandler.ADD)
        {
            
            this.object.setPosition (s);
            this.object.setEndPosition (e);                
            
        }
            
        try
        {

            this.viewer.saveObject (this.object,
                                    true);

            if (this.mode == CommentActionHandler.ADD)
            {
    
                // Add the item to the chapter.
                this.chapter.addNote (this.object);
    
            }
                                           
            this.viewer.fireProjectEvent (this.object.getObjectType (),
                                          (this.mode == CommentActionHandler.ADD ? ProjectEvent.NEW : ProjectEvent.EDIT),
                                          this.object);
                                           
        } catch (Exception ex)
        {

            Environment.logError ("Unable to save/add comment: " +
                                  this.object,
                                  ex);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to " + ((this.mode == CommentActionHandler.ADD) ? "add new " : "save") + " comment.");

            return false;

        }
        
        try
        {

            Position pos = this.editor.getDocument ().createPosition (this.object.getPosition ());

            this.object.setTextPosition (pos);            

            if (this.object.getEndPosition () > -1)
            {

                this.object.setEndTextPosition (this.editor.getDocument ().createPosition (this.object.getEndPosition ()));

            }                
            
        } catch (Exception ex) {
            
            Environment.logError ("Unable to set text position",
                                  ex);
            
        }

        if (this.object.getChapter () != null)
        {

            this.editor.grabFocus ();

        }
        
        // Need to reindex the chapter to ensure that things are in the right order.    
        this.chapter.reindex ();
            
        // Expand the note type.
        this.viewer.showObjectInTree (Note.OBJECT_TYPE,
                                      new TreeParentNode (Note.OBJECT_TYPE,
                                                          type));
                                             
        this.viewer.reloadTreeForObjectType (Note.OBJECT_TYPE);

        this.viewer.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
        
        if (e > s)
        {
            
            this.editor.setSelectionEnd (s);
            
        }
        
        return true;

    }

}
