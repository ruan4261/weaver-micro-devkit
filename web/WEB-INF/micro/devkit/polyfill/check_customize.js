// weaverEC jQuery
jQuery(document).ready(function () {
  if (!Array.isArray(window._check_customize_))
    window._check_customize_ = []
  window._check_customize_.push(window.checkCustomize)
  window._check_customize_.push(function () {
    // enter your custom verification program
  })

  window.checkCustomize = function () {
    var len = window._check_customize_.length
    for (var i = 0; i < len; i++) {
      var checkCustomize = window._check_customize_[i]
      if (typeof checkCustomize === 'function') {
        try {
          if (checkCustomize() === false)
            return false
        } catch (e) {
          alert('[checkCustomize]校验程序异常!')
          if (window.console) {
            console.log(window._check_customize_[i])
            console.log(e)
          } else alert(e.toString())
          return false
        }
      }
    }
    return true
  }

  // ie8+
  try {
    Object.defineProperty(window, 'checkCustomize', {writable: false})
    Object.defineProperty(window, '_check_customize_', {writable: false})
  } catch (e) {
  }
})