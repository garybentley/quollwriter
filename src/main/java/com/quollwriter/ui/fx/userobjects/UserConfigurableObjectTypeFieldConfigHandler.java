package com.quollwriter.ui.fx.userobjects;

import java.util.*;

import javafx.beans.property.*;

import com.quollwriter.ui.fx.components.*;
import com.quollwriter.data.*;

public interface UserConfigurableObjectTypeFieldConfigHandler
{

    public Set<Form.Item> getExtraFormItems ();

    public boolean updateFromExtraFormItems ();

    public Set<StringProperty> getExtraFormItemErrors (UserConfigurableObjectType objType);

    public StringProperty getConfigurationDescription ();

}
