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
    private static StringBuilder logCapturado = new StringBuilder();

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
                logCapturado = new StringBuilder();

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

    // Método auxiliar para log duplo (console + captura)
    private static void log(String mensagem) {
        System.out.println(mensagem);
        synchronized (logCapturado) {
            logCapturado.append(mensagem).append("\n");
        }
    }

    private static void logInline(String mensagem) {
        System.out.print(mensagem);
        synchronized (logCapturado) {
            logCapturado.append(mensagem);
        }
    }

    public static String getLogs() {
        synchronized (logCapturado) {
            return logCapturado.toString();
        }
    }

    private void printHeader() {
        synchronized (lock) {
            log("\n========================================");
            log("   EXECUÇÃO ROUND ROBIN ");
            log("========================================");
            log("Total de Pacientes: " + pacientes.size());
            log("Quantum: " + quantum);
            for (Paciente p : pacientes) {
                log("  - Paciente " + p.getNome() +
                        ": Arrival=" + p.getArrival() +
                        ", Burst=" + p.getBurst());
            }
            log("========================================\n");
        }
    }

    private void printFila() {
        logInline("Fila de Espera: [ ");
        for (Paciente p : filaProcessos) {
            logInline(p.getNome() + " ");
        }
        log("]");
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
                            log("[Tempo " + tempoAtual + "] Nova chegada: " + p.getNome());
                        }
                    }
                }

                Paciente pacienteAtual = pacienteAtualPorMedico.get(idMedico);
                int quantumRestante = quantumRestantePorMedico.get(idMedico);

                // Tenta alocar paciente se o médico está livre
                if (pacienteAtual == null && !filaProcessos.isEmpty()) {
                    pacienteAtual = filaProcessos.poll();
                    quantumRestante = quantum;

                    log("[Médico " + idMedico + "] TROCA DE CONTEXTO → iniciou " +
                            pacienteAtual.getNome());
                    trocasContextoPorMedico.put(idMedico, trocasContextoPorMedico.get(idMedico) + 1);

                    pacienteAtualPorMedico.put(idMedico, pacienteAtual);
                    quantumRestantePorMedico.put(idMedico, quantumRestante);
                }

                // Executa 1 unidade de tempo
                if (pacienteAtual != null) {

                    // Evita que execute um processo já finalizado
                    if (pacienteAtual.getRemaining() <= 0) {
                        pacienteAtualPorMedico.put(idMedico, null);
                        quantumRestantePorMedico.put(idMedico, 0);
                        continue; // pula para o próximo tempo
                    }

                    pacienteAtual.setRemaining(pacienteAtual.getRemaining() - 1);
                    quantumRestante--;
                    tempoOcupadoPorMedico.put(idMedico, tempoOcupadoPorMedico.get(idMedico) + 1);

                    ganttPorMedico.get(idMedico).append(pacienteAtual.getNome()).append("|");

                    log("[Médico " + idMedico + "] executando " +
                            pacienteAtual.getNome() +
                            " (restante=" + pacienteAtual.getRemaining() +
                            ", quantum restante=" + quantumRestante + ")");

                    // Verifica se finalizou
                    if (pacienteAtual.getRemaining() == 0) {
                        log("[Médico " + idMedico + "] FINALIZADO → " +
                                pacienteAtual.getNome());
                        pacienteAtual.setTempoFinalizacao(tempoAtual + 1);
                        pacienteAtualPorMedico.put(idMedico, null);
                        quantumRestantePorMedico.put(idMedico, 0);
                        finalizados.incrementAndGet();
                    }
                    // Quantum acabou (chegou a 0)
                    else if (quantumRestante == 0) {
                        log("[Médico " + idMedico + "] Quantum acabou → " +
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
                        log("\n--------------------------------------");
                        log("[Tempo " + tempoAtual + "]");
                        printFila();
                        log("--------------------------------------");
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
        log("\n================ RESULTADOS GERAIS ================\n");

        // Imprime Gantt e utilização por médico
        for (Map.Entry<Integer, StringBuilder> entry : ganttPorMedico.entrySet()) {
            int med = entry.getKey();
            log("GANTT MÉDICO " + med + ":");
            log("CPU " + med + ": " + entry.getValue());
            log("Trocas de Contexto: " + trocasContextoPorMedico.get(med));

            double utilizacao = tempoAtual > 0 ?
                    (tempoOcupadoPorMedico.get(med) / (double) tempoAtual) * 100.0 : 0;
            log("Utilização: " + String.format("%.1f", utilizacao) + "%");
            log("");
        }

        log("Tempo Total de Simulação: " + tempoAtual);
        log("Quantum: " + quantum);

        // ================================
        //       MÉTRICAS DE DESEMPENHO
        // ================================

        log("\n--- MÉTRICAS POR PACIENTE ---");

        double somaTurnaround = 0;
        double somaEspera = 0;

        for (Paciente p : pacientes) {

            int turnaround = p.getTempoFinalizacao() - p.getArrival();
            int espera = turnaround - p.getBurst();

            p.setTurnaround(turnaround);
            p.setTempoEspera(espera);

            somaTurnaround += turnaround;
            somaEspera += espera;

            log("Paciente " + p.getNome()
                    + " | Finalização: " + p.getTempoFinalizacao()
                    + " | Turnaround: " + turnaround
                    + " | Espera: " + espera
            );
        }

        double avgTurnaround = somaTurnaround / pacientes.size();
        double avgEspera = somaEspera / pacientes.size();

        log("\n--- MÉTRICAS GERAIS ---");
        log("Tempo Médio de Execução (Turnaround): " + String.format("%.2f", avgTurnaround));
        log("Tempo Médio de Espera: " + String.format("%.2f", avgEspera));

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
            logCapturado = new StringBuilder();
        }
    }
}