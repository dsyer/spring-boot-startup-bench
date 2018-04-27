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

import java.util.List;

import com.example.boot.BootApplication;
import com.example.demo.DemoApplication;
import com.example.micro.MicroApplication;
import com.example.mini.MiniApplication;

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
public class MiniBenchmark {

	@Benchmark
	public void boot(MainState state) throws Exception {
		state.setMainClass(BootApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void mini(MainState state) throws Exception {
		state.setMainClass(MiniApplication.class.getName());
		state.run();
	}

	@Benchmark
	public void micro(MainState state) throws Exception {
		state.setMainClass(MicroApplication.class.getName());
		state.run();
	}

	@State(Scope.Benchmark)
	public static class MainState extends ProcessLauncherState {

		public static enum Sample {
			jlog, demo;

			private Class<?> config;

			private Sample(Class<?> config) {
				this.config = config;
			}

			private Sample() {
				this.config = DemoApplication.class;
			}

			public Class<?> getConfig() {
				return config;
			}

		}

		@Param
		private Sample sample = Sample.demo;

		public MainState() {
			super("target");
		}

		@TearDown(Level.Invocation)
		public void stop() throws Exception {
			super.after();
		}

		@Override
		protected void customize(List<String> args) {
			args.add("-Dserver.port=0");
		}

		@Setup(Level.Trial)
		public void start() throws Exception {
			if (sample != Sample.demo) {
				setProfiles(sample.toString());
			}
			super.before();
		}
	}

}
