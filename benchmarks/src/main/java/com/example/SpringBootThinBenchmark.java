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
import java.util.Collection;

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
@BenchmarkMode(Mode.SingleShotTime)
public class SpringBootThinBenchmark {

	@Benchmark
	public void basic14xThin(Basic14xThinState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void basic14xPrecomputeThin(Basic14xPrecomputeThinState state)
			throws Exception {
		state.run();
	}

	@Benchmark
	public void basic15xThin(Basic15xThinState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void basic13xThin(Basic13xThinState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void petclinicLatestThin(PetclinicLatestThinState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void petclinicLatesPrecomputeThin(PetclinicLatestPrecomputeThinState state)
			throws Exception {
		state.run();
	}

	@Benchmark
	public void petclinicThin(PetclinicThinState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void petclinicPrecomputeThin(PetclinicPrecomputeThinState state)
			throws Exception {
		state.run();
	}

	public static void main(String[] args) throws Exception {
		Basic14xThinState state = new Basic14xThinState();
		state.run();
	}

	@State(Scope.Benchmark)
	public static class Basic14xThinState extends ProcessLauncherState {
		public Basic14xThinState() {
			super("target", "-jar", jarFile("com.example:demo:jar:thin:0.0.1-SNAPSHOT"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class Basic14xPrecomputeThinState extends ProcessLauncherState {
		public Basic14xPrecomputeThinState() {
			super("target", "-jar", jarFile("com.example:demo:jar:thin:0.0.1-SNAPSHOT"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@Setup(Level.Trial)
		public void compute() throws Exception {
			Collection<String> properties = capture("--thin.classpath=properties");
			FileUtils.writeLines(new File("target/thin.properties"), properties);
		}

		@TearDown(Level.Trial)
		public void clean() throws Exception {
			new File("target/thin.properties").delete();
		}

	}

	@State(Scope.Benchmark)
	public static class Basic15xThinState extends ProcessLauncherState {
		public Basic15xThinState() {
			super("target", "-jar", "../src/test/resources/demo-1.5.3-thin.jar",
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class Basic13xThinState extends ProcessLauncherState {
		public Basic13xThinState() {
			super("target", "-jar", "../src/test/resources/demo-1.3.8-thin.jar",
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class PetclinicLatestThinState extends ProcessLauncherState {
		public PetclinicLatestThinState() {
			super("target", "-jar",
					jarFile("com.example:petclinic-latest:jar:thin:1.4.2"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class PetclinicLatestPrecomputeThinState extends ProcessLauncherState {
		public PetclinicLatestPrecomputeThinState() {
			super("target", "-jar",
					jarFile("com.example:petclinic-latest:jar:thin:1.4.2"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@Setup(Level.Trial)
		public void compute() throws Exception {
			Collection<String> properties = capture("--thin.classpath=properties");
			FileUtils.writeLines(new File("target/thin.properties"), properties);
		}

		@TearDown(Level.Trial)
		public void clean() throws Exception {
			new File("target/thin.properties").delete();
		}

	}

	@State(Scope.Benchmark)
	public static class PetclinicThinState extends ProcessLauncherState {
		public PetclinicThinState() {
			super("target", "-jar", jarFile("com.example:petclinic:jar:thin:1.4.2"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class PetclinicPrecomputeThinState extends ProcessLauncherState {
		public PetclinicPrecomputeThinState() {
			super("target", "-jar", jarFile("com.example:petclinic:jar:thin:1.4.2"),
					"--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@Setup(Level.Trial)
		public void compute() throws Exception {
			Collection<String> properties = capture("--thin.classpath=properties");
			FileUtils.writeLines(new File("target/thin.properties"), properties);
		}

		@TearDown(Level.Trial)
		public void clean() throws Exception {
			new File("target/thin.properties").delete();
		}

	}

}
