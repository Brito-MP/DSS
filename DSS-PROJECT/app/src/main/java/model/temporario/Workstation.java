package model.temporario;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Workstation implements Serializable {
    private List <Food> foods;
    private WorkstationType workstationType;


    public Workstation (WorkstationType workstationType){
        this.foods = new ArrayList<>();
        this.workstationType = workstationType;
    }

    public WorkstationType getWorkstationType(){
        return this.workstationType;
    }

    public String toString(){
        return "workstation type: " + this.workstationType;
    }
}
