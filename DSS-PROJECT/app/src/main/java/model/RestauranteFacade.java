package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import data.DAOconfig;
import data.utilitiesDAO;
import model.gestao.Alimento;
import model.gestao.GestaoFacade;
import model.gestao.InterGestaoL;
import model.pedidos.InterPedidoL;
import model.pedidos.PedidoException;
import model.pedidos.PedidosFacade;
import model.preparacoes.InterPreparacoesL;
import model.preparacoes.PreparacoesFacade;

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

    //======================================================================================
    //  Métodos do Sub-Sistema pedidos
    //======================================================================================
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
    public boolean registaTroca(String idProduto, String idAlimentoAtual, String idAlimentoDesejado) throws PedidoException {
        Alimento alimentoDesejado = this.gestao.getAlimento(idAlimentoDesejado);
        boolean registou = this.pedidos.registaTroca(idProduto, idAlimentoAtual, alimentoDesejado);

        return registou;
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

}
