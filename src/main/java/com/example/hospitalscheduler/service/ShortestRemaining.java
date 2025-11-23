package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

public class ShortestRemaining {

    private int trocasContexto = 0;
    private int tempoTotalSimulacao = 0;
    private int tempoOcupadoMedico = 0;
    private final int idMedico;
    private Paciente paciente;

    public ShortestRemaining(int idMedico, Paciente paciente) {
        this.paciente = paciente;
        this.idMedico = idMedico;
    }

    private void printHeader(Paciente paciente) {
        System.out.println("\n========================================");
        System.out.println("      EXECUÇÃO SRTF POR MÉDICO " + idMedico);
        System.out.println("========================================");
        System.out.println("Paciente: " + paciente.getNome());
        System.out.println("Burst: " + paciente.getBurst());
        System.out.println("Arrival: " + paciente.getArrival());
        System.out.println("========================================\n");
    }

    private void printExecucao(Paciente paciente) {
        System.out.println("Médico " + idMedico + " em Execução: " + paciente.getNome());
    }

    private void printGantt(StringBuilder gantt) {
        System.out.println("\nGANTT MÉDICO " + idMedico + ":");
        System.out.println("CPU " + idMedico + ": " + gantt);
    }

    public void executar() {

        printHeader(paciente);

        paciente.setRemaining(paciente.getBurst());

        int tempoAtual = paciente.getArrival();
        boolean finalizado = false;

        StringBuilder gantt = new StringBuilder();

        System.out.println("--------------------------------------");
        System.out.println("Tempo " + tempoAtual);
        System.out.println("--------------------------------------");

        // Troca de contexto inicial
        System.out.println("TROCA DE CONTEXTO → Médico " + idMedico +
                " iniciou " + paciente.getNome());
        trocasContexto++;

        while (!finalizado) {

            // Executa uma unidade de tempo
            paciente.setRemaining(paciente.getRemaining() - 1);
            tempoOcupadoMedico++;

            gantt.append(paciente.getNome()).append("|");

            printExecucao(paciente);

            if (paciente.getRemaining() == 0) {
                System.out.println("FINALIZADO → " + paciente.getNome());
                finalizado = true;
            }

            tempoAtual++;
            tempoTotalSimulacao++;

            if (!finalizado) {
                System.out.println("\n--------------------------------------");
                System.out.println("Tempo " + tempoAtual);
                System.out.println("--------------------------------------");
                printExecucao(paciente);
            }
        }

        // Resultados
        System.out.println("\n================ RESULTADOS ================\n");
        printGantt(gantt);
        System.out.println("Trocas de Contexto: " + trocasContexto);
        System.out.println("Utilização do Médico: " + calcularUtilizacao() + "%");
        System.out.println("\nFim da simulação.\n");
    }

    private double calcularUtilizacao() {
        if (tempoTotalSimulacao == 0) return 0;
        return (tempoOcupadoMedico / (double) tempoTotalSimulacao) * 100.0;
    }
}

