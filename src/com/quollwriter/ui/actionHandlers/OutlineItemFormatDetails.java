package com.quollwriter.ui.actionHandlers;

import java.util.Set;

import javax.swing.*;

import java.awt.event.ActionListener;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public class OutlineItemFormatDetails implements ChapterItemFormatDetails<OutlineItem, ProjectViewer>
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

            desc = Environment.getUIString (LanguageStrings.objectnames, LanguageStrings.singular, Scene.OBJECT_TYPE) + ": " + UIUtils.getObjectALink (item.getScene ()) + "<br />" + desc;

        }

        return desc;

    }

    @Override
    public Set<JComponent> getTools (OutlineItem                      item,
                                     ChapterItemViewer<ProjectViewer> qep)
    {

        return null;

    }

    public ActionListener getEditItemActionHandler (OutlineItem                      item,
                                                    ChapterItemViewer<ProjectViewer> viewer)
    {

        return new ChapterItemActionHandler (item,
                                             viewer,
                                             ChapterItemActionHandler.EDIT,
                                             -1);

    }

    public ActionListener getDeleteItemActionHandler (OutlineItem                      item,
                                                      ChapterItemViewer<ProjectViewer> qep,
                                                      boolean                          showAtItem)
    {

        return new DeleteChapterItemActionHandler (item,
                                                   qep.getViewer (),
                                                   showAtItem);

    }

}
