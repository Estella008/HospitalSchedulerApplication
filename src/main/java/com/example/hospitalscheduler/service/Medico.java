package com.example.hospitalscheduler.service;

import com.example.hospitalscheduler.DTO.Paciente;

import java.util.ArrayList;
import java.util.List;

public class Medico implements Runnable{
    private static String algoritimo;
    private static List<Paciente> filaPacientes = new ArrayList<>();
    private static Integer quantumMax;
    //quando finalizar deixar  false
    static public boolean iniciou;
    //usado na primeira chamada
    public Medico(String algoritimoSelecinado,Integer quantum ,List<Paciente> listaPacientes){
        algoritimo = algoritimoSelecinado;
        filaPacientes = listaPacientes;
        quantumMax = quantum;
        iniciou = true;
    }
    //usado nas outras chamadas
    public Medico(){
    }

    public synchronized void adicionar(Paciente paciente) {
        filaPacientes.add(paciente);
        notify();
    }

    public Boolean executar(String algoritmo, Integer quantum,Paciente paciente) {
        if(paciente == null) return false;
        switch (algoritmo) {
            case "RR":
                 executarRR(paciente, quantum);
                 return true;
            case "SJF":
                 executarSJF(paciente);
                return true;
            case "SRTF":
                executarSRTF(paciente);
                return true;
            case "PRIORIDADE":
                executarPrioridade(paciente);
                return true;
            default:
                return false;


        }
    }

    private Object executarSRTF(Paciente paciente) {
        return "SRTF executado";
    }

    private Object executarPrioridade(Paciente paciente) {
        return "Prioridade executado";
    }

    private Object executarRR(Paciente paciente, Integer quantum) {
        RoundRobin roundRobin = new RoundRobin(quantum, paciente);
        roundRobin.executar();
        //alterar o retorno para se executar retorno true se não retorne false
        return "RR executado com quantum = " + quantum + " e núcleos";
    }

    private Object executarSJF(Paciente paciente) {
        ShortestJobFirst sjf = new ShortestJobFirst(paciente);
        sjf.executar();
        //alterar o retorno para se executar retorno true se não retorne false
        return "SJF (Shortest Job First) executado com  núcleos.";
    }

    @Override
    public void run() {

        while (true) {
            boolean executou;
            Paciente pacienteVez = null;
           synchronized (filaPacientes){
               if(filaPacientes.isEmpty()){
                   break;
               }else{
                   pacienteVez = filaPacientes.remove(0);

               }
           }
            executou = executar(algoritimo,quantumMax,pacienteVez);

            if(!executou){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
