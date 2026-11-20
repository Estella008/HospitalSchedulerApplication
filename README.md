# Hospital Scheduler Application 🏥⏱️

> **Trabalho da disciplina de Sistemas Operacionais**  
> **Curso:** Sistemas de Informação  
> **Instituição:** IFMG (Instituto Federal de Minas Gerais)  

Este projeto consiste numa aplicação **Spring Boot** que simula o funcionamento de algoritmos clássicos de escalonamento de CPU, aplicando-os metaforicamente a um contexto de triagem e atendimento hospitalar.

## 👥 Autores

Trabalho realizado em grupo por:
* **Aquiles**
* **Estella**
* **Paola**
* **Matheus**

---

## 📖 Sobre o Projeto

O objetivo principal é demonstrar o comportamento de sistemas multiprocessados e a gestão de filas de processos através de uma interface visual e temática.

Nesta simulação, os conceitos de SO são mapeados da seguinte forma:
* **CPU/Núcleo** ➡️ **Médico**
* **Processo** ➡️ **Paciente**
* **Burst Time** ➡️ **Duração do Atendimento**
* **Arrival Time** ➡️ **Tempo de Chegada**
* **Prioridade** ➡️ **Gravidade do Paciente**

## 🚀 Funcionalidades

* **Simulação de Algoritmos:**
    * **Round Robin (RR):** Escalonamento circular com *Quantum* (preemptivo).
    * **Shortest Job First (SJF):** Prioriza o processo mais curto (não preemptivo).
    * **Shortest Remaining Time First (SRTF):** Prioriza o processo com menor tempo restante (preemptivo).
    * **Prioridade:** Execução baseada no nível de urgência (não preemptivo).
* **Multiprocessamento:** Suporte para simulação com **1, 2 ou 4 médicos** (threads) a trabalhar em paralelo.
* **Métricas de Desempenho:** Cálculo automático de *Turnaround Time* e *Tempo de Espera*.
* **Logs Visuais:** Exibição passo a passo da execução, trocas de contexto e diagrama de Gantt em formato de texto.

## 🛠️ Tecnologias Utilizadas

* **Java 17**
* **Spring Boot 3.5.7** (Web, Thymeleaf)
* **Maven** (Gestão de dependências)
* **Frontend:** HTML5, Bootstrap 5.3, JavaScript (Fetch API)
* **Concorrência:** Uso de `Threads`, `AtomicInteger` e blocos `synchronized` para gestão de recursos partilhados.

## ⚙️ Como Executar

### Pré-requisitos
* Java JDK 17+ instalado.

### Passo a passo

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/Estella008/HospitalSchedulerApplication.git
    cd HospitalSchedulerApplication
    ```

2.  **Execute a aplicação via Maven Wrapper:**
    * **Windows:**
        ```cmd
        mvnw.cmd spring-boot:run
        ```
    * **Linux / macOS:**
        ```bash
        ./mvnw spring-boot:run
        ```

3.  **Acesse no navegador:**
    Abra [http://localhost:8081](http://localhost:8081)

## 📋 Como Usar

1.  Na tela inicial, selecione o **Algoritmo** de escalonamento.
2.  Escolha o número de **Médicos** (Núcleos).
3.  Se escolheu *Round Robin*, defina o valor do **Quantum**.
4.  Adicione os pacientes informando:
    * Tempo de Chegada
    * Duração (Burst)
    * Prioridade (Quanto menor o número, maior a prioridade ou vice-versa, dependendo da implementação específica do algoritmo selecionado).
5.  Clique em **"Iniciar Simulação"** para ver os resultados e o Gráfico de Gantt textual.

---
*Este projeto foi desenvolvido para fins académicos.*
