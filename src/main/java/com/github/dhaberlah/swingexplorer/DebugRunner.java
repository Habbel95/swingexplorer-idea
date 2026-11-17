package com.github.dhaberlah.swingexplorer;

import com.intellij.debugger.impl.GenericDebuggerRunner;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.target.TargetEnvironmentAwareRunProfileState;
import com.intellij.execution.ui.RunContentDescriptor;
import org.jetbrains.annotations.*;
import org.jetbrains.concurrency.Promise;

/**
 * Gets called if the executor wants to execute a java program with swing explorer integration with the debug-option
 *
 * @author d.haberlah, 21.12.2023
 */
public class DebugRunner extends GenericDebuggerRunner {
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

  @NotNull
  @Override
  protected Promise<@Nullable RunContentDescriptor> doExecuteAsync(@NotNull TargetEnvironmentAwareRunProfileState pState,
                                                                   @NotNull ExecutionEnvironment pEnv) throws ExecutionException {
    if (!(pState instanceof JavaCommandLine javaState)) return super.doExecuteAsync(pState, pEnv);
    int port = SwingExplorerSupport.patch(javaState.getJavaParameters());
    return super.doExecuteAsync(pState, pEnv).onSuccess(pDescr -> {
      if (pDescr != null) NotificationListenerImpl.create(pEnv.getProject(), port);
    });
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState pState, @NotNull ExecutionEnvironment pEnv) throws ExecutionException {
    if (!(pState instanceof JavaCommandLine javaState)) return super.doExecute(pState, pEnv);
    int port = SwingExplorerSupport.patch(javaState.getJavaParameters());
    RunContentDescriptor descr = super.doExecute(pState, pEnv);
    if (descr != null) NotificationListenerImpl.create(pEnv.getProject(), port);
    return descr;
  }

}
