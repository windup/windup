/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 .
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.jboss.windup.configreal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Global utils class.
 *
 * @author Ondrej Zizka
 */
public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);


    /**
     *  Prints app help.
     */
    public static void writeHelp() {
        System.out.println();
        System.out.println(" JBoss configuration migration tool for AS 5 / EAP 5 -> AS 7 / EAP 6 / WildFly 8");
        System.out.println();
        System.out.println(" Usage:");
        System.out.println();
        System.out.println("    java -jar Windup.jar [<option>, ...] [srcServer.dir=]<dir> [destServer.dir=]<dir>");
        System.out.println();
        System.out.println("       <srcServer.dir>   is expected to contain path to AS 5 or EAP 5 home directory, i.e. the one with server/ subdirectory.");
        System.out.println();
        System.out.println("       <destServer.dir>   is expected to contain path to AS 7 or EAP 6 home directory, i.e. the one with jboss-modules.jar.");
        System.out.println();
        System.out.println(" Options:");
        System.out.println();
        System.out.println("    conf.<ruleset>.<property>=<value> := Module-specific options.");
        System.out.println("        <ruleset> := Name of one of rulesets. E.g. java, javaee, datasource, jaas, security, ...");
        System.out.println("        <property> := Name of the property to set. Specific per ruleset. " +
                "May occur multiple times.");
        System.out.println();
    }
    

    


    
    /**
     *  Throws a formatted message (name + errMsg) if string is null or empty.
     */
    public static void throwIfBlank(String string, String errMsg, String name) {
        if ((string == null) || (string.isEmpty())) {
            throw new IllegalArgumentException(name + errMsg);
        }
    }
    
    
    /**
     *  Returns null for empty strings.
     */
    public static String nullIfEmpty(String str){
        return str == null ? null : (str.isEmpty() ? null : str);
    }
    
    
    public static Throwable getRootCause( Throwable ex ){
        Throwable cause;
        do{
            cause = ex.getCause();
            if( cause == null )
                return ex;
            ex = cause;
        } while( true ); // Can exceptions ever create a loop?
    }

}// class
