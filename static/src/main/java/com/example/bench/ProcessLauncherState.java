/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.bench;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.config.ShutdownApplicationListener;
import com.example.config.StartupApplicationListener;
import com.example.demo.DemoApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.thin.ArchiveUtils;
import org.springframework.boot.loader.thin.DependencyResolver;
import org.springframework.boot.loader.thin.PathResolver;

public class ProcessLauncherState {

	private static final Logger log = LoggerFactory.getLogger(ProcessLauncherState.class);

	private Process started;
	private List<String> args = new ArrayList<>();
	private List<String> progs = new ArrayList<>();
	private static List<String> DEFAULT_JVM_ARGS = Arrays.asList("-Xmx128m", "-cp", "",
			"-Djava.security.egd=file:/dev/./urandom", "-noverify");
	private File home;
	private String mainClass = DemoApplication.class.getName();
	private String name = "thin";
	private String[] profiles = new String[0];

	private BufferedReader buffer;

	public ProcessLauncherState(String dir, String... args) {
		this.args.addAll(DEFAULT_JVM_ARGS);
		String vendor = System.getProperty("java.vendor", "").toLowerCase();
		if (vendor.contains("ibm") || vendor.contains("j9")) {
			this.args.addAll(Arrays.asList("-Xms32m", "-Xquickstart", "-Xshareclasses",
					"-Xscmx128m"));
		}
		else {
			this.args.addAll(Arrays.asList("-XX:TieredStopAtLevel=1"));
		}
		if (System.getProperty("bench.args") != null) {
			this.args.addAll(Arrays.asList(System.getProperty("bench.args").split(" ")));
		}
		this.progs.addAll(Arrays.asList(args));
		this.home = new File(dir);
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProfiles(String... profiles) {
		this.profiles = profiles;
	}

	private String getClasspath() {
		PathResolver resolver = new PathResolver(DependencyResolver.instance());
		Archive root = ArchiveUtils.getArchive(getClass());
		List<Archive> resolved = resolver.resolve(root, name, profiles);
		File app = new File("target/original-benchmarks.jar");
		if (!app.exists()) {
			app = new File("target/classes");
		}
		StringBuilder builder = new StringBuilder(app.getAbsolutePath());
		try {
			for (Archive archive : resolved) {
				if (archive.getUrl().equals(root.getUrl())) {
					continue;
				}
				if (builder.length() > 0) {
					builder.append(File.pathSeparator);
				}
				builder.append(file(archive.getUrl().toString()));
			}
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException("Cannot find archive", e);
		}
		log.debug("Classpath: " + builder);
		return builder.toString();
	}

	private String file(String path) {
		if (path.endsWith("!/")) {
			path = path.substring(0, path.length() - 2);
		}
		if (path.startsWith("jar:")) {
			path = path.substring("jar:".length());
		}
		if (path.startsWith("file:")) {
			path = path.substring("file:".length());
		}
		return path;
	}

	public void after() throws Exception {
		if (started != null && started.isAlive()) {
			System.err.println(
					"Stopped " + mainClass + ": " + started.destroyForcibly().waitFor());
		}
	}

	private BufferedReader getBuffer() {
		return this.buffer;
	}

	public void run() throws Exception {
		List<String> jvmArgs = new ArrayList<>(this.args);
		customize(jvmArgs);
		started = exec(jvmArgs.toArray(new String[0]), this.progs.toArray(new String[0]));
		InputStream stream = started.getInputStream();
		this.buffer = new BufferedReader(new InputStreamReader(stream));
		monitor();
	}

	public void before() throws Exception {
		int classpath = args.indexOf("-cp");
		if (classpath >= 0 && args.get(classpath + 1).length() == 0) {
			args.set(classpath + 1, getClasspath());
		}
	}

	protected void customize(List<String> args) {
	}

	protected Process exec(String[] jvmArgs, String... progArgs) {
		List<String> args = new ArrayList<>(Arrays.asList(jvmArgs));
		args.add(0, System.getProperty("java.home") + "/bin/java");
		if (mainClass.length() > 0) {
			args.add(mainClass);
		}
		int classpath = args.indexOf("-cp");
		if (classpath >= 0 && args.get(classpath + 1).length() == 0) {
			args.set(classpath + 1, getClasspath());
		}
		args.addAll(Arrays.asList(progArgs));
		ProcessBuilder builder = new ProcessBuilder(args);
		builder.redirectErrorStream(true);
		builder.directory(getHome());
		if (!"false".equals(System.getProperty("debug", "false"))) {
			System.err.println("Executing: " + builder.command());
		}
		Process started;
		try {
			started = builder.start();
			return started;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException("Cannot calculate classpath");
		}
	}

	protected void monitor() throws Exception {
		// use this method to wait for an app to start
		output(getBuffer(), StartupApplicationListener.MARKER);
		// output(getBuffer(), "Started");
	}

	protected void finish() throws Exception {
		// use this method to wait for an app to stop
		output(getBuffer(), ShutdownApplicationListener.MARKER);
	}

	protected void drain() throws Exception {
		output(getBuffer(), null);
	}

	protected static void output(BufferedReader br, String marker) throws Exception {
		StringBuilder sb = new StringBuilder();
		String line = null;
		if (!"false".equals(System.getProperty("debug", "false"))) {
			System.err.println("Scanning for: " + marker);
		}
		while ((marker != null || br.ready()) && (line = br.readLine()) != null
				&& (marker == null || !line.contains(marker))) {
			sb.append(line + System.getProperty("line.separator"));
			if (!"false".equals(System.getProperty("debug", "false"))) {
				System.out.println(line);
			}
			line = null;
		}
		if (line != null) {
			if (!"false".equals(System.getProperty("debug", "false"))) {
				System.out.println(line);
			}
			sb.append(line + System.getProperty("line.separator"));
		}
		System.out.println(sb.toString());
	}

	public File getHome() {
		return home;
	}
}
