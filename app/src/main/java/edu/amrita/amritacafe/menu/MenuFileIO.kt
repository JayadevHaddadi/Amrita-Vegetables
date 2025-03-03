package edu.amrita.amritacafe.menu

import android.content.Context
import edu.amrita.amritacafe.activities.capitalizeWords
import java.io.File
import java.io.FileOutputStream

fun createDefualtFilesIfNecessary(context: Context) {
    val dir = File(context.filesDir, "Menus")

    println("Path: : $dir")
    println("Exists? : " + dir.exists())
    if (!dir.exists()) {
        println("Created: " + dir.mkdirs())
    }

    val BREAKFAST_FILE = File(dir, "Breakfast.txt")

    if (!BREAKFAST_FILE.isFile) {
        createMenuFileFromMenuList(BREAKFAST_FILE, DEFAULT_BREAKFAST_MENU)
    }
}

fun createMenuFileFromMenuList(file: File, list: List<MenuItemUS>) {
    file.createNewFile()
    val fos = FileOutputStream(file, false)
    var category = ""

    for (item in list) {
        val nextCategory = item.category.toUpperCase()
        if (category != nextCategory) {
            category = nextCategory
            fos.write("\n$category\n".toByteArray())
        }

        fos.write("${item.malaylamName.capitalizeWords()}, ${item.englishName.toUpperCase()}, ${item.price.toInt()}\n".toByteArray())
    }

    fos.close()
}

fun overrideFile(text: String, file: File, context: Context) {
    val fos = FileOutputStream(file, false)
    fos.write(text.toByteArray())
    fos.close()
}

fun getListOfMenu(file: File): List<MenuItemUS> { //file parameter added.
    val readLines = file.readText()
    val (_, list) = readMenuFromText(readLines)
    return list
}

fun saveIfValidText(text: String, context: Context, file: File): String {
    try {
        val (_, list) = readMenuFromText(text)
        createMenuFileFromMenuList(file, list)
    } catch (e: BadMenuException) {
        return e.message ?: "Bad menu"
    } catch (e: Exception) {
        return "Failed on saving file"
    }
    return "Successfully saved ${file.name}"
}

private fun readMenuFromText(allText: String): Pair<String, List<MenuItemUS>> {
    val lineByLine = allText.split("\n")

    val menu = mutableListOf<MenuItemUS>()

    var category = ""
    var itemNr = 1
    try {
        for (line in lineByLine) {
            val columns = line.split(",").map { it.trim() }
            if (columns.size == 1) {
                if (columns[0].isEmpty())
                    continue
                category = columns[0].toUpperCase()
                itemNr = 1
            } else
                menu.add(
                    MenuItemUS(
                        columns[0].capitalizeWords(),
                        columns[1].toUpperCase(),
                        columns[2].toFloat(),
                        category
                    )
                )
            itemNr++
        }
        for (item in menu) {
            println("${item.malaylamName},${item.englishName},${item.price},${item.category}")
        }
        return Pair("Successfully saved", menu.toList())
    } catch (e: Exception) {
        throw BadMenuException("Save failed at:\nCategory: ${category}\nItem in category: ${itemNr}")
    }
}

class BadMenuException(message: String) : Throwable(message)
