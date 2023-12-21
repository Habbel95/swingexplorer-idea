package com.github.dhaberlah.swingexplorer;

import javax.swing.*;

/**
 * @author w.glanzer, 12.04.2020
 */
public class DummyFrame {

  public static void main(String[] args) {
    JFrame frame = new JFrame("dummy");
    frame.setBounds(100, 100, 800, 600);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    JButton button = new JButton("Test Click");
    button.addActionListener((e) -> {
      //Test for Breakpoint
      System.out.println(e.getActionCommand());
    });
    frame.add(button);
    SwingUtilities.invokeLater(() -> frame.setVisible(true));
  }

}
