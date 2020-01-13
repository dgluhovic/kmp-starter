package com.kmp_starter.core.base

class AndroidBaseVMTest : BaseVMTest(sleeper = object : Sleeper {
    override fun sleepThread(delay: Long) {
        Thread.sleep(delay)
    }
})