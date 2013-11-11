if (!window.$j) {
    window.$j = window.jQuery;
}

PerfTestLog = {
    _logTitle: null,
    _logValues: null,
    _isVisible: false,

    init: function (artifactPath, logFileName) {
        var filePath = this._getFilePath(artifactPath, logFileName);
        if (filePath) {
            window.requestFileSystem = window.requestFileSystem || window.webkitRequestFileSystem;
            var xhr = new XMLHttpRequest();
            xhr.open('GET', filePath, true);
            xhr.responseType = 'text';
            var that = this;
            xhr.onload = function (e) {
                if (this.status == 200) {
                    var lines = this.responseText.split("\n");
                    that._logTitle = lines[0].split("\t");
                    that._jmeterLog = lines.slice(1);
                }
            };
            xhr.send();
        }
    },

    _getFilePath: function(artifactDir, fileName) {
        var url;
        $j.ajax({
            type: "get",
            url: artifactDir,
            data: "lazy-tree-update=1",
            async: false,
            success: function(data) {
                $j(data).find("a").each(function() {
                    var href = $j(this).attr("href");
                    if (href.indexOf(fileName) > -1) {
                        url = href;
                    }
                });
            }
        });
        return url;
    },

    show: function (from, to) {

        if (this._isVisible) {
            $j("#jmeterLogContainer").empty();
        }

        var logHolder = $j('<table style="width: auto"></table>');
        this._createTitle(logHolder);
        this._createLogData(from, to, logHolder);
        $j("#jmeterLogContainer").append(logHolder);

        $j("#jmeterLogDiv").css("display", "block");
        this._isVisible = true;
    },

    hide: function() {
        $j("#jmeterLogDiv").css("display", "none");
        $j("#jmeterLogContainer").empty();
        this._isVisible = false;
    },

    _createTitle: function(logHolder) {
        if (this._logTitle) {
            var titleHolder = $j('<tr id="jmeterLogTitle"></tr>');
            this._logTitle.each(function(titlePart) {
                var thText = '<th></th>';
                if (titlePart == "label") {
                    thText = '<th style="min-width: 150px;"></th>';
                }
                $j(titleHolder).append($j(thText).text(titlePart));
            });
            $j(logHolder).append(titleHolder);
        }
    },

    _createLogData: function(from, to, logHolder) {
        if (this._jmeterLog) {
            this._jmeterLog.each(function(line) {
                var itemHolder = $j('<tr></tr>');
                var items = line.split("\t");
                if (items[0] >= from && items[0] < to)  {
                    for(var i = 0; i < items.length ; i++) {
                        $j(itemHolder).append($j('<td></td>').text(items[i]));
                    }
                    $j(logHolder).append(itemHolder);
                }
            });
        }
    }
}