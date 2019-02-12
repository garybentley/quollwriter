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

    public static QuollPopup showErrorMessage (Window    window,
                                               String... message)
    {

        return ComponentUtils.showErrorMessage (window,
                                                getUILanguageStringProperty (message));

    }

    public static QuollPopup showErrorMessage (Window         window,
                                               StringProperty message)
    {

        return ComponentUtils.showErrorMessage (window.getScene ().getRoot (),
                                                message);

    }

    public static QuollPopup showErrorMessage (Node      showOn,
                                               String... message)
    {

        return ComponentUtils.showErrorMessage (showOn,
                                                getUILanguageStringProperty (message));

    }

    public static QuollPopup showErrorMessage (Node           showOn,
                                               StringProperty message)
    {

        VBox b = new VBox ();

        // TODO Style these?
        Text t = new Text ();
        t.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (errormessage,text),
                                                             //"%s<br /><br /><a href='%s:%s'>Click here to contact Quoll Writer support about this problem.</a>",
                                                             message,
                                                             Constants.ACTION_PROTOCOL,
                                                             AbstractViewer.Command.reportbug.toString ()));

        QuollPopup qp = ComponentUtils.showMessage (showOn,
                                                    StyleClassNames.ERROR,
                                                    getUILanguageStringProperty (errormessage,title),
                                                    t);

        t.setOnMouseClicked (ev -> qp.close ());

        return qp;

    }

    public static QuollPopup showMessage (Window         window,
                                          StringProperty message)
    {

        return ComponentUtils.showMessage (window.getScene ().getRoot (),
                                           message);

    }

    public static QuollPopup showMessage (Node showOn,
                                          StringProperty message)
    {

        return ComponentUtils.showMessage (showOn,
                                           getUILanguageStringProperty (generalmessage,title),
                                           message);

    }

    public static QuollPopup showMessage (Window         window,
                                          StringProperty title,
                                         StringProperty message)
    {

        return ComponentUtils.showMessage (window.getScene ().getRoot (),
                                           title,
                                           message);

    }

    public static QuollPopup showMessage (Window         window,
                                          String         styleName,
                                          StringProperty title,
                                          Node           content,
                                          Set<Button>    buttons)
    {

        return ComponentUtils.showMessage (window.getScene ().getRoot (),
                                           styleName,
                                           title,
                                           content,
                                           buttons);

    }

    public static QuollPopup showMessage (Node           showOn,
                                          String         styleName,
                                          StringProperty title,
                                          Node           content,
                                          Set<Button>    buttons)
    {

        VBox b = new VBox ();

        ButtonBar bb = new ButtonBar ();
        bb.getButtons ().addAll (buttons);

        b.getChildren ().addAll (content, bb);

        QuollPopup qp = QuollPopup.builder ()
            .title ((title != null ? title : getUILanguageStringProperty (generalmessage,LanguageStrings.title)))
            .styleClassName (styleName != null ? styleName : StyleClassNames.MESSAGE)
            .content (b)
            .withClose (true)
            .hideOnEscape (true)
            .build ();

        qp.show (showOn,
                 300,
                 300);

        return qp;

    }

    public static QuollPopup showMessage (Node           showOn,
                                          StringProperty title,
                                          StringProperty message)
    {

        Text t = new Text ();
        t.textProperty ().bind (message);

        return ComponentUtils.showMessage (showOn,
                                           title,
                                           t);

    }

    public static QuollPopup showMessage (Node   showOn,
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

    public static QuollPopup showMessage (Node           showOn,
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
                                                  Runnable       onConfirm,
                                                  Node           showOn)
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
                                                  final Runnable  onConfirm,
                                                  final Runnable  onCancel,
                                                  final Runnable  onClose,
                                                  Node            showOn)
    {

        Form f = Form.builder ()
            .styleClassName (style != null ? style : StyleClassNames.QUESTION)
            .description (message)
            .confirmButton ((confirmButtonLabel != null ? confirmButtonLabel : getUILanguageStringProperty (buttons,confirm)),
                            ev ->
                            {

                                UIUtils.runLater (onConfirm);

                            })
            .cancelButton ((cancelButtonLabel != null ? cancelButtonLabel : getUILanguageStringProperty (buttons,cancel)),
                           ev ->
                           {

                               UIUtils.runLater (onCancel);

                           })
            .build ();

        QuollPopup qp = QuollPopup.builder ()
            .title (title)
            .styleClassName (StyleClassNames.QUESTION)
            .content (f)
            .onClose (onClose)
            .withClose (true)
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

        qp.show (showOn,
                 300,
                 300);

        return qp;

    }

}
