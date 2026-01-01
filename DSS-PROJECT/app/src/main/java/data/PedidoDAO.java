package data;

import model.pedidos.Pedido;
import model.pedidos.Produto;
import model.preparacoes.PreparacoesFacade;
import model.pedidos.Item;
import model.pedidos.Menu;
import model.pedidos.Estado;
import model.gestao.Alimento;

import java.sql.*;
import java.util.*;

/**
 * DAO para Pedidos
 * Implementa o padrão Map<Long, Pedido> para persistência de pedidos
 */
public class PedidoDAO implements Map<Long, Pedido> {
    private static PedidoDAO singleton = null;

    private PedidoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {

            // Tabela Alimentos
            String sql = "CREATE TABLE IF NOT EXISTS alimentos (" +
                    "Id VARCHAR(50) NOT NULL PRIMARY KEY," +
                    "Nome VARCHAR(100) NOT NULL," +
                    "Quantidade INT DEFAULT 0)";
            stm.executeUpdate(sql);

            // Tabela Produtos (abstrata - armazena info comum)
            sql = "CREATE TABLE IF NOT EXISTS produtos (" +
                    "Id VARCHAR(50) NOT NULL PRIMARY KEY," +
                    "Nome VARCHAR(100) NOT NULL," +
                    "Preco DOUBLE NOT NULL," +
                    "TempoConfecaoEsperado DOUBLE NOT NULL," +
                    "TipoProduto VARCHAR(20) NOT NULL)"; // 'ITEM' ou 'MENU'
            stm.executeUpdate(sql);

            // Tabela Item_Alimentos (relaciona itens com alimentos)
            sql = "CREATE TABLE IF NOT EXISTS item_alimentos (" +
                    "ItemId VARCHAR(50) NOT NULL," +
                    "AlimentoId VARCHAR(50) NOT NULL," +
                    "PRIMARY KEY(ItemId, AlimentoId)," +
                    "FOREIGN KEY(ItemId) REFERENCES produtos(Id) ON DELETE CASCADE," +
                    "FOREIGN KEY(AlimentoId) REFERENCES alimentos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Tabela Item_Trocas (possíveis trocas de alimentos)
            sql = "CREATE TABLE IF NOT EXISTS item_trocas (" +
                    "ItemId VARCHAR(50) NOT NULL," +
                    "AlimentoOriginalId VARCHAR(50) NOT NULL," +
                    "AlimentoTrocaId VARCHAR(50) NOT NULL," +
                    "PRIMARY KEY(ItemId, AlimentoOriginalId, AlimentoTrocaId)," +
                    "FOREIGN KEY(ItemId) REFERENCES produtos(Id) ON DELETE CASCADE," +
                    "FOREIGN KEY(AlimentoOriginalId) REFERENCES alimentos(Id) ON DELETE CASCADE," +
                    "FOREIGN KEY(AlimentoTrocaId) REFERENCES alimentos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Tabela Menu_Itens (relaciona menus com itens)
            sql = "CREATE TABLE IF NOT EXISTS menu_itens (" +
                    "MenuId VARCHAR(50) NOT NULL," +
                    "ItemId VARCHAR(50) NOT NULL," +
                    "PRIMARY KEY(MenuId, ItemId)," +
                    "FOREIGN KEY(MenuId) REFERENCES produtos(Id) ON DELETE CASCADE," +
                    "FOREIGN KEY(ItemId) REFERENCES produtos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Tabela Pedidos
            sql = "CREATE TABLE IF NOT EXISTS pedidos (" +
                    "Id BIGINT NOT NULL PRIMARY KEY," +
                    "Estado VARCHAR(20) DEFAULT 'PorPagar'," +
                    "Nota VARCHAR(255) DEFAULT ''," +
                    "Preco DOUBLE DEFAULT 0," +
                    "TempoConfecaoEsperado DOUBLE DEFAULT 0," +
                    "TempoConfecaoReal DOUBLE DEFAULT 0," +
                    "Tipo BOOLEAN DEFAULT TRUE)"; // true -> restaurante; false -> takeaway
            stm.executeUpdate(sql);

            // Tabela Pedido_Produtos (relaciona pedidos com produtos)
            sql = "CREATE TABLE IF NOT EXISTS pedido_produtos (" +
                    "PedidoId BIGINT NOT NULL," +
                    "ProdutoId VARCHAR(50) NOT NULL," +
                    "PRIMARY KEY(PedidoId, ProdutoId)," +
                    "FOREIGN KEY(PedidoId) REFERENCES pedidos(Id) ON DELETE CASCADE," +
                    "FOREIGN KEY(ProdutoId) REFERENCES produtos(Id) ON DELETE CASCADE)";
            stm.executeUpdate(sql);

            // Inicializar produtos e relações
            inicializarProdutos();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    private void inicializarProdutos() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            // Verificar UMA VEZ se a tabela está vazia
            if (tabelaProdutosVazia(conn)) {
                System.out.println("→ Inicializando BD com dados padrão...\n");
                // PRIMEIRO: Inicializar alimentos na tabela alimentos
                inicializarAlimentos(conn);
                // DEPOIS: Inicializar items, menus e suas relações
                inicializarItems(conn);
                inicializarMenus(conn);
                inicializarTrocas(conn);
                System.out.println("→ Todas as inicializações completadas!\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    private boolean tabelaProdutosVazia(Connection conn) throws SQLException {
        try (Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM produtos")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private void inicializarAlimentos(Connection conn) throws SQLException {
        // Inserir os 10 alimentos padrão na tabela alimentos
        PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO alimentos (Id, Nome, Quantidade) VALUES (?, ?, ?)");

        String[][] alimentos = {
                { "carne_vaca", "Carne de Vaca", "100" },
                { "carne_frango", "Carne de Frango", "100" },
                { "bacon", "Bacon", "50" },
                { "alface", "Alface", "50" },
                { "tomate", "Tomate", "50" },
                { "pao_normal", "Pão Normal", "100" },
                { "pao_brioche", "Pão Brioche", "50" },
                { "cebola", "Cebola", "50" },
                { "batata", "Batata", "100" },
                { "nugget", "Nugget", "100" }
        };

        for (String[] alimento : alimentos) {
            pstm.setString(1, alimento[0]);
            pstm.setString(2, alimento[1]);
            pstm.setInt(3, Integer.parseInt(alimento[2]));
            pstm.executeUpdate();
        }

        System.out.println("→ Todos os 10 alimentos inseridos com sucesso!");

        pstm.close();
    }

    private void inicializarItems(Connection conn) throws SQLException {
        System.out.println("→ Iniciando inicializarItems()...");
        // Inserir produtos
        PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO produtos (Id, Nome, Preco, TempoConfecaoEsperado, TipoProduto) VALUES (?, ?, ?, ?, ?)");

        // batataFrita
        pstm.setString(1, "batataFrita");
        pstm.setString(2, "Batatas Fritas");
        pstm.setDouble(3, 4.0);
        pstm.setDouble(4, 100.0);
        pstm.setString(5, "ITEM");
        pstm.executeUpdate();

        // BigMac
        pstm.setString(1, "BigMac");
        pstm.setString(2, "BigMac");
        pstm.setDouble(3, 20.0);
        pstm.setDouble(4, 300.0);
        pstm.setString(5, "ITEM");
        pstm.executeUpdate();
        // McChicken
        pstm.setString(1, "mcchicken");
        pstm.setString(2, "McChicken");
        pstm.setDouble(3, 20.0);
        pstm.setDouble(4, 300.0);
        pstm.setString(5, "ITEM");
        pstm.executeUpdate();

        // Coca-Cola
        pstm.setString(1, "coca_cola");
        pstm.setString(2, "Coca-Cola");
        pstm.setDouble(3, 1.5);
        pstm.setDouble(4, 10.0);
        pstm.setString(5, "ITEM");
        pstm.executeUpdate();
        // Sumol
        pstm.setString(1, "sumol");
        pstm.setString(2, "Sumol");
        pstm.setDouble(3, 1.5);
        pstm.setDouble(4, 10.0);
        pstm.setString(5, "ITEM");
        pstm.executeUpdate();

        pstm.close();

        // Inserir alimentos para items
        System.out.println("→ Inserindo relações item_alimentos...");
        pstm = conn.prepareStatement("INSERT INTO item_alimentos (ItemId, AlimentoId) VALUES (?, ?)");

        // batataFrita tem alimento batata
        pstm.setString(1, "batataFrita");
        pstm.setString(2, "batata");
        pstm.executeUpdate();

        // BigMac tem alimentos: alface, carne_vaca, cebola, pao_normal, tomate
        pstm.setString(1, "BigMac");
        pstm.setString(2, "alface");
        pstm.executeUpdate();

        pstm.setString(1, "BigMac");
        pstm.setString(2, "carne_vaca");
        pstm.executeUpdate();

        pstm.setString(1, "BigMac");
        pstm.setString(2, "cebola");
        pstm.executeUpdate();

        pstm.setString(1, "BigMac");
        pstm.setString(2, "pao_normal");
        pstm.executeUpdate();

        pstm.setString(1, "BigMac");
        pstm.setString(2, "tomate");
        pstm.executeUpdate();

        // Coca-Cola não tem alimentos associados

        System.out.println("→ Item_alimentos inseridos com sucesso!");
        pstm.close();
    }

    private void inicializarMenus(Connection conn) throws SQLException {
        System.out.println("→ Iniciando inicializarMenus()...");
        // Inserir produtos do tipo MENU
        PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO produtos (Id, Nome, Preco, TempoConfecaoEsperado, TipoProduto) VALUES (?, ?, ?, ?, ?)");

        // Menu 1: Combo Clássico (BigMac + Batatas + Coca)
        pstm.setString(1, "comboClassico");
        pstm.setString(2, "Combo Clássico");
        pstm.setDouble(3, 24.0); // 20 + 4 + 1.5 - desconto
        pstm.setDouble(4, 300.0); // máximo dos 3 produtos
        pstm.setString(5, "MENU");
        int r1 = pstm.executeUpdate();
        System.out.println("  → Menu 1 inserted: " + r1 + " rows");

        // Menu 2: Combo Duplo (2x BigMac + Batatas + Coca)
        pstm.setString(1, "comboDuplo");
        pstm.setString(2, "Combo Duplo");
        pstm.setDouble(3, 40.0); // 20 + 20 + 4 + 1.5 - desconto
        pstm.setDouble(4, 300.0);
        pstm.setString(5, "MENU");
        int r2 = pstm.executeUpdate();
        System.out.println("  → Menu 2 inserted: " + r2 + " rows");

        pstm.close();

        // DEBUG: Verificar registos inseridos de MENU
        System.out.println("→ Todos os produtos de tipo MENU inseridos com sucesso!");
        System.out.flush(); // Force flush immediately

        PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM produtos WHERE TipoProduto = 'MENU'");
        ResultSet rsCheck = checkStmt.executeQuery();

        System.out.println("\n=== Registos de MENU em produtos ===");
        while (rsCheck.next()) {
            String produtoId = rsCheck.getString("Id");
            String nome = rsCheck.getString("Nome");
            double preco = rsCheck.getDouble("Preco");
            double tempo = rsCheck.getDouble("TempoConfecaoEsperado");
            String tipo = rsCheck.getString("TipoProduto");
            System.out.println("  ID: " + produtoId + " | Nome: " + nome + " | Preço: " + preco + "€ | Tempo: " + tempo
                    + "s | Tipo: " + tipo);
        }
        rsCheck.close();
        checkStmt.close();

        // Inserir relações menu-itens
        System.out.println("→ Inserindo relações menu-itens...");
        pstm = conn.prepareStatement("INSERT INTO menu_itens (MenuId, ItemId) VALUES (?, ?)");

        // comboClassico contém BigMac, batataFrita e coca_cola
        pstm.setString(1, "comboClassico");
        pstm.setString(2, "BigMac");
        pstm.executeUpdate();

        pstm.setString(1, "comboClassico");
        pstm.setString(2, "batataFrita");
        pstm.executeUpdate();

        pstm.setString(1, "comboClassico");
        pstm.setString(2, "coca_cola");
        pstm.executeUpdate();

        // comboDuplo contém BigMac, batataFrita e coca_cola (BigMac aparece uma única
        // vez na tabela)
        pstm.setString(1, "comboDuplo");
        pstm.setString(2, "BigMac");
        pstm.executeUpdate();

        pstm.setString(1, "comboDuplo");
        pstm.setString(2, "batataFrita");
        pstm.executeUpdate();

        pstm.setString(1, "comboDuplo");
        pstm.setString(2, "coca_cola");
        pstm.executeUpdate();

        System.out.println("→ Todas as relações menu-itens inseridas com sucesso!\n");

        pstm.close();
    }

    private void inicializarTrocas(Connection conn) throws SQLException {
        // Verifica se a tabela já tem trocas
        try (Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM item_trocas")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Tabela vazia, inicializa trocas
                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO item_trocas (ItemId, AlimentoOriginalId, AlimentoTrocaId) VALUES (?, ?, ?)");

                // BigMac: carne_vaca pode ser trocada por carne_frango
                pstm.setString(1, "BigMac");
                pstm.setString(2, "carne_vaca");
                pstm.setString(3, "carne_frango");
                pstm.executeUpdate();

                // BigMac: carne_vaca pode ser trocada por bacon
                pstm.setString(1, "BigMac");
                pstm.setString(2, "carne_vaca");
                pstm.setString(3, "bacon");
                pstm.executeUpdate();

                // BigMac: pao_normal pode ser trocado por pao_brioche
                pstm.setString(1, "BigMac");
                pstm.setString(2, "pao_normal");
                pstm.setString(3, "pao_brioche");
                pstm.executeUpdate();

                // batataFrita: batata pode ser trocada por nugget (opcional)
                pstm.setString(1, "batataFrita");
                pstm.setString(2, "batata");
                pstm.setString(3, "nugget");
                pstm.executeUpdate();

                System.out.println("→ Todas as trocas inseridas com sucesso!");

                pstm.close();
            }
        }
    }

    public static PedidoDAO getInstance() {
        if (PedidoDAO.singleton == null) {
            PedidoDAO.singleton = new PedidoDAO();
        }
        return PedidoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM pedidos")) {
            if (rs.next()) {
                i = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return i;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        boolean r = false;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM pedidos WHERE Id=?")) {
            pstm.setLong(1, (Long) key);
            try (ResultSet rs = pstm.executeQuery()) {
                r = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return r;
    }

    @Override
    public boolean containsValue(Object value) {
        Pedido p = (Pedido) value;
        return this.containsKey(p.getIdCounter());
    }

    @Override
    public Pedido get(Object key) {
        Pedido p = null;
        if (!(key instanceof Long))
            return null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("SELECT * FROM pedidos WHERE Id=?")) {
            pstm.setLong(1, (Long) key);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    p = new Pedido();
                    p.setId(rs.getLong("Id"));
                    p.setEstado(Estado.valueOf(rs.getString("Estado")));
                    p.setNota(rs.getString("Nota"));
                    p.setPreco(rs.getDouble("Preco"));
                    p.setTempoConfecaoEsperado(rs.getDouble("TempoConfecaoEsperado"));
                    p.setTempoConfecaoReal(rs.getDouble("TempoConfecaoReal"));
                    p.setTipo(rs.getBoolean("Tipo"));

                    // Carregar produtos associados ao pedido
                    p.setProdutos(getProdutosPedido((Long) key, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    /**
     * Remove um alimento
     */
    public void removeAlimento(String id) {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("DELETE FROM alimentos WHERE Id=?")) {
            pstm.setString(1, id);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    private List<Produto> getProdutosPedido(Long pedidoId, Connection conn) throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT ProdutoId FROM pedido_produtos WHERE PedidoId=?")) {
            pstm.setLong(1, pedidoId);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String produtoId = rs.getString("ProdutoId");
                    Produto produto = getProduto(produtoId, conn);
                    if (produto != null) {
                        produtos.add(produto);
                    }
                }
            }
        }
        return produtos;
    }

    public Produto getProduto(String produtoId, Connection conn) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT * FROM produtos WHERE Id=?")) {
            pstm.setString(1, produtoId);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    String tipo = rs.getString("TipoProduto");
                    String id = rs.getString("Id");
                    String nome = rs.getString("Nome");
                    double preco = rs.getDouble("Preco");
                    double tempo = rs.getDouble("TempoConfecaoEsperado");

                    if ("ITEM".equals(tipo)) {
                        return getItem(id, nome, preco, tempo, conn);
                    } else if ("MENU".equals(tipo)) {
                        return getMenu(id, nome, preco, tempo, conn);
                    }
                }
            }
        }
        return null;
    }

    private Item getItem(String id, String nome, double preco, double tempo, Connection conn) throws SQLException {
        Item item = new Item(id, preco, nome, tempo);

        // Carregar alimentos do item
        Map<String, Alimento> alimentos = new HashMap<>();
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT a.* FROM alimentos a " +
                        "INNER JOIN item_alimentos ia ON a.Id = ia.AlimentoId " +
                        "WHERE ia.ItemId = ?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Alimento alimento = new Alimento(
                            rs.getInt("Quantidade"),
                            rs.getString("Id"),
                            rs.getString("Nome"));
                    alimentos.put(alimento.getId(), alimento);
                }
            }
        }
        item.setAlimentos(alimentos);

        // Carregar trocas possíveis
        Map<String, List<String>> trocas = new HashMap<>();
        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT AlimentoOriginalId, AlimentoTrocaId FROM item_trocas WHERE ItemId = ?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String originalId = rs.getString("AlimentoOriginalId");
                    String trocaId = rs.getString("AlimentoTrocaId");

                    trocas.putIfAbsent(originalId, new ArrayList<>());
                    trocas.get(originalId).add(trocaId);
                }
            }
        }
        item.setTrocas(trocas);

        return item;
    }

    private Menu getMenu(String id, String nome, double preco, double tempo, Connection conn) throws SQLException {
        List<Item> itens = new ArrayList<>();

        try (PreparedStatement pstm = conn.prepareStatement(
                "SELECT p.* FROM produtos p " +
                        "INNER JOIN menu_itens mi ON p.Id = mi.ItemId " +
                        "WHERE mi.MenuId = ?")) {
            pstm.setString(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Item item = getItem(
                            rs.getString("Id"),
                            rs.getString("Nome"),
                            rs.getDouble("Preco"),
                            rs.getDouble("TempoConfecaoEsperado"),
                            conn);
                    itens.add(item);
                }
            }
        }

        return new Menu(id, preco, nome, tempo, itens);
    }

    private void saveProduto(Produto produto, Connection conn) throws SQLException {
        String tipoProduto = produto instanceof Menu ? "MENU" : "ITEM";

        try (PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO produtos VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Preco=VALUES(Preco), " +
                        "TempoConfecaoEsperado=VALUES(TempoConfecaoEsperado), TipoProduto=VALUES(TipoProduto)")) {
            pstm.setString(1, produto.getId());
            pstm.setString(2, produto.getNome());
            pstm.setDouble(3, produto.getPreco());
            pstm.setDouble(4, produto.getTempoConfecaoEsperado());
            pstm.setString(5, tipoProduto);
            pstm.executeUpdate();
        }

        if (produto instanceof Menu) {
            saveMenu((Menu) produto, conn);
        } else if (produto instanceof Item) {
            saveItem((Item) produto, conn);
        }
    }

    private void saveItem(Item item, Connection conn) throws SQLException {
        // Remover alimentos e trocas antigos
        try (PreparedStatement pstm = conn.prepareStatement(
                "DELETE FROM item_alimentos WHERE ItemId=?")) {
            pstm.setString(1, item.getId());
            pstm.executeUpdate();
        }

        try (PreparedStatement pstm = conn.prepareStatement(
                "DELETE FROM item_trocas WHERE ItemId=?")) {
            pstm.setString(1, item.getId());
            pstm.executeUpdate();
        }

        // Inserir alimentos do item
        if (item.getAlimentos() != null) {
            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO item_alimentos (ItemId, AlimentoId) VALUES (?, ?)")) {
                for (Alimento alimento : item.getAlimentos().values()) {
                    saveAlimento(alimento, conn);
                    pstm.setString(1, item.getId());
                    pstm.setString(2, alimento.getId());
                    pstm.addBatch();
                }
                pstm.executeBatch();
            }
        }

        // Inserir trocas do item
        if (item.getTrocas() != null) {
            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO item_trocas (ItemId, AlimentoOriginalId, AlimentoTrocaId) VALUES (?, ?, ?)")) {
                for (Map.Entry<String, List<String>> entrada : item.getTrocas().entrySet()) {
                    String alimentoOriginalId = entrada.getKey();
                    for (String alimentoTrocaId : entrada.getValue()) {
                        pstm.setString(1, item.getId());
                        pstm.setString(2, alimentoOriginalId);
                        pstm.setString(3, alimentoTrocaId);
                        pstm.addBatch();
                    }
                }
                pstm.executeBatch();
            }
        }
    }

    private void saveMenu(Menu menu, Connection conn) throws SQLException {
        // Remover itens antigos do menu
        try (PreparedStatement pstm = conn.prepareStatement(
                "DELETE FROM menu_itens WHERE MenuId=?")) {
            pstm.setString(1, menu.getId());
            pstm.executeUpdate();
        }

        // Inserir novos itens do menu
        if (menu.getItens() != null) {
            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO menu_itens (MenuId, ItemId) VALUES (?, ?)")) {
                for (Item item : menu.getItens()) {
                    saveItem(item, conn);
                    pstm.setString(1, menu.getId());
                    pstm.setString(2, item.getId());
                    pstm.addBatch();
                }
                pstm.executeBatch();
            }
        }
    }

    private void saveAlimento(Alimento alimento, Connection conn) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement(
                "INSERT INTO alimentos VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Quantidade=VALUES(Quantidade)")) {
            pstm.setString(1, alimento.getId());
            pstm.setString(2, alimento.getNome());
            pstm.setInt(3, alimento.getQuantidade());
            pstm.executeUpdate();
        }
    }

    @Override
    public Pedido put(Long key, Pedido p) {
        Pedido res = null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstm = conn.prepareStatement(
                    "INSERT INTO pedidos VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE Estado=VALUES(Estado), Nota=VALUES(Nota), " +
                            "Preco=VALUES(Preco), TempoConfecaoEsperado=VALUES(TempoConfecaoEsperado), " +
                            "TempoConfecaoReal=VALUES(TempoConfecaoReal), Tipo=VALUES(Tipo)")) {
                pstm.setLong(1, p.getIdCounter());
                pstm.setString(2, p.getEstado().toString());
                pstm.setString(3, p.getNota());
                pstm.setDouble(4, p.getPreco());
                pstm.setDouble(5, p.getTempoConfecaoEsperado());
                pstm.setDouble(6, p.getTempoConfecaoReal());
                pstm.setBoolean(7, p.getTipo());
                pstm.executeUpdate();
            }

            // Remover produtos antigos
            try (PreparedStatement pstm = conn.prepareStatement(
                    "DELETE FROM pedido_produtos WHERE PedidoId=?")) {
                pstm.setLong(1, p.getIdCounter());
                pstm.executeUpdate();
            }

            // Inserir produtos do pedido
            for (Produto produto : p.getProdutos()) {
                // SÓ salvar o produto se ele NÃO existir ainda na BD
                if (!produtoExiste(produto.getId(), conn)) {
                    saveProduto(produto, conn);
                }

                try (PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO pedido_produtos (PedidoId, ProdutoId) VALUES (?, ?)")) {
                    pstm.setLong(1, p.getIdCounter());
                    pstm.setString(2, produto.getId());
                    pstm.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    private boolean produtoExiste(String produtoId, Connection conn) throws SQLException {
        try (PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM produtos WHERE Id=?")) {
            pstm.setString(1, produtoId);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public Pedido remove(Object key) {
        Pedido p = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("DELETE FROM pedidos WHERE Id=?")) {
            pstm.setLong(1, (Long) key);
            pstm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return p;
    }

    @Override
    public void putAll(Map<? extends Long, ? extends Pedido> pedidos) {
        for (Pedido p : pedidos.values()) {
            this.put(p.getIdCounter(), p);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {
            stm.executeUpdate("TRUNCATE pedido_produtos");
            stm.executeUpdate("TRUNCATE menu_itens");
            stm.executeUpdate("TRUNCATE item_trocas");
            stm.executeUpdate("TRUNCATE item_alimentos");
            stm.executeUpdate("TRUNCATE pedidos");
            stm.executeUpdate("TRUNCATE produtos");
            stm.executeUpdate("TRUNCATE alimentos");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<Long> keySet() {
        throw new NullPointerException("Not implemented!");
    }

    @Override
    public Collection<Pedido> values() {
        Collection<Pedido> res = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT Id FROM pedidos")) {
            while (rs.next()) {
                Long id = rs.getLong("Id");
                Pedido p = this.get(id);
                res.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return res;
    }

    @Override
    public Set<Entry<Long, Pedido>> entrySet() {
        throw new NullPointerException("Not implemented!");
    }
}