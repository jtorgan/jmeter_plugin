package perf_statistic.server.ref_data;

import com.intellij.util.containers.SortedList;
import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.parameters.ParametersProvider;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.statistics.build.BuildDataStorage;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import perf_statistic.common.*;

import java.math.BigDecimal;
import java.util.*;

public class BuildHistoryRefCheckAdapter extends BuildServerAdapter {
	private static final String CHECK_REFERENCE_ACTIVITY_NAME_BUILD_HISTORY = "Check reference values calculated from BUILD HISTORY";

	protected final BuildDataStorage myStorage;

	public BuildHistoryRefCheckAdapter(@NotNull final BuildDataStorage storage, @NotNull final EventDispatcher<BuildServerListener> dispatcher) {
		myStorage = storage;
		dispatcher.addListener(this);
	}

	@Override
	public void beforeBuildFinish(@NotNull final SRunningBuild runningBuild) {
		ParametersProvider parametersProvider = runningBuild.getParametersProvider();
		if ("true".equals(parametersProvider.get(PluginConstants.PARAMS_REF_CHECK)) && "true".equals(parametersProvider.get(PluginConstants.PARAMS_REF_TYPE_BUILD_HISTORY))) {
			runningBuild.addBuildMessage(DefaultMessagesInfo.createBlockStart(CHECK_REFERENCE_ACTIVITY_NAME_BUILD_HISTORY, DefaultMessagesInfo.BLOCK_TYPE_MODULE));
			boolean[] referenceMetrics = new boolean[4];
			referenceMetrics[2] = Boolean.parseBoolean(parametersProvider.get(PluginConstants.PARAMS_REF_METRIC_MAX));
			referenceMetrics[0] = Boolean.parseBoolean(parametersProvider.get(PluginConstants.PARAMS_REF_METRIC_AVG));
			referenceMetrics[1] = Boolean.parseBoolean(parametersProvider.get(PluginConstants.PARAMS_REF_METRIC_LINE90));
            referenceMetrics[3] = Boolean.parseBoolean(parametersProvider.get(PluginConstants.PARAMS_REF_METRIC_MEDIAN));

			Double criticalVariation = parametersProvider.get(PluginConstants.PARAMS_VARIATION_CRITICAL) == null ? 0.15 : Double.parseDouble(parametersProvider.get(PluginConstants.PARAMS_VARIATION_CRITICAL));
			Double variation = parametersProvider.get(PluginConstants.PARAMS_VARIATION_WARN) == null ? Double.NEGATIVE_INFINITY : Double.parseDouble(parametersProvider.get(PluginConstants.PARAMS_VARIATION_WARN));

			Map<String, PerformanceMessage> currentValues = getServiceMessagesToCompareReferenceValues(runningBuild, referenceMetrics);

//		    calculate ref values form history build
			Map<String, List<BigDecimal>> historyValues = new HashMap<String, List<BigDecimal>>();
			SBuildType buildType = runningBuild.getBuildType();
			if (buildType != null) {
				for (String key : currentValues.keySet()) {
					if (currentValues.get(key) == null)
						continue;
//					init array
					List<BigDecimal> values = historyValues.get(key);
					if (values == null) {
						values = new SortedList<BigDecimal>(new Comparator<BigDecimal>() {
							@Override
							public int compare(BigDecimal o1, BigDecimal o2) {
								if (o1 == null) return -1;
								return o1.compareTo(o2);
							}
						});
					}
//					extract all values from history
					int i = 0;

					List<SFinishedBuild> historyBuilds = buildType.getHistory();
					int considerHistoryCount = Integer.parseInt(parametersProvider.get(PluginConstants.PARAMS_REF_BUILD_COUNT)); //todo: limit count of build
					int actualHistoryCount = historyBuilds.size();

					while (i < actualHistoryCount && i < considerHistoryCount) {
						BigDecimal val = myStorage.getValue(key, historyBuilds.get(i).getBuildId());
						if (val != null) {
							values.add(val);
						}
						i++;
					}
					historyValues.put(key, values);
				}

			}
			if (!historyValues.isEmpty()) {
				for (String key : currentValues.keySet()) {
					if (currentValues.get(key) == null)
						continue;
					PerformanceMessage message = currentValues.get(key);
					List<BigDecimal> values = historyValues.get(key);

					if (values.isEmpty())
						continue;

					int count = 0;
					double sum = 0;
					double v25 = values.get((int) Math.round(values.size() * 0.25)).doubleValue();
					double v75 = values.get((int) Math.round(values.size() * 0.75) - 1).doubleValue();

					for (BigDecimal value : values) {
						if (value.doubleValue() <= v75 + 1.5 * (v75 - v25) && value.doubleValue() >= v25 - 1.5 * (v75 - v25)) {
							count++;
							sum += value.doubleValue();
						}
					}

					long average = Math.round(sum / count);
					long actual = Long.parseLong(message.getValue());

					boolean exceedVariation = variation != Double.NEGATIVE_INFINITY && actual > average * (1 + variation);
					boolean exceedCriticalVariation = actual > average * (1 + criticalVariation);

					String refMsg = PerformanceMessageParser.createJMeterMessage(message.getTestsGroupName(), message.getTestName(),
							PerformanceStatisticMetrics.getMetricByKey(message.getMetric()).getReferenceKey(), average, null, exceedCriticalVariation || exceedVariation);
					runningBuild.addBuildMessage(DefaultMessagesInfo.createTextMessage(refMsg));


					if (exceedCriticalVariation) {
						String fullName = message.getTestsGroupName().isEmpty() ? message.getTestName() : message.getTestsGroupName() + ": " + message.getTestName();
						String description = "Metric - " + message.getMetric() + "; test - " + fullName
								+ "; \nreference value: " + Math.round(average)
								+ "; current value: " + Math.round(actual)
								+ "; variation: " + criticalVariation;
						BuildProblemData buildProblem = BuildProblemData.createBuildProblem(StringUtils.getBuildProblemId(message.getMetric(), fullName),
								PluginConstants.CRITICAL_PERFORMANCE_PROBLEM_TYPE, description, fullName);
						runningBuild.addBuildProblem(buildProblem);
					}
				}
			}
			runningBuild.addBuildMessage(DefaultMessagesInfo.createBlockEnd(CHECK_REFERENCE_ACTIVITY_NAME_BUILD_HISTORY, DefaultMessagesInfo.BLOCK_TYPE_MODULE));
		}
	}


	private String getAlias(PerformanceMessage message) {
		String testsGroup = message.getTestsGroupName();
		String testName = message.getTestName();
		String code = message.getCode();

		String alias = StringUtils.replaceNonWordSymbols(testsGroup.isEmpty() ? testName : testsGroup + testName);
		if (code != null) {
			alias += StringUtils.replaceNonWordSymbols(code);
		}
		return alias;
	}

	private Map<String, PerformanceMessage> getServiceMessagesToCompareReferenceValues(@NotNull SRunningBuild build, boolean[] referenceMetrics) {
		Map<String, PerformanceMessage> currentValues = new HashMap<String, PerformanceMessage>();
		String buildTypeId = build.getBuildTypeExternalId();
		for (Iterator<LogMessage> iterator = build.getBuildLog().getMessagesIterator(); iterator.hasNext();) {
			PerformanceMessage message = PerformanceMessageParser.getPerformanceTestingMessage(iterator.next().getText().trim());
			if (message != null) {
				PerformanceStatisticMetrics metric = PerformanceStatisticMetrics.getMetricByKey(message.getMetric());
				 if (metric != null && metric == PerformanceStatisticMetrics.AVERAGE && referenceMetrics[0]
						 || metric == PerformanceStatisticMetrics.LINE90 && referenceMetrics[1]
                         || metric == PerformanceStatisticMetrics.MEDIAN && referenceMetrics[3]
						 || metric == PerformanceStatisticMetrics.MAX && referenceMetrics[2]){
					 currentValues.put(buildTypeId + '_' + message.getMetric() + '_' + getAlias(message), message);
				 } else if (metric == null) {
					metric = PerformanceStatisticMetrics.getMetricByReferenceKey(message.getMetric());
					if (metric != null) {
						if (metric == PerformanceStatisticMetrics.AVERAGE && referenceMetrics[0]
								|| metric == PerformanceStatisticMetrics.LINE90 && referenceMetrics[1]
                                || metric == PerformanceStatisticMetrics.MEDIAN && referenceMetrics[3]
								|| metric == PerformanceStatisticMetrics.MAX && referenceMetrics[2]){
							currentValues.put(buildTypeId + '_' + metric.getKey() + '_' + getAlias(message), null);
						}
					}
				}
			}
		}

		return currentValues;
	}

	private final String REFERENCE_DATA_STORAGE = "perf.test.reference.values";

	private void updateRefDataStorage(SBuildType buildType, String key, String value) {
//		RefCheck_Test_Max_Openrandomadminpageegprojectsorsettings
//		RefCheck_Test_90Line_Viewreleasenotespage

		CustomDataStorage storage = buildType.getCustomDataStorage(REFERENCE_DATA_STORAGE);
		storage.putValue(key, value);
	}

/*	public static void main(String[] args) {
		for (int i =0; i < 10 ; i++)  {
			System.out.println(i + " * 0.25 = " +  Math.round(i * 0.25));
			System.out.println(i + " * 0.75 = " +  Math.round(i * 0.75));
		}
	}*/
}
