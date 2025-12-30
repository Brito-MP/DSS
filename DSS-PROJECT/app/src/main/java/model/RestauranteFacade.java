package model;

import java.util.List;

import model.preparacoes.InterPreparacoesL;
import model.gestao.Alimento;
import model.gestao.InterGestaoL;
import model.pedidos.InterPedidoL;
import model.pedidos.PedidoException;
import model.pedidos.PedidosFacade;


public class RestauranteFacade implements InterRestauranteL{
    private InterGestaoL gestao;
    private InterPreparacoesL preparacoes;
    private InterPedidoL pedidos;

    public RestauranteFacade (){
        this.gestao = null;
        this.preparacoes = null;
        this.pedidos = new PedidosFacade();
    }






    //======================================================================================
    //  Métodos do Sub-Sistema pedidos
    //======================================================================================
    @Override
    public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo) {
        return this.pedidos.registaPedido(codigoProdutos, nota, tipo);
    }

    @Override
    public void registaItem(String id, double preco, String nome, float tempoConfecaoEsperado){
        this.pedidos.registaItem(id,preco,nome, tempoConfecaoEsperado);
    }

    @Override
    public void validaPagamento(long idPedido){
        this.pedidos.validaPagamento(idPedido);
    }

    @Override
    public boolean registaTroca(String idProduto, String idAlimentoAtual, String idAlimentoDesejado) throws PedidoException{
        Alimento alimentoDesejado = this.gestao.getAlimento(idAlimentoDesejado);
        boolean registou = this.pedidos.registaTroca(idProduto, idAlimentoAtual, alimentoDesejado);
        return registou;
    }


}
