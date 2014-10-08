/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.soa.esb.samples.quickstart.helloworldftpaction.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CreateTestFile {
	
	public static void main(final String[] args) {
		if (args.length != 5)
		{
			System.err.println("Usage: java " + CreateTestFile.class.getName() + " <hostname> <username> <password> <filename> <contents>") ;
			System.exit(1) ;
		}
		else
		{
			final String hostname = args[0] ;
			final String username = args[1] ;
			final String password = args[2] ;
			final String filename = args[3] ;
			final String contents = args[4] ;
			
			final URL url ;
			final String filenameVal ;
			if (filename.charAt(0) == '/')
			{
				filenameVal = (filename.length() > 1 ? "%2F" + filename.substring(1) : "%2F") ;
			}
			else
			{
				filenameVal = filename ;
			}
			try
			{
				url = new URL("ftp://" + username + ":" + password + "@" + hostname + "/" + filenameVal) ;
			}
			catch (final MalformedURLException murle)
			{
				exit("Invalid URL: " + filenameVal, murle, 2) ;
				return ; // for compiler
			}
			final URLConnection connection ;
			try
			{
				connection = url.openConnection() ;
			}
			catch (final IOException ioe)
			{
				exit("Error accessing location: " + filenameVal, ioe, 3) ;
				return ; // for compiler
			}
			connection.setDoOutput(true) ;
			final OutputStream os ;
			try
			{
				os = connection.getOutputStream() ;
			}
			catch (final IOException ioe)
			{
				exit("Error obtaining output stream for location: " + filenameVal, ioe, 4) ;
				return ; // for compiler
			}
			
			try
			{
				final PrintStream ps = new PrintStream(os) ;
				ps.print(contents) ;
				ps.close() ;
			}
			finally
			{
				try
				{
					os.close() ;
				}
				catch (final IOException ioe) {} //ignore
			}
		}
	}
	
	private static void exit(final String message, final Throwable th, final int exitValue)
	{
		System.err.println(message) ;
		th.printStackTrace() ;
		System.exit(exitValue) ;
	}
}
