if (!window.$j) {
    window.$j = window.jQuery;
}

BS.JMeterPerfmon = {
    subPlots: {},

    addPlot: function(plotID, data, max, format, startColor) {
        var isBytesFormat = format == 'bytes';
        var correctedFormat = Format.getFormatByMax(max, format);

        this.initPlot(plotID, data, max, correctedFormat, isBytesFormat, startColor);
        this.initLegend(plotID);
        this.initTooltip(plotID, correctedFormat, isBytesFormat);
        this.initLegendCrosshair(plotID, correctedFormat, isBytesFormat);
        this.initClick(plotID);
    },

    initPlot: function (plotID, data, max, format, isBytesFormat, startColor) {
        var chartElem = $j("#chart" + plotID);

        var chartData = [];
        var i = startColor;
        for (var key in data) {
            chartData.push({ data: data[key], label: key, color: ++i, lines: {order: 1}});
        }

        chartElem.css({
            width: 900,
            height: 200
        });

        var stacked =  false;//plotID.indexOf("memory") != -1 || plotID.indexOf("cpu") != -1 || plotID.indexOf("pool") != -1;

        var settings = {
            series: {
                stack: stacked,
                lines: { show: true, fill: stacked},
                points: { show: true, radius: 1 }
            },
            legend: {
                show: false
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
                    return Format.formatTime(val);
                }
            },
            yaxis: {
                min: (format == Format.percent) ? 0 : null,
                max: (format == Format.percent) ? 100 : null,
                tickFormatter: function (val) {
                    return Format.format(val, format, isBytesFormat, 0);
                },
                labelWidth: 50
            },
            selection: {
                mode: "x"
            }
        };

        this.subPlots[plotID] = {
            dataset: chartData,
            settings: settings,
            plot: $j.plot(chartElem, chartData, settings),
            selected: false,
            zoom: false
        };
    },

    initLegend: function(plotID) {
        // set colors
        var data = this.subPlots[plotID].plot.getData();
        var colors = {};
        for (var i = 0; i < data.length; ++i) {
            colors[data[i].label] = data[i].color;
        }
        $j("#legend"+ plotID).children().each(function() {
            var self = $j(this);
            var key = self.text().trim();

            var div = $j("<div>&nbsp;</div>").css({
                display: "inline-block",
                width: 16,
                height: 16,
                marginRight: 6,
                background: colors[key]
            });
            self.children("label").before(div);
        });

        // bind events
        var legendElem = $j("#legend" + plotID);
        var subplot = this.subPlots[plotID];
        function toggleSeries() {
            var data = [];
            var allData = subplot.dataset;
            legendElem.find("input:checked").each(function () {
                var key = $j(this).attr("name");
                if (key) {
                    for (var i in allData) {
                        if (allData[i].label == key) {
                            data.push(allData[i]);
                            break;
                        }
                    }
                }
            });
            subplot.plot.setData(data);
            subplot.plot.draw();
        }

        legendElem.find("input").change(function () {
            toggleSeries();
        });
    },

    initTooltip: function (plotID, format, isBytesFormat) {
        var chartElem = $j("#chart" + plotID);
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

        chartElem.bind("plothover", function (event, pos, item) {
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;

                    $j("#tooltip").remove();
                    var time = Format.formatTime(item.datapoint[0]),
                            value = Format.format(item.datapoint[1], format, isBytesFormat, (format == Format.percent) ? 2 : 0);
                    showTooltip(item.pageX, item.pageY, item.series.label.split("=")[0].trim() + " at " + time + " is <b>" + value + "</b>");
                }
            } else {
                $j("#tooltip").remove();
                previousPoint = null;
            }
        });
    },

    initLegendCrosshair: function(plotID, format, isBytesFormat) {
        var legends = {};
        $j("#legend" + plotID).find("label").each(function() {
            var label = $j(this).text().trim();
            legends[label] = $j(this);
        });

        var plot = this.subPlots[plotID].plot;
        var updateLegendTimeout = null;
        var latestPosition = null;

        function updateLegend() {
            updateLegendTimeout = null;

            var axes = plot.getAxes();

            if (latestPosition.x < axes.xaxis.min || latestPosition.x > axes.xaxis.max ||
                    latestPosition.y < axes.yaxis.min || latestPosition.y > axes.yaxis.max)
                return;

            var i, j, dataset = plot.getData();
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

                var label = series.label;
                legends[label].text(label + " = " + Format.format(y, format, isBytesFormat, (format == Format.percent) ? 2 : 0));
            }
        }

        $j("#chart" + plotID).bind("plothover",function (event, pos/*, item*/) {
            latestPosition = pos;
            if (!updateLegendTimeout)
                updateLegendTimeout = setTimeout(updateLegend, 50);
        }).bind("mouseout", function (event) {
                    var dataset = plot.getData();
                    for (var i = 0; i < dataset.length; ++i) {
                        var label = dataset[i].label.split("=")[0].trim();
                        legends[label].text(label);
                    }
                });
    },

    initClick: function (plotID) {
        var that = this;
        this.selectionEvent = false;

        $j("#chart" + plotID).bind("plotselected", function (event, ranges) {
            if (ranges.xaxis) {
                that.selectionEvent = true;
                var start = parseInt(ranges.xaxis.from.toFixed());
                var end = parseInt(ranges.xaxis.to.toFixed());

                that.zoomIn(plotID, start, end);
                that.showLog(plotID, start, end, true);
            } else {
                that.clear(plotID);
            }
        }).bind("plotclick", function (event, pos, item) {
                    if (item && !that.selectionEvent) {
                        var start = item.datapoint[0];
                        var end = item.series.data[item.dataIndex + 1][0];
                        that.showLog(plotID, start, end, false);
                    } else if (!item && !that.selectionEvent){
                        that.clear(plotID);
                        that.zoomOut(plotID);
                    }
                    that.selectionEvent = false;
                });
    },

    showLog: function(plotID, start, end, moreDetails) {
        this.clearAllSelection();

        var plot = this.subPlots[plotID].plot;
        plot.isSelected = true;
        plot.setSelection({ xaxis: { from: start, to: end }}, true);

        var period = Format.formatTime(start, moreDetails) + '(' + start + ') - ' + Format.formatTime(end, moreDetails) + '(' + end + ') : ';
        $j("#jmeterTimePeriod").text(period);
        JMeterLog.show(start, end);
    },

    clear: function(plotID) {
        var plot = this.subPlots[plotID].plot;
        plot.clearSelection();
        plot.isSelected = false;
        JMeterLog.hide();
    },

    zoomOut: function(plotID) {
        var data = this.subPlots[plotID].plot.getData();
        if (data.length != 0) {
            this.subPlots[plotID].plot = $j.plot($j("#chart" + plotID), data, this.subPlots[plotID].settings);
        }
    },

    zoomIn: function(plotID, from, to) {
        this.subPlots[plotID].plot = $j.plot( $j("#chart" + plotID), this.subPlots[plotID].plot.getData(),
                $j.extend(true, {xaxis:{min: from, max: to}}, this.subPlots[plotID].settings));
    },

    clearAllSelection: function() {
        for(var key in this.subPlots) {
            this.subPlots[key].plot.clearSelection();
            this.subPlots[key].plot.isSelected = false;
        }
    }

}


// graph states
var stateShown = "shown";
var stateHidden = "hidden";

function setUIState(state, id) {
    if (state.indexOf(stateShown) != -1) {
        $j('#' + id).show();
        $j("a[name=" + id + "]").text("[Hide]");
    } else if (state.indexOf(stateHidden) != -1) {
        $j('#' + id).parent().css("padding-bottom", "0");
        $j('#' + id).hide();
        $j("a[name=" + id + "]").text("[Show]");
    }
}


function sendState(buildTypeId, newState, graphID) {
    BS.ajaxRequest("/app/jmeter/**", {
        method: "post",
        parameters: 'buildTypeId=' + buildTypeId + '&state=' + newState + '&graphId=' + graphID,
        onComplete: function(transport) {
            if (transport.responseXML) {
                alert(transport.responseXML);
            }
        }
    });
}

$j(document).ready(function () {
    var buildTypeId = $j("input[name=buildTypeId]").val().trim();

    $j('.collapse').click(function (event) {
        event.preventDefault();
        var newState = $j(this).text().indexOf("Show") != -1 ? stateShown : stateHidden;
        var graphID = $j(this).attr('name');
        setUIState(newState, graphID);
        if (newState == stateShown) {
            $j('#' + graphID).parent().css("padding-bottom", "20px");
        }
        sendState(buildTypeId, newState, graphID);
    });

    $j('.expandAll').click(function (event) {
        event.preventDefault();
        $j('.collapsible').each( function() {
            $j(this).closest('table').find('a').text("[Hide]");
            $j(this).show();
        });
        sendState(buildTypeId, stateShown, "");
    });
});

