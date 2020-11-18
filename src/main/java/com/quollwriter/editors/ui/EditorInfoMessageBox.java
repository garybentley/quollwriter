package com.quollwriter.editors.ui;

import java.awt.image.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.embed.swing.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.editors.messages.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class EditorInfoMessageBox extends MessageBox<EditorInfoMessage>
{

    private VBox responseBox = null;

    public EditorInfoMessageBox (EditorInfoMessage mess,
                                 AbstractViewer    viewer)
    {

        super (mess,
               viewer);

        mess.dealtWithProperty ().addListener ((pr, oldv, newv) ->
        {

            this.responseBox.setVisible (!newv);

        });

        this.getStyleClass ().add (StyleClassNames.EDITORINFO);

        List<String> prefix = Arrays.asList (editors,messages,contactinfo);

        StringProperty text = getUILanguageStringProperty (prefix,sent,title);
        //"Sent name/avatar";

        if (!this.message.isSentByMe ())
        {

            text = getUILanguageStringProperty (prefix,received,title);
            //"Received name/avatar update";

        }

        QuollLabel h = QuollLabel.builder ()
            .label (text)
            .styleClassName (StyleClassNames.TITLE)
            .build ();

        this.getChildren ().add (h);

        HBox edInfo = new HBox ();

        this.getChildren ().add (edInfo);

        if (this.message.getAvatar () != null)
        {

            edInfo.getChildren ().add (new ImageView (this.message.getAvatar ()));

        }

        String n = this.message.getName ();

        if (n == null)
        {

            n = this.message.getEditor ().getMainName ();

        }

        QuollLabel name = QuollLabel.builder ()
            .label (new SimpleStringProperty (n))
            .styleClassName (StyleClassNames.NAME)
            .build ();

        edInfo.getChildren ().add (name);

        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
           )
        {

            this.responseBox = new VBox ();

            this.getChildren ().add (this.responseBox);

            QuollButton update = QuollButton.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,received,buttons,LanguageStrings.update)))
                .onAction (ev ->
                {

                    EditorEditor ed = this.message.getEditor ();

                    try
                    {

                        // Just update the info.
                        String newName = this.message.getName ();

                        if (newName != null)
                        {

                            ed.setName (newName.trim ());

                        }

                        Image newImage = this.message.getAvatar ();

                        if (newImage != null)
                        {

                            ed.setAvatar (newImage);

                        }

                        EditorsEnvironment.updateEditor (ed);

                        this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (this.message);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,editor,edit,actionerror));
                                                  //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ();

            QuollButton ignore = QuollButton.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,received,buttons,LanguageStrings.ignore)))
                .onAction (ev ->
                {

                    try
                    {

                        this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (this.message);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update message: " +
                                              this.message,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,editor,edit,actionerror));
                                                  //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ();

            QuollButtonBar bb = QuollButtonBar.builder ()
                .button (update)
                .button (ignore)
                .build ();

            this.responseBox.getChildren ().add (bb);

        }

    }

    public boolean isAutoDealtWith ()
    {

        return false;

    }

}
