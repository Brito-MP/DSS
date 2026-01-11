package model.pedidos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.PedidoDAO;
import data.ProdutoDAO;
import model.gestao.Alimento;
import model.pedidos.Estado;

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
    public List<Pedido> getPedidosPorPagar() {
        List<Pedido> pedidos = ((PedidoDAO) this.pedidos).getPedidosPorEstado(Estado.PorPagar);
        List<Pedido> clones = new ArrayList<>();
        for (Pedido p : pedidos) {
            clones.add(p.clone());
        }
        return clones;
    }

    @Override
    public List<Pedido> getPedidosEmPreparacao() {
        List<Pedido> pedidos = ((PedidoDAO) this.pedidos).getPedidosPorEstado(Estado.EmPreparacao);
        List<Pedido> clones = new ArrayList<>();
        for (Pedido p : pedidos) {
            clones.add(p.clone());
        }
        return clones;
    }

    @Override
    public List<Pedido> getPedidosConcluidos() {
        List<Pedido> pedidos = ((PedidoDAO) this.pedidos).getPedidosPorEstado(Estado.Concluido);
        List<Pedido> clones = new ArrayList<>();
        for (Pedido p : pedidos) {
            clones.add(p.clone());
        }
        return clones;
    }

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
                Produto produto = this.produtos.get(codigo);
                if (produto != null) {
                    produtosSelecionados.add(produto);
                    precoTotal += produto.getPreco();
                    tempoTotal += produto.getTempoConfecaoEsperado();
                }
            } catch (Exception e) {
                // Ignorar produtos não encontrados
            }
        }

        Pedido pedido = new Pedido(produtosSelecionados, nota, tipo, precoTotal, tempoTotal);

        this.pedidos.put(pedido.getIdCounter(), pedido);

        return pedido.getIdCounter();
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
    public void validaPagamento(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);

        pedido.pagamentoConcluido();
        this.pedidos.put(idPedido, pedido);
    }

    @Override
    public void entregarPedido(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);

        if (pedido == null) {
            return;
        }

        pedido.setEstado(Estado.Entregue);
        this.pedidos.put(idPedido, pedido);
    }

    @Override
    public String geraFatura(long idPedido) {
        Pedido pedido = this.pedidos.get(idPedido);
        if (pedido == null) {
            return "";
        }

        StringBuilder fatura = new StringBuilder();
        fatura.append("\n========== FATURA ==========\n");
        fatura.append("Pedido #").append(pedido.getIdCounter()).append("\n");
        fatura.append("Tipo: ").append(pedido.getTipo() ? "Restaurante" : "Take Away").append("\n");
        fatura.append("---\n");

        for (Produto produto : pedido.getProdutos()) {
            fatura.append("• ").append(produto.getNome())
                    .append(" - €").append(String.format("%.2f", produto.getPreco())).append("\n");
        }

        fatura.append("---\n");
        fatura.append("Total: €").append(String.format("%.2f", pedido.getPreco())).append("\n");
        fatura.append("Tempo estimado: ").append((int) pedido.getTempoConfecaoEsperado()).append(" min\n");

        if (pedido.getNota() != null && !pedido.getNota().isEmpty()) {
            fatura.append("Nota: ").append(pedido.getNota()).append("\n");
        }

        fatura.append("===========================\n");
        return fatura.toString();
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
}
