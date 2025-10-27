package com.ch220048.controlgastospersonales

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AgregarGastoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etNombreGasto: TextInputEditText
    private lateinit var etMonto: TextInputEditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var tvFecha: TextView
    private lateinit var btnSeleccionarFecha: MaterialButton
    private lateinit var etNotas: TextInputEditText
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: MaterialButton

    private var fechaSeleccionada: String = ""
    private var categoriaSeleccionada: String = ""

    // Variables para modo edición
    private var modoEdicion = false
    private var gastoId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_gasto)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        inicializarVistas()
        configurarCategoria()
        configurarFecha()
        verificarModoEdicion()
        configurarBotones()
    }

    private fun inicializarVistas() {
        etNombreGasto = findViewById(R.id.etNombreGasto)
        etMonto = findViewById(R.id.etMonto)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        tvFecha = findViewById(R.id.tvFecha)
        btnSeleccionarFecha = findViewById(R.id.btnSeleccionarFecha)
        etNotas = findViewById(R.id.etNotas)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun configurarCategoria() {
        val categorias = arrayOf(
            "Selecciona una categoría",
            "Comida",
            "Transporte",
            "Entretenimiento",
            "Salud",
            "Educación",
            "Servicios",
            "Compras",
            "Vivienda",
            "Otros"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter

        spinnerCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    categoriaSeleccionada = categorias[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun configurarFecha() {
        // Establecer fecha actual por defecto
        val calendar = Calendar.getInstance()
        fechaSeleccionada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        tvFecha.text = fechaSeleccionada

        btnSeleccionarFecha.setOnClickListener {
            mostrarDatePicker()
        }
    }

    private fun verificarModoEdicion() {
        // Verificar si se recibieron datos para editar
        modoEdicion = intent.getBooleanExtra("MODO_EDICION", false)

        if (modoEdicion) {
            // Cambiar título
            title = "Editar Gasto"
            btnGuardar.text = "Actualizar"

            // Cargar datos existentes
            gastoId = intent.getStringExtra("GASTO_ID") ?: ""
            etNombreGasto.setText(intent.getStringExtra("GASTO_NOMBRE"))
            etMonto.setText(intent.getDoubleExtra("GASTO_MONTO", 0.0).toString())

            val categoria = intent.getStringExtra("GASTO_CATEGORIA") ?: ""
            val categorias = resources.getStringArray(android.R.array.emailAddressTypes)
            // Buscar la posición de la categoría en el spinner
            val categoriasArray = arrayOf(
                "Selecciona una categoría",
                "Comida", "Transporte", "Entretenimiento", "Salud",
                "Educación", "Servicios", "Compras", "Vivienda", "Otros"
            )
            val posicion = categoriasArray.indexOf(categoria)
            if (posicion >= 0) {
                spinnerCategoria.setSelection(posicion)
            }

            fechaSeleccionada = intent.getStringExtra("GASTO_FECHA") ?: ""
            tvFecha.text = fechaSeleccionada
            etNotas.setText(intent.getStringExtra("GASTO_NOTAS"))
        } else {
            title = "Agregar Gasto"
            btnGuardar.text = "Guardar"
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()

        // Si ya hay una fecha seleccionada, usarla como base
        if (fechaSeleccionada.isNotEmpty()) {
            try {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fecha = dateFormat.parse(fechaSeleccionada)
                if (fecha != null) {
                    calendar.time = fecha
                }
            } catch (e: Exception) {
                // Usar fecha actual si hay error
            }
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                fechaSeleccionada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                tvFecha.text = fechaSeleccionada
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun configurarBotones() {
        btnGuardar.setOnClickListener {
            if (validarCampos()) {
                if (modoEdicion) {
                    actualizarGasto()
                } else {
                    guardarGasto()
                }
            }
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun validarCampos(): Boolean {
        val nombre = etNombreGasto.text.toString().trim()
        val montoStr = etMonto.text.toString().trim()

        if (nombre.isEmpty()) {
            etNombreGasto.error = "Ingresa un nombre"
            etNombreGasto.requestFocus()
            return false
        }

        if (montoStr.isEmpty()) {
            etMonto.error = "Ingresa un monto"
            etMonto.requestFocus()
            return false
        }

        val monto = montoStr.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            etMonto.error = "Ingresa un monto válido"
            etMonto.requestFocus()
            return false
        }

        if (categoriaSeleccionada.isEmpty() || categoriaSeleccionada == "Selecciona una categoría") {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
            return false
        }

        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun guardarGasto() {
        val nombre = etNombreGasto.text.toString().trim()
        val monto = etMonto.text.toString().toDouble()
        val notas = etNotas.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        val gasto = hashMapOf(
            "nombre" to nombre,
            "monto" to monto,
            "categoria" to categoriaSeleccionada,
            "fecha" to fechaSeleccionada,
            "notas" to notas,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        btnGuardar.isEnabled = false
        btnGuardar.text = "Guardando..."

        db.collection("gastos")
            .add(gasto)
            .addOnSuccessListener {
                Toast.makeText(this, "Gasto guardado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnGuardar.isEnabled = true
                btnGuardar.text = "Guardar"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarGasto() {
        val nombre = etNombreGasto.text.toString().trim()
        val monto = etMonto.text.toString().toDouble()
        val notas = etNotas.text.toString().trim()

        val gastoActualizado = hashMapOf(
            "nombre" to nombre,
            "monto" to monto,
            "categoria" to categoriaSeleccionada,
            "fecha" to fechaSeleccionada,
            "notas" to notas
        )

        btnGuardar.isEnabled = false
        btnGuardar.text = "Actualizando..."

        db.collection("gastos")
            .document(gastoId)
            .update(gastoActualizado as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Gasto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                btnGuardar.isEnabled = true
                btnGuardar.text = "Actualizar"
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}