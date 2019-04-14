package algorithms;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.ext.moea.MOEA;
import io.jenetics.ext.moea.UFTournamentSelector;
import io.jenetics.ext.moea.Vec;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import model.NonDominatedSet;
import model.Solution;
import model.TravelingThiefProblem;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class JeneticAlgorithm implements Algorithm {

    private TravelingThiefProblem problem;

    private Vec<double[]> fitness(final Genotype gt) {
        Solution solution = getSolutionFromGenotype(gt);

        return Vec.of(solution.profit, -solution.time);
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
                .optimize(Optimize.MAXIMUM)
                .maximalPhenotypeAge(5)
                .alterers(new Mutator(0.01), new UniformCrossover(0.03))
                .populationSize(700)
                .offspringSelector(new TournamentSelector<>(4))
                .survivorsSelector(UFTournamentSelector.ofVec())
                .build();

        final ISeq<Phenotype> genotypes = (ISeq<Phenotype>) engine.stream()
                .limit(300)
                .collect(MOEA.toParetoSet(IntRange.of(200)));

        List<Solution> solutions = genotypes.stream()
                .map(Phenotype::getGenotype)
                .map(this::getSolutionFromGenotype)
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
