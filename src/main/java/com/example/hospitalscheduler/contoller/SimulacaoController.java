package com.example.hospitalscheduler.contoller;

import com.example.hospitalscheduler.DTO.Paciente;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.hospitalscheduler.service.EscalonadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class SimulacaoController {

    @Autowired
    private EscalonadorService escalonadorService;

    @PostMapping("/simular")
    @ResponseBody // devolve JSON
    public Object simular(@RequestParam Map<String, String> params) throws Exception {

        String algoritmo = params.get("selectAlgoritmo");
        int nucleos = Integer.parseInt(params.get("selectNucleos"));
        Integer quantum = params.containsKey("quantum") && !params.get("quantum").isEmpty()
                ? Integer.parseInt(params.get("quantum"))
                : null;

        String pacientesJson = params.get("pacientes");

        ObjectMapper mapper = new ObjectMapper();
        List<Paciente> pacientes = mapper.readValue(
                pacientesJson,
                new TypeReference<List<Paciente>>() {}
        );

        // chamar o algoritmo correto
        return escalonadorService.executar(algoritmo, nucleos, quantum, pacientes);
    }
}
