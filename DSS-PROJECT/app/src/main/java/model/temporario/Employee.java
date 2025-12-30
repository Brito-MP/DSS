package model.temporario;

import java.io.Serializable;

public class Employee implements Serializable{
    private Workstation workstation;
    private int id;
    private String name;
    private static int idCounter = 0;
    private WorkstationType workstationType;



    public Employee(String name, WorkstationType workstationType){
        this.workstation = new Workstation(workstationType);
        this.id = idCounter++;
        this.name = name;
        this.workstationType = workstationType;
    }


    public static int getIdCounter(){
        return Employee.idCounter;
    }

    public static void setIdCounter(int idCounter){
        Employee.idCounter = idCounter;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public Workstation getWorkstation(){
        return this.workstation;
    }

    @Override
    public String toString() {
        return "Name: " + getName() + " -> " + "Id: " + getId() + " -> "  + "workstation: " + workstationType;
    }


}

