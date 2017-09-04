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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.example.demo.DemoApplication;

import org.openjdk.jmh.util.FileUtils;
import org.openjdk.jmh.util.Utils;

import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.thin.ArchiveUtils;
import org.springframework.boot.loader.thin.DependencyResolver;
import org.springframework.boot.loader.thin.PathResolver;

public class ProcessLauncherState {

	private Process started;
	private List<String> args;
	private File home;
	private String mainClass = DemoApplication.class.getName();
	private int length;
	private String name = "thin";
	private String[] profiles = new String[0];
	private int classpath = 0;

	public ProcessLauncherState(String dir, String... args) {
		this.args = new ArrayList<>(Arrays.asList(args));
		int count = 0;
		this.args.add(count++, System.getProperty("java.home") + "/bin/java");
		this.args.add(count++, "-Xmx128m");
		this.args.add(count++, "-cp");
		this.classpath = count;
		this.args.add(count++, "");
		this.args.add(count++, "-Djava.security.egd=file:/dev/./urandom");
		this.args.add(count++, "-XX:TieredStopAtLevel=1"); // zoom
		this.args.add(count++, "-noverify");
		if (System.getProperty("bench.args") != null) {
			this.args.addAll(count++,
					Arrays.asList(System.getProperty("bench.args").split(" ")));
		}
		this.length = args.length;
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
		StringBuilder builder = new StringBuilder(
				new File("target/classes").getAbsolutePath());
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

	public void before() throws Exception {
		String name = getClass().getSimpleName().contains("_")
				? getClass().getSimpleName().split("_")[1] : "";
		if (name.length() == 4) {
			setProfiles(name.toLowerCase());
		}
		else {
			setProfiles();
		}
		args.set(this.classpath, getClasspath());
	}

	public void after() throws Exception {
		if (started != null && started.isAlive()) {
			System.err.println(
					"Stopped " + mainClass + ": " + started.destroyForcibly().waitFor());
		}
	}

	public Collection<String> capture(String... additional) throws Exception {
		List<String> args = new ArrayList<>(this.args);
		args.addAll(Arrays.asList(additional));
		ProcessBuilder builder = new ProcessBuilder(args);
		builder.directory(home);
		builder.redirectErrorStream(true);
		customize(builder);
		if (!"false".equals(System.getProperty("debug", "false"))) {
			System.err.println("Running: " + Utils.join(args, " "));
		}
		started = builder.start();
		return FileUtils.readAllLines(started.getInputStream());
	}

	public void run() throws Exception {
		List<String> args = new ArrayList<>(this.args);
		args.add(args.size() - this.length, this.mainClass);
		ProcessBuilder builder = new ProcessBuilder(args);
		builder.directory(home);
		builder.redirectErrorStream(true);
		customize(builder);
		if (!"false".equals(System.getProperty("debug", "false"))) {
			System.err.println("Running: " + Utils.join(args, " "));
		}
		started = builder.start();
		monitor();
	}

	protected void customize(ProcessBuilder builder) {
	}

	protected void monitor() throws IOException {
		System.out.println(output(started.getInputStream(), "Started"));
	}

	protected static String output(InputStream inputStream, String marker)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		while ((line = br.readLine()) != null && !line.contains(marker)) {
			sb.append(line + System.getProperty("line.separator"));
		}
		if (line != null) {
			sb.append(line + System.getProperty("line.separator"));
		}
		return sb.toString();
	}

	public File getHome() {
		return home;
	}
}