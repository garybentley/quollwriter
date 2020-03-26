package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.nio.file.*;

import org.jdom.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class RenameAssetPopup extends PopupContent<ProjectViewer>
{

    private static final String POPUP_ID = "renameasset";

    private Asset asset = null;

    public RenameAssetPopup (ProjectViewer viewer,
                             Asset         asset)
    {

        super (viewer);

        this.asset = asset;

        QuollTextField text = QuollTextField.builder ()
            .text (this.asset.getName ())
            .build ();

        Form f = Form.builder ()
            .inViewer (this.viewer)
            .confirmButton (LanguageStrings.project,actions,renameasset,confirm)
            .cancelButton (actions,cancel)
            .description (getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,renameasset,LanguageStrings.text),
                                                       this.asset.getUserConfigurableObjectType ().nameProperty ()))
            .item (text)
            .build ();

        f.setOnCancel (ev ->
        {

            this.close ();

        });

        f.setOnConfirm (ev ->
        {

            String v = text.getText ().trim ();

            if ((v == null)
                ||
                (v.length () == 0)
               )
            {

                ev.consume ();

                f.showError (getUILanguageStringProperty (LanguageStrings.project,actions,renameasset,errors,novalue));
                //"Please enter a new name.";

                return;

            }

            boolean exists = false;

            Set<Asset> cs = this.viewer.getProject ().getAllAssetsByName (v,
                                                                          this.asset.getUserConfigurableObjectType ());

            if (cs.size () > 0)
            {

                for (Asset a : cs)
                {

                    if (a.getKey () != this.asset.getKey ())
                    {

                        exists = true;

                    }

                }

            }

            if (exists)
            {

                f.showError (getUILanguageStringProperty (Arrays.asList (project,actions,renameasset,errors,valueexists),
                                                          this.asset.getUserConfigurableObjectType ().nameProperty ()));

                return;

            }

            try
            {

                this.asset.setName (v);

                this.viewer.saveObject (this.asset,
                                        true);

                this.viewer.fireProjectEvent (ProjectEvent.Type.asset,
                                              ProjectEvent.Action.rename,
                                              this.asset);

                this.close ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to change name of asset: " +
                                      this.asset +
                                      " to: " +
                                      v,
                                      e);

                f.showError (getUILanguageStringProperty (project,actions,renamechapter,actionerror));
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
            .title (getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,renameasset,title),
                                                 this.asset.getUserConfigurableObjectType ().nameProperty ()))
            .styleClassName (StyleClassNames.RENAME)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (RenameAssetPopup.getPopupIdForAsset (this.asset))
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.asset,
                                          ProjectEvent.Action.rename);

        return p;

    }

    public static String getPopupIdForAsset (Asset a)
    {

        return POPUP_ID + a.getKey ();

    }

}
