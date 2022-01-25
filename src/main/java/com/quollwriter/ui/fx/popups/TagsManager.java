package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.util.stream.*;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import com.quollwriter.*;
import com.quollwriter.data.Tag;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TagsManager extends PopupContent
{

    public static final String POPUP_ID = "tagsmanager";

    public TagsManager (AbstractViewer viewer)
    {

        super (viewer);

        VBox b = new VBox ();

        TextItemManager man = TextItemManager.builder ()
            .addTitle (getUILanguageStringProperty (tags,actions,manage,newtag,title))
            .addDescription (getUILanguageStringProperty (tags,actions,manage,newtag,text))
            .currentItemsTitle (getUILanguageStringProperty (tags,actions,manage,table,title))
            .currentItemsDescription (getUILanguageStringProperty (tags,actions,manage,table,text))
            .valueExistsError (getUILanguageStringProperty (tags,actions,manage,table,edit,errors,valueexists))
            .noValueError (getUILanguageStringProperty (tags,actions,manage,table,edit,errors,novalue))
            .items (FXCollections.observableList (Environment.getAllTags ().stream ()
                        .map (p -> p.getName ())
                        .collect (Collectors.toList ())))
            .build ();

        b.getChildren ().add (man);

        man.setOnItemRemoved (ev ->
        {

            String v = ev.getOldValue ();
            Tag t = Environment.getTagByName (v);

            if (t != null)
            {

                try
                {

                    Environment.deleteTag (t);

                } catch (Exception e) {

                    Environment.logError ("Unable to delete tag: " + t,
                                          e);

                    man.showExistingError (getUILanguageStringProperty (tags,actions,manage,table,delete,actionerror));

                }

            }

        });

        man.setOnItemAdded (ev ->
        {

            String v = ev.getNewValue ();

            if ((v == null)
                ||
                (v.trim ().length () == 0)
               )
            {

                man.showNewError (getUILanguageStringProperty (tags,actions,manage,newtag,errors,novalue));
                return;

            }

            // TODO Remove... Won't ever be hit.
            if (Environment.getTagByName (v) != null)
            {

                man.showNewError (getUILanguageStringProperty (tags,actions,manage,newtag,errors,valueexists));
                return;

            }

            try
            {

                Tag t = new Tag ();
                t.setName (v);

                Environment.saveTag (t);

            } catch (Exception e) {

                Environment.logError ("Unable to save tag: " +
                                      v,
                                      e);

                man.showNewError (getUILanguageStringProperty (tags,actions,manage,newtag,actionerror));

                return;

            }

        });

        man.setOnItemChanged (ev ->
        {

            String v = ev.getNewValue ();
            String n = ev.getOldValue ();

            if (v.equals (n))
            {

                return;

            }

            // TODO Remove won't ever be hit.
            if ((v == null)
                ||
                (v.trim ().length () == 0)
               )
            {

                man.showExistingError (getUILanguageStringProperty (tags,actions,manage,table,edit,errors,novalue));
                return;

            }

            Tag ot = Environment.getTagByName (n);
            Tag nt = Environment.getTagByName (v);

            // TODO Remove check, won't ever be hit.
            if (nt != null)
            {

                if (!nt.equals (ot))
                {

                    man.showExistingError (getUILanguageStringProperty (tags,actions,manage,table,edit,errors,valueexists));
                    return;

                }

            } else {

                ot.setName (v);

                try
                {

                    Environment.saveTag (ot);

                } catch (Exception e) {

                    Environment.logError ("Unable to update tag: " + ot,
                                          e);

                    man.showExistingError (getUILanguageStringProperty (tags,actions,manage,table,edit,actionerror));

                }

            }

        });

        b.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (tags,actions,manage,finish)
                        .onAction (ev ->
                        {

                            this.close ();

                        })
                        .build ())
            .build ());

        this.getChildren ().add (b);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (tags,actions,manage,title)
            .styleClassName (StyleClassNames.TAGSMANAGER)
            .headerIconClassName (StyleClassNames.TAG)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        return p;

    }

}
