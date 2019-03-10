package algorithms;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;

import java.util.List;
import java.util.stream.Collectors;

import static io.jenetics.engine.Limits.bySteadyFitness;

public class JeneticAlgorithm implements Algorithm {

    private TravelingThiefProblem problem;

    private double fitness(final Genotype gt) {
        final PermutationChromosome dc = (PermutationChromosome) gt.getChromosome(0);
        final BitChromosome bc = (BitChromosome) gt.getChromosome(1);

        List<Integer> permutationGenes = (List<Integer>) dc.stream().map(gene -> ((EnumGene) gene).getAlleleIndex()).collect(Collectors.toList());
        permutationGenes = permutationGenes.stream().map(integer -> integer + 1).collect(Collectors.toList());
        permutationGenes.add(0, 0);
        List<Boolean> booleanGenes = bc.stream().map(bitGene -> bitGene.booleanValue()).collect(Collectors.toList());
        Solution solution = problem.evaluate(permutationGenes, booleanGenes);

        if (solution.profit == -Double.MAX_VALUE || solution.time == Double.MAX_VALUE)
            return Double.MIN_VALUE;

        return solution.profit -  solution.time;
    }

    @Override
    public List<Solution> solve(TravelingThiefProblem problem) {
        this.problem = problem;

        Genotype encoding = Genotype.of(
                (Chromosome) PermutationChromosome.ofInteger(problem.numOfCities - 1),
                (Chromosome) BitChromosome.of(problem.numOfItems, 0.1)
        );

        final Engine engine = Engine
                .builder(this::fitness, encoding)
                .populationSize(1_000)
                .build();

        final List<EvolutionResult> genotypes = (List<EvolutionResult>) engine.stream()
                .limit(bySteadyFitness(1000))
                .collect(Collectors.toList());

        List<Solution> solutions = genotypes.stream()
                .map(evolutionResult -> (List<Solution>) evolutionResult.getPopulation()
                        .stream()
                        .map(o -> ((Phenotype)o).getGenotype())
                        .map(o -> getSolutionFromGenotype((Genotype) o))
                        .collect(Collectors.toList()))
                .flatMap(List::stream)
                .peek(solution -> solution.source = "JENETIC")
                .collect(Collectors.toList());
        NonDominatedSet nonDominatedSet = new NonDominatedSet();
        solutions.forEach(nonDominatedSet::add);

        return nonDominatedSet.entries;

    }

    private Solution getSolutionFromGenotype(Genotype genotype) {
        final PermutationChromosome dc = (PermutationChromosome) genotype.getChromosome(0);
        final BitChromosome bc = (BitChromosome) genotype.getChromosome(1);

        List<Integer> permutationGenes = (List<Integer>) dc.stream().map(gene -> ((EnumGene) gene).getAlleleIndex()).collect(Collectors.toList());
        List<Boolean> booleanGenes = bc.stream().map(bitGene -> bitGene.booleanValue()).collect(Collectors.toList());


        permutationGenes = permutationGenes.stream().map(integer -> integer + 1).collect(Collectors.toList());
        permutationGenes.add(0, 0);

        return problem.evaluate(permutationGenes, booleanGenes, true);
    }

}
