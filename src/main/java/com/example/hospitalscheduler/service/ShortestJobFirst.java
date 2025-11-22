package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

public class ShortestJobFirst {
    private Paciente paciente;

    public ShortestJobFirst(Paciente paciente) {
        this.paciente = paciente;
    }

    public void executar() {
        // Inicializa o tempo restante
        paciente.setRemaining(paciente.getBurst());

        // Tempo atual começa no momento de chegada do paciente
        int tempoAtual = paciente.getArrival();

        System.out.println("      Início da simulação SJF (Shortest Job First)      ");
        System.out.println("Paciente: " + paciente.getNome());
        System.out.println("Tempo de chegada: " + paciente.getArrival());
        System.out.println("Burst time: " + paciente.getBurst());
        System.out.println();

        System.out.println("Tempo " + tempoAtual + ":");
        System.out.println("Paciente " + paciente.getNome() + " iniciado (Burst: " + paciente.getBurst() + ")");
        System.out.println();

        // Executar o processo até completar
        while (paciente.getRemaining() > 0) {
            // Executa 1 unidade de tempo
            paciente.setRemaining(paciente.getRemaining() - 1);

            System.out.println("Tempo " + tempoAtual + ":");
            System.out.println("Executando " + paciente.getNome() +
                    " (restante=" + paciente.getRemaining() + ")");

            // Avança o tempo
            tempoAtual++;

            // Verifica se terminou
            if (paciente.getRemaining() == 0) {
                System.out.println(paciente.getNome() + " FINALIZADO no tempo " + tempoAtual);
            }

            System.out.println();
        }

        System.out.println("    Fim da simulação SJF     ");
        System.out.println("Tempo total de execução: " + (tempoAtual - paciente.getArrival()) + " unidades");
    }
}