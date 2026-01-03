package model.pedidos;

import java.util.List;
import java.util.Map;

import model.gestao.Alimento;

public interface InterPedidoL {

    long registaPedido(List<String> codigoProdutos, String nota, boolean tipo);

    // void registaItem(String id, double preco, String nome, double
    // tempoConfecaoEsperado);

    void validaPagamento(long idPedido);

    boolean registaTroca(long idPedido, String idProduto, String idAlimentoAtual, Alimento alimentoDesejado)
            throws PedidoException;

    List<Produto> getProdutosPedido(long idPedido);

    Map<String, Alimento> getAlimentosItem(long idPedido, String idProduto) throws PedidoException;

    List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
            throws PedidoException;

    void atualizaEstadoPedido(Long idPedido);
}
