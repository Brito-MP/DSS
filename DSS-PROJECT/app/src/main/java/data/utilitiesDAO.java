package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class utilitiesDAO {

    /**
     * Centraliza toda a lógica de população de bases de dados Altera aqui os
     * dados (alimentos, items, menus, trocas)
     */
    public static void inicializarBaseDados(Connection conn) throws SQLException {
        inicializarAlimentos(conn);
        inicializarItems(conn);
        inicializarMenus(conn);
        inicializarTrocas(conn);
        inicializarPostos(conn);
    }

    private static void inicializarPostos(Connection conn) throws SQLException {
        // Garantir tabelas (caso PostoDAO ainda não tenha sido carregado)
        try (Statement stm = conn.createStatement()) {
            stm.executeUpdate("CREATE TABLE IF NOT EXISTS postos (Id VARCHAR(50) NOT NULL PRIMARY KEY)");
            stm.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS posto_alimentos ("
                    + "PostoId VARCHAR(50) NOT NULL,"
                    + "AlimentoId VARCHAR(50) NOT NULL,"
                    + "Quantidade INT DEFAULT 0,"
                    + "PRIMARY KEY(PostoId, AlimentoId),"
                    + "FOREIGN KEY(PostoId) REFERENCES postos(Id) ON DELETE CASCADE,"
                    + "FOREIGN KEY(AlimentoId) REFERENCES alimentos(Id) ON DELETE CASCADE"
                    + ")");
        }

        // Só povoa se ainda não houver postos
        try (Statement stm = conn.createStatement(); ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM postos")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("→ Inicializando postos...");

                try (PreparedStatement insPosto = conn.prepareStatement(
                        "INSERT INTO postos (Id) VALUES (?) ON DUPLICATE KEY UPDATE Id=VALUES(Id)"); PreparedStatement insStock = conn.prepareStatement(
                                "INSERT INTO posto_alimentos (PostoId, AlimentoId, Quantidade) "
                                + "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Quantidade=VALUES(Quantidade)")) {

                    // Posto A
                    insPosto.setString(1, "postoA");
                    insPosto.executeUpdate();
                    Object[][] stockA = {
                        {"carne_vaca", 10},
                        {"carne_frango", 10},
                        {"batata", 30},
                        {"alface", 10}
                    };
                    for (Object[] s : stockA) {
                        insStock.setString(1, "postoA");
                        insStock.setString(2, (String) s[0]);
                        insStock.setInt(3, (Integer) s[1]);
                        insStock.executeUpdate();
                    }

                    // Posto B
                    insPosto.setString(1, "postoB");
                    insPosto.executeUpdate();
                    Object[][] stockB = {
                        {"pao_normal", 20},
                        {"pao_brioche", 10},
                        {"cebola", 15},
                        {"tomate", 15}
                    };
                    for (Object[] s : stockB) {
                        insStock.setString(1, "postoB");
                        insStock.setString(2, (String) s[0]);
                        insStock.setInt(3, (Integer) s[1]);
                        insStock.executeUpdate();
                    }
                }

                System.out.println("✓ Postos inicializados com sucesso!\n");
            }
        }
    }

    private static void inicializarAlimentos(Connection conn) throws SQLException {
        try (Statement stm = conn.createStatement(); ResultSet rs = stm.executeQuery(
                "SELECT COUNT(*) FROM alimentos WHERE Id IN ('carne_vaca', 'carne_frango', 'bacon')")) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("→ Inicializando alimentos...");

                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO alimentos (Id, Nome, Quantidade) VALUES (?, ?, ?)");

                // ====== ALTERAR AQUI OS ALIMENTOS ======
                String[][] alimentos = {
                    {"carne_vaca", "Carne de Vaca", "100"},
                    {"carne_frango", "Carne de Frango", "100"},
                    {"bacon", "Bacon", "50"},
                    {"alface", "Alface", "50"},
                    {"tomate", "Tomate", "50"},
                    {"pao_normal", "Pão Normal", "100"},
                    {"pao_brioche", "Pão Brioche", "50"},
                    {"cebola", "Cebola", "50"},
                    {"batata", "Batata", "100"},
                    {"nugget", "Nugget", "100"}
                };

                for (String[] alimento : alimentos) {
                    pstm.setString(1, alimento[0]);
                    pstm.setString(2, alimento[1]);
                    pstm.setInt(3, Integer.parseInt(alimento[2]));
                    pstm.executeUpdate();
                }

                pstm.close();
                System.out.println("✓ Alimentos inicializados com sucesso!\n");
            }
        }
    }

    private static void inicializarItems(Connection conn) throws SQLException {
        try (Statement stm = conn.createStatement(); ResultSet rs = stm.executeQuery(
                "SELECT COUNT(*) FROM produtos WHERE TipoProduto='ITEM' AND Id IN ('BigMac', 'batataFrita', 'coca_cola')")) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("→ Inicializando items...");

                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO produtos (Id, Nome, Preco, TempoConfecaoEsperado, TipoProduto) VALUES (?, ?, ?, ?, ?)");

                // ====== ALTERAR AQUI OS ITEMS ======
                Object[][] items = {
                    {"batataFrita", "Batatas Fritas", 4.0, 100.0},
                    {"BigMac", "BigMac", 20.0, 300.0},
                    {"mcchicken", "McChicken", 20.0, 300.0},
                    {"coca_cola", "Coca-Cola", 1.5, 10.0},
                    {"sumol", "Sumol", 1.5, 10.0}
                };

                for (Object[] item : items) {
                    pstm.setString(1, (String) item[0]);
                    pstm.setString(2, (String) item[1]);
                    pstm.setDouble(3, (Double) item[2]);
                    pstm.setDouble(4, (Double) item[3]);
                    pstm.setString(5, "ITEM");
                    pstm.executeUpdate();
                }

                pstm.close();

                // Inserir relações item_alimentos
                inicializarAlimentosItems(conn);

                System.out.println("✓ Items inicializados com sucesso!\n");
            }
        }
    }

    private static void inicializarAlimentosItems(Connection conn) throws SQLException {
        try (Statement stm = conn.createStatement(); ResultSet rs = stm.executeQuery(
                "SELECT COUNT(*) FROM item_alimentos WHERE ItemId IN ('BigMac', 'batataFrita')")) {

            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO item_alimentos (ItemId, AlimentoId) VALUES (?, ?)");

                // ====== ALTERAR AQUI AS RELAÇÕES ITEM-ALIMENTOS ======
                String[][] itemAlimentos = {
                        { "batataFrita", "batata" },
                        { "BigMac", "alface" },
                        { "BigMac", "carne_vaca" },
                        { "BigMac", "cebola" },
                        { "BigMac", "pao_normal" },
                        { "BigMac", "tomate" },
                        { "mcchicken", "alface"},
                        { "mcchicken", "carne_frango"},
                        { "mcchicken", "pao_brioche" },
                        { "mcchicken", "tomate"}
                };

                for (String[] rel : itemAlimentos) {
                    pstm.setString(1, rel[0]);
                    pstm.setString(2, rel[1]);
                    pstm.executeUpdate();
                }

                pstm.close();
            }
        }
    }

    private static void inicializarMenus(Connection conn) throws SQLException {
        try (Statement stm = conn.createStatement(); ResultSet rs = stm.executeQuery(
                "SELECT COUNT(*) FROM produtos WHERE TipoProduto='MENU' AND Id IN ('menubigmac', 'menumcchicken')")) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("→ Inicializando menus...");

                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO produtos (Id, Nome, Preco, TempoConfecaoEsperado, TipoProduto) VALUES (?, ?, ?, ?, ?)");

                // ====== ALTERAR AQUI OS MENUS ======
                Object[][] menus = {
                    {"menubigmac", "Menu BigMac", 24.0, 300.0},
                    {"menumcchicken", "Menu McChicken", 40.0, 300.0}
                };

                for (Object[] menu : menus) {
                    pstm.setString(1, (String) menu[0]);
                    pstm.setString(2, (String) menu[1]);
                    pstm.setDouble(3, (Double) menu[2]);
                    pstm.setDouble(4, (Double) menu[3]);
                    pstm.setString(5, "MENU");
                    pstm.executeUpdate();
                }

                pstm.close();

                System.out.println("✓ Menus inseridos com sucesso!\n");
            }
        }

        inicializarMenuItens(conn);
    }

    private static void inicializarMenuItens(Connection conn) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement(
                "DELETE FROM menu_itens WHERE MenuId IN ('menubigmac', 'menumcchicken')"); PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO menu_itens (MenuId, ItemId) VALUES (?, ?)")) {

            delete.executeUpdate();

            // ====== ALTERAR AQUI AS RELAÇÕES MENU-ITENS ======
            String[][] menuItens = {
                // Menu BigMac
                {"menubigmac", "BigMac"},
                {"menubigmac", "batataFrita"},
                {"menubigmac", "coca_cola"},
                // Menu McChicken
                {"menumcchicken", "mcchicken"},
                {"menumcchicken", "batataFrita"},
                {"menumcchicken", "sumol"}
            };

            for (String[] rel : menuItens) {
                insert.setString(1, rel[0]);
                insert.setString(2, rel[1]);
                insert.executeUpdate();
            }
        }
    }

    private static void inicializarTrocas(Connection conn) throws SQLException {
        try (Statement stm = conn.createStatement(); ResultSet rs = stm.executeQuery(
                "SELECT COUNT(*) FROM item_trocas WHERE ItemId='BigMac'")) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("→ Inicializando trocas...");

                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO item_trocas (ItemId, AlimentoOriginalId, AlimentoTrocaId) VALUES (?, ?, ?)");

                // ====== ALTERAR AQUI AS TROCAS (BIDIRECIONAIS) ======
                String[][] trocas = {
                    // carne_vaca <-> carne_frango
                    {"BigMac", "carne_vaca", "carne_frango"},
                    {"BigMac", "carne_frango", "carne_vaca"},
                    // carne_vaca <-> bacon
                    {"BigMac", "carne_vaca", "bacon"},
                    {"BigMac", "bacon", "carne_vaca"},
                    // pao_normal <-> pao_brioche
                    {"BigMac", "pao_normal", "pao_brioche"},
                    {"BigMac", "pao_brioche", "pao_normal"}
                };

                for (String[] troca : trocas) {
                    pstm.setString(1, troca[0]);
                    pstm.setString(2, troca[1]);
                    pstm.setString(3, troca[2]);
                    pstm.executeUpdate();
                }

                pstm.close();
                System.out.println("✓ Trocas inicializadas com sucesso!\n");
            }
        }
    }
}
