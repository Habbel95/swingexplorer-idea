package com.github.dhaberlah.swingexplorer;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.*;

import java.nio.file.*;

/**
 * Dependency to external jar files
 *
 * @author w.glanzer, 12.04.2020
 */
class Dependencies {

  private VirtualFile swagJarFile;
  private VirtualFile swexplJarFile;

  /**
   * Returns the agent (swag.jar)
   *
   * @return the agent file
   * @throws ExecutionException if file could not be found
   */
  @NotNull
  public VirtualFile getAgentFile() throws ExecutionException {
    checkValid();
    return swagJarFile;
  }

  /**
   * Returns the explorer (swexpl.jar)
   *
   * @return the explorer file
   * @throws ExecutionException if file could not be found
   */
  @NotNull
  public VirtualFile getExplorerFile() throws ExecutionException {
    checkValid();
    return swexplJarFile;
  }

  /**
   * @throws ExecutionException if this object is not valid and the dependencies could not be found
   */
  private void checkValid() throws ExecutionException {
    if (swagJarFile == null || swexplJarFile == null) initJarFiles();
    if (swagJarFile == null || swexplJarFile == null) throw new ExecutionException("SwingExplorer jars could not be found");
  }

  /**
   * Searches the dependency files in plugin directory
   */
  private void initJarFiles() {
    String ownJar = PathManager.getJarPathForClass(Dependencies.class);
    if (ownJar == null) return;

    Path libDir = Paths.get(ownJar).getParent();
    if (libDir == null) return;

    swagJarFile = findFile(libDir.resolve("swag.jar"));
    swexplJarFile = findFile(libDir.resolve("swexpl.jar"));
  }

  @Nullable
  private static VirtualFile findFile(@NotNull Path pPath) {
    return Files.isRegularFile(pPath) ? LocalFileSystem.getInstance().refreshAndFindFileByNioFile(pPath) : null;
  }

}
