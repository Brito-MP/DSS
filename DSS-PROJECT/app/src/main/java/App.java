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
        app.testaDAO();
        // app.run();
    }

    public App() {
        this.model = new RestauranteFacade();


    }

    private void testaDAO() {
        System.out.println("========== TESTE DAO ==========\n");

        try {

            // Criar primeiro pedido
            System.out.println(" Criando primeiro pedido...");
            long pedidoId1 = this.model.registaPedido(
                    Arrays.asList("BigMac", "batataFrita", "coca_cola"),
                    "batataSemSal",
                    true);
            System.out.println("   Pedido 1 criado com ID: " + pedidoId1 + "\n");

            // Criar segundo pedido
            System.out.println(" Criando segundo pedido...");
            long pedidoId2 = this.model.registaPedido(
                    Arrays.asList("BigMac", "coca_cola"),
                    "semCebola",
                    false);
            System.out.println("   Pedido 2 criado com ID: " + pedidoId2 + "\n");

            // Validar pagamentos
            System.out.println(" Validando pagamentos...");
            this.model.validaPagamento(pedidoId1);
            this.model.validaPagamento(pedidoId2);
            System.out.println("   Pagamentos validados!\n");

            // Testar trocas
            System.out.println(" Testando trocas de alimentos...");
            try {
                boolean trocaRealizada = this.model.registaTroca("BigMac", "carne_vaca", "carne_frango");
                if (trocaRealizada) {
                    System.out.println("   Troca realizada com sucesso!\n");
                } else {
                    System.out.println("   Troca não foi possível realizar.\n");
                }
            } catch (Exception e) {
                System.out.println("   Erro ao tentar troca: " + e.getMessage() + "\n");
            }

            System.out.println("========== FIM DO TESTE DAO ==========\n");

        } catch (Exception e) {
            System.out.println("Erro durante o teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
