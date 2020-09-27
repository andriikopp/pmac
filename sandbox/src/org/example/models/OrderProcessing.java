package org.example.models;

import static org.pmac.PMaCUtil.*;

import java.util.List;

import org.pmac.PMaCLabel;

public class OrderProcessing implements PMaCLabel {

	@Override
	public List<String> getProcess() {
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
