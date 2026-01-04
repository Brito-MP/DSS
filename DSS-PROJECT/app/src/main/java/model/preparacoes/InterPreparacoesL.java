package model.preparacoes;

import java.util.List;

public interface InterPreparacoesL {

    void encerrarPedido(long idPedido, String postoId);

    void removerPedidoFila(long idPedido, List<Long> filaPedidos);

    void adicionaListaPedidos(long idPedido);

    void requisitarIngredientes(long idPedido, String idPosto);

    void atrasarPedido(long idPedido, double tempoAtraso);

    void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos);

    List<Long> getFilaPedidos();
}
