package com.example.flying

import org.junit.Test

import org.junit.Assert.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.security.AccessController.getContext


class ut_object_reader {
    @Test
    fun read_obj_file() {

        val in_stream : InputStream = getContext().getResources().openRawResource(R.raw.ego_fighter)

        in_stream.bufferedReader().forEachLine {
            println(it)
        }


        assertEquals(4, 2 + 2)
    }
}


