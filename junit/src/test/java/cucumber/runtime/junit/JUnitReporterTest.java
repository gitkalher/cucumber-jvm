package cucumber.runtime.junit;

import cucumber.api.PendingException;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JUnitReporterTest {

    private JUnitReporter jUnitReporter;
    private RunNotifier runNotifier;

    @Test
    public void resultWithError() {
        createNonStrictReporter();
        Result result = mock(Result.class);
        Throwable exception = mock(Throwable.class);
        when(result.getError()).thenReturn(exception);

        Description description = mock(Description.class);
        createRunNotifier(description);

        jUnitReporter.result(result);

        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);
        verify(runNotifier).fireTestFailure(failureArgumentCaptor.capture());

        Failure failure = failureArgumentCaptor.getValue();
        assertEquals(description, failure.getDescription());
        assertEquals(exception, failure.getException());
    }

    @Test
    public void result_with_undefined_step_non_strict() {
        createNonStrictReporter();
        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(Result.UNDEFINED);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier).fireTestIgnored();
    }

    @Test
    public void result_with_undefined_step_strict() {
        createStrictReporter();
        createDefaultRunNotifier();
        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;
        EachTestNotifier executionUnitNotifier = mock(EachTestNotifier.class);
        jUnitReporter.executionUnitNotifier = executionUnitNotifier;

        jUnitReporter.result(Result.UNDEFINED);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verifyAddFailureWithPendingException(stepNotifier);
        verifyAddFailureWithPendingException(executionUnitNotifier);
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    private void verifyAddFailureWithPendingException(EachTestNotifier stepNotifier) {
        ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);
        verify(stepNotifier).addFailure(captor.capture());
        Throwable error = captor.getValue();
        assertTrue(error instanceof PendingException);
    }

    @Test
    public void result_with_pending_step_non_strict() {
        createNonStrictReporter();
        Result result = mock(Result.class);
        when(result.getError()).thenReturn(new PendingException());

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier).fireTestIgnored();
    }

    @Test
    public void result_with_pending_step_strict() {
        createStrictReporter();
        createDefaultRunNotifier();
        Result result = mock(Result.class);
        when(result.getError()).thenReturn(new PendingException());

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;
        EachTestNotifier executionUnitNotifier = mock(EachTestNotifier.class);
        jUnitReporter.executionUnitNotifier = executionUnitNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier, times(0)).fireTestStarted();
        verify(stepNotifier, times(0)).fireTestFinished();
        verifyAddFailureWithPendingException(stepNotifier);
        verifyAddFailureWithPendingException(executionUnitNotifier);
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void result_without_error_non_strict() {
        createNonStrictReporter();
        Result result = mock(Result.class);

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier).fireTestStarted();
        verify(stepNotifier).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void result_without_error_strict() {
        createStrictReporter();
        Result result = mock(Result.class);

        EachTestNotifier stepNotifier = mock(EachTestNotifier.class);
        jUnitReporter.stepNotifier = stepNotifier;

        jUnitReporter.result(result);

        verify(stepNotifier).fireTestStarted();
        verify(stepNotifier).fireTestFinished();
        verify(stepNotifier, times(0)).addFailure(Matchers.<Throwable>any(Throwable.class));
        verify(stepNotifier, times(0)).fireTestIgnored();
    }

    @Test
    public void forward_calls_to_formatter_interface_methods() throws Exception {
        String uri = "uri";
        Feature feature = mock(Feature.class);
        Background background = mock(Background.class);
        ScenarioOutline scenarioOutline = mock(ScenarioOutline.class);
        Examples examples = mock(Examples.class);
        Scenario scenario = mock(Scenario.class);
        Step step = mock(Step.class);
        Formatter formatter = mock(Formatter.class);
        jUnitReporter = new JUnitReporter(mock(Reporter.class), formatter, false);

        jUnitReporter.uri(uri);
        jUnitReporter.feature(feature);
        jUnitReporter.scenarioOutline(scenarioOutline);
        jUnitReporter.examples(examples);
        jUnitReporter.startOfScenarioLifeCycle(scenario);
        jUnitReporter.background(background);
        jUnitReporter.scenario(scenario);
        jUnitReporter.step(step);
        jUnitReporter.endOfScenarioLifeCycle(scenario);
        jUnitReporter.eof();
        jUnitReporter.done();
        jUnitReporter.close();

        verify(formatter).uri(uri);
        verify(formatter).feature(feature);
        verify(formatter).scenarioOutline(scenarioOutline);
        verify(formatter).examples(examples);
        verify(formatter).startOfScenarioLifeCycle(scenario);;
        verify(formatter).background(background);
        verify(formatter).scenario(scenario);
        verify(formatter).step(step);
        verify(formatter).endOfScenarioLifeCycle(scenario);
        verify(formatter).eof();
        verify(formatter).done();
        verify(formatter).close();
    }

    @Test
    public void forward_calls_to_reporter_interface_methods() throws Exception {
        Match match = mock(Match.class);
        Result result = mockResult();
        ExecutionUnitRunner executionUnitRunner = mockExecutionUnitRunner();
        String mimeType = "mimeType";
        byte data[] = new byte[] {1};
        String text = "text";
        Reporter reporter = mock(Reporter.class);
        jUnitReporter = new JUnitReporter(reporter, mock(Formatter.class), false);

        jUnitReporter.startExecutionUnit(executionUnitRunner, mock(RunNotifier.class));
        jUnitReporter.before(match, result);
        jUnitReporter.step(mock(Step.class));
        jUnitReporter.match(match);
        jUnitReporter.embedding(mimeType, data);
        jUnitReporter.write(text);
        jUnitReporter.result(result);
        jUnitReporter.after(match, result);

        verify(reporter).before(match, result);
        verify(reporter).match(match);
        verify(reporter).embedding(mimeType, data);
        verify(reporter).write(text);
        verify(reporter).result(result);
        verify(reporter).after(match, result);
    }

    private Result mockResult() {
        Result result = mock(Result.class);
        when(result.getStatus()).thenReturn("passed");
        return result;
    }

    private ExecutionUnitRunner mockExecutionUnitRunner() {
        ExecutionUnitRunner executionUnitRunner = mock(ExecutionUnitRunner.class);
        when(executionUnitRunner.getDescription()).thenReturn(mock(Description.class));
        return executionUnitRunner;
    }

    private void createDefaultRunNotifier() {
        createRunNotifier(mock(Description.class));
    }

    private void createRunNotifier(Description description) {
        runNotifier = mock(RunNotifier.class);
        ExecutionUnitRunner executionUnitRunner = mock(ExecutionUnitRunner.class);
        when(executionUnitRunner.getDescription()).thenReturn(description);
        jUnitReporter.startExecutionUnit(executionUnitRunner, runNotifier);
    }

    private void createStrictReporter() {
        createReporter(true);
    }

    private void createNonStrictReporter() {
        createReporter(false);
    }

    private void createReporter(boolean strict) {
        Formatter formatter = mock(Formatter.class);
        Reporter reporter = mock(Reporter.class);

        jUnitReporter = new JUnitReporter(reporter, formatter, strict);
    }

}
