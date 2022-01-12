package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.nio.file.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class RenameWarmupPopup extends PopupContent<WarmupProjectViewer>
{

    private static final String POPUP_ID = "renamewarmup";

    private Warmup warmup = null;
    private QuollTextField text = null;

    public RenameWarmupPopup (WarmupProjectViewer viewer,
                              Warmup              warmup)
    {

        super (viewer);

        this.warmup = warmup;

        this.text = QuollTextField.builder ()
            .text (this.warmup.getChapter ().getName ())
            .build ();

        Form f = Form.builder ()
            .inViewer (this.viewer)
            .confirmButton (LanguageStrings.warmups,actions,renamewarmup,confirm)
            .cancelButton (actions,cancel)
            .description (LanguageStrings.warmups,actions,renamewarmup,LanguageStrings.text)
            .item (text)
            .build ();

        f.setOnCancel (ev ->
        {

            this.close ();

        });

        f.setOnConfirm (ev ->
        {

            String v = this.text.getText ().trim ();

            if ((v == null)
                ||
                (v.length () == 0)
               )
            {

                ev.consume ();

                f.showError (getUILanguageStringProperty (warmups,actions,renamewarmup,errors,novalue));
                //"Please enter a new name.";

                return;

            }

            boolean exists = false;

            Set<Chapter> cs = this.warmup.getChapter ().getBook ().getAllChaptersWithName (v);

            if (cs.size () > 0)
            {

                for (Chapter c : cs)
                {

                    if (c.getKey () != this.warmup.getChapter ().getKey ())
                    {

                        exists = true;
                        break;

                    }

                }

            }

            if (exists)
            {

                f.showError (getUILanguageStringProperty (warmups,actions,renamewarmup,errors,valueexists));
                //"Another {chapter} with that name already exists.";

                return;

            }

            try
            {

                this.warmup.getChapter ().setName (v);

                this.viewer.saveObject (this.warmup,
                                        true);

                this.viewer.fireProjectEvent (ProjectEvent.Type.warmup,
                                              ProjectEvent.Action.rename,
                                              this.warmup);

                this.close ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to change name of warmup: " +
                                      this.warmup +
                                      " to: " +
                                      v,
                                      e);

                f.showError (getUILanguageStringProperty (warmups,actions,renamewarmup,actionerror));
                //"An internal error has occurred.\n\nUnable to change name of {chapter}.");

            }

        });

        VBox b = new VBox ();
        VBox.setVgrow (f, Priority.ALWAYS);
        b.getChildren ().addAll (f);

        this.getChildren ().addAll (b);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (LanguageStrings.warmups,actions,renamewarmup,title))
            .styleClassName (StyleClassNames.RENAME)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (RenameWarmupPopup.getPopupIdForWarmup (this.warmup))
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                           ev ->
        {

            UIUtils.forceRunLater (() ->
            {

                this.text.selectAll ();
                this.text.requestFocus ();

            });

        });

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.warmup,
                                          ProjectEvent.Action.rename);

        return p;

    }

    public static String getPopupIdForWarmup (Warmup c)
    {

        return POPUP_ID + c.getKey ();

    }

}
