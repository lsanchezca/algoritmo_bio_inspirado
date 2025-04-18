import es.urjc.grafo.ABII.Algorithms.Algorithm;
import es.urjc.grafo.ABII.Model.Evaluator;
import es.urjc.grafo.ABII.Model.Instance;
import es.urjc.grafo.ABII.Model.Solution;
import org.junit.jupiter.api.Assertions;

import es.urjc.grafo.ABII.Algorithms.Export;


import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class AlgorithmGeneralTest {

    private static Map<String, Double> expectedQuality = new HashMap<>();

    private static void fillMap(){
        expectedQuality.put("instancia_00.txt", 245.7);
        expectedQuality.put("instancia_01.txt", 484.41);
        expectedQuality.put("instancia_03.txt", 807.04);
        expectedQuality.put("instancia_05.txt", 1122.45);
        expectedQuality.put("instancia_07.txt", 2349.78);
    }

    public static void generalTest(String instancePath, Algorithm algorithm, long maxTimePerInstance) {
        try {
            fillMap();
            File folder = new File(instancePath);
            long numberOfInstances = 0;
            Instant instant = Instant.now();
            for (File fileEntry : folder.listFiles()) {
                numberOfInstances++;
                Instance instance = new Instance(instancePath + File.separator + fileEntry.getName());
                Solution solution = algorithm.run(instance);
                Assertions.assertTrue(Evaluator.isFeasible(solution, instance), "La solución no es factible");
                double score = Evaluator.evaluate(solution, instance);
                System.out.println("Para la instancia " + fileEntry.getName() +
                        " el score del algoritmo " + algorithm + " es " + score);
                StringBuilder sb = new StringBuilder();
                sb.append("La solución es: \n");
                List<String> routeDetails = new ArrayList<>();
                for (int i = 0; i < solution.routes().length; i++) {
                    StringBuilder route = new StringBuilder();
                    route.append("Ruta del vehículo " + i + ": ");
                    for (int j = 0; j < solution.routes()[i].size() - 1; j++) {
                        route.append(solution.routes()[i].get(j) + " -> ");
                    }
                    route.append(solution.routes()[i].getLast());
                    routeDetails.add(route.toString());
                    sb.append(route.toString() + "\n");
                }
                System.out.println(sb);

                // Exportar los resultados a CSV
                Export.exportToCSV(routeDetails, fileEntry.getName());

                Assertions.assertTrue(
                        score <= expectedQuality.getOrDefault(fileEntry.getName(), Double.MAX_VALUE),
                        "La calidad de la solución no es suficiente: " + score + " vs " + expectedQuality.getOrDefault(fileEntry.getName(), Double.MAX_VALUE));
            }
            Duration elapsedTime = Duration.between(instant, Instant.now());
            Assertions.assertTrue(elapsedTime.getSeconds() < (maxTimePerInstance * numberOfInstances),
                    "El algoritmo ha tardado más de 60 segundos de media: " + elapsedTime.getSeconds() + " vs " + (maxTimePerInstance * numberOfInstances));
        }
        catch (UnsupportedOperationException e) {
            Assertions.fail(algorithm.toString() + " no está implementado");
        }
        catch (Exception e) {
            System.out.println("Error en la ejecución del algoritmo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
