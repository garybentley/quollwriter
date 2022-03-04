package com.quollwriter.editors.ui;

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
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;

public class CommentItemFormatter extends AbstractChapterItemFormatter<Note, AbstractProjectViewer>
{

    public CommentItemFormatter (AbstractProjectViewer viewer,
                                 IPropertyBinder     binder,
                                 Note                item,
                                 Runnable            onNewPopupShown,
                                 boolean             itemEditable,
                                 Supplier<Set<Node>> extraControls)
    {

        super (viewer,
               binder,
               item,
               onNewPopupShown,
               itemEditable,
               extraControls);

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

        if (this.item.isDealtWith ())
        {

            QuollLabel l = QuollLabel.builder ()
                .styleClassName (StyleClassNames.DEALTWITH)
                .label (new SimpleStringProperty (Environment.formatDateTime (this.item.getDealtWith ())))
                .build ();

            v.getChildren ().add (l);

            this.binder.addChangeListener (this.item.dealtWithProperty (),
                                           (pr, oldv, newv) ->
            {

                l.setVisible (this.item.isDealtWith ());

            });

        }

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
