package com.example.hospitalscheduler.service.EscalonadorPrioridade;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import com.example.hospitalscheduler.DTO.Paciente;



public class EscolonadorPrioridadeNaoPreempitivo {
      public static void escalonar(List<Paciente> pacientes) {

    // Fila de prontos, ordenada por prioridade (menor valor = maior prioridade)
    PriorityQueue<Paciente> filaProntos = new PriorityQueue<>(new ComparadorPrioridade());

    List<Paciente> concluidos = new ArrayList<>();

    int tempoAtual = 0;
    int restantes = pacientes.size();

    while (restantes > 0) {

        // Adiciona pacientes que já chegaram
        for (Paciente p : pacientes) {
            if (p.getArrival() <= tempoAtual && p.getTempoFinalizacao() == 0) {
                if (!filaProntos.contains(p)) {
                    filaProntos.add(p);
                }
            }
        }

        if (!filaProntos.isEmpty()) {

            // Pega o paciente de maior prioridade
            Paciente atual = filaProntos.poll();

            // Executa até o fim (não preemptivo)
            tempoAtual += atual.getBurst();

            atual.setTempoFinalizacao(tempoAtual);

            int turnaround = tempoAtual - atual.getArrival();
            atual.setTurnaround(turnaround);

            int espera = turnaround - atual.getBurst();
            atual.setTempoEspera(espera);

            concluidos.add(atual);

            restantes--;

        } else {
            // Ninguém na fila → avança o tempo
            tempoAtual++;
        }
    }

    // Exibe os resultados
    System.out.println("\n--- Resultados do Escalonamento por Prioridade Não-Preemptivo ---");
    System.out.println("Paciente | Burst | Chegada | Prioridade | Espera | Turnaround");

    for (Paciente p : concluidos) {
        System.out.printf("%-8d | %-5d | %-7d | %-10d | %-6d | %-10d\n",
                p.getNome(), p.getBurst(), p.getArrival(),
                p.getPriority(), p.getTempoEspera(), p.getTurnaround());
    }

    double esperaMedia = concluidos.stream()
            .mapToInt(Paciente::getTempoEspera)
            .average().orElse(0);

    double turnaroundMedio = concluidos.stream()
            .mapToInt(Paciente::getTurnaround)
            .average().orElse(0);

    System.out.printf("\nTempo de Espera Médio: %.2f\n", esperaMedia);
    System.out.printf("Tempo de Turnaround Médio: %.2f\n", turnaroundMedio);
}

}
