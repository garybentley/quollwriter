package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.event.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ComponentUtils
{

    public static QuollPopup showErrorMessage (AbstractViewer showOn,
                                               String...      message)
    {

        return ComponentUtils.showErrorMessage (showOn,
                                                getUILanguageStringProperty (message));

    }

    public static QuollPopup showErrorMessage (AbstractViewer showOn,
                                               StringProperty message)
    {

        TextFlow t = BasicHtmlTextFlow.builder ()
            .text (getUILanguageStringProperty (Arrays.asList (errormessage,text),
                                                //"%s<br /><br /><a href='%s:%s'>Click here to contact Quoll Writer support about this problem.</a>",
                                                message,
                                                Constants.ACTION_PROTOCOL,
                                                AbstractViewer.CommandIds.reportbug))
            .withViewer (UIUtils.getViewer (showOn))
            .build ();

        Button b = QuollButton.builder ()
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .label (getUILanguageStringProperty (LanguageStrings.buttons,LanguageStrings.close))
            .build ();

        Set<Button> buttons = new LinkedHashSet<> ();
        buttons.add (b);

        QuollPopup qp = ComponentUtils.showMessage (showOn,
                                                    StyleClassNames.ERROR,
                                                    getUILanguageStringProperty (errormessage,title),
                                                    t,
                                                    buttons);

        b.setOnAction (ev ->
        {

            qp.close ();

        });

        return qp;

    }

    public static QuollPopup showMessage (AbstractViewer showOn,
                                          StringProperty message)
    {

        return ComponentUtils.showMessage (showOn,
                                           getUILanguageStringProperty (generalmessage,title),
                                           message);

    }

    public static QuollPopup showMessage (AbstractViewer showOn,
                                          String         styleName,
                                          StringProperty title,
                                          Node           content,
                                          Set<Button>    buttons)
    {

        VBox b = new VBox ();

        // TODO Handle buttons being null.
        ButtonBar bb = new ButtonBar ();
        bb.getButtons ().addAll (buttons);

        HBox h = new HBox ();
        h.getStyleClass ().add (StyleClassNames.BUTTONS);
        h.getChildren ().add (bb);

        b.getChildren ().addAll (content, h);

        QuollPopup qp = QuollPopup.builder ()
            .title ((title != null ? title : getUILanguageStringProperty (generalmessage,LanguageStrings.title)))
            .styleClassName (styleName != null ? styleName : StyleClassNames.MESSAGE)
            .content (b)
            .withClose (true)
            .withViewer (showOn)
            .hideOnEscape (true)
            .removeOnClose (true)
            .show ()
            .build ();

        return qp;

    }

    public static QuollPopup showMessage (AbstractViewer showOn,
                                          StringProperty title,
                                          StringProperty message)
    {

        BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
            .withViewer (showOn)
            .text (message)
            .build ();

        return ComponentUtils.showMessage (showOn,
                                           title,
                                           t);

    }

    public static QuollPopup showMessage (AbstractViewer showOn,
                                          StringProperty title,
                                          Node           message)
    {

        Set<Button> buttons = new LinkedHashSet<> ();
        Button close = ComponentUtils.createCloseButton (null);
        buttons.add (close);

        QuollPopup qp = ComponentUtils.showMessage (showOn,
                                                    StyleClassNames.MESSAGE,
                                                    title,
                                                    message,
                                                    buttons);

        close.setOnAction (ev ->
        {

            qp.close ();

        });

        return qp;

    }

    public static Button createCloseButton (EventHandler<ActionEvent> onAction)
    {

        return QuollButton.builder ()
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .label (getUILanguageStringProperty (buttons,close))
            .onAction (onAction)
            .build ();

    }

    public static QuollPopup showMessage (AbstractViewer showOn,
                                          String         styleName,
                                          StringProperty title,
                                          Node           message)
    {

        Button close = ComponentUtils.createCloseButton (null);

        Set<Button> buttons = new LinkedHashSet<> ();
        buttons.add (close);

        QuollPopup qp = ComponentUtils.showMessage (showOn,
                                                    styleName,
                                                    title,
                                                    message,
                                                    buttons);

        close.setOnAction (ev -> qp.close ());

        return qp;

    }

    public static QuollPopup createQuestionPopup (StringProperty title,
                                                  String         style,
                                                  StringProperty message,
                                                  StringProperty confirmButtonLabel,
                                                  StringProperty cancelButtonLabel,
                                                  EventHandler<Form.FormEvent> onConfirm,
                                                  AbstractViewer showOn)
    {

        return ComponentUtils.createQuestionPopup (title,
                                            style,
                                            message,
                                            confirmButtonLabel,
                                            cancelButtonLabel,
                                            onConfirm,
                                            null,
                                            null,
                                            showOn);

    }

    public static QuollPopup createQuestionPopup (StringProperty  title,
                                                  String          style,
                                                  StringProperty  message,
                                                  StringProperty  confirmButtonLabel,
                                                  StringProperty  cancelButtonLabel,
                                                  final EventHandler<Form.FormEvent>  onConfirm,
                                                  final EventHandler<Form.FormEvent>  onCancel,
                                                  final Runnable  onClose,
                                                  AbstractViewer  showOn)
    {

        Form f = Form.builder ()
            .styleClassName (style != null ? style : StyleClassNames.QUESTION)
            .description (message)
            .confirmButton ((confirmButtonLabel != null ? confirmButtonLabel : getUILanguageStringProperty (buttons,confirm)))
            .cancelButton ((cancelButtonLabel != null ? cancelButtonLabel : getUILanguageStringProperty (buttons,cancel)))
            .build ();

        f.setOnConfirm (onConfirm);
        f.setOnCancel (onCancel);

        QuollPopup qp = QuollPopup.builder ()
            .title (title)
            .styleClassName (StyleClassNames.QUESTION)
            .content (f)
            .onClose (onClose)
            .withClose (true)
            .withViewer (showOn)
            .hideOnEscape (true)
            .build ();

        Button cancel = f.getCancelButton ();

        if (cancel != null)
        {

            cancel.addEventHandler (ActionEvent.ACTION,
                                    ev -> qp.close ());

        }

        Button confirm = f.getConfirmButton ();

        if (confirm != null)
        {

            confirm.addEventHandler (ActionEvent.ACTION,
                                     ev -> qp.close ());

        }

        qp.show ();

        return qp;

    }

}
