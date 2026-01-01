package data;

import model.gestao.Alimento;

import java.sql.*;
import java.util.*;

/**
 * DAO para Alimentos
 * Implementa o padrão Map<String, Alimento> para persistência de alimentos
 */
public class AlimentoDAO implements Map<String, Alimento> {
    private static AlimentoDAO singleton = null;

    private AlimentoDAO() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {

            // Tabela Alimentos
            String sql = "CREATE TABLE IF NOT EXISTS alimentos (" +
                    "Id VARCHAR(50) NOT NULL PRIMARY KEY," +
                    "Nome VARCHAR(100) NOT NULL," +
                    "Quantidade INT DEFAULT 0)";
            stm.executeUpdate(sql);

            // Inicializar alimentos por defeito
            inicializarAlimentos();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    private void inicializarAlimentos() {
        // Verifica se a tabela já tem alimentos
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM alimentos")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Tabela vazia, inicializa alimentos
                this.put("carne_vaca", new Alimento(100, "carne_vaca", "Carne Vaca"));
                this.put("carne_frango", new Alimento(100, "carne_frango", "Carne Frango"));
                this.put("bacon", new Alimento(80, "bacon", "Bacon"));
                this.put("alface", new Alimento(60, "alface", "Alface"));
                this.put("tomate", new Alimento(70, "tomate", "Tomate"));
                this.put("pao_normal", new Alimento(50, "pao_normal", "Pão Normal"));
                this.put("pao_brioche", new Alimento(40, "pao_brioche", "Pão Brioche"));
                this.put("cebola", new Alimento(50, "cebola", "Cebola"));
                this.put("batata", new Alimento(120, "batata", "Batata"));
                this.put("nugget", new Alimento(90, "nugget", "Nugget"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    

    public static AlimentoDAO getInstance() {
        if (AlimentoDAO.singleton == null) {
            AlimentoDAO.singleton = new AlimentoDAO();
        }
        return AlimentoDAO.singleton;
    }

    @Override
    public int size() {
        int i = 0;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT count(*) FROM alimentos")) {
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
                PreparedStatement pstm = conn.prepareStatement("SELECT Id FROM alimentos WHERE Id=?")) {
            pstm.setString(1, (String) key);
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
        Alimento a = (Alimento) value;
        return this.containsKey(a.getId());
    }

    @Override
    public Alimento get(Object key) {
        Alimento a = null;
        if (!(key instanceof String))
            return null;
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("SELECT * FROM alimentos WHERE Id=?")) {
            pstm.setString(1, (String) key);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    a = new Alimento(
                            rs.getInt("Quantidade"),
                            rs.getString("Id"),
                            rs.getString("Nome"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return a;
    }

    @Override
    public Alimento put(String key, Alimento alimento) {
        Alimento oldAlimento = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement(
                        "INSERT INTO alimentos (Id, Nome, Quantidade) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE Nome=VALUES(Nome), Quantidade=VALUES(Quantidade)")) {
            pstm.setString(1, alimento.getId());
            pstm.setString(2, alimento.getNome());
            pstm.setInt(3, alimento.getQuantidade());
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return oldAlimento;
    }

    @Override
    public Alimento remove(Object key) {
        Alimento a = this.get(key);
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                PreparedStatement pstm = conn.prepareStatement("DELETE FROM alimentos WHERE Id=?")) {
            pstm.setString(1, (String) key);
            pstm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return a;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Alimento> alimentos) {
        for (Alimento a : alimentos.values()) {
            this.put(a.getId(), a);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement()) {
            stm.executeUpdate("TRUNCATE alimentos");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }

    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Alimento> values() {
        Collection<Alimento> alimentos = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD);
                Statement stm = conn.createStatement();
                ResultSet rs = stm.executeQuery("SELECT * FROM alimentos")) {
            while (rs.next()) {
                Alimento a = new Alimento(
                        rs.getInt("Quantidade"),
                        rs.getString("Id"),
                        rs.getString("Nome"));
                alimentos.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
        return alimentos;
    }

    @Override
    public Set<Entry<String, Alimento>> entrySet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    //========================================================================================================================
    //  Metodos 
    //========================================================================================================================

}
