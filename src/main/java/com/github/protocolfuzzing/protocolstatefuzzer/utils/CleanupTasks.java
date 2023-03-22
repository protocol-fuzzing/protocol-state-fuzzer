package com.github.protocolfuzzing.protocolstatefuzzer.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * Collection of tasks which are executed on termination. An alternative to shutdown hook.
 */
public class CleanupTasks {
	protected List<Runnable> tasks;

	public CleanupTasks() {
		tasks = new LinkedList<>();
	}

	public void submit(Runnable runnable) {
		tasks.add(runnable);
	}

	public void execute() {
		for (Runnable task : tasks) {
			task.run();
		}
	}
}
