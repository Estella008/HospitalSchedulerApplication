package com.example.hospitalscheduler.utils;


import java.util.ArrayList;
import java.util.List;

import com.example.hospitalscheduler.DTO.GanttEvent;

public class GanttTimeline {
    private List<GanttEvent> eventos = new ArrayList<>();

    public void addEvent(GanttEvent evento) {
        eventos.add(evento);
    }

    public List<GanttEvent> getEventos() {
        return eventos;
    }
}
