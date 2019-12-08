package com.quollwriter.ui.fx;

import java.util.*;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.ChapterItem;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public abstract class AbstractChapterItemFormatter<E extends ChapterItem> implements ChapterItemFormatter
{

    protected ProjectViewer viewer = null;
    protected E item = null;
    private IPropertyBinder binder = null;
    private LinkedToPanel linkedToPanel = null;
    private Runnable popupShown = null;

    public AbstractChapterItemFormatter (ProjectViewer   viewer,
                                         IPropertyBinder binder,
                                         E               item,
                                         Runnable        onNewPopupShown)
    {

        this.viewer = viewer;
        this.item = item;
        this.binder = binder;
        this.popupShown = onNewPopupShown;

    }

    public Node format ()
    {

        VBox v = new VBox ();

        VBox lv = new VBox ();
        lv.getStyleClass ().add (StyleClassNames.LINKEDTO);
        lv.managedProperty ().bind (lv.visibleProperty ());

        this.binder.addSetChangeListener (this.item.getLinks (),
                                          ev ->
        {

            lv.setVisible (this.item.getLinks ().size () > 0);

        });

        lv.setVisible (this.item.getLinks ().size () > 0);

        lv.getChildren ().add (QuollLabel.builder ()
            .label (linkedto,view,title)
            .styleClassName (StyleClassNames.SUBTITLE)
            .build ());

        this.linkedToPanel = new LinkedToPanel (this.item,
                                                this.binder,
                                                this.viewer);

        lv.getChildren ().add (new ScrollPane (this.linkedToPanel));

        lv.getChildren ().add (new Label ("Click the link to button again to finish editing."));

        ToolBar t = new ToolBar ();
        t.getStyleClass ().add (StyleClassNames.BUTTONS);
        t.getItems ().add (QuollButton.builder ()
            .styleClassName (StyleClassNames.EDIT)
            .tooltip (getUILanguageStringProperty (Arrays.asList (edititem,tooltip),
                                                  Environment.getObjectTypeName (this.item)))
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectViewer.CommandId.editobject,
                                        this.item);

                UIUtils.runLater (this.popupShown);

            })
            .build ());

        t.getItems ().add (QuollButton.builder ()
            .styleClassName (StyleClassNames.LINK)
            .tooltip (getUILanguageStringProperty (Arrays.asList (linkitem,tooltip),
                                                  Environment.getObjectTypeName (this.item)))
            .onAction (ev ->
            {

                lv.setVisible (true);

                // Show the link to edit panel.
                if (this.linkedToPanel.isEditVisible ())
                {

                    this.linkedToPanel.showView ();
                    lv.setVisible (this.item.getLinks ().size () > 0);

                } else {

                    this.linkedToPanel.showEdit ();

                }

            })
            .build ());

        t.getItems ().add (QuollButton.builder ()
            .styleClassName (StyleClassNames.DELETE)
            .tooltip (getUILanguageStringProperty (Arrays.asList (deleteitem,tooltip),
                                                  Environment.getObjectTypeName (this.item)))
            .onAction (ev ->
            {

                this.viewer.showDeleteChapterItemPopup (this.item,
                                                        this.viewer.getEditorForChapter (this.item.getChapter ()).getNodeForChapterItem (this.item));

                UIUtils.runLater (this.popupShown);

            })
            .build ());

        v.getChildren ().addAll (this.getContent (), lv, t);

        return v;

    }

    public abstract Node getContent ();

}
