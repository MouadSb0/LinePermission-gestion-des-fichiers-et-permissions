package ma.youcode.lineperm.service;

import ma.youcode.lineperm.model.AccessLog;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatsService {
    private final List<AccessLog> logs;

    public StatsService(List<AccessLog> logs) {
        this.logs = logs;
    }

    public long getNombreTotalActions() {
        return logs.stream().count();
    }

    public long getNombreAccesRefuses() {
        return logs.stream()
                .filter(log -> "REFUSE".equals(log.getResultat()))
                .count();
    }

    public List<String> getUtilisateursDistincts() {
        return logs.stream()
                .map(AccessLog::getUtilisateur)
                .distinct()
                .collect(Collectors.toList());
    }

    public Map<String, Long> getActionsParUtilisateur() {
        return logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getUtilisateur, Collectors.counting()));
    }

    public List<Map.Entry<String, Long>> getTop3Fichiers() {
        Map<String, Long> parFichier = logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getFichier, Collectors.counting()));

        return parFichier.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    public List<AccessLog> getAccesRefusesUtilisateur(String nom) {
        return logs.stream()
                .filter(log -> log.getUtilisateur().equals(nom))
                .filter(log -> "REFUSE".equals(log.getResultat()))
                .collect(Collectors.toList());
    }

    public Optional<Map.Entry<String, Long>> getUtilisateurLePlusActif() {
        Map<String, Long> parUtilisateur = getActionsParUtilisateur();
        return parUtilisateur.entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    public Map<String, Long> getRepartitionParType() {
        return logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getAction, Collectors.counting()));
    }
}
