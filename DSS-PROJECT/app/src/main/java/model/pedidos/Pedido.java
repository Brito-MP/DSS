package model.pedidos;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private static long Id;
    private long idInstance;
    private double tempoConfecaoEsperado;
    private double tempoConfecaoReal; // ??????????????????
    // private TalaoPagamento talaoPagamento;
    private String nota; // ?? Nota como string ou Class ??
    private List<Produto> produtos;
    private Estado estado;
    private double preco;
    private boolean tipo; // true -> restaurante; false -> takeaway
    private String tipoString;

    public Pedido() {
        this.idInstance = Pedido.Id++;
        this.tempoConfecaoEsperado = 0;
        this.tempoConfecaoReal = 0;
        this.nota = "";
        this.produtos = new ArrayList<>();
        this.estado = Estado.PorPagar;
        this.preco = 0;
        this.tipo = true;
        this.tipoString = "Restaurante";
    }

    public Pedido(List<Produto> produtosSelecionados, String nota, boolean tipo) {
        this();
        for (Produto p : produtosSelecionados) {
            this.produtos.add(p.clone());
            this.tempoConfecaoEsperado += p.getTempoConfecaoEsperado();
            this.preco += p.getPreco();

        }
        this.tempoConfecaoReal = this.tempoConfecaoEsperado;
        this.nota = nota;
        this.tipo = tipo;
        this.tipoString = this.tipo ? "Restaurante" : "TakeAway";
    }

    public Long getId() {
        return this.idInstance;
    }

    public void pagamentoConcluido() {
        this.estado = Estado.Concluido;
    }

    public Pedido clone() {
        Pedido cloned = new Pedido();
        cloned.idInstance = this.idInstance;
        cloned.estado = this.estado;
        cloned.nota = this.nota;
        cloned.preco = this.preco;
        cloned.tempoConfecaoEsperado = this.tempoConfecaoEsperado;
        cloned.tempoConfecaoReal = this.tempoConfecaoReal;
        cloned.produtos = new ArrayList<>(this.produtos); // ?? clonar os objetos 1 a 1 ??
        return cloned;
    }

    @Override
    public String toString() { 
        return "\nPedido\n{id: " + idInstance +
                ",\n estado: " + estado +
                ",\n nota: " + nota +
                ",\n preço: " + preco +
                ",\n tempoConfecaoEsperado: " + tempoConfecaoEsperado +
                ",\n tempoConfecaoReal: " + tempoConfecaoReal +
                ", \n tipo: " + tipoString +
                ",\n produtos: " + produtos.toString() + "\n";
    }

}
