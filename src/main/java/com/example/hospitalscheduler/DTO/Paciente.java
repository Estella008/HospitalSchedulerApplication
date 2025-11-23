package com.example.hospitalscheduler.DTO;

public class Paciente {
    private int nome;
    private int arrival; //chegada
    private int burst; //tempo de duração
    private int priority; //prioridade
    private int RemainingTime; //tempo restante de atendimento
    private int tempoFinalizacao; //tempo que o processo finalizou
    private int tempoEspera; //quanto tempo paciente ficou esperando na fila
    private int turnaround; //tempo total da chegada até a finalização

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
