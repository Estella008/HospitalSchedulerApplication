package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EscalonadorService {


    public void start(String algoritimo, int nucleos, Integer quantum, List<Paciente> pacientes) {
        for (int i = 1; i <= nucleos; i++) {
            if(!Medico.iniciou) {
                new Thread(new Medico(algoritimo, quantum, pacientes)).start();
            }else{
                new Thread(new Medico());
            }
        }
    }
}
