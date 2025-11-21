package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class ShortestJobFirst {
    private List<Paciente> pacientes;
    private int nucleos;

    public ShortestJobFirst(List<Paciente> pacientes, int nucleos) {
        this.pacientes = pacientes;
        this.nucleos = nucleos;
    }

    public void executar() {
        //fila de prioridade ordenada menor duração primeiro
        //se houver empate, usa a ordem de chegada
        PriorityQueue<Paciente> filaProcessos = new PriorityQueue<>(
                Comparator.comparingInt(Paciente::getBurst)
                        .thenComparingInt(Paciente::getArrival)
        );

        //ordenando a lista original por ordem de chegada para facilitar a inserção na simulação
        pacientes.sort(Comparator.comparingInt(Paciente::getArrival));

        int tempoAtual = 0;
        int finalizados = 0;
        int total = pacientes.size();
        int indexChegada = 0;

        for (Paciente p : pacientes) {
            p.setRemaining(p.getBurst());
        }

        //array representando os núcleos, se null, o núcleo tá livre.
        Paciente[] cpus = new Paciente[nucleos];

        System.out.println("Início da simulação SJF (Shortest Job First)");

        while (finalizados < total) {
            System.out.println("\nTempo " + tempoAtual + ":");

            //adicionar processos que chegaram no tempo atual na fila de prontos
            while (indexChegada < total && pacientes.get(indexChegada).getArrival() <= tempoAtual) {
                Paciente p = pacientes.get(indexChegada);
                filaProcessos.offer(p);
                System.out.println("Chegada de novo processo: " + p.getNome() + " (Burst: " + p.getBurst() + ")");
                indexChegada++;
            }

            //alocar processos nas CPUs livres
            for (int i = 0; i < nucleos; i++) {
                if (cpus[i] == null && !filaProcessos.isEmpty()) {
                    Paciente p = filaProcessos.poll();
                    cpus[i] = p;
                    System.out.println("CPU " + i + " iniciou " + p.getNome() + " (Burst: " + p.getBurst() + ")");
                }
            }

            //executar 1 qantum em cada CPU ocupada
            for (int i = 0; i < nucleos; i++) {
                Paciente atual = cpus[i];

                if (atual != null) {
                    atual.setRemaining(atual.getRemaining() - 1);

                    System.out.println("CPU " + i + " executando " + atual.getNome() +
                            " (restante=" + atual.getRemaining() + ")");

                    //verifica se terminou
                    if (atual.getRemaining() == 0) {
                        System.out.println(atual.getNome() + " FINALIZADO no tempo " + (tempoAtual + 1));
                        cpus[i] = null; // Libera a CPU
                        finalizados++;
                    }
                }
            }

            tempoAtual++;
        }

        System.out.println("\nFim da simulação SJF");
    }
}