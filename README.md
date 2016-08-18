A custom class loader based on eclipse code.

This code is based on eclipse jarinjar classloaders from http://git.eclipse.org/c/jdt/eclipse.jdt.ui.git/plain/org.eclipse.jdt.ui/jar%20in%20jar%20loader/org/eclipse/jdt/internal/jarinjarloader/ .

Based on this answer http://stackoverflow.com/questions/1729054/including-dependencies-in-a-jar-with-maven

# Status

[![Download](https://api.bintray.com/packages/raisercostin/maven/eclipse-jarinjarloader/images/download.svg)](https://bintray.com/raisercostin/maven/eclipse-jarinjarloader/_latestVersion)
<!--
[![Build Status](https://travis-ci.org/raisercostin/eclipse-jarinjarloader.svg?branch=master)](https://travis-ci.org/raisercostin/eclipse-jarinjarloader)
[![Codacy Badge](https://www.codacy.com/project/badge/fe1bb28a7735433d89a238ce6f6305c1)](https://www.codacy.com/app/raisercostin/eclipse-jarinjarloader)
-->


# How to use it

You will just need to change the properties for main class and the version of the jarinjarloader.
	
	<project>
		...
		<properties>
			<main.class>org.uzene.app.sayapp.SayAppMain</main.class>
			<jarinjarloader.version>1.0</jarinjarloader.version>
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
								<includeScope>test</includeScope>
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
							</manifestEntries>
						</archive>
					</configuration>
				</plugin>
			</plugins>
		</build>
	</project>

# How it works
A classloader will serve classes from all the dependency libraries stored in lib folder in the jar.

# To do
- It will be nice for classloader to detect the type of OS and based on that to reconfigure the classpath according to the swt libraries specific to that OS.

# How to build and release

	mvn release:prepare release:perform -DskipTests=true -Prelease -Darguments="-DskipTests=true -Prelease"

# Other solutions
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
