package model.gestao;

import java.util.Collection;
import java.util.Map;

public interface InterGestaoL {
    public Alimento getAlimento(String idAlimento);
    public Collection<Alimento> getAlimentos();

    public double apresentaTempoConfecao();

    public Map<String, Integer> apresentaStock();

    public void enviaMensagem(String mensagem);

    //public void registaAlimento(String id, String nome, int quantidade);

    //public void removeAlimento(String idAlimento);

    //public Collection<Alimento> getAlimentos();

}
