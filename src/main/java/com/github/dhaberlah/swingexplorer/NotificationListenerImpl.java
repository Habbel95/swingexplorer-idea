package com.github.dhaberlah.swingexplorer;

import com.intellij.execution.ExecutionException;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.notification.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.util.Map;

/**
 * Listener that gets called, if an action happened in swingexplorer frame (something like "navigate to")
 *
 * @author w.glanzer, 25.09.2015
 */
class NotificationListenerImpl implements Runnable, NotificationListener {
  private static final String NOTIFICATION_ID_WINDOW = "SWEXP_NOTIFICATION";
  private final Project project;
  private final int port;

  private NotificationListenerImpl(@NotNull Project pProject, int pPort) {
    project = pProject;
    port = pPort;
  }

  /**
   * Creates a new NotificationListener and connects it automatically to the running instance
   *
   * @param pProject Current Project
   * @param pPort    Port to connect to
   */
  public static void create(@NotNull Project pProject, int pPort) {
    ApplicationManager.getApplication().executeOnPooledThread(new NotificationListenerImpl(pProject, pPort));
  }

  @Override
  public void run() {
    try {
      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/server");
      //noinspection resource do not close, because we need it asynchronously
      JMXConnector jmxc = connectToSwingExplorer(url);
      MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
      ObjectName name = new ObjectName("org.swingexplorer:name=IDESupport");
      mbsc.invoke(name, "connect", new Object[0], new String[0]);
      mbsc.addNotificationListener(name, this, null, null);
    } catch (Exception e) {
      throw new RuntimeException("failed to connect to running instance", e);
    }
  }

  @Override
  public void handleNotification(Notification notification, Object handback) {
    //noinspection unchecked
    Map<Object, Object> data = (Map<Object, Object>) notification.getUserData();
    if (data == null) return;
    String className = (String) data.get("className");
    Object lineNumber = data.get("lineNumber");

    if (className != null) navigate(project, className, lineNumber instanceof Integer ? (Integer) lineNumber : 1);
  }

  /**
   * Connects to a running instance of Swing Explorer
   *
   * @param pURL URL to connect to
   * @return the connected Connector, not null
   * @throws Exception if unable to connect
   */
  @NotNull
  private JMXConnector connectToSwingExplorer(@NotNull JMXServiceURL pURL) throws Exception {
    Exception ex = null;
    for (int retries = 0; retries < 10; retries++) {
      try {
        return JMXConnectorFactory.connect(pURL, null);
      } catch (IOException e) {
        ex = e;

        // Retry
        Thread.sleep(500);
      }
    }

    throw new ExecutionException("Could not connect to SwingExplorer", ex);
  }

  /**
   * Navigates to a location in the given class.
   *
   * @param pProject    Project to search in
   * @param pClass      FQN of a class to search in
   * @param pLineNumber line number to scroll to (1-based)
   */
  private void navigate(@NotNull Project pProject, @NotNull String pClass, int pLineNumber) {
    ReadAction.nonBlocking(() -> {
          PsiClass psiClass = JavaPsiFacade.getInstance(pProject).findClass(pClass, GlobalSearchScope.allScope(pProject));
          PsiFile psiFile = psiClass == null ? null : psiClass.getContainingFile();
          return psiFile == null ? null : psiFile.getVirtualFile();
        })
        .inSmartMode(pProject)
        .expireWhen(pProject::isDisposed)
        .finishOnUiThread(ModalityState.nonModal(), virtualFile -> {
          if (virtualFile == null) {
            NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_ID_WINDOW)
                .createNotification("Swing Explorer", "Class not found: " + pClass, NotificationType.ERROR).notify(pProject);
            return;
          }

          OpenFileDescriptor descriptor = new OpenFileDescriptor(pProject, virtualFile, Math.max(pLineNumber - 1, 0), 0);
          if (descriptor.canNavigate()) {
            descriptor.navigate(true);
            ProjectUtil.focusProjectWindow(pProject, true);
          }
        })
        .submit(AppExecutorUtil.getAppExecutorService());
  }
}