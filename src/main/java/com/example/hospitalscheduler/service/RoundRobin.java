package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin {

    private int quantum;
    private List<Paciente> pacientes;
    private int nucleos;

    //construtor
    public RoundRobin(int quantum, List<Paciente> pacientes, int nucleos) {
        this.quantum = quantum;
        this.pacientes = pacientes;
        this.nucleos = nucleos;
    }

    //cabeçalho
    private void printHeader() {
        System.out.println("\n========================================");
        System.out.println("        EXECUÇÃO ROUND ROBIN");
        System.out.println("========================================");
        System.out.println("Quantidade de Médicos (CPUs): " + nucleos);
        System.out.println("Quantidade de Pacientes: " + pacientes.size());
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
    private void printExecucao(Paciente[] cpus) {
        System.out.println("Médicos em Execução:");
        for (int i = 0; i < cpus.length; i++) {
            if (cpus[i] != null)
                System.out.println("   • Médico " + i + " → " + cpus[i].getNome());
            else
                System.out.println("   • Médico " + i + " → (ocioso)");
        }
    }

    //exibe os estados da CPU ao longo do tempo
    private void printGantt(Map<Integer, StringBuilder> gantt) {
        System.out.println("\nGANTT POR MÉDICO:");
        for (int i = 0; i < gantt.size(); i++) {
            System.out.println("CPU " + i + ": " + gantt.get(i));
        }
    }

    public void executar() {

        printHeader();

        //criando fila de processos
        Queue<Paciente> filaProcessos = new LinkedList<>();
        //ordenando cada processo por ordem de chegada
        pacientes.sort((a, b) -> Integer.compare(a.getArrival(), b.getArrival()));
        //simulação de um relógio
        int tempoAtual = 0;
        //AtomicInteger garante que não vai dar conflito
        AtomicInteger finalizados = new AtomicInteger(0);
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

        //para formar o Gantt
        Map<Integer, StringBuilder> gantt = new HashMap<>();
        for (int i = 0; i < nucleos; i++){
            gantt.put(i, new StringBuilder());
        }

        //criando array de threads
        Thread[] medicos = new Thread[nucleos];

        //criando identificação para cada médico
        for (int i = 0; i < nucleos; i++) {
            final int idCPU = i;

            //para cada posição do array é criada uma thread
            medicos[i] = new Thread(() -> {

                while (true) {
                    //armazena paciente que médico irá atender
                    Paciente atual;

                    //sincroniza para duas CPUs não mexam nela ao mesmo tempo
                    synchronized (filaProcessos) {
                        //verifica se todos os pacientes foram atendidos
                        if (finalizados.get() >= total) break;

                        //CPU livre, pega outro paciente da fila
                        if (cpus[idCPU] == null && !filaProcessos.isEmpty()) {
                            //retira da fila
                            Paciente p = filaProcessos.poll();
                            //coloca na cpu
                            cpus[idCPU] = p;
                            //inicializa o quantum
                            quantumRestante[idCPU] = quantum;

                            //exibe a troca de contexto
                            System.out.println("TROCA DE CONTEXTO → Médico " + idCPU +
                                    " iniciou " + p.getNome());
                        }

                        //atribui paciente a variável
                        atual = cpus[idCPU];
                    }

                    if (atual != null) {
                        //simula 1s
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }

                        //diminui o quantum e o burst
                        atual.setRemaining(atual.getRemaining() - 1);
                        quantumRestante[idCPU]--;

                        //grava qual paciente foi atendido pelo médico
                        gantt.get(idCPU).append(atual.getNome()).append("|");

                        //exibe como está a execução
                        System.out.println("Médico " + idCPU + " executando " + atual.getNome() +
                                " (restante=" + atual.getRemaining() +
                                ", quantum=" + quantumRestante[idCPU] + ")");


                        //analisa se o paciente terminou
                        if (atual.getRemaining() == 0) {
                            System.out.println("FINALIZADO → " + atual.getNome());
                            cpus[idCPU] = null;
                            finalizados.incrementAndGet();
                            continue;
                        }

                        //volta o processo para a fila quando o quantum acaba
                        if (quantumRestante[idCPU] == 0) {
                            System.out.println("Quantum acabou → " + atual.getNome() +
                                    " volta para fila");
                            synchronized (filaProcessos) {
                                filaProcessos.add(atual);
                                cpus[idCPU] = null;
                            }
                        }

                        //fica ociosa e espera meio segundo para tentar pegar outro processo
                    } else {
                        gantt.get(idCPU).append("ocioso|");
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                        }
                    }

                }
            });

            //médico começa a executar em paralelo
            medicos[i].start();
        }
        while (true) {
            //indica o tempo atual
            System.out.println("\n--------------------------------------");
            System.out.println("Tempo " + tempoAtual);
            System.out.println("--------------------------------------");

            //adiciona processos que chegaram no tempo atual
            while (indexChegada < total && pacientes.get(indexChegada).getArrival() <= tempoAtual) {
                Paciente p = pacientes.get(indexChegada);
                //sincroniza pois a fila está sendo acessada por muitas threads ao mesmo tempo
                synchronized (filaProcessos) {
                    filaProcessos.add(p);
                }
                //exibe que um novo paciente chegou
                System.out.println("Nova chegada: " + p.getNome());
                indexChegada++;
            }
            //sincroniza a fila para exibir seu estado atual
            synchronized (filaProcessos) {
                printFila(filaProcessos);
                printExecucao(cpus);

                if (finalizados.get() >= total) break;
            }

            tempoAtual++;

            //colocando thread para dormir
            try { Thread.sleep(100);
            } catch (Exception e) {

            }
        }

        for (Thread t : medicos) {
            try { t.join();
            } catch (Exception e) {

            }
        }

        System.out.println("\n================ RESULTADOS ================\n");
        printGantt(gantt);
        System.out.println("\nFim da simulação.\n");
    }
}
