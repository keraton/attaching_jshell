/*
 * Copyright (c) 2017, Olivier Cinquin
 */

package uk.org.cinquin.attaching_jshell;

import static jdk.jshell.execution.JdiExecutionControlProvider.PARAM_HOST_NAME;
import static jdk.jshell.execution.JdiExecutionControlProvider.PARAM_TIMEOUT;

import java.util.HashMap;
import java.util.Map;

import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

/**
 * This provider is detected at runtime by {@link jdk.jshell.spi.ExecutionControl}::generate.
 * It needs to be advertised as a service in a META-INF/services directory to be made
 * available.
 *
 * Created by olivier on 4/29/17.
 */
public class AttachToExistingVMProvider implements ExecutionControlProvider {

    private final String PARAM_PORT = "port";
    private final String PARAM_REMOTE_CALLBACK = "remoteCallback";
    private final String PARAM_REMOTE_URL = "remoteUrl";

	@Override
	public String name() {
		return "attachToExistingVM";
	}


	public Map<String,String> defaultParameters() {
		Map<String, String> result = new HashMap<>();

		// Locale parameters
		result.put(PARAM_HOST_NAME, "localhost");
		result.put(PARAM_PORT, "4242");
        result.put(PARAM_TIMEOUT, "60000"); // 60s

        // Remote parameters
        result.put(PARAM_REMOTE_CALLBACK, "true");
        result.put(PARAM_REMOTE_URL, "http://localhost:8000/startJshell");

		return result;
	}

	@Override
	public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters)  {
		Map<String, String> dp  = defaultParameters();
		if (parameters == null) {
			parameters = dp;
		}
		int timeout = Integer.parseUnsignedInt(parameters.getOrDefault(PARAM_TIMEOUT, dp.get(PARAM_TIMEOUT)));
		String hostName = parameters.getOrDefault(PARAM_HOST_NAME, dp.get(PARAM_HOST_NAME));
		int port = Integer.parseUnsignedInt(parameters.getOrDefault(PARAM_PORT, dp.get(PARAM_PORT)));

        boolean remoteCallback = Boolean.parseBoolean(parameters.getOrDefault(PARAM_REMOTE_CALLBACK, dp.get(PARAM_REMOTE_CALLBACK)));
        String remoteHostName = parameters.getOrDefault(PARAM_REMOTE_URL, dp.get(PARAM_REMOTE_URL));

		return ExistingVMJdi.create(env, timeout, hostName, port, remoteCallback, remoteHostName);
	}
}
