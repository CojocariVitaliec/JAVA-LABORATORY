
package Server_Lab4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server_Lab4 {

  static int PORT, USERS;

  public Server_Lab4(int port, int users) throws IOException {
    PORT = port;    
    USERS = users;
    Socket[] mysocks = new Socket[USERS];
    String[] names = new String[USERS];
    Socket temp_sock = null;
    int count = 0;
    for (int i = 0; i < USERS; i++) {
      mysocks[i] = null;
      names[i] = null;
    }
    ServerSocket serverSocket = null;
    boolean listening = true;
    System.out.println("Încercați să vă conectați la server...");
    try {
      serverSocket = new ServerSocket(PORT);
      System.out.println("Serverul este conectat!");
    } catch (IOException e) {
      System.err.println("Server nu răspuns la portul: " + PORT);
      System.exit(-1);
    }
    
    while (listening) {
      if (count < USERS && ((temp_sock = serverSocket.accept()) != null)) {
        for (int i = 0; i < USERS; i++) {
          if (mysocks[i] == null) {
            count = i;
            mysocks[i] = temp_sock;
            temp_sock = null;
            new ServerThread(mysocks[count], mysocks, count, names);
            break;
          }
        }
        count = 0;        
        for (int i = 0; i < USERS; i++) {
          if (mysocks[i] != null) {
            count++;
          }
        }
        System.out.println("Pe server este " + count + ".");        
      } else {
        System.out.println("Serverul este plin");
      }      
    }
    serverSocket.close();
  }

  public static void main(String[] args) throws IOException {
    new Server_Lab4(8888, 10);
  }  
}

class ServerThread extends Thread {
  
  String names[] = null;
  String name = null;
  public int own_num;
  private Socket socket = null;
  private Socket[] mys = null;
  PrintWriter outAll = null;
  
  public ServerThread(Socket socket, Socket[] my, int num, String names[]) {
    super("ServerThread");
    this.socket = socket;
    this.mys = my;
    own_num = num;
    this.names = names;
    this.start();
  }

  @Override
  public void run() {
    try {
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
      BufferedReader in = new BufferedReader(
              new InputStreamReader(
              socket.getInputStream()));
      String inputLine, outputLine;
      while ((inputLine = in.readLine()) != null) {
        if (inputLine.equalsIgnoreCase("2112333")) {          
          for (int i = 0; i < Server_Lab4.USERS; i++) {
            if (names[i] != null) {
              if (name.compareTo(names[i]) == 0) {
                names[i] = null;
                break;
              }
            }
          }
          for (int i = 0; i < Server_Lab4.USERS; i++) {
            if (mys[i] != null) {              
              outAll = new PrintWriter(mys[i].getOutputStream(), true);
              outAll.println("^#");              
              for (int j = 0; j < Server_Lab4.USERS; j++) {
                if (names[j] != null) {
                  outAll.println(names[j]);
                }
              }
              outAll = null;
            }
          }
          break;          
        }
        if (inputLine.charAt(0) == '^') {
          for (int i = 0; i < Server_Lab4.USERS; i++) {
            if (names[i] == null) {
              name = inputLine;
              names[i] = inputLine;
              break;
            }
          }
          for (int i = 0; i < Server_Lab4.USERS; i++) {
            if (mys[i] != null) {              
              outAll = new PrintWriter(mys[i].getOutputStream(), true);
              outAll.println("^#");              
              for (int j = 0; j < Server_Lab4.USERS; j++) {
                if (names[j] != null) {
                  outAll.println(names[j]);
                }
              }
              outAll = null;
            }
          }
        } else {
          for (int i = 0; i < Server_Lab4.USERS; i++) {
            if (mys[i] != null) {              
              outAll = new PrintWriter(mys[i].getOutputStream(), true);
              outAll.println(inputLine);              
              outAll = null;
            }
          }
        }
      }      
      socket.close();
      mys[own_num] = null;      
      out.close();
      in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }  
}
