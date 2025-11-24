package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import java.util.*;

public class Medico implements Runnable {
    private int idMedico;
    private static String algoritmo;
    private static List<Paciente> filaPacientes;
    private static Integer quantumMax;

    public Medico(int idMedico, String algoritmoSelecionado, Integer quantum, List<Paciente> listaPacientes) {
        this.idMedico = idMedico;
        Medico.algoritmo = algoritmoSelecionado;
        Medico.filaPacientes = listaPacientes;
        Medico.quantumMax = quantum;
    }

    public Medico(int idMedico) {
        this.idMedico = idMedico;
    }

    public boolean executar(String algoritmo, Integer quantum, Paciente paciente) {
        // Paciente não é usado aqui, pois cada algoritmo gerencia internamente
        try {
            switch (algoritmo) {
                case "RR":
                    executarRR();
                    return true;
                case "SJF":
                    executarSJF();
                    return true;
                case "SRTF":
                    executarSRTF();
                    return true;
                case "PRIORIDADE":
                    executarPrioridade();
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("[Médico " + idMedico + "] Erro: " + e.getMessage());
            return false;
        }
    }

    private void executarSRTF() {
        // Médico participa da execução paralela do SRTF
        ShortestRemaining srtf = new ShortestRemaining(idMedico, filaPacientes);
        srtf.executar();
    }

    private void executarSJF() {
        // Médico participa da execução paralela do SJF
       ShortestJobFirst sjt = new ShortestJobFirst(idMedico,filaPacientes);
       sjt.executar();
    }

    private void executarPrioridade() {
        // Médico participa da execução paralela do Prioridade
        Prioridade prio = new Prioridade(idMedico, filaPacientes);
        prio.executar();
    }

    private void executarRR() {
        // Médico participa da execução paralela do Round Robin
        RoundRobin rr = new RoundRobin(idMedico, quantumMax, filaPacientes);
        rr.executar();
    }

    @Override
    public void run() {
        System.out.println("[Médico " + idMedico + "] Iniciou atendimento - Algoritmo: " + algoritmo);

        // Todos os algoritmos agora executam de forma paralela
        executar(algoritmo, quantumMax, null);

        System.out.println("[Médico " + idMedico + "] Finalizou atendimento");
    }
}