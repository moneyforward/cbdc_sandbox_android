package com.moneyforward.cbdcsandbox.model.exception

import com.moneyforward.cbdcsandbox.model.MyNumberCommandError

class MyNumberException(myNumberCommandError: MyNumberCommandError, argRetryCount: Int = 0) :
    Exception("MyNumber Error") {
    val error = myNumberCommandError
    val retryCount = argRetryCount
}