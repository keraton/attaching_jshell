/*
 * Copyright (c) 2017, Olivier Cinquin
 */

package uk.org.cinquin.attaching_jshell;

import static jdk.jshell.Snippet.Status.VALID;
import static jdk.jshell.execution.JdiExecutionControlProvider.PARAM_HOST_NAME;
import static jdk.jshell.execution.JdiExecutionControlProvider.PARAM_TIMEOUT;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import org.junit.jupiter.api.*;

/**
 * Test class to programmatically invoke JShell in a way that it attaches to an
 * already-existing VM.
 * Created by olivier on 4/29/17.
 */
class CustomJShellTest {

	Process process = null;

	@BeforeEach
 	void init() throws IOException, InterruptedException {
		process = JavaProcess.exec(ExistingVMRemoteExecutionControl.class);
	}

	@AfterEach
	void destroy() {
		if (process != null) {
			process.destroy();
		}
	}


	@Test
	void should_run_test () throws InterruptedException {
	    // Given

		Map<String, String> params = Map.of(PARAM_HOST_NAME, "localhost",
											PARAM_TIMEOUT, "10000");

		JShell shell = JShell.builder()
							.executionEngine(new AttachToExistingVMProvider(), params)
							.build();

		// When
		List<SnippetEvent> eval1 = shell.eval("int k = 3 + 15;");
		List<SnippetEvent> eval2 = shell.eval("import uk.org.cinquin.attaching_jshell.VMRemoteTarget;");
		List<SnippetEvent> eval3 = shell.eval("VMRemoteTarget.theGoodsForTesting();");

		// Then
		Assertions.assertEquals(VALID, eval1.get(0).status());
		Assertions.assertEquals(VALID, eval2.get(0).status());
		Assertions.assertEquals(VALID, eval3.get(0).status());


		Thread.sleep(1_000);
		shell.close();

	}

}
