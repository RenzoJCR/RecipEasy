package com.recipeasy.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.recipeasy.activities.RecipeDetailActivity
import com.recipeasy.adapters.RecipeAdapter
import com.recipeasy.database.DatabaseHelper
import com.recipeasy.interfaces.OnRecipeClickListener
import com.recipeasy.interfaces.SearchableFragment
import com.recipeasy.models.Receta
import com.recipeasy.R
import com.recipeasy.utils.PdfGenerator
import com.recipeasy.utils.PermissionHelper
import com.recipeasy.utils.SharedPreferencesHelper
import java.io.File
import androidx.appcompat.app.AlertDialog
import android.content.pm.PackageManager
class FavoritosFragment : Fragment(), OnRecipeClickListener, SearchableFragment {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var fabExportPdf: FloatingActionButton

    private val dbHelper: DatabaseHelper? by lazy {
        if (isAdded && context != null) {
            DatabaseHelper(requireContext())
        } else {
            null
        }
    }

    private val prefs: SharedPreferencesHelper? by lazy {
        if (isAdded && context != null) {
            SharedPreferencesHelper(requireContext())
        } else {
            null
        }
    }

    private var recipeList: List<Receta> = listOf()
    private var filteredList: List<Receta> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favoritos, container, false)
        initViews(view)
        loadFavorites()
        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewFavoritos)
        fabExportPdf = view.findViewById(R.id.fabExportPdf)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recipeAdapter = RecipeAdapter(filteredList, this)
        recyclerView.adapter = recipeAdapter

        // Configurar botón de exportar PDF
        fabExportPdf.setOnClickListener {
            exportFavoritesToPdf()
        }
    }

    private fun exportFavoritesToPdf() {
        if (recipeList.isEmpty()) {
            Toast.makeText(requireContext(), "No tienes recetas favoritas para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        // Para Android 10+ (API 29+) ya no se necesita pedir permiso para guardar en Downloads
        // Solo necesitamos verificar para Android 9 o inferior
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // Android 9 o inferior - verificar permiso
            if (!PermissionHelper.checkStoragePermission(requireActivity())) {
                if (PermissionHelper.shouldShowRequestPermissionRationale(requireActivity())) {
                    // Mostrar explicación
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permiso necesario")
                        .setMessage("Necesitamos permiso de almacenamiento para guardar el PDF de tus recetas favoritas.")
                        .setPositiveButton("OK") { _, _ ->
                            PermissionHelper.requestStoragePermission(requireActivity())
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                } else {
                    PermissionHelper.requestStoragePermission(requireActivity())
                }
                return
            }
        }

        // Si tenemos permiso (o estamos en Android 10+), generar PDF
        generatePdfNow()
    }

    private fun generatePdfNow() {
        // Mostrar mensaje de progreso
        Toast.makeText(requireContext(), "Generando PDF...", Toast.LENGTH_SHORT).show()

        // Deshabilitar botón mientras se genera
        fabExportPdf.isEnabled = false

        // Generar PDF en segundo plano
        Thread {
            try {
                val pdfGenerator = PdfGenerator(requireContext())
                val pdfFile = pdfGenerator.generateFavoritesPdf(recipeList)

                requireActivity().runOnUiThread {
                    fabExportPdf.isEnabled = true

                    if (pdfFile != null && pdfFile.exists()) {
                        sharePdfFile(pdfFile)
                        Toast.makeText(
                            requireContext(),
                            "PDF guardado en: Downloads/RecipEasy/",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(requireContext(), "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    fabExportPdf.isEnabled = true
                    Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun sharePdfFile(pdfFile: File) {
        try {
            // PRIMERO: Mostrar mensaje con ubicación del archivo
            val filePath = pdfFile.absolutePath
            val shortPath = filePath.substringAfterLast("/Android/")

            // SEGUNDO: Intentar abrir el archivo localmente
            try {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    pdfFile
                )

                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Verificar si hay app para ver PDFs
                val packageManager = requireContext().packageManager
                if (viewIntent.resolveActivity(packageManager) != null) {
                    startActivity(viewIntent)
                } else {
                    // Si no hay app para PDFs, ofrecer compartir
                    offerShareOption(pdfFile, uri)
                }

            } catch (e: Exception) {
                // Si falla, solo mostrar ubicación
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "PDF guardado en: Downloads/RecipEasy/",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun offerShareOption(pdfFile: File, uri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("PDF Guardado")
            .setMessage("El PDF se ha guardado exitosamente.\n\n¿Deseas compartirlo?")
            .setPositiveButton("Sí, compartir") { _, _ ->
                tryShareWithFallback(pdfFile, uri)
            }
            .setNegativeButton("No, gracias") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Ver ubicación") { _, _ ->
                showFileLocation(pdfFile)
            }
            .show()
    }

    private fun tryShareWithFallback(pdfFile: File, uri: Uri) {
        try {
            // Intentar compartir con FileProvider
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Mis Recetas Favoritas - RecipEasy")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))

        } catch (e: Exception) {
            // Fallback: compartir con URI directa (solo para apps que lo soporten)
            try {
                val fallbackUri = Uri.fromFile(pdfFile)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, fallbackUri)
                }
                startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
            } catch (e2: Exception) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo compartir. El PDF está guardado localmente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showFileLocation(pdfFile: File) {
        val location = "📂 ${pdfFile.parent}\n📄 ${pdfFile.name}\n📊 ${pdfFile.length() / 1024} KB"

        AlertDialog.Builder(requireContext())
            .setTitle("Ubicación del PDF")
            .setMessage(location)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadFavorites() {
        dbHelper?.let { helper ->
            prefs?.let { preferences ->
                val usuarioId = preferences.getUserId()
                if (usuarioId != -1) {
                    recipeList = helper.getFavorites(usuarioId)
                    filteredList = recipeList
                    recipeAdapter.updateRecipes(filteredList)

                    // Mostrar/ocultar botón de exportar según si hay favoritos
                    fabExportPdf.visibility = if (recipeList.isEmpty()) View.GONE else View.VISIBLE
                } else {
                    fabExportPdf.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    override fun onRecipeClick(receta: Receta) {
        val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
        intent.putExtra("RECETA", receta)
        startActivity(intent)
    }

    override fun onSearchQuery(query: String) {
        dbHelper?.let { helper ->
            prefs?.let { preferences ->
                if (query.isEmpty()) {
                    filteredList = recipeList
                } else {
                    val globalResults = helper.searchRecipesGlobal(query)
                    val usuarioId = preferences.getUserId()
                    if (usuarioId != -1) {
                        val favoriteIds = helper.getFavorites(usuarioId).map { it.id }
                        filteredList = globalResults.filter { it.id in favoriteIds }
                    } else {
                        filteredList = emptyList()
                    }
                }
                recipeAdapter.updateRecipes(filteredList)
            }
        }
    }
}