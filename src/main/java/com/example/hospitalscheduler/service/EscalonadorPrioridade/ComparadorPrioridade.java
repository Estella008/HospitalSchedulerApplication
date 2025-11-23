import java.util.Comparator;
import com.example.hospitalscheduler.DTO.Paciente;
class ComparadorPrioridade implements Comparator<Paciente> {
    @Override
    public int compare(Paciente p1, Paciente p2) {
        // Se as prioridades forem diferentes, ordena por prioridade
        if (p1.getPriority() != p2.getPriority()) {
            return p1.getPriority() - p2.getPriority();
        }
        // Se as prioridades forem iguais, ordena por tempo de chegada (FCFS)
        return p1.getArrival() - p2.getArrival();
    }
}