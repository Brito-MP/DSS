package model.preparacoes;

import java.util.HashMap;
import java.util.Map;

public class Posto {

    private final String id;
    private final Map<String, Integer> quantidadeAlimento; // idAlimento -> quantidade

    public Posto(String id) {
        this.id = id;
        this.quantidadeAlimento = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public Map<String, Integer> getQuantidadeAlimento() {
        return quantidadeAlimento;
    }

}
