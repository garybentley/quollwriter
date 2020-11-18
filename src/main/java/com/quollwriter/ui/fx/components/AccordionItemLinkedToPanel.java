package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import com.quollwriter.uistrings.UILanguageStringsManager;
import static com.quollwriter.LanguageStrings.*;

public class AccordionItemLinkedToPanel extends VBox
{

    private LinkedToPanel linkedToPanel = null;
    private AccordionItem acc = null;
    private ToolBar editButs = null;
    private Button editCon = null;
    private Hyperlink noitems = null;
    private NamedObject object = null;
    private IPropertyBinder binder = null;

    public AccordionItemLinkedToPanel (NamedObject                         obj,
                                       IPropertyBinder                     binder,
                                       AbstractProjectViewer               viewer,
                                       Function<Set<NamedObject>, Boolean> onSave,
                                       Runnable                            onCancel)
    {

        this.object = obj;
        this.binder = binder;
        this.linkedToPanel = new LinkedToPanel (obj,
                                                binder,
                                                viewer);

        QuollButton saveB = QuollButton.builder ()
           .tooltip (getUILanguageStringProperty (Arrays.asList (linkedto,edit,buttons,save,tooltip),
                                                  obj.nameProperty ()))
           .iconName (StyleClassNames.SAVE)
           .buttonType (ButtonBar.ButtonData.APPLY)
           .onAction (ev ->
           {

               if (onSave != null)
               {

                  if (onSave.apply (this.linkedToPanel.getSelected ()))
                  {

                      this.showView ();
                      return;

                  }

              }

           })
           .build ();

        QuollButton cancelB = QuollButton.builder ()
           .tooltip (getUILanguageStringProperty (linkedto,edit,buttons,cancel,tooltip))
           .iconName (StyleClassNames.CANCEL)
           .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
           .build ();

        cancelB.addEventHandler (ActionEvent.ACTION,
                                ev ->
        {

            if (onCancel != null)
            {

                UIUtils.runLater (onCancel);

            }

           this.showView ();

        });

        ToolBar bb = new ToolBar ();
        bb.getStyleClass ().add (StyleClassNames.BUTTONS);
        bb.getItems ().addAll (saveB, cancelB);
        bb.managedProperty ().bind (bb.visibleProperty ());
        bb.setVisible (false);

        this.editButs = bb;

        this.noitems = QuollHyperlink.builder ()
            .styleClassName (StyleClassNames.PLACEHOLDER)
            .label (linkedto,nolinksedit)
            .onAction (ev ->
            {

                this.showEdit ();

            })
            .build ();
        noitems.managedProperty ().bind (noitems.visibleProperty ());
        noitems.setVisible (obj.getLinks ().size () == 0);

        this.binder.addSetChangeListener (obj.getLinks (),
                                          ev ->
        {

            noitems.setVisible (obj.getLinks ().size () == 0);

        });

        VBox content = new VBox ();
        content.getChildren ().addAll (this.linkedToPanel, this.editButs, this.noitems);

        Set<MenuItem> contextMenuItems = new LinkedHashSet<> ();
        contextMenuItems.add (QuollMenuItem.builder ()
            .label (getUILanguageStringProperty (linkedto,headerpopupmenu,items,edit))
            .onAction (ev ->
            {

                this.showEdit ();

            })
            .build ());

        this.editCon = QuollButton.builder ()
            .iconName (StyleClassNames.EDIT)
            .tooltip (getUILanguageStringProperty (Arrays.asList (linkedto,view,buttons,edit,tooltip),
                                                 Environment.getObjectTypeName (obj)))
            .onAction (ev ->
            {

                this.showEdit ();

            })
            .build ();

        Set<javafx.scene.Node> headerCons = new LinkedHashSet<> ();
        headerCons.add (this.editCon);

        VBox.setVgrow (this.linkedToPanel,
                       Priority.ALWAYS);

        this.acc = AccordionItem.builder ()
            .title (getUILanguageStringProperty (linkedto,view,title))
            .styleClassName (StyleClassNames.LINKEDTO)
            .openContent (content)
            .contextMenu (contextMenuItems)
            .headerControls (headerCons)
            .build ();

        VBox.setVgrow (this.acc,
                       Priority.ALWAYS);

        this.getChildren ().addAll (this.acc);

    }

    private void showEdit ()
    {

        this.noitems.setVisible (false);
        this.acc.setContentVisible (true);
        this.editCon.setVisible (false);
        this.editButs.setVisible (true);
        this.linkedToPanel.showEdit ();

    }

    private void showView ()
    {

        this.noitems.setVisible (this.object.getLinks ().size () == 0);
        this.editCon.setVisible (true);
        this.editButs.setVisible (false);
        this.linkedToPanel.showView ();

    }

    public LinkedToPanel getLinkedToPanel ()
    {

        return this.linkedToPanel;

    }

    public boolean isOpen ()
    {

        return this.acc.isOpen ();

    }

    public void setContentVisible (boolean v)
    {

        this.acc.setContentVisible (v);

    }

}
