package com.quollwriter.editors.messages;

@FunctionalInterface
public interface EditorMessageFilter
{

    public boolean accept (EditorMessage m);

}
