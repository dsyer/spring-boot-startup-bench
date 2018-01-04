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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.util.FileUtils;

@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.AverageTime)
public class CdsBenchmark {

	@Benchmark
	public void sharedClasses(SharedState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void thinMain(ThinMainState state) throws Exception {
		state.run();
	}

	@State(Scope.Benchmark)
	public static class SharedState extends ThinMainState {

		@Setup(Level.Trial)
		public void setup() throws Exception {
			Process dump = exec(
					"-Xshare:dump -XX:+UnlockDiagnosticVMOptions -XX:SharedArchiveFile=app.jsa -cp $CLASSPATH",
					"", "");
			FileUtils.readAllLines(dump.getInputStream());
			dump.waitFor();
		}

		@Override
		protected void customize(ProcessBuilder builder) {
			builder.command().addAll(Arrays.asList(
					"-Xshare:on -XX:+UnlockDiagnosticVMOptions -XX:SharedArchiveFile=app.jsa"
							.split(" ")));
			super.customize(builder);
		}

		@Override
		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class ThinMainState extends ProcessLauncherState {

		private Map<String, String> environment = new LinkedHashMap<>();

		public ThinMainState() {
			super("target", "-cp",
					"$CLASSPATH:" + jarFile("com.example:petclinic:jar:thin:1.4.2"),
					"org.springframework.samples.petclinic.PetClinicApplication",
					"--server.port=0");
			environment();
		}

		private void environment() {
			Process started = exec("-jar",
					jarFile("com.example:petclinic:jar:thin:1.4.2"), "--thin.classpath");
			try {
				Collection<String> lines = FileUtils
						.readAllLines(started.getInputStream());
				started.waitFor();
				environment.put("CLASSPATH", lines.iterator().next());
			}
			catch (Exception e) {
				throw new IllegalStateException("Cannot calculate classpath");
			}
		}

		protected Process exec(String jvmArgs, String main, String progArgs) {
			List<String> args = new ArrayList<>();
			args.add(System.getProperty("java.home") + "/bin/java");
			args.addAll(Arrays.asList(
					"-Xmx128m -Djava.security.egd=file:/dev/./urandom".split(" ")));
			args.addAll(Arrays.asList(jvmArgs.split(" ")));
			if (main.length() > 0) {
				args.add(main);
			}
			args.addAll(Arrays.asList(progArgs.split(" ")));
			ProcessBuilder builder = new ProcessBuilder(args);
			builder.redirectErrorStream(true);
			builder.directory(getHome());
			precustomize(builder);
			if (!"false".equals(System.getProperty("debug", "false"))) {
				System.err.println("Executing: " + builder.command());
			}
			Process started;
			try {
				started = builder.start();
				return started;
			}
			catch (Exception e) {
				throw new IllegalStateException("Cannot calculate classpath");
			}
		}

		protected void precustomize(ProcessBuilder builder) {
			commonArgs(builder.command());
			replaceEnvironment(builder);
		}

		@Override
		protected void customize(ProcessBuilder builder) {
			commonArgs(builder.command());
			replaceEnvironment(builder);
		}

		private void commonArgs(List<String> command) {
			command.addAll(1, Arrays.asList("-noverify".split(" ")));
		}

		private void replaceEnvironment(ProcessBuilder builder) {
			builder.environment().putAll(this.environment);
			for (int i = 0; i < builder.command().size(); i++) {
				String value = builder.command().get(i);
				for (String key : environment.keySet()) {
					builder.command().set(i,
							value.replace("$" + key, environment.get(key)));
				}
			}
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

}
