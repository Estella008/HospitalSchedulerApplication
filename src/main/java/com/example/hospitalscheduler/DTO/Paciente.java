package com.example.hospitalscheduler.DTO;

public class Paciente {
    private int nome;
    private int arrival;
    private int burst;
    private int priority;
    private int RemainingTime;
    private int tempoFinalizacao;
    private int tempoEspera;
    private int turnaround;

    public int getArrival() { return arrival; }
    public void setArrival(int arrival) { this.arrival = arrival; }

    public int getBurst() { return burst; }
    public void setBurst(int burst) { this.burst = burst; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getRemaining() {
        return RemainingTime;
    }
    public void setRemaining(int remainingTime) {
        RemainingTime = remainingTime;
    }

    public int getNome() {
        return nome;
    }

    public void setNome(int nome) {
        this.nome = nome;
    }

    public int getRemainingTime() {
        return RemainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        RemainingTime = remainingTime;
    }

    public int getTempoFinalizacao() {
        return tempoFinalizacao;
    }

    public void setTempoFinalizacao(int tempoFinalizacao) {
        this.tempoFinalizacao = tempoFinalizacao;
    }

    public int getTempoEspera() {
        return tempoEspera;
    }

    public void setTempoEspera(int tempoEspera) {
        this.tempoEspera = tempoEspera;
    }

    public int getTurnaround() {
        return turnaround;
    }

    public void setTurnaround(int turnaround) {
        this.turnaround = turnaround;
    }
}
