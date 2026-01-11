package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import data.DAOconfig;
import data.utilitiesDAO;
import model.gestao.Alimento;
import model.gestao.GestaoFacade;
import model.gestao.InterGestaoL;
import model.pedidos.InterPedidoL;
import model.pedidos.Pedido;
import model.pedidos.PedidoException;
import model.pedidos.PedidosFacade;
import model.pedidos.Produto;
import model.preparacoes.InterPreparacoesL;
import model.preparacoes.PreparacoesFacade;
import model.preparacoes.TipoPosto;

public class RestauranteFacade implements InterRestauranteL {

    private InterGestaoL gestao;
    private InterPreparacoesL preparacoes;
    private InterPedidoL pedidos;

    public RestauranteFacade() {
        try (Connection conn = DriverManager.getConnection(DAOconfig.URL, DAOconfig.USERNAME, DAOconfig.PASSWORD)) {
            utilitiesDAO.inicializarBaseDados(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erro a inicializar base de dados", e);
        }

        this.pedidos = new PedidosFacade();
        this.gestao = new GestaoFacade();
        this.preparacoes = new PreparacoesFacade();

    }

    // ======================================================================================
    // Métodos do Sub-Sistema pedidos
    // ======================================================================================
    @Override
    public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo) {
        return this.pedidos.registaPedido(codigoProdutos, nota, tipo);
    }

    @Override
    public void validaPagamento(long idPedido) {
        this.pedidos.validaPagamento(idPedido);
        this.preparacoes.adicionaListaPedidos(idPedido);
    }

    @Override
    public boolean registaTroca(long idPedido, String idProduto, String idAlimentoAtual, String idAlimentoDesejado)
            throws PedidoException {
        Alimento alimentoDesejado = this.gestao.getAlimento(idAlimentoDesejado);
        boolean registou = this.pedidos.registaTroca(idPedido, idProduto, idAlimentoAtual, alimentoDesejado);

        return registou;
    }

    @Override
    public List<Produto> getProdutosPedido(long idPedido) {
        return this.pedidos.getProdutosPedido(idPedido);
    }

    @Override
    public Map<String, Alimento> getAlimentosItem(long idPedido, String idProduto) throws PedidoException {
        return this.pedidos.getAlimentosItem(idPedido, idProduto);
    }

    @Override
    public double apresentaTempoConfecao() {
        return this.gestao.apresentaTempoConfecao();
    }

    @Override
    public Map<String, Integer> apresentaStock() {
        return this.gestao.apresentaStock();
    }

    @Override
    public void enviaMensagem(String mensagem) {
        this.gestao.enviaMensagem(mensagem);
    }

    @Override
    public List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
            throws PedidoException {
        return this.pedidos.getSubstitutosDisponiveis(idPedido, idProduto, idAlimentoAtual);
    }

    @Override
    public void entregarPedido(long idPedido) {
        this.pedidos.entregarPedido(idPedido);
    }

    public String geraFatura(long idPedido) {
        return this.pedidos.geraFatura(idPedido);
    }

    public Alimento getAlimento(String idAlimento) {
        return this.gestao.getAlimento(idAlimento);
    }

    public boolean registaTrocaEmItemDoMenu(long idPedido, String idItemNoMenu, String idAlimentoAtual,
            String idAlimentoDesejado) throws PedidoException {
        // Usar PedidoDAO diretamente
        data.PedidoDAO pedidoDAO = data.PedidoDAO.getInstance();
        Pedido pedido = pedidoDAO.get(idPedido);

        if (pedido == null) {
            throw new PedidoException("Pedido não encontrado");
        }

        // Procurar o Item dentro dos Menus
        for (Produto produto : pedido.getProdutos()) {
            if (produto instanceof model.pedidos.Menu) {
                model.pedidos.Menu menu = (model.pedidos.Menu) produto;
                for (model.pedidos.Item item : menu.getItens()) {
                    if (item.getId().equals(idItemNoMenu)) {
                        // Encontrou o Item, fazer a troca
                        Alimento alimentoDesejado = this.gestao.getAlimento(idAlimentoDesejado);
                        if (alimentoDesejado == null) {
                            throw new PedidoException("Alimento desejado não encontrado");
                        }
                        item.registaTroca(idAlimentoAtual, alimentoDesejado);

                        // Guardar o Pedido na DAO
                        pedidoDAO.put(idPedido, pedido);
                        return true;
                    }
                }
            }
        }

        throw new PedidoException("Item " + idItemNoMenu + " não encontrado no Menu");
    }

    @Override
    public void encerrarPedido(long idPedido, String postoId) {
        this.preparacoes.encerrarPedido(idPedido, postoId);
    }

    @Override
    public void removerPedidoFila(long idPedido, List<Long> filaPedidos) {
        this.preparacoes.removerPedidoFila(idPedido, filaPedidos);
    }

    @Override
    public void requisitarIngredientes(long idPedido, String idPosto) {
        this.preparacoes.requisitarIngredientes(idPedido, idPosto);
    }

    @Override
    public void atrasarPedido(long idPedido, double tempoAtraso) {
        this.preparacoes.atrasarPedido(idPedido, tempoAtraso);
    }

    @Override
    public void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos) {
        this.preparacoes.atualizaFilaPedidos(idPedido, filaPedidos);
    }

    @Override
    public List<Pedido> getPedidosPorPagar() {
        return this.pedidos.getPedidosPorPagar();
    }

    @Override
    public List<Pedido> getPedidosEmPreparacao() {
        return this.pedidos.getPedidosEmPreparacao();
    }

    @Override
    public List<Pedido> getPedidosConcluidos() {
        return this.pedidos.getPedidosConcluidos();
    }

    @Override
    public List<Long> getPedidosConcluidosIds() {
        return this.pedidos.getPedidosConcluidosIds();
    }

    @Override
    public List<Long> getFilaPedidos() {
        return this.preparacoes.getFilaPedidos();
    }

    @Override
    public boolean autenticaFuncionario(long id, String password) {
        return this.preparacoes.autenticaFuncionario(id, password);
    }

    @Override
    public boolean funcionarioEAdmin(long id) {
        return this.preparacoes.funcionarioEAdmin(id);
    }

    @Override
    public boolean postoExiste(String postoId) {
        return this.preparacoes.postoExiste(postoId);
    }

    @Override
    public List<String> getPostosLivres() {
        return this.preparacoes.getPostosLivres();
    }

    @Override
    public boolean ocuparPosto(String postoId, long funcionarioId) {
        return this.preparacoes.ocuparPosto(postoId, funcionarioId);
    }

    @Override
    public void libertarPostoDeFuncionario(long funcionarioId) {
        this.preparacoes.libertarPostoDeFuncionario(funcionarioId);
    }

    @Override
    public void registaFuncionario(long id, String nome, String password, boolean admin) {
        this.preparacoes.registaFuncionario(id, nome, password, admin);
    }

    @Override
    public TipoPosto getTipoPosto(String postoId) {
        return this.preparacoes.getTipoPosto(postoId);
    }

    @Override
    public String getPostoDeFuncionario(long funcionarioId) {
        return this.preparacoes.getPostoDeFuncionario(funcionarioId);
    }

    @Override
    public boolean ingredientesSuficientes(long idPedido, String postoId) {
        return this.preparacoes.ingredientesSuficientes(idPedido, postoId);
    }

}
