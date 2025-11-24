package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EscalonadorService {

    public String start(String algoritmo, int nucleos, Integer quantum, List<Paciente> pacientes) {
        // Reset para nova simulação (limpa estado estático)
        resetAlgoritmo(algoritmo);

        // Criar threads dos médicos
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= nucleos; i++) {
            Medico medico;
            if (i == 1) {
                // Primeiro médico inicializa os dados compartilhados
                medico = new Medico(i, algoritmo, quantum, pacientes);

            } else {
                // Demais médicos apenas recebem o ID
                medico = new Medico(i);
            }
            Thread t = new Thread(medico);
            threads.add(t);
            t.start();
        }

        // Aguarda todos terminarem
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Retorna os logs capturados
        return getLogsAlgoritmo(algoritmo);
    }

    /**
     * Retorna os logs capturados de cada algoritmo
     */
    private String getLogsAlgoritmo(String algoritmo) {
        switch (algoritmo) {
            case "SRTF":
                return ShortestRemaining.getLogs();
            case "RR":
                return RoundRobin.getLogs();
            case "SJF":
                return ShortestJobFirst.getLogs();
            case "PRIORIDADE":
                return Prioridade.getLogs();
            default:
                return "Algoritmo desconhecido: " + algoritmo;
        }
    }

    /**
     * Reseta o estado estático de cada algoritmo antes de executar
     */
    private void resetAlgoritmo(String algoritmo) {
        switch (algoritmo) {
            case "SRTF":
                ShortestRemaining.reset();
                break;
            case "RR":
                RoundRobin.reset();
                break;
            case "SJF":
                ShortestJobFirst.reset();
                break;
            case "PRIORIDADE":
                Prioridade.reset();
                break;
            default:
                System.err.println("Algoritmo desconhecido: " + algoritmo);
        }
    }
}