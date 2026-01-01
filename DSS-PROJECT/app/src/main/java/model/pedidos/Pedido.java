package model.pedidos;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private static long IdCounter;
    private long idInstance;
    private double tempoConfecaoEsperado;
    private double tempoConfecaoReal; // vai ser incrementado pelo funcionario se houver atraso
    // private TalaoPagamento talaoPagamento; acho que n é necessario, o toString faz isto
    private String nota; // ?? metemos List para suportar varias notas ??
    private List<Produto> produtos;
    private Estado estado; // PorPagar (nao sei se e necessario este estado porque o pedido nao entra na BD antes de ser pago), EmPreparacao, Concluido, Entregue 
    private double preco;
    private boolean tipo; // true -> restaurante; false -> takeaway

    // ====================================================================================================
    //  CONSTRUTORES
    // ====================================================================================================
    public Pedido() {
        this.idInstance = Pedido.IdCounter++;
        this.tempoConfecaoEsperado = 0;
        this.tempoConfecaoReal = 0;
        this.nota = "";
        this.produtos = new ArrayList<>();
        this.estado = Estado.PorPagar;
        this.preco = 0;
        this.tipo = true;
    }

    public Pedido(List<Produto> produtosSelecionados, String nota, boolean tipo, double preco, double tempoConfecaoEsperado) {
        this();
        for (Produto p : produtosSelecionados) {
            this.produtos.add(p.clone());
            this.tempoConfecaoEsperado += p.getTempoConfecaoEsperado();
            this.preco += p.getPreco();

        }
        this.tempoConfecaoReal = this.tempoConfecaoEsperado;
        this.nota = nota;
        this.tipo = tipo;
        this.preco = preco;
        this.tempoConfecaoEsperado = tempoConfecaoEsperado;
    }

    // ====================================================================================================
    // GETTERS E SETTERS
    // ====================================================================================================
    public Long getIdCounter() {
        return this.idInstance;
    }

    public void setId(long id) {
        this.idInstance = id;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setTempoConfecaoEsperado(double tempoConfecaoEsperado) {
        this.tempoConfecaoEsperado = tempoConfecaoEsperado;
    }

    public void setTempoConfecaoReal(double tempoConfecaoReal) {
        this.tempoConfecaoReal = tempoConfecaoReal;
    }

    public void setTipo(boolean tipo) {
        this.tipo = tipo;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public Estado getEstado() {
        return this.estado;
    }

    public String getNota() {
        return this.nota;
    }

    public double getPreco() {
        return this.preco;
    }

    public double getTempoConfecaoEsperado() {
        return this.tempoConfecaoEsperado;
    }

    public double getTempoConfecaoReal() {
        return this.tempoConfecaoReal;
    }

    public boolean getTipo() {
        return this.tipo;
    }

    public List<Produto> getProdutos() {
        return this.produtos;
    }

    // ====================================================================================================
    // MÉTODOS
    // ====================================================================================================
    public void pagamentoConcluido() {
        this.estado = Estado.EmPreparacao;
    }

    // ====================================================================================================
    // TOSTRING CLONE EQUALS
    // ====================================================================================================

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
                ", \n tipo: " + (this.tipo ? "Restaurante" : "TakeAway") +
                ",\n produtos: " + produtos.toString() + "\n";
    }

}
