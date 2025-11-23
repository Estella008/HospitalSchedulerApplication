package com.example.hospitalscheduler.contoller;
import com.example.hospitalscheduler.DTO.Paciente;
import com.example.hospitalscheduler.service.GanttService;
import com.example.hospitalscheduler.service.Medico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/escalar")
public class Es {

    @Autowired
    private GanttService ganttService;

    @PostMapping
    public String escalonar(@RequestParam String algoritmo,
                            @RequestParam Integer quantum,
                            @ModelAttribute("pacientes") List<Paciente> pacientes,
                            Model model) {

        // Reset Gantt
        ganttService.reset();

        // Cria thread
        Medico medico = new Medico(algoritmo, quantum, pacientes);
        Thread t = new Thread(medico);
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        model.addAttribute("gantt", ganttService.getBlocks());
        return "resultado";
    }
}

