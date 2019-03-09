package com.quollwriter.ui.fx;

import java.util.*;

@FunctionalInterface
public interface ProjectEventListener extends EventListener
{

    public void eventOccurred (ProjectEvent ev);

}
