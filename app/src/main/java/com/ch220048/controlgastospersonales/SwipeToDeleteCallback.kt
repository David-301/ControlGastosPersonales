package com.ch220048.controlgastospersonales

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeToDeleteCallback(context: Context) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {

    private val deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete)
    private val intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
    private val intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336") // Rojo Material Design
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    // Paint para el texto
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val deleteText = "ELIMINAR"

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Dibujar fondo rojo
        background.color = backgroundColor

        if (dX > 0) {
            // Swipe hacia la derecha
            background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
            background.draw(c)

            // Calcular posición del ícono
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconLeft = itemView.left + deleteIconMargin
            val deleteIconRight = itemView.left + deleteIconMargin + intrinsicWidth
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            // Dibujar ícono
            deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon?.setTint(Color.WHITE)
            deleteIcon?.draw(c)

            // Dibujar texto "ELIMINAR" a la derecha del ícono
            val textX = deleteIconRight.toFloat() + 24f
            val textY = itemView.top + (itemHeight / 2f) + (textPaint.textSize / 3f)
            c.drawText(deleteText, textX, textY, textPaint)

        } else {
            // Swipe hacia la izquierda
            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
            background.draw(c)

            // Calcular posición del ícono (al lado derecho)
            val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
            val deleteIconRight = itemView.right - deleteIconMargin
            val deleteIconLeft = deleteIconRight - intrinsicWidth
            val deleteIconBottom = deleteIconTop + intrinsicHeight

            // Dibujar ícono
            deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
            deleteIcon?.setTint(Color.WHITE)
            deleteIcon?.draw(c)

            // Dibujar texto "ELIMINAR" a la izquierda del ícono
            val textWidth = textPaint.measureText(deleteText)
            val textX = deleteIconLeft - textWidth - 24f
            val textY = itemView.top + (itemHeight / 2f) + (textPaint.textSize / 3f)
            c.drawText(deleteText, textX, textY, textPaint)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        c?.drawRect(left, top, right, bottom, clearPaint)
    }
}