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

	@Benchmark
	public void fatJar142(FatJar142State state) throws Exception {
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
		} finally {
			state.stop();
		}
	}

	@State(Scope.Benchmark)
	public static class FatJar142State extends ProcessLauncherState {
		public FatJar142State() {
			super("target", "-jar", jarFile("com.example:configserver:jar:142:0.0.1-SNAPSHOT"), "--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class FatJar138State extends ProcessLauncherState {
		public FatJar138State() {
			super("target", "-jar", jarFile("com.example:configserver:jar:138:0.0.1-SNAPSHOT"), "--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class ExplodedDevtoolsState extends DevToolsLauncherState {
		public ExplodedDevtoolsState() {
			super("target/demo", "/BOOT-INF/classes/.restart", jarFile("com.example:configserver:jar:142:0.0.1-SNAPSHOT"), "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", "-Dspring.devtools.livereload.enabled=false",
					"-Dspring.devtools.restart.pollInterval=100", "-Dspring.devtools.restart.quietPeriod=10",
					"demo.ConfigServerApplication", "--server.port=0");
		}

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
			super("target/demo", "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", "demo.ConfigServerApplication",
					"--server.port=0");
			unpack("target/demo", jarFile("com.example:configserver:jar:142:0.0.1-SNAPSHOT"));
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

}
