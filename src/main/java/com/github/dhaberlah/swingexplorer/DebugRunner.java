package com.github.dhaberlah.swingexplorer;


import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.*;
import org.jetbrains.annotations.NotNull;

/**
 * Gets called if the executor wants to execute a java program with swing explorer integration with the debug-option
 *
 * @author d.haberlah, 21.12.2023
 */
public class DebugRunner extends Runner {
  public static final String ID = "DebugWithSEClass";

  @NotNull
  @Override
  public String getRunnerId() {
    return ID;
  }

  @Override
  public boolean canRun(@NotNull String pExecutorID, @NotNull RunProfile pProfile) {
    return pExecutorID.equals(DebugExecutor.ID) && (pProfile instanceof ApplicationConfiguration);
  }

  /**
   @see Runner#initJavaSettings(JavaCommandLineState, Dependencies, int, String...)
   */
  @Override
  protected void initJavaSettings(@NotNull JavaCommandLineState pProfileState,
                                  @NotNull Dependencies pDependencies,
                                  int pPort,
                                  String... additionalVmParameter)
      throws ExecutionException {

    super.initJavaSettings(pProfileState, pDependencies, pPort,
                           "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5050");
  }

}
