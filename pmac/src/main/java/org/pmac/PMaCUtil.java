package org.pmac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.reflections.Reflections;

public class PMaCUtil {
	public static final String MODELS_DIR = "models";
	public static final int TRACES_NUM = 1000;

	private PMaCUtil() {
	}

	public static List<String> task(String name) {
		return Arrays.asList(new String[] { name });
	}

	@SafeVarargs
	public static List<String> sequence(List<String>... x) {
		List<String> workflow = new ArrayList<>();
		Arrays.asList(x).stream().forEach(y -> workflow.addAll(y));
		return workflow;
	}

	public static List<String> parallel(List<String> a, List<String> b) {
		List<String> workflow = new ArrayList<>();

		if (Math.random() > 0.5) {
			workflow.addAll(a);
			workflow.addAll(b);
		} else {
			workflow.addAll(b);
			workflow.addAll(a);
		}

		return workflow;
	}

	public static List<String> exclusive(List<String> a, List<String> b) {
		List<String> workflow = new ArrayList<>();

		if (Math.random() > 0.5) {
			workflow.addAll(a);
		} else {
			workflow.addAll(b);
		}

		return workflow;
	}

	@SafeVarargs
	public static List<String> process(List<String>... x) {
		List<String> workflow = new ArrayList<>();

		for (List<String> y : x) {
			for (String z : y) {
				workflow.add(z);
			}
		}

		return workflow;
	}

	public static void run(String pack) {
		try {
			Reflections reflections = new Reflections(pack);
			Set<Class<? extends PMaCLabel>> annotated = reflections.getSubTypesOf(PMaCLabel.class);

			System.out.println("Models processed:");

			for (Class<?> clazz : annotated) {
				Map<String, BpmnModelInstance> traces = new HashMap<>();

				for (int i = 1; i <= TRACES_NUM; i++) {
					PMaCLabel model = (PMaCLabel) clazz.newInstance();
					List<String> trace = model.getProcess();
					traces.put(trace.toString(), translateToBpmnModel(trace));
				}

				int traceNum = 1;

				for (String key : traces.keySet()) {
					storeBpmnModelInstance(clazz.getSimpleName(), String.valueOf(traceNum), traces.get(key));
					traceNum++;
				}

				System.out.printf("- %s\n", clazz.getSimpleName());
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static BpmnModelInstance translateToBpmnModel(List<String> x) {
		@SuppressWarnings("rawtypes")
		AbstractFlowNodeBuilder builder = Bpmn.createProcess().startEvent();

		for (String y : x) {
			builder = builder.userTask().name(y);
		}

		return builder.endEvent().done();
	}

	private static void storeBpmnModelInstance(String processName, String traceName, BpmnModelInstance modelInstance) {
		try {
			if (!Files.exists(Paths.get("./" + MODELS_DIR + "/" + processName))) {
				new File("./" + MODELS_DIR + "/" + processName).mkdirs();
			}

			Files.write(Paths.get("./" + MODELS_DIR + "/" + processName + "/" + traceName + ".bpmn"),
					Bpmn.convertToString(modelInstance).getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
