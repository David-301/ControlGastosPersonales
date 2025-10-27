package com.ch220048.controlgastospersonales

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EstadisticasActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvTotalGastos: TextView
    private lateinit var tvPromedioDiario: TextView
    private lateinit var tvCategoriaMasGastada: TextView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var btnRegresar: MaterialButton

    private var listaGastos: MutableList<Gasto> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        inicializarVistas()
        configurarListeners()
        cargarGastos()
    }

    private fun inicializarVistas() {
        tvTotalGastos = findViewById(R.id.tvTotalGastos)
        tvPromedioDiario = findViewById(R.id.tvPromedioDiario)
        tvCategoriaMasGastada = findViewById(R.id.tvCategoriaMasGastada)
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        btnRegresar = findViewById(R.id.btnRegresar)
    }

    private fun configurarListeners() {
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun cargarGastos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("gastos")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                listaGastos.clear()

                for (document in documents) {
                    val gasto = document.toObject(Gasto::class.java)
                    gasto.id = document.id
                    listaGastos.add(gasto)
                }

                if (listaGastos.isNotEmpty()) {
                    calcularEstadisticas()
                    configurarGraficoPastel()
                    configurarGraficoBarras()
                } else {
                    mostrarSinDatos()
                }
            }
            .addOnFailureListener {
                mostrarSinDatos()
            }
    }

    private fun calcularEstadisticas() {
        // Calcular total de gastos
        var total = 0.0
        for (gasto in listaGastos) {
            total += gasto.monto
        }
        tvTotalGastos.text = "$${String.format("%.2f", total)}"

        // Calcular promedio diario (últimos 30 días)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val hace30Dias = calendar.timeInMillis

        var totalUltimos30Dias = 0.0
        var diasConGastos = 0
        val fechasConGastos = mutableSetOf<String>()

        for (gasto in listaGastos) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaGasto = dateFormat.parse(gasto.fecha)

                if (fechaGasto != null && fechaGasto.time >= hace30Dias) {
                    totalUltimos30Dias += gasto.monto
                    fechasConGastos.add(gasto.fecha)
                }
            } catch (e: Exception) {
                // Ignorar gastos con fechas inválidas
            }
        }

        diasConGastos = if (fechasConGastos.isEmpty()) 1 else fechasConGastos.size
        val promedioDiario = totalUltimos30Dias / diasConGastos
        tvPromedioDiario.text = "$${String.format("%.2f", promedioDiario)}"

        // Encontrar categoría más gastada
        val gastosPorCategoria = mutableMapOf<String, Double>()

        for (gasto in listaGastos) {
            val categoriaActual = gastosPorCategoria[gasto.categoria] ?: 0.0
            gastosPorCategoria[gasto.categoria] = categoriaActual + gasto.monto
        }

        val categoriaMasGastada = gastosPorCategoria.maxByOrNull { it.value }
        if (categoriaMasGastada != null) {
            tvCategoriaMasGastada.text = "${categoriaMasGastada.key} ($${String.format("%.2f", categoriaMasGastada.value)})"
        } else {
            tvCategoriaMasGastada.text = "N/A"
        }
    }

    private fun configurarGraficoPastel() {
        // Agrupar gastos por categoría
        val gastosPorCategoria = mutableMapOf<String, Float>()

        for (gasto in listaGastos) {
            val categoriaActual = gastosPorCategoria[gasto.categoria] ?: 0f
            gastosPorCategoria[gasto.categoria] = categoriaActual + gasto.monto.toFloat()
        }

        // Crear entradas para el gráfico
        val entries = ArrayList<PieEntry>()
        for ((categoria, monto) in gastosPorCategoria) {
            entries.add(PieEntry(monto, categoria))
        }

        // Configurar dataset
        val dataSet = PieDataSet(entries, "")

        // Colores personalizados
        val colors = ArrayList<Int>()
        colors.add(Color.rgb(103, 126, 234))  // Morado
        colors.add(Color.rgb(255, 159, 64))   // Naranja
        colors.add(Color.rgb(255, 99, 132))   // Rojo
        colors.add(Color.rgb(75, 192, 192))   // Verde agua
        colors.add(Color.rgb(54, 162, 235))   // Azul
        colors.add(Color.rgb(153, 102, 255))  // Morado claro
        colors.add(Color.rgb(255, 205, 86))   // Amarillo
        colors.add(Color.rgb(201, 203, 207))  // Gris
        colors.add(Color.rgb(255, 140, 0))    // Naranja oscuro

        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        // Configurar datos
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        // Configurar gráfico
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "Categorías"
        pieChart.setCenterTextSize(16f)
        pieChart.setUsePercentValues(true)

        // Configurar leyenda
        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.setDrawInside(false)
        legend.textSize = 10f

        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    private fun configurarGraficoBarras() {
        // Agrupar gastos por mes
        val gastosPorMes = mutableMapOf<String, Float>()
        val dateFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        for (gasto in listaGastos) {
            try {
                val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fecha = dateFormatInput.parse(gasto.fecha)

                if (fecha != null) {
                    val mesAño = dateFormat.format(fecha)
                    val montoActual = gastosPorMes[mesAño] ?: 0f
                    gastosPorMes[mesAño] = montoActual + gasto.monto.toFloat()
                }
            } catch (e: Exception) {
                // Ignorar gastos con fechas inválidas
            }
        }

        // Ordenar por fecha
        val gastosPorMesOrdenados = gastosPorMes.toSortedMap()

        // Crear entradas para el gráfico (últimos 6 meses)
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f

        // Convertir a lista y tomar los últimos 6 elementos
        val listaGastosMes = gastosPorMesOrdenados.entries.toList()
        val ultimos6Meses = if (listaGastosMes.size > 6) {
            listaGastosMes.subList(listaGastosMes.size - 6, listaGastosMes.size)
        } else {
            listaGastosMes
        }

        for (entry in ultimos6Meses) {
            val mesAño = entry.key
            val monto = entry.value

            entries.add(BarEntry(index, monto))

            // Convertir "MM/yyyy" a "MMM" (Ene, Feb, etc.)
            try {
                val fecha = dateFormat.parse(mesAño)
                if (fecha != null) {
                    labels.add(monthFormat.format(fecha))
                } else {
                    labels.add(mesAño)
                }
            } catch (e: Exception) {
                labels.add(mesAño)
            }

            index++
        }

        // Si no hay datos, mostrar mensaje
        if (entries.isEmpty()) {
            entries.add(BarEntry(0f, 0f))
            labels.add("Sin datos")
        }

        // Configurar dataset
        val dataSet = BarDataSet(entries, "Gastos Mensuales")
        dataSet.color = Color.rgb(103, 126, 234)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK

        // Configurar datos
        val data = BarData(dataSet)
        data.barWidth = 0.5f

        // Configurar gráfico
        barChart.data = data
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)

        // Configurar eje X
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Configurar eje Y izquierdo
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f

        // Ocultar eje Y derecho
        barChart.axisRight.isEnabled = false

        // Configurar leyenda
        val legend = barChart.legend
        legend.isEnabled = false

        barChart.invalidate()
    }

    private fun mostrarSinDatos() {
        tvTotalGastos.text = "$0.00"
        tvPromedioDiario.text = "$0.00"
        tvCategoriaMasGastada.text = "No hay datos"

        // Configurar gráficos vacíos
        pieChart.clear()
        pieChart.setNoDataText("No hay gastos registrados")
        pieChart.invalidate()

        barChart.clear()
        barChart.setNoDataText("No hay gastos registrados")
        barChart.invalidate()
    }
}