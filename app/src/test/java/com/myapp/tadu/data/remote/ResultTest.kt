package com.myapp.tadu.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun success_carriesTheValue() {
        val result = Result.Success("task")

        assertEquals("task", result.data)
    }

    @Test
    fun error_carriesTheException() {
        val exception = IllegalStateException("boom")

        val result = Result.Error(exception)

        assertTrue(result.exception === exception)
    }
}
