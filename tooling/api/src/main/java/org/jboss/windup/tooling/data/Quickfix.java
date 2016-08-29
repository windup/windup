package org.jboss.windup.tooling.data;

import org.jboss.windup.graph.model.QuickfixType;

public interface Quickfix
{

    QuickfixType getType();

    String getName();

    String getSearch();


    String getReplacement();


    String getNewline();

}