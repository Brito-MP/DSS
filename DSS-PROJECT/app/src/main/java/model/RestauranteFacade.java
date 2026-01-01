package model;

import java.util.List;

import model.gestao.Alimento;
import model.gestao.GestaoFacade;
import model.gestao.InterGestaoL;
import model.pedidos.InterPedidoL;
import model.pedidos.PedidoException;
import model.pedidos.PedidosFacade;
import model.preparacoes.InterPreparacoesL;
import model.preparacoes.PreparacoesFacade;


public class RestauranteFacade implements InterRestauranteL{
    private InterGestaoL gestao;
    private InterPreparacoesL preparacoes;
    private InterPedidoL pedidos;

    public RestauranteFacade (){
        this.pedidos = new PedidosFacade();
        this.gestao = new GestaoFacade();
        this.preparacoes = new PreparacoesFacade();
        
    }






    //======================================================================================
    //  Métodos do Sub-Sistema pedidos
    //======================================================================================
    @Override
    public long registaPedido(List<String> codigoProdutos, String nota, boolean tipo) {
        return this.pedidos.registaPedido(codigoProdutos, nota, tipo);
    }

    @Override
    public void validaPagamento(long idPedido){
        this.pedidos.validaPagamento(idPedido);
        //this.gestao.adicionaListaPedidos(idPedido); implementar na gestao
    }

    @Override
    public boolean registaTroca(String idProduto, String idAlimentoAtual, String idAlimentoDesejado) throws PedidoException{
        Alimento alimentoDesejado = this.gestao.getAlimento(idAlimentoDesejado);
        boolean registou = this.pedidos.registaTroca(idProduto, idAlimentoAtual, alimentoDesejado);
        
        return registou;
    }


}
