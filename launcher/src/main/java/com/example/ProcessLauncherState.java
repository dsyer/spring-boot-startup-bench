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
package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.example.config.StartupApplicationListener;

public class ProcessLauncherState {

	private static final String CLASSPATH = "BOOT-INF/classes" + File.pathSeparator
			+ "BOOT-INF/lib/*";

	private Process started;
	private List<String> args = new ArrayList<>();
	private List<String> progs = new ArrayList<>();
	private static List<String> DEFAULT_JVM_ARGS = Arrays.asList("-Xmx128m",
			"-Djava.security.egd=file:/dev/./urandom", "-noverify");
	private File home;
	private BufferedReader buffer;
	private String[] marker = new String[] { StartupApplicationListener.MARKER };
	private String jar;
	private String[] globals = new String[0];

	public ProcessLauncherState(String home, String... args) {
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
		this.home = new File(home);
		this.home.mkdirs();
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream("benchmark.properties"));
			this.jar = properties.getProperty("jar.file");
			this.globals = StringUtils
					.commaDelimitedListToStringArray(properties.getProperty("args", ""));
			if (properties.containsKey("markers")) {
				this.marker = StringUtils.commaDelimitedListToStringArray(
						properties.getProperty("markers", ""));
			}
			this.progs.addAll(Arrays.asList(this.globals));
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void setMarker(String... marker) {
		this.marker = marker;
	}

	public void after() throws Exception {
		if (started != null && started.isAlive()) {
			started.destroyForcibly().waitFor();
		}
	}

	public void clean() throws Exception {
		FileCopyUtils.deleteRecursively(this.home);
	}

	public void replace(String pattern, String value) {
		for (int i = 0; i < args.size(); i++) {
			String arg = args.get(i).replace(pattern, value);
			args.set(i, arg);
		}
	}

	public void run() throws Exception {
		List<String> jvmArgs = new ArrayList<>(this.args);
		customize(jvmArgs);
		started = exec(jvmArgs.toArray(new String[0]), this.progs.toArray(new String[0]));
		InputStream stream = started.getInputStream();
		this.buffer = new BufferedReader(new InputStreamReader(stream));
		monitor();
	}

	protected void customize(List<String> args) {
	}

	protected Process exec(String[] jvmArgs, String... progArgs) {
		List<String> args = new ArrayList<>(Arrays.asList(jvmArgs));
		args.add(0, System.getProperty("java.home") + "/bin/java");
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

	protected void monitor() throws IOException {
		output(this.buffer, this.marker);
	}

	protected static void output(BufferedReader br, String... markers)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		String line = null;
		if (!"false".equals(System.getProperty("debug", "false"))) {
			System.err.println("Scanning for: " + Arrays.asList(markers));
		}
		while ((markers.length > 0 || br.ready()) && (line = br.readLine()) != null) {
			boolean found = false;
			sb.append(line + System.getProperty("line.separator"));
			if (!"false".equals(System.getProperty("debug", "false"))) {
				System.out.println(line);
			}
			for (String marker : markers) {
				if (line.contains(marker)) {
					line = null;
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}
		if (line != null) {
			if (!"false".equals(System.getProperty("debug", "false"))) {
				System.out.println(line);
			}
			sb.append(line + System.getProperty("line.separator"));
		}
		System.out.println(sb.toString());
	}

	public void copy(String path, String jar) {
		File dest = new File(path);
		dest.mkdirs();
		try {
			File file = new File(jar);
			Files.copy(file.toPath(), dest.toPath().resolve(file.getName()));
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed", e);
		}
	}

	public void fatJar() {
		if (!args.contains("-jar")) {
			args.add("-jar");
			args.add(this.jar);
		}
	}

	public void mainClassFromManifest() {
		if (!args.contains("-cp")) {
			unpack();
			args.add("-cp");
			unpackBenchmarkJar();
			args.add(".");
			args.add(findAttribute(Attributes.Name.MAIN_CLASS.toString()));
		}
	}

	private void unpackBenchmarkJar() {
		try {
			File base = new File(ProcessLauncherState.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI());
			if (base.getName().endsWith(".jar")) {
				FileCopyUtils.copy(base,
						new File(new File(home, "BOOT-INF/lib"), base.getName()));
			}
			else {
				FileCopyUtils.copyRecursively(base, new File(home, "BOOT-INF/classes"));
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void startClassFromManifest() {
		if (!args.contains("-cp")) {
			unpack();
			args.add("-cp");
			args.add(addBenchmarkJar(CLASSPATH));
			args.add(findAttribute("Start-Class"));
		}
	}

	private String addBenchmarkJar(String path) {
		try {
			path = path + File.pathSeparator
					+ new File(ProcessLauncherState.class.getProtectionDomain()
							.getCodeSource().getLocation().toURI()).getAbsolutePath();
		}
		catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
		return path;
	}

	private String findAttribute(String attr) {
		try (JarFile jarFile = new JarFile(this.jar)) {
			Manifest manifest = jarFile.getManifest();
			String cls = manifest.getMainAttributes().getValue(attr);
			return cls;
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void unpack() {
		FileCopyUtils.deleteRecursively(home);
		home.mkdirs();
		unpack(home, this.jar);
	}

	private void unpack(File home, String jar) {
		ProcessBuilder builder = new ProcessBuilder(getJarExec(), "xf",
				new File(jar).getAbsolutePath());
		Process started = null;
		try {
			builder.directory(home);
			builder.redirectErrorStream(true);
			started = builder.start();
			started.waitFor();
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed", e);
		}
		finally {
			if (started != null && started.isAlive()) {
				started.destroy();
			}
		}
	}

	private String getJarExec() {
		String home = System.getProperty("java.home");
		String jar = home + "/../bin/jar";
		if (new File(jar).exists()) {
			return jar;
		}
		jar = home + "/../bin/jar.exe";
		if (new File(jar).exists()) {
			return jar;
		}
		return home + "/bin/jar";
	}

	public File getHome() {
		return home;
	}

	public void jarFile(String path) {
		try {
			this.jar = new File(path).getAbsoluteFile().getCanonicalPath();
		}
		catch (IOException e) {
			throw new IllegalStateException("Cannot find benchmarks", e);
		}
	}

	public String jarFile() {
		return new File(this.jar).getAbsolutePath();
	}

	public static void setLauncherArgs(String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException(
					"The first argument must be a jar location");
		}
		Properties properties = new Properties();
		properties.setProperty("jar.file", new File(args[0]).getAbsolutePath());
		if (args.length > 1) {
			List<String> globals = new ArrayList<>();
			List<String> markers = new ArrayList<>();
			for (int i = 1; i < args.length; i++) {
				String arg = args[i];
				if (arg.startsWith("--marker")) {
					if (arg.contains("=")) {
						markers.add(arg.substring(arg.indexOf("=") + 1));
					}
					else if (i < args.length - 1) {
						i++;
						markers.add(args[i]);
					}
				}
				else {
					globals.add(arg);
				}
			}
			if (!globals.isEmpty()) {
				properties.setProperty("args", StringUtils
						.arrayToCommaDelimitedString(globals.toArray(new String[0])));
			}
			if (!markers.isEmpty()) {
				properties.setProperty("markers", StringUtils
						.arrayToCommaDelimitedString(markers.toArray(new String[0])));
			}
		}
		try {
			properties.store(new FileOutputStream("benchmark.properties"),
					"Create by Launcher");
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}