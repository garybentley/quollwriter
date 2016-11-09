package com.quollwriter.ui.actionHandlers;

import java.util.Set;

import javax.swing.*;

import java.awt.event.ActionListener;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public class OutlineItemFormatDetails implements ChapterItemFormatDetails<OutlineItem>
{

    public String getIcon (OutlineItem item)
    {
        
        return item.getObjectType ();
        
    }
    
    public String getTitle (OutlineItem item)
    {

        return Environment.getObjectTypeName (item.getObjectType ());
        
    }

    public String getItemDescription (OutlineItem item)
    {
        
        String desc = item.getDescription ().getMarkedUpText ();
        
        if (item.getScene () != null)
        {
            
            desc = "{Scene}: " + UIUtils.getObjectALink (item.getScene ()) + "<br />" + desc;
          
        }

        return desc;

    }

    @Override
    public Set<JComponent> getTools (OutlineItem         item,
                                     AbstractEditorPanel qep)
    {
        
        return null;
        
    }
    
    public AbstractActionHandler getEditItemActionHandler (OutlineItem         item,
                                                           AbstractEditorPanel qep)
    {

        return new ChapterItemActionHandler (item,
                                             qep,
                                             AbstractActionHandler.EDIT,
                                             -1);

    }

    public ActionListener getDeleteItemActionHandler (OutlineItem         item,
                                                      AbstractEditorPanel qep,
                                                      boolean             showAtItem)
    {

        return new DeleteChapterItemActionHandler (item,
                                                   qep.getViewer (),
                                                   showAtItem);

    }

}
