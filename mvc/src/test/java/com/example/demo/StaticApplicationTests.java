package com.example.demo;

import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport.ConditionAndOutcomes;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

public class StaticApplicationTests {

	@Test
	public void test() throws Exception {
		StringBuilder builder = new StringBuilder("package com.sample;\n\n"
				+ "import org.springframework.boot.SpringApplication;\n"
				+ "import org.springframework.context.annotation.Configuration;\n"
				+ "import org.springframework.context.annotation.Import;\n");
		Set<String> imports = new LinkedHashSet<>();
		Set<String> configs = new LinkedHashSet<>();
		ConfigurableApplicationContext context = SpringApplication
				.run(DemoApplication.class, "--debug", "--server.port=0");
		ConditionEvaluationReport report = context
				.getBean(ConditionEvaluationReport.class);
		for (Entry<String, ConditionAndOutcomes> entry : report
				.getConditionAndOutcomesBySource().entrySet()) {
			ConditionAndOutcomes outcomes = entry.getValue();
			String name = entry.getKey();
			if (outcomes.isFullMatch() && isAutoConfig(name)) {
				imports.add(name);
				configs.add(StringUtils.getFilenameExtension(name));
			}
		}
		for (String type : report.getUnconditionalClasses()) {
			imports.add(type);
			configs.add(StringUtils.getFilenameExtension(type));
		}
		for (String string : imports) {
			builder.append("import " + string + ";\n");
		}
		builder.append("\n@Configuration\n");
		builder.append("@Import({");
		builder.append(
				StringUtils.collectionToDelimitedString(configs, ".class, \n    "));
		builder.append(".class})\n");
		builder.append("public class SlimApplication {\n" + "\n"
				+ "	public static void main(String[] args) {\n"
				+ "		SpringApplication.run(SlimApplication.class, args);\n" + "	}\n"
				+ "}\n" + "");
		context.close();
		System.err.println(builder);
	}

	private boolean isAutoConfig(String name) {
		return !name.contains("#") && !name.contains("$")
				&& name.contains("AutoConfiguration");
	}

}
