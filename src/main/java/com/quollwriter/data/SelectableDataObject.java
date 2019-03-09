package com.quollwriter.data;

public class SelectableDataObject
{

    public boolean     selected = false;
    public NamedObject obj = null;
    public boolean     parentNode = false;

    public SelectableDataObject(NamedObject obj)
    {

        this.obj = obj;

    }

    public String toString ()
    {
        
        return "selectable-data-object(selected: " + this.selected + ", object: " + this.obj + ")";
        
    }

}
