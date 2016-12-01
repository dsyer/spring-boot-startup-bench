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
public class PetclinicBenchmark {

	@Benchmark
	public void fatJar(BasicState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void noverify(NoVerifyState state) throws Exception {
		state.run();
	}

	@Benchmark
	public void devtoolsRestart(ExplodedDevtoolsState state) throws Exception {
		state.run();
	}

	public static void main(String[] args) throws Exception {
		BasicState state = new BasicState();
		state.run();
	}

	@State(Scope.Benchmark)
	public static class BasicState extends ProcessLauncherState {
		public BasicState() {
			super("target", "-jar", jarFile("com.example:petclinic:1.4.2"), "--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class NoVerifyState extends ProcessLauncherState {
		public NoVerifyState() {
			super("target", "-noverify", "-jar", jarFile("com.example:petclinic:1.4.2"), "--server.port=0");
		}

		@TearDown(Level.Iteration)
		public void stop() throws Exception {
			super.after();
		}
	}

	@State(Scope.Benchmark)
	public static class ExplodedDevtoolsState extends DevToolsLauncherState {

		public ExplodedDevtoolsState() {
			super("target/demo", "/BOOT-INF/classes/.restart", jarFile("com.example:petclinic:1.4.2"), "-cp", "BOOT-INF/classes:BOOT-INF/lib/*",
					"-Dspring.devtools.livereload.enabled=false", "-Dspring.devtools.restart.pollInterval=100",
					"-Dspring.devtools.restart.quietPeriod=10",
					"org.springframework.samples.petclinic.PetClinicApplication", "--server.port=0");
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

}
