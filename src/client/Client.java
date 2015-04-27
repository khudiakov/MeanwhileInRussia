package client;

import java.net.*;
import java.io.*;

public class Client {
    Socket clientSocket = null;
    BufferedReader in;
    PrintWriter out;

    boolean Client(String host, int port) {
        try {
            clientSocket = new Socket(host, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    void send(String message) {
        out.println(message);
    }

    String read() {
        try {
            String msg = in.readLine();
            System.out.println("RECEIVE:"+msg);
            return msg;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    boolean ready(){
        try {
            return in.ready();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    void close() {
        try {
            out.close();
            in.close();
            clientSocket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}