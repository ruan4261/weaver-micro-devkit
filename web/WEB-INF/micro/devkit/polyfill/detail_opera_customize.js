(function () {

  /**
   * @type {{ALL: number, ADD: number, DEL: number}}
   */
  window.DetailOperaType = {
    ALL: 0,
    DEL: -1,
    ADD: 1
  }

  var resetDetailOperaHandler = function (detailTableIndex) {
    if (window['_custom_detail_opera_func_chain' + detailTableIndex] !== undefined)
      return

    window['_custom_detail_opera_func_chain' + detailTableIndex] = []

    var chainHandler = function (type) {
      var funcChain = window['_custom_detail_opera_func_chain' + detailTableIndex]
      for (var i = 0; i < funcChain.length; i++) {
        var funcObj = funcChain[i]
        var funcType = funcObj['type']
        if (type === DetailOperaType.ALL || funcType === type) {
          funcObj['callback']()
        }
      }
    }

    window['_customAddFun' + detailTableIndex] = chainHandler.bind(undefined, DetailOperaType.ADD)
    window['_customDelFun' + detailTableIndex] = chainHandler.bind(undefined, DetailOperaType.DEL)

    // ie8+
    try {
      Object.defineProperty(window, '_custom_detail_opera_func_chain' + detailTableIndex, {writable: false})
      Object.defineProperty(window, '_customAddFun' + detailTableIndex, {writable: false})
      Object.defineProperty(window, '_customDelFun' + detailTableIndex, {writable: false})
    } catch (e) {
    }
  }

  /**
   * 对明细行进行操作时触发
   * 系统调用方:
   * ADD: window.['_customAddFun' + detailTableIndex]
   * DEL: window.['_customDelFun' + detailTableIndex]
   *
   * @param detailTableIndex 明细表索引, 从0开始
   * @param type 见window.detailOperaType
   * @param func 回调函数, 无参数
   */
  window.registerDetailOpera = function (detailTableIndex, type, func) {
    resetDetailOperaHandler(detailTableIndex)
    var funcObj = {
      type: type,
      callback: func
    }
    window['_custom_detail_opera_func_chain' + detailTableIndex].push(funcObj)
  }

  /**
   * 获取表单页面中指定明细表所有行数据的索引
   *
   * @param detailIndex 明细表索引是对于当前流程而言的, 与模板无关(同一个明细表在不同节点模板内拥有相同的索引)
   */
  window.getCheckNodeIndexSeq = function (detailIndex) {
    var key = "check_node_" + detailIndex
    var jqSeq = jQuery('[name="' + key + '"]')
    var idxSeq = []
    for (var i = 0; i < jqSeq.length; i++) {
      idxSeq[i] = jqSeq[i].value
    }
    return idxSeq
  }

  /**
   * 对指定明细表指定字段(列)绑定变化事件, 绑定的数据行包括已存在的数据行及未来新建的数据行
   * 在参数完全相同的情况下, 该方法只需执行一次足矣
   *
   * @param detailIndex 明细表索引是对于当前流程而言的, 与模板无关(同一个明细表在不同节点模板内拥有相同的索引)
   * @param fieldId 单个字段的id
   * @param event 指定触发的原事件, 其将被用于jQuery.bind的第一个参数,
   *              如果该字段为空, 则默认使用bindPropertyChange进行绑定
   * @param func 可用参数列表[rowIdx, changedElement, event], 真实回调方法会bind此func, 如无特例, this指向window
   */
  window.bindDetailFieldEvent = function (detailIndex, fieldId, event, func) {
    var bindEvent = function (idx) {
      var jq = jQuery("#field" + fieldId + "_" + idx)
      var ele = jq[0]
      var callback = func.bind(undefined, idx, ele)
      if (event) {
        jq.bind(event, callback)
      } else {
        jq.bindPropertyChange(callback)
      }
    }

    // bind data row already existed
    var idxSeq = getCheckNodeIndexSeq(detailIndex)
    for (var i = 0; i < idxSeq.length; i++) {
      var idx = idxSeq[i]
      bindEvent(idx)
    }

    // bind new data row
    window.registerDetailOpera(detailIndex, DetailOperaType.ADD, function () {
      var seq = getCheckNodeIndexSeq(detailIndex)
      var len = seq.length
      var lastIdx = seq[len - 1]// the new data added
      bindEvent(lastIdx)
    })
  }

  window.bindDetailFieldChangeEvent = function (detailIndex, fieldId, func) {
    window.bindDetailFieldEvent(detailIndex, fieldId, undefined, func)
  }

})()