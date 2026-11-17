package com.github.dhaberlah.swingexplorer;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Shared logic of {@link Runner} and {@link DebugRunner}: wraps the launched program with SwingExplorer.
 */
final class SwingExplorerSupport {
  static final String LAUNCHER_CLASS = "org.swingexplorer.Launcher";
  private static final String PORT_PROPERTY = "swex.mport";

  private SwingExplorerSupport() {
  }

  /**
   * Adds the SwingExplorer agent and jars to the given parameters and replaces the main class with the
   * SwingExplorer launcher (the real main class becomes its first program argument).
   *
   * @param pJavaParameters parameters of the program to start
   * @return the JMX port SwingExplorer listens on for IDE notifications
   */
  static int patch(@NotNull JavaParameters pJavaParameters) throws ExecutionException {
    ParametersList vmParametersList = pJavaParameters.getVMParametersList();

    // already patched (e.g. state reused) -> don't wrap twice
    if (LAUNCHER_CLASS.equals(pJavaParameters.getMainClass())) {
      String port = vmParametersList.getPropertyValue(PORT_PROPERTY);
      if (port != null) return Integer.parseInt(port);
    }

    Dependencies dependencies = new Dependencies();
    int port = getFreePort();

    pJavaParameters.getClassPath().add(dependencies.getAgentFile());
    pJavaParameters.getClassPath().add(dependencies.getExplorerFile());

    vmParametersList.add("-javaagent:" + dependencies.getAgentFile().getPath());
    vmParametersList.add("-Xbootclasspath/a:" + dependencies.getAgentFile().getPath());
    vmParametersList.add("-D" + PORT_PROPERTY + "=" + port);
    vmParametersList.add("-Dcom.sun.management.jmxremote");

    String mainClass = pJavaParameters.getMainClass();
    pJavaParameters.setMainClass(LAUNCHER_CLASS);
    pJavaParameters.getProgramParametersList().addAt(0, mainClass);
    return port;
  }

  private static int getFreePort() throws ExecutionException {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    } catch (IOException e) {
      throw new ExecutionException("Could not open port!", e);
    }
  }
}
