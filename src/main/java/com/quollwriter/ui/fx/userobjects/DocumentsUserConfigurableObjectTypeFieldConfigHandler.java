package com.quollwriter.ui.fx.userobjects;

import java.time.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class DocumentsUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private DocumentsUserConfigurableObjectTypeField field = null;

    public DocumentsUserConfigurableObjectTypeFieldConfigHandler (DocumentsUserConfigurableObjectTypeField f)
    {

        this.field = f;

    }

    @Override
    public StringProperty getConfigurationDescription ()
    {

        return null;

    }

    @Override
    public boolean updateFromExtraFormItems ()
    {

        return true;

    }

    @Override
    public Set<StringProperty> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {

        return new LinkedHashSet<> ();

    }

    @Override
    public Set<Form.Item> getExtraFormItems ()
    {

        return null;

    }

}
