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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.AverageTime)
public class MainBenchmark {

	@Benchmark
	public void fat(FatState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void launcher(LauncherState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void main(MainState state) throws Exception {
		state.run();
	}

	public static void main(String[] args) throws Exception {
		LauncherState state = new LauncherState();
		state.run();
	}

	@State(Scope.Benchmark)
	public static class FatState extends ProcessLauncherState {
		public FatState() {
			super("target/demo", "--server.port=0");
			fatJar();
			setMarker("started on port");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@TearDown(Level.Trial)
		public void close() throws Exception {
			super.clean();
		}

	}

	@State(Scope.Benchmark)
	public static class LauncherState extends ProcessLauncherState {
		public LauncherState() {
			super("target/demo", "--server.port=0");
			mainClassFromManifest();
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@TearDown(Level.Trial)
		public void close() throws Exception {
			super.clean();
		}
	}

	@State(Scope.Benchmark)
	public static class MainState extends ProcessLauncherState {
		public MainState() {
			super("target/demo", "--server.port=0");
			startClassFromManifest();
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@TearDown(Level.Trial)
		public void close() throws Exception {
			super.clean();
		}

	}

}
