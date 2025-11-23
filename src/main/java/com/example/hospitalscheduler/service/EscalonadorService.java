package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import com.example.hospitalscheduler.DTO.ResultadoDTO;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EscalonadorService {

    // Método agora retorna ResultadoDTO para o Controller
    public ResultadoDTO start(String algoritmo, int nucleos, Integer quantum, List<Paciente> pacientes) {

        switch (algoritmo) {
            case "SJF":
                // Chama o SJF passando a lista completa e o número de núcleos
                ShortestJobFirst sjf = new ShortestJobFirst(pacientes, nucleos);
                return sjf.executar();

            case "RR":
                // Se você já atualizou o RoundRobin para retornar ResultadoDTO, use assim:
                // RoundRobin rr = new RoundRobin(quantum, pacientes, nucleos);
                // return rr.executar();
                return new ResultadoDTO("Round Robin precisa ser atualizado para o novo padrão", 0, 0, 0, 0);

            default:
                return new ResultadoDTO("Algoritmo não implementado ou não reconhecido", 0, 0, 0, 0);
        }
    }
}