package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;
import com.example.hospitalscheduler.DTO.ResultadoDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EscalonadorService {

    public void start(String algoritmo, int nucleos, Integer quantum, List<Paciente> pacientes) {
        // Reset para nova simulação
        if (algoritmo.equals("SRTF")) {
            ShortestRemaining.reset();
        }

        // Não precisa achatar para SRTF - todos processam a lista original
        List<Paciente> listaPacientes = algoritmo.equals("SRTF") ?
                pacientes : achatarLista(organizarListaPacientes(pacientes));

        // Criar threads dos médicos
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= nucleos; i++) {
            Medico medico;
            if (i == 1) {
                medico = new Medico(i, algoritmo, quantum, listaPacientes);
            } else {
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
    }
    private List<List<Paciente>> organizarListaPacientes(List<Paciente> filaPacientes) {

        List<List<Paciente>> listaSeparadora = new ArrayList<>();

        for (Paciente paciente : filaPacientes) {
            int chegada = paciente.getArrival();

            // Procura se já existe uma sublista com esse arrival
            List<Paciente> sublista = null;
            for (List<Paciente> grupo : listaSeparadora) {
                if (grupo.get(0).getArrival() == chegada) {
                    sublista = grupo;
                    break;
                }
            }

            // Se não existir um grupo ainda, cria
            if (sublista == null) {
                sublista = new ArrayList<>();
                listaSeparadora.add(sublista);
            }

            // Adiciona o paciente na sublista correta
            sublista.add(paciente);
        }

        // Agora ordena cada sublista pelo burst (menor primeiro)
        for (List<Paciente> grupo : listaSeparadora) {
            grupo.sort(Comparator.comparingInt(Paciente::getBurst));
        }

        return listaSeparadora;
    }

    private List<Paciente> achatarLista(List<List<Paciente>> listaSeparadora) {
        List<Paciente> listaFinal = new ArrayList<>();

        // Primeiro ordena a lista de listas pelo arrival do primeiro paciente
        listaSeparadora.sort(
                Comparator.comparingInt(lista -> lista.get(0).getArrival())
        );

        // Agora concatena tudo na ordem
        for (List<Paciente> grupo : listaSeparadora) {
            listaFinal.addAll(grupo);
        }

        return listaFinal;
    }
}