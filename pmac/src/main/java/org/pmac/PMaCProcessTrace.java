package org.pmac;

import java.util.List;

public class PMaCProcessTrace {
	private List<PMaCActivity> trace;

	public PMaCProcessTrace(List<PMaCActivity> trace) {
		this.trace = trace;
	}

	public List<PMaCActivity> getTrace() {
		return trace;
	}

	public void setTrace(List<PMaCActivity> trace) {
		this.trace = trace;
	}

	@Override
	public String toString() {
		return trace.toString();
	}
}
