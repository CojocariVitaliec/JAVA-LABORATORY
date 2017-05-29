package client_lab4;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

class Interface extends Frame {

  Dialog form = null;
  TextArea wnd_msg = null, wnd_names = null;
  Button b_send = null, b_transfer = null, b_view = null;
  TextField to_send = null;

  public Interface(String window_name) {
    super(window_name);
    setFont(new Font("Comic", Font.BOLD, 12));
    setBackground(Color.black);

    wnd_msg = new TextArea("", 14, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
    wnd_msg.setEditable(false);
    wnd_names = new TextArea("", 14, 10, TextArea.SCROLLBARS_VERTICAL_ONLY);
    wnd_names.setEditable(false);

    to_send = new TextField(50);
    b_view = new Button("Vizualizare jurnal");
    b_transfer = new Button("Încarcă");
    b_send = new Button("Trimite");

    Panel for_sends = new Panel();
    Panel for_wnds = new Panel();

    for_sends.add(b_view);
    for_sends.add(b_transfer);
    for_sends.add(to_send);
    for_sends.add(b_send);

    for_wnds.add(wnd_names);
    for_wnds.add(wnd_msg);

    add(for_sends, BorderLayout.SOUTH);
    add(for_wnds, BorderLayout.CENTER);
    setSize(650, 350);
    setLocationCentre(this);
    setResizable(false);
    setVisible(true);

    b_transfer.addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File("."));
                chooser.setFileFilter(
                        new javax.swing.filechooser.FileFilter() {

                          @Override
                          public boolean accept(File f) {
                            return f.getName().toLowerCase().endsWith(".txt")
                                    || f.isDirectory();
                          }

                          @Override
                          public String getDescription() {
                            return "TXT files";
                          }
                        });
                int request = chooser.showOpenDialog(new JFrame());
                if (request == JFileChooser.APPROVE_OPTION) {
                  System.out.println("File load: " + chooser.getSelectedFile().getAbsolutePath());
                  try {
                    new TransferData(chooser.getSelectedFile(), Form.file);
                  } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                  }
                }
              }
            });
    b_view.addActionListener(
            new ActionListener() {

              @Override
              public void actionPerformed(ActionEvent arg0) {
                try {
                  new ViewData(Form.name[0]);
                } catch (IOException e) {
                }
              }
            });
  }

  private static void setLocationCentre(Interface f) {
    Dimension us = f.getSize(), them = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation((them.width - us.width) / 2, (them.height - us.height) / 2);
  }
}

class ViewData extends JFrame {

  TextArea data;

  public ViewData(String nameUser) throws IOException {
    super("Log file for User: " + nameUser);
    File file = new File(Form.dir + "/" + nameUser + ".log");
    BufferedReader buf = new BufferedReader(new FileReader(file));

    data = new TextArea();
    data.setEditable(false);
    add(data, BorderLayout.CENTER);

    String line;
    while ((line = buf.readLine()) != null) {
      data.appendText(line + "\n");
    }
    buf.close();

    this.setSize(400, 400);
    this.setLocationCentre(this);
    this.setResizable(false);
    setVisible(true);
  }

  private static void setLocationCentre(ViewData f) {
    Dimension us = f.getSize(), them = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation((them.width - us.width) / 2, (them.height - us.height) / 2);
  }
}

class TransferData extends Thread {

  DateFormat date;
  BufferedReader input;
  RandomAccessFile acces;

  public TransferData(File sourse, File destination) throws IOException {
    date = DateFormat.getDateTimeInstance();
    input = new BufferedReader(new FileReader(sourse));

    acces = new RandomAccessFile(destination, "rw");
    acces.seek(destination.length());
    String line = null;
    acces.writeBytes("[" + date.format(new Date()) + "]:\n");
    while ((line = input.readLine()) != null) {
      acces.writeBytes(line + "\n");
    }
    acces.close();
    input.close();
    start();
  }
}

class Form extends Dialog implements ActionListener {

  public TextField for_host = null, for_name = null;
  public Button begin = null;
  public String[] host = null;
  static public String[] name = null;
  Label l_host = new Label("Host:   ");
  Label l_name = new Label("Nume:");
  static File dir, file;

  public Form(Interface chat, String[] host, String[] name) {
    super(chat, "Înregistrare:", true);
    setFont(new Font("Comic", Font.BOLD, 12));
    setBackground(Color.black);

    this.host = host;
    this.name = name;
    for_host = new TextField(10);
    for_name = new TextField(10);

    Panel for_texts = new Panel();
    for_texts.add(l_host);
    for_texts.add(for_host);
    for_texts.add(l_name);
    for_texts.add(for_name);

    begin = new Button("Start");
    begin.addActionListener(this);

    Panel for_button = new Panel();
    for_button.add(begin);

    add(for_texts, BorderLayout.CENTER);
    add(for_button, BorderLayout.SOUTH);
    setSize(180, 125);

    setLocationCentre(this);
    setResizable(false);
    setVisible(true);
  }

  private static void setLocationCentre(Form f) {
    Dimension us = f.getSize(), them = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation((them.width - us.width) / 2, (them.height - us.height) / 2);
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    host[0] = for_host.getText();
    name[0] = for_name.getText();

    dir = new File("SrvLogs");
    if (dir.exists()) {
      System.out.println("Directoriul: " + dir.getAbsolutePath() + " exista");
      try {
        System.out.println("Control la fisier cu logs");
        file = new File(dir + "/" + name[0] + ".log");
        if (file.exists()) {
          System.out.println("Fisierul: " + file.getName() + " exista");
          dispose();
        } else if (file.createNewFile()) {
          System.out.println("Pe server s-a creat fisierul: " + name[0]);
          dispose();
        }
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    } else if (dir.mkdir()) {
      System.out.println("Directoriul: " + dir.getAbsolutePath() + " s-a creat");
      try {
        System.out.println("Control la fisier cu logs");
        file = new File(dir + "/" + name[0] + ".log");
        if (file.exists()) {
          System.out.println("Fisierul: " + file.getName() + " exista");
          dispose();
        } else if (file.createNewFile()) {
          System.out.println("Pe server s-a creat fisierul: " + name[0]);
          dispose();
        }
      } catch (IOException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}

class Read implements ActionListener {

// Transmiterea mesajului
  Interface wnd;
  String name = "not :";
  Socket ss;
  PrintWriter out = null;
  String fromUser = "";
  DateFormat date;

  public Read(Socket s, Interface w) {
    wnd = w;
    ss = s;
    try {
      out = new PrintWriter(ss.getOutputStream(), true);
    } catch (IOException io) {
    }
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    date = DateFormat.getTimeInstance();
    fromUser = wnd.to_send.getText();
    wnd.to_send.setText("");
    out.println("[" + date.format(new Date()) + "] " + name + fromUser);
    fromUser = "";
  }

  public void setName(String name) {
    this.name = name + " : ";
  }
}

public class Client_Lab4 {

  public static void main(String[] args) throws IOException {
    String[] name = new String[1], host = new String[1];
    Interface wnd = new Interface("Chat");
    new Form(wnd, host, name);
    Read reader = null;
    Socket kkSocket = null;
    BufferedReader in = null;
    try {
      kkSocket = new Socket(host[0], 8888);
      in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
    } catch (UnknownHostException e) {
      System.err.println("Don't know about host.");
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection.");
      System.exit(1);
    }
    String fromServer;
    reader = new Read(kkSocket, wnd);
    reader.setName(name[0]);
    wnd.setTitle(name[0]);
    final PrintWriter out = reader.out;
    // mesaje de sistem
    out.println("^" + name[0]);
    wnd.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent we) {
        out.println("2112333");
        System.exit(0);
      }
    });
    wnd.b_send.addActionListener(reader);
    wnd.to_send.addActionListener(reader);
    while (((fromServer = in.readLine()) != null) && (reader != null)) {
      if ((fromServer.charAt(0) == '^') && (fromServer.charAt(1) == '#')) {
        wnd.wnd_names.setText("");
        continue;
      }
      if (fromServer.charAt(0) == '^') {
        wnd.wnd_names.append(fromServer.substring(1) + '\n');
      } else {
        wnd.wnd_msg.append(fromServer + "\n");
      }
    }
    in.close();
    kkSocket.close();
  }
}
