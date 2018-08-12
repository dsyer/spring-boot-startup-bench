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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

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
import org.openjdk.jmh.util.FileUtils;

@Measurement(iterations = 5)
@Warmup(iterations = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.SingleShotTime)
public class CdsBenchmark {

	@Benchmark
	public void main(SnapshotState state) throws Exception {
		state.setMainClass(state.getSample().getConfig().getName());
		state.run();
	}

	@Benchmark
	public void old(OldState state) throws Exception {
		state.setMainClass(state.getSample().getConfig().getName());
		state.run();
	}

	@State(Scope.Benchmark)
	public static class SnapshotState extends ApplicationState {
		@Override
		@Setup(Level.Trial)
		public void start() throws Exception {
			if (getSample() != Sample.demo) {
				setProfiles(getSample().toString(), "snapshot");
			}
			else {
				setProfiles("snapshot");
			}
			super.start();
		}
	}

	@State(Scope.Benchmark)
	public static class OldState extends ApplicationState {
		@Override
		@Setup(Level.Trial)
		public void start() throws Exception {
			if (getSample() != Sample.demo) {
				setProfiles(getSample().toString(), "old");
			}
			else {
				setProfiles("old");
			}
			super.start();
		}
	}

	@State(Scope.Benchmark)
	public static class ApplicationState extends ProcessLauncherState {

		public static enum Sample {
			empt, demo, actr, jdbc, actj, jpae(JpaApplication.class), conf;

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

		public ApplicationState() {
			super("target", "--server.port=0");
		}

		@Override
		protected String getClasspath() {
			return super.getClasspath(false);
		}

		@Override
		protected void customize(List<String> args) {
			args.addAll(Arrays.asList("-Xshare:on", "-XX:+UseAppCDS",
					"-XX:SharedArchiveFile=app.jsa"));
			super.customize(args);
		}

		@TearDown(Level.Invocation)
		public void stop() throws Exception {
			super.after();
		}

		protected void start() throws Exception {
			Process started = exec(
					new String[] { "-Xshare:off", "-XX:+UseAppCDS",
							"-XX:DumpLoadedClassList=app.classlist", "-cp", "" },
					"--server.port=0");
			output(new BufferedReader(new InputStreamReader(started.getInputStream())),
					"Started");
			started.destroyForcibly();
			Process dump = exec(new String[] { "-Xshare:dump", "-XX:+UseAppCDS",
					"-XX:SharedClassListFile=app.classlist",
					"-XX:SharedArchiveFile=app.jsa", "-cp", "" });
			FileUtils.readAllLines(dump.getInputStream());
			dump.waitFor();
			super.before();
		}

		public Sample getSample() {
			return sample;
		}

		public void setSample(Sample sample) {
			this.sample = sample;
		}

	}

}
