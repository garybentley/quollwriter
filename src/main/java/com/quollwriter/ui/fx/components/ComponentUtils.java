package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

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
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class ComponentUtils
{

    public static void showErrorMessage (StringProperty message)
    {

        // TODO

    }

    public static QuollPopup showErrorMessage (AbstractViewer showOn,
                                               String...      message)
    {

        return ComponentUtils.showErrorMessage (showOn,
                                                showOn,
                                                message);

    }

    public static QuollPopup showErrorMessage (AbstractViewer showOn,
                                               StringProperty message)
    {
System.out.println ("HERE: " + message.getValue ());
        return ComponentUtils.showErrorMessage (showOn,
                                                showOn,
                                                message);

    }

    public static QuollPopup showErrorMessage (URLActionHandler handler,
                                               PopupsViewer     showOn,
                                               String...        message)
    {

        return ComponentUtils.showErrorMessage (handler,
                                                showOn,
                                                getUILanguageStringProperty (message));

    }

    public static QuollPopup showErrorMessage (URLActionHandler handler,
                                               PopupsViewer     showOn,
                                               StringProperty   message)
    {

        return QuollPopup.errorBuilder ()
            .withViewer (showOn)
            .withHandler (handler)
            .message (message)
            .build ();

    }
/*
TODO Remove
    public static QuollPopup showErrorMessage (URLActionHandler handler,
                                               PopupsViewer     showOn,
                                               StringProperty   message)
    {

        TextFlow t = BasicHtmlTextFlow.builder ()
            .text (getUILanguageStringProperty (Arrays.asList (errormessage,text),
                                                //"%s<br /><br /><a href='%s:%s'>Click here to contact Quoll Writer support about this problem.</a>",
                                                message,
                                                Constants.ACTION_PROTOCOL,
                                                AbstractViewer.CommandId.reportbug))
            .withHandler (handler)
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
*/
/*
    public static QuollPopup showMessage (AbstractViewer showOn,
                                          StringProperty message)
    {

        return ComponentUtils.showMessage (showOn,
                                           showOn,
                                           message);

    }
*/
/*
TODO Remove
    public static QuollPopup showMessage (AbstractViewer showOn,
                                          String...      message)
    {

        return ComponentUtils.showMessage (showOn,
                                           showOn,
                                           message);

    }
    */
/*
TODO Remove
    public static QuollPopup showMessage (URLActionHandler handler,
                                          PopupsViewer     showOn,
                                          String...        message)
    {

        return ComponentUtils.showMessage (handler,
                                           showOn,
                                           getUILanguageStringProperty (message));

    }
    */
/*
TODO Remove
    public static QuollPopup showMessage (URLActionHandler handler,
                                          PopupsViewer     showOn,
                                          StringProperty   message)
    {

        return QuollPopup.messageBuilder ()
            .withViewer (showOn)
            .withHandler (handler)
            .message (message)
            .build ();

    }
*/
/*
TODO Remove
    public static QuollPopup showMessage (PopupsViewer   showOn,
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

        UIUtils.runLater (() ->
        {

            qp.toFront ();

        });

        return qp;

    }
    */
/*
TODO remove
    public static QuollPopup showMessage (AbstractViewer showOn,
                                          StringProperty title,
                                          StringProperty message)
    {

        return ComponentUtils.showMessage (showOn,
                                           showOn,
                                           title,
                                           message);

    }
    */
/*
TODO Remove
    public static QuollPopup showMessage (URLActionHandler handler,
                                          PopupsViewer     showOn,
                                          StringProperty   title,
                                          StringProperty   message)
    {

        BasicHtmlTextFlow t = BasicHtmlTextFlow.builder ()
            .withHandler (handler)
            .text (message)
            .build ();

        return ComponentUtils.showMessage (showOn,
                                           title,
                                           t);

    }
    */
/*
TODO Remove
    public static QuollPopup showMessage (PopupsViewer showOn,
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
*/
    public static Button createCloseButton (EventHandler<ActionEvent> onAction)
    {

        return QuollButton.builder ()
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .label (getUILanguageStringProperty (buttons,close))
            .onAction (onAction)
            .build ();

    }
/*
TODO Remove
    public static QuollPopup showMessage (PopupsViewer   showOn,
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
*/
/*
TODO Remove
    public static QuollPopup createQuestionPopup (StringProperty title,
                                                  String         style,
                                                  StringProperty message,
                                                  StringProperty confirmButtonLabel,
                                                  StringProperty cancelButtonLabel,
                                                  EventHandler<ActionEvent> onConfirm,
                                                  URLActionHandler handler,
                                                  PopupsViewer   showOn)
    {

        return ComponentUtils.createQuestionPopup (title,
                                            style,
                                            message,
                                            confirmButtonLabel,
                                            cancelButtonLabel,
                                            onConfirm,
                                            null,
                                            null,
                                            handler,
                                            showOn);

    }
*/
/*
TODO Remove
    public static QuollPopup createYesConfirmPopup (StringProperty  title,
                                                    String          style,
                                                    StringProperty  message,
                                                    StringProperty  entryLabel,
                                                    StringProperty  confirmButtonLabel,
                                                   StringProperty  cancelButtonLabel,
                                                   final EventHandler<Form.FormEvent>  onConfirm,
                                                   final EventHandler<Form.FormEvent>  onCancel,
                                                   final Runnable  onClose,
                                                   AbstractViewer  viewer)
    {

        return ComponentUtils.createYesConfirmPopup (title,
                                                     style,
                                                     message,
                                                     entryLabel,
                                                     confirmButtonLabel,
                                                     cancelButtonLabel,
                                                     onConfirm,
                                                     onCancel,
                                                     onClose,
                                                     viewer,
                                                     viewer);

    }
    */
/*
TODO REmove
    public static QuollPopup createYesConfirmPopup (StringProperty  title,
                                                    String          style,
                                                    StringProperty  message,
                                                    StringProperty  entryLabel,
                                                    StringProperty  confirmButtonLabel,
                                                   StringProperty  cancelButtonLabel,
                                                   final EventHandler<Form.FormEvent>  onConfirm,
                                                   final EventHandler<Form.FormEvent>  onCancel,
                                                   final Runnable  onClose,
                                                   URLActionHandler handler,
                                                   PopupsViewer  showOn)
    {

        return ComponentUtils.createTextEntryPopup (title,
                                                    style,
                                                    message,
                                                    null,
                                                    v ->
                                                    {

                                                        if ((v == null)
                                                            ||
                                                            (!v.trim ().equalsIgnoreCase (getUIString (form,affirmativevalue)))
                                                           )
                                                        {

                                                            return getUILanguageStringProperty (form,errors,affirmativevalue);
                                                            //"Please enter the word Yes below.";

                                                        }

                                                        return null;

                                                    },
                                                    confirmButtonLabel,
                                                    cancelButtonLabel,
                                                    onConfirm,
                                                    onCancel,
                                                    onClose,
                                                    handler,
                                                    showOn);

    }
    */
/*
TODO Remove
    public static QuollPopup createTextEntryPopup (StringProperty  title,
                                                   String          style,
                                                   StringProperty  message,
                                                   StringProperty  entryLabel,
                                                   ValueValidator<String> validator,
                                                   StringProperty  confirmButtonLabel,
                                                   StringProperty  cancelButtonLabel,
                                                   final EventHandler<Form.FormEvent>  onConfirm,
                                                   final EventHandler<Form.FormEvent>  onCancel,
                                                   final Runnable  onClose,
                                                   AbstractViewer  viewer)
    {

        return ComponentUtils.createTextEntryPopup (title,
                                                    style,
                                                    message,
                                                    entryLabel,
                                                    validator,
                                                    confirmButtonLabel,
                                                    cancelButtonLabel,
                                                    onConfirm,
                                                    onCancel,
                                                    onClose,
                                                    viewer,
                                                    viewer);

    }
*/
/*
TODO Remove
    public static QuollPopup createTextEntryPopup (StringProperty  title,
                                                   String          style,
                                                   StringProperty  message,
                                                   StringProperty  entryLabel,
                                                   ValueValidator<String> validator,
                                                   StringProperty  confirmButtonLabel,
                                                   StringProperty  cancelButtonLabel,
                                                   final EventHandler<Form.FormEvent>  onConfirm,
                                                   final EventHandler<Form.FormEvent>  onCancel,
                                                   final Runnable  onClose,
                                                   URLActionHandler handler,
                                                   PopupsViewer  showOn)
    {

        if (validator == null)
        {

            throw new IllegalArgumentException ("Expected a validator.");

        }

        QuollTextField tf = QuollTextField.builder ()
            .build ();
        // TODO Make a constant.
        tf.setId ("text");

        Form f = Form.builder ()
            .styleClassName (StyleClassNames.TEXTENTRY)
            .description (message)
            .withHandler (handler)
            .item (entryLabel,
                   tf)
            .confirmButton ((confirmButtonLabel != null ? confirmButtonLabel : getUILanguageStringProperty (buttons,confirm)))
            .cancelButton ((cancelButtonLabel != null ? cancelButtonLabel : getUILanguageStringProperty (buttons,cancel)))
            .build ();

        f.setOnCancel (onCancel);

        QuollPopup qp = QuollPopup.builder ()
            .title (title)
            .styleClassName (style != null ? style : StyleClassNames.QUESTION)
            .content (f)
            .onClose (onClose)
            .withClose (true)
            .withViewer (showOn)
            .hideOnEscape (true)
            .removeOnClose (true)
            .build ();

        Button cancel = f.getCancelButton ();

        if (cancel != null)
        {

            cancel.addEventHandler (ActionEvent.ACTION,
                                    ev -> qp.close ());

        }

        Runnable r = () ->
        {

            f.hideError ();

            StringProperty m = validator.isValid (tf.getText ());

            if (m != null)
            {

                f.showError (m);
                return;

            }

            onConfirm.handle (new Form.FormEvent (f,
                                                  Form.FormEvent.CONFIRM_EVENT));

            qp.close ();

        };

        f.setOnConfirm (ev ->
        {

            UIUtils.runLater (r);

        });

        qp.show ();

        UIUtils.runLater (() ->
        {

            tf.requestFocus ();

        });

        return qp;

    }
*/
    public static QuollPopup createPasswordEntryPopup (StringProperty  title,
                                                       String          style,
                                                       StringProperty  message,
                                                       StringProperty  entryLabel,
                                                       ValueValidator<String> validator,
                                                       StringProperty  confirmButtonLabel,
                                                       StringProperty  cancelButtonLabel,
                                                       final Consumer<String> onProvided,
                                                       final Runnable         onCancel,
                                                       final Runnable  onClose,
                                                       AbstractViewer  viewer)
    {

        return ComponentUtils.createPasswordEntryPopup (title,
                                                        style,
                                                        message,
                                                        entryLabel,
                                                        validator,
                                                        confirmButtonLabel,
                                                        cancelButtonLabel,
                                                        onProvided,
                                                        onCancel,
                                                        onClose,
                                                        viewer,
                                                        viewer);

    }

    public static QuollPopup createPasswordEntryPopup (StringProperty  title,
                                                       String          style,
                                                       StringProperty  message,
                                                       StringProperty  entryLabel,
                                                       ValueValidator<String> validator,
                                                       StringProperty  confirmButtonLabel,
                                                       StringProperty  cancelButtonLabel,
                                                       final Consumer<String> onProvided,
                                                       final Runnable         onCancel,
                                                       final Runnable  onClose,
                                                       URLActionHandler handler,
                                                       PopupsViewer  showOn)
    {

        if (validator == null)
        {

            throw new IllegalArgumentException ("Expected a validator.");

        }

        PasswordField tf = new PasswordField ();
        // TODO Make a constant.
        tf.setId ("password");

        Form f = Form.builder ()
            .styleClassName (style != null ? style : StyleClassNames.PASSWORD)
            .description (message)
            .withHandler (handler)
            .item (entryLabel,
                   tf)
            .confirmButton ((confirmButtonLabel != null ? confirmButtonLabel : getUILanguageStringProperty (buttons,confirm)))
            .cancelButton ((cancelButtonLabel != null ? cancelButtonLabel : getUILanguageStringProperty (buttons,cancel)))
            .build ();

        Runnable r = () ->
        {

            f.hideError ();

            StringProperty m = validator.isValid (tf.getText ());

            if (m != null)
            {

                f.showError (m);
                return;

            }

            onProvided.accept (tf.getText ());

        };

        UIUtils.addDoOnReturnPressed (tf,
                                      r);

        f.setOnConfirm (ev ->
        {

            UIUtils.runLater (r);

        });
        f.setOnCancel (ev ->
        {

            UIUtils.runLater (onCancel);

        });

        QuollPopup qp = QuollPopup.builder ()
            .title (title)
            .styleClassName (StyleClassNames.PASSWORD)
            .content (f)
            .onClose (onClose)
            .withClose (true)
            .withViewer (showOn)
            .hideOnEscape (true)
            .removeOnClose (true)
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

    public static QuollPopup createQuestionPopup (StringProperty  title,
                                                  String          style,
                                                  StringProperty  message,
                                                  StringProperty  confirmButtonLabel,
                                                  StringProperty  cancelButtonLabel,
                                                  EventHandler<ActionEvent> onConfirm,
                                                  EventHandler<ActionEvent> onCancel,
                                                  Runnable  onClose,
                                                  AbstractViewer  viewer)
    {

        return ComponentUtils.createQuestionPopup (title,
                                                   style,
                                                   message,
                                                   confirmButtonLabel,
                                                   cancelButtonLabel,
                                                   onConfirm,
                                                   onCancel,
                                                   onClose,
                                                   viewer,
                                                   viewer);

    }

    public static QuollPopup createQuestionPopup (StringProperty  title,
                                                  String          style,
                                                  StringProperty  message,
                                                  StringProperty  confirmButtonLabel,
                                                  StringProperty  cancelButtonLabel,
                                                  EventHandler<ActionEvent> onConfirm,
                                                  EventHandler<ActionEvent> onCancel,
                                                  Runnable  onClose,
                                                  URLActionHandler handler,
                                                  PopupsViewer  showOn)
    {

        Button confirm = QuollButton.builder ()
            .label ((confirmButtonLabel != null ? confirmButtonLabel : getUILanguageStringProperty (buttons,LanguageStrings.confirm)))
            .buttonType (ButtonBar.ButtonData.OK_DONE)
            .styleClassName (StyleClassNames.CONFIRM)
            .onAction (onConfirm)
            .build ();

        Set<Button> buts = new LinkedHashSet<> ();
        buts.add (confirm);

        QuollPopup qp = ComponentUtils.createQuestionPopup (title,
                                                   style,
                                                   message,
                                                   buts,
                                                   cancelButtonLabel,
                                                   onCancel,
                                                   onClose,
                                                   handler,
                                                   showOn);

        confirm.addEventHandler (ActionEvent.ACTION,
                                 ev -> qp.close ());

        return qp;

    }

    public static QuollPopup createQuestionPopup (StringProperty  title,
                                                  String          style,
                                                  StringProperty  message,
                                                  Set<Button>     buttons,
                                                  StringProperty  cancelButtonLabel,
                                                  EventHandler<ActionEvent> onCancel,
                                                  AbstractViewer viewer)
    {

        return ComponentUtils.createQuestionPopup (title,
                                                   style,
                                                   message,
                                                   buttons,
                                                   cancelButtonLabel,
                                                   onCancel,
                                                   // On close
                                                   null,
                                                   viewer);

    }

    public static QuollPopup createQuestionPopup (StringProperty  title,
                                                  String          style,
                                                  StringProperty  message,
                                                  Set<Button>     buttons,
                                                  StringProperty  cancelButtonLabel,
                                                  EventHandler<ActionEvent> onCancel,
                                                  Runnable  onClose,
                                                  AbstractViewer viewer)
    {

        return ComponentUtils.createQuestionPopup (title,
                                                   style,
                                                   message,
                                                   buttons,
                                                   cancelButtonLabel,
                                                   onCancel,
                                                   onClose,
                                                   viewer,
                                                   viewer);

    }

    public static QuollPopup createQuestionPopup (StringProperty  title,
                                                  String          style,
                                                  StringProperty  message,
                                                  Set<Button>     buttons,
                                                  StringProperty  cancelButtonLabel,
                                                  EventHandler<ActionEvent> onCancel,
                                                  Runnable  onClose,
                                                  URLActionHandler handler,
                                                  PopupsViewer  showOn)
    {

        BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.MESSAGE)
            .text (message)
            .withHandler (handler)
            .build ();

        return ComponentUtils.createQuestionPopup (title,
                                                   style,
                                                   desc,
                                                   buttons,
                                                   cancelButtonLabel,
                                                   onCancel,
                                                   onClose,
                                                   showOn);

    }

    public static QuollPopup createQuestionPopup (StringProperty  title,
                                                  String          style,
                                                  Node            message,
                                                  Set<Button>     buttons,
                                                  StringProperty  cancelButtonLabel,
                                                  EventHandler<ActionEvent> onCancel,
                                                  Runnable  onClose,
                                                  PopupsViewer  showOn)
    {

        VBox b = new VBox ();
        b.setFillWidth (true);

/*
        Button confirm = QuollButton.builder ()
            .label ((confirmButtonLabel != null ? confirmButtonLabel : getUILanguageStringProperty (buttons,LanguageStrings.confirm)))
            .buttonType (ButtonBar.ButtonData.OK_DONE)
            .styleClassName (StyleClassNames.CONFIRM)
            .onAction (onConfirm)
            .build ();

        Button cancel = QuollButton.builder ()
            .label ((cancelButtonLabel != null ? cancelButtonLabel : getUILanguageStringProperty (buttons,LanguageStrings.cancel)))
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .styleClassName (StyleClassNames.CANCEL)
            .onAction (onCancel)
            .build ();
*/
        Set<Button> buts = new LinkedHashSet<> (buttons);

        Button cancel = QuollButton.builder ()
            .label ((cancelButtonLabel != null ? cancelButtonLabel : getUILanguageStringProperty (LanguageStrings.buttons,LanguageStrings.cancel)))
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .styleClassName (StyleClassNames.CANCEL)
            .onAction (onCancel)
            .build ();

        buts.add (cancel);

        Node bb = QuollButtonBar.builder ()
            .buttons (buts)
            .build ();

        b.getChildren ().addAll (message, bb);

        QuollPopup qp = QuollPopup.builder ()
            .title (title)
            .styleClassName (style != null ? style : StyleClassNames.QUESTION)
            .content (b)
            .onClose (onClose)
            .withClose (true)
            .withViewer (showOn)
            .hideOnEscape (true)
            .removeOnClose (true)
            .build ();

        if (style != null)
        {

            qp.getStyleClass ().add (StyleClassNames.QUESTION);

        }

        cancel.addEventHandler (ActionEvent.ACTION,
                                ev -> qp.close ());

        qp.show ();

        return qp;

    }

}
