package com.huji.ARfurnitureplanner.Help


import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.sandy.kotlinfragment.HelpTechnicalDescriptionFrag
import com.huji.ARfurnitureplanner.R

class HelpActivity : AppCompatActivity() {
    private val buttonArrayList = ArrayList<String>()
    private lateinit var toMeasurement: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.greeting_activity)

        var fManager = supportFragmentManager

        var tx = fManager.beginTransaction()

        tx.add(R.id.frag, HelpTechnicalDescriptionFrag())
        tx.addToBackStack(null)
        tx.commit()


    }

    override fun onBackPressed() {}


}
