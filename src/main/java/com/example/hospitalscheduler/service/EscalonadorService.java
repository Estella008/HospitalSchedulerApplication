package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscalonadorService {
    public Object executar(String algoritmo, int nucleos, Integer quantum, List<Paciente> pacientes) {

        switch (algoritmo) {
            case "RR":
                return executarRR(pacientes, quantum, nucleos);
            case "SJF":
                return executarSJF(pacientes, nucleos);
            case "SRTF":
                return executarSRTF(pacientes, nucleos);
            case "PRIORIDADE":
                return executarPrioridade(pacientes, nucleos);
            default:
                throw new IllegalArgumentException("Algoritmo não reconhecido");
        }
    }

    private Object executarRR(List<Paciente> pacientes, Integer quantum, int nucleos) {
        RoundRobin roundRobin = new RoundRobin(quantum, pacientes, nucleos);
        roundRobin.executar();
        return "RR executado com quantum = " + quantum + " e " + nucleos + " núcleos";
    }

    private Object executarSJF(List<Paciente> pacientes, int nucleos) {
        return "SJF executado";
    }

    private Object executarSRTF(List<Paciente> pacientes, int nucleos) {
        return "SRTF executado";
    }

    private Object executarPrioridade(List<Paciente> pacientes, int nucleos) {
        return "Prioridade executado";
    }
}
