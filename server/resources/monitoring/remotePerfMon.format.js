if (!window.$j) {
  window.$j = window.jQuery;
}

Format = {
//  X-axis formatting
  formatTime: function (value, includeMs) {
    var date = new Date(value);
    var time = date.getHours() + ":" + this.toString2(date.getMinutes()) + ":" + this.toString2(date.getSeconds());
    if (includeMs) {
      time = time + ":" + date.getMilliseconds();
    }
    return time;
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

  getFormatByMax: function(max, format) {
    if (format == this.bytes) {
      return this._getBytesFormat(max);
    }
    return this._getNumberFormat(max) + format;
  },

  _getBytesFormat: function(max) {
    if (max >= 1073741824)
      return 'Gb';
    if (max >= 1048576)
      return 'Mb';
    if (max >= 1024)
      return 'Kb';
    return 'bytes';
  },

  _getNumberFormat: function(max) {
//        if (max > 1000000)
//            return 'M';
//        if (max > 10000)
//            return 'K';
    return '';
  },

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
//        if (getMetricType.indexOf('M') == 0) {
//            number = (number / 1000000).toFixed(0);
//        } else if (getMetricType.indexOf('K') == 0) {
//            number = (number / 1000).toFixed(0);
//        } else {
    number = number.toFixed(precision);
    return number + ' ' + format;
  }
}