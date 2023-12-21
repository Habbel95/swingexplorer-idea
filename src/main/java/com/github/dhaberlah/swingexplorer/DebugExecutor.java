package com.github.dhaberlah.swingexplorer;

import com.intellij.execution.executors.DefaultDebugExecutor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * Creates a custom debug action and links it to the DebugRunner.
 *
 * @author d.haberlah, 21.12.2023
 * @see DebugRunner
 */
public class DebugExecutor extends DefaultDebugExecutor {
  public static final String ID = "Debug-with-SE";
  private static final ImageIcon ICON = new ImageIcon(Objects.requireNonNull(DebugExecutor.class.getResource("debug.png")));

  @NotNull
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getContextActionId() {
    return DebugRunner.ID;
  }

  @NotNull
  @Override
  public Icon getIcon() {
    return ICON;
  }
}
