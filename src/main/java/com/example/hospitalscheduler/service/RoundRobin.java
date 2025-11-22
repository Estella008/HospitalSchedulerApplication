package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

public class RoundRobin {
    private int quantum;
    private Paciente paciente;

    public RoundRobin(int quantum, Paciente paciente) {
        this.quantum = quantum;
        this.paciente = paciente;
    }

    public void executar() {
        // Simulação de um relógio
        int tempoAtual = paciente.getArrival();

        // Inicializando o tempo restante do paciente
        paciente.setRemaining(paciente.getBurst());

        System.out.println("      Início da simulação Round-Robin      ");
        System.out.println("Paciente: " + paciente.getNome());
        System.out.println("Tempo de chegada: " + paciente.getArrival());
        System.out.println("Burst time: " + paciente.getBurst());
        System.out.println("Quantum: " + quantum);
        System.out.println();

        // Loop de execução
        while (paciente.getRemaining() > 0) {
            // Calcula quanto tempo será executado neste ciclo
            int tempoExecucao = Math.min(quantum, paciente.getRemaining());

            System.out.println("Tempo " + tempoAtual + ":");
            System.out.println("Executando " + paciente.getNome() +
                    " por " + tempoExecucao + " unidade(s) de tempo");

            // Reduz o tempo restante
            paciente.setRemaining(paciente.getRemaining() - tempoExecucao);

            // Avança o tempo
            tempoAtual += tempoExecucao;

            System.out.println("Tempo restante: " + paciente.getRemaining());

            // Se ainda há tempo restante, o quantum terminou
            if (paciente.getRemaining() > 0) {
                System.out.println("Quantum terminou, processo continua no próximo ciclo");
            } else {
                System.out.println(paciente.getNome() + " FINALIZADO no tempo " + tempoAtual);
            }

            System.out.println();
        }

        System.out.println("    Fim da simulação     ");
        System.out.println("Tempo total de execução: " + (tempoAtual - paciente.getArrival()) + " unidades");
    }
}