package com.lws.infinitelooprv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = mutableListOf<String>()
        for (i in 0..30) {
            list.add("abc${i + 1}")
        }
        val layoutManager = LooperLayoutManager()
        val adapter = MyAdapter(this, list)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }
}
