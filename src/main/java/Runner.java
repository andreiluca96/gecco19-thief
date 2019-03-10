import algorithms.Algorithm;
import algorithms.ExhaustiveSearch;
import algorithms.JeneticAlgorithm;
import algorithms.RandomLocalSearch;
import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class Runner {


    static final ClassLoader LOADER = Runner.class.getClassLoader();

    public static void main(String[] args) throws IOException {

        List<String> instanceToRun = Arrays.asList("a280-n279");
        //List<String> instanceToRun = Competition.INSTANCES;

        for (String instance : instanceToRun) {

            // readProblem the problem from the file
            String fname = String.format("resources/%s.txt", instance);
            InputStream is = LOADER.getResourceAsStream(fname);

            TravelingThiefProblem problem = Util.readProblem(is);
            problem.name = instance;

            // number of solutions that will be finally necessary for submission - not used here
            int numOfSolutions = Competition.numberOfSolutions(problem);

            // initialize your algorithm
            Algorithm randomAlgorithm = new RandomLocalSearch(500);
            Algorithm jeneticAlgorithm = new JeneticAlgorithm();

            // use it to to solve the problem and return the non-dominated set
            List<Solution> nds = randomAlgorithm.solve(problem);
            List<Solution> jds = jeneticAlgorithm.solve(problem);

            NonDominatedSet ndsFinal = new NonDominatedSet();
            nds.forEach(ndsFinal::add);
            jds.forEach(ndsFinal::add);

            List<Solution> finalSolution = ndsFinal.entries;

            // sort by time and printSolutions it
            finalSolution.sort(Comparator.comparing(a -> a.time));
            long jeneticSolutions = finalSolution.stream().filter(solution -> solution.source.equals("JENETIC")).count();

            System.out.println(1.0 * jeneticSolutions / finalSolution.size());


//            System.out.println(finalSolution.size());
//            for(Solution s : finalSolution) {
//                System.out.println(s.time + " " + s.profit);
//            }
//
//            Util.printSolutions(finalSolution, true);
//            System.out.println(problem.name + " " + finalSolution.size());
//
//            File dir = new File("results");
//            if (!dir.exists()) dir.mkdirs();
//            Util.writeSolutions("results", "TEAM_JENETICS", problem, finalSolution);


        }



    }

}