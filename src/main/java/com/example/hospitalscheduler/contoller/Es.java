package com.example.hospitalscheduler.contoller;

import com.example.hospitalscheduler.DTO.Paciente;
import com.example.hospitalscheduler.service.GanttService;
import com.example.hospitalscheduler.service.Medico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/escalar")
public class Es {

    @Autowired
    private GanttService ganttService;

    @PostMapping
    public String escalonar(@RequestParam String algoritmo,
                            @RequestParam(required = false) Integer quantum,
                            @RequestParam("nome[]") List<String> nomes,
                            @RequestParam("burst[]") List<Integer> bursts,
                            @RequestParam("arrival[]") List<Integer> arrivals,
                            @RequestParam("priority[]") List<Integer> prioridades,
                            Model model) {

        // Montando a lista de pacientes manualmente
        List<Paciente> pacientes = new ArrayList<>();

        for (int i = 0; i < nomes.size(); i++) {
            Paciente p = new Paciente();
            p.setNome(nomes.get(i));
            p.setBurst(bursts.get(i));
            p.setArrival(arrivals.get(i));
            p.setPriority(prioridades.get(i));

            pacientes.add(p);
        }

        // Resetando Gantt
        ganttService.reset();

        // Executando thread
        Medico medico = new Medico(algoritmo, quantum, pacientes);
        Thread t = new Thread(medico);
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Enviando Gantt para o HTML
        model.addAttribute("gantt", ganttService.getBlocks());

        return "resultado";
    }
}
