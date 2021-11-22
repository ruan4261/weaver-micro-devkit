(function () {
  window.getUrlParams = function () {
    var query = location.search// from ? to #
    var params = {}
    if (query.length > 1) {
      var queryStr = query.substr(1)
      var entry = queryStr.split('&')
      for (var i = 0; i < entry.length; i++) {
        var kv = entry[i].split('=')
        var key = kv[0]
        // maybe undefined
        params[kv] = kv[1]
      }
    }
    return params
  }
})()