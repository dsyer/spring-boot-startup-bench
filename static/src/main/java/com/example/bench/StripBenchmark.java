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
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.SingleShotTime)
public class StripBenchmark {

	@Benchmark
	public void strip(ApplicationState state) throws Exception {
		state.run();
	}

	@State(Scope.Benchmark)
	public static class ApplicationState extends ProcessLauncherState {

		public static enum Sample {

			slim(SlimApplication.class), thin(ThinApplication.class), lite(
					LiteApplication.class), func(FuncApplication.class);

			private Class<?> config;

			private Sample(Class<?> config) {
				this.config = config;
			}

			public Class<?> getConfig() {
				return this.config;
			}
		}

		@Param
		public Sample sample = Sample.thin;

		public ApplicationState() {
			super("target", "--server.port=0");
		}

		@TearDown(Level.Invocation)
		public void stop() throws Exception {
			super.after();
		}

		@Setup(Level.Trial)
		public void start() throws Exception {
			setMainClass(sample.getConfig().getName());
			super.before();
		}
	}

}
