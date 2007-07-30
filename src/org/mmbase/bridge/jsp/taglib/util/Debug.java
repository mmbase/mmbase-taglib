/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.jsp.taglib.util;


/**
 * Representation of the values of 'debug' attributes, as used in tags like mm:include and
 * mm:component.
 *
 * @author Michiel Meeuwissen
 * @since MMBase-1.9
 * @version $Id: Debug.java,v 1.3 2007-07-30 17:22:59 michiel Exp $
 */
public enum Debug {

    NONE,

    HTML {
        public String start(String name, Object url) {
            return "\n<!-- " + name + " page = '" + url + "' -->\n";
        }
        public String end(String name, Object url) {
            return "\n<!-- END " + name + " page = '" + url + "' -->\n";
        }
    },


    CSS {
        public String start(String name, Object url) {
            return "\n/* " + name +  " page  = '" + url + "' */\n";
        }
        public String end(String name, Object url) {
            return "\n/* END " + name + " page = '" + url + "' */\n";
        }
    },

    XML {
        public String start(String name, Object url) {
            return "<!-- " + name + " page = '" + url + "' -->";
        }
        public String end(String name, String url) {
            return "<!-- END " + name + " page = '" + url + "' -->";
        }
    },

    PLAIN {
        public String start(String name, Object url) {
            return "[start:" + name + ":'" + url + "']";
        }
        public String end(String name, Object url) {
            return "[end:" + name + ":'" + url + "']";
        }
    };

    public static Debug valueOfOrEmpty(String s) {
        if (s == null || s.length() == 0) return NONE;
        return valueOf(s.toUpperCase());
    }


    public String start(String name, Object url) {
        return "";
    }

    public String end(String name, Object url) {
        return "";
    }

}
