package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ShortestJobFirst {

    private int idMedico;
    private static List<Paciente> pacientes;
    private static PriorityQueue<Paciente> filaProntos;
    private static final Object lock = new Object();
    private static AtomicInteger finalizados = new AtomicInteger(0);
    private static volatile int tempoAtual = 0;
    private static volatile boolean sistemaAtivo = true;
    private static Map<Integer, StringBuilder> ganttPorMedico = new HashMap<>();
    private static Map<Integer, Integer> trocasContextoPorMedico = new HashMap<>();
    private static Map<Integer, Integer> tempoOcupadoPorMedico = new HashMap<>();
    private static Map<Integer, Paciente> pacienteAtualPorMedico = new HashMap<>();
    private static AtomicInteger medicosFinalizados = new AtomicInteger(0);
    private static int totalMedicos = 0;
    private static StringBuilder logCapturado = new StringBuilder();


    public ShortestJobFirst(int idMedico, List<Paciente> listaPacientes) {
        this.idMedico = idMedico;

        synchronized (ShortestJobFirst.class) {
            if (pacientes == null) {
                pacientes = listaPacientes;
                // PriorityQueue ordena por burst (menor primeiro), depois por arrival
                filaProntos = new PriorityQueue<>(
                        Comparator.comparingInt(Paciente::getBurst)
                                .thenComparingInt(Paciente::getArrival)
                );
                finalizados.set(0);
                tempoAtual = 0;
                sistemaAtivo = true;
                ganttPorMedico.clear();
                trocasContextoPorMedico.clear();
                tempoOcupadoPorMedico.clear();
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
            pacienteAtualPorMedico.put(idMedico, null);
        }
    }

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
            log("   EXECUÇÃO SJF - MÉDICO " + idMedico);
            log("========================================");
            log("Total de Pacientes: " + pacientes.size());
            for (Paciente p : pacientes) {
                log("  - Paciente " + p.getNome() +
                        ": Arrival=" + p.getArrival() +
                        ", Burst=" + p.getBurst());
            }
            log("========================================\n");
        }
    }

    private void printFila() {
        System.out.print("Fila de Prontos (ordenada por burst): [ ");
        List<Paciente> temp = new ArrayList<>(filaProntos);
        for (Paciente p : temp) {
            System.out.print(p.getNome() + "(" + p.getBurst() + ") ");
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
                // Apenas Médico 1 gerencia chegadas
                if (idMedico == 1) {
                    // Adiciona pacientes que chegaram no tempo atual
                    for (Paciente p : pacientes) {
                        if (p.getArrival() == tempoAtual && p.getRemaining() == p.getBurst()) {
                            filaProntos.add(p);
                            log("[Tempo " + tempoAtual + "] Nova chegada: " +
                                    p.getNome() + " (Burst: " + p.getBurst() + ")");
                        }
                    }
                }

                Paciente pacienteAtual = pacienteAtualPorMedico.get(idMedico);

                // SJF é NÃO-PREEMPTIVO: só pega novo paciente se terminou o anterior
                if (pacienteAtual == null && !filaProntos.isEmpty()) {
                    pacienteAtual = filaProntos.poll(); // Pega o de menor burst

                    log("[Médico " + idMedico + "] TROCA DE CONTEXTO → iniciou " +
                            pacienteAtual.getNome() + " (Burst: " + pacienteAtual.getBurst() + ")");
                    trocasContextoPorMedico.put(idMedico, trocasContextoPorMedico.get(idMedico) + 1);

                    pacienteAtualPorMedico.put(idMedico, pacienteAtual);
                }

                // Executa 1 unidade de tempo (SJF executa até o fim)
                if (pacienteAtual != null) {
                    pacienteAtual.setRemaining(pacienteAtual.getRemaining() - 1);
                    tempoOcupadoPorMedico.put(idMedico, tempoOcupadoPorMedico.get(idMedico) + 1);

                    ganttPorMedico.get(idMedico).append(pacienteAtual.getNome()).append("|");

                    log("[Médico " + idMedico + "] executando " +
                            pacienteAtual.getNome() +
                            " (restante=" + pacienteAtual.getRemaining() + ")");

                    // Verifica se finalizou (SJF NÃO interrompe no meio)
                    if (pacienteAtual.getRemaining() == 0) {

                        pacienteAtual.setTempoFinalizacao(tempoAtual + 1);
                        pacienteAtual.setTurnaround(tempoAtual - pacienteAtual.getArrival());
                        pacienteAtual.setTempoEspera(pacienteAtual.getTurnaround() - pacienteAtual.getBurst());
                        // =====================================

                        log("[Médico " + idMedico + "] FINALIZADO → " +
                                pacienteAtual.getNome());

                        pacienteAtualPorMedico.put(idMedico, null);
                        finalizados.incrementAndGet();
                    }
                } else {
                    // Médico ocioso
                    ganttPorMedico.get(idMedico).append("ocioso|");
                }

                // Apenas médico 1 avança o tempo
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

        for (Map.Entry<Integer, StringBuilder> entry : ganttPorMedico.entrySet()) {
            int med = entry.getKey();
            log("GANTT MÉDICO " + med + ":");
            log("CPU " + med + ": " + entry.getValue());
            log("Trocas de Contexto: " + trocasContextoPorMedico.get(med));

            double utilizacao = tempoAtual > 0 ?
                    (tempoOcupadoPorMedico.get(med) / (double) tempoAtual) * 100.0 : 0;
            log("Utilização: " + String.format("%.1f", utilizacao) + "%");

        }

        log("Tempo Total de Simulação: " + tempoAtual);

        // ============================================
        //         MÉTRICAS DE DESEMPENHO (NOVO)
        // ============================================

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

        log("\nFim da simulação.\n");
    }

    public static void reset() {
        synchronized (ShortestJobFirst.class) {
            pacientes = null;
            filaProntos = null;
            finalizados.set(0);
            tempoAtual = 0;
            sistemaAtivo = false;
            ganttPorMedico.clear();
            trocasContextoPorMedico.clear();
            tempoOcupadoPorMedico.clear();
            pacienteAtualPorMedico.clear();
            medicosFinalizados.set(0);
            totalMedicos = 0;
            logCapturado = new StringBuilder();
        }
    }
}