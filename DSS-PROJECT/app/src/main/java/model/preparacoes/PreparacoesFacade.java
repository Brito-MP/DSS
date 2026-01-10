package model.preparacoes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.AlimentoDAO;
import data.PedidoDAO;
import data.PostoDAO;
import model.gestao.Alimento;
import model.pedidos.Estado;
import model.pedidos.Item;
import model.pedidos.Menu;
import model.pedidos.Pedido;
import model.pedidos.Produto;

public class PreparacoesFacade implements InterPreparacoesL {

    private List<Long> filaPedidos;
    private final Map<String, Posto> postos; // idPosto -> Posto
    private final Map<Long, Pedido> pedidos; // idPedido -> Pedido
    private final Map<String, Alimento> alimentos; // idAlimento -> Alimento

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public PreparacoesFacade() {
        this.postos = PostoDAO.getInstance();
        this.pedidos = PedidoDAO.getInstance();
        this.alimentos = AlimentoDAO.getInstance();
        this.filaPedidos = new ArrayList<>();
    }

    @Override
    public void encerrarPedido(long idPedido, String postoId) {
        Pedido pedido = this.pedidos.get(idPedido);
        Posto posto = this.postos.get(postoId);

        if (pedido == null || posto == null) {
            return;
        }

        // Desconta ingredientes usados pelo pedido no posto responsável.
        for (Produto produto : pedido.getProdutos()) {
            if (produto instanceof Item) {
                consumirParaItem((Item) produto, posto);
            } else if (produto instanceof Menu) {
                for (Item item : ((Menu) produto).getItens()) {
                    consumirParaItem(item, posto);
                }
            }
        }

        this.postos.put(postoId, posto);
        pedido.setEstado(Estado.Concluido);
        removerPedidoFila(idPedido, filaPedidos);
        this.pedidos.put(idPedido, pedido);
    }

    @Override
    public void removerPedidoFila(long idPedido, List<Long> filaPedidos) {
        Pedido pedido = this.pedidos.get(idPedido);

        if (pedido == null) {
            return;
        }

        if (pedido.getEstado() == Estado.Concluido) {
            filaPedidos.remove(idPedido);
        }
    }

    @Override
    public void adicionaListaPedidos(long idPedido) {
        if (this.filaPedidos == null) {
            this.filaPedidos = new ArrayList<>();
        }
        if (!this.filaPedidos.contains(idPedido)) {
            this.filaPedidos.add(idPedido);
        }
    }

    @Override
    public void requisitarIngredientes(long idPedido, String idPosto) {
        Pedido pedido = this.pedidos.get(idPedido);

        if (pedido == null) {
            return;
        }

        Posto posto = this.postos.get(idPosto);

        if (posto == null) {
            return;
        }

        boolean atualizouPosto = false;

        for (Produto produto : pedido.getProdutos()) {
            if (produto instanceof Item) {
                atualizouPosto |= requisitarParaItem((Item) produto, posto);
            } else if (produto instanceof Menu) {
                for (Item item : ((Menu) produto).getItens()) {
                    atualizouPosto |= requisitarParaItem(item, posto);
                }
            }
        }

        if (atualizouPosto) {
            this.postos.put(idPosto, posto);
        }

    }

    private boolean requisitarParaItem(Item item, Posto posto) {
        boolean houveAtualizacao = false;

        for (Alimento alimento : item.getAlimentos().values()) {
            int requerido = alimento.getQuantidade();
            int emPosto = posto.getQuantidadeAlimento().getOrDefault(alimento.getId(), 0);

            if (emPosto >= requerido) {
                continue;
            }

            Alimento stockGlobal = this.alimentos.get(alimento.getId());
            int disponivel = stockGlobal != null ? stockGlobal.getQuantidade() : 0;

            int aTrazer = Math.min(20, disponivel);

            if (aTrazer > 0) {
                posto.getQuantidadeAlimento().put(alimento.getId(), emPosto + aTrazer);
                this.alimentos.put(alimento.getId(),
                        new Alimento(disponivel - aTrazer, alimento.getId(), alimento.getNome()));
                houveAtualizacao = true;
            }
        }

        return houveAtualizacao;
    }

    private void consumirParaItem(Item item, Posto posto) {
        Map<String, Integer> stock = posto.getQuantidadeAlimento();

        for (Alimento alimento : item.getAlimentos().values()) {
            int atual = stock.getOrDefault(alimento.getId(), 0);
            int novo = Math.max(0, atual - alimento.getQuantidade());
            stock.put(alimento.getId(), novo);
        }
    }

    @Override
    public void atrasarPedido(long idPedido, double tempoAtraso) {
        Pedido pedido = this.pedidos.get(idPedido);

        if (pedido == null) {
            return;
        }

        double novoTempo = pedido.getTempoConfecaoReal() + tempoAtraso;
        pedido.setTempoConfecaoReal(novoTempo);
        this.pedidos.put(idPedido, pedido);
    }

    @Override
    public void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos) {
        Pedido pedidoAtrasado = this.pedidos.get(idPedido);

        if (pedidoAtrasado == null || filaPedidos == null) {
            return;
        }

        double tempoAtraso = pedidoAtrasado.getTempoConfecaoReal() - pedidoAtrasado.getTempoConfecaoEsperado();

        if (tempoAtraso <= 0 || !filaPedidos.remove(idPedido)) {
            return;
        }

        List<Long> novaFila = new ArrayList<>();
        List<Long> restantes = new ArrayList<>();
        double tempoOcupado = 0;

        for (Long outroId : filaPedidos) {
            Pedido outroPedido = this.pedidos.get(outroId);
            double tempoPedido = outroPedido != null ? outroPedido.getTempoConfecaoEsperado() : 0;

            if (tempoOcupado + tempoPedido <= tempoAtraso) {
                novaFila.add(outroId);
                tempoOcupado += tempoPedido;
            } else {
                restantes.add(outroId);
            }
        }

        novaFila.add(idPedido);
        novaFila.addAll(restantes);

        filaPedidos.clear();
        filaPedidos.addAll(novaFila);

    }

    @Override
    public List<Long> getFilaPedidos() {
        if (this.filaPedidos == null) {
            this.filaPedidos = new ArrayList<>();
        }
        return new ArrayList<>(this.filaPedidos);
    }

}
