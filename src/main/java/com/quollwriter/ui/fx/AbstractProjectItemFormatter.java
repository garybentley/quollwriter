package com.quollwriter.ui.fx;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

public abstract class AbstractProjectItemFormatter<E extends ChapterItem> extends AbstractChapterItemFormatter<E, ProjectViewer>
{

    public AbstractProjectItemFormatter (ProjectViewer       viewer,
                                         IPropertyBinder     binder,
                                         E                   item,
                                         Runnable            onNewPopupShown,
                                         Supplier<Set<Node>> extraControls)
    {

        this (viewer,
              binder,
              item,
              onNewPopupShown,
              true,
              extraControls);

    }

    public AbstractProjectItemFormatter (ProjectViewer       viewer,
                                         IPropertyBinder     binder,
                                         E                   item,
                                         Runnable            onNewPopupShown,
                                         boolean             editable,
                                         Supplier<Set<Node>> extraControls)
    {

        super (viewer,
               binder,
               item,
               onNewPopupShown,
               editable,
               extraControls);

    }

    @Override
    public void saveItem (E item)
                   throws GeneralException
    {

        viewer.saveObject (item,
                           false);

    }

    @Override
    public void deleteItem (E item)
    {

        viewer.showDeleteChapterItemPopup (item,
                                           viewer.getEditorForChapter (item.getChapter ()).getNodeForChapterItem (item));

    }

    @Override
    public void editItem (E item)
    {

        this.viewer.runCommand (ProjectViewer.CommandId.editobject,
                                item);

    }

}
