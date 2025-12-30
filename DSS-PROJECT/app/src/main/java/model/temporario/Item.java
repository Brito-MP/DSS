package model.temporario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Item implements Serializable {
    private List<Anotation> anotations;
    private List<Food> foods;
    private double price;

    public Item (){
        this.anotations = new ArrayList<>();
        this.foods = new ArrayList<>();
        this.price = 0;
    }

    public Item (List<Anotation> anotations, List<Food> foods, double price){
        this.anotations = anotations;
        this.foods = foods;
        this.price = price;
    }
}
