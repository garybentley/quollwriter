package com.quollwriter.ui.actionHandlers;

import java.util.Set;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public class NoteFormatDetails<V extends AbstractViewer> implements ChapterItemFormatDetails<Note, V>
{

    public String getIcon (Note item)
    {
                
        if (item.isEditNeeded ())
        {
            
            return "edit-needed-note";

        }

        return item.getObjectType ();
        
    }
    
    public String getTitle (Note item)
    {
                
        if (item.isEditNeeded ())
        {
            
            return "Edit Needed";
            
        }
        
        return Environment.getObjectTypeName (item.getObjectType ());
        
    }
    
    public String getItemDescription (Note item)
    {

        String summ = item.getSummary ();

        if ((!item.getType ().equals (""))
            &&
            (!item.isEditNeeded ())
           )
        {

            summ = "<b>" + item.getType () + "</b>: " + summ;
            
            if (item.getDescription () != null)
            {
                
                summ += "<br />" + item.getDescription ().getMarkedUpText ();
                
            }

            return summ;
            
        } else {
            
            return item.getDescription ().getMarkedUpText ();
            
        }

    }    
    
    @Override
    public Set<JComponent> getTools (Note                 item,
                                     ChapterItemViewer<V> ep)
    {
        
        return null;
        
    }
    
    @Override
    public ActionListener getEditItemActionHandler (Note                 item,
                                                    ChapterItemViewer<V> ep)
    {

        return new NoteActionHandler (item,
                                      ep);

    }

    @Override
    public ActionListener getDeleteItemActionHandler (Note                 item,
                                                      ChapterItemViewer<V> ep,
                                                      boolean              showAtItem)
    {

        return new DeleteChapterItemActionHandler (item,
                                                   // FOr now... to please the compiler...
                                                   (AbstractProjectViewer) ep.getViewer (),
                                                   showAtItem);

    }

}
