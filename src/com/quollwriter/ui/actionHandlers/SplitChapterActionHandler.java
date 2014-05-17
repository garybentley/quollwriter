package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.Markup;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.text.*;

public class SplitChapterActionHandler extends AbstractActionHandler
{

    private JTextField    nameField = UIUtils.createTextField ();
    private Form          f = null;
    private ProjectViewer projectViewer = null;
    private Chapter       addFrom = null;

    public SplitChapterActionHandler (Chapter       addFrom,
                                      ProjectViewer pv)
    {

        super (new Chapter (addFrom.getBook (),
                            null),
               pv,
               AbstractActionHandler.ADD);

        this.projectViewer = pv;
        this.addFrom = addFrom;
        
        final SplitChapterActionHandler _this = this;

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
                                      "Please select a {Chapter} name.");

            return false;

        }
        
        try
        {            

            QuollEditorPanel panel = this.projectViewer.getEditorForChapter (this.addFrom);
        
            QTextEditor ed = panel.getEditor ();

            int start = ed.getSelectionStart ();
            int end = ed.getSelectionEnd ();
            
            if (start == end)
            {
                
                end = ed.getText ().length ();
            
            }
        
            int shiftBy = -1 * start;
        
            Chapter c = this.addFrom.getBook ().createChapterAfter (this.addFrom,
                                                                    this.nameField.getText ());

            this.dataObject = c;

            String newText = ed.getText ().substring (start,
                                                      end);
            
            // Get the text.
            c.setText (newText);
            
            // Get the markup and shift
            Markup newM = new Markup (ed.getMarkup (),
                                      start,
                                      end);
            newM.shiftBy (shiftBy);
            
            c.setMarkup (newM.toString ());
            
            this.projectViewer.saveObject (c,
                                           true);

            List toSave = new ArrayList ();
                                           
            // Handle notes, scenes, outline items.
            Set<ChapterItem> its = this.addFrom.getChapterItemsWithPositionBetween (start,
                                                                                    end);
            
            for (ChapterItem it : its)
            {
                
                // Null out the standard Position objects and set the underlying start/end positions.
                it.shiftPositionBy (shiftBy);
                
                // Set the chapter.
                it.setChapter (c);

                // Save the item.
                toSave.add (it);
                
            }                                           
            
            this.projectViewer.saveObjects (toSave,
                                            true);
                        
            ed.removeText (start,
                           end - start);

            this.projectViewer.fireProjectEvent (this.dataObject.getObjectType (),
                                                 ProjectEvent.NEW,
                                                 this.dataObject);
                            
            // Save the chapter.
            panel.saveObject ();
                                 
            // Reload existing chapter?
            panel.reinitIconColumn ();
                                 
            this.projectViewer.reloadChapterTree ();
                                 
            // Open the new chapter.
            this.projectViewer.editChapter (c);            
                                                                                      
        } catch (Exception e)
        {

            Environment.logError ("Unable to add new chapter with name: " +
                                  this.nameField.getText (),
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to add new " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE) + ".");

            return false;

        }
/*
        Chapter nc = (Chapter) this.dataObject;

        try
        {

            this.projectViewer.editChapter (nc);

            this.projectViewer.addChapterToTreeAfter (nc,
                                                      this.addFrom);

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  nc,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "An internal error has occurred.\n\nUnable to edit " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE) + ".");

            return false;

        }
*/
        return true;

    }

    private String getSelectedText ()
    {

        QTextEditor ed = this.projectViewer.getEditorForChapter (this.addFrom).getEditor ();
            
        int start = ed.getSelectionStart ();
        int end = ed.getSelectionEnd ();

        if (start == end)
        {
            
            end = ed.getText ().length ();
            
        }
    
        return ed.getText ().substring (start,
                                        end);        
        
    }
    
    public String getTitle (int mode)
    {

        return "Split " + Environment.getObjectTypeName (Chapter.OBJECT_TYPE);

    }

    public String getIcon (int mode)
    {

        return Chapter.OBJECT_TYPE + "-split";

    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

        List<FormItem> items = new ArrayList ();

        items.add (new FormItem (Environment.replaceObjectNames ("New {Chapter} Name"),
                                 this.nameField));
        
        String text = this.getSelectedText ();
    
        Paragraph para = new Paragraph (text,
                                        0);
    
        //SentenceIterator iter = new SentenceIterator (text);

        int count = para.getWordCount ();
        
        //int count = UIUtils.getWordCount (text);
        
        // Get the first sentence.
        text = para.getFirstSentence ().getText ();
        
        //text = iter.next ();
        
        if (text != null)
        {
            
            text = text.trim ();
            
            if (text.length () > 150)
            {
                
                text = text.trim ().substring (0, 150) + "...";
                    
            }
                                
            JTextArea t = new JTextArea (text);
            
            t.setLineWrap (true);
            t.setWrapStyleWord (true);
            t.setOpaque (false);
            t.setBorder (null);
            t.setEditable (false);
            t.setSize (new Dimension (300, 300));
            
            Box p = new Box (BoxLayout.X_AXIS);
            p.add (t);
            p.setOpaque (false);
            
            items.add (new FormItem ("Start at",
                                     p));

            QTextEditor ed = this.projectViewer.getEditorForChapter (this.addFrom).getEditor ();
                
            int start = ed.getSelectionStart ();
            int end = ed.getSelectionEnd ();
            
            text = para.getLastSentence ().getText ();//iter.last ();
                 
            if ((end > start)
                &&
                (text != null)
               )
            {
                
                text = text.trim ();
                
                if (text.length () > 150)
                {
                    
                    text = "... " + text.trim ().substring (text.length () - 150);
                        
                }
                
                JTextArea et = new JTextArea (text);
                
                et.setLineWrap (true);
                et.setWrapStyleWord (true);
                et.setOpaque (false);
                et.setBorder (null);
                et.setEditable (false);
                et.setSize (new Dimension (300, 300));
                
                Box ep = new Box (BoxLayout.X_AXIS);
                ep.add (et);
                ep.setOpaque (false);
                
                items.add (new FormItem ("End at",
                                         ep));
                                     
            }
            
            items.add (new FormItem ("Words",
                                     new JLabel (Environment.formatNumber (count))));

        }
        
        if (text != null)
        {
                    
        }
        
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
