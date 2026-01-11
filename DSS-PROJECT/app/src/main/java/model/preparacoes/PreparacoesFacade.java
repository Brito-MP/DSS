package model.preparacoes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.AlimentoDAO;
import data.FuncionarioDAO;
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
    private Map<String, Posto> postos; // idPosto -> Posto
    private final Map<Long, Funcionario> funcionarios; // idFuncionario-> Funcionario
    private Map<Long, Pedido> pedidos; // idPedido -> Pedido
    private Map<String, Alimento> alimentos; // idAlimento -> Alimento

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public PreparacoesFacade() {
        this.postos = PostoDAO.getInstance();
        this.pedidos = PedidoDAO.getInstance();
        this.alimentos = AlimentoDAO.getInstance();
        this.filaPedidos = new ArrayList<>();
        this.funcionarios = FuncionarioDAO.getInstance();
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

    @Override
    public boolean autenticaFuncionario(long id, String password) {
        Funcionario f = this.funcionarios.get(id);
        return f != null && f.getPassword().equals(password);
    }

    @Override
    public boolean funcionarioEAdmin(long id) {
        Funcionario f = this.funcionarios.get(id);
        return f != null && f.isAdmin();
    }

    @Override
    public boolean postoExiste(String postoId) {
        return this.postos.containsKey(postoId);
    }

    @Override
    public List<String> getPostosLivres() {
        List<String> livres = new ArrayList<>();
        for (Posto posto : this.postos.values()) {
            if (posto != null && posto.estaLivre()) {
                livres.add(posto.getId());
            }
        }
        return livres;
    }

    @Override
    public boolean ocuparPosto(String postoId, long funcionarioId) {
        Posto posto = this.postos.get(postoId);
        if (posto == null || !posto.estaLivre()) {
            return false;
        }
        // Impede que o mesmo funcionário ocupe múltiplos postos em simultâneo
        for (Posto p : this.postos.values()) {
            Long ocupadoPor = p != null ? p.getFuncionarioId() : null;
            if (ocupadoPor != null && ocupadoPor == funcionarioId) {
                return false;
            }
        }
        posto.setFuncionarioId(funcionarioId);
        this.postos.put(postoId, posto);
        return true;
    }

    @Override
    public void libertarPostoDeFuncionario(long funcionarioId) {
        for (Posto posto : this.postos.values()) {
            Long ocupadoPor = posto != null ? posto.getFuncionarioId() : null;
            if (ocupadoPor != null && ocupadoPor == funcionarioId) {
                posto.setFuncionarioId(null);
                this.postos.put(posto.getId(), posto); 
            }
        }
    }

    @Override
    public void registaFuncionario(long id, String nome, String password, boolean admin) {
        Perfil perfil = admin ? Perfil.ADMIN : Perfil.NORMAL;
        Funcionario f = new Funcionario(id, nome, password, perfil);
        this.funcionarios.put(id, f);
    }

    @Override
    public TipoPosto getTipoPosto(String postoId) {
        Posto posto = this.postos.get(postoId);
        return posto != null ? posto.getTipo() : null;
    }

}
