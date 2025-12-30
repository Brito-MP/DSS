import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import model.InterRestauranteL;
import model.RestauranteFacade;
import model.temporario.Restaurant;

public class App {

    private InterRestauranteL model;
    private Scanner scanner;

    public static void main(String[] args) {
        App app = new App();
        //app.run();
    }

    public App() {
        this.model = new RestauranteFacade();

        this.model.registaItem("BigMac", 20, "BigMac", 300);
        this.model.registaItem("batataFrita", 4, "Batatas Fritas", 100);
        this.model.registaItem("Coca-Cola", 1.5, "Coca-Cola", 300);

        this.model.registaItem("McChicken", 7, "McChicken", 200);
        this.model.registaItem("Nuggets", 6, "Nuggets", 80);
        this.model.registaItem("Sumol", 1, "Sumol", 1);



        long pedidoId = this.model.registaPedido(
            Arrays.asList("BigMac", "batataFrita", "Coca-Cola"),
            "batataSemSal",
            true
        );

        long pedidoId1 = this.model.registaPedido(
            Arrays.asList("McChicken", "Nuggets", "Sumol"),
            "carneExtra", 
            false
        );

        System.out.println("Pedido registado com ID: " + pedidoId);
        System.out.println("Pedido registado com ID: " + pedidoId1);


        this.model.validaPagamento(pedidoId);
        this.model.validaPagamento(pedidoId1);


        /*
         * try {
         * model = Restaurant.readObj("Restaurante.obj");
         * } catch (IOException | ClassNotFoundException e) {
         * System.out.println("Oops! Não consegui ler! " + e.getMessage());
         * model = new Restaurant(); // Cria uma nova instância de Restaurante
         * model.initializeDefaultData(); // Inicializa os dados padrão
         * try {
         * model.save("Restaurante.obj"); // Save the newly created object
         * } catch (IOException saveException) {
         * System.out.println("Oops! Não consegui gravar! " +
         * saveException.getMessage());
         * }
         * }
         * scanner = new Scanner(System.in);
         */
    }
/* 
    private void run() {
        NewMenu menu = new NewMenu(new String[] {
                "Data 🔐",
        });
        menu.setHandler(1, this::showData);
        // menu.setHandler(2, this::createUser);

        // System.out.println(model.getMusics());
        menu.run();
        try {
            model.save("Restaurante.obj");
        } catch (IOException e) {
            System.out.println("Oops! Não consegui gravar! " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private void showData() {
        System.out.println("\n");
        System.out.println("Foods:");
        if (model.getFoods().isEmpty()) {
            System.out.println(" (empty)");
        } else {
            model.getFoods().forEach((name, food) -> System.out.println(food.toString()));
        }

        System.out.println("\nEmployees:");
        if (model.getEmployees().isEmpty()) {
            System.out.println(" (empty)");
        } else {
            model.getEmployees().forEach((id, emp) -> System.out.println(emp.toString()));
        }

        System.out.println("\nOrders:");
        if (model.getOrders().isEmpty()) {
            System.out.println(" (empty)");
        } else {
            model.getOrders().forEach((id, ord) -> System.out.println(ord.toString()));
        }

        System.out.println("\nClients:");
        if (model.getClients().isEmpty()) {
            System.out.println(" (empty)");
        } else {
            model.getClients().forEach((id, client) -> System.out.println(" - " + id + " -> " + client.toString()));
        }
    } */
}
