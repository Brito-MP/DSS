package model.preparacoes;

import java.util.List;

import model.preparacoes.TipoPosto;

public interface InterPreparacoesL {

    void encerrarPedido(long idPedido, String postoId);

    void removerPedidoFila(long idPedido, List<Long> filaPedidos);

    void adicionaListaPedidos(long idPedido);

    void requisitarIngredientes(long idPedido, String idPosto);

    void atrasarPedido(long idPedido, double tempoAtraso);

    void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos);

    List<Long> getFilaPedidos();

    boolean autenticaFuncionario(long id, String password);

    boolean funcionarioEAdmin(long id);

    boolean postoExiste(String postoId);

    List<String> getPostosLivres();

    boolean ocuparPosto(String postoId, long funcionarioId);

    void libertarPostoDeFuncionario(long funcionarioId);

    void registaFuncionario(long id, String nome, String password, boolean admin);

    TipoPosto getTipoPosto(String postoId);
}
