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
    private QuollButton editBut = null;
    private QuollButton linkBut = null;
    private QuollButton deleteBut = null;
    private QuollButton saveBut = null;
    private QuollButton cancelBut = null;

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

        this.editBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.EDIT)
            .tooltip (getUILanguageStringProperty (Arrays.asList (edititem,tooltip),
                                                  Environment.getObjectTypeName (this.item)))
            .onAction (ev ->
            {

                this.viewer.runCommand (ProjectViewer.CommandId.editobject,
                                        this.item);

                UIUtils.runLater (this.popupShown);

            })
            .build ();
        this.editBut.managedProperty ().bind (this.editBut.visibleProperty ());

        this.linkBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.LINK)
            .tooltip (getUILanguageStringProperty (Arrays.asList (linkitem,tooltip),
                                                  Environment.getObjectTypeName (this.item)))
            .onAction (ev ->
            {

                lv.setVisible (true);

                this.saveBut.setVisible (true);
                this.cancelBut.setVisible (true);
                this.editBut.setVisible (false);
                this.linkBut.setVisible (false);
                this.deleteBut.setVisible (false);

                this.linkedToPanel.showEdit ();

            })
            .build ();
        this.linkBut.managedProperty ().bind (this.linkBut.visibleProperty ());

        this.deleteBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.DELETE)
            .tooltip (getUILanguageStringProperty (Arrays.asList (deleteitem,tooltip),
                                                  Environment.getObjectTypeName (this.item)))
            .onAction (ev ->
            {

                this.viewer.showDeleteChapterItemPopup (this.item,
                                                        this.viewer.getEditorForChapter (this.item.getChapter ()).getNodeForChapterItem (this.item));

                UIUtils.runLater (this.popupShown);

            })
            .build ();
        this.deleteBut.managedProperty ().bind (this.deleteBut.visibleProperty ());

        this.saveBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.SAVE)
            .tooltip (chapteritems,links,save,buttons,confirm,tooltip)
            .onAction (ev ->
            {

                this.item.setLinks (this.linkedToPanel.getSelected ());

                try
                {

                    this.viewer.saveObject (this.item,
                                            true);
                    this.saveBut.setVisible (false);
                    this.cancelBut.setVisible (false);
                    this.editBut.setVisible (true);
                    this.linkBut.setVisible (true);
                    this.deleteBut.setVisible (true);
                    this.linkedToPanel.showView ();
                    lv.setVisible (this.item.getLinks ().size () > 0);                    

                } catch (Exception e) {

                    Environment.logError ("Unable to save item: " +
                                          this.item,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (chapteritems,links,save,actionerror));

                }

            })
            .build ();
        this.saveBut.managedProperty ().bind (this.saveBut.visibleProperty ());
        this.saveBut.setVisible (false);

        this.cancelBut = QuollButton.builder ()
            .styleClassName (StyleClassNames.CANCEL)
            .tooltip (chapteritems,links,save,buttons,confirm,tooltip)
            .onAction (ev ->
            {

                this.saveBut.setVisible (false);
                this.cancelBut.setVisible (false);
                this.editBut.setVisible (true);
                this.linkBut.setVisible (true);
                this.deleteBut.setVisible (true);
                this.linkedToPanel.showView ();
                lv.setVisible (this.item.getLinks ().size () > 0);

            })
            .build ();
        this.cancelBut.managedProperty ().bind (this.cancelBut.visibleProperty ());
        this.cancelBut.setVisible (false);

        //ToolBar t = new ToolBar ();
        HBox t = new HBox ();
        t.getStyleClass ().addAll (StyleClassNames.BUTTONS, StyleClassNames.TOOLBAR);
        t.getChildren ().addAll (this.editBut, this.linkBut, this.deleteBut, this.saveBut, this.cancelBut);

        v.getChildren ().addAll (this.getContent (), lv, t);

        return v;

    }

    public abstract Node getContent ();

}
