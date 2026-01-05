
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import model.InterRestauranteL;
import model.RestauranteFacade;
import model.gestao.Alimento;
import model.pedidos.Item;
import model.pedidos.Pedido;
import model.pedidos.PedidoException;
import model.pedidos.Produto;

public class App {

    // ========== ATRIBUTOS ==========
    private InterRestauranteL model;
    private Scanner scanner;
    private String[] menus = {"menubigmac", "menumcchicken"};
    private String[] itens = {"BigMac", "mcchicken", "batataFrita", "coca_cola", "sumol"};
    private List<String> produtos_escolhidos = new ArrayList<>();
    private String nota = "";
    private long idPedido = -1;

    // ========== MAIN E CONSTRUTOR ==========
    public static void main(String[] args) {
        // PedidoDAO.getInstance().clear(); // LIMPAR A BASE DE DADOS

        App app = new App();
        app.run();
    }

    public App() {
        this.model = new RestauranteFacade();
        this.scanner = new Scanner(System.in);
    }

    // ========== MENU PRINCIPAL ==========
    private void run() {
        NewMenu menu = new NewMenu(new String[]{
            "TestaDAO",
            "COO",
            "Funcionario",
            "Cliente"
        });

        menu.setHandler(1, this::testaDAO);
        menu.setHandler(2, this::menuCOO);
        menu.setHandler(3, this::menuFuncionario);
        menu.setHandler(4, this::menuCliente);

        menu.run();
        scanner.close();
        System.exit(0);
    }

    // ========== MENUS DE UTILIZADOR ==========
    private void menuCOO() {
        NewMenu menu = new NewMenu(new String[] {
                "Ver Tempo Médio de Confecção",
                "Ver Stock de Alimentos"
        });

        menu.setHandler(1, () -> {
            double tempoMedio = model.apresentaTempoConfecao();
            System.out.printf("Tempo médio de confecção: %.2f minutos\n",tempoMedio);
        });
        menu.setHandler(2, () -> {
            Map<String, Integer> stock = model.apresentaStock();
            System.out.println("Stock de Alimentos:\n");
            for(Map.Entry<String, Integer> entry : stock.entrySet()){
                System.out.println("Alimento ID: " + entry.getKey() + " | Quantidade: " + entry.getValue());
            }
        });

        menu.run();
    }

    // ========== MENUS DE PEDIDO ==========
    private void menuFuncionario() {
        NewMenu menu = new NewMenu(new String[]{
            "Caixa",
            "Cozinha"
        });
        menu.setHandler(1, () -> {
            caixa();
        });
        menu.setHandler(2, () -> {
            cozinha();
        });
        /*menu.setHandler(3, () -> {
            embalador_empratador();
        }); */
        menu.runOnce();
    }

    private void caixa() {
        List<Pedido> pendentes = model.getPedidosPorPagar();

        String[] opcoes = new String[pendentes.size()];
        for (int i = 0; i < pendentes.size(); i++) {
            Pedido pedido = pendentes.get(i);
            String tipo = pedido.getTipo() ? "Restaurante" : "TakeAway";
            opcoes[i] = "Pedido #" + pedido.getIdCounter() + " | " + tipo + " | €"
                    + String.format("%.2f", pedido.getPreco());
        }

        NewMenu menu = new NewMenu(opcoes);

        for (int i = 0; i < pendentes.size(); i++) {
            final int index = i;
            menu.setHandler(i + 1, () -> {
                Pedido pedidoSelecionado = pendentes.get(index);
                model.validaPagamento(pedidoSelecionado.getIdCounter());
                System.out.println("✓ Pagamento validado para o pedido " + pedidoSelecionado.getIdCounter() + ".");
            });
        }

        menu.runOnce();
    }

    private void cozinha() {

        NewMenu menu = new NewMenu(new String[]{
            "Pedidos em preparação",
            "Requisitar ingredientes",
            "Atrasar pedido e atualizar fila",
            "Encerrar pedido"
            });

        menu.setHandler(1, () -> displayPedidosEmPreparacao());
        menu.setHandler(2, () -> requisitarIngredientes());
        menu.setHandler(3, () -> atrasarPedidoEAtualizarFila());
        menu.setHandler(4, () -> encerrarPedido());

        menu.runOnce();
    }

    private void menuCliente() {
        boolean[] pagamentoConcluido = {false};

        while (!pagamentoConcluido[0]) {
            NewMenu menu = new NewMenu(new String[]{
                "Take Away",
                "No Restaurante"
            });
            menu.setHandler(1, () -> {
                boolean pagou = takeAway();
                if (pagou) {
                    pagamentoConcluido[0] = true;
                }
            });
            menu.setHandler(2, () -> {
                boolean pagou = restaurante();
                if (pagou) {
                    pagamentoConcluido[0] = true;
                }
            });

            menu.runOnce();
        }
    }

    // ========== MENUS DE PEDIDO ==========
    private boolean takeAway() {
        boolean[] concluido = {false};
        boolean[] pagou = {false};

        while (!concluido[0]) {
            NewMenu menu = new NewMenu(new String[]{
                "Menus",
                "Itens",
                "Pedido",
                "Adicionar nota",
                "Registar Pedido"
            });

            menu.setHandler(1, () -> displayMenus());
            menu.setHandler(2, () -> displayItens());
            menu.setHandler(3, () -> displayPedido());
            menu.setHandler(4, () -> {
                nota = adicionaNota();
            });
            menu.setHandler(5, () -> {
                idPedido = model.registaPedido(produtos_escolhidos, nota, false);
                boolean pagamento = menuTrocasItem(idPedido);
                if (pagamento) {
                    pagou[0] = true;
                    concluido[0] = true;
                }
            });

            menu.runOnce();
        }
        return pagou[0];
    }

    private boolean restaurante() {
        boolean[] concluido = {false};
        boolean[] pagou = {false};

        while (!concluido[0]) {
            NewMenu menu = new NewMenu(new String[]{
                "Menus",
                "Itens",
                "Pedido",
                "Adicionar nota",
                "Registar Pedido"
            });

            menu.setHandler(1, () -> displayMenus());
            menu.setHandler(2, () -> displayItens());
            menu.setHandler(3, () -> displayPedido());
            menu.setHandler(4, () -> {
                nota = adicionaNota();
            });
            menu.setHandler(5, () -> {
                idPedido = model.registaPedido(produtos_escolhidos, nota, true);
                boolean pagamento = menuTrocasItem(idPedido);
                if (pagamento) {
                    pagou[0] = true;
                    concluido[0] = true;
                }
            });

            menu.runOnce();
        }
        return pagou[0];
    }

    // ========== DISPLAY DE MENUS E ITENS ==========
    private void displayMenus() {
        NewMenu menu = new NewMenu(new String[]{
            "Menu BigMac",
            "Menu McChicken"
        });

        menu.setHandler(1, () -> addEscolhidos(menus[0]));
        menu.setHandler(2, () -> addEscolhidos(menus[1]));

        menu.run();
    }

    private void displayItens() {
        NewMenu menu = new NewMenu(new String[]{
            "BigMac",
            "McChicken",
            "Batata Frita",
            "Coca Cola",
            "Sumol"
        });

        menu.setHandler(1, () -> addEscolhidos(itens[0]));
        menu.setHandler(2, () -> addEscolhidos(itens[1]));
        menu.setHandler(3, () -> addEscolhidos(itens[2]));
        menu.setHandler(4, () -> addEscolhidos(itens[3]));
        menu.setHandler(5, () -> addEscolhidos(itens[4]));

        menu.run();
    }

    // ========== MENUS DE PRODUTO ==========
    private void menuItem(String menuProduto) {
        NewMenu menu = new NewMenu(new String[]{
            "Adicionar ao Pedido",
            "Adicionar troca",
            "Adicionar Nota"
        });

        menu.setHandler(1, () -> addEscolhidos(menuProduto));
        menu.setHandler(2, () -> displayTrocasProduto());
        menu.setHandler(3, () -> {
            System.out.println("Adicionar nota - Em construção");
        });

        menu.run();
    }

    private boolean menuTrocasItem(long idPedido) {
        boolean[] pago = {false}; // Array para permitir modificação dentro da lambda

        while (!pago[0]) {
            NewMenu menu = new NewMenu(new String[]{
                "Realizar Troca",
                "Pagar",});

            menu.setHandler(1, () -> menuRealizarTroca(idPedido));
            menu.setHandler(2, () -> {
                model.validaPagamento(idPedido);
                System.out.println("✓ Pagamento realizado com sucesso!");
                pago[0] = true;
            });

            menu.runOnce();
        }

        return pago[0]; // Retorna true se pagou, false se saiu sem pagar
    }

    // ========== MENUS DE TROCA ==========
    private void menuRealizarTroca(long idPedido) {
        // Obter produtos do pedido via facade
        List<Produto> produtos = model.getProdutosPedido(idPedido);
        if (produtos.isEmpty()) {
            System.out.println("✗ Pedido não encontrado ou não tem produtos!");
            return;
        }

        // Criar array com nomes dos produtos
        String[] opcoesProdutos = new String[produtos.size()];
        for (int i = 0; i < produtos.size(); i++) {
            Produto p = produtos.get(i);
            opcoesProdutos[i] = p.getNome() + " (" + p.getId() + ")";
        }

        NewMenu menuProdutos = new NewMenu(opcoesProdutos);

        // Configurar handlers para cada produto
        for (int i = 0; i < produtos.size(); i++) {
            final int index = i;
            menuProdutos.setHandler(i + 1, () -> {
                Produto produtoEscolhido = produtos.get(index);
                if (produtoEscolhido instanceof Item) {
                    menuEscolherAlimento(idPedido, produtoEscolhido.getId());
                } else {
                    System.out.println("✗ Apenas itens individuais permitem trocas de alimentos!");
                }
            });
        }

        menuProdutos.runOnce();
    }

    private void menuEscolherAlimento(long idPedido, String idProduto) {
        try {
            // Obter alimentos do item via facade
            Map<String, Alimento> alimentos = model.getAlimentosItem(idPedido, idProduto);

            if (alimentos.isEmpty()) {
                System.out.println("✗ Este item não tem alimentos para trocar!");
                return;
            }

            // Criar array com nomes dos alimentos
            List<String> alimentosIds = new ArrayList<>(alimentos.keySet());
            String[] opcoesAlimentos = new String[alimentosIds.size()];

            for (int i = 0; i < alimentosIds.size(); i++) {
                String alimentoId = alimentosIds.get(i);
                Alimento alimento = alimentos.get(alimentoId);
                opcoesAlimentos[i] = alimento.getNome() + " (" + alimentoId + ")";
            }

            NewMenu menuAlimentos = new NewMenu(opcoesAlimentos);

            // Configurar handlers para cada alimento
            for (int i = 0; i < alimentosIds.size(); i++) {
                final int index = i;
                menuAlimentos.setHandler(i + 1, () -> {
                    String alimentoAtualId = alimentosIds.get(index);
                    menuEscolherSubstituto(idPedido, idProduto, alimentoAtualId);
                });
            }

            menuAlimentos.runOnce();
        } catch (PedidoException e) {
            System.out.println("✗ Erro: " + e.getMessage());
        }
    }

    private void menuEscolherSubstituto(long idPedido, String idProduto, String alimentoAtualId) {
        try {
            // Obter substitutos disponíveis via facade
            List<String> trocasDisponiveis = model.getSubstitutosDisponiveis(idPedido, idProduto, alimentoAtualId);

            if (trocasDisponiveis.isEmpty()) {
                System.out.println("✗ Não existem trocas disponíveis para este alimento!");
                return;
            }

            String[] opcoesSubstitutos = new String[trocasDisponiveis.size()];
            for (int i = 0; i < trocasDisponiveis.size(); i++) {
                String substitutoId = trocasDisponiveis.get(i);
                opcoesSubstitutos[i] = substitutoId; // Pode melhorar buscando nome da BD
            }

            NewMenu menuSubstitutos = new NewMenu(opcoesSubstitutos);

            // Configurar handlers para cada substituto
            for (int i = 0; i < trocasDisponiveis.size(); i++) {
                final int index = i;
                menuSubstitutos.setHandler(i + 1, () -> {
                    String alimentoDesejadoId = trocasDisponiveis.get(index);
                    try {
                        boolean sucesso = model.registaTroca(idPedido, idProduto, alimentoAtualId, alimentoDesejadoId);
                        if (sucesso) {
                            System.out.println("✓ Troca realizada com sucesso!");
                            System.out.println("  " + alimentoAtualId + " → " + alimentoDesejadoId);
                        } else {
                            System.out.println("✗ Não foi possível realizar a troca!");
                        }
                    } catch (PedidoException e) {
                        System.out.println("✗ Erro ao realizar troca: " + e.getMessage());
                    }
                });
            }

            menuSubstitutos.runOnce();
        } catch (PedidoException e) {
            System.out.println("✗ Erro: " + e.getMessage());
        }
    }

    // ========== GESTÃO DE PEDIDO ==========
    private void addEscolhidos(String item) {
        produtos_escolhidos.add(item);
        System.out.println("✓ " + item + " adicionado ao pedido!");
    }

    private void displayPedido() {
        System.out.println("\n========== PEDIDO ==========");
        if (produtos_escolhidos.isEmpty()) {
            System.out.println("Nenhum item adicionado ao pedido!");
        } else {
            System.out.println("Itens do pedido:");
            for (int i = 0; i < produtos_escolhidos.size(); i++) {
                System.out.println((i + 1) + ". " + produtos_escolhidos.get(i));
            }
        }
        System.out.println("Nota: " + nota + "\n");
    }

    private long escolherPedidoEmPreparacao() {
        List<Pedido> pedidos = model.getPedidosEmPreparacao();

        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido em preparação.");
            return -1;
        }

        String[] opcoes = new String[pedidos.size()];
        for (int i = 0; i < pedidos.size(); i++) {
            Pedido p = pedidos.get(i);
            String tipo = p.getTipo() ? "Restaurante" : "Take Away";
            opcoes[i] = "Pedido #" + p.getIdCounter() + " | " + tipo + " | €" + String.format("%.2f", p.getPreco());
        }

        final long[] selecionado = {-1};
        NewMenu menu = new NewMenu(opcoes);
        for (int i = 0; i < pedidos.size(); i++) {
            final int idx = i;
            menu.setHandler(i + 1, () -> selecionado[0] = pedidos.get(idx).getIdCounter());
        }

        menu.runOnce();
        return selecionado[0];
    }

    private void displayPedidosEmPreparacao() {
        List<Pedido> pedidos = model.getPedidosEmPreparacao();

        System.out.println("\n===== PEDIDOS EM PREPARAÇÃO =====");
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido em preparação!\n");
            return;
        }

        for (Pedido pedido : pedidos) {
            String tipo = pedido.getTipo() ? "Restaurante" : "Take Away";
            String notaPedido = pedido.getNota() == null || pedido.getNota().isEmpty() ? "-" : pedido.getNota();

            System.out.println("Pedido #" + pedido.getIdCounter() + " | " + tipo + " | €"
                    + String.format("%.2f", pedido.getPreco()));
            System.out.println("  Nota: " + notaPedido);
            System.out.println("  Itens:");

            List<Produto> produtos = pedido.getProdutos();
            for (int i = 0; i < produtos.size(); i++) {
                System.out.println("    " + (i + 1) + ". " + produtos.get(i).getNome());
            }
            System.out.println();
        }
    }

    private void requisitarIngredientes() {
        long idPedido = escolherPedidoEmPreparacao();
        if (idPedido < 0) {
            return;
        }

        System.out.print("ID do posto: ");
        String postoId = scanner.nextLine().trim();
        if (postoId.isEmpty()) {
            System.out.println("ID do posto inválido.");
            return;
        }

        model.requisitarIngredientes(idPedido, postoId);
        System.out.println("✓ Ingredientes requisitados para o pedido " + idPedido + " no posto " + postoId + ".");
    }

    private void atrasarPedidoEAtualizarFila() {
        long idPedido = escolherPedidoEmPreparacao();
        if (idPedido < 0) {
            return;
        }

        System.out.print("Tempo de atraso (ex: 2.5): ");
        double atraso;
        try {
            atraso = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Valor inválido.");
            return;
        }

        model.atrasarPedido(idPedido, atraso);

        List<Long> fila = model.getFilaPedidos();
        if (fila == null) {
            System.out.println("Fila de pedidos indisponível.");
            return;
        }

        model.atualizaFilaPedidos(idPedido, fila);
        System.out.println("✓ Atraso aplicado e fila atualizada: " + fila);
    }

    private void encerrarPedido() {
        long idPedido = escolherPedidoEmPreparacao();
        if (idPedido < 0) {
            return;
        }

        System.out.print("ID do posto responsável: ");
        String postoId = scanner.nextLine().trim();
        if (postoId.isEmpty()) {
            System.out.println("ID do posto inválido.");
            return;
        }

        model.encerrarPedido(idPedido, postoId);
        System.out.println("✓ Pedido " + idPedido + " encerrado no posto " + postoId + ".");
    }

    private String adicionaNota() {
        System.out.print("Insira a nota para o pedido: ");
        this.nota = scanner.nextLine();
        System.out.println("✓ Nota adicionada: " + nota);
        return nota;
    }

    private void displayTrocasProduto() {
        // Em construção
    }

    private void pagar(boolean takeAway) {
        System.out.println("Pagar - Em construção");
    }

    // ========== TESTE DAO ==========
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
                    Arrays.asList("BigMac", "batataFrita", "sumol"),
                    "batataSemSal",
                    true);
            System.out.println("   Pedido 2 criado com ID: " + pedidoId2 + "\n");

            // Criar terceiro pedido
            System.out.println(" Criando terceiro pedido...");
            long pedidoId3 = this.model.registaPedido(
                    Arrays.asList("menumcchicken"),
                    "ketchupExtra",
                    false);
            System.out.println("   Pedido 3 criado com ID: " + pedidoId3 + "\n");

            // Validar pagamentos
            System.out.println(" Validando pagamentos...");
            this.model.validaPagamento(pedidoId1);
            this.model.validaPagamento(pedidoId2);
            System.out.println("   Pagamentos validados!\n");

            // Testar trocas
            System.out.println(" Testando trocas de alimentos...");
            try {
                boolean trocaRealizada = this.model.registaTroca(pedidoId1, "BigMac", "carne_vaca", "carne_frango");
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
