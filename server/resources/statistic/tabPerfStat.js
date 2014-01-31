function hideChart(testChartContainer, testTitleContainer) {
  $j(testChartContainer).css("display", "none");
  $j(testTitleContainer).attr("class", "test-collapse-unselected");
}

function showChart(testChartContainer, testTitleContainer) {
  $j(testChartContainer).css("display", "table-row");
  $j(testTitleContainer).attr("class", "test-collapse-selected");
}

function filterByName() {
  $j("#testGroupFilter").val("none");
  var name = $j("#testNameFilter").val();
  if (name == "none") {
    showAllItems();
  } else {
    $j("#perfTestFailed").find("tbody.testRowData").each(applyFilterToItem(this, name));
    $j("#perfTestSuccess").find("tbody.testRowData").each(applyFilterToItem(this, name));
  }
}

function testGroupFilter() {
  $j("#testNameFilter").val("none");
  var threads = $j("#testGroupFilter").val();
  if (threads == "none") {
    showAllItems();
  } else {
    $j("#perfTestFailed").find("tbody.testRowData").each(applyFilterToItem(this, threads));
    $j("#perfTestSuccess").find("tbody.testRowData").each(applyFilterToItem(this, threads));
  }
}

function applyFilterToItem(el, value) {
  var currValue = $j(el).find("input[name=threads]").val();
  if (currValue != value) {
    $j(el).css("display", "none");
  } else {
    $j(el).css("display", "table-row-group");
  }
}

function showAllItems() {
  $j("#perfTestFailed").find("tbody.testRowData").css("display", "table-row-group");
  $j("#perfTestSuccess").find("tbody.testRowData").css("display", "table-row-group");
}