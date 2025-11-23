package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin {

    private int idMedico;
    private static int quantum;
    private static List<Paciente> pacientes;
    private static Queue<Paciente> filaProcessos;
    private static final Object lock = new Object();
    private static AtomicInteger finalizados = new AtomicInteger(0);
    private static volatile int tempoAtual = 0;
    private static volatile boolean sistemaAtivo = true;
    private static Map<Integer, StringBuilder> ganttPorMedico = new HashMap<>();
    private static Map<Integer, Integer> trocasContextoPorMedico = new HashMap<>();
    private static Map<Integer, Integer> tempoOcupadoPorMedico = new HashMap<>();
    private static Map<Integer, Integer> quantumRestantePorMedico = new HashMap<>();
    private static Map<Integer, Paciente> pacienteAtualPorMedico = new HashMap<>();
    private static AtomicInteger medicosFinalizados = new AtomicInteger(0);
    private static int totalMedicos = 0;

    public RoundRobin(int idMedico, int quantum, List<Paciente> listaPacientes) {
        this.idMedico = idMedico;

        synchronized (RoundRobin.class) {
            if (pacientes == null) {
                RoundRobin.quantum = quantum;
                pacientes = listaPacientes;
                filaProcessos = new LinkedList<>();
                finalizados.set(0);
                tempoAtual = 0;
                sistemaAtivo = true;
                ganttPorMedico.clear();
                trocasContextoPorMedico.clear();
                tempoOcupadoPorMedico.clear();
                quantumRestantePorMedico.clear();
                pacienteAtualPorMedico.clear();
                medicosFinalizados.set(0);
                totalMedicos = 0;

                // Inicializa remaining
                for (Paciente p : pacientes) {
                    p.setRemaining(p.getBurst());
                }

                // Ordena por arrival
                pacientes.sort(Comparator.comparingInt(Paciente::getArrival));
            }

            totalMedicos++;
            ganttPorMedico.put(idMedico, new StringBuilder());
            trocasContextoPorMedico.put(idMedico, 0);
            tempoOcupadoPorMedico.put(idMedico, 0);
            quantumRestantePorMedico.put(idMedico, 0);
            pacienteAtualPorMedico.put(idMedico, null);
        }
    }

    private void printHeader() {
        synchronized (lock) {
            System.out.println("\n========================================");
            System.out.println("   EXECUÇÃO ROUND ROBIN - MÉDICO " + idMedico);
            System.out.println("========================================");
            System.out.println("Total de Pacientes: " + pacientes.size());
            System.out.println("Quantum: " + quantum);
            for (Paciente p : pacientes) {
                System.out.println("  - Paciente " + p.getNome() +
                        ": Arrival=" + p.getArrival() +
                        ", Burst=" + p.getBurst());
            }
            System.out.println("========================================\n");
        }
    }

    private void printFila() {
        System.out.print("Fila de Espera: [ ");
        for (Paciente p : filaProcessos) {
            System.out.print(p.getNome() + " ");
        }
        System.out.println("]");
    }

    public void executar() {
        if (idMedico == 1) {
            printHeader();
        }

        // Aguarda todos os médicos estarem prontos
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return;
        }

        int total = pacientes.size();

        while (sistemaAtivo && finalizados.get() < total) {

            synchronized (lock) {
                // Apenas Médico 1 gerencia chegadas e tempo
                if (idMedico == 1) {
                    // Adiciona pacientes que chegaram no tempo atual
                    for (Paciente p : pacientes) {
                        if (p.getArrival() == tempoAtual && p.getRemaining() == p.getBurst()) {
                            filaProcessos.add(p);
                            System.out.println("[Tempo " + tempoAtual + "] Nova chegada: " + p.getNome());
                        }
                    }
                }

                Paciente pacienteAtual = pacienteAtualPorMedico.get(idMedico);
                int quantumRestante = quantumRestantePorMedico.get(idMedico);

                // Tenta alocar paciente se o médico está livre
                if (pacienteAtual == null && !filaProcessos.isEmpty()) {
                    pacienteAtual = filaProcessos.poll();
                    quantumRestante = quantum;

                    System.out.println("[Médico " + idMedico + "] TROCA DE CONTEXTO → iniciou " +
                            pacienteAtual.getNome());
                    trocasContextoPorMedico.put(idMedico, trocasContextoPorMedico.get(idMedico) + 1);

                    pacienteAtualPorMedico.put(idMedico, pacienteAtual);
                    quantumRestantePorMedico.put(idMedico, quantumRestante);
                }

                // Executa 1 unidade de tempo
                if (pacienteAtual != null) {
                    pacienteAtual.setRemaining(pacienteAtual.getRemaining() - 1);
                    quantumRestante--;
                    tempoOcupadoPorMedico.put(idMedico, tempoOcupadoPorMedico.get(idMedico) + 1);

                    ganttPorMedico.get(idMedico).append(pacienteAtual.getNome()).append("|");

                    System.out.println("[Médico " + idMedico + "] executando " +
                            pacienteAtual.getNome() +
                            " (restante=" + pacienteAtual.getRemaining() +
                            ", quantum restante=" + quantumRestante + ")");

                    // Verifica se finalizou
                    if (pacienteAtual.getRemaining() == 0) {
                        System.out.println("[Médico " + idMedico + "] FINALIZADO → " +
                                pacienteAtual.getNome());
                        pacienteAtualPorMedico.put(idMedico, null);
                        quantumRestantePorMedico.put(idMedico, 0);
                        finalizados.incrementAndGet();
                    }
                    // Quantum acabou (chegou a 0)
                    else if (quantumRestante == 0) {
                        System.out.println("[Médico " + idMedico + "] Quantum acabou → " +
                                pacienteAtual.getNome() + " volta para fila");
                        filaProcessos.add(pacienteAtual);
                        pacienteAtualPorMedico.put(idMedico, null);
                        quantumRestantePorMedico.put(idMedico, 0);
                    } else {
                        // Atualiza quantum restante (ainda tem quantum)
                        quantumRestantePorMedico.put(idMedico, quantumRestante);
                    }
                } else {
                    // Médico ocioso
                    ganttPorMedico.get(idMedico).append("ocioso|");
                }

                // Apenas médico 1 avança o tempo e imprime fila
                if (idMedico == 1) {
                    tempoAtual++;
                    if (finalizados.get() < total) {
                        System.out.println("\n--------------------------------------");
                        System.out.println("[Tempo " + tempoAtual + "]");
                        printFila();
                        System.out.println("--------------------------------------");
                    }
                }
            }

            // Sincroniza tempo entre médicos
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Último médico a finalizar imprime resultados
        int medicosFin = medicosFinalizados.incrementAndGet();

        if (medicosFin >= totalMedicos) {
            synchronized (lock) {
                if (finalizados.get() >= total) {
                    imprimirResultados();
                }
            }
        }
    }

    private void imprimirResultados() {
        System.out.println("\n================ RESULTADOS GERAIS ================\n");

        for (Map.Entry<Integer, StringBuilder> entry : ganttPorMedico.entrySet()) {
            int med = entry.getKey();
            System.out.println("GANTT MÉDICO " + med + ":");
            System.out.println("CPU " + med + ": " + entry.getValue());
            System.out.println("Trocas de Contexto: " + trocasContextoPorMedico.get(med));

            double utilizacao = tempoAtual > 0 ?
                    (tempoOcupadoPorMedico.get(med) / (double) tempoAtual) * 100.0 : 0;
            System.out.println("Utilização: " + String.format("%.1f", utilizacao) + "%");
            System.out.println();
        }

        System.out.println("Tempo Total de Simulação: " + tempoAtual);
        System.out.println("Quantum: " + quantum);
        System.out.println("\nFim da simulação.\n");
    }

    public static void reset() {
        synchronized (RoundRobin.class) {
            pacientes = null;
            filaProcessos = null;
            finalizados.set(0);
            tempoAtual = 0;
            sistemaAtivo = false;
            ganttPorMedico.clear();
            trocasContextoPorMedico.clear();
            tempoOcupadoPorMedico.clear();
            quantumRestantePorMedico.clear();
            pacienteAtualPorMedico.clear();
            medicosFinalizados.set(0);
            totalMedicos = 0;
        }
    }
}