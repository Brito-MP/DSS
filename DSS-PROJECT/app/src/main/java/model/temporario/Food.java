package model.temporario;

import java.io.Serializable;

public class Food implements Serializable{
    private String nome;
    private int quantity;

    public Food (String nome, int quantity){
        this.nome = nome;
        this.quantity = quantity;
    }

    public String getName (){
        return this.nome;
    }

    public int getQuantity(){
        return this.quantity;
    }

    @Override
    public String toString() {
        return "food: " + getName() + "  ->" +  " quantity:" + getQuantity();
    }
}
