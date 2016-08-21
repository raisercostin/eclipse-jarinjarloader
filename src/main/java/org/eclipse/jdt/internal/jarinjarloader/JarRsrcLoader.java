/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ferenc Hechler - initial API and implementation
 *     Ferenc Hechler, ferenc_hechler@users.sourceforge.net - 219530 [jar application] add Jar-in-Jar ClassLoader option
 *     Ferenc Hechler, ferenc_hechler@users.sourceforge.net - 262746 [jar exporter] Create a builder for jar-in-jar-loader.zip
 *     Ferenc Hechler, ferenc_hechler@users.sourceforge.net - 262748 [jar exporter] extract constants for string literals in JarRsrcLoader et al.
 *******************************************************************************/
package org.eclipse.jdt.internal.jarinjarloader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This class will be compiled into the binary jar-in-jar-loader.zip. This ZIP is used for the "Runnable JAR File
 * Exporter"
 * 
 * @since 3.5
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JarRsrcLoader {
	private static class ManifestInfo {
		String rsrcMainClass;
		String[] rsrcClassPath;
		/** @see http://maven.apache.org/enforcer/enforcer-rules/requireOS.html */
		private Map<String,String> librariesToFilter = new TreeMap<>();

		public void configureConditionalPath(String[] conditionalLibraries) {
			if (conditionalLibraries != null)
				for (String condition : conditionalLibraries) {
					String[] cond = splitSpaces(condition, ',');
					String condOsName = cond[0];
					String condOsArch = cond[1];
					String libraryName = cond[2];
					String libValue = condition(condOsName,condOsArch);
					//System.out.println("conditional library ["+libraryName+"] condition ["+libValue+"]");
					librariesToFilter.put(libraryName,libValue);
				}
		}

		public boolean accept(String library, String osName, String osArch) {
			boolean notConditionalLibrarySoAcceptIt = true;
			String libFilter = condition(osName,osArch);
			for (Entry<String, String> lib : librariesToFilter.entrySet()) {
				if (library.contains(lib.getKey())){
					notConditionalLibrarySoAcceptIt = false;
					String libCondition = lib.getValue();
					boolean accept = libCondition.equals(libFilter);
					//System.out.println("conditional lib "+library+"["+libCondition+"] is "+(accept?"accepted":"ignored")+" filter="+libFilter);
					if(accept==true)
						//accept early
						//if false maybe there is another conditional libaray that allowes this library
						return true;
				}
			}
			return notConditionalLibrarySoAcceptIt;
		}

		private String condition(String osName, String osArch) {
			return osName+"---"+osArch;
		}
	}

	public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException, IOException {
		ManifestInfo mi = getManifestInfo();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL.setURLStreamHandlerFactory(new RsrcURLStreamHandlerFactory(cl));
		List<String> original = Arrays.asList(mi.rsrcClassPath);
		List<String> all = filter(mi, original);

		URL[] rsrcUrls = new URL[all.size()];
		for (int i = 0; i < all.size(); i++) {
			String rsrcPath = all.get(i);
			if (rsrcPath.endsWith(JIJConstants.PATH_SEPARATOR))
				rsrcUrls[i] = new URL(JIJConstants.INTERNAL_URL_PROTOCOL_WITH_COLON + rsrcPath);
			else
				rsrcUrls[i] = new URL(JIJConstants.JAR_INTERNAL_URL_PROTOCOL_WITH_COLON + rsrcPath
						+ JIJConstants.JAR_INTERNAL_SEPARATOR);
		}
		ClassLoader jceClassLoader = new URLClassLoader(rsrcUrls, null);
		Thread.currentThread().setContextClassLoader(jceClassLoader);
		try {
			Class c = Class.forName(mi.rsrcMainClass, true, jceClassLoader);
			Method main = c.getMethod(JIJConstants.MAIN_METHOD_NAME, new Class[] { args.getClass() });
			main.invoke((Object) null, new Object[] { args });
		} catch (UnsatisfiedLinkError e) {
			throw new Error(
					"The conditional loaded libraries wheren't filtered properly. The initial libs where \n initial=["
							+ original + "] while the filtered ones \nfiltered=[" + all + "]",
					e);
		}
	}
	private static List<String> filter(ManifestInfo mi, List<String> all) {
		String osName = System.getProperty("os.name").toLowerCase(Locale.US);
		String osArch = System.getProperty("os.arch").toLowerCase(Locale.US);
		String osVersion = System.getProperty("os.version").toLowerCase(Locale.US);

		System.out.println("osName=" + osName);
		System.out.println("osArch=" + osArch);
		System.out.println("osVersion=" + osVersion);
		List<String> result = new ArrayList<>();
		System.out.println("loading libraries [" + all + "]");
		for (String library : all) {
			if (mi.accept(library, osName, osArch))
				result.add(library);
		}
		System.out.println("filtered final libraries [" + result + "]");
		return result;
	}

	private static ManifestInfo getManifestInfo() throws IOException {
		Enumeration resEnum;
		resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
		while (resEnum.hasMoreElements()) {
			try {
				URL url = (URL) resEnum.nextElement();
				InputStream is = url.openStream();
				if (is != null) {
					System.out.println("reading from " + url);
					ManifestInfo result = new ManifestInfo();
					Manifest manifest = new Manifest(is);
					Attributes mainAttribs = manifest.getMainAttributes();
					result.rsrcMainClass = mainAttribs.getValue(JIJConstants.REDIRECTED_MAIN_CLASS_MANIFEST_NAME);
					String rsrcCP = mainAttribs.getValue(JIJConstants.REDIRECTED_CLASS_PATH_MANIFEST_NAME);
					if (rsrcCP == null)
						rsrcCP = JIJConstants.DEFAULT_REDIRECTED_CLASSPATH;
					result.rsrcClassPath = splitSpaces(rsrcCP, ' ');
					result.configureConditionalPath(
							splitSpaces(mainAttribs.getValue("OS-Conditional-Class-Path"), ':'));
					if ((result.rsrcMainClass != null) && !result.rsrcMainClass.trim().equals("")) //$NON-NLS-1$
						return result;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
				// Silently ignore wrong manifests on classpath?
			}
		}
		System.err.println(
				"Missing attributes for JarRsrcLoader in Manifest (" + JIJConstants.REDIRECTED_MAIN_CLASS_MANIFEST_NAME //$NON-NLS-1$
						+ ", " + JIJConstants.REDIRECTED_CLASS_PATH_MANIFEST_NAME + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		return null;
	}

	/**
	 * JDK 1.3.1 does not support String.split(), so we have to do it manually. Skip all spaces (tabs are not handled)
	 * 
	 * @param line
	 *            the line to split
	 * @return array of strings
	 */
	private static String[] splitSpaces(String line, char separator) {
		if (line == null)
			return null;
		List result = new ArrayList();
		int firstPos = 0;
		while (firstPos < line.length()) {
			int lastPos = line.indexOf(separator, firstPos);
			if (lastPos == -1)
				lastPos = line.length();
			if (lastPos > firstPos) {
				result.add(line.substring(firstPos, lastPos));
			}
			firstPos = lastPos + 1;
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

}
