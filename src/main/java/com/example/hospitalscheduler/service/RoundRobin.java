package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

import java.util.*;

public class RoundRobin {

    private int quantum;
    private Paciente paciente;
    private int trocasContexto = 0;
    private int tempoTotalSimulacao = 0;
    private int tempoOcupadoMedico = 0;

    //construtor
    public RoundRobin(int quantum, Paciente paciente) {
        this.quantum = quantum;
        this.paciente = paciente;
    }

    //cabeçalho
    private void printHeader() {
        System.out.println("\n========================================");
        System.out.println("        EXECUÇÃO ROUND ROBIN");
        System.out.println("========================================");
        System.out.println("Quantidade de Médicos (CPUs): 1");
        System.out.println("Quantidade de Pacientes: 1");
        System.out.println("Quantum: " + quantum);
        System.out.println("========================================\n");
    }

    //printa fila de execução dos pacientes
    private void printFila(Queue<Paciente> fila) {
        System.out.print("Fila de Espera: [ ");
        for (Paciente p : fila) System.out.print(p.getNome() + " ");
        System.out.println("]");
    }

    //mostra se médico esta ocioso ou executando algum processo (paciente)
    private void printExecucao(Paciente cpu) {
        System.out.println("Médicos em Execução:");
        if (cpu != null)
            System.out.println("   • Médico 0 → " + cpu.getNome());
        else
            System.out.println("   • Médico 0 → (ocioso)");
    }

    //exibe os estados da CPU ao longo do tempo
    private void printGantt(StringBuilder gantt) {
        System.out.println("\nGANTT POR MÉDICO:");
        System.out.println("CPU 0: " + gantt);
    }

    public void executar() {

        printHeader();

        //criando fila de processos
        Queue<Paciente> filaProcessos = new LinkedList<>();

        //simulação de um relógio
        int tempoAtual = 0;
        //quantidade de processos finalizados
        int finalizados = 0;

        //inicializando a marcação do tempo restante do paciente
        paciente.setRemaining(paciente.getBurst());

        //paciente associado ao médico (cpu)
        Paciente cpu = null;
        //controla o quantum do processo que esta sendo processado
        int quantumRestante = 0;

        //para formar o Gantt
        StringBuilder gantt = new StringBuilder();

        System.out.println("--------------------------------------");
        System.out.println("Tempo " + tempoAtual);
        System.out.println("--------------------------------------");

        // Loop principal
        while (finalizados < 1) {

            //adiciona paciente quando chegar seu tempo de chegada
            if (tempoAtual == paciente.getArrival()) {
                filaProcessos.add(paciente);
                System.out.println("Nova chegada: " + paciente.getNome());
            }

            //alocar paciente ao médico se estiver livre
            if (cpu == null && !filaProcessos.isEmpty()) {
                //retorna processo e o tira da fila
                Paciente p = filaProcessos.poll();
                cpu = p;
                quantumRestante = quantum;

                System.out.println("TROCA DE CONTEXTO → Médico 0 iniciou " + p.getNome());
                trocasContexto++;
            }

            //executar 1 unidade de tempo
            if (cpu != null) {
                cpu.setRemaining(cpu.getRemaining() - 1);
                quantumRestante--;
                tempoOcupadoMedico++;

                //grava qual paciente foi atendido pelo médico
                gantt.append(cpu.getNome()).append("|");

                System.out.println("Médico 0 executando " + cpu.getNome() +
                        " (restante=" + cpu.getRemaining() +
                        ", quantum=" + quantumRestante + ")");

                //analisa se o paciente terminou
                if (cpu.getRemaining() == 0) {
                    System.out.println("FINALIZADO → " + cpu.getNome());
                    cpu = null;
                    finalizados++;
                }
                //finaliza o quantum do processo
                else if (quantumRestante == 0) {
                    System.out.println("Quantum acabou → " + cpu.getNome() + " volta para fila");
                    filaProcessos.add(cpu);
                    cpu = null;
                }
            } else {
                //médico ocioso
                gantt.append("ocioso|");
            }

            //avançar o tempo
            tempoAtual++;
            tempoTotalSimulacao++;

            //exibir estado a cada unidade de tempo
            if (finalizados < 1) {
                System.out.println("\n--------------------------------------");
                System.out.println("Tempo " + tempoAtual);
                System.out.println("--------------------------------------");
                printFila(filaProcessos);
                printExecucao(cpu);
            }
        }

        System.out.println("\n================ RESULTADOS ================\n");
        printGantt(gantt);
        System.out.println("Tempo Médio de Espera: " + calcularTempoMedioEspera());
        System.out.println("Turnaround Médio: " + calcularTurnaroundMedio());
        System.out.println("Trocas de Contexto: " + getTrocasContexto());
        System.out.println("Utilização Média dos Médicos: " + calcularUtilizacaoMedicos() + "%");
        System.out.println("\nFim da simulação.\n");
    }

    double calcularTempoMedioEspera() {
        return paciente.getTempoEspera();
    }

    double calcularTurnaroundMedio() {
        return paciente.getTurnaround();
    }

    int getTrocasContexto() {
        return trocasContexto;
    }

    double calcularUtilizacaoMedicos() {
        if (tempoTotalSimulacao == 0) return 0;
        return (tempoOcupadoMedico / (double) tempoTotalSimulacao) * 100.0;
    }
}