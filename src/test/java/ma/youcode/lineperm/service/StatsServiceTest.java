package ma.youcode.lineperm.service;

import ma.youcode.lineperm.model.AccessLog;
import java.util.Arrays;
import java.util.List;

public class StatsServiceTest {
    public static void main(String[] args) {
        List<AccessLog> logs = Arrays.asList(
            AccessLog.fromLine("2026-09-15;10:00;alice;LECTURE;fichier.txt;OK"),
            AccessLog.fromLine("2026-09-15;10:05;alice;ECRITURE;fichier.txt;REFUSE"),
            AccessLog.fromLine("2026-09-15;10:10;bob;LECTURE;fichier.txt;OK")
        );
        
        StatsService service = new StatsService(logs);
        
        System.out.println("Test getNombreTotalActions: " + (service.getNombreTotalActions() == 3 ? "PASS" : "FAIL"));
        System.out.println("Test getNombreAccesRefuses: " + (service.getNombreAccesRefuses() == 1 ? "PASS" : "FAIL"));
        System.out.println("Test getUtilisateursDistincts: " + (service.getUtilisateursDistincts().size() == 2 ? "PASS" : "FAIL"));
        
        System.out.println("All tests finished.");
    }
}
