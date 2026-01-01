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

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public PreparacoesFacade() {
        this.postos = PostoDAO.getInstance();
        this.filaPedidos = new ArrayList<>();
    }

    @Override
    public void encerrarPedido(long idPedido, String postoId) {
        PedidoDAO dao = PedidoDAO.getInstance();
        Pedido pedido = dao.get(idPedido);
        PostoDAO postoDAO = PostoDAO.getInstance();
        Posto posto = postoDAO.get(postoId);

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

        postoDAO.put(postoId, posto);
        pedido.setEstado(Estado.Concluido);
        dao.put(idPedido, pedido);
    }

    @Override
    public void removerPedidoFila(long idPedido, List<Long> filaPedidos) {
        PedidoDAO dao = PedidoDAO.getInstance();
        Pedido pedido = dao.get(idPedido);

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
        PedidoDAO pedDao = PedidoDAO.getInstance();
        Pedido pedido = pedDao.get(idPedido);

        if (pedido == null) {
            return;
        }

        PostoDAO postDao = PostoDAO.getInstance();
        Posto posto = postDao.get(idPosto);

        if (posto == null) {
            return;
        }

        AlimentoDAO alimDao = AlimentoDAO.getInstance();

        boolean atualizouPosto = false;

        for (Produto produto : pedido.getProdutos()) {
            if (produto instanceof Item) {
                atualizouPosto |= requisitarParaItem((Item) produto, posto, alimDao);
            } else if (produto instanceof Menu) {
                for (Item item : ((Menu) produto).getItens()) {
                    atualizouPosto |= requisitarParaItem(item, posto, alimDao);
                }
            }
        }

        if (atualizouPosto) {
            postDao.put(idPosto, posto);
        }

    }

    private boolean requisitarParaItem(Item item, Posto posto, AlimentoDAO alimDao) {
        boolean houveAtualizacao = false;

        for (Alimento alimento : item.getAlimentos().values()) {
            int requerido = alimento.getQuantidade();
            int emPosto = posto.getQuantidadeAlimento().getOrDefault(alimento.getId(), 0);

            if (emPosto >= requerido) {
                continue;
            }

            Alimento stockGlobal = alimDao.get(alimento.getId());
            int disponivel = stockGlobal != null ? stockGlobal.getQuantidade() : 0;

            int aTrazer = Math.min(20, disponivel);

            if (aTrazer > 0) {
                posto.getQuantidadeAlimento().put(alimento.getId(), emPosto + aTrazer);
                alimDao.put(alimento.getId(), new Alimento(disponivel - aTrazer, alimento.getId(), alimento.getNome()));
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
        PedidoDAO dao = PedidoDAO.getInstance();
        Pedido pedido = dao.get(idPedido);

        if (pedido == null) {
            return;
        }

        double novoTempo = pedido.getTempoConfecaoReal() + tempoAtraso;
        pedido.setTempoConfecaoReal(novoTempo);
        dao.put(idPedido, pedido);
    }

    @Override
    public void atualizaFilaPedidos(long idPedido, List<Long> filaPedidos) {
        PedidoDAO dao = PedidoDAO.getInstance();
        Pedido pedidoAtrasado = dao.get(idPedido);

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
            Pedido outroPedido = dao.get(outroId);
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

}
