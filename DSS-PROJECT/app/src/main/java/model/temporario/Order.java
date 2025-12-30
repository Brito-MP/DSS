package model.temporario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.pedidos.Produto;

public class Order implements Serializable{
    private List<Produto> products;
    private OrderType orderType;
    private Invoice invoice;
    private int id;
    private static int idCounter = 0;


    public Order (){
        this. products = new ArrayList<>();
        this. orderType = OrderType.IN_RESTAURANT;
        this. invoice = new Invoice();
        this.id = idCounter++;
    }

     public static int getIdCounter(){
        return Order.idCounter;
    }

    public static void setIdCounter(int idCounter){
        Order.idCounter = idCounter;
    }

    public int getId(){
        return this.id;
    }

   @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(getId())
          .append("\n -> orderType=").append(orderType)
          .append("\n -> invoice=").append(invoice)
          .append("\n -> products=");
        if (products == null || products.isEmpty()) {
            sb.append("(empty)");
        } else {
            sb.append("[");
            for (int i = 0; i < products.size(); i++) {
                sb.append(products.get(i));
                if (i < products.size() - 1) sb.append(", ");
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}

