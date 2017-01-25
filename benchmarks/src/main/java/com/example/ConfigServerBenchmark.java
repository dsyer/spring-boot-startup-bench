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

import java.io.File;

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

@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.AverageTime)
public class ConfigServerBenchmark {

	private static final String CLASSPATH = "BOOT-INF/classes" + File.pathSeparator
			+ "BOOT-INF/lib/*";

	@Benchmark
	public void fatJar142(FatJar142State state) throws Exception {
		state.run();
	}

	@Benchmark
	public void fatJar150(FatJar150State state) throws Exception {
		state.run();
	}

	@Benchmark
	public void fatJar138(FatJar138State state) throws Exception {
		state.run();
	}

	@Benchmark
	public void devtoolsRestart(ExplodedDevtoolsState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void explodedJarMain(MainState state) throws Exception {
		state.run();
	}

	public static void main(String[] args) throws Exception {
		ExplodedDevtoolsState state = new ExplodedDevtoolsState();
		state.setup();
		try {
			while (true) {
				state.run();
			}
		}
		finally {
			state.stop();
		}
	}

	@State(Scope.Benchmark)
	public static class FatJar150State extends ProcessLauncherState {
		public FatJar150State() {
			super("target", "-jar",
					jarFile("com.example:configserver:jar:150:0.0.1-SNAPSHOT"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class FatJar142State extends ProcessLauncherState {
		public FatJar142State() {
			super("target", "-jar",
					jarFile("com.example:configserver:jar:142:0.0.1-SNAPSHOT"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class FatJar138State extends ProcessLauncherState {
		public FatJar138State() {
			super("target", "-jar",
					jarFile("com.example:configserver:jar:138:0.0.1-SNAPSHOT"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class ExplodedDevtoolsState extends DevToolsLauncherState {
		public ExplodedDevtoolsState() {
			super("target/demo", "/BOOT-INF/classes/.restart",
					jarFile("com.example:configserver:jar:142:0.0.1-SNAPSHOT"), "-cp",
					CLASSPATH, "-Dspring.devtools.livereload.enabled=false",
					"-Dspring.devtools.restart.pollInterval=100",
					"-Dspring.devtools.restart.quietPeriod=10",
					"demo.ConfigServerApplication", "--server.port=0");
		}

		@Override
		@Setup(Level.Trial)
		public void setup() throws Exception {
			super.setup();
		}

		@TearDown(Level.Trial)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class MainState extends ProcessLauncherState {
		public MainState() {
			super("target/demo", "-cp", CLASSPATH, "demo.ConfigServerApplication",
					"--server.port=0");
			unpack("target/demo",
					jarFile("com.example:configserver:jar:142:0.0.1-SNAPSHOT"));
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

}
