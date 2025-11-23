package com.example.hospitalscheduler.service.EscalonadorPrioridade;
import com.example.hospitalscheduler.service.GanttService;

import com.example.hospitalscheduler.DTO.Paciente;

public class PrioridadeNaoPreemptivo {

    private final Paciente paciente;
    private final GanttService ganttService;

    public PrioridadeNaoPreemptivo(Paciente paciente, GanttService ganttService) {
        this.paciente = paciente;
        this.ganttService = ganttService;
    }

    public void executar() {
        int start = ganttService.getCurrentTime();
        int end = start + paciente.getBurst();

        try {
            // Simula execução real
            Thread.sleep(paciente.getBurst() * 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Preenche resultados do paciente
        paciente.setTempoFinalizacao(end);
        paciente.setTurnaround(end - paciente.getArrival());
        paciente.setTempoEspera(paciente.getTurnaround() - paciente.getBurst());

        // Adiciona ao gráfico de Gantt
        ganttService.add(
                paciente.getNome(),
                "Medico Prioridade",
                start,
                end
        );
    }
}
