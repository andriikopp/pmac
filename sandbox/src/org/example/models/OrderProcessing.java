package org.example.models;

import static org.pmac.PMaCUtil.*;

import org.pmac.PMaCLabel;
import org.pmac.PMaCProcessTrace;

public class OrderProcessing implements PMaCLabel {

	@Override
	public PMaCProcessTrace getProcess() {
		return process(
			task("Receive order"),
			task("Verify order"),
			parallel(
				sequence(
					task("Check warehouse"),
					exclusive(
						task("Order from supplier"),
						task("Order from warehouse")
					)
				),
				task("Contact client")
			),
			exclusive(
				task("Confirm order"),
				task("Cancel order")
			)
		);
	}
}
