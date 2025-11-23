package com.example.hospitalscheduler.DTO;

import java.util.List;

public class ResultadoEscalonamento {
        private List<Paciente> pacientes;
    private List<GanttEvent> gantt;

    public ResultadoEscalonamento(List<Paciente> pacientes, List<GanttEvent> gantt) {
        this.pacientes = pacientes;
        this.gantt = gantt;
    }

    public List<Paciente> getPacientes() {
        return pacientes;
    }

    public List<GanttEvent> getGantt() {
        return gantt;
    }
    
}
