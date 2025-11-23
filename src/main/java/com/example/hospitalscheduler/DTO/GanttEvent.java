package com.example.hospitalscheduler.DTO;

public class GanttEvent {

    private String paciente;   // nome ou id do paciente
    private String medico;     // opcional — se quiser mostrar qual médico executou
    private int start;         // tempo de início
    private int end;           // tempo de fim

    public GanttEvent(String paciente, String medico, int start, int end) {
        this.paciente = paciente;
        this.medico = medico;
        this.start = start;
        this.end = end;
    }

    public String getPaciente() {
        return paciente;
    }

    public String getMedico() {
        return medico;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
