package com.example.hospitalscheduler.DTO;

public class Paciente {
    private int nome;
    private int arrival;
    private int burst;
    private int priority;
    private int RemainingTime;

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
}
