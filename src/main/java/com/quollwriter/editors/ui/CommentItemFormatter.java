package com.quollwriter.editors.ui;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;

public class CommentItemFormatter extends AbstractChapterItemFormatter<Note, AbstractProjectViewer>
{

    public CommentItemFormatter (AbstractProjectViewer viewer,
                                 IPropertyBinder     binder,
                                 Note                item,
                                 Runnable            onNewPopupShown)
    {

        super (viewer,
               binder,
               item,
               onNewPopupShown);

    }

    @Override
    public void saveItem (Note item)
                   throws GeneralException
    {

        viewer.saveObject (item,
                           false);

    }

    @Override
    public void deleteItem (Note item)
    {

        // TODO Fix!
        if (this.viewer instanceof EditorProjectViewer)
        {

            ((EditorProjectViewer) viewer).showDeleteComment (item);

        }

    }

    @Override
    public void editItem (Note item)
    {

        this.viewer.runCommand (ProjectViewer.CommandId.editobject,
                                item);

    }

    @Override
    public Node getContent ()
    {

        VBox v = new VBox ();

        String summ = this.item.getSummary ();

        String desc = null;

        if ((!this.item.getType ().equals (""))
            &&
            (!this.item.isEditNeeded ())
           )
        {

            summ = "<b>" + this.item.getType () + "</b>: " + summ;

            if (this.item.getDescription () != null)
            {

                summ += "<br />" + this.item.getDescription ().getMarkedUpText ();

            }

            desc = summ;

        } else {

            desc = item.getDescription ().getMarkedUpText ();

        }

        QuollTextView t = QuollTextView.builder ()
            .text (desc)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();
            /*
        BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
            .text (desc)
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();
*/
        v.getChildren ().add (t);

        return v;

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.COMMENT;

    }

    @Override
    public StringProperty getPopupTitle ()
    {

        return null;
        /*
        if (mode == CommentActionHandler.EDIT)
        {

            return getUILanguageStringProperty (comments,edit,title);
            //"Edit Comment";

        }

        return getUILanguageStringProperty (comments,_new,title);
*/
    }

}
