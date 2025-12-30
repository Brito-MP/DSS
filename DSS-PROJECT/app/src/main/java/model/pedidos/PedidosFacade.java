package model.pedidos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.gestao.Alimento;

public class PedidosFacade implements InterPedidoL {
    private Map<String, Produto> produtos;
    private Map<Long, Pedido> pedidos;
    //private Map<Long, Pedido> pedidos;

    public PedidosFacade() {
        this.produtos = new HashMap<>();
        this.pedidos = new HashMap<>();
        this.pedidos = new HashMap<>();

    }

    @Override
    public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo) {
        List<Produto> produtosSelecionados = new ArrayList<>();
        for (String codigo : codigoProdutos) {
            Produto produto = this.produtos.get(codigo);
            if (produto != null) {
                produtosSelecionados.add(produto);
            }
        }
        Pedido pedido = new Pedido(produtosSelecionados, nota, tipo);
        this.pedidos.put(pedido.getId(), pedido);
        System.out.println(pedidos);
        System.out.println("=========================================");
        return pedido.getId();
    }

    @Override
    public void registaItem(String id, double preco, String nome, double tempoConfecaoEsperado) {
        Item item = new Item(id, preco, nome, tempoConfecaoEsperado);
        this.produtos.put(id,item);
    }

    @Override
    public void validaPagamento(long idPedido){
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido != null) {
            pedido.pagamentoConcluido();
            System.out.println("Pagamento validado para o pedido: " + idPedido);
        } else {
            System.out.println("Pedido não encontrado: " + idPedido);
        }
        System.out.println(pedido);
    }

    @Override
    public boolean registaTroca(String idProduto, String idAlimentoAtual, Alimento alimentoDesejado) throws PedidoException{
        Produto produto = this.produtos.get(idProduto);
        produto.registaTroca(idAlimentoAtual, alimentoDesejado);
        return true;
    }
}