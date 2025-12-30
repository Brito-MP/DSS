package model.pedidos;

import java.util.List;

import model.gestao.Alimento;

public class Menu extends Produto {
    private List<Item> itens;

    public Menu(String id, double preco, String nome, double tempoConfecaoEsperado, List<Item> itens) {
        super(id, preco, nome, tempoConfecaoEsperado);
        for (Item t : itens) {
            this.itens.add(t);
        }
    }

    @Override
    public Menu clone() {
        return new Menu(super.getId(), super.getPreco(), super.getNome(), super.getTempoConfecaoEsperado(), this.itens);
    }

    public void registaTroca(String idAlimentoAtual, Alimento alimentoDesejado) throws PedidoException{
        int count = 0;
        for (Item item : this.itens){
            try {
                item.registaTroca(idAlimentoAtual, alimentoDesejado);
            } catch (PedidoException e){
                count++;
            }
        }
        if(count == this.itens.size()) throw new PedidoException("Erro no regista troca do pedidos/Menu.java");
    }

}
