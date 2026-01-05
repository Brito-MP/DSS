package model.gestao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import data.AlimentoDAO;
import data.PedidoDAO;
import model.pedidos.Pedido;

public class GestaoFacade implements InterGestaoL {
    Map<String, Alimento> alimentos;
    Map<Long, Pedido> pedidos;

    // ====================================================================================================
    // CONSTRUTORES
    // ====================================================================================================
    public GestaoFacade() {
        this.alimentos = AlimentoDAO.getInstance();
        this.pedidos = PedidoDAO.getInstance();
    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================

    @Override
    public Alimento getAlimento(String idAlimento) {
        Alimento a = this.alimentos.get(idAlimento);
        return a != null ? a.clone() : null;
    }

    @Override
    public Collection<Alimento> getAlimentos() {
        Collection<Alimento> copia = new ArrayList<>();
        for (Alimento alimento : this.alimentos.values()) {
            if (alimento != null) {
                copia.add(alimento.clone());
            }
        }
        return copia;
    }

    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    @Override
    public double apresentaTempoConfecao() {
        Collection<Pedido> todosPedidos = this.pedidos.values();
        double somaTempos = 0;
        int totalPedidos = 0;
        double media = 0;

        for (Pedido pedido : todosPedidos) {
            if (pedido == null) {
                continue;
            }
            // Consideramos apenas o restaurante atual (assume-se um único restaurante)
            somaTempos += pedido.getTempoConfecaoReal();
            totalPedidos++;
        }
        
        media = totalPedidos > 0 ? somaTempos / totalPedidos : 0;
        return media;
    }

    @Override
    public Map<String, Integer> apresentaStock(){
        Map<String, Integer> stockAtual = new HashMap<>();

        Collection<Alimento> listaAlimentos = this.getAlimentos();
        for (Alimento alimento : listaAlimentos) {
            if (alimento == null) {
                continue;
            }
            stockAtual.put(alimento.getId(), alimento.getQuantidade());
        }

        return stockAtual;
    }

    @Override
    public void enviaMensagem(String mensagem) {
        // Placeholder para integração futura com sistema de mensagens
        // (ex: fila de mensagens, email, push notification)
        // Neste momento assume-se um único restaurante ativo.
        if (mensagem == null || mensagem.isEmpty()) {
            return;
        }
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================
}
