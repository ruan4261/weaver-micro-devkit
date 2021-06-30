(function () {
  window.dynamic_import_javascript = function (url, code, type, frameWindow) {
    frameWindow = frameWindow || window
    var doc = frameWindow.document
    var container = frameWindow.document.head || frameWindow.document.body
    var newEle = doc.createElement('script')
    if (url)
      newEle.src = url
    if (code)
      newEle.appendChild(document.createTextNode(code))
    newEle.type = type || 'text/javascript'
    container.append(newEle)
  }

  var _curry_dynamic_import_javascript = function (url) {
    window.dynamic_import_javascript(url, false, false, false)
  }

  _curry_dynamic_import_javascript('/micro/devkit/polyfill/get_url_params.js')
  _curry_dynamic_import_javascript('/micro/devkit/polyfill/check_customize.js')
  _curry_dynamic_import_javascript('/micro/devkit/polyfill/detail_opera_customize.js')
})()