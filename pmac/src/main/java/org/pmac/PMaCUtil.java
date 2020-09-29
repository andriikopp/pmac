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

	public static PMaCProcessTrace task(String name) {
		return new PMaCProcessTrace(Arrays.asList(new PMaCActivity[] { new PMaCActivity(name) }));
	}

	@SafeVarargs
	public static PMaCProcessTrace sequence(PMaCProcessTrace... x) {
		List<PMaCActivity> workflow = new ArrayList<>();
		Arrays.asList(x).stream().forEach(y -> workflow.addAll(y.getTrace()));
		return new PMaCProcessTrace(workflow);
	}

	public static PMaCProcessTrace parallel(PMaCProcessTrace a, PMaCProcessTrace b) {
		List<PMaCActivity> workflow = new ArrayList<>();

		if (Math.random() > 0.5) {
			workflow.addAll(a.getTrace());
			workflow.addAll(b.getTrace());
		} else {
			workflow.addAll(b.getTrace());
			workflow.addAll(a.getTrace());
		}

		return new PMaCProcessTrace(workflow);
	}

	public static PMaCProcessTrace exclusive(PMaCProcessTrace a, PMaCProcessTrace b) {
		List<PMaCActivity> workflow = new ArrayList<>();

		if (Math.random() > 0.5) {
			workflow.addAll(a.getTrace());
		} else {
			workflow.addAll(b.getTrace());
		}

		return new PMaCProcessTrace(workflow);
	}

	@SafeVarargs
	public static PMaCProcessTrace process(PMaCProcessTrace... x) {
		List<PMaCActivity> workflow = new ArrayList<>();

		for (PMaCProcessTrace y : x) {
			for (PMaCActivity z : y.getTrace()) {
				workflow.add(z);
			}
		}

		return new PMaCProcessTrace(workflow);
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
					List<PMaCActivity> trace = model.getProcess().getTrace();
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

	private static BpmnModelInstance translateToBpmnModel(List<PMaCActivity> x) {
		@SuppressWarnings("rawtypes")
		AbstractFlowNodeBuilder builder = Bpmn.createProcess().startEvent();

		for (PMaCActivity y : x) {
			builder = builder.userTask().name(y.getName());
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
