package jmeter_runner.server.build_perfmon;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BaseSeries extends Series {
	public BaseSeries(@NotNull String label) {
		super(label);
	}

	@Override
	public List<List<Long>> getValues() {
		return toListFormat();
	}
}
