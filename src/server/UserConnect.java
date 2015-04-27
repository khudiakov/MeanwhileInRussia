package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gangbang on 10.05.2014.
 */
public class UserConnect implements Runnable {
    static List<String> maps = new ArrayList<String>();
    static List<Game> games = new ArrayList<Game>();
    BufferedReader in;
    PrintWriter out;
    Socket client;


    public UserConnect(Socket newClient){
        try {
            client = newClient;
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (Exception e){
            System.out.println("Error "+e.getMessage());
        }
    }


    public static Game findGame(int id) {
        for (Game game: games) {
            if (game.id == id) {
                return game;
            }
        }
        return null;
    }


    static public void DeleteGame(int ID) {
        games.remove(findGame(ID));
    }


    public void sendMapsAndGames() {
        String output ="";
        for (String name : maps) {
            output += name + " ";
        }
        output+="\n";
        for (Game game : games) {
            output += game.id+ ":" + game.map + ":" + game.speed + ":" + game.port+" ";
        }
        System.out.println(output);
        out.println(output);
    }


    public String readInput() {
        String input="";
        try {
            input = in.readLine();
        }
        catch (Exception e) {
            System.out.println("Error "+e.getMessage());
        }
        return input;
    }


    public ServerSocket createSocket() {
        ServerSocket gameServer = null;
        try {
            gameServer = new ServerSocket(0);
        }
        catch (Exception e) {
            System.out.println("Error "+e.getMessage());
        }
        return gameServer;
    }


    @Override
    public void run() {
        sendMapsAndGames();
        String input = readInput();
//        Pattern newGamePattern = Pattern.compile("new ([\\d\\w]+) ([\\d]+\\.?[\\d]*)");
//        Pattern joinGamePattern = Pattern.compile("join (\\d+)");
//        Matcher m;
        System.out.println(input);
        if (input.startsWith("new")) {
//            m = newGamePattern.matcher(input);
//            m.matches();
            String[] m = input.split(" ");
            String mapName = m[1];
            float speed = Float.valueOf(m[2]);
            ServerSocket gameServer = createSocket();
            Labyrinth newLab = new Labyrinth(gameServer, speed, mapName);
            Thread createNewGame = new Thread(newLab);
            createNewGame.start();
            Game newGame = new Game(gameServer, newLab, mapName, speed);
            games.add(newGame);
            newLab.labID=newGame.id;
            System.out.println("Send:" + newGame.port);
            out.println(newGame.port);
            newGame.gameLab.newPlayer();
        }
        else if (input.startsWith("join")) {
//            m = joinGamePattern.matcher(input);
//            m.matches();
            String[] m = input.split(" ");
            int gameID = Integer.parseInt(m[1]);
            Game existGame = findGame(gameID);
            existGame.gameLab.newPlayer();
        }
        out.close();
    }
}
