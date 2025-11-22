package com.example.hospitalscheduler.contoller;

import com.example.hospitalscheduler.DTO.Paciente;
import com.example.hospitalscheduler.DTO.ResultadoDTO;
import com.example.hospitalscheduler.service.EscalonadorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
public class SimulacaoController {

    @Autowired
    private EscalonadorService escalonadorService;

    @PostMapping("/simular")
    public ResponseEntity<ResultadoDTO> simular(@RequestParam Map<String, String> params) throws Exception {

        String algoritmo = params.get("selectAlgoritmo");
        int nucleos = Integer.parseInt(params.get("selectNucleos"));
        Integer quantum = params.containsKey("quantum") && !params.get("quantum").isEmpty()
                ? Integer.parseInt(params.get("quantum")) : null;

        String pacientesJson = params.get("pacientes");
        ObjectMapper mapper = new ObjectMapper();
        List<Paciente> pacientes = mapper.readValue(pacientesJson, new TypeReference<List<Paciente>>() {});

        // Define IDs sequenciais
        for (int i = 0; i < pacientes.size(); i++) {
            pacientes.get(i).setNome(i + 1);
        }

        // Executa e recebe o resultado
        ResultadoDTO resultado = escalonadorService.start(algoritmo, nucleos, quantum, pacientes);

        return ResponseEntity.ok(resultado);
    }
}