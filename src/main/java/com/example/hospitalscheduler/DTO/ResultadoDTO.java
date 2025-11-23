package com.example.hospitalscheduler.DTO;

public class ResultadoDTO {
    private String logExecucao; // Todo o texto do "console" (Gantt, trocas, etc)


    public ResultadoDTO(String logExecucao) {
        this.logExecucao = logExecucao;

    }

    // Getters
    public String getLogExecucao() {
        return logExecucao;
    }

}