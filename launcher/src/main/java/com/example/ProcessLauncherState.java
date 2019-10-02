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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.example.config.StartupApplicationListener;
import org.openjdk.jmh.util.FileUtils;

import org.springframework.lang.Nullable;

public class ProcessLauncherState {

	private static final String CLASSPATH = "BOOT-INF/classes" + File.pathSeparator
			+ "BOOT-INF/lib/*";

	public static final String CLASS_COUNT_MARKER = "Class count";

	public static final String BEAN_COUNT_MARKER = "Bean count";

	private Process started;
	private List<String> args = new ArrayList<>();
	private List<String> progs = new ArrayList<>();
	private static List<String> DEFAULT_JVM_ARGS = Arrays
			.asList("-Djava.security.egd=file:/dev/./urandom", "-noverify");
	private File home;
	private BufferedReader buffer;
	private String[] marker = new String[] { StartupApplicationListener.MARKER };
	private String jar;
	private String[] globals = new String[0];

	private long classes;

	private int beans;

	private long memory;

	private long heap;

	public long getClasses() {
		return classes;
	}

	public int getBeans() {
		return beans;
	}

	public double getMemory() {
		return memory / (1024. * 1024);
	}

	public double getHeap() {
		return heap / (1024. * 1024);
	}

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
			if (toolsAvailable()) {
				Map<String, Long> metrics = VirtualMachineMetrics.fetch(getPid());
				System.err.println(metrics);
				this.memory = VirtualMachineMetrics.total(metrics);
				this.heap = VirtualMachineMetrics.heap(metrics);
				if (metrics.containsKey("Classes")) {
					this.classes = metrics.get("Classes");
				}
			}
			System.err.println("Stopping: " + started.destroyForcibly().waitFor());
		}
	}

	public String getPid() {
		String pid = null;
		try {
			if (started != null) {
				Field field = findField(started.getClass(), "pid");
				makeAccessible(field);
				pid = "" + field.get(started);
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
		return pid;
	}

	private static void makeAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers())
				|| !Modifier.isPublic(field.getDeclaringClass().getModifiers())
				|| Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
		}
	}

	private static Field findField(Class<?> clazz, @Nullable String name) {
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			Field[] fields = searchType.getDeclaredFields();
			for (Field field : fields) {
				if ((name == null || name.equals(field.getName()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
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

	protected void output(BufferedReader br, String... markers) throws IOException {
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
			if (line.contains(CLASS_COUNT_MARKER)) {
				classes = Integer
						.valueOf(line.substring(line.lastIndexOf("=") + 1).trim());
			}
			if (line.contains(BEAN_COUNT_MARKER)) {
				int count = Integer
						.valueOf(line.substring(line.lastIndexOf("=") + 1).trim());
				beans = count > beans ? count : beans;
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

	public static boolean toolsAvailable() {
		try {
			Class.forName("com.sun.tools.attach.VirtualMachine");
			return true;
		}
		catch (Throwable e) {
		}
		return false;
	}

	public void mainClassFromManifest() {
		if (!args.contains("-cp")) {
			unpack();
			args.add("-cp");
			args.add(classpathWithBenchmarkJar());
			args.add(findAttribute(Attributes.Name.MAIN_CLASS.toString()));
		}
	}

	private String classpathWithBenchmarkJar() {
		String path = ".";
		try {
			File base = new File(ProcessLauncherState.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI());
			File classes = new File(home, "BOOT-INF/classes");
			if (classes.exists()) {
				// It's a fat jar
				if (base.getName().endsWith(".jar")) {
					FileCopyUtils.copy(base,
							new File(new File(home, "BOOT-INF/lib"), base.getName()));
				}
				else {
					// Need to merge spring factories, but only for tests, so no biggy
					FileCopyUtils.copyRecursively(base, classes);
				}
			}
			else {
				if (isThinJar()) {
					File bench = new File("target/bench");
					if (base.getName().endsWith(".jar")) {
						bench.mkdirs();
						unpack(bench, base.getAbsolutePath());
					}
					else {
						bench = base;
					}
					FileCopyUtils.copyRecursively(new File(bench, "com"),
							new File(home, "com"));
					// TODO: Need to merge spring factories
					FileCopyUtils.copy(new File(bench, "META-INF/spring.factories"),
							new File(home, "META-INF/spring.factories"));
				}
				else {
					// Shaded Jar
					path = path + File.pathSeparator + base;
				}
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return path;
	}

	private boolean isThinJar() {
		return new File(home,
				"org/springframework/boot/loader/wrapper/ThinJarWrapper.class").exists();
	}

	public void startClassFromManifest() {
		if (!args.contains("-cp")) {
			unpack();
			String path = classpath(CLASSPATH);
			args.add("-cp");
			args.add(addBenchmarkJar(path));
			String mainClass = findAttribute("Start-Class");
			if (mainClass == null) {
				// Could be shaded
				mainClass = findAttribute(Attributes.Name.MAIN_CLASS.toString());
			}
			args.add(mainClass);
		}
	}

	private String classpath(String path) {
		if (isThinJar()) {
			List<String> args = new ArrayList<>(this.args);
			args.add("-jar");
			args.add(jar);
			Process started = exec(args.toArray(new String[0]),
					new String[] { "--thin.classpath" });
			InputStream stream = started.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			List<String> lines;
			try {
				started.waitFor(10, TimeUnit.SECONDS);
				lines = new ArrayList<>(FileUtils.readAllLines(reader));
			}
			catch (Exception e) {
				throw new IllegalStateException(e);
			}
			if (lines.isEmpty()) {
				throw new IllegalStateException("Cannot find classpath from " + jar);
			}
			String result = lines.get(0);
			if (!result.contains(jar)) {
				throw new IllegalStateException(
						"Cannot find classpath from " + jar + ", found " + result);
			}
			return result;
		}
		File classes = new File(home, "BOOT-INF/classes");
		if (classes.exists()) {
			return path;
		}
		return ".";
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