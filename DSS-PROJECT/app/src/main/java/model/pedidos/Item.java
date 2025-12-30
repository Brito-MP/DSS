package model.pedidos;

import java.util.List;
import java.util.Map;

import model.gestao.Alimento;

public class Item extends Produto {
    private Map<String,Alimento> alimentos;
    private Map<String, List<String>> trocas; // <idAlimento que tenho, id alimento que posso trocar>

    public Item(){
        super();
    }

    public Item(String id, double preco, String nome, double tempoConfecaoEsperado){
        super(id,preco,nome, tempoConfecaoEsperado);
    }

    
    public void registaTroca(String idAlimentoAtual, Alimento alimentoDesejado) throws PedidoException{
        if(!this.trocas.containsKey(idAlimentoAtual)) throw new PedidoException("O id: " + idAlimentoAtual + " não existe como entrada no map de trocas (registaTroca - pedidos/Item.java)");
        List<String> trocasDisponiveis = this.trocas.get(idAlimentoAtual);
        
        if(!trocasDisponiveis.contains(alimentoDesejado.getId())) throw new PedidoException("O id: " + alimentoDesejado.getId() + " não é um id válido na lista dos alimentos disponíveis para troca do id: " + idAlimentoAtual + "registaTroca - pedidos/Item.java");

        this.alimentos.remove(idAlimentoAtual);
        this.alimentos.put(alimentoDesejado.getId(), alimentoDesejado);
    }

    @Override
    public Item clone() {
        return new Item(super.getId(), super.getPreco(), super.getNome(), super.getTempoConfecaoEsperado());

    }
    
}
