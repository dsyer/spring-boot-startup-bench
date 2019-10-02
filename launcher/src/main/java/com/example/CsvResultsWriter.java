/*
 * Copyright 2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package com.example;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.format.OutputFormat;

class CsvResultsWriter {

	public void write(OutputFormat output, Collection<RunResult> results) {

		try {
			String report = CsvResultsFormatter.createReport(results);

			output.println(report);
		}
		catch (Exception e) {

			StringWriter trace = new StringWriter();
			e.printStackTrace(new PrintWriter(trace));

			output.println("Report creation failed: " + trace.toString());
		}
	}

}
