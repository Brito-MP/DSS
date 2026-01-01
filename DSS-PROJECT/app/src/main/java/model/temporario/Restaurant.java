package model.temporario;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Restaurant implements Serializable {
   // private Map <String, Food> foods;
    private Map <Integer, Employee> employees;
    private Map <Integer, Order> orders;
    private Map <Integer, Client> clients;
    
    

    public Restaurant(){
        //this.foods = new LinkedHashMap<>();
        this.employees = new LinkedHashMap<>();
        this.orders = new LinkedHashMap<>();
        this.clients = new LinkedHashMap<>();
    }


 public void save(String nomef) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nomef));

        oos.writeObject(this);
        oos.writeObject(Employee.getIdCounter());
        oos.writeObject(Order.getIdCounter());
        oos.writeObject(Client.getIdCounter());

        oos.close();
    }


 public static Restaurant readObj(String nomef) throws IOException, ClassNotFoundException{
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nomef));
        Restaurant r = (Restaurant) ois.readObject();

        // Atualizar os contadores
        Integer idCounterEmployee = (Integer) ois.readObject();
        Employee.setIdCounter(idCounterEmployee);

        Integer idCounterOrder = (Integer) ois.readObject();
        Order.setIdCounter(idCounterOrder);

        Integer idCounterClient = (Integer) ois.readObject();
        Client.setIdCounter(idCounterClient);

        ois.close();

        return r;
    }


    public void initializeDefaultData() {
    
        //Food alface = new Food("alface",10);
       // this.foods.put(alface.getName(),alface);

        Employee Gonçalo = new Employee("Gonçalo", WorkstationType.MOTOBOY);
        this.employees.put(Gonçalo.getId(), Gonçalo);

        Order order1 = new Order();
        this.orders.put(order1.getId(),order1);

        Client client1 = new Client();
        this.clients.put(client1.getId(),client1);
    }

    //public Map<String,Food> getFoods(){return foods;}
    public Map<Integer, Employee> getEmployees() { return employees; }
    public Map<Integer, Order> getOrders() { return orders; }
    public Map<Integer, Client> getClients() { return clients; }
}
