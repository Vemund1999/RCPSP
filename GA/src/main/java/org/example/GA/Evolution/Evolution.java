package org.example.GA.Evolution;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.GenerationScheme.GenerationScheme;
import org.example.GA.Individual.Individual;
import org.example.GA.Individual.Node.AttributeNode;
import org.example.GA.Individual.Node.FunctionNode;
import org.example.GA.Individual.Node.Node;
import org.example.Project.Project;
import org.example.GA.Utils.IndividualUtils;

import java.io.IOException;
import java.util.*;





@AllArgsConstructor
public class Evolution {


    // class for a child
    @Data
    @AllArgsConstructor
    private static class Child {
        int[] parentIndexes;
        int siblingIndex;
        Individual body;
    }






    // class to hold run statistics
    @Data
    @NoArgsConstructor
    public static class Run {

        public ArrayList<Float> avgFitnesses = new ArrayList<>();
        public ArrayList<Float> bestFitness = new ArrayList<>();
        public ArrayList<Float> worstFitness = new ArrayList<>();

        public ArrayList<Integer> bestFitnessIndex = new ArrayList<>();
        public ArrayList<Integer> worstFitnessIndex = new ArrayList<>();

        public Individual[] individualsFinalPop;

        public Map<String, ArrayList<Integer>> makeSpansOfSomeProjects = new HashMap<>();

        public boolean win = false;



    }



    public static Run run(int type, int q, float pInternalNode, int popSize, int runs, int maxDepth, int minDepth, String folderPathToProjectFIles, String survivorMethod, String projectPath, String pathToBoundsFile, String pathToSaveSchedule) throws IOException, InterruptedException {
        assert (type == 30 || type == 60 || type == 90 || type == 120): "wrong type";

        Run run = new Run();

        // loading project network
        Project project = Project.fromFile(folderPathToProjectFIles + projectPath, type, pathToBoundsFile);




        // initialize population, and evaluate their fitnesses
        Individual[] individuals = new Individual[popSize];
        for (int i = 0; i < popSize; i++) {
            Individual individual = IndividualUtils.generateTree(minDepth, maxDepth);
            float fitness = evaluateOnTrainingSet(individual, project, pathToSaveSchedule);
            individual.setFitness(fitness);
            individuals[i] = individual;
        }


        // for run statistics
        run.makeSpansOfSomeProjects.put( "GA best makespan", new ArrayList<>());



        // improve population
        for (int i = 0; i < runs; i++) {

            // select individuals to crossover ... [parent sets][parents]
            int[][] indexesToMate = parentSelection(individuals);

            // crossover of selected individuals
            Child[] children = crossover(indexesToMate, individuals, pInternalNode, minDepth);

            // mutate individuals
            children = mutate(children, maxDepth, minDepth, pInternalNode);

            // determine fitness
            for (int j=0;j<children.length;j++)
                children[j].getBody().setFitness( evaluateOnTrainingSet( children[j].getBody() , project, pathToSaveSchedule) );



            // survivor selection =================
            // robin hood
            if (Objects.equals(survivorMethod, "robinHood"))
                individuals = survivorSelection(individuals, children, q, popSize);

            // combining children and parents into a single Indibiduals array
            Individual[] tmpIndividuals = new Individual[individuals.length + children.length];
            for (int j = 0;j<individuals.length;j++)
                tmpIndividuals[j] = individuals[j];
            for (int j = 0;j<children.length;j++)
                tmpIndividuals[j + individuals.length] = children[j].getBody();
            individuals = tmpIndividuals;

            if (Objects.equals(survivorMethod, "roulette"))
                // roulette
                individuals = survivorSelectionWithRoulette(individuals, popSize);

            if (Objects.equals(survivorMethod, "pureElitism"))
                // pure fitness based selection
                individuals = pureElitismSurvivol(individuals, popSize);



            // print some info

            // printFitnesses(individuals);;
            float avgFitness = EvolutionUtils.getAvePopFitness(individuals);
            run.avgFitnesses.add( avgFitness );

            // beste fitness
            int best_i = EvolutionUtils.fittestIndex(individuals);
            run.bestFitness.add( individuals[best_i].getFitness() );
            run.bestFitnessIndex.add( best_i );

            // worst fitness
            int worst_i = EvolutionUtils.leastFittestIndex(individuals);
            run.worstFitness.add( individuals[worst_i].getFitness() );
            run.worstFitnessIndex.add( worst_i );

            run.makeSpansOfSomeProjects.get( "GA best makespan") .add((int) individuals[best_i].getFitness());

        }
        run.individualsFinalPop = individuals;

        if (run.bestFitness.get( run.getBestFitness().size() - 1 ) - project.getLB() <= 0)
            run.win = true;


        return run;
    }






    public static Individual[] pureElitismSurvivol(Individual[] individuals, int popSize) {
        Individual[] survivors = new Individual[popSize];

        // getting fitnesses
        double[] fitnesses = new double[individuals.length];
        for (int i = 0; i < individuals.length; i++)
            fitnesses[i] = individuals[i].getFitness();

        Integer[] indices = new Integer[individuals.length];
        for (int i = 0; i < individuals.length; i++)
            indices[i] = i;

        // Sort indices by fitness ascending. lower fitness is better
        Arrays.sort(indices, Comparator.comparingDouble(i -> fitnesses[i]));

        // filter
        for (int i = 0; i < popSize; i++)
            survivors[i] = individuals[indices[i]];

        return survivors;
    }


    private static Individual[] survivorSelection(Individual[] parents, Child[] children, int q, int popSize) {

        int[] wins = new int[parents.length + children.length];
        // count wins
        Individual[] allIndividuals = new Individual[parents.length + children.length];
        Random randGenerator = new Random();
        for (int i=1;i<=2;i++) {
            for (int j=0;j<parents.length;j++) {
                // indexes belonging to parents and children
                int[] parentIndexesToNotPick = children[j].getParentIndexes();
                int[] childrenIndexesToNotPick = {j, children[j].getSiblingIndex()};

                Individual individual;
                if (j == 0) // pick a parent
                    individual = parents[j];
                else // pick children
                    individual = children[j].body;
                int win = 0;
                int n=0;
                while (n < q) {
                    boolean loopAgain = false;
                    int iToCompare = randGenerator.nextInt(0, parents.length);
                    boolean pickParentToCompare = randGenerator.nextBoolean();
                    // checking if index shouldnt be picked
                    if (pickParentToCompare) {
                        for (int index : parentIndexesToNotPick)
                            if (index == iToCompare)
                                loopAgain = true;
                    }
                    else { // pick child
                        for (int index : childrenIndexesToNotPick)
                            if (index == iToCompare)
                                loopAgain = true;
                    }
                    // picked index that shouldnt be picked?
                    if (loopAgain)
                        continue;

                    Individual individualToCompare = parents[iToCompare];
                    if (individual.getFitness() > individualToCompare.getFitness())
                        win += 1;

                    n+=1;
                }
                allIndividuals[j*i] = individual;
                wins[j*i] = win;
            }
        }

        // filter out individuals based on wins
        Integer[] indices = new Integer[wins.length];
        for (int i = 0; i < wins.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (i, j) -> Integer.compare(wins[j], wins[i]));
        Individual[] filteredSortedIndividuals = new Individual[popSize];

        for (int i = 0; i < popSize; i++)
            filteredSortedIndividuals[i] = allIndividuals[indices[i]];



        return filteredSortedIndividuals;
    }







    private static Child[] mutate(Child[] children, int maxHeight, int minHeight, float pInternal) {
        if ( (maxHeight - minHeight) < 2)
            throw new RuntimeException("difference metweeb maxheight and minheight must be at least 2");

        for (int i=0;i<children.length;i++) {
            Individual mutation = IndividualUtils.generateTree(2, maxHeight-minHeight);

            Node cutoffNode = selectCutoffPoint(children[i].getBody(), pInternal, minHeight);
            children[i].setBody( attachSubTreeToTree(children[i].getBody(), cutoffNode, mutation.getRoot()) );
        }
        return children;
    }



    private static Node selectCutoffPoint(Individual individual, float pInternalNode, int minHeight) {

        Random randGenerator = new Random();

        Queue<Node> nodes = new LinkedList<>();
        nodes.add(individual.getRoot());
        int individualHeight = IndividualUtils.findHeight(individual.getRoot());
        int height = 0;
        while (!nodes.isEmpty()) {

            int levelSize = nodes.size();

            for (int i = 0; i < levelSize; i++) {
                Node current = nodes.poll();

                if ((height >= minHeight || height >= (individualHeight - (minHeight - 1))) && (current instanceof FunctionNode)) {
                    float randNum = randGenerator.nextFloat(0, 1);
                    if (randNum > pInternalNode)
                        return current; // return found cutoff
                }

                if (current instanceof AttributeNode)
                    continue;

                nodes.add( current.getLeft() );
                nodes.add( current.getRight() );
            }
            height+=1;
        }
        // no cuttoff was found. looking again
        return selectCutoffPoint(individual, pInternalNode, minHeight);
    }










    private static Individual attachSubTreeToTree(Individual individual, Node cutoffNode, Node newNode) {
        Node copyOfIndividualRoot = IndividualUtils.makeDeepCopy(individual.getRoot());
        Node copyOfCuttoffNode = IndividualUtils.makeDeepCopy(cutoffNode);
        Node copyOfNewNode = IndividualUtils.makeDeepCopy(newNode);


        // navigating tree using BFS
        Queue<Node> nodes = new LinkedList<>();
        nodes.add(copyOfIndividualRoot);
        while (!nodes.isEmpty()) {
            Node node = nodes.poll();
            if (node instanceof AttributeNode)
                continue;
            Node left = node.getLeft();
            Node right = node.getRight();

            if (left.getId() == copyOfCuttoffNode.getId()) {
                node.setLeft(copyOfNewNode);
                break;
            } else if (right.getId() == copyOfCuttoffNode.getId()) {
                node.setRight(copyOfNewNode);
                break;
            }


            nodes.add(left);
            nodes.add(right);
        }

        return new Individual(copyOfIndividualRoot);
    }





    private static Child[] crossover(int[][] indexesToMate, Individual[] parents, float pInternalNode, int minHeight) {

        Child[] children = new Child[indexesToMate.length];
        Node crossoverPoint_ParentOne = null;
        Node crossoverPoint_ParentTwo = null;
        int childIndex = 0;
        for (int i=0;i< indexesToMate.length / 2;i++) {
            // getting parents
            int parentOneIndex = indexesToMate[i][0];
            int parenTwoIndex = indexesToMate[i][1];
            Individual parentOne = parents[ parentOneIndex ];
            Individual parentTwo = parents[ parenTwoIndex ];
            // selecting crossover point in each parent
            crossoverPoint_ParentOne = selectCutoffPoint(parentOne, pInternalNode, minHeight);
            crossoverPoint_ParentTwo = selectCutoffPoint(parentTwo, pInternalNode, minHeight);
            // exchanging subtrees
            Individual childOne = attachSubTreeToTree(parentOne, crossoverPoint_ParentOne, crossoverPoint_ParentTwo);
            Individual childTwo = attachSubTreeToTree(parentTwo, crossoverPoint_ParentTwo, crossoverPoint_ParentOne);

            // initilizing children
            int[] parentIndexes = new int[2];
            parentIndexes[0] = parentOneIndex;
            parentIndexes[1] = parenTwoIndex;

            children[childIndex] = new Child(parentIndexes, childIndex+1, childOne);
            children[childIndex+1] = new Child(parentIndexes, childIndex, childTwo);
            childIndex+=2;
        }
        return children;


    }






    private static LinkedHashMap<Integer, Double> exponentialRanking(Individual[] individuals) throws InterruptedException {
        int populationSize = individuals.length;

        // getting fitnesses
        double[] fitnesses = new double[individuals.length];
        for (int i = 0; i < populationSize; i++)
            fitnesses[i] = individuals[i].getFitness();

        // Rank individuals by fitness, storing indices for access later.
        Integer[] indices = new Integer[populationSize];
        for (int i = 0; i < populationSize; i++) {
            indices[i] = i;
        }

        // Sort indices by fitness ascending. lower fitness is better
        Arrays.sort(indices, Comparator.comparingDouble(i -> fitnesses[i]));

        // Define parameters for exponential ranking
        double alpha = 0.1;  // Probability decay rate, where alpha < 1.0 means higher ranked are more likely

        // Calculate selection probabilities based on exponential ranking
        double[] probabilities = new double[populationSize];
        double normalizationSum = 0;

        for (int i = 0; i < populationSize; i++) {
            int index = indices[i];
            probabilities[index] = Math.pow(alpha, i);  // Assign probability proportional to alpha^rank
            normalizationSum += probabilities[index];   // Sum for normalization
        }

        // Normalize probabilities so they sum to 1
        for (int i = 0; i < populationSize; i++) {
            int index = indices[i];
            probabilities[index] /= normalizationSum;
        }

        // mapping probabilities to individuals
        LinkedHashMap<Integer, Double> probabilitiesIndividuals = new LinkedHashMap<>();
        for (int i = 0; i < populationSize; i++) {
            int index = indices[i];
            probabilitiesIndividuals.put(index, probabilities[index]);
        }


        return probabilitiesIndividuals;
    }




    public static Individual[] survivorSelectionWithRoulette(Individual[] individuals, int popSize) throws InterruptedException {
        LinkedHashMap<Integer, Double> probabilities = exponentialRanking(individuals);
        Individual[] survivors = new Individual[individuals.length];

        int[] pickedIndexes = new int[popSize];

        for (int i=0;i<popSize;i++) {
            boolean alreadyPicked = false;

            while (true) {
                // choose a survivor
                int survivorIndex = rouletteSelectionOfIndividual(probabilities);
                survivors[i] = individuals[survivorIndex];

                // check if indeax already picked
                for (int pickedIndex : pickedIndexes)
                    if (pickedIndex == survivorIndex)
                        alreadyPicked = true;
                if (alreadyPicked)
                    continue;

                // since not picked
                pickedIndexes[i] = survivorIndex;
                survivors[i] = individuals[survivorIndex];
                break;
            }
        }

        return survivors;
    }


    // roulette wheel
    private static int[][] parentSelection(Individual[] individuals) throws InterruptedException {
        int[][] parentsToMate = new int[individuals.length][2];

        LinkedHashMap<Integer, Double> probabilities = exponentialRanking(individuals);
        for (int i=0;i<individuals.length;i++) {
            int parentOne = rouletteSelectionOfIndividual(probabilities);
            int parentTwo = parentOne;
            while (parentOne == parentTwo)
                parentTwo = rouletteSelectionOfIndividual(probabilities);
            parentsToMate[i][0] = parentOne;
            parentsToMate[i][1] = parentTwo;
        }
        return parentsToMate;
    }



    private static int rouletteSelectionOfIndividual(LinkedHashMap<Integer, Double> individualsProbabilities) {
        Random random = new Random();
        double rand = random.nextDouble();
        double cumulativeProbability = 0.0;

        for (Map.Entry<Integer, Double> entry : individualsProbabilities.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (rand <= cumulativeProbability)
                return entry.getKey();
        }
        throw new NullPointerException("didnt find a value for roulette");

    }





    private static int evaluateOnTrainingSet(Individual individual, Project project, String pathToSaveSchedule) {
        // run individual on all projects in set, and get the makespan
        Float[] priorityList = individual.applyToProject(project);

        // return makespan of project using priority list
        return  GenerationScheme.applySerialGenerationScheme(project, priorityList, pathToSaveSchedule);
    }








}
