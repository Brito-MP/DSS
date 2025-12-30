package model.temporario;

import java.io.Serializable;

public class Client implements Serializable {
    private int id;
    private static int idCounter = 0;
    private int points = 0;

    public Client(){
        this.id = idCounter++;
        this.points = 0;
    }  

    public Client(int points){
        this.points = points;
    }

    public static int getIdCounter(){
        return Client.idCounter;
    }

    public static void setIdCounter(int idCounter){
        Client.idCounter = idCounter;
    }

    public int getId(){
        return this.id;
    }

    @Override
    public String toString() {
        return "Client{id=" + getId() + "}";
    }
}
