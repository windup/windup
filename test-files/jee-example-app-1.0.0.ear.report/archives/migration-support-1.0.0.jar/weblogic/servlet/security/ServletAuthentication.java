package weblogic.servlet.security;

import javax.security.auth.Subject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.security.auth.callback.CallbackHandler;
import weblogic.security.services.AppContext;
import org.migration.support.NotImplemented;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

public class ServletAuthentication{
    public static int assertIdentity(final HttpServletRequest request,final HttpServletResponse response,final String realmName){
        throw new NotImplemented();
    }
    public static int assertIdentity(final HttpServletRequest request,final HttpServletResponse response,final String realmName,final AppContext appContext){
        throw new NotImplemented();
    }
    public static int authenticate(final CallbackHandler handler,final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static int authObject(final String username,final Object credential,final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static int authObject(final String username,final Object credential,final HttpSession session,final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static void done(final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static void generateNewSessionID(final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static Cookie getSessionCookie(final HttpServletRequest request,final HttpServletResponse response){
        throw new NotImplemented();
    }
    public static String getTargetURIForFormAuthentication(final HttpSession session){
        throw new NotImplemented();
    }
    public static String getTargetURLForFormAuthentication(final HttpSession session){
        throw new NotImplemented();
    }
    public static boolean invalidateAll(final HttpServletRequest req){
        throw new NotImplemented();
    }
    public static void killCookie(final HttpServletRequest req){
        throw new NotImplemented();
    }
    public static int login(final CallbackHandler handler,final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static int login(final String username,final String password,final HttpServletRequest request,final HttpServletResponse response){
        throw new NotImplemented();
    }
    public static boolean logout(final HttpServletRequest req){
        throw new NotImplemented();
    }
    public static void runAs(final Subject subject,final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static int strong(final HttpServletRequest request,final HttpServletResponse response){
        throw new NotImplemented();
    }
    public static int strong(final HttpServletRequest request,final HttpServletResponse response,final String realmName){
        throw new NotImplemented();
    }
    public int weak(final HttpServletRequest request,final HttpServletResponse response){
        throw new NotImplemented();
    }
    public static int weak(final String username,final String password,final HttpServletRequest request){
        throw new NotImplemented();
    }
    public static int weak(final String username,final String password,final HttpServletRequest request,final HttpServletResponse response){
        throw new NotImplemented();
    }
    public static int weak(final String username,final String password,final HttpSession session){
        throw new NotImplemented();
    }
}
