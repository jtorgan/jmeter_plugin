if (!window.$j) {
    window.$j = window.jQuery;
}

JMeterLog = {
    _jmeterLogTitle: null,
    _jmeterLogValues: null,
    _isVisible: false,

    init: function (artifactPath) {
        var filePath = this._getFilePath(artifactPath);
        if (filePath) {
            window.requestFileSystem = window.requestFileSystem || window.webkitRequestFileSystem;
            var xhr = new XMLHttpRequest();
            xhr.open('GET', filePath, true);
            xhr.responseType = 'text';
            var that = this;
            xhr.onload = function (e) {
                if (this.status == 200) {
                    var lines = this.responseText.split("\n");
                    that._jmeterLogTitle = lines[0].split(",");
                    that._jmeterLog = lines.slice(1);
                }
            };
            xhr.send();
        }
    },

    _getFilePath: function(artifactPath) {
        var url;
        $j.ajax({
            type: "get",
            url: artifactPath,
            data: "lazy-tree-update=1",
            async: false,
            success: function(data) {
                $j(data).find("a").each(function() {
                    var href = $j(this).attr("href");
                    if (href.contains(".jtl")) {
                        url = href;
                    }
                });
            }
        });
        return url;
    },

    show: function (from, to) {
        $j("#loadingLog").show();

        if (this._isVisible) {
            $j("#jmeterLogContainer").empty();
        }

        var logHolder = $j('<table></table>');
        this._createTitle(logHolder);
        this._createLogData(from, to, logHolder);
        $j("#jmeterLogContainer").append(logHolder);

        $j("#loadingLog").hide();
        $j("#jmeterLogDiv").css("display", "block");
        this._isVisible = true;
    },

    hide: function() {
        $j("#jmeterLogDiv").css("display", "none");
        $j("#jmeterLogContainer").empty();
        this._isVisible = false;
    },

    _createTitle: function(logHolder) {
        if (this._jmeterLogTitle) {
            var titleHolder = $j('<tr id="jmeterLogTitle"></tr>');
            this._jmeterLogTitle.each(function(titlePart) {
                $j(titleHolder).append($j('<th></th>').text(titlePart));
            });
            $j(logHolder).append(titleHolder);
        }
    },

    _createLogData: function(from, to, logHolder) {
        if (this._jmeterLog) {
            this._jmeterLog.each(function(line) {
                var itemHolder = $j('<tr></tr>');
                var items = line.split(",");
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