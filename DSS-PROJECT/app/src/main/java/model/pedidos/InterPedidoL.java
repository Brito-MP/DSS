package model.pedidos;

import java.util.List;

import model.gestao.Alimento;

public interface InterPedidoL {

    long registaPedido(List<String> codigoProdutos, String nota, boolean tipo);

    void registaItem(String id, double preco, String nome, double tempoConfecaoEsperado);

    void validaPagamento(long idPedido);

    boolean registaTroca(String idProduto, String idAlimentoAtual, Alimento alimentoDesejado) throws PedidoException;

    void atualizaEstadoPedido(Long idPedido);
}
