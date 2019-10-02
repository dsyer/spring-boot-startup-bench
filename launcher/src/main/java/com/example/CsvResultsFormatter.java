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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.util.ScoreFormatter;
import org.openjdk.jmh.util.Statistics;

/**
 * Utility to create a CSV-formatted report.
 */
class CsvResultsFormatter {

	/**
	 * Create a report in CSV format.
	 *
	 * @param results
	 * @return
	 */
	static String createReport(Collection<RunResult> results) {

		StringBuilder report = new StringBuilder(System.lineSeparator());
		Map<String, Integer> params = detectParameters(results);
		Map<String, Integer> auxes = detectAuxes(results);

		StringBuilder header = new StringBuilder();
		header.append("class, method, ");
		params.forEach((key, value) -> header.append(key).append(", "));
		auxes.forEach((key, value) -> header.append(propertyName(key)).append(", "));
		header.append("median, mean, range");
		report.append(header.toString()).append(System.lineSeparator());

		for (RunResult result : results) {
			StringBuilder builder = new StringBuilder();
			if (result.getParams() != null) {
				String benchmark = result.getParams().getBenchmark();
				String cls = benchmark.contains(".")
						? benchmark.substring(0, benchmark.lastIndexOf("."))
						: benchmark;
				String mthd = benchmark.substring(benchmark.lastIndexOf(".") + 1);
				builder.append(cls).append(", ").append(mthd).append(", ");
				for (int i = 0; i < params.values().size(); i++) {
					boolean found = false;
					for (String param : result.getParams().getParamsKeys()) {
						if (params.get(param) == i) {
							builder.append(result.getParams().getParam(param))
									.append(", ");
							found = true;
						}
					}
					if (!found) {
						builder.append(", ");
					}
				}
			}

			if (result.getAggregatedResult() != null) {

				@SuppressWarnings("rawtypes")
				Map<String, Result> second = result.getAggregatedResult()
						.getSecondaryResults();
				for (int i = 0; i < auxes.values().size(); i++) {
					boolean found = false;
					for (String param : second.keySet()) {
						if (auxes.get(param) == i) {
							builder.append(ScoreFormatter.format(
									second.get(param).getStatistics().getPercentile(0.5)))
									.append(", ");
							found = true;
						}
					}
					if (!found) {
						builder.append(", ");
					}
				}
				// primary result is derived from aggregate result
				Statistics statistics = result.getPrimaryResult().getStatistics();
				builder.append(ScoreFormatter.format(statistics.getPercentile(0.5)));
				builder.append(", ");
				builder.append(ScoreFormatter.format(statistics.getMean()));
				builder.append(", ");
				double error = (statistics.getMax() - statistics.getMin()) / 2;
				builder.append(ScoreFormatter.format(error));

				report.append(builder.toString()).append(System.lineSeparator());
			}
		}

		return report.toString();
	}

	private static Map<String, Integer> detectAuxes(Collection<RunResult> results) {
		Map<String, Integer> auxes = new LinkedHashMap<>();
		int auxPlaces = 0;
		for (RunResult result : results) {
			if (result.getAggregatedResult() != null) {
				@SuppressWarnings("rawtypes")
				Map<String, Result> second = result.getAggregatedResult()
						.getSecondaryResults();
				if (second != null) {
					for (String aux : second.keySet()) {
						int count = auxPlaces;
						auxes.computeIfAbsent(aux, key -> count);
						auxPlaces++;
					}
				}
			}
		}
		return auxes;
	}

	private static Map<String, Integer> detectParameters(Collection<RunResult> results) {
		Map<String, Integer> params = new LinkedHashMap<>();
		int paramPlaces = 0;
		for (RunResult result : results) {
			if (result.getParams() != null) {
				for (String param : result.getParams().getParamsKeys()) {
					int count = paramPlaces;
					if (params.containsKey(param)) {
						continue;
					}
					params.put(param, count);
					paramPlaces++;
				}
			}
		}
		return params;
	}

	private static String propertyName(String key) {
		if (key.matches("get[A-Z].*")) {
			key = changeFirstCharacterCase(key.substring(3), false);
		}
		return key;
	}

	private static String changeFirstCharacterCase(String str, boolean capitalize) {
		if (str == null || str.length() == 0) {
			return str;
		}

		char baseChar = str.charAt(0);
		char updatedChar;
		if (capitalize) {
			updatedChar = Character.toUpperCase(baseChar);
		}
		else {
			updatedChar = Character.toLowerCase(baseChar);
		}
		if (baseChar == updatedChar) {
			return str;
		}

		char[] chars = str.toCharArray();
		chars[0] = updatedChar;
		return new String(chars, 0, chars.length);
	}
}
