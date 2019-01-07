package uk.org.cinquin.attaching_jshell;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jdk.jshell.execution.RemoteExecutionControl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.concurrent.CompletableFuture.runAsync;
import static jdk.jshell.execution.Util.forwardExecutionControlAndIO;


public class ExistingVMRemoteExecutionControl extends RemoteExecutionControl {

	/**
	 * Launch the agent, connecting to the JShell-core over the socket specified
	 * in the command-line argument.
	 *
	 * This method is adapted from the corresponding method in the base JDK class.
	 *
	 * @param args hostname:port
	 * @throws Exception any unexpected exception
	 */
	public static void startJshell(String hostname, String port)  {
        runAsync(()-> {

            try (final Socket socket = new Socket(hostname, Integer.parseInt(port))) {

                InputStream inStream = socket.getInputStream();
                OutputStream outStream = socket.getOutputStream();
                Map<String, Consumer<OutputStream>> outputs = new HashMap<>();
                outputs.put("out", st -> System.setOut(new PrintStream(st, true)));
                outputs.put("err", st -> System.setErr(new PrintStream(st, true)));
                Map<String, Consumer<InputStream>> input = new HashMap<>();
                input.put("in", System::setIn);

                // And forward
                forwardExecutionControlAndIO(new RemoteExecutionControl(), inStream, outStream, outputs, input);

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
	}


	public static void main(String[] args) throws IOException {
        startServer();
    }

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/startJshell", new JshellCallHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class JshellCallHandler implements HttpHandler {


        @Override
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            // hostname=...&port=...
            Map<String,String> keyParams = new HashMap<>();
            String[] splits = query.split("&");
            for (String split : splits) {
                String[] keyValue = split.split("=");
                keyParams.put(keyValue[0], keyValue[1]);
            }

            ExistingVMRemoteExecutionControl.startJshell(keyParams.get("hostname"), keyParams.get("port"));


            String response = "JDI interface started";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static void breakPoint() {
        System.out.println("hello");// Put break point in here !!
    }
}
