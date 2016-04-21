package org.jboss.windup.util;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
interface Converter<TFromC, TToC>
{
    TToC from(TFromC m);

}
