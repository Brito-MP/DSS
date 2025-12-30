package model.pedidos;

import model.gestao.Alimento;

public abstract class Produto {
    private String id;
    private double preco;
    private String nome;
    private double tempoConfecaoEsperado;

    public Produto() {
        this.id = "";
        this.preco = 0;
        this.nome = "";
    }

    public Produto(String id, double preco, String nome, double tempoConfecaoEsperado) {
        this.id = id;
        this.preco = preco;
        this.nome = nome;
        this.tempoConfecaoEsperado = tempoConfecaoEsperado;
    }

    @Override
    public abstract Produto clone();

    public String getId() {
        return this.id;
    }

    public double getPreco() {
        return this.preco;
    }

    public String getNome() {
        return this.nome;
    }

    public double getTempoConfecaoEsperado() {
        return this.tempoConfecaoEsperado;
    }

    public abstract void registaTroca(String idAlimentoAtual, Alimento alimentoDesejado) throws PedidoException;

    @Override
    public String toString() {
        return  " id: " + id +
                ", nome: " + nome +
                ", preco: " + preco +
                ", tempoConfecaoEsperado: " + tempoConfecaoEsperado + "\n";
    }
}
