package org.jboss.windup.util.test;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Convenience class to get paths important for the testsuite.
 * 
 * @see WINDUP-191
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public final class TestUtil 
{
    // Inner class, so that everything is under TestUtil, which will cover more than just paths.
    // Leaving everything at level 0 is a mess.
    // Having it scattered over multiple classess is a mess.
    // Having to call new TestUtil().getPaths().getModuleDirAbs() is an overkill.
    public static final class Dirs 
    {
        
        /**
         * Returns a relative path of this (Maven) module - ${project.basedir}.
         */
        public static Path getWorkDirAbs()
        {
            return Paths.get(System.getProperty("user.dir")).normalize();
        }

        /**
         * Returns a relative path of this (Maven) module - ${project.basedir}.
         */
        public static Path getModuleDir()
        {
            return getWorkDirAbs().toAbsolutePath().relativize( getModuleDirAbs().toAbsolutePath() );
        }

        /**
         * Passes an absolute path of this (Maven) module - ${project.basedir}.
         */
        public static Path getModuleDirAbs()
        {
            return sysPropToPath("ts.module.dir");
        }


        /**
         * Passes a relative path to the directory of the root project directory.
         */
        public static Path getProjectRootDir()
        {
            return getModuleDir().resolve(sysPropToPath("ts.projectRoot.dir"));
        }

        /**
         * Passes a relative path to the directory with test files.
         */
        public static Path getTestFilesDir()
        {
            return getModuleDir().resolve(sysPropToPath("ts.testFiles.dir"));
        }
        
        private static Path sysPropToPath(final String prop)
        {
            final String val = System.getProperty(prop);
            if(val == null)  return null;
            return Paths.get(val).normalize();
        }
    }
    
    /**
     * Workaround for WINDUP-197 Forge + Surefire hide exception's message and cause
     */
    public static Exception rewrap(Exception ex)
    {
        String msg = ex.getMessage();
        if(ex instanceof InvocationTargetException){
            InvocationTargetException ite = (InvocationTargetException)ex;
            if( ite.getTargetException() instanceof Exception )
                ex = (Exception) ite.getTargetException();
            msg = ite.getTargetException().getMessage();
        }

        return new Exception(msg, ex);
    }

}// class
