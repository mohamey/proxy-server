import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


public class VisualConsole{
  private JFrame mainFrame;
  private JTextArea textOutput;
  private JScrollPane outputPane;
  private JTextField input;
  private Container pane;
  private VisualBackend handler;

  void VisualConsole(){
    prepareConsoleWindow();
  }

  //Prepare management console
  private void prepareConsoleWindow(){
    mainFrame = new JFrame("Proxy Server Console");
    mainFrame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent windowEvent){
        int confirmed = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the proxy server?", "Exit Proxy Server", JOptionPane.YES_NO_OPTION);
        if(confirmed == JOptionPane.YES_OPTION){
          mainFrame.dispose();
        }
        System.exit(0);
      }
    });

    //Create a backend Handler
    handler = new VisualBackend();

    //Begin graphing the UI
    pane = mainFrame.getContentPane();
    pane.setLayout(new GridBagLayout());
    pane.setSize(400, 400);
    GridBagConstraints constraints = new GridBagConstraints();

    textOutput = new JTextArea("");
    outputPane = new JScrollPane(textOutput);
    input = new JTextField("");

    //Add event handler to allow commands to be input
    input.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        handleInput(input.getText());
      }
    });

    //Specify parameters to place UI widgets
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = 0;
    constraints.ipadx = 400;
    constraints.ipady = 350;
    constraints.weightx = 1;
    constraints.gridy = 0;
    pane.add(outputPane, constraints);

    constraints.gridy = 1;
    constraints.ipady = 20;
    pane.add(input, constraints);
    pane.setSize(400, 400);
    mainFrame.pack();
    mainFrame.setVisible(true);

  }

  //Handle input from the user in the management console
  private void handleInput(String command){
    String response = handler.parseInput(command);
    textOutput.append(response);
    input.setText("");
  }

  public static void main(String [] args){
    VisualConsole temp = new VisualConsole();
    temp.prepareConsoleWindow();
  }
}
