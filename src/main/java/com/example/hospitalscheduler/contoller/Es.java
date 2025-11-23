package com.example.hospitalscheduler.contoller;

import com.example.hospitalscheduler.DTO.Paciente;
import com.example.hospitalscheduler.service.GanttService;
import com.example.hospitalscheduler.service.Medico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/escalar")
public class Es {

    @Autowired
    private GanttService ganttService;

    @PostMapping
    public String escalonar(
            @RequestParam String algoritmo,
            @RequestParam(required = false) Integer quantum,
            @RequestParam("nome") List<String> nomes,
            @RequestParam("tempo") List<Integer> tempos,
            Model model) {

        // Limpa eventos antigos
        ganttService.reset();

        // Monta lista de pacientes recebidos do formulário
        List<Paciente> pacientes = new ArrayList<>();
        for (int i = 0; i < nomes.size(); i++) {
            pacientes.add(new Paciente(nomes.get(i), tempos.get(i)));
        }

        // Executa o algoritmo em thread
        Medico medico = new Medico(algoritmo, quantum, pacientes);
        Thread t = new Thread(medico);
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Envia os dados do gantt para o front
        model.addAttribute("gantt", ganttService.getBlocks());

        return "resultado";
    }
}
