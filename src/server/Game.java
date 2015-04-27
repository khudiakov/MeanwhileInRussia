package server;

import java.net.ServerSocket;

/**
 * Created by gangbang on 10.05.2014.
 */
public class Game {
    static int gameCount=0;
    int id;
    int port = 0;
    float speed = 0;
    String map;
    ServerSocket gameSocket;
    Labyrinth gameLab;
    public Game(ServerSocket socket, Labyrinth lab, String map, float speed){
        id=gameCount;
        gameCount++;
        gameLab = lab;
        gameSocket=socket;
        this.port=socket.getLocalPort();
        this.map=map;
        this.speed=speed;
    }
}
