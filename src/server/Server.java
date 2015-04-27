package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class Server {
    public static void main(String[] args) {
        try{
            findAllMaps();
            ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
            Socket client;
            while (true) {
                client = server.accept();
                UserConnect newUser = new UserConnect(client);
                Thread createNewUser = new Thread(newUser);
                createNewUser.start();
            }
        } catch(Exception e){
            System.out.println("Error "+e.getMessage());
        }
    }


    static public void findAllMaps() {
        File[] files;
        String folder = "examples/";
        File path = new File(folder);
        files = path.listFiles();
//        Pattern pattern = Pattern.compile("(?:\\w+//)+(\\w+).lab");
        for (int i=0; i<files.length; i++) {
            System.out.println(files[i].toString());
            String name = files[i].toString().substring(folder.length());
            System.out.println(name);
            UserConnect.maps.add(name);

//            Matcher m = pattern.matcher(files[i].toString());
//            if (m.matches()) {
//                System.out.println(m.group(1));
//                UserConnect.maps.add(m.group(1));
//            }
        }

    }
}