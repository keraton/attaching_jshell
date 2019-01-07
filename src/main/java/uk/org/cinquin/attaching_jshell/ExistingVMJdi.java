package uk.org.cinquin.attaching_jshell;

import com.sun.jdi.VirtualMachine;
import jdk.jshell.execution.JdiExecutionControl;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import uk.org.cinquin.attaching_jshell.internal.JavaProcess;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static jdk.jshell.execution.Util.remoteInputOutput;

/**
 * This class is derived from {@link jdk.jshell.execution.JdiDefaultExecutionControl}.
 * Instead of launching a new VM in which to run snippets, it attaches to an already-running
 * VM, identified using the hostname and port parameters. The attached VM is kept alive
 * when the execution engine is closed.
 *
 * Ideally this class would extend {@link jdk.jshell.execution.JdiDefaultExecutionControl} and
 * thus avoid code duplication, but that did not seem possible given limited visibility of
 * members of that class.
 *
 * Created by olivier on 4/29/17.
 */
public class ExistingVMJdi extends JdiExecutionControl {



	private static Process process = null;


	/**
	 * Creates an ExecutionControl instance based on a JDI
	 * {@code ListeningConnector} or {@code LaunchingConnector}.
	 *
	 * Initialize JDI and use it to launch the remote JVM. Set-up a socket for
	 * commands and results. This socket also transports the user
	 * input/output/error.
	 *
	 * @param env the context passed by
	 * {@brokenlink jdk.jshell.spi.ExecutionControl#start(jdk.jshell.spi.ExecutionEnv) }
	 * hostname, applies to listening only (!isLaunch)
	 * @param standAlone
	 * @return the channel
	 * @throws IOException if there are errors in set-up
	 */
	static ExecutionControl create(ExecutionEnv env, int millsTimeout, String hostName, int port, boolean remoteCallback, boolean standAlone, String remoteUrl)  {
		try (final ServerSocket listener = new ServerSocket(port, 1)) {
			// timeout on I/O-socket
			listener.setSoTimeout(millsTimeout);

			if (remoteCallback) {
				callRemoteJshell(remoteUrl, hostName, port);
			}
			else if (standAlone) {
				// Kill the process if it is exist
				if (process != null && process.isAlive()) {
					process.destroy();

					// Wait until process is really dead
					while (process.isAlive()) {
						Thread.sleep(10);
					}
				}
				int debugPort = port + 1;
				process = JavaProcess.exec(ExistingVMRemoteExecutionControl.class, debugPort);

				// Make sure that the server is up for the client 😓
				Thread.sleep(500);

				callRemoteJshell(remoteUrl, hostName, port);
			}

			// Set-up the commands/results on the socket.  Piggy-back snippet
			// output.
			Socket socket = listener.accept();

			// out before in -- match remote creation so we don't hang
			OutputStream out = socket.getOutputStream();
			Map<String, OutputStream> outputs = new HashMap<>();
			outputs.put("out", env.userOut());
			outputs.put("err", env.userErr());

			Map<String, InputStream> input = new HashMap<>();
			input.put("in", env.userIn());

			return remoteInputOutput(socket.getInputStream(), out, outputs, input,
				(objIn, objOut) -> new ExistingVMJdi(objOut, objIn));
		}
		catch (Exception  e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private static void callRemoteJshell(String remoteUrl, String hostname, int port) throws IOException, InterruptedException {

		// http://localhost:8000/startJshell?hostname=localhost&port=12345
		HttpClient httpClient = HttpClient.newHttpClient();
		httpClient.send(HttpRequest.newBuilder()
								.uri(URI.create(remoteUrl + "?hostname=" + hostname + "&port=" + port))
								.GET()
								.build(),
								ofString());
	}


	/**
	 * Create an instance.
	 *
	 * @param cmdout the output for commands
	 * @param cmdin the input for responses
	 */
	private ExistingVMJdi(ObjectOutput cmdout, ObjectInput cmdin) {
		super(cmdout, cmdin);
	}

	@Override
	public void close() {
		// Kill the other VM when we shutdown
		process.destroy();
		super.close();
	}


	@Override
	protected synchronized VirtualMachine vm() {
		// NOT IMPLEMENTED
		return null;
	}

}
