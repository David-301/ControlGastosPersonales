package com.ch220048.controlgastospersonales

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Vistas
    private lateinit var toolbar: Toolbar
    private lateinit var tvTotalMes: TextView
    private lateinit var tvCantidadGastos: TextView
    private lateinit var rvGastos: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var fabAgregarGasto: FloatingActionButton
    private lateinit var scrollChipsFiltros: HorizontalScrollView
    private lateinit var chipGroupFiltros: ChipGroup

    // Adapter
    private lateinit var gastoAdapter: GastoAdapter
    private var listaGastos: MutableList<Gasto> = mutableListOf()
    private var listaGastosFiltrados: MutableList<Gasto> = mutableListOf()

    // Filtros
    private var categoriasSeleccionadas: MutableSet<String> = mutableSetOf()
    private var filtroMes: Int? = null
    private var filtroAño: Int? = null
    private var filtroPeriodo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Verificar autenticación
        if (auth.currentUser == null) {
            irALogin()
            return
        }

        // Inicializar vistas
        inicializarVistas()

        // Configurar toolbar
        setSupportActionBar(toolbar)

        // Configurar RecyclerView
        configurarRecyclerView()

        // Configurar Swipe to Delete
        configurarSwipeToDelete()

        // Configurar listeners
        configurarListeners()
    }

    override fun onResume() {
        super.onResume()
        // Cargar gastos cada vez que se vuelve a esta pantalla
        cargarGastos()
    }

    private fun inicializarVistas() {
        toolbar = findViewById(R.id.toolbar)
        tvTotalMes = findViewById(R.id.tvTotalMes)
        tvCantidadGastos = findViewById(R.id.tvCantidadGastos)
        rvGastos = findViewById(R.id.rvGastos)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        fabAgregarGasto = findViewById(R.id.fabAgregarGasto)
        scrollChipsFiltros = findViewById(R.id.scrollChipsFiltros)
        chipGroupFiltros = findViewById(R.id.chipGroupFiltros)
    }

    private fun configurarRecyclerView() {
        gastoAdapter = GastoAdapter(listaGastos) { gasto ->
            // Click en un gasto para editarlo
            val intent = Intent(this, AgregarGastoActivity::class.java)
            intent.putExtra("GASTO_ID", gasto.id)
            intent.putExtra("GASTO_NOMBRE", gasto.nombre)
            intent.putExtra("GASTO_MONTO", gasto.monto)
            intent.putExtra("GASTO_CATEGORIA", gasto.categoria)
            intent.putExtra("GASTO_FECHA", gasto.fecha)
            intent.putExtra("GASTO_NOTAS", gasto.notas)
            intent.putExtra("MODO_EDICION", true)
            startActivity(intent)
        }
        rvGastos.layoutManager = LinearLayoutManager(this)
        rvGastos.adapter = gastoAdapter
    }

    private fun configurarSwipeToDelete() {
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                eliminarGasto(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rvGastos)
    }

    private fun configurarListeners() {
        fabAgregarGasto.setOnClickListener {
            val intent = Intent(this, AgregarGastoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cargarGastos() {
        val userId = auth.currentUser?.uid ?: return

        // Obtener gastos del usuario actual
        db.collection("gastos")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(
                        this,
                        "Error al cargar gastos: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    // Convertir documentos a objetos Gasto
                    listaGastos.clear()
                    for (document in snapshots.documents) {
                        val gasto = document.toObject(Gasto::class.java)
                        if (gasto != null) {
                            gasto.id = document.id
                            listaGastos.add(gasto)
                        }
                    }

                    // Aplicar filtros si hay activos
                    if (categoriasSeleccionadas.isNotEmpty() || filtroPeriodo != null) {
                        aplicarFiltros()
                    } else {
                        // Actualizar UI sin filtros
                        mostrarGastos()
                        calcularTotalMes()
                    }

                } else {
                    // No hay gastos
                    mostrarVacio()
                }
            }
    }

    private fun eliminarGasto(position: Int) {
        // Determinar si estamos trabajando con la lista filtrada o completa
        val listaActual = if (categoriasSeleccionadas.isNotEmpty() || filtroPeriodo != null) {
            listaGastosFiltrados
        } else {
            listaGastos
        }

        if (position < 0 || position >= listaActual.size) return

        val gastoEliminado = listaActual[position]
        val posicionEliminada = position

        // Eliminar de ambas listas
        listaActual.removeAt(position)
        listaGastos.remove(gastoEliminado)
        gastoAdapter.actualizarLista(listaActual)

        // Recalcular totales
        if (listaActual.isEmpty()) {
            mostrarVacio()
        } else {
            if (categoriasSeleccionadas.isNotEmpty() || filtroPeriodo != null) {
                calcularTotalFiltrado()
                actualizarContadorFiltrado()
            } else {
                calcularTotalMes()
                actualizarContador()
            }
        }

        // Mostrar Snackbar con opción de deshacer
        Snackbar.make(
            findViewById(android.R.id.content),
            "Gasto eliminado",
            Snackbar.LENGTH_LONG
        ).setAction("DESHACER") {
            // Restaurar gasto en ambas listas
            listaActual.add(posicionEliminada, gastoEliminado)
            listaGastos.add(gastoEliminado)
            gastoAdapter.actualizarLista(listaActual)

            layoutEmpty.visibility = View.GONE
            rvGastos.visibility = View.VISIBLE

            if (categoriasSeleccionadas.isNotEmpty() || filtroPeriodo != null) {
                calcularTotalFiltrado()
                actualizarContadorFiltrado()
            } else {
                calcularTotalMes()
                actualizarContador()
            }
        }.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                // Si no presionó "Deshacer", eliminar de Firebase
                if (event != DISMISS_EVENT_ACTION) {
                    eliminarDeFirebase(gastoEliminado.id)
                }
            }
        }).show()
    }

    private fun eliminarDeFirebase(gastoId: String) {
        if (gastoId.isEmpty()) return

        db.collection("gastos")
            .document(gastoId)
            .delete()
            .addOnSuccessListener {
                // Eliminado exitosamente
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al eliminar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                // Recargar gastos para restaurar el estado correcto
                cargarGastos()
            }
    }

    private fun mostrarGastos() {
        layoutEmpty.visibility = View.GONE
        rvGastos.visibility = View.VISIBLE
        gastoAdapter.actualizarLista(listaGastos)
        actualizarContador()
    }

    private fun actualizarContador() {
        val cantidad = listaGastos.size
        tvCantidadGastos.text = if (cantidad == 1) {
            "1 gasto registrado"
        } else {
            "$cantidad gastos registrados"
        }
    }

    private fun mostrarVacio() {
        layoutEmpty.visibility = View.VISIBLE
        rvGastos.visibility = View.GONE
        tvTotalMes.text = "$0.00"
        tvCantidadGastos.text = "0 gastos registrados"
    }

    private fun calcularTotalMes() {
        // Calcular solo gastos del mes actual
        val calendar = Calendar.getInstance()
        val mesActual = calendar.get(Calendar.MONTH) + 1
        val añoActual = calendar.get(Calendar.YEAR)

        var totalMes = 0.0

        for (gasto in listaGastos) {
            // Parsear la fecha del gasto (formato: dd/MM/yyyy)
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaGasto = dateFormat.parse(gasto.fecha)

                if (fechaGasto != null) {
                    val calGasto = Calendar.getInstance()
                    calGasto.time = fechaGasto

                    val mesGasto = calGasto.get(Calendar.MONTH) + 1
                    val añoGasto = calGasto.get(Calendar.YEAR)

                    // Si es del mes y año actual, sumar
                    if (mesGasto == mesActual && añoGasto == añoActual) {
                        totalMes += gasto.monto
                    }
                }
            } catch (e: Exception) {
                // Si hay error parseando la fecha, incluir el gasto de todos modos
                totalMes += gasto.monto
            }
        }

        // Actualizar TextView
        tvTotalMes.text = "$${String.format("%.2f", totalMes)}"
    }

    // Crear menú de opciones
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_estadisticas -> {
                val intent = Intent(this, EstadisticasActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_filter -> {
                mostrarDialogoFiltros()
                true
            }
            R.id.action_logout -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun mostrarDialogoFiltros() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filtros, null)

        // Referencias a los checkboxes
        val cbComida = dialogView.findViewById<CheckBox>(R.id.cbComida)
        val cbTransporte = dialogView.findViewById<CheckBox>(R.id.cbTransporte)
        val cbEntretenimiento = dialogView.findViewById<CheckBox>(R.id.cbEntretenimiento)
        val cbSalud = dialogView.findViewById<CheckBox>(R.id.cbSalud)
        val cbEducacion = dialogView.findViewById<CheckBox>(R.id.cbEducacion)
        val cbServicios = dialogView.findViewById<CheckBox>(R.id.cbServicios)
        val cbCompras = dialogView.findViewById<CheckBox>(R.id.cbCompras)
        val cbVivienda = dialogView.findViewById<CheckBox>(R.id.cbVivienda)
        val cbOtros = dialogView.findViewById<CheckBox>(R.id.cbOtros)

        // Referencias a los radio buttons
        val rgPeriodo = dialogView.findViewById<RadioGroup>(R.id.rgPeriodo)
        val rbTodos = dialogView.findViewById<RadioButton>(R.id.rbTodos)
        val rbEsteMes = dialogView.findViewById<RadioButton>(R.id.rbEsteMes)
        val rbMesPasado = dialogView.findViewById<RadioButton>(R.id.rbMesPasado)
        val rbUltimos3Meses = dialogView.findViewById<RadioButton>(R.id.rbUltimos3Meses)

        // Cargar filtros actuales
        cbComida.isChecked = categoriasSeleccionadas.contains("Comida")
        cbTransporte.isChecked = categoriasSeleccionadas.contains("Transporte")
        cbEntretenimiento.isChecked = categoriasSeleccionadas.contains("Entretenimiento")
        cbSalud.isChecked = categoriasSeleccionadas.contains("Salud")
        cbEducacion.isChecked = categoriasSeleccionadas.contains("Educación")
        cbServicios.isChecked = categoriasSeleccionadas.contains("Servicios")
        cbCompras.isChecked = categoriasSeleccionadas.contains("Compras")
        cbVivienda.isChecked = categoriasSeleccionadas.contains("Vivienda")
        cbOtros.isChecked = categoriasSeleccionadas.contains("Otros")

        // Seleccionar período actual
        when (filtroPeriodo) {
            "estemes" -> rbEsteMes.isChecked = true
            "mespasado" -> rbMesPasado.isChecked = true
            "ultimos3meses" -> rbUltimos3Meses.isChecked = true
            else -> rbTodos.isChecked = true
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Botón Aplicar
        dialogView.findViewById<MaterialButton>(R.id.btnAplicarFiltros).setOnClickListener {
            // Guardar categorías seleccionadas
            categoriasSeleccionadas.clear()
            if (cbComida.isChecked) categoriasSeleccionadas.add("Comida")
            if (cbTransporte.isChecked) categoriasSeleccionadas.add("Transporte")
            if (cbEntretenimiento.isChecked) categoriasSeleccionadas.add("Entretenimiento")
            if (cbSalud.isChecked) categoriasSeleccionadas.add("Salud")
            if (cbEducacion.isChecked) categoriasSeleccionadas.add("Educación")
            if (cbServicios.isChecked) categoriasSeleccionadas.add("Servicios")
            if (cbCompras.isChecked) categoriasSeleccionadas.add("Compras")
            if (cbVivienda.isChecked) categoriasSeleccionadas.add("Vivienda")
            if (cbOtros.isChecked) categoriasSeleccionadas.add("Otros")

            // Guardar período seleccionado
            when (rgPeriodo.checkedRadioButtonId) {
                R.id.rbEsteMes -> {
                    filtroPeriodo = "estemes"
                    val calendar = Calendar.getInstance()
                    filtroMes = calendar.get(Calendar.MONTH) + 1
                    filtroAño = calendar.get(Calendar.YEAR)
                }
                R.id.rbMesPasado -> {
                    filtroPeriodo = "mespasado"
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, -1)
                    filtroMes = calendar.get(Calendar.MONTH) + 1
                    filtroAño = calendar.get(Calendar.YEAR)
                }
                R.id.rbUltimos3Meses -> {
                    filtroPeriodo = "ultimos3meses"
                    filtroMes = null
                    filtroAño = null
                }
                else -> {
                    filtroPeriodo = null
                    filtroMes = null
                    filtroAño = null
                }
            }

            aplicarFiltros()
            mostrarChipsFiltros()
            dialog.dismiss()
        }

        // Botón Limpiar
        dialogView.findViewById<MaterialButton>(R.id.btnLimpiarFiltros).setOnClickListener {
            cbComida.isChecked = false
            cbTransporte.isChecked = false
            cbEntretenimiento.isChecked = false
            cbSalud.isChecked = false
            cbEducacion.isChecked = false
            cbServicios.isChecked = false
            cbCompras.isChecked = false
            cbVivienda.isChecked = false
            cbOtros.isChecked = false
            rbTodos.isChecked = true
        }

        // Botón Cancelar
        dialogView.findViewById<MaterialButton>(R.id.btnCancelarFiltros).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun aplicarFiltros() {
        listaGastosFiltrados.clear()

        for (gasto in listaGastos) {
            var cumpleFiltro = true

            // Filtro por categorías (si hay categorías seleccionadas)
            if (categoriasSeleccionadas.isNotEmpty()) {
                if (!categoriasSeleccionadas.contains(gasto.categoria)) {
                    cumpleFiltro = false
                }
            }

            // Filtro por período
            if (filtroPeriodo == "ultimos3meses") {
                // Últimos 3 meses
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -3)
                val fechaLimite = calendar.timeInMillis

                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaGasto = dateFormat.parse(gasto.fecha)

                    if (fechaGasto == null || fechaGasto.time < fechaLimite) {
                        cumpleFiltro = false
                    }
                } catch (e: Exception) {
                    cumpleFiltro = false
                }
            } else if (filtroMes != null && filtroAño != null) {
                // Mes específico
                try {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaGasto = dateFormat.parse(gasto.fecha)

                    if (fechaGasto != null) {
                        val calGasto = Calendar.getInstance()
                        calGasto.time = fechaGasto

                        val mesGasto = calGasto.get(Calendar.MONTH) + 1
                        val añoGasto = calGasto.get(Calendar.YEAR)

                        if (mesGasto != filtroMes || añoGasto != filtroAño) {
                            cumpleFiltro = false
                        }
                    } else {
                        cumpleFiltro = false
                    }
                } catch (e: Exception) {
                    cumpleFiltro = false
                }
            }

            if (cumpleFiltro) {
                listaGastosFiltrados.add(gasto)
            }
        }

        // Actualizar UI
        if (listaGastosFiltrados.isEmpty()) {
            mostrarVacio()
        } else {
            layoutEmpty.visibility = View.GONE
            rvGastos.visibility = View.VISIBLE

            gastoAdapter.actualizarLista(listaGastosFiltrados)
            actualizarContadorFiltrado()
            calcularTotalFiltrado()
        }
    }

    private fun mostrarChipsFiltros() {
        chipGroupFiltros.removeAllViews()

        val tieneCategoria = categoriasSeleccionadas.isNotEmpty()
        val tienePeriodo = filtroPeriodo != null

        if (!tieneCategoria && !tienePeriodo) {
            scrollChipsFiltros.visibility = View.GONE
            return
        }

        scrollChipsFiltros.visibility = View.VISIBLE

        // Agregar chips de categorías
        for (categoria in categoriasSeleccionadas) {
            val chip = Chip(this)
            chip.text = categoria
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                categoriasSeleccionadas.remove(categoria)
                aplicarFiltros()
                mostrarChipsFiltros()
            }
            chipGroupFiltros.addView(chip)
        }

        // Agregar chip de período
        if (tienePeriodo) {
            val chip = Chip(this)
            chip.text = when (filtroPeriodo) {
                "estemes" -> "Este mes"
                "mespasado" -> "Mes pasado"
                "ultimos3meses" -> "Últimos 3 meses"
                else -> ""
            }
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                filtroPeriodo = null
                filtroMes = null
                filtroAño = null
                aplicarFiltros()
                mostrarChipsFiltros()
            }
            chipGroupFiltros.addView(chip)
        }

        // Agregar botón "Limpiar todo"
        val chipLimpiar = Chip(this)
        chipLimpiar.text = "Limpiar todo"
        chipLimpiar.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor("#f44336")
        )
        chipLimpiar.setTextColor(android.graphics.Color.WHITE)
        chipLimpiar.setOnClickListener {
            limpiarFiltros()
        }
        chipGroupFiltros.addView(chipLimpiar)
    }

    private fun limpiarFiltros() {
        categoriasSeleccionadas.clear()
        filtroMes = null
        filtroAño = null
        filtroPeriodo = null
        listaGastosFiltrados.clear()

        if (listaGastos.isEmpty()) {
            mostrarVacio()
        } else {
            layoutEmpty.visibility = View.GONE
            rvGastos.visibility = View.VISIBLE

            gastoAdapter.actualizarLista(listaGastos)
            actualizarContador()
            calcularTotalMes()
        }

        mostrarChipsFiltros()
        Toast.makeText(this, "Filtros eliminados", Toast.LENGTH_SHORT).show()
    }

    private fun actualizarContadorFiltrado() {
        val cantidad = listaGastosFiltrados.size
        tvCantidadGastos.text = if (cantidad == 1) {
            "1 gasto filtrado"
        } else {
            "$cantidad gastos filtrados"
        }
    }

    private fun calcularTotalFiltrado() {
        var total = 0.0
        for (gasto in listaGastosFiltrados) {
            total += gasto.monto
        }
        tvTotalMes.text = "$${String.format("%.2f", total)}"
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                irALogin()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}