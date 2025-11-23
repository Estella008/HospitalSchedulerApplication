package com.example.hospitalscheduler.service;
import com.example.hospitalscheduler.DTO.GanttEvent;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;



@Service
public class GanttService {

    private static GanttService instance;

    private int time = 0;
    private List<GanttEvent> blocks = new ArrayList<>();

    public GanttService() { instance = this; }

    public static GanttService getInstance() { return instance; }

    public void add(String paciente, String medico, int start, int end) {
        blocks.add(new GanttEvent(paciente, medico, start, end));
        time = end;
    }

    public int getCurrentTime() { return time; }

    public List<GanttEvent> getBlocks() { return blocks; }

    public void reset() {
        blocks.clear();
        time = 0;
    }
}
