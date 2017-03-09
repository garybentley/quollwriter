package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;

public class AddChapterActionHandler extends AbstractFormPopup<ProjectViewer, NamedObject>
{

    private TextFormItem  nameField = null;
    private Chapter       addAfter = null;
    private Book          book = null;
    private ActionListener afterAdd = null;
    private Chapter       chapterToAdd = null;
    
    public AddChapterActionHandler(Book          b,
                                   Chapter       addAfter,
                                   ProjectViewer pv,
                                   Chapter       chapterToAdd)
    {

        this (b,
              addAfter,
              pv);
    
        this.chapterToAdd = chapterToAdd;
    
    }
    
    public AddChapterActionHandler(Book          b,
                                   Chapter       addAfter,
                                   ProjectViewer pv)
    {

        super (new Chapter (b,
                            null),
               pv,
               AbstractActionHandler.ADD,
               true);

        this.book = b;
        this.addAfter = addAfter;

        final AddChapterActionHandler _this = this;

        this.nameField = new TextFormItem ("Name",
                                           null);
        
        this.nameField.setDoOnReturnPressed (new ActionListener ()
        {
          
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.save ();
                
            }
            
        });
                
    }

    public void setDoAfterAdd (ActionListener onAdd)
    {
        
        this.afterAdd = onAdd;
        
    }
    
    @Override    
    public void handleCancel ()
    {

        // Nothing to do.

    }

    @Override
    public Set<String> getFormErrors ()
    {

        Set<String> errs = new LinkedHashSet ();
    
        if (this.nameField.getValue () == null)
        {

            errs.add ("Please select a name.");

        }

        return errs;
        
    }
    
    @Override    
    public boolean handleSave ()
    {

        String n = this.nameField.getText ();

        Chapter newChapter = null;
        
        try
        {

            if (this.chapterToAdd != null)
            {
                
                this.chapterToAdd.setName (n);
                
                newChapter = this.book.createChapterAfter (this.addAfter,
                                                           this.chapterToAdd);

            } else {
        
                newChapter = this.book.createChapterAfter (this.addAfter,
                                                           n);

            }
                                                                
            this.viewer.saveObject (newChapter,
                                    true);

            this.viewer.fireProjectEvent (newChapter.getObjectType (),
                                          ProjectEvent.NEW,
                                          newChapter);
                                                                                      
        } catch (Exception e)
        {

            Environment.logError ("Unable to add new chapter with name: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to add new {chapter}.");

            return false;

        }

        try
        {

            this.viewer.editChapter (newChapter);

            this.viewer.addChapterToTreeAfter (newChapter,
                                               this.addAfter);

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  newChapter,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to edit {chapter}.");

            return false;

        }

        if (this.afterAdd != null)
        {
            
            UIUtils.doLater (this.afterAdd);
            
        }
        
        return true;

    }

    @Override
    public String getTitle ()
    {

        return "Add New {Chapter}";

    }

    @Override
    public Icon getIcon (int iconSizeType)
    {

        return Environment.getIcon (Chapter.OBJECT_TYPE + "-add",
                                    iconSizeType);

    }

    @Override    
    public Set<FormItem> getFormItems (String selectedText)
    {

        Set<FormItem> items = new LinkedHashSet ();
        items.add (this.nameField);

        return items;

    }

    @Override
    public JComponent getFocussedField ()
    {

        return this.nameField;

    }
/*    
    public int getShowAtPosition ()
    {

        return -1;

    }
*/
}
