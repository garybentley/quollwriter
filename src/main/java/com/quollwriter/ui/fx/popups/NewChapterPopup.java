package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class NewChapterPopup extends PopupContent<ProjectViewer>
{

    public static final String POPUP_ID = "createchapter";
    private QuollTextField nameField = null;

    public NewChapterPopup (Chapter       chapterToAdd,
                            ProjectViewer viewer,
                            Chapter       addAfter,
                            Runnable      afterAdd)
    {

        super (viewer);

        this.nameField = QuollTextField.builder ()
            .build ();

        NewProjectObjectPopupPanel<ProjectViewer> pp = NewProjectObjectPopupPanel.<ProjectViewer>builder ()
            .item (Arrays.asList (project,actions,newchapter,labels,name),
                   nameField)
            .showLinkedTo (true)
            .withBinder (this.getBinder ())
            .withViewer (viewer)
            .onCancel (ev -> this.close ())
            .onConfirm ((ev, links) ->
            {

                // The compiler has problems determining the type here, not sure why.
                Form.FormEvent fev = (Form.FormEvent) ev;

                // Check duplication?
                String n = nameField.getText ().trim ();

                if (n.length () == 0)
                {

                    fev.consume ();

                    fev.getForm ().showError (getUILanguageStringProperty (project,actions,newchapter,errors,novalue));
                              //"Please select a name.");

                    return;

                }

                Set<Chapter> cs = this.viewer.getProject ().getBook (0).getAllChaptersWithName (n);

                if (cs.size () > 0)
                {

                    fev.getForm ().showError (getUILanguageStringProperty (project,actions,newchapter,errors,valueexists));

                    return;

                }

                Chapter newChapter = null;

                try
                {

                    Book book = viewer.getProject ().getBooks ().get (0);

                    if (chapterToAdd != null)
                    {

                        chapterToAdd.setName (n);
                        chapterToAdd.setBook (book);

                        newChapter = chapterToAdd;
/*
                        newChapter = book.createChapterAfter (addAfter,
                                                              chapterToAdd);
*/
                    } else {

                        Chapter ch = new Chapter (book,
                                                  n);

                        ch.setBook (book);

                        newChapter = ch;

                    }

                    final Chapter _newChapter = newChapter;

                    links.stream ()
                        .forEach (o -> _newChapter.addLinkTo (o));

                    // Save to set up the key...
                    viewer.saveObject (newChapter,
                                       true);

                    // Insert the chapter into the book.
                    newChapter = book.createChapterAfter (addAfter,
                                                          newChapter);

                    // Save again to set up the index... grrr...
                    viewer.saveObject (newChapter,
                                       true);

                    viewer.editChapter (newChapter);

                    viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                             ProjectEvent.Action._new,
                                             newChapter);

                    this.close ();

                    UIUtils.runLater (afterAdd);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to add new chapter with name: " +
                                          n,
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (project,actions,newchapter,actionerror));
                                              //"Unable to add new {chapter}.");

                    return;

                }

            })
            .build ();

        this.getChildren ().add (pp);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (project,actions,newchapter,title)
            .styleClassName (StyleClassNames.CREATECHAPTER)
            .styleSheet (POPUP_ID)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        UIUtils.runLater (() ->
        {

            this.nameField.requestFocus ();

        });

        return p;

    }

}
