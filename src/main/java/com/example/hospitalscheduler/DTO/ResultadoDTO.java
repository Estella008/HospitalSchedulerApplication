package com.example.hospitalscheduler.DTO;

public class ResultadoDTO {
    private String logExecucao; // Todo o texto do "console" (Gantt, trocas, etc)
    private double tempoMedioEspera;
    private double tempoMedioTurnaround;
    private double utilizacaoCpu;
    private int trocasContexto;

    public ResultadoDTO(String logExecucao, double tempoMedioEspera, double tempoMedioTurnaround, double utilizacaoCpu, int trocasContexto) {
        this.logExecucao = logExecucao;
        this.tempoMedioEspera = tempoMedioEspera;
        this.tempoMedioTurnaround = tempoMedioTurnaround;
        this.utilizacaoCpu = utilizacaoCpu;
        this.trocasContexto = trocasContexto;
    }

    // Getters
    public String getLogExecucao() {
        return logExecucao;
    }

    public double getTempoMedioEspera() {
        return tempoMedioEspera;
    }

    public double getTempoMedioTurnaround() {
        return tempoMedioTurnaround;
    }

    public double getUtilizacaoCpu() {
        return utilizacaoCpu;
    }

    public int getTrocasContexto() {
        return trocasContexto;
    }
}