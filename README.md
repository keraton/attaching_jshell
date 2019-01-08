# Attaching jshell

JShell is a very useful new tool in JDK 9 that allows for interactive use of Java in a
"read-eval-print loop". It is obvious that JShell would be particularly well suited for
interactions with live JVMs, to examine their behavior in real time or to leverage their
resources (e.g. if they have expensive objects already loaded, or if they run on powerful
remote machines on which it is not practical to launch a JShell instance). Attaching to
live JVMs would also provide an indirect way of populating objects in the JShell
environment (a feature that
[has been requested](http://mail.openjdk.java.net/pipermail/kulla-dev/2016-November/001774.html)).
Unfortunately the current implementation of JShell does not give the option to connect to
an already-running JVM (it starts a new JVM on the local host), although that
[may change in the future](https://bugs.openjdk.java.net/browse/JDK-8131021).

This project provides a JShell execution engine that can attach to any already-running
JVM, as long as that JVM has been started appropriately.

## Example usage
- call JShell as follows:
`java -cp lib/attaching_jshell.jar jdk.internal.jshell.tool.JShellToolProvider  --execution "attachToExistingVM:hostname(XXXhostname),port(XXXport)"` it will wait until 60 seconds to be called.
- Start the target JVM with and call
`new uk.org.cinquin.attaching_jshell.startJshell(host, port)` from that JVM
prior to using JShell using the same values of `XXXhostname` and `XXXport` as above

A simple way of making objects accessible to JShell is to have static fields point at
them (see example in `ExistingVMRemoteExecutionControl` class).

The example commands above are provided in the test scripts `run_test_target` (to be
executed first) and `run_jshell`. From the JShell instance, run for example
```
 import uk.org.cinquin.attaching_jshell.ExistingVMRemoteExecutionControl;
 String s = ExistingVMRemoteExecutionControl.breakPoint()
```

To debug the target JVM, one can run it with jdwp option `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:4568`, then with an IDE we can get do a remote debugging

## Implementation notes
The need to use the `ExistingVMRemoteExecutionControl` class from the target JVM stems
from limitations in the Java Debug Interface (JDI). An alternative would be to use the
JVM Tool Interface, which would require compiling platform-specific native binaries, or
to use JDI in a more hackish way to get the JShell connection established.


## Limitations

- The standard error stream of the target JVM is captured by JShell and is currently not
restored when JShell exits.
- The versions of JShell on the target JVM and the one running JShell probably need to
match.
