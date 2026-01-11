package model;

import java.util.List;
import java.util.Map;

import model.gestao.Alimento;
import model.pedidos.Pedido;
import model.pedidos.PedidoException;
import model.pedidos.Produto;

public interface InterRestauranteL {

        public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo);

        public void validaPagamento(long idPedido);

        public boolean registaTroca(long idPedido, String idProduto, String idAlimentoAtual, String idAlimentoDesejado)
                        throws PedidoException;

        public double apresentaTempoConfecao();

        public Map<String, Integer> apresentaStock();

        public void enviaMensagem(String mensagem);

        public void entregarPedido(long idPedido);

        public String geraFatura(long idPedido);

        public void encerrarPedido(long idPedido, String postoId);

        public void removerPedidoFila(long idPedido, List<Long> filaPedidos);

        public void requisitarIngredientes(long idPedido, String idPosto);

        public void atrasarPedido(long idPedido, double tempoAtraso);

        public void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos);

        public List<Pedido> getPedidosPorPagar();

        public List<Pedido> getPedidosEmPreparacao();

        public List<Pedido> getPedidosConcluidos();

        public List<Long> getFilaPedidos();

        public List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
                        throws PedidoException;

        public List<Produto> getProdutosPedido(long idPedido);

        public Map<String, Alimento> getAlimentosItem(long idPedido, String idProduto) throws PedidoException;

}
