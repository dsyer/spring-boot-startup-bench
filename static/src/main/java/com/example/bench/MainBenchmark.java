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
import com.example.jpa.JpaApplication;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.SingleShotTime)
public class MainBenchmark {

	@Benchmark
	public void main(MainState state) throws Exception {
		state.setMainClass(state.sample.getConfig().getName());
		state.run();
	}

	@Benchmark
	public void live(DynamicState state) throws Exception {
		state.setMainClass(state.sample.getConfig().getName());
		state.run();
	}

	@State(Scope.Benchmark)
	public static class MainState extends ProcessLauncherState {

		public static enum Sample {
			empt, demo, actr, jdbc, actj, jpae(
					JpaApplication.class), conf, erka, busr, zuul, erkb, slth;

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
			super("target", "--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}

		@Setup(Level.Trial)
		public void start() throws Exception {
			if (sample != Sample.demo) {
				setProfiles(sample.toString());
			}
			super.before();
		}
	}

	@State(Scope.Benchmark)
	public static class DynamicState extends DevToolsLauncherState {

		private static final Logger log = LoggerFactory.getLogger(DynamicState.class);

		public static enum Sample {
			demo, actr, jdbc, actj, jpae(
					JpaApplication.class), conf, erka, busr, zuul, erkb, slth;

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

		public DynamicState() {
			super("target", "classes/.restart");
		}

		@TearDown(Level.Trial)
		public void stop() throws Exception {
			super.after();
		}

		@Setup(Level.Iteration)
		public void touch() throws Exception {
			log.info("Starting iteration");
			super.update();
			log.info("Wrote trigger file");
		}

		@Setup(Level.Trial)
		public void start() throws Exception {
			log.info("Starting trial");
			if (sample != Sample.demo) {
				setProfiles(sample.toString(), "devtools");
			}
			else {
				setProfiles("devtools");
			}
			super.before();
		}
	}
}
