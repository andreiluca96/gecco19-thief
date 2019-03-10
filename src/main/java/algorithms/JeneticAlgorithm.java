package algorithms;

import io.jenetics.BitChromosome;
import io.jenetics.BitGene;
import io.jenetics.Chromosome;
import io.jenetics.EnumGene;
import io.jenetics.Genotype;
import io.jenetics.PermutationChromosome;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class JeneticAlgorithm implements Algorithm {

    private TravelingThiefProblem problem;

    private double fitness(final Genotype gt) {
        Solution solution = getSolutionFromGenotype(gt);

        if (solution.profit == -Double.MAX_VALUE || solution.time == Double.MAX_VALUE)
            return Double.MIN_VALUE;

        return solution.profit -  solution.time;
    }

    @Override
    public List<Solution> solve(TravelingThiefProblem problem) {
        this.problem = problem;

        Genotype encoding = Genotype.of(
                PermutationChromosome.ofInteger(problem.numOfCities - 1),
                (Chromosome) BitChromosome.of(problem.numOfItems, 0.05)
        );

        final Engine engine = Engine
                .builder(this::fitness, encoding)
                .populationSize(250)
                .build();

        final List<EvolutionResult> genotypes = (List<EvolutionResult>) engine.stream()
                .limit(500)
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
        List<Boolean> booleanGenes = bc.stream().map(BitGene::booleanValue).collect(Collectors.toList());

        permutationGenes = permutationGenes.stream().map(integer -> integer + 1).collect(Collectors.toList());
        permutationGenes.add(0, 0);

        return problem.evaluate(permutationGenes, booleanGenes, true);
    }

}
