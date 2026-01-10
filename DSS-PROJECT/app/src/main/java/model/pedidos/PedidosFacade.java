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

        double precoTotal = 0;
        double tempoTotal = 0;

        for (String codigo : codigoProdutos) {
            try {
                // Buscar o produto da BD usando this.produtos
                Produto produto = this.produtos.get(codigo);
                if (produto != null) {
                    produtosSelecionados.add(produto);
                    // Calcular preço e tempo totais
                    precoTotal += produto.getPreco();
                    tempoTotal += produto.getTempoConfecaoEsperado();
                } else {
                    System.out.println("⚠ Produto não encontrado: " + codigo); // apagar no futuro
                }
            } catch (Exception e) {
                System.out.println("❌ Erro ao buscar produto " + codigo + ": " + e.getMessage()); // apagar n no futuro
                e.printStackTrace();
            }
        }

        Pedido pedido = new Pedido(produtosSelecionados, nota, tipo, precoTotal, tempoTotal);

        this.pedidos.put(pedido.getIdCounter(), pedido);

        return pedido.getIdCounter();
    }

    /*
     * @Override
     * public void registaItem(String id, double preco, String nome, double
     * tempoConfecaoEsperado) {
     * Item item = new Item(id, preco, nome, tempoConfecaoEsperado);
     * this.produtos.put(id, item);
     * }
     */
    @Override
    public void validaPagamento(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);

        pedido.pagamentoConcluido();
        this.pedidos.put(idPedido, pedido);
    }

    @Override
    public boolean registaTroca(long idPedido, String idProduto, String idAlimentoAtual, Alimento alimentoDesejado)
            throws PedidoException {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido != null) {
            // Delegar a troca para o pedido (composição)
            pedido.registaTroca(idProduto, idAlimentoAtual, alimentoDesejado);
            // Persistir as alterações no pedido
            this.pedidos.put(idPedido, pedido);
            return true;
        }
        throw new PedidoException("Pedido " + idPedido + " não encontrado");
    }

    @Override
    public List<Produto> getProdutosPedido(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido != null) {
            return pedido.getProdutos();
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Alimento> getAlimentosItem(long idPedido, String idProduto) throws PedidoException {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido == null) {
            throw new PedidoException("Pedido " + idPedido + " não encontrado");
        }

        // Procurar o produto no pedido
        for (Produto produto : pedido.getProdutos()) {
            if (produto.getId().equals(idProduto) && produto instanceof Item) {
                Item item = (Item) produto;
                return item.getAlimentos();
            }
        }
        throw new PedidoException("Produto " + idProduto + " não encontrado ou não é um Item");
    }

    @Override
    public List<String> getSubstitutosDisponiveis(long idPedido, String idProduto, String idAlimentoAtual)
            throws PedidoException {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido == null) {
            throw new PedidoException("Pedido " + idPedido + " não encontrado");
        }

        // Procurar o produto no pedido
        for (Produto produto : pedido.getProdutos()) {
            if (produto.getId().equals(idProduto) && produto instanceof Item) {
                Item item = (Item) produto;
                Map<String, List<String>> trocas = item.getTrocas();
                if (trocas.containsKey(idAlimentoAtual)) {
                    return trocas.get(idAlimentoAtual);
                }
                throw new PedidoException("Não existem trocas disponíveis para o alimento " + idAlimentoAtual);
            }
        }
        throw new PedidoException("Produto " + idProduto + " não encontrado ou não é um Item");
    }

    @Override
    public void entregarPedido(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);

        if (pedido == null) {
            System.out.println("✗ Pedido " + idPedido + " não encontrado para entrega.");
            return;
        }

        pedido.setEstado(Estado.Entregue);
        this.pedidos.put(idPedido, pedido);
    }

    /*
     * @Override
     * public void atualizaEstadoPedido(Long idPedido) {
     * Pedido pedido = pedidos.get(idPedido);
     * 
     * if (pedido != null) {
     * pedido.pedidoEntregue();
     * pedidos.put(idPedido, pedido);
     * 
     * System.out.println("Pedido " + idPedido +
     * " foi entregue e o estado foi atualizado para: " + pedido.getEstado()); //
     * apagar no futuro???
     * } else {
     * System.out.println("Pedido com ID " + idPedido + " não encontrado.");//
     * apagar no futuro
     * }
     * }
     */

    @Override
    public List<Pedido> getPedidosPorPagar() {
        return this.getPedidosPorEstado(Estado.PorPagar);
    }

    @Override
    public List<Pedido> getPedidosEmPreparacao() {
        return this.getPedidosPorEstado(Estado.EmPreparacao);
    }

    @Override
    public List<Pedido> getPedidosConcluidos() {
        return this.getPedidosPorEstado(Estado.Concluido);
    }

    @Override
    public List<Long> getPedidosConcluidosIds() {
        List<Long> ids = new ArrayList<>();
        for (Pedido p : this.pedidos.values()) {
            if (p.getEstado() == Estado.Concluido) {
                ids.add(p.getIdCounter());
            }
        }
        return ids;
    }

    private List<Pedido> getPedidosPorEstado(Estado estado) {
        List<Pedido> clones = new ArrayList<>();
        for (Pedido p : this.pedidos.values()) {
            if (p.getEstado() == estado) {
                clones.add(p.clone());
            }
        }
        return clones;
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
}
