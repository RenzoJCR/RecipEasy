package com.recipeasy.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.recipeasy.models.Receta
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {

    companion object {
        private const val FOLDER_NAME = "RecipEasy"
        private const val FILE_PREFIX = "Recetas_Favoritas_"
    }

    fun generateFavoritesPdf(recipes: List<Receta>): File? {
        if (recipes.isEmpty()) return null

        try {
            // Crear nombre de archivo
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val userName = "Usuario" // Podrías obtener el nombre del usuario logueado
            val fileName = "Recetas_Favoritas_${userName}_$timestamp.pdf"

            // Crear directorio
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDir = File(downloadsDir, FOLDER_NAME)
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            // Crear archivo PDF
            val pdfFile = File(appDir, fileName)
            val outputStream = FileOutputStream(pdfFile)
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)

            // Márgenes más pequeños para más contenido
            document.setMargins(30f, 30f, 30f, 30f)

            // CABECERA
            val header = Paragraph("Mis Recetas Favoritas")
                .setFontSize(24f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5f)

            document.add(header)

            val subtitle = Paragraph("Recetario Personal - RecipEasy")
                .setFontSize(14f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(10f)

            document.add(subtitle)

            // INFORMACIÓN DE GENERACIÓN
            val dateInfo = Paragraph("Generado el: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())} | " +
                    "Total: ${recipes.size} recetas")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(20f)

            document.add(dateInfo)

            // LÍNEA SEPARADORA
            document.add(createSeparatorLine())
            document.add(Paragraph().setMarginBottom(15f))

            // AGREGAR CADA RECETA SEGUIDA
            recipes.forEachIndexed { index, receta ->
                addRecipeToDocument(document, receta, index + 1)

                // Solo agregar separador si no es la última receta
                if (index < recipes.size - 1) {
                    document.add(Paragraph().setMarginBottom(10f)) // Espacio pequeño
                    document.add(createSeparatorLine())
                    document.add(Paragraph().setMarginBottom(15f))
                }
            }

            // PIE DE PÁGINA
            document.add(Paragraph().setMarginBottom(20f))
            val footer = Paragraph("© RecipEasy - Tu app de recetas para principiantes")
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY)

            document.add(footer)

            document.close()
            return pdfFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun addRecipeToDocument(document: Document, receta: Receta, number: Int) {
        try {
            // TÍTULO DE RECETA (con número)
            val recipeTitle = Paragraph("$number. ${receta.nombre}")
                .setFontSize(16f)
                .setBold()
                .setMarginBottom(5f)

            document.add(recipeTitle)

            // INFORMACIÓN BÁSICA EN UNA LÍNEA
            val metaInfo = Paragraph("📅 ${receta.categoria.replaceFirstChar { it.uppercase() }} | " +
                    "⚡ ${receta.dificultad} | " +
                    "⏱️ ${receta.tiempoPreparacion} min | " +
                    "👥 ${receta.porciones} porciones")
                .setFontSize(11f)
                .setMarginBottom(10f)

            document.add(metaInfo)

            // DESCRIPCIÓN (si existe)
            if (receta.descripcion.isNotBlank()) {
                val description = Paragraph(receta.descripcion)
                    .setFontSize(11f)
                    .setItalic()
                    .setMarginBottom(10f)

                document.add(description)
            }

            // INGREDIENTES (en dos columnas si son muchos)
            val ingredientsTitle = Paragraph("📋 Ingredientes:")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f)

            document.add(ingredientsTitle)

            receta.ingredientes.forEach { ingrediente ->
                val ingredientItem = Paragraph("• $ingrediente")
                    .setFontSize(11f)
                    .setMarginLeft(10f)
                    .setMarginBottom(2f)

                document.add(ingredientItem)
            }

            document.add(Paragraph().setMarginBottom(8f))

            // PASOS DE PREPARACIÓN
            val stepsTitle = Paragraph("👩‍🍳 Preparación:")
                .setFontSize(12f)
                .setBold()
                .setMarginBottom(5f)

            document.add(stepsTitle)

            receta.pasos.forEachIndexed { stepIndex, paso ->
                val stepItem = Paragraph("${stepIndex + 1}. $paso")
                    .setFontSize(11f)
                    .setMarginLeft(10f)
                    .setMarginBottom(6f)

                document.add(stepItem)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createSeparatorLine(): Paragraph {
        return Paragraph("─".repeat(60))
            .setFontSize(10f)
            .setFontColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
    }

    fun getPdfDirectoryPath(): String {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, FOLDER_NAME)
        return appDir.absolutePath
    }
}