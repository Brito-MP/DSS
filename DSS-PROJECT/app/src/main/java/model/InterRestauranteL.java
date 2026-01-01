package model;

import java.util.List;

import model.pedidos.PedidoException;


public interface InterRestauranteL {
    public long registaPedido(List <String> codigoProdutos, String nota, boolean tipo); //return codigoPedidoRegistado
    public void validaPagamento(long idPedido); // Precisamos de uma classe Pagamento ?? Não podemos usar este método tanto para pagar MBWay como para pagar balcão??
    public boolean registaTroca(String idProduto, String idAlimentoAtual, String idAlimentoDesejado) throws PedidoException;    


















}
