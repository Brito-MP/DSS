package model;

import java.util.List;
import java.util.Map;

import model.preparacoes.InterPreparacoesL;
import model.gestao.Alimento;
import model.gestao.GestaoFacade;
import model.gestao.InterGestaoL;
import model.pedidos.InterPedidoL;
import model.pedidos.PedidoException;
import model.pedidos.PedidosFacade;
import model.pedidos.Produto;

public class RestauranteFacade implements InterRestauranteL {
    private InterGestaoL gestao;
    private InterPreparacoesL preparacoes;
    private InterPedidoL pedidos;

    public RestauranteFacade() {
        this.pedidos = new PedidosFacade();
        this.gestao = new GestaoFacade();
        this.preparacoes = null;

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
        // this.gestao.adicionaListaPedidos(idPedido); implementar na gestao
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
    public List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
            throws PedidoException {
        return this.pedidos.getSubstitutosDisponiveis(idPedido, idProduto, idAlimentoAtual);
    }

}
