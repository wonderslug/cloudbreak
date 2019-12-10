package com.sequenceiq.it.cloudbreak.performance;

import java.util.List;
import java.util.function.Consumer;

import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.sequenceiq.it.cloudbreak.context.MeasuredTestContext;

public class Util {

    public static final String KEY_PERFORMANCE_INDICATOR = "keyPerformanceIndicator";

    private Util() {
    }

    public static KeyPerformanceIndicator getKeyPerformance(Measure allMeasurement) {
        return BasicStatistic.build(allMeasurement);
    }

    public static KeyPerformanceIndicator getKeyPerformance(ITestContext iTestContext) {
        return getKeyPerformance(collectMeasurements(iTestContext));
    }

    public static Measure collectMeasurements(ITestContext iTestContext) {
        if (iTestContext == null) {
            throw new IllegalArgumentException("No testng testcontext is given.");
        }
        IResultMap failed = iTestContext.getFailedTests();
        IResultMap success = iTestContext.getPassedTests();
        MeasureAll allMeasurement = new MeasureAll();
        failed.getAllResults().stream().forEach(
                getiTestResultConsumer(allMeasurement)
        );
        success.getAllResults().stream().forEach(
                getiTestResultConsumer(allMeasurement)
        );

        return allMeasurement;
    }

    private static Consumer<ITestResult> getiTestResultConsumer(MeasureAll allMeasurement) {
        return result -> {
            Object[] param = result.getParameters();
            if (param.length > 0) {
                if (param[0] instanceof MeasuredTestContext) {
                    List<PerformanceIndicator> all = ((MeasuredTestContext) param[0]).getMeasure().getAll();
                    String testFullName = result.getTestClass().getClass().getCanonicalName() + "." + result.getTestName();
                    all.stream().forEach(pi -> pi.setTestName(testFullName));
                    allMeasurement.addAll(all);
                }
            }
        };
    }
}
