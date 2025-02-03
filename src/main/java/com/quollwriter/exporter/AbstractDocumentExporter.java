package com.quollwriter.exporter;

import java.util.*;

import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.fx.components.*;

public abstract class AbstractDocumentExporter implements DocumentExporter
{

    protected Project proj = null;

    public AbstractDocumentExporter ()
    {

    }

    public void setProject (Project p)
    {

        this.proj = p;

    }

}
