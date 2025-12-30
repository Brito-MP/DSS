package model.gestao;

import java.util.Map;

public class GestaoFacade implements InterGestaoL {
    Map<String,Alimento> alimentos;

    @Override
    public Alimento getAlimento(String idAlimento) {
        return this.alimentos.get(idAlimento).clone();
    }
    
}
