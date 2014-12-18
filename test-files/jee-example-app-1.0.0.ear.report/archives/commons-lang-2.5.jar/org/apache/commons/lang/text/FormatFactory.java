package org.apache.commons.lang.text;

import java.text.Format;
import java.util.Locale;

public interface FormatFactory{
    Format getFormat(String p0,String p1,Locale p2);
}
