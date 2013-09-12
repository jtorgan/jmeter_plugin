// Charts are implemented using Flot library (http://code.google.com/p/flot/), distributed under MIT license.
//
// Useful links:
// Flot API:        http://people.iola.dk/olau/flot/API.txt
// Flot examples:   http://people.iola.dk/olau/flot/examples/

if (!window.$j) {
  window.$j = window.jQuery;
}

BS.Perfmon = {
  COLORS: {
    cpu: "#CB4B4B",
    disk: "#EDC240",
    memory: "#AFD8F8"
  },

  init: function(chartElem, legendElem, chartData) {
    chartElem = $j(chartElem);
    legendElem = $j(legendElem);

    this.initPlot(chartElem, chartData);
    this.initLegend(legendElem);
    this.initTooltip(chartElem);
    this.initCrosshair(chartElem, legendElem);
  },

  initPlot: function(chartElem, chartData) {
    var cpuData = [],
            diskData = [],
            memoryData = [];
    for (var i = 0; i < chartData.labels.length; i++) {
      var time = chartData.labels[i];
      cpuData.push([time, chartData.cpu[i]]);
      diskData.push([time, chartData.disk[i]]);
      memoryData.push([time, chartData.memory[i]]);
    }

    // Note: using "label_" instead of "label" causes the plot not to show the legend.
    // But the label value is still available on hover.
    this.chartData = [
      { data: cpuData, label_: "CPU", color: this.COLORS.cpu },
      { data: diskData, label_: "Disk", color: this.COLORS.disk },
      { data: memoryData, label_: "Memory", color: this.COLORS.memory }
    ];

    chartElem.css({
      width: 650,
      height: 400
    });

    this.plot = $j.plot(chartElem, this.chartData, {
      series: {
        lines: { show: true },
        points: { show: true, radius: 2 }
      },
      crosshair: {
        mode: "x"
      },
      grid: {
        hoverable: true,
        clickable: true
      },
      xaxis: {
        mode: 'time',
        tickFormatter: function (val) {
          return BS.Perfmon.Util.formatTime(val);
        }
      },
      yaxis: {
        min: 0,
        max: 100,
        tickFormatter: function(val) {
          return val + "%";
        }
      },
      selection: {
        mode: "x"
      }
    });
  },

  initTooltip: function(chartElem) {
    function showTooltip(x, y, contents) {
      $j('<div id="tooltip">' + contents + '</div>').css({
        position: 'absolute',
        display: 'none',
        top: y + 6,
        left: x + 12,
        border: '1px solid #fdd',
        padding: '2px',
        'background-color': '#fee',
        opacity: 0.80
      }).appendTo("body").fadeIn(200);
    }

    var previousPoint = null;

    chartElem.bind("plothover", function(event, pos, item) {
      if (item) {
        if (previousPoint != item.dataIndex) {
          previousPoint = item.dataIndex;

          $j("#tooltip").remove();
          var time = BS.Perfmon.Util.formatTime(item.datapoint[0]),
                  value = item.datapoint[1].toFixed(2);

          showTooltip(item.pageX, item.pageY, item.series.label_ + " at " + time + " is <b>" + value + "%</b>");
        }
      } else {
        $j("#tooltip").remove();
        previousPoint = null;
      }
    });
  },

  initCrosshair: function(chartElem, legendElem) {
    var legends = {};
    legendElem.find("label").each(function() {
      var className = $j(this).parent().attr("class");
      legends[className] = $j(this);
    });

    var that = this;
    var updateLegendTimeout = null;
    var latestPosition = null;

    function updateLegend() {
      updateLegendTimeout = null;

      var axes = that.plot.getAxes();
      if (latestPosition.x < axes.xaxis.min || latestPosition.x > axes.xaxis.max ||
              latestPosition.y < axes.yaxis.min || latestPosition.y > axes.yaxis.max)
        return;

      var i, j, dataset = that.plot.getData();
      for (i = 0; i < dataset.length; ++i) {
        var series = dataset[i];

        // find the nearest points, x-wise
        for (j = 0; j < series.data.length; ++j)
          if (series.data[j][0] > latestPosition.x)
            break;

        // now interpolate
        var y, p1 = series.data[j - 1], p2 = series.data[j];
        if (p1 == null)
          y = p2[1];
        else if (p2 == null)
          y = p1[1];
        else
          y = p1[1] + (p2[1] - p1[1]) * (latestPosition.x - p1[0]) / (p2[0] - p1[0]);

        var label = series.label_;
        legends[label.toLowerCase()].text(label + " = " + y.toFixed(1) + "%");
      }
    }

    chartElem.bind("plothover", function(event, pos/*, item*/) {
      latestPosition = pos;
      if (!updateLegendTimeout)
        updateLegendTimeout = setTimeout(updateLegend, 50);
    }).bind("mouseout", function() {
              var dataset = that.plot.getData();
              for (var i = 0; i < dataset.length; ++i) {
                var label = dataset[i].label_;
                legends[label.toLowerCase()].text(label);
              }
            });
  },

  initLegend: function(legendElem) {
    var that = this;

    legendElem.find("input").change(function() {
      var allData = that.chartData,
              data = [];

      if ($j("#show-cpu").is(":checked")) {
        data.push(allData[0]);
      }
      if ($j("#show-disk").is(":checked")) {
        data.push(allData[1]);
      }
      if ($j("#show-memory").is(":checked")) {
        data.push(allData[2]);
      }

      that.plot.setData(data);
      // that.plot.setupGrid();
      that.plot.draw();
    });

    legendElem.children().each(function() {
      var self = $j(this),
              className = self.attr("class"),
              color = that.COLORS[className];

      var div = $j("<div>&nbsp;</div>").css({
        display: "inline-block",
        width: 16,
        height: 16,
        marginRight: 6,
        background: color
      });
      self.children("label").before(div);

      self.css({clear: "both"});
      self.children("input").css({"float": "right"});
    });

    legendElem.width(160);
  }


};

BS.Perfmon.Util = {
  formatTime: function(value) {
    var date = new Date(value);
    return date.getHours() + ":" + this.toString2(date.getMinutes()) + ":" + this.toString2(date.getSeconds());
  },

  toString2: function(num) {
    if (num < 10) {
      return "0" + num;
    }
    return num;
  }
};
