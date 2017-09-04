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

import com.example.demo.DemoApplication;
import com.example.func.FuncApplication;
import com.example.lite.LiteApplication;
import com.example.slim.SlimApplication;
import com.example.thin.ThinApplication;

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
public class DemoBenchmark {

	@Benchmark
	public void demo(ApplicationState state) throws Exception {
		state.setMainClass(DemoApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void actr(Actr state) throws Exception {
		state.setMainClass(DemoApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void jdbc(Jdbc state) throws Exception {
		state.setMainClass(DemoApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void empty(Empt state) throws Exception {
		state.setMainClass(DemoApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void slim(ApplicationState state) throws Exception {
		state.setMainClass(SlimApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void thin(ApplicationState state) throws Exception {
		state.setMainClass(ThinApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void lite(ApplicationState state) throws Exception {
		state.setMainClass(LiteApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void func(ApplicationState state) throws Exception {
		state.setMainClass(FuncApplication.class.getName());
		state.run();
	}

	@State(Scope.Benchmark)
	public static class ApplicationState extends ProcessLauncherState {
		
		public ApplicationState() {
			super("target", "--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@Setup(Level.Trial)
		public void start() throws Exception {
			super.before();
		}
	}

	@State(Scope.Benchmark)
	public static class Actr extends ApplicationState {
	}

	@State(Scope.Benchmark)
	public static class Jdbc extends ApplicationState {
	}

	@State(Scope.Benchmark)
	public static class Empt extends ApplicationState {
	}
}
