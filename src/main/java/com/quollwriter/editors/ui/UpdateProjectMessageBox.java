package com.quollwriter.editors.ui;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class UpdateProjectMessageBox extends MessageBox<UpdateProjectMessage>
{

    private VBox responseBox = null;
    private Label previousLabel = null;

    public UpdateProjectMessageBox (UpdateProjectMessage mess,
                                    AbstractViewer       viewer)
    {

        super (mess,
               viewer);

        StringProperty text = getUILanguageStringProperty (editors,messages,updateproject,sent,title);
        //"{Project} update sent";//Sent {project} update";

       if (!this.message.isSentByMe ())
       {

           text = getUILanguageStringProperty (editors,messages,updateproject,received,title);
           //"{Project} update received"; //Received {project} update";

       }

       this.binder.addChangeListener (this.message.dealtWithProperty (),
                                      (pr, oldv, newv) ->
       {

           this.update ();

       });

       this.binder.addChangeListener (this.message.getEditor ().editorStatusProperty (),
                                      (pr, oldv, newv) ->
       {

           this.update ();

       });

       this.getStyleClass ().add (StyleClassNames.UPDATEPROJECTMESSAGE);

       this.getChildren ().add (Header.builder ()
        .title (text)
        .iconClassName (StyleClassNames.PROJECT)
        .styleClassName (StyleClassNames.HEADER)
        .build ());

       Node details = EditorsUIUtils.getProjectMessageDetails (this.message,
                                                               this.viewer,
                                                               this);

       details.getStyleClass ().add (StyleClassNames.MESSAGEDETAILS);
       this.getChildren ().add (details);

       this.responseBox = new VBox ();
       this.responseBox.getStyleClass ().add (StyleClassNames.RESPONSE);
       this.responseBox.managedProperty ().bind (this.responseBox.visibleProperty ());
       this.responseBox.setVisible (false);
       this.getChildren ().add (this.responseBox);

       if (!this.message.isSentByMe ())
       {

           // Not sent by me.
           if ((!this.message.isDealtWith ())
               &&
               (!this.message.getEditor ().isPrevious ())
              )
           {

               QuollButtonBar responseButs = QuollButtonBar.builder ()
                    .button (QuollButton.builder ()
                        .label (editors,messages,updateproject,received,undealtwith,buttons,LanguageStrings.update)
                        .onAction (ev ->
                        {

                            EditorsUIUtils.showProjectUpdate (this.message,
                                                              this.viewer,
                                                              null);

                        })
                        .build ())
                    .build ();

               this.responseBox.getChildren ().add (responseButs);
               this.responseBox.setVisible (true);

           }

       }

       this.previousLabel = QuollLabel.builder ()
        .styleClassName (StyleClassNames.PREVIOUS)
        .build ();

    }

    private void update ()
    {

        if (this.message.isDealtWith ())
        {

            this.responseBox.setVisible (false);

        }

        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPrevious ())
           )
        {

            this.previousLabel.textProperty ().unbind ();
            this.previousLabel.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,messages,updateproject,received,undealtwith,previouseditor),
                                                        //"<b>%s</b> is a previous {contact}.  This message can no longer be acted upon.",
                                                                                  this.message.getEditor ().getMainName ()));
            this.previousLabel.setVisible (true);
            this.responseBox.setVisible (false);

        }

    }

    @Override
    public boolean isShowAttentionBorder ()
    {

        if (this.message.getEditor ().isPrevious ())
        {

            return false;

        }

        return super.isShowAttentionBorder ();

    }

    @Override
    public boolean isAutoDealtWith ()
    {

        return false;

    }

}
