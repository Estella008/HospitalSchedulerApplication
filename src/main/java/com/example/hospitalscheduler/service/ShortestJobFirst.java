package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import com.example.hospitalscheduler.DTO.ResultadoDTO;

import java.util.*;

public class ShortestJobFirst {
    private List<Paciente> pacientes;
    private int nucleos;

    // Buffer para armazenar o texto que antes ia para o console
    private StringBuilder log = new StringBuilder();

    private int trocasContexto = 0;
    private int tempoOcupadoMedico = 0;

    public ShortestJobFirst(List<Paciente> pacientes, int nucleos) {
        this.pacientes = pacientes;
        this.nucleos = nucleos;
    }

    // Métodos auxiliares para escrever no log
    private void logLn(String texto) {
        log.append(texto).append("\n");
    }
    private void logTxt(String texto) {
        log.append(texto);
    }

    public ResultadoDTO executar() {
        logLn("=== SIMULAÇÃO SHORTEST JOB FIRST (SJF) ===");

        // Ordena por chegada
        pacientes.sort(Comparator.comparingInt(Paciente::getArrival));
        for (Paciente p : pacientes) p.setRemaining(p.getBurst());

        PriorityQueue<Paciente> filaProntos = new PriorityQueue<>(
                Comparator.comparingInt(Paciente::getBurst).thenComparingInt(Paciente::getArrival)
        );

        Paciente[] cpus = new Paciente[nucleos];
        int finalizados = 0;
        int total = pacientes.size();
        int indexChegada = 0;
        int tempoAtual = 0;
        StringBuilder gantt = new StringBuilder();

        while (finalizados < total) {
            // Chegada
            while (indexChegada < total && pacientes.get(indexChegada).getArrival() <= tempoAtual) {
                Paciente novo = pacientes.get(indexChegada);
                filaProntos.add(novo);
                logLn(String.format("Tempo %d: [CHEGADA] Paciente %d (Burst: %d)", tempoAtual, novo.getNome(), novo.getBurst()));
                indexChegada++;
            }

            // Alocação (Troca de Contexto)
            for (int i = 0; i < nucleos; i++) {
                if (cpus[i] == null && !filaProntos.isEmpty()) {
                    Paciente p = filaProntos.poll();
                    cpus[i] = p;
                    trocasContexto++;
                    logLn(String.format("Tempo %d: [ALOCAÇÃO] Médico %d atende Paciente %d", tempoAtual, i, p.getNome()));
                }
            }

            // Execução
            for (int i = 0; i < nucleos; i++) {
                if (cpus[i] != null) {
                    Paciente p = cpus[i];
                    p.setRemaining(p.getRemaining() - 1);
                    tempoOcupadoMedico++;
                    gantt.append("P").append(p.getNome()).append("|");

                    if (p.getRemaining() == 0) {
                        int finish = tempoAtual + 1;
                        p.setTurnaround(finish - p.getArrival());
                        p.setTempoEspera(p.getTurnaround() - p.getBurst());
                        logLn(String.format("Tempo %d: [FINALIZADO] Paciente %d saiu do sistema.", finish, p.getNome()));
                        cpus[i] = null;
                        finalizados++;
                    }
                } else {
                    gantt.append("_|");
                }
            }
            tempoAtual++;
        }

        // Monta o resultado final
        double mediaEspera = pacientes.stream().mapToInt(Paciente::getTempoEspera).average().orElse(0);
        double mediaTurnaround = pacientes.stream().mapToInt(Paciente::getTurnaround).average().orElse(0);
        double utilizacao = ((double) tempoOcupadoMedico / (tempoAtual * nucleos)) * 100.0;

        logLn("\n=== RESULTADOS FINAIS ===");
        logLn("Gantt: " + gantt.toString());
        logLn(String.format("Tempo Médio de Espera: %.2f", mediaEspera));
        logLn(String.format("Turnaround Médio: %.2f", mediaTurnaround));
        logLn(String.format("Utilização CPU: %.2f%%", utilizacao));
        logLn("Trocas de Contexto: " + trocasContexto);

        return new ResultadoDTO(log.toString(), mediaEspera, mediaTurnaround, utilizacao, trocasContexto);
    }
}