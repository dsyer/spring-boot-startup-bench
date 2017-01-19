/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
public class JsaBenchmark {

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
			Process started = exec(
					"-Xshare:off -XX:DumpLoadedClassList=app.classlist -cp $CLASSPATH:"
							+ jarFile("com.example:petclinic:jar:thin:1.4.2"),
					"org.springframework.samples.petclinic.PetClinicApplication",
					"--server.port=0");
			output(started.getInputStream(), "Started");
			started.destroyForcibly();
			Process dump = exec(
					"-Xshare:dump -XX:SharedArchiveFile=app.jsa -XX:SharedClassListFile=app.classlist -cp $CLASSPATH",
					"", "");
			FileUtils.readAllLines(dump.getInputStream());
			dump.waitFor();
		}

		@Override
		protected void precustomize(ProcessBuilder builder) {
			commonArgs(builder.command());
			super.precustomize(builder);
		}

		@Override
		protected void customize(ProcessBuilder builder) {
			commonArgs(builder.command());
			builder.command().addAll(
					Arrays.asList("-Xshare:on -XX:SharedArchiveFile=app.jsa".split(" ")));
			super.customize(builder);
		}

		private void commonArgs(List<String> command) {
			command.addAll(1, Arrays.asList(
					"-XX:+UnlockCommercialFeatures -XX:+UseAppCDS -noverify".split(" ")));
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
			replaceEnvironment(builder);
		}

		@Override
		protected void customize(ProcessBuilder builder) {
			replaceEnvironment(builder);
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
