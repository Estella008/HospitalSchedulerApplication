package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Prioridade {

    private int idMedico;

    private static List<Paciente> pacientes;
    private static List<Paciente> filaProcessos;

    private static final Object lock = new Object();

    private static AtomicInteger finalizados = new AtomicInteger(0);
    private static volatile int tempoAtual = 0;
    private static volatile boolean sistemaAtivo = true;

    private static Map<Integer, StringBuilder> ganttPorMedico = new HashMap<>();
    private static Map<Integer, Integer> tempoOcupadoPorMedico = new HashMap<>();
    private static Map<Integer, Paciente> pacienteAtualPorMedico = new HashMap<>();
    private static Map<Integer, Integer> trocasContextoPorMedico = new HashMap<>();

    private static AtomicInteger medicosFinalizados = new AtomicInteger(0);
    private static int totalMedicos = 0;

    private static StringBuilder logCapturado = new StringBuilder();


    public Prioridade(int idMedico, List<Paciente> listaPacientes) {
        this.idMedico = idMedico;

        synchronized (Prioridade.class) {
            if (pacientes == null) {

                pacientes = new ArrayList<>();
                filaProcessos = new ArrayList<>();

                finalizados.set(0);
                tempoAtual = 0;
                sistemaAtivo = true;
                ganttPorMedico.clear();
                tempoOcupadoPorMedico.clear();
                pacienteAtualPorMedico.clear();
                trocasContextoPorMedico.clear();
                medicosFinalizados.set(0);
                totalMedicos = 0;
                logCapturado = new StringBuilder();

                // Copia pacientes e inicializa dados
                for (Paciente p : listaPacientes) {
                    p.setRemaining(p.getBurst());
                    pacientes.add(p);
                }

                // Ordena apenas por arrival (chegada)
                pacientes.sort(Comparator.comparingInt(Paciente::getArrival));
            }

            totalMedicos++;
            ganttPorMedico.put(idMedico, new StringBuilder());
            tempoOcupadoPorMedico.put(idMedico, 0);
            pacienteAtualPorMedico.put(idMedico, null);
            trocasContextoPorMedico.put(idMedico, 0);
        }
    }


    private static void log(String msg) {
        System.out.println(msg);
        synchronized (logCapturado) {
            logCapturado.append(msg).append("\n");
        }
    }

    private static void logInline(String msg) {
        System.out.print(msg);
        synchronized (logCapturado) {
            logCapturado.append(msg);
        }
    }


    public static String getLogs() {
        return logCapturado.toString();
    }


    private void printHeader() {
        log("\n========================================");
        log("   EXECUÇÃO PRIORIDADE - MÉDICO " + idMedico);
        log("========================================");

        for (Paciente p : pacientes) {
            log("  Paciente " + p.getNome() +
                    " | Arrival=" + p.getArrival() +
                    " | Burst=" + p.getBurst() +
                    " | Prioridade=" + p.getPriority());
        }

        log("========================================\n");
    }


    private void printFila() {
        logInline("Fila de espera: [ ");
        for (Paciente p : filaProcessos) {
            logInline(p.getNome() + "(P" + p.getPriority()+ ") ");
        }
        log("]");
    }


    // -----------------------------------------------------
    // EXECUÇÃO
    // -----------------------------------------------------
    public void executar() {

        if (idMedico == 1) {
            printHeader();
        }

        try {
            Thread.sleep(100);
        } catch (Exception ignored) {}

        int total = pacientes.size();

        while (sistemaAtivo && finalizados.get() < total) {

            synchronized (lock) {

                if (idMedico == 1) {
                    // Chegadas no tempo
                    for (Paciente p : pacientes) {
                        if (p.getArrival() == tempoAtual && p.getRemaining() == p.getBurst()) {
                            filaProcessos.add(p);
                            log("[Tempo " + tempoAtual + "] Chegada: " + p.getNome());
                        }
                    }
                }


                Paciente atual = pacienteAtualPorMedico.get(idMedico);

                // Se médico está livre
                if (atual == null) {

                    if (!filaProcessos.isEmpty()) {

                        // Ordena fila por prioridade
                        filaProcessos.sort(Comparator.comparingInt(Paciente::getPriority));

                        atual = filaProcessos.remove(0);

                        pacienteAtualPorMedico.put(idMedico, atual);

                        trocasContextoPorMedico.put(idMedico,
                                trocasContextoPorMedico.get(idMedico) + 1);

                        log("[Médico " + idMedico + "] TROCA DE CONTEXTO → iniciou " + atual.getNome());
                    }
                }

                
                if (atual != null) {
                    atual.setRemaining(atual.getRemaining() - 1);

                    tempoOcupadoPorMedico.put(idMedico,
                            tempoOcupadoPorMedico.get(idMedico) + 1);

                    ganttPorMedico.get(idMedico).append(atual.getNome()).append("|");

                    log("[Médico " + idMedico + "] executando " +
                            atual.getNome() + " (restante=" + atual.getRemaining() + ")");

                    if (atual.getRemaining() == 0) {
                        atual.setTempoFinalizacao(tempoAtual + 1);
                        pacienteAtualPorMedico.put(idMedico, null);
                        finalizados.incrementAndGet();

                        log("[Médico " + idMedico + "] FINALIZADO → " + atual.getNome());
                    }

                } else {
                    
                    ganttPorMedico.get(idMedico).append("ocio|");
                }


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

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }


        int medF = medicosFinalizados.incrementAndGet();
        if (medF >= totalMedicos) {

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
        log("------------ MÉDICO " + med + " ------------");
        log("GANTT:");
        log(entry.getValue().toString());
        log("Trocas de Contexto: " + trocasContextoPorMedico.get(med));

        double utilizacao = tempoAtual > 0 ?
                (tempoOcupadoPorMedico.get(med) / (double) tempoAtual) * 100.0 : 0;
        log("Utilização da CPU: " + String.format("%.1f", utilizacao) + "%\n");
    }

    log("Tempo Total de Simulação: " + tempoAtual + "\n");

   
    log("\n=========== MÉTRICAS POR PACIENTE ===========\n");

    double somaTurnaround = 0;
    double somaEspera = 0;

    for (Paciente p : pacientes) {
        int turnaround = p.getTempoFinalizacao() - p.getArrival();
        int espera = turnaround - p.getBurst();

        p.setTurnaround(turnaround);
        p.setTempoEspera(espera);

        somaTurnaround += turnaround;
        somaEspera += espera;

        log("Paciente " + p.getNome() +
                " → Finalização=" + p.getTempoFinalizacao() +
                " | Turnaround=" + turnaround +
                " | Espera=" + espera);
    }

    double avgTurnaround = somaTurnaround / pacientes.size();
    double avgEspera = somaEspera / pacientes.size();

    log("\n=========== MÉTRICAS GERAIS ===========");
    log("Tempo Médio de Execução (Turnaround): " + String.format("%.2f", avgTurnaround));
    log("Tempo Médio de Espera: " + String.format("%.2f", avgEspera));

    log("\n========== FIM DA SIMULAÇÃO ==========\n");
}


    public static void reset() {
        synchronized (Prioridade.class) {

            pacientes = null;
            filaProcessos = null;

            finalizados.set(0);
            tempoAtual = 0;
            sistemaAtivo = false;

            ganttPorMedico.clear();
            tempoOcupadoPorMedico.clear();
            pacienteAtualPorMedico.clear();
            trocasContextoPorMedico.clear();

            medicosFinalizados.set(0);
            totalMedicos = 0;

            logCapturado = new StringBuilder();
        }
    }
}
