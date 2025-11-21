package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobin {
    private int quantum;
    private List<Paciente> pacientes;
    private int nucleos;

    public RoundRobin(int quantum, List<Paciente> pacientes, int nucleos) {
        this.quantum = quantum;
        this.pacientes = pacientes;
        this.nucleos = nucleos;
    }

    public void executar() {
        //criando fila de processos
        Queue<Paciente> filaProcessos = new LinkedList<>();
        //ordenando cada processo por ordem de chegada
        pacientes.sort((a, b) -> Integer.compare(a.getArrival(), b.getArrival()));
        //adicionando cada processo na fila
        for (Paciente p : pacientes) {
            filaProcessos.offer(p);
        }

        //simulação de um relógio
        int tempoAtual = 0;
        //quantidade de processos finalizados
        int finalizados = 0;
        //analisa quantos processos tem
        int total = pacientes.size();
        //inidce para percorrer a lista de ordem de chegada
        int indexChegada = 0;

        //inicializando a marcação do tempo restante de cada paciente
        for (Paciente p : pacientes) {
            p.setRemaining(p.getBurst());
        }

        //array de pacientes associado a um nucleo da cpu
        Paciente[] cpus = new Paciente[nucleos];
        //controla o quantum de cada processo que esta sendo processado
        int[] quantumRestante = new int[nucleos];

        System.out.println("      Início da simulação Round-Robin      ");

        // Loop principal
        while (finalizados < total) {

            System.out.println("\nTempo " + tempoAtual + ":");

            //adicionar processos que chegaram no tempo atual
            while (indexChegada < total && pacientes.get(indexChegada).getArrival() <= tempoAtual) {
                Paciente p = pacientes.get(indexChegada);
                filaProcessos.add(p);
                System.out.println("Chegada de novo processo: " + p.getNome());
                indexChegada++;
            }

            //alocar processos as CPUs livres
            for (int i = 0; i < nucleos; i++) {
                //adiciona se nao tem processo nessa CPU e se tiver mais processos na fila
                if (cpus[i] == null && !filaProcessos.isEmpty()) {
                    //retorna processo e o tira da fila
                    Paciente p = filaProcessos.poll();
                    cpus[i] = p;
                    quantumRestante[i] = quantum;

                    System.out.println("CPU " + i + " iniciou " + p.getNome());
                }
            }

            //executar 1 unidade de tempo em cada CPU
            for (int i = 0; i < nucleos; i++) {
                Paciente atual = cpus[i];

                if (atual != null) {
                    atual.setRemaining(atual.getRemaining() - 1);
                    quantumRestante[i]--;

                    System.out.println("CPU " + i + " executando " + atual.getNome() +
                            " (restante=" + atual.getRemaining() +
                            ", quantumRestante=" + quantumRestante[i] + ")");

                    //analisa se o processo terminou
                    if (atual.getRemaining() == 0) {
                        System.out.println(+ atual.getNome() + " FINALIZADO no tempo " + (tempoAtual + 1));

                        cpus[i] = null;
                        finalizados++;
                        continue;
                    }

                    //finaliza o quantum do processo
                    if (quantumRestante[i] == 0) {
                        System.out.println("Quantum terminou para " + atual.getNome() + ", voltando para fila");
                        filaProcessos.add(atual);
                        cpus[i] = null;
                    }
                }
            }

            //avançar o tempo
            tempoAtual++;
        }

        System.out.println("\n    Fim da simulação     ");
    }
}
