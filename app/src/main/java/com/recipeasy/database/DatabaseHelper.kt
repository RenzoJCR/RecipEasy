package com.recipeasy.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.recipeasy.models.Receta
import com.recipeasy.models.Usuario
import com.recipeasy.utils.Constants

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RecipEasy.db"
        private const val DATABASE_VERSION = 5

        // Tabla Usuarios
        const val TABLE_USERS = "usuarios"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_NAME = "nombre"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"

        // Tabla Recetas
        const val TABLE_RECIPES = "recetas"
        const val COLUMN_RECIPE_ID = "id"
        const val COLUMN_RECIPE_NAME = "nombre"
        const val COLUMN_RECIPE_DESCRIPTION = "descripcion"
        const val COLUMN_RECIPE_IMAGE = "imagen"

        const val COLUMN_RECIPE_VIDEO_URL = "video_url"
        const val COLUMN_RECIPE_TIME = "tiempo_preparacion"
        const val COLUMN_RECIPE_PORTIONS = "porciones"
        const val COLUMN_RECIPE_DIFFICULTY = "dificultad"
        const val COLUMN_RECIPE_INGREDIENTS = "ingredientes"
        const val COLUMN_RECIPE_STEPS = "pasos"
        const val COLUMN_RECIPE_CATEGORY = "categoria"

        // Tabla Favoritos
        const val TABLE_FAVORITES = "favoritos"
        const val COLUMN_FAVORITE_ID = "id"
        const val COLUMN_FAV_USER_ID = "usuario_id"
        const val COLUMN_FAV_RECIPE_ID = "receta_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de usuarios
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUserTable)

        // Crear tabla de recetas
        val createRecipeTable = """
            CREATE TABLE $TABLE_RECIPES (
                $COLUMN_RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RECIPE_NAME TEXT NOT NULL,
                $COLUMN_RECIPE_DESCRIPTION TEXT,
                $COLUMN_RECIPE_IMAGE TEXT,
                $COLUMN_RECIPE_VIDEO_URL TEXT,
                $COLUMN_RECIPE_TIME INTEGER,
                $COLUMN_RECIPE_PORTIONS INTEGER,
                $COLUMN_RECIPE_DIFFICULTY TEXT,
                $COLUMN_RECIPE_INGREDIENTS TEXT,
                $COLUMN_RECIPE_STEPS TEXT,
                $COLUMN_RECIPE_CATEGORY TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createRecipeTable)

        // Crear tabla de favoritos
        val createFavoritesTable = """
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FAV_USER_ID INTEGER,
                $COLUMN_FAV_RECIPE_ID INTEGER,
                FOREIGN KEY($COLUMN_FAV_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
                FOREIGN KEY($COLUMN_FAV_RECIPE_ID) REFERENCES $TABLE_RECIPES($COLUMN_RECIPE_ID),
                UNIQUE($COLUMN_FAV_USER_ID, $COLUMN_FAV_RECIPE_ID)
            )
        """.trimIndent()
        db.execSQL(createFavoritesTable)

        // Insertar datos de ejemplo
        insertSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL(
                "ALTER TABLE $TABLE_RECIPES ADD COLUMN $COLUMN_RECIPE_VIDEO_URL TEXT"
            )
        }
    }

    fun backfillVideoUrls() {
        val db = writableDatabase

        val urlByImage = mapOf(
            // DESAYUNOS
            "breakfast_tamal_verde" to "https://youtu.be/qMgEv-1mf4c?si=zq8F3rE1RAlhlWTY",
            "breakfast_pan_chicharron" to "https://youtu.be/G8xM27hT2q8?si=ULT59h2tBle6wtJA",
            "breakfast_humita" to "https://youtu.be/t6ZGbT-Dskk?si=wj2JhkGQgCKg3Wkb",
            "breakfast_caldo_gallina" to "https://youtu.be/qG-Lh46DHqE?si=_19lO8DIezLmnKty",
            "breakfast_butifarra" to "https://youtu.be/22w5ToaTSDQ?si=v2Wrv8v58yv9RnCA",
            "breakfast_chicharron_pescado" to "https://youtu.be/S825H-ksTvU?si=QX6rAg9V7W1QxpzD",
            "breakfast_revuelto_rocoto" to "https://youtu.be/WjCgbqgoCrc?si=oGT6o2rH0dn1RX5j",

            // ALMUERZOS
            "lunch_lomo_saltado" to "https://youtu.be/sWXRJbGi6yQ?si=m_CKPTwOGrTwDwVg",
            "lunch_aji_gallina" to "https://youtu.be/UdnT9ka7yAk?si=EPtto3C02UBzRa7Y",
            "lunch_causa_limena" to "https://youtu.be/gtCIqYUCekU?si=q1U8tie65CBKPhuq",
            "lunch_papa_huancaina" to "https://youtu.be/IjWgPVBCHXU?si=JoaIyjbkCWZeJ30L",
            "lunch_arroz_pollo" to "https://youtu.be/Lk8OV9GMdXY?si=CCS4iLDgOrlqwSHY",
            "lunch_seco_cordero" to "https://youtu.be/ik4MHm7ahRA?si=eUeWJ6lprkm9fiQG",
            "lunch_ceviche_mixto" to "https://youtu.be/CuuFn81HJYk?si=h1QnhM4lpF1-56z5",

            // CENAS
            "dinner_tacu_tacu" to "https://youtu.be/bH0VjyvgQjc?si=_T6Urv_9n2gpe2gO",
            "dinner_arroz_chaufa" to "https://youtu.be/M_r2lIuQ3qI?si=KZQfaoZdAtT6Db95",
            "dinner_pollo_brasa" to "https://youtu.be/YpEXS20-SX4?si=6n8-Hyo7B4R6UGrA",
            "dinner_anticuchos" to "https://youtu.be/MTVagyVam_o?si=7tiLaP-VNG3_99zA",
            "dinner_parihuela" to "https://youtu.be/L99qG4HMTxk?si=75DBjgBVUSJUa2ct",
            "dinner_carapulcra" to "https://youtu.be/no5l20i8WnM?si=PXEA1ar-2bV0Y6tN",
            "dinner_chaufa_mariscos" to "https://youtu.be/hquYb706444?si=M5iR9OW-AAdV2o-F",

            // POSTRES
            "dessert_mazamorra_morada" to "https://youtu.be/0iFwT8mr_a4?si=JdqyzKQ3Yj_eby53",
            "dessert_arroz_leche" to "https://youtu.be/hMY_bn6jTS8?si=C3HjOZmeIvn0GQ70",
            "dessert_picarones" to "https://youtu.be/ZjnUl4UzApg?si=nd_yy-jsmOX9rar6",
            "dessert_suspiro_limena" to "https://youtu.be/pdI3hr58U50?si=EbZzqgn6hqMODrxJ",
            "dessert_turron_pepa" to "https://youtu.be/TElTX-lW0pM?si=W3HAFUG3kfqaUjHj",
            "dessert_crema_volteada" to "https://youtu.be/ikRLowcQqTA?si=pzbQxMeF02kG1QPZ",
            "dessert_ranfanote" to "https://youtu.be/gswqr5KGM0o?si=8XRP6Wiu0mgo8Axf"
        )

        db.beginTransaction()
        try {
            urlByImage.forEach { (imageKey, url) ->
                db.execSQL(
                    "UPDATE $TABLE_RECIPES SET $COLUMN_RECIPE_VIDEO_URL = ? WHERE $COLUMN_RECIPE_IMAGE = ?",
                    arrayOf(url, imageKey)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }


    private fun insertSampleData(db: SQLiteDatabase) {
        // LIMPIAR RECETAS EXISTENTES PRIMERO
        db.delete(TABLE_RECIPES, null, null)
        db.delete(TABLE_FAVORITES, null, null)

        // ==================== DESAYUNOS PERUANOS ====================
        insertRecipe(db, Receta(
            nombre = "Tamal Verde",
            descripcion = "Tamal relleno de cerdo con salsa verde, típico del desayuno limeño",
            imagen = "breakfast_tamal_verde",
            videoUrl = "https://youtu.be/qMgEv-1mf4c?si=zq8F3rE1RAlhlWTY",
            tiempoPreparacion = 180,
            porciones = 6,
            dificultad = "Medio",
            ingredientes = listOf(
                "2 tazas de maíz molido",
                "1 kg de pierna de cerdo",
                "1/2 taza de manteca de cerdo",
                "1 cebolla grande picada",
                "4 dientes de ajo picados",
                "1/2 taza de culantro molido",
                "2 cucharadas de ají verde molido",
                "1/2 taza de maní tostado molido",
                "Hojas de plátano para envolver",
                "Sal y pimienta al gusto"
            ),
            pasos = listOf(
                "Cocinar la pierna de cerdo en agua con sal hasta que esté tierna, luego desmenuzar",
                "En una sartén, dorar la cebolla y el ajo en la manteca",
                "Agregar el culantro, ají verde y maní. Cocinar por 5 minutos",
                "Mezclar el maíz molido con 4 tazas de caldo del cerdo y la salsa verde",
                "Batir la masa hasta que esté suave y cremosa",
                "Extender las hojas de plátano, poner una porción de masa y relleno de cerdo",
                "Envolver bien los tamales y cocinar a vapor por 1.5 horas"
            ),
            categoria = Constants.CATEGORY_DESAYUNO
        ))

        insertRecipe(db, Receta(
            nombre = "Pan con Chicharrón",
            descripcion = "Clásico sandwich limeño con chicharrón de cerdo, camote y salsa criolla",
            imagen = "breakfast_pan_chicharron",
            videoUrl = "https://youtu.be/G8xM27hT2q8?si=ULT59h2tBle6wtJA",
            tiempoPreparacion = 90,
            porciones = 4,
            dificultad = "Medio",
            ingredientes = listOf(
                "8 panes franceses",
                "1 kg de panceta de cerdo con piel",
                "2 camotes medianos",
                "2 cebollas rojas",
                "2 limones",
                "1 ají limo picado",
                "1/4 taza de culantro picado",
                "Sal y pimienta al gusto",
                "Aceite vegetal para freír"
            ),
            pasos = listOf(
                "Cortar la panceta en trozos y sazonar con sal",
                "Freír el cerdo en su propia grasa a fuego medio hasta que esté crocante",
                "Hervir los camotes hasta que estén tiernos, luego pelar y cortar en rodajas",
                "Preparar la salsa criolla: cortar cebolla en pluma, mezclar con ají limo, culantro y jugo de limón",
                "Calentar los panes ligeramente",
                "Armar los sandwiches: pan, chicharrón, camote y salsa criolla"
            ),
            categoria = Constants.CATEGORY_DESAYUNO
        ))

        insertRecipe(db, Receta(
            nombre = "Humita",
            descripcion = "Pasta de maíz fresco envuelta en hojas de choclo y cocida al vapor",
            imagen = "breakfast_humita",
            videoUrl = "https://youtu.be/t6ZGbT-Dskk?si=wj2JhkGQgCKg3Wkb",
            tiempoPreparacion = 60,
            porciones = 8,
            dificultad = "Fácil",
            ingredientes = listOf(
                "12 choclos frescos",
                "1 cebolla picada finamente",
                "2 dientes de ajo picados",
                "1/4 taza de aceite vegetal",
                "1/2 taza de queso fresco desmenuzado",
                "1/4 taza de leche evaporada",
                "2 cucharadas de azúcar",
                "Hojas de choclo para envolver",
                "Sal y pimienta al gusto"
            ),
            pasos = listOf(
                "Rallar los choclos y reservar la pasta",
                "En una sartén, sofreír la cebolla y ajo en aceite hasta que estén transparentes",
                "Mezclar el sofrito con la pasta de choclo",
                "Agregar el queso, leche, azúcar, sal y pimienta",
                "Mezclar bien hasta obtener una masa homogénea",
                "Lavar las hojas de choclo y formar paquetes con la masa",
                "Cocinar al vapor por 45 minutos hasta que estén firmes"
            ),
            categoria = Constants.CATEGORY_DESAYUNO
        ))

        insertRecipe(db, Receta(
            nombre = "Caldo de Gallina",
            descripcion = "Reconfortante sopa de gallina con fideos y hierbas aromáticas",
            imagen = "breakfast_caldo_gallina",
            videoUrl = "https://youtu.be/qG-Lh46DHqE?si=_19lO8DIezLmnKty",
            tiempoPreparacion = 120,
            porciones = 6,
            dificultad = "Fácil",
            ingredientes = listOf(
                "1 gallina de corral cortada en presas",
                "2 litros de agua",
                "1 cebolla grande",
                "4 dientes de ajo",
                "2 ramas de apio",
                "2 zanahorias",
                "1 taza de fideos cabello de ángel",
                "1/4 taza de culantro picado",
                "2 huevos duros (opcional)",
                "Sal, pimienta y comino al gusto"
            ),
            pasos = listOf(
                "En una olla grande, dorar las presas de gallina",
                "Agregar agua, cebolla entera, ajos, apio y zanahorias",
                "Cocinar a fuego lento por 1.5 horas hasta que la gallina esté tierna",
                "Retirar las verduras y desmenuzar la gallina",
                "Agregar los fideos y cocinar por 10 minutos",
                "Rectificar la sazón con sal, pimienta y comino",
                "Servir caliente con culantro fresco y huevo duro"
            ),
            categoria = Constants.CATEGORY_DESAYUNO
        ))

        insertRecipe(db, Receta(
            nombre = "Butifarra",
            descripcion = "Sandwich peruano de jamón del país con salsa criolla",
            imagen = "breakfast_butifarra",
            videoUrl = "https://youtu.be/22w5ToaTSDQ?si=v2Wrv8v58yv9RnCA",
            tiempoPreparacion = 30,
            porciones = 4,
            dificultad = "Fácil",
            ingredientes = listOf(
                "4 panes franceses",
                "400g de jamón del país en lonjas",
                "1 cebolla roja grande",
                "2 limones",
                "1 ají limo picado finamente",
                "1/4 taza de culantro picado",
                "Lechuga fresca",
                "Mayonesa al gusto",
                "Sal y pimienta al gusto"
            ),
            pasos = listOf(
                "Preparar la salsa criolla: cortar cebolla en plumas delgadas",
                "Mezclar cebolla con ají limo, culantro, jugo de limón, sal y pimienta",
                "Dejar marinar por 10 minutos",
                "Tostar ligeramente los panes",
                "Untar mayonesa en los panes",
                "Colocar lechuga, lonjas de jamón del país y abundante salsa criolla",
                "Servir inmediatamente"
            ),
            categoria = Constants.CATEGORY_DESAYUNO
        ))

        insertRecipe(db, Receta(
            nombre = "Chicharrón de Pescado",
            descripcion = "Filetes de pescado empanizados y fritos, acompañados de yuca y salsa criolla",
            imagen = "breakfast_chicharron_pescado",
            videoUrl = "https://youtu.be/S825H-ksTvU?si=QX6rAg9V7W1QxpzD",
            tiempoPreparacion = 45,
            porciones = 4,
            dificultad = "Medio",
            ingredientes = listOf(
                "4 filetes de pescado blanco (600g)",
                "1 taza de harina",
                "2 huevos batidos",
                "1 taza de pan molido",
                "500g de yuca fresca",
                "1 cebolla roja",
                "2 limones",
                "Aceite vegetal para freír",
                "Sal, pimienta y ají no moto al gusto"
            ),
            pasos = listOf(
                "Sazonar los filetes de pescado con sal, pimienta y ají no moto",
                "Pasar por harina, huevo batido y pan molido",
                "Freír en aceite caliente hasta dorar por ambos lados",
                "Pelar y cocinar la yuca en agua con sal hasta que esté tierna",
                "Preparar salsa criolla con cebolla en pluma, limón y sal",
                "Servir el pescado con yuca y salsa criolla"
            ),
            categoria = Constants.CATEGORY_DESAYUNO
        ))

        insertRecipe(db, Receta(
            nombre = "Revuelto de Rocoto",
            descripcion = "Huevos revueltos con rocoto, cebolla y tomate, picante y sabroso",
            imagen = "breakfast_revuelto_rocoto",
            videoUrl = "https://youtu.be/WjCgbqgoCrc?si=oGT6o2rH0dn1RX5j",
            tiempoPreparacion = 25,
            porciones = 3,
            dificultad = "Fácil",
            ingredientes = listOf(
                "6 huevos",
                "2 rocotos sin venas ni semillas",
                "1 cebolla roja picada",
                "1 tomate picado sin piel",
                "2 cucharadas de aceite vegetal",
                "1/4 taza de leche evaporada",
                "50g de queso fresco desmenuzado",
                "Sal y pimienta al gusto"
            ),
            pasos = listOf(
                "Cortar los rocotos en tiras delgadas (usar guantes)",
                "En una sartén, sofreír la cebolla y rocoto en aceite",
                "Agregar el tomate y cocinar hasta que esté blando",
                "Batir los huevos con leche, sal y pimienta",
                "Verter los huevos sobre el sofrito y revolver suavemente",
                "Cuando esté casi cocido, agregar el queso fresco",
                "Servir caliente con pan o arroz blanco"
            ),
            categoria = Constants.CATEGORY_DESAYUNO
        ))

        // ==================== ALMUERZOS PERUANOS ====================
        insertRecipe(db, Receta(
            nombre = "Lomo Saltado",
            descripcion = "Salteado de lomo fino con cebolla, tomate y papas fritas, fusión peruano-china",
            imagen = "lunch_lomo_saltado",
            videoUrl = "https://youtu.be/sWXRJbGi6yQ?si=m_CKPTwOGrTwDwVg",
            tiempoPreparacion = 40,
            porciones = 4,
            dificultad = "Medio",
            ingredientes = listOf(
                "600g de lomo fino en tiras",
                "2 cebollas rojas en gajos",
                "3 tomates en gajos",
                "2 ajíes amarillos sin venas",
                "4 papas amarillas",
                "1/4 taza de sillao (salsa de soya)",
                "2 cucharadas de vinagre rojo",
                "1/4 taza de caldo de carne",
                "2 cucharadas de aceite vegetal",
                "Culantro picado y arroz blanco para acompañar"
            ),
            pasos = listOf(
                "Cortar las papas en bastones y freír hasta dorar",
                "Sellar la carne en aceite muy caliente por 2 minutos",
                "Agregar cebolla y ají amarillo, saltear 2 minutos más",
                "Incorporar tomate, sillao, vinagre y caldo",
                "Cocinar a fuego alto por 3 minutos hasta reducir la salsa",
                "Agregar las papas fritas y mezclar suavemente",
                "Servir inmediatamente con arroz blanco y culantro fresco"
            ),
            categoria = Constants.CATEGORY_ALMUERZO
        ))

        insertRecipe(db, Receta(
            nombre = "Aji de Gallina",
            descripcion = "Pollo deshilachado en cremosa salsa de ají amarillo con nueces y queso",
            imagen = "lunch_aji_gallina",
            videoUrl = "https://youtu.be/UdnT9ka7yAk?si=EPtto3C02UBzRa7Y",
            tiempoPreparacion = 90,
            porciones = 6,
            dificultad = "Medio",
            ingredientes = listOf(
                "1 kg de pechuga de pollo",
                "8 ajíes amarillos secos",
                "1 cebolla picada",
                "4 dientes de ajo",
                "1/2 taza de nueces molidas",
                "1/2 taza de queso parmesano rallado",
                "4 rebanadas de pan blanco sin corteza",
                "1 taza de leche evaporada",
                "1/4 taza de aceite vegetal",
                "Arroz blanco, papas sancochadas y huevo duro para acompañar"
            ),
            pasos = listOf(
                "Cocinar el pollo en agua con sal hasta estar tierno, luego deshilachar",
                "Remojar los ajíes en agua caliente, quitar venas y semillas",
                "Licuar ajíes con cebolla, ajo, nueces, queso y pan remojado en leche",
                "En una olla, calentar aceite y freír la pasta de ají por 5 minutos",
                "Agregar el pollo deshilachado y cocinar 10 minutos",
                "Incorporar la leche restante y cocinar hasta espesar",
                "Servir con arroz, papas sancochadas y huevo duro"
            ),
            categoria = Constants.CATEGORY_ALMUERZO
        ))

        insertRecipe(db, Receta(
            nombre = "Causa Limeña",
            descripcion = "Pastel frío de papa amarilla con relleno de pollo o atún",
            imagen = "lunch_causa_limena",
            videoUrl = "https://youtu.be/gtCIqYUCekU?si=q1U8tie65CBKPhuq",
            tiempoPreparacion = 60,
            porciones = 8,
            dificultad = "Medio",
            ingredientes = listOf(
                "2 kg de papas amarillas",
                "1/2 taza de ají amarillo molido",
                "1/2 taza de aceite vegetal",
                "Jugo de 4 limones",
                "400g de pechuga de pollo cocida y desmenuzada",
                "1/2 taza de mayonesa",
                "1 palta madura",
                "2 huevos duros en rodajas",
                "Aceitunas y lechuga para decorar",
                "Sal y pimienta al gusto"
            ),
            pasos = listOf(
                "Cocinar las papas hasta que estén tiernas, pelar y prensar",
                "Mezclar la papa con ají amarillo, aceite, jugo de limón, sal y pimienta",
                "Para el relleno: mezclar pollo con mayonesa y sazonar",
                "En un molde, colocar una capa de papa, luego el relleno de pollo",
                "Agregar rodajas de palta y huevo duro",
                "Cubrir con otra capa de papa y presionar suavemente",
                "Refrigerar por 1 hora antes de desmoldar y decorar"
            ),
            categoria = Constants.CATEGORY_ALMUERZO
        ))

        insertRecipe(db, Receta(
            nombre = "Papa a la Huancaína",
            descripcion = "Papas sancochadas bañadas en cremosa salsa de ají amarillo y queso",
            imagen = "lunch_papa_huancaina",
            videoUrl = "https://youtu.be/IjWgPVBCHXU?si=JoaIyjbkCWZeJ30L",
            tiempoPreparacion = 45,
            porciones = 6,
            dificultad = "Fácil",
            ingredientes = listOf(
                "12 papas amarillas medianas",
                "8 galletas de soda",
                "200g de queso fresco",
                "4 ajíes amarillos sin venas",
                "1 cebolla picada",
                "2 dientes de ajo",
                "1/2 taza de aceite vegetal",
                "1/2 taza de leche evaporada",
                "Huevos duros, aceitunas y lechuga para decorar",
                "Sal al gusto"
            ),
            pasos = listOf(
                "Cocinar las papas con piel hasta que estén tiernas, luego pelar",
                "Licuar los ajíes con cebolla, ajo, galletas, queso y leche",
                "Ir agregando aceite en hilo hasta emulsionar la salsa",
                "Sazonar con sal y refrigerar la salsa por 30 minutos",
                "Colocar las papas en plato sobre lechuga",
                "Bañar con salsa huancaína y decorar con huevo duro y aceitunas",
                "Servir frío como entrada"
            ),
            categoria = Constants.CATEGORY_ALMUERZO
        ))

        insertRecipe(db, Receta(
            nombre = "Arroz con Pollo",
            descripcion = "Arroz verde perfumado con culantro y pollo tierno",
            imagen = "lunch_arroz_pollo",
            videoUrl = "https://youtu.be/Lk8OV9GMdXY?si=CCS4iLDgOrlqwSHY",
            tiempoPreparacion = 60,
            porciones = 6,
            dificultad = "Fácil",
            ingredientes = listOf(
                "1 kg de pollo en presas",
                "3 tazas de arroz",
                "1 manojo de culantro",
                "1 cebolla picada",
                "4 dientes de ajo picados",
                "1 ají verde sin venas",
                "1/2 taza de cerveza negra",
                "1/2 taza de arvejas",
                "1 zanahoria en cubos",
                "Sal, pimienta y comino al gusto"
            ),
            pasos = listOf(
                "Licuar el culantro con ají verde y un poco de agua",
                "Dorar las presas de pollo en aceite, retirar y reservar",
                "En la misma olla, sofreír cebolla y ajo",
                "Agregar el arroz y sofreír por 2 minutos",
                "Incorporar la pasta de culantro, cerveza y 5 tazas de agua caliente",
                "Agregar el pollo, arvejas y zanahoria",
                "Cocinar a fuego bajo por 20 minutos hasta que el arroz esté listo"
            ),
            categoria = Constants.CATEGORY_ALMUERZO
        ))

        insertRecipe(db, Receta(
            nombre = "Seco de Cordero",
            descripcion = "Guiso de cordero con culantro, frejoles y yuca",
            imagen = "lunch_seco_cordero",
            videoUrl = "https://youtu.be/ik4MHm7ahRA?si=eUeWJ6lprkm9fiQG",
            tiempoPreparacion = 150,
            porciones = 6,
            dificultad = "Difícil",
            ingredientes = listOf(
                "1.5 kg de cordero en trozos",
                "2 manojos de culantro",
                "2 cebollas picadas",
                "6 dientes de ajo",
                "1 taza de chicha de jora o cerveza negra",
                "2 tazas de frejoles canarios remojados",
                "500g de yuca pelada",
                "2 cucharadas de ají mirasol molido",
                "Comino, pimienta y sal al gusto"
            ),
            pasos = listOf(
                "Licuar el culantro con un poco de agua",
                "Sellar el cordero en aceite caliente, retirar y reservar",
                "En la misma olla, sofreír cebolla y ajo",
                "Agregar ají mirasol, comino y pimienta",
                "Volver a poner el cordero, agregar chicha de jora y cocinar 30 minutos",
                "Agregar los frejoles y agua suficiente, cocinar 1 hora más",
                "Acompañar con yuca sancochada y arroz blanco"
            ),
            categoria = Constants.CATEGORY_ALMUERZO
        ))

        insertRecipe(db, Receta(
            nombre = "Ceviche Mixto",
            descripcion = "Pescado y mariscos marinados en jugo de limón con cebolla y ají",
            imagen = "lunch_ceviche_mixto",
            videoUrl = "https://youtu.be/CuuFn81HJYk?si=h1QnhM4lpF1-56z5",
            tiempoPreparacion = 35,
            porciones = 4,
            dificultad = "Fácil",
            ingredientes = listOf(
                "500g de filete de pescado blanco en cubos",
                "200g de camarones cocidos",
                "200g de calamares en anillos",
                "200g de conchas de abanico",
                "15 limones verdes",
                "2 cebollas rojas en pluma",
                "2 ajíes limo picados",
                "1 rama de apio picado",
                "1/2 taza de culantro picado",
                "Camote, choclo y lechuga para acompañar"
            ),
            pasos = listOf(
                "En un bowl, mezclar el pescado con sal y jugo de limón",
                "Agregar los mariscos y mezclar suavemente",
                "Incorporar cebolla, ají limo, apio y culantro",
                "Refrigerar por 15 minutos (no más para que no se cocine demasiado)",
                "Servir inmediatamente con camote sancochado, choclo y lechuga",
                "Acompañar con caldo de ceviche (leche de tigre)"
            ),
            categoria = Constants.CATEGORY_ALMUERZO
        ))

        // ==================== CENAS PERUANAS ====================
        insertRecipe(db, Receta(
            nombre = "Tacu Tacu con Lomo",
            descripcion = "Tortilla de arroz y frejoles refritos con lomo saltado",
            imagen = "dinner_tacu_tacu",
            videoUrl = "https://youtu.be/bH0VjyvgQjc?si=_T6Urv_9n2gpe2gO",
            tiempoPreparacion = 50,
            porciones = 4,
            dificultad = "Medio",
            ingredientes = listOf(
                "3 tazas de arroz cocido",
                "2 tazas de frejoles canarios cocidos",
                "400g de lomo fino en tiras",
                "1 cebolla roja en gajos",
                "2 tomates en gajos",
                "2 cucharadas de sillao",
                "1 cucharada de vinagre tinto",
                "1/4 taza de caldo de carne",
                "Aceite vegetal para freír",
                "Plátano frito para acompañar"
            ),
            pasos = listOf(
                "Mezclar el arroz con los frejoles y formar tortillas",
                "Freír las tortillas en aceite hasta dorar por ambos lados",
                "En otra sartén, sellar la carne rápidamente",
                "Agregar cebolla, tomate, sillao, vinagre y caldo",
                "Cocinar a fuego alto hasta reducir la salsa",
                "Servir el tacu tacu con el lomo saltado por encima",
                "Acompañar con plátano frito"
            ),
            categoria = Constants.CATEGORY_CENA
        ))

        insertRecipe(db, Receta(
            nombre = "Arroz Chaufa",
            descripcion = "Arroz frito estilo chifa con pollo, huevo y verduras",
            imagen = "dinner_arroz_chaufa",
            videoUrl = "https://youtu.be/M_r2lIuQ3qI?si=KZQfaoZdAtT6Db95",
            tiempoPreparacion = 30,
            porciones = 4,
            dificultad = "Fácil",
            ingredientes = listOf(
                "4 tazas de arroz cocido frío",
                "200g de pechuga de pollo en cubos",
                "3 huevos batidos",
                "1 cebolla china picada",
                "1/2 taza de tortilla de huevo en tiras",
                "3 cucharadas de sillao",
                "1 cucharada de aceite de ajonjolí",
                "2 cucharadas de aceite vegetal",
                "Sal y pimienta al gusto"
            ),
            pasos = listOf(
                "En un wok, calentar aceite y cocinar el pollo hasta dorar",
                "Agregar la cebolla china y saltear por 1 minuto",
                "Incorporar los huevos batidos y revolver rápidamente",
                "Agregar el arroz frío y desgranar con tenedor",
                "Verter el sillao y aceite de ajonjolí, mezclar bien",
                "Agregar las tiras de tortilla y saltear 2 minutos más",
                "Servir caliente inmediatamente"
            ),
            categoria = Constants.CATEGORY_CENA
        ))

        insertRecipe(db, Receta(
            nombre = "Pollo a la Brasa",
            descripcion = "Pollo marinado y asado con carbón, crocante por fuera y jugoso por dentro",
            imagen = "dinner_pollo_brasa",
            videoUrl = "https://youtu.be/YpEXS20-SX4?si=6n8-Hyo7B4R6UGrA",
            tiempoPreparacion = 120,
            porciones = 4,
            dificultad = "Difícil",
            ingredientes = listOf(
                "1 pollo entero (1.5 kg)",
                "1/2 taza de vinagre tinto",
                "1/4 taza de sillao",
                "4 dientes de ajo molidos",
                "1 cucharada de ají panca molido",
                "1 cucharada de comino molido",
                "1 cucharada de orégano seco",
                "2 cucharadas de manteca de cerdo",
                "Papas fritas y ensalada criolla para acompañar"
            ),
            pasos = listOf(
                "Mezclar todos los ingredientes del marinado",
                "Adobar el pollo por dentro y por fuera, refrigerar 4 horas",
                "Precalentar horno a 200°C o preparar carbón",
                "Asar el pollo por 1.5 horas, girando ocasionalmente",
                "Pincelar con manteca derretida cada 20 minutos",
                "Cuando esté dorado y cocido, retirar y reposar 10 minutos",
                "Servir con papas fritas y ensalada criolla"
            ),
            categoria = Constants.CATEGORY_CENA
        ))

        insertRecipe(db, Receta(
            nombre = "Anticuchos",
            descripcion = "Brochetas de corazón de res marinadas en ají panca y especias",
            imagen = "dinner_anticuchos",
            videoUrl = "https://youtu.be/MTVagyVam_o?si=7tiLaP-VNG3_99zA",
            tiempoPreparacion = 60,
            porciones = 4,
            dificultad = "Medio",
            ingredientes = listOf(
                "1 kg de corazón de res limpio",
                "1/2 taza de ají panca molido",
                "4 dientes de ajo molidos",
                "1 cucharada de comino molido",
                "1/4 taza de vinagre tinto",
                "2 cucharadas de aceite vegetal",
                "Papas sancochadas y choclo para acompañar",
                "Sal y pimienta al gusto"
            ),
            pasos = listOf(
                "Cortar el corazón en trozos de 3 cm",
                "Preparar el adobo: mezclar ají panca, ajo, comino, vinagre y aceite",
                "Marinar el corazón por lo menos 2 horas en refrigeración",
                "Ensartar en brochetas de metal o madera",
                "Asar a la parrilla o en sartén por 10-15 minutos, girando",
                "Servir con papas sancochadas, choclo y salsa de ají"
            ),
            categoria = Constants.CATEGORY_CENA
        ))

        insertRecipe(db, Receta(
            nombre = "Parihuela",
            descripcion = "Sustanciosa sopa de mariscos con pescado y especias",
            imagen = "dinner_parihuela",
            videoUrl = "https://youtu.be/L99qG4HMTxk?si=75DBjgBVUSJUa2ct",
            tiempoPreparacion = 60,
            porciones = 6,
            dificultad = "Medio",
            ingredientes = listOf(
                "500g de filete de pescado firme",
                "500g de mariscos mixtos (camarones, calamares, mejillones)",
                "1 cebolla roja picada",
                "4 dientes de ajo picados",
                "2 tomates picados",
                "1/4 taza de ají panca molido",
                "1/2 taza de vino blanco",
                "2 litros de caldo de pescado",
                "Culantro y perejil picados",
                "Yuca y arroz para acompañar"
            ),
            pasos = listOf(
                "En una olla grande, sofreír cebolla y ajo en aceite",
                "Agregar tomate y ají panca, cocinar 5 minutos",
                "Incorporar el vino blanco y reducir a la mitad",
                "Agregar el caldo de pescado y hervir 15 minutos",
                "Añadir los mariscos según su tiempo de cocción",
                "Finalmente agregar el pescado y cocinar 5 minutos",
                "Servir caliente con culantro fresco"
            ),
            categoria = Constants.CATEGORY_CENA
        ))

        insertRecipe(db, Receta(
            nombre = "Carapulcra",
            descripcion = "Guiso de papa seca con cerdo y maní, de origen andino",
            imagen = "dinner_carapulcra",
            videoUrl = "https://youtu.be/no5l20i8WnM?si=PXEA1ar-2bV0Y6tN",
            tiempoPreparacion = 120,
            porciones = 6,
            dificultad = "Difícil",
            ingredientes = listOf(
                "500g de papa seca remojada",
                "500g de costilla de cerdo en trozos",
                "1/2 taza de maní tostado molido",
                "2 cebollas picadas",
                "4 dientes de ajo picados",
                "2 cucharadas de ají panca molido",
                "1/2 taza de vino tinto",
                "2 tazas de caldo de carne",
                "Comino, orégano y pimienta al gusto"
            ),
            pasos = listOf(
                "Dorar la costilla de cerdo en su propia grasa, retirar",
                "En la misma olla, sofreír cebolla y ajo",
                "Agregar ají panca, comino y orégano",
                "Incorporar la papa seca escurrida y el maní molido",
                "Volver a poner la carne, agregar vino y caldo",
                "Cocinar a fuego lento por 1.5 horas hasta espesar",
                "Servir con arroz blanco y salsa criolla"
            ),
            categoria = Constants.CATEGORY_CENA
        ))

        insertRecipe(db, Receta(
            nombre = "Chaufa de Mariscos",
            descripcion = "Arroz frito con variedad de mariscos frescos y salsa de ostión",
            imagen = "dinner_chaufa_mariscos",
            videoUrl = "https://youtu.be/hquYb706444?si=M5iR9OW-AAdV2o-F",
            tiempoPreparacion = 35,
            porciones = 4,
            dificultad = "Medio",
            ingredientes = listOf(
                "4 tazas de arroz cocido frío",
                "300g de mariscos mixtos (camarones, calamares, conchas)",
                "2 huevos batidos",
                "1 cebolla china picada",
                "1/2 pimiento rojo en cubos",
                "3 cucharadas de salsa de ostión",
                "1 cucharada de jengibre rallado",
                "2 cucharadas de aceite vegetal",
                "Cebollita de verdeo para decorar"
            ),
            pasos = listOf(
                "Saltear los mariscos en aceite caliente por 2 minutos, retirar",
                "En el mismo wok, hacer tortilla con los huevos batidos, desmenuzar",
                "Agregar cebolla china, pimiento y jengibre, saltear 1 minuto",
                "Incorporar el arroz frío y desgranar bien",
                "Verter la salsa de ostión y mezclar",
                "Devolver los mariscos a la preparación",
                "Saltear 2 minutos más y servir con cebollita de verdeo"
            ),
            categoria = Constants.CATEGORY_CENA
        ))

        // ==================== POSTRES PERUANOS ====================
        insertRecipe(db, Receta(
            nombre = "Mazamorra Morada",
            descripcion = "Postre espeso de maíz morado con frutas secas y especias",
            imagen = "dessert_mazamorra_morada",
            videoUrl = "https://youtu.be/0iFwT8mr_a4?si=JdqyzKQ3Yj_eby53",
            tiempoPreparacion = 90,
            porciones = 8,
            dificultad = "Medio",
            ingredientes = listOf(
                "2 tazas de maíz morado en grano",
                "1 raja de canela",
                "4 clavos de olor",
                "1 taza de harina de camote o maicena",
                "1 taza de azúcar",
                "1/2 taza de guindones",
                "1/2 taza de pasas",
                "1/2 taza de ciruelas pasas sin hueso",
                "1 manzana pelada en cubos",
                "Cáscara de naranja y limón"
            ),
            pasos = listOf(
                "Cocinar el maíz morado con canela, clavo y cáscaras por 1 hora",
                "Colar el caldo y descartar los sólidos",
                "Disolver la harina en un poco de agua fría",
                "En una olla, hervir el caldo con azúcar",
                "Agregar la harina disuelta y revolver hasta espesar",
                "Incorporar todas las frutas y cocinar 15 minutos más",
                "Servir frío o caliente espolvoreado con canela"
            ),
            categoria = Constants.CATEGORY_POSTRES
        ))

        insertRecipe(db, Receta(
            nombre = "Arroz con Leche",
            descripcion = "Postre cremoso de arroz con leche, canela y pasas",
            imagen = "dessert_arroz_leche",
            videoUrl = "https://youtu.be/hMY_bn6jTS8?si=C3HjOZmeIvn0GQ70",
            tiempoPreparacion = 60,
            porciones = 6,
            dificultad = "Fácil",
            ingredientes = listOf(
                "1 taza de arroz de grano corto",
                "1 litro de leche entera",
                "1 litro de agua",
                "1 raja de canela",
                "1 taza de azúcar",
                "1/2 taza de pasas",
                "1 cucharadita de esencia de vainilla",
                "Canela en polvo para decorar"
            ),
            pasos = listOf(
                "Cocinar el arroz en agua con canela hasta que esté blando",
                "Agregar la leche y cocinar a fuego lento 30 minutos",
                "Incorporar el azúcar y las pasas",
                "Cocinar 15 minutos más revolviendo frecuentemente",
                "Retirar del fuego y agregar esencia de vainilla",
                "Servir frío o caliente espolvoreado con canela"
            ),
            categoria = Constants.CATEGORY_POSTRES
        ))

        insertRecipe(db, Receta(
            nombre = "Picarones",
            descripcion = "Anillos fritos de masa de camote y zapato bañados en miel de chancaca",
            imagen = "dessert_picarones",
            videoUrl = "https://youtu.be/ZjnUl4UzApg?si=nd_yy-jsmOX9rar6",
            tiempoPreparacion = 120,
            porciones = 8,
            dificultad = "Difícil",
            ingredientes = listOf(
                "500g de camote amarillo cocido",
                "500g de zapato cocido",
                "1 kg de harina",
                "50g de levadura fresca",
                "1/2 taza de azúcar",
                "1 cucharadita de anís en grano",
                "1 raja de canela",
                "1 clavo de olor",
                "Aceite para freír",
                "500g de chancaca para la miel"
            ),
            pasos = listOf(
                "Mezclar los camotes y zapatos cocidos y prensados",
                "Disolver la levadura en agua tibia con azúcar",
                "Mezclar la harina con la levadura y las verduras",
                "Amasar hasta obtener una masa suave, dejar levar 1 hora",
                "Para la miel: cocinar chancaca con especias y agua hasta espesar",
                "Formar anillos con la masa y freír en aceite caliente",
                "Bañar los picarones calientes con la miel de chancaca"
            ),
            categoria = Constants.CATEGORY_POSTRES
        ))

        insertRecipe(db, Receta(
            nombre = "Suspiro a la Limeña",
            descripcion = "Dulce de manjar blanco cubierto con merengue de vino oporto",
            imagen = "dessert_suspiro_limena",
            videoUrl = "https://youtu.be/pdI3hr58U50?si=EbZzqgn6hqMODrxJ",
            tiempoPreparacion = 45,
            porciones = 6,
            dificultad = "Medio",
            ingredientes = listOf(
                "1 lata de leche condensada",
                "1 lata de leche evaporada",
                "6 yemas de huevo",
                "1 cucharadita de esencia de vainilla",
                "4 claras de huevo",
                "1/2 taza de azúcar",
                "1/4 taza de vino oporto",
                "Canela en polvo para decorar"
            ),
            pasos = listOf(
                "En una olla, cocinar las leches a fuego bajo por 20 minutos",
                "Batir las yemas y agregar lentamente a la leche caliente",
                "Cocinar 5 minutos más hasta espesar, agregar vainilla",
                "Verter en copas individuales y dejar enfriar",
                "Para el merengue: batir claras a punto de nieve con azúcar",
                "Agregar el vino oporto y batir hasta picos firmes",
                "Cubrir el manjar blanco con merengue y decorar con canela"
            ),
            categoria = Constants.CATEGORY_POSTRES
        ))

        insertRecipe(db, Receta(
            nombre = "Turrón de Doña Pepa",
            descripcion = "Postre de capas de masa crujiente unidas con miel de anís",
            imagen = "dessert_turron_pepa",
            videoUrl = "https://youtu.be/TElTX-lW0pM?si=W3HAFUG3kfqaUjHj",
            tiempoPreparacion = 180,
            porciones = 12,
            dificultad = "Difícil",
            ingredientes = listOf(
                "1 kg de harina",
                "500g de manteca vegetal",
                "10 yemas de huevo",
                "1/2 taza de anís en grano",
                "1 taza de azúcar impalpable",
                "1/2 taza de leche evaporada",
                "Colorantes alimenticios (amarillo, rojo, verde)",
                "1 kg de chancaca para la miel",
                "Dulces mixtos para decorar"
            ),
            pasos = listOf(
                "Mezclar harina con manteca hasta arena",
                "Agregar yemas, anís molido y leche, amasar",
                "Dividir la masa en partes y colorear",
                "Formar tiras delgadas y hornear a 180°C por 15 minutos",
                "Para la miel: cocinar chancaca con anís hasta punto de hebra",
                "Armar capas de masa bañadas con miel caliente",
                "Decorar con dulces mixtos y dejar reposar 24 horas"
            ),
            categoria = Constants.CATEGORY_POSTRES
        ))

        insertRecipe(db, Receta(
            nombre = "Crema Volteada",
            descripcion = "Flan de leche con caramelo, versión peruana del crème caramel",
            imagen = "dessert_crema_volteada",
            videoUrl = "https://youtu.be/ikRLowcQqTA?si=pzbQxMeF02kG1QPZ",
            tiempoPreparacion = 90,
            porciones = 8,
            dificultad = "Medio",
            ingredientes = listOf(
                "1 lata de leche condensada",
                "1 lata de leche evaporada",
                "6 huevos enteros",
                "1 cucharadita de esencia de vainilla",
                "1 taza de azúcar para el caramelo",
                "1/4 taza de agua",
                "1 cucharadita de jugo de limón"
            ),
            pasos = listOf(
                "Preparar caramelo: derretir azúcar con agua y limón hasta dorar",
                "Verter caramelo en moldes y girar para cubrir",
                "Licuar las leches con huevos y vainilla",
                "Colar la mezcla y verter en los moldes con caramelo",
                "Cocinar a baño maría en horno a 180°C por 45 minutos",
                "Enfriar completamente y refrigerar 4 horas",
                "Desmoldar volteando cuidadosamente"
            ),
            categoria = Constants.CATEGORY_POSTRES
        ))

        insertRecipe(db, Receta(
            nombre = "Ranfañote",
            descripcion = "Postre de pan duro, miel de chancaca y frutos secos",
            imagen = "dessert_ranfanote",
            videoUrl = "https://youtu.be/gswqr5KGM0o?si=8XRP6Wiu0mgo8Axf",
            tiempoPreparacion = 40,
            porciones = 6,
            dificultad = "Fácil",
            ingredientes = listOf(
                "500g de pan duro en cubos",
                "500g de chancaca",
                "1 raja de canela",
                "3 clavos de olor",
                "1/2 taza de coco rallado",
                "1/2 taza de maní tostado",
                "1/2 taza de pasas",
                "1/2 taza de nueces picadas",
                "1/4 taza de ajonjolí tostado"
            ),
            pasos = listOf(
                "Tostar ligeramente los cubos de pan en horno",
                "Derretir la chancaca con especias y un poco de agua",
                "Colar la miel y mezclar con el pan tostado",
                "Agregar coco, maní, pasas y nueces",
                "Mezclar bien hasta que todo esté impregnado",
                "Formar porciones y espolvorear con ajonjolí",
                "Dejar enfriar completamente antes de servir"
            ),
            categoria = Constants.CATEGORY_POSTRES
        ))
    }

    private fun insertRecipe(db: SQLiteDatabase, receta: Receta) {
        val values = ContentValues().apply {
            put(COLUMN_RECIPE_NAME, receta.nombre)
            put(COLUMN_RECIPE_DESCRIPTION, receta.descripcion)
            put(COLUMN_RECIPE_IMAGE, receta.imagen)
            put(COLUMN_RECIPE_VIDEO_URL, receta.videoUrl)
            put(COLUMN_RECIPE_TIME, receta.tiempoPreparacion)
            put(COLUMN_RECIPE_PORTIONS, receta.porciones)
            put(COLUMN_RECIPE_DIFFICULTY, receta.dificultad)
            put(COLUMN_RECIPE_INGREDIENTS, receta.ingredientes.joinToString("||"))
            put(COLUMN_RECIPE_STEPS, receta.pasos.joinToString("||"))
            put(COLUMN_RECIPE_CATEGORY, receta.categoria)
        }
        db.insert(TABLE_RECIPES, null, values)
    }

    // Método para agregar usuario
    fun addUser(usuario: Usuario): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, usuario.nombre)
            put(COLUMN_USER_EMAIL, usuario.email)
            put(COLUMN_USER_PASSWORD, usuario.password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    // Método para verificar credenciales de login
    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Método para verificar si el email ya existe
    fun checkEmailExists(email: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Método para obtener todas las recetas de una categoría
    fun getRecipesByCategory(categoria: String): List<Receta> {
        val recipes = mutableListOf<Receta>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_RECIPES WHERE $COLUMN_RECIPE_CATEGORY = ?"
        val cursor = db.rawQuery(query, arrayOf(categoria))

        while (cursor.moveToNext()) {
            val recipe = Receta.fromDatabase(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DESCRIPTION)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_IMAGE)),
                videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_VIDEO_URL)),
                tiempoPreparacion = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_TIME)),
                porciones = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PORTIONS)),
                dificultad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DIFFICULTY)),
                ingredientesString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS)),
                pasosString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS)),
                categoria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY))
            )
            recipes.add(recipe)
        }
        cursor.close()
        return recipes
    }

    // Método para obtener una receta por ID
    fun getRecipeById(id: Int): Receta? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_RECIPES WHERE $COLUMN_RECIPE_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val recipe = Receta.fromDatabase(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DESCRIPTION)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_IMAGE)),
                videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_VIDEO_URL)),
                tiempoPreparacion = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_TIME)),
                porciones = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PORTIONS)),
                dificultad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DIFFICULTY)),
                ingredientesString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS)),
                pasosString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS)),
                categoria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY))
            )
            cursor.close()
            recipe
        } else {
            cursor.close()
            null
        }
    }

    // Método para buscar recetas por nombre
    fun searchRecipes(query: String): List<Receta> {
        val recipes = mutableListOf<Receta>()
        val db = readableDatabase
        val searchQuery = "SELECT * FROM $TABLE_RECIPES WHERE $COLUMN_RECIPE_NAME LIKE ?"
        val cursor = db.rawQuery(searchQuery, arrayOf("%$query%"))

        while (cursor.moveToNext()) {
            val recipe = Receta.fromDatabase(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DESCRIPTION)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_IMAGE)),
                videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_VIDEO_URL)),
                tiempoPreparacion = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_TIME)),
                porciones = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PORTIONS)),
                dificultad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DIFFICULTY)),
                ingredientesString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS)),
                pasosString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS)),
                categoria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY))
            )
            recipes.add(recipe)
        }
        cursor.close()
        return recipes
    }

    // Métodos para favoritos
    fun addFavorite(usuarioId: Int, recetaId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FAV_USER_ID, usuarioId)
            put(COLUMN_FAV_RECIPE_ID, recetaId)
        }
        return db.insert(TABLE_FAVORITES, null, values)
    }

    fun removeFavorite(usuarioId: Int, recetaId: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_FAVORITES,
            "$COLUMN_FAV_USER_ID = ? AND $COLUMN_FAV_RECIPE_ID = ?",
            arrayOf(usuarioId.toString(), recetaId.toString())
        )
    }

    fun isFavorite(usuarioId: Int, recetaId: Int): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_FAVORITES WHERE $COLUMN_FAV_USER_ID = ? AND $COLUMN_FAV_RECIPE_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString(), recetaId.toString()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getFavorites(usuarioId: Int): List<Receta> {
        val recipes = mutableListOf<Receta>()
        val db = readableDatabase
        val query = """
        SELECT r.* FROM $TABLE_RECIPES r
        INNER JOIN $TABLE_FAVORITES f ON r.$COLUMN_RECIPE_ID = f.$COLUMN_FAV_RECIPE_ID
        WHERE f.$COLUMN_FAV_USER_ID = ?
    """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(usuarioId.toString()))

        while (cursor.moveToNext()) {
            val recipe = Receta.fromDatabase(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DESCRIPTION)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_IMAGE)),
                videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_VIDEO_URL)),
                tiempoPreparacion = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_TIME)),
                porciones = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PORTIONS)),
                dificultad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DIFFICULTY)),
                ingredientesString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS)),
                pasosString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS)),
                categoria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY))
            )
            recipes.add(recipe)
        }
        cursor.close()
        return recipes
    }

    // Método para búsqueda global en todas las categorías
    fun searchRecipesGlobal(query: String): List<Receta> {
        val recipes = mutableListOf<Receta>()
        val db = readableDatabase
        val searchQuery = """
        SELECT * FROM $TABLE_RECIPES 
        WHERE $COLUMN_RECIPE_NAME LIKE ? 
        OR $COLUMN_RECIPE_DESCRIPTION LIKE ?
        OR $COLUMN_RECIPE_INGREDIENTS LIKE ?
    """.trimIndent()
        val searchPattern = "%$query%"
        val cursor = db.rawQuery(searchQuery, arrayOf(searchPattern, searchPattern, searchPattern))

        while (cursor.moveToNext()) {
            val recipe = Receta.fromDatabase(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DESCRIPTION)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_IMAGE)),
                videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_VIDEO_URL)),
                tiempoPreparacion = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_TIME)),
                porciones = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PORTIONS)),
                dificultad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DIFFICULTY)),
                ingredientesString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS)),
                pasosString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS)),
                categoria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY))
            )
            recipes.add(recipe)
        }
        cursor.close()
        return recipes
    }

    // Método para obtener usuario por email
    fun getUserByEmail(email: String): Usuario? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))

        return if (cursor.moveToFirst()) {
            val usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD))
            )
            cursor.close()
            usuario
        } else {
            cursor.close()
            null
        }
    }

    // Método para obtener todas las recetas (para estadísticas)
    fun getAllRecipes(): List<Receta> {
        val recipes = mutableListOf<Receta>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_RECIPES"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val recipe = Receta.fromDatabase(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DESCRIPTION)),
                imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_IMAGE)),
                videoUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_VIDEO_URL)),
                tiempoPreparacion = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_TIME)),
                porciones = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_PORTIONS)),
                dificultad = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DIFFICULTY)),
                ingredientesString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS)),
                pasosString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS)),
                categoria = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY))
            )
            recipes.add(recipe)
        }
        cursor.close()
        return recipes
    }
}