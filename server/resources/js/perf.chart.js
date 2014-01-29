if (!window.$j) {
  window.$j = window.jQuery;
}

BS.PerfStat = {
  initPlot: function (container, data, yformat, label, logLines, logTitles) {
    container.css({
      width: 1400,
      height: 150
    });

    var settings = this.initSettings(yformat);

    var plot = window.$j.plot(container, data, settings);
    plot.unhighlightSerie = function (seriess) { };
    plot.showToolTip = function (seriess) { };

    function showTooltip(x, y, contents) {
      $j('<div id="tooltip">' + contents + '</div>').css({
        position: 'absolute',
        display: 'none',
        top: y + 6,
        left: x + 12,
        border: '1px solid rgba(0, 0, 0, 0.2)',
        padding: '2px',
        'background-color': '#ffffff',
        'box-shadow': '0 5px 10px rgba(0, 0, 0, 0.2)'
      }).appendTo("body").fadeIn(200);
    }

    var previousPoint = null;

    container.unbind("plothover").bind("plothover", function (event, pos, item) {
      if (item) {
        if (previousPoint != item.dataIndex) {
          previousPoint = item.dataIndex;
          $j("#tooltip").remove();
          var value = item.datapoint[1];
          var msg = label + "<b> " + value + " " + yformat + " </b> ";
          if (logLines && logTitles && logLines[item.datapoint[0]]) {
            msg = msg + "<br/><b>Log line details:</b>";
            var parts = logLines[item.datapoint[0]];
            for (var i = 0; i < logTitles.length; i++) {
              if (i == 0) {
                msg = msg + "<br/><i style='color: #537081'>" + logTitles[i] + ": </i> " + BS.PerfStat.Format.formatTime(Number(parts[i])) + " (" + parts[i] + ")";
              } else {
                msg = msg + "<br/><i style='color: #537081'>" + logTitles[i] + ": </i> " + parts[i];
              }
            }
          }
          showTooltip(item.pageX, item.pageY, msg);
        }
      } else {
        $j("#tooltip").remove();
        previousPoint = null;
      }
    });

    container.unbind("plotselected").bind("plotselected", function (event, ranges) {
      if (ranges.xaxis) {
        var start = parseInt(ranges.xaxis.from.toFixed());
        var end = parseInt(ranges.xaxis.to.toFixed());

        plot = $j.plot(container, data,
                $j.extend(true, {xaxis: {min: start, max: end}}, settings));
        plot.unhighlightSerie = function (seriess) { };
        plot.showToolTip = function (seriess) { };
      } else {
        plot.clearSelection();
      }
    });

    container.unbind("plotunselected").bind("plotunselected", function () {
      plot = $j.plot(container, data, settings);
      plot.unhighlightSerie = function (seriess) { };
      plot.showToolTip = function (seriess) { };
    });

    container.unbind("plotclick");
  },

  initSettings: function (yformat) {
    return {
      series: {
        lines: { show: true },
        points: { show: true, radius: 1 }
      },
      grid: {
        hoverable: true,
        clickable: true
      },
      xaxis: {
        mode: 'time',
        tickFormatter: function (val) {
          return BS.PerfStat.Format.formatTime(val);
        }
      },
      yaxis: {
        tickFormatter: function (val) {
          return BS.PerfStat.Format.format(val, yformat, false, 0);
        },
        labelWidth: 50
      },
      legend: {
        show: false
      },
      selection: {
        mode: "x"
      },
      BSChart: {
        format: "duration",
        onHighlightSerie: function (series) {
        }
      }
    };
  }
};

BS.PerfStat.Format = {
  formatTime: function (value) {
    var date = new Date(value);
    return date.getHours() + ":" + this.toString2(date.getMinutes()) + ":" + this.toString2(date.getSeconds());
  },

  toString2: function (num) {
    if (num < 10) {
      return "0" + num;
    }
    return num;
  },
  //  Y-axis and legend crosshair formatting
  bytes:  "byte",
  percent: "%",
  ms: "ms",
  number: "",

  format: function(value, format, isBytes, precision) {
    if (isBytes) {
      return this._formatBytes(value, format);
    }
    return this._formatNumber(value, format, precision);
  },
  _formatBytes: function(bytes, format) {
    if (format == 'Gb')
      bytes = (bytes / 1073741824);
    if (format == 'Mb')
      bytes = (bytes / 1048576);
    if (format == 'Kb')
      bytes = (bytes / 1024);
    return bytes.toFixed(2) + ' ' + format;
  },

  _formatNumber: function(number, format, precision) {
    number = number.toFixed(precision);
    return number + ' ' + format;
  }
};
