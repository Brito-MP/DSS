package model.pedidos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.PedidoDAO;
import data.ProdutoDAO;
import model.gestao.Alimento;

public class PedidosFacade implements InterPedidoL {
    private Map<String, Produto> produtos;
    private Map<Long, Pedido> pedidos;
    // private Map<Long, Pedido> pedidos;

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public PedidosFacade() {
        this.pedidos = PedidoDAO.getInstance();
        this.produtos = ProdutoDAO.getInstance();
        

    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================

    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    @Override
    public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo) {
        List<Produto> produtosSelecionados = new ArrayList<>();
        ProdutoDAO dao = ProdutoDAO.getInstance();

        double precoTotal = 0;
        double tempoTotal = 0;

        for (String codigo : codigoProdutos) {
            try {
                // Buscar o produto da BD usando ProdutoDAO
                Produto produto = dao.get(codigo);
                if (produto != null) {
                    produtosSelecionados.add(produto);
                    // Calcular preço e tempo totais
                    precoTotal += produto.getPreco();
                    tempoTotal += produto.getTempoConfecaoEsperado();
                } else {
                    System.out.println("⚠ Produto não encontrado: " + codigo); //apagar no futuro
                }
            } catch (Exception e) {
                System.out.println("❌ Erro ao buscar produto " + codigo + ": " + e.getMessage()); //apagar n no futuro
                e.printStackTrace();
            }
        }

        Pedido pedido = new Pedido(produtosSelecionados, nota, tipo, precoTotal, tempoTotal);

        this.pedidos.put(pedido.getIdCounter(), pedido);

        return pedido.getIdCounter();
    }

    @Override
    public void registaItem(String id, double preco, String nome, double tempoConfecaoEsperado) {
        Item item = new Item(id, preco, nome, tempoConfecaoEsperado);
        this.produtos.put(id, item);
    }

    @Override
    public void validaPagamento(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido != null) {
            pedido.pagamentoConcluido();
            this.pedidos.put(idPedido, pedido);
            System.out.println("Pagamento validado para o pedido: " + idPedido); // apagar no futuro
        } else {
            System.out.println("Pedido não encontrado: " + idPedido); // apagar no futuro
        }
        System.out.println(pedido); // apagar no futuro
    }

    @Override
    public boolean registaTroca(String idProduto, String idAlimentoAtual, Alimento alimentoDesejado)
            throws PedidoException {
        Produto produto = this.produtos.get(idProduto);
        if (produto != null) {
            produto.registaTroca(idAlimentoAtual, alimentoDesejado);
            this.produtos.put(idProduto, produto);
            return true;
        }
        return false;
    }



    @Override
    public void atualizaEstadoPedido(Long idPedido) {
            Pedido pedido = pedidos.get(idPedido);
            
            if (pedido != null) {
                pedido.pedidoEntregue();
                pedidos.put(idPedido, pedido);

                System.out.println("Pedido " + idPedido + " foi entregue e o estado foi atualizado para: " + pedido.getEstado()); // apagar no futuro???
            } else {
                System.out.println("Pedido com ID " + idPedido + " não encontrado.");// apagar no futuro 
            }
        }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
}



