package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ShortestRemaining {

    private int idMedico;
    private static List<Paciente> pacientes;
    private static final Object lock = new Object();
    private static AtomicInteger finalizados = new AtomicInteger(0);
    private static volatile int tempoAtual = 0;
    private static volatile boolean sistemaAtivo = true;
    private static Map<Integer, StringBuilder> ganttPorMedico = new HashMap<>();
    private static Map<Integer, Integer> trocasContextoPorMedico = new HashMap<>();
    private static Map<Integer, Integer> tempoOcupadoPorMedico = new HashMap<>();
    private static AtomicInteger medicosFinalizados = new AtomicInteger(0);
    private static int totalMedicos = 0;

    public ShortestRemaining(int idMedico, List<Paciente> listaPacientes) {
        this.idMedico = idMedico;

        synchronized (ShortestRemaining.class) {
            if (pacientes == null) {
                pacientes = listaPacientes;
                finalizados.set(0);
                tempoAtual = 0;
                sistemaAtivo = true;
                ganttPorMedico.clear();
                trocasContextoPorMedico.clear();
                tempoOcupadoPorMedico.clear();
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
        }
    }

    private void printHeader() {
        synchronized (lock) {
            System.out.println("\n========================================");
            System.out.println("   EXECUÇÃO SRTF - MÉDICO " + idMedico);
            System.out.println("========================================");
            System.out.println("Total de Pacientes: " + pacientes.size());
            for (Paciente p : pacientes) {
                System.out.println("  - Paciente " + p.getNome() +
                        ": Arrival=" + p.getArrival() +
                        ", Burst=" + p.getBurst());
            }
            System.out.println("========================================\n");
        }
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

        Paciente pacienteAtual = null;
        int total = pacientes.size();

        while (sistemaAtivo && finalizados.get() < total) {

            synchronized (lock) {
                // Encontra pacientes disponíveis no tempo atual
                List<Paciente> disponiveis = new ArrayList<>();

                for (Paciente p : pacientes) {
                    if (p.getArrival() <= tempoAtual && p.getRemaining() > 0) {
                        disponiveis.add(p);
                    }
                }

                if (disponiveis.isEmpty()) {
                    // Nenhum paciente disponível ainda
                    ganttPorMedico.get(idMedico).append("ocioso|");
                    pacienteAtual = null;
                } else {
                    // Escolhe o com menor tempo restante
                    Paciente menorRestante = disponiveis.get(0);
                    for (Paciente p : disponiveis) {
                        if (p.getRemaining() < menorRestante.getRemaining()) {
                            menorRestante = p;
                        } else if (p.getRemaining() == menorRestante.getRemaining()) {
                            // Desempate por arrival
                            if (p.getArrival() < menorRestante.getArrival()) {
                                menorRestante = p;
                            }
                        }
                    }

                    // Verifica se algum médico já está processando este paciente
                    boolean pacienteDisponivel = true;
                    for (int i = 1; i <= ganttPorMedico.size(); i++) {
                        if (i != idMedico) {
                            StringBuilder ganttOutro = ganttPorMedico.get(i);
                            if (ganttOutro != null && ganttOutro.length() > 0) {
                                String[] partes = ganttOutro.toString().split("\\|");
                                if (partes.length > 0) {
                                    String ultimoAtendido = partes[partes.length - 1];
                                    if (ultimoAtendido.equals(String.valueOf(menorRestante.getNome()))) {
                                        pacienteDisponivel = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (!pacienteDisponivel) {
                        // Outro médico está atendendo, fica ocioso
                        ganttPorMedico.get(idMedico).append("ocioso|");
                        pacienteAtual = null;
                    } else {
                        // Pode atender
                        if (menorRestante != pacienteAtual) {
                            if (pacienteAtual != null && pacienteAtual.getRemaining() > 0) {
                                System.out.println("[Médico " + idMedico + "] PREEMPÇÃO → Paciente " +
                                        pacienteAtual.getNome() + " pausado");
                            }
                            System.out.println("[Médico " + idMedico + "] TROCA DE CONTEXTO → iniciou " +
                                    menorRestante.getNome());
                            trocasContextoPorMedico.put(idMedico, trocasContextoPorMedico.get(idMedico) + 1);
                        }

                        pacienteAtual = menorRestante;

                        // Executa 1 unidade
                        pacienteAtual.setRemaining(pacienteAtual.getRemaining() - 1);
                        ganttPorMedico.get(idMedico).append(pacienteAtual.getNome()).append("|");
                        tempoOcupadoPorMedico.put(idMedico, tempoOcupadoPorMedico.get(idMedico) + 1);

                        System.out.println("[Médico " + idMedico + "] executando " +
                                pacienteAtual.getNome() + " (restante=" +
                                pacienteAtual.getRemaining() + ")");

                        if (pacienteAtual.getRemaining() == 0) {
                            System.out.println("[Médico " + idMedico + "] FINALIZADO → " +
                                    pacienteAtual.getNome());
                            pacienteAtual.setTempoFinalizacao(tempoAtual + 1);
                            finalizados.incrementAndGet();
                            pacienteAtual = null;
                        }
                    }
                }

                // Avança o tempo (apenas um médico avança)
                if (idMedico == 1) {
                    tempoAtual++;
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

            // ============================================
            //         MÉTRICAS DE DESEMPENHO (NOVO)
            // ============================================

            System.out.println("\n--- MÉTRICAS POR PACIENTE ---");

            double somaTurnaround = 0;
            double somaEspera = 0;

            for (Paciente p : pacientes) {

                int turnaround = p.getTempoFinalizacao() - p.getArrival();
                int espera = turnaround - p.getBurst();

                p.setTurnaround(turnaround);
                p.setTempoEspera(espera);

                somaTurnaround += turnaround;
                somaEspera += espera;

                System.out.println("Paciente " + p.getNome()
                        + " | Finalização: " + p.getTempoFinalizacao()
                        + " | Turnaround: " + turnaround
                        + " | Espera: " + espera
                );
            }

            double avgTurnaround = somaTurnaround / pacientes.size();
            double avgEspera = somaEspera / pacientes.size();

            System.out.println("\n--- MÉTRICAS GERAIS ---");
            System.out.println("Tempo Médio de Execução (Turnaround): " + String.format("%.2f", avgTurnaround));
            System.out.println("Tempo Médio de Espera: " + String.format("%.2f", avgEspera));

            System.out.println("\nFim da simulação.\n");
        }


        public static void reset() {
        synchronized (ShortestRemaining.class) {
            pacientes = null;
            finalizados.set(0);
            tempoAtual = 0;
            sistemaAtivo = false;
            ganttPorMedico.clear();
            trocasContextoPorMedico.clear();
            tempoOcupadoPorMedico.clear();
            medicosFinalizados.set(0);
            totalMedicos = 0;
        }
    }
}