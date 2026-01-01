
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import model.InterRestauranteL;
import model.RestauranteFacade;

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
                    Arrays.asList("menumcchicken"),
                    "ketchupExtra",
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
                boolean trocaRealizada = this.model.registaTroca("BigMac", "carne_frango", "carne_vaca");
                if (trocaRealizada) {
                    System.out.println("   Troca realizada com sucesso!\n");

                } else {
                    System.out.println("   Troca não foi possível realizar.\n");
                }
            } catch (Exception e) {
                System.out.println("   Erro ao tentar troca: " + e.getMessage() + "\n");
            }

            // Testes de preparação
            System.out.println(" Testando preparação...");
            List<Long> fila = new ArrayList<>();
            fila.add(pedidoId1);
            fila.add(pedidoId2);

            // Requisitar ingredientes para o primeiro pedido no postoA
            this.model.requisitarIngredientes(pedidoId1, "postoA");
            System.out.println("   Ingredientes requisitados para pedido " + pedidoId1 + " em postoA");

            // Atrasar o primeiro pedido e reordenar a fila
            this.model.atrasarPedido(pedidoId1, 500.0);
            this.model.atualizaFilaPedidos(pedidoId1, fila);
            System.out.println("   Fila após atraso e reordenação: " + fila);

            // Encerrar os pedidos e removê-los da fila
            this.model.encerrarPedido(pedidoId1, "postoA");
            this.model.removerPedidoFila(pedidoId1, fila);
            System.out.println("   Pedido " + pedidoId1 + " encerrado e removido da fila: " + fila);

            this.model.encerrarPedido(pedidoId2, "postoA");
            this.model.removerPedidoFila(pedidoId2, fila);
            System.out.println("   Pedido " + pedidoId2 + " encerrado e removido da fila: " + fila + "\n");

            System.out.println("========== FIM DO TESTE DAO ==========\n");

        } catch (Exception e) {
            System.out.println("Erro durante o teste: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
