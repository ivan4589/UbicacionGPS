package com.example.ubicaciongps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.Serializable
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnActualizar: View
    private lateinit var btnMiUbicacion: View
    private var lastTaxiList: List<Taxi> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewTaxis)
        progressBar = findViewById(R.id.progressBar)
        btnActualizar = findViewById(R.id.btnActualizar)
        btnMiUbicacion = findViewById(R.id.btnMiUbicacion)

        btnActualizar.setOnClickListener {
            fetchData()
        }

        btnMiUbicacion.setOnClickListener {
            if (lastTaxiList.isNotEmpty()) {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("TAXI_LIST", lastTaxiList as Serializable)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Cargando datos, espera un momento...", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchData()
    }

    private fun fetchData() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = URL("https://clasespersonales.com/taxis/listaxis.php").readText()
                val jsonObject = JSONObject(response)
                val taxisArray = jsonObject.getJSONArray("taxis")
                val list = mutableListOf<Taxi>()

                for (i in 0 until taxisArray.length()) {
                    val item = taxisArray.getJSONObject(i)
                    list.add(
                        Taxi(
                            movil = item.getString("movil"),
                            carnet = item.getString("chofer"),
                            latitud = item.getString("latitud"),
                            longitud = item.getString("longitud")
                        )
                    )
                }

                lastTaxiList = list

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    recyclerView.adapter = TaxiAdapter(list)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}