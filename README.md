# eclipse-jarinjarloader

[![Download](https://api.bintray.com/packages/raisercostin/maven/eclipse-jarinjarloader/images/download.svg)](https://bintray.com/raisercostin/maven/eclipse-jarinjarloader/_latestVersion)
<!--
[![Build Status](https://travis-ci.org/raisercostin/eclipse-jarinjarloader.svg?branch=master)](https://travis-ci.org/raisercostin/eclipse-jarinjarloader)
[![Codacy Badge](https://www.codacy.com/project/badge/fe1bb28a7735433d89a238ce6f6305c1)](https://www.codacy.com/app/raisercostin/eclipse-jarinjarloader)
-->

## Desciption
A custom class loader based on eclipse code to allow executable jars and custom exclusion of some libraries at runtime.

## Features
- All libraries are bundled inside jar as jars by maven.
- Some libraries can be filtered out depending on the runtime operating system. The classloader to detect the type of OS and based on that to reconfigure the classpath according to the swt libraries specific to that OS.
This question should provide support http://stackoverflow.com/questions/2706222/create-cross-platform-java-swt-application , http://mchr3k.github.io/swtjar/

## How to use it

See the [eclipse-jarinjarloader-swt-sample/pom.xml](https://github.com/raisercostin/eclipse-jarinjarloader/blob/master/eclipse-jarinjarloader-swt-sample/pom.xml) to see how to configure this.
You will just need to change the properties for main class and the version of the jarinjarloader.

	<?xml version="1.0" encoding="UTF-8"?>
	<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
		<modelVersion>4.0.0</modelVersion>
		<prerequisites>
			<maven>3.0.4</maven>
		</prerequisites>
		<parent>
			<groupId>org.raisercostin</groupId>
			<artifactId>eclipse-jarinjarloader-parent</artifactId>
			<version>1.3-SNAPSHOT</version>
		</parent>
		<artifactId>eclipse-jarinjarloader-swt-sample</artifactId>
		<packaging>jar</packaging>
	
		<properties>
			<swt.version>4.3</swt.version>
			<main.class>org.raisercostin.jarinjarloader.sample.JarInJarSwtMain</main.class>
			<jarinjarloader.version>${project.version}</jarinjarloader.version>
		</properties>
	
		<build>
			<plugins>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-dependency-plugin</artifactId>
					<executions>
						<execution>
							<id>copy-dependencies</id>
							<phase>prepare-package</phase>
							<goals>
								<goal>copy-dependencies</goal>
							</goals>
							<configuration>
								<outputDirectory>${project.build.directory}/classes/lib</outputDirectory>
								<overWriteReleases>false</overWriteReleases>
								<overWriteSnapshots>false</overWriteSnapshots>
								<overWriteIfNewer>true</overWriteIfNewer>
								<includeScope>runtime</includeScope>
							</configuration>
						</execution>
						<execution>
							<id>unpack</id>
							<phase>prepare-package</phase>
							<goals>
								<goal>unpack</goal>
							</goals>
							<configuration>
								<artifactItems>
									<artifactItem>
										<groupId>org.raisercostin</groupId>
										<artifactId>eclipse-jarinjarloader</artifactId>
										<version>${jarinjarloader.version}</version>
										<type>jar</type>
										<overWrite>false</overWrite>
										<outputDirectory>${project.build.directory}/classes</outputDirectory>
									</artifactItem>
								</artifactItems>
							</configuration>
						</execution>
					</executions>
				</plugin>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-jar-plugin</artifactId>
					<configuration>
						<archive>
							<manifest>
								<addClasspath>true</addClasspath>
								<classpathPrefix>lib/</classpathPrefix>
								<mainClass>org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader</mainClass>
							</manifest>
							<manifestEntries>
								<Class-Path>./</Class-Path>
								<Rsrc-Main-Class>${main.class}</Rsrc-Main-Class>
								<Class-Path-Filter-Match-On-osName-linux---osArch-i386>org.eclipse.swt.gtk.linux.x86</Class-Path-Filter-Match-On-osName-linux---osArch-i386>
								<Class-Path-Filter-Match-On-osName-linux---osArch-x86-64>org.eclipse.swt.gtk.linux.x86_64</Class-Path-Filter-Match-On-osName-linux---osArch-x86-64>
								<Class-Path-Filter-Match-On-osName-linux---osArch-amd64>org.eclipse.swt.gtk.linux.x86_64</Class-Path-Filter-Match-On-osName-linux---osArch-amd64>
								<Class-Path-Filter-Match-On-osName-windows-7---osArch-x86>org.eclipse.swt.win32.win32.x86</Class-Path-Filter-Match-On-osName-windows-7---osArch-x86>
								<Class-Path-Filter-Match-On-osName-windows---osArch-x86>org.eclipse.swt.win32.win32.x86</Class-Path-Filter-Match-On-osName-windows---osArch-x86>
								<Class-Path-Filter-Match-On-osName-windows---osArch-x86-64>org.eclipse.swt.win32.win32.x86_64</Class-Path-Filter-Match-On-osName-windows---osArch-x86-64>
								<Class-Path-Filter-Match-On-osName-windows---osArch-amd64>org.eclipse.swt.win32.win32.x86_64</Class-Path-Filter-Match-On-osName-windows---osArch-amd64>
								<Class-Path-Filter-Match-On-osName-mac-os-x---osArch-i386>org.eclipse.swt.cocoa.macosx</Class-Path-Filter-Match-On-osName-mac-os-x---osArch-i386>
								<Class-Path-Filter-Match-On-osName-mac-os-x---osArch-x86-64>org.eclipse.swt.cocoa.macosx.x86_64</Class-Path-Filter-Match-On-osName-mac-os-x---osArch-x86-64>
							</manifestEntries>
						</archive>
					</configuration>
				</plugin>
			</plugins>
		</build>
		<dependencies>
			<!--dependency needed to compile on the current dev environment-->
			<dependency>
				<groupId>org.eclipse.swt</groupId>
				<artifactId>${swt.artifactId}</artifactId>
				<version>${swt.version}</version>
				<scope>compile</scope>
			</dependency>
			<!--dependencies needed to be included at runtime. The classpath loader will filter them.-->
			<dependency>
				<groupId>org.eclipse.swt</groupId>
				<artifactId>org.eclipse.swt.win32.win32.x86</artifactId>
				<version>${swt.version}</version>
				<scope>runtime</scope>
			</dependency>
			<dependency>
				<groupId>org.eclipse.swt</groupId>
				<artifactId>org.eclipse.swt.win32.win32.x86_64</artifactId>
				<version>${swt.version}</version>
				<scope>runtime</scope>
			</dependency>
			<dependency>
				<groupId>org.eclipse.swt</groupId>
				<artifactId>org.eclipse.swt.cocoa.macosx.x86_64</artifactId>
				<version>${swt.version}</version>
				<scope>runtime</scope>
			</dependency>
			<dependency>
				<groupId>org.eclipse.swt</groupId>
				<artifactId>org.eclipse.swt.gtk.linux.x86</artifactId>
				<version>${swt.version}</version>
				<scope>runtime</scope>
			</dependency>
		</dependencies>
		<!-- https://github.com/playn/playn/blob/master/java-swt/pom.xml -->
		<!-- NOTE: this only works for the developer's workstation; if you are going 
			to deploy a real app using the swt-java backend, you have to include all 
			of the jars and select the right one outside of Java, or do even more jiggery 
			pokery as described here: http://stackoverflow.com/questions/2706222/create-cross-platform-java-swt-application -->
		<profiles>
			<profile> <!-- Linux -->
				<id>gtk_linux_x86</id>
				<activation>
					<os>
						<name>linux</name>
						<arch>i386</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.gtk.linux.x86</swt.artifactId>
				</properties>
			</profile>
			<profile>
				<id>gtk_linux_x86_64</id>
				<activation>
					<os>
						<name>linux</name>
						<arch>x86_64</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.gtk.linux.x86_64</swt.artifactId>
				</properties>
			</profile>
			<profile>
				<id>gtk_linux_amd64</id>
				<activation>
					<os>
						<name>linux</name>
						<arch>amd64</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.gtk.linux.x86_64</swt.artifactId>
				</properties>
			</profile>
			<profile> <!-- Windows -->
				<id>win32_x86</id>
				<activation>
					<os>
						<family>windows</family>
						<arch>x86</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.win32.win32.x86</swt.artifactId>
				</properties>
			</profile>
			<profile>
				<id>win32_x86_64</id>
				<activation>
					<os>
						<family>windows</family>
						<arch>x86_64</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.win32.win32.x86_64</swt.artifactId>
				</properties>
			</profile>
			<profile>
				<id>win32_x86_amd64</id>
				<activation>
					<os>
						<family>windows</family>
						<arch>amd64</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.win32.win32.x86_64</swt.artifactId>
				</properties>
			</profile>
			<profile> <!-- Mac OS X -->
				<id>cocoa_macosx_x86</id>
				<activation>
					<os>
						<name>mac os x</name>
						<arch>i386</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.cocoa.macosx</swt.artifactId>
				</properties>
			</profile>
			<profile>
				<id>cocoa_macosx_x86_64</id>
				<activation>
					<os>
						<name>mac os x</name>
						<arch>x86_64</arch>
					</os>
				</activation>
				<properties>
					<swt.artifactId>org.eclipse.swt.cocoa.macosx.x86_64</swt.artifactId>
				</properties>
			</profile>
		</profiles>
		<repositories>
			<repository>
				<id>jcenter-bintray</id>
				<name>Bintray JCenter Maven Repository</name>
				<layout>default</layout>
				<url>https://jcenter.bintray.com/</url>
				<releases>
					<enabled>true</enabled>
				</releases>
				<snapshots>
					<enabled>false</enabled>
				</snapshots>
			</repository>
		</repositories>
	</project>

## How it works
A classloader will serve classes from all the dependency libraries stored in lib folder in the jar.


## How to build and release

	mvn release:prepare release:perform -DskipTests=true -Prelease -Darguments="-DskipTests=true -Prelease"

## Credits
- This code is based on eclipse jarinjar classloaders from [eclipse code](http://git.eclipse.org/c/jdt/eclipse.jdt.ui.git/plain/org.eclipse.jdt.ui/jar%20in%20jar%20loader/org/eclipse/jdt/internal/jarinjarloader/).
- Based on [stackoverflow answer](http://stackoverflow.com/questions/1729054/including-dependencies-in-a-jar-with-maven).


## Other solutions
Other similar techniques that don't work properly:
- onejar - http://one-jar.sourceforge.net/ , https://code.google.com/archive/p/onejar-maven-plugin/ , 
- uberjar - http://fiji.sc/Uber-JAR
- maven assembly jar-with-dependencies
- shade - http://maven.apache.org/plugins/maven-shade-plugin/index.html
- war - 
- guardpro - 

```
There are three common methods for constructing an uber-JAR:

Unshaded. Unpack all JAR files, then repack them into a single JAR. * Pro: Works with Java's default class loader. * Con: Files present in multiple JAR files with the same path (e.g., META-INF/services/javax.script.ScriptEngineFactory) will overwrite one another, resulting in faulty behavior. * Tools: Maven Assembly Plugin, Classworlds Uberjar
Shaded. Same as unshaded, but rename (i.e., "shade") all packages of all dependencies. * Pro: Works with Java's default class loader. Avoids some (not all) dependency version clashes. * Con: Files present in multiple JAR files with the same path (e.g., META-INF/services/javax.script.ScriptEngineFactory) will overwrite one another, resulting in faulty behavior. * Tools: Maven Shade Plugin
JAR of JARs. The final JAR file contains the other JAR files embedded within. * Pro: Avoids dependency version clashes. All resource files are preserved. * Con: Needs to bundle a special "bootstrap" classloader to enable Java to load classes from the wrapped JAR files. Debugging class loader issues becomes more complex.
Tools: Eclipse JAR File Exporter, One-JAR.
```
