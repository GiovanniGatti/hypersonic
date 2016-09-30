package player.contest;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;

@DisplayName("A contest")
class ContestTest implements WithAssertions {
    /*
     * private ExecutorService gameExecutorService;
     * private ExecutorService matchExecutorService;
     * 
     * @BeforeEach
     * void init() {
     * gameExecutorService = Executors.newFixedThreadPool(2);
     * matchExecutorService = Executors.newFixedThreadPool(3);
     * }
     * 
     * @Test
     * 
     * @DisplayName("returns the classification of a battle between multiple ais on multiple game engines")
     * void returnsClassificationBetweenMultipleAIsOnMultipleGameEngines() throws Exception {
     * Function<IntSupplier, Supplier<AI>> firstAI =
     * (t) -> () -> MockedAI.anyConf(ImmutableMap.of("id", "first"));
     * 
     * Function<IntSupplier, Supplier<AI>> secondAI =
     * (t) -> () -> MockedAI.anyConf(ImmutableMap.of("id", "second"));
     * 
     * Function<IntSupplier, Supplier<AI>> thirdAI =
     * (t) -> () -> MockedAI.anyConf(ImmutableMap.of("id", "third"));
     * 
     * List<Function<IntSupplier, Supplier<AI>>> ais = Arrays.asList(firstAI, secondAI, thirdAI);
     * 
     * List<Supplier<GameEngine>> gameEngines = Arrays.asList(
     * () -> MockedGE.anyWithWinner(Winner.PLAYER),
     * () -> MockedGE.anyWithWinner(Winner.OPPONENT),
     * () -> MockedGE.anyWithWinner(Winner.PLAYER));
     * 
     * Contest contest = new Contest(
     * ais,
     * gameEngines,
     * gameExecutorService,
     * matchExecutorService);
     * 
     * ContestResult contestResult = contest.call();
     * 
     * List<Score> classifications = contestResult.getClassification();
     * 
     * assertThat(classifications).hasSize(3);
     * 
     * Score first = classifications.get(0);
     * Score second = classifications.get(1);
     * Score third = classifications.get(2);
     * 
     * assertThat(first.getAi().getConf()).containsEntry("id", "first");
     * assertThat(first.getVictoryCount()).isEqualTo(4);
     * 
     * assertThat(second.getAi().getConf()).containsEntry("id", "second");
     * assertThat(second.getVictoryCount()).isEqualTo(3);
     * 
     * assertThat(third.getAi().getConf()).containsEntry("id", "third");
     * assertThat(third.getVictoryCount()).isEqualTo(2);
     * }
     * 
     * @Test
     * 
     * @DisplayName("cannot run with a one single AI")
     * void throwISEWhenSingleAIIsProvided() {
     * Function<IntSupplier, Supplier<AI>> singleAI =
     * (t) -> () -> MockedAI.anyConf(ImmutableMap.of("id", "first"));
     * 
     * List<Function<IntSupplier, Supplier<AI>>> ais = Collections.singletonList(singleAI);
     * 
     * List<Supplier<GameEngine>> gameEngine = Collections.singletonList(() -> MockedGE.anyWithWinner(Winner.PLAYER));
     * 
     * Contest contest = new Contest(
     * ais,
     * gameEngine,
     * gameExecutorService,
     * matchExecutorService);
     * 
     * assertThatExceptionOfType(IllegalStateException.class)
     * .isThrownBy(contest::call)
     * .withMessageContaining("Unable to play a contest with a single provided AI");
     * }
     * 
     * @Nested
     * 
     * @DisplayName("that finished, returns a result with")
     * class Statisticts {
     * 
     * @Test
     * 
     * @DisplayName("the right average evaluate")
     * void averageScore() throws ExecutionException, InterruptedException {
     * List<Function<IntSupplier, Supplier<AI>>> ais = Arrays.asList(
     * (t) -> ContestTest::anyPlayerAI,
     * (t) -> MockedAI::any);
     * 
     * List<Supplier<GameEngine>> gameEngines = Arrays.asList(
     * () -> MockedGE.anyWithPlayerScore(10),
     * () -> MockedGE.anyWithPlayerScore(20));
     * 
     * Contest contest = new Contest(
     * ais,
     * gameEngines,
     * gameExecutorService,
     * matchExecutorService);
     * 
     * ContestResult result = contest.call();
     * 
     * Optional<Score> maybeScore =
     * result.getClassification()
     * .stream()
     * .filter(t -> ContestTest.isPlayerAI(t.getAi()))
     * .findFirst();
     * 
     * assertThat(maybeScore).isPresent();
     * 
     * Score score = maybeScore.get();
     * 
     * assertThat(score.getAverageScore()).isEqualTo(15.0);
     * }
     * 
     * @Test
     * 
     * @DisplayName("the right average number of rounds")
     * void averageNumberOfRounds() throws ExecutionException, InterruptedException {
     * List<Function<IntSupplier, Supplier<AI>>> ais = Arrays.asList(
     * (t) -> ContestTest::anyPlayerAI,
     * (t) -> MockedAI::any);
     * 
     * List<Supplier<GameEngine>> gameEngines = Arrays.asList(
     * () -> MockedGE.anyWithNumberOfRounds(10),
     * () -> MockedGE.anyWithNumberOfRounds(20));
     * 
     * Contest contest = new Contest(
     * ais,
     * gameEngines,
     * gameExecutorService,
     * matchExecutorService);
     * 
     * ContestResult result = contest.call();
     * 
     * Optional<Score> maybeScore =
     * result.getClassification()
     * .stream()
     * .filter(t -> ContestTest.isPlayerAI(t.getAi()))
     * .findFirst();
     * 
     * assertThat(maybeScore).isPresent();
     * 
     * Score score = maybeScore.get();
     * 
     * assertThat(score.getAverageNumberOfRounds()).isEqualTo(15.0);
     * }
     * 
     * @Test
     * 
     * @DisplayName("the right average win rate")
     * void averageWinRate() throws ExecutionException, InterruptedException {
     * List<Function<IntSupplier, Supplier<AI>>> ais = Arrays.asList(
     * (t) -> ContestTest::anyPlayerAI,
     * (t) -> MockedAI::any);
     * 
     * List<Supplier<GameEngine>> gameEngines = Arrays.asList(
     * () -> MockedGE.anyWithWinner(Winner.PLAYER),
     * () -> MockedGE.anyWithWinner(Winner.PLAYER),
     * () -> MockedGE.anyWithWinner(Winner.OPPONENT));
     * 
     * Contest contest = new Contest(
     * ais,
     * gameEngines,
     * gameExecutorService,
     * matchExecutorService);
     * 
     * ContestResult result = contest.call();
     * 
     * Optional<Score> maybeScore =
     * result.getClassification()
     * .stream()
     * .filter(t -> ContestTest.isPlayerAI(t.getAi()))
     * .findFirst();
     * 
     * assertThat(maybeScore).isPresent();
     * 
     * Score score = maybeScore.get();
     * 
     * assertThat(score.getAverageWinRate()).isBetween(2.0 / 3 - 0.001, 2.0 / 3 + 0.001);
     * }
     * }
     * 
     * private static AI anyPlayerAI() {
     * return MockedAI.anyConf(ImmutableMap.of("id", "player"));
     * }
     * 
     * private static boolean isPlayerAI(AI ai) {
     * Map<String, Object> conf = ai.getConf();
     * return "player".equals(conf.get("id"));
     * }
     */
}