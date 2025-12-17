/**
 * ТЕЛЕФОННАЯ КНИГА - MVC в одном файле
 * Полная реализация MVC архитектуры в одном файле Kotlin
 */

// ================================
// МОДЕЛЬ (MODEL) - данные и логика
// ================================

// Сущность контакта
data class Contact(val id: Int, val name: String, val phone: String)

// Модель данных
class ContactModel {
    private val contacts = mutableListOf<Contact>()
    private var nextId = 1
    private val fileName = "phonebook.txt"

    init {
        loadFromFile()
    }

    // CRUD операции
    fun addContact(name: String, phone: String): Contact {
        val contact = Contact(nextId++, name, phone)
        contacts.add(contact)
        saveToFile()
        return contact
    }

    fun getAllContacts(): List<Contact> = contacts.toList()

    fun searchContacts(query: String): List<Contact> {
        return contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query)
        }
    }

    fun deleteContact(id: Int): Boolean {
        val result = contacts.removeIf { it.id == id }
        if (result) saveToFile()
        return result
    }

    private fun saveToFile() {
        try {
            val file = java.io.File(fileName)
            val content = contacts.joinToString("\n") { "${it.id}|${it.name}|${it.phone}" }
            file.writeText(content)
        } catch (e: Exception) {
            println("⚠️ Не удалось сохранить в файл: ${e.message}")
        }
    }

    private fun loadFromFile() {
        try {
            val file = java.io.File(fileName)
            if (file.exists()) {
                file.readLines().forEach { line ->
                    val parts = line.split("|")
                    if (parts.size == 3) {
                        val id = parts[0].toInt()
                        val name = parts[1]
                        val phone = parts[2]
                        contacts.add(Contact(id, name, phone))
                        if (id >= nextId) nextId = id + 1
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠️ Не удалось загрузить из файла")
        }
    }
}

// ================================
// ПРЕДСТАВЛЕНИЕ (VIEW) - интерфейс
// ================================

class ContactView {

    fun showWelcome() {
        println("\n" + "⭐".repeat(45))
        println("            ТЕЛЕФОННАЯ КНИГА v2.0")
        println("⭐".repeat(45))
    }

    fun showMainMenu() {
        println("\n" + "=".repeat(40))
        println("ГЛАВНОЕ МЕНЮ:")
        println("=".repeat(40))
        println("1. 📝 Добавить новый контакт")
        println("2. 📋 Показать все контакты")
        println("3. 🔍 Найти контакт")
        println("4. 🗑️  Удалить контакт")
        println("5. 📊 Статистика")
        println("6. ❌ Выход")
        println("-".repeat(40))
        print("Выберите действие (1-6): ")
    }

    fun showContacts(contacts: List<Contact>) {
        if (contacts.isEmpty()) {
            println("\n📭 Телефонная книга пуста")
        } else {
            println("\n" + "📋".repeat(25))
            println("СПИСОК КОНТАКТОВ (${contacts.size}):")
            println("📋".repeat(25))
            contacts.forEach { contact ->
                println("  ${contact.id}. ${contact.name.padEnd(15)} | ${contact.phone}")
            }
            println("-".repeat(40))
        }
    }

    fun showContactDetails(contact: Contact) {
        println("\n" + "📄".repeat(20))
        println("ДЕТАЛИ КОНТАКТА:")
        println("📄".repeat(20))
        println("  ID:      ${contact.id}")
        println("  Имя:     ${contact.name}")
        println("  Телефон: ${contact.phone}")
        println("-".repeat(40))
    }

    fun showStatistics(total: Int) {
        println("\n" + "📊".repeat(20))
        println("СТАТИСТИКА:")
        println("📊".repeat(20))
        println("  Всего контактов: $total")
        println("-".repeat(40))
    }

    fun showMessage(message: String, type: String = "info") {
        when (type) {
            "success" -> println("✅ $message")
            "error" -> println("❌ $message")
            "warning" -> println("⚠️  $message")
            else -> println("💡 $message")
        }
    }

    // Методы для ввода данных
    fun askForName(): String {
        print("Введите имя контакта: ")
        return readLine()?.trim() ?: ""
    }

    fun askForPhone(): String {
        print("Введите номер телефона: ")
        return readLine()?.trim() ?: ""
    }

    fun askForSearch(): String {
        print("Введите имя или номер для поиска: ")
        return readLine()?.trim() ?: ""
    }

    fun askForId(action: String = "удаления"): Int? {
        print("Введите ID контакта для $action: ")
        val input = readLine()?.trim()
        return input?.toIntOrNull()
    }

    fun askForConfirmation(message: String): Boolean {
        print("$message (да/нет): ")
        return readLine()?.trim()?.lowercase() == "да"
    }

    fun showGoodbye() {
        println("\n" + "👋".repeat(20))
        println("  Спасибо за использование!")
        println("  Все данные сохранены.")
        println("👋".repeat(20))
    }
}

// ================================
// КОНТРОЛЛЕР (CONTROLLER) - логика
// ================================

class ContactController {
    private val model = ContactModel()
    private val view = ContactView()
    private var isRunning = true

    fun start() {
        view.showWelcome()

        while (isRunning) {
            view.showMainMenu()
            val choice = readLine()?.trim()

            when (choice) {
                "1" -> addContact()
                "2" -> showAllContacts()
                "3" -> searchContact()
                "4" -> deleteContact()
                "5" -> showStats()
                "6" -> exit()
                else -> view.showMessage("Неверный выбор. Попробуйте снова.", "error")
            }
        }
    }

    private fun addContact() {
        println("\n" + "➕".repeat(20))
        println("ДОБАВЛЕНИЕ КОНТАКТА")
        println("➕".repeat(20))

        val name = view.askForName()
        if (name.isEmpty()) {
            view.showMessage("Имя не может быть пустым", "error")
            return
        }

        val phone = view.askForPhone()
        if (phone.isEmpty() || !phone.any { it.isDigit() }) {
            view.showMessage("Некорректный номер телефона", "error")
            return
        }

        val contact = model.addContact(name, phone)
        view.showMessage("Контакт успешно добавлен!", "success")
        view.showContactDetails(contact)
    }

    private fun showAllContacts() {
        val contacts = model.getAllContacts()
        view.showContacts(contacts)
    }

    private fun searchContact() {
        println("\n" + "🔍".repeat(20))
        println("ПОИСК КОНТАКТА")
        println("🔍".repeat(20))

        val query = view.askForSearch()
        if (query.isEmpty()) {
            view.showMessage("Поисковый запрос не может быть пустым", "error")
            return
        }

        val results = model.searchContacts(query)
        if (results.isEmpty()) {
            view.showMessage("Контакты не найдены", "warning")
        } else {
            view.showMessage("Найдено контактов: ${results.size}", "success")
            view.showContacts(results)

            // Показать детали если нашли один контакт
            if (results.size == 1) {
                if (view.askForConfirmation("Показать детали контакта?")) {
                    view.showContactDetails(results[0])
                }
            }
        }
    }

    private fun deleteContact() {
        println("\n" + "🗑️".repeat(20))
        println("УДАЛЕНИЕ КОНТАКТА")
        println("🗑️".repeat(20))

        val id = view.askForId()
        if (id == null) {
            view.showMessage("Неверный ID. Должен быть числом.", "error")
            return
        }

        // Сначала ищем контакт для подтверждения
        val contacts = model.getAllContacts()
        val contactToDelete = contacts.find { it.id == id }

        if (contactToDelete == null) {
            view.showMessage("Контакт с ID $id не найден", "error")
            return
        }

        view.showContactDetails(contactToDelete)

        if (view.askForConfirmation("Вы уверены что хотите удалить этот контакт?")) {
            val success = model.deleteContact(id)
            if (success) {
                view.showMessage("Контакт успешно удален!", "success")
            } else {
                view.showMessage("Не удалось удалить контакт", "error")
            }
        } else {
            view.showMessage("Удаление отменено", "info")
        }
    }

    private fun showStats() {
        val contacts = model.getAllContacts()
        view.showStatistics(contacts.size)

        if (contacts.isNotEmpty()) {
            println("\nПоследние 3 контакта:")
            contacts.takeLast(3).forEach {
                println("  ${it.id}. ${it.name}")
            }
        }
    }

    private fun exit() {
        view.showMessage("Сохранение данных...")
        isRunning = false
        view.showGoodbye()
    }
}

// ================================
// ТОЧКА ВХОДА (MAIN)
// ================================

fun main() {
    println("🚀 Запуск телефонной книги...")

    // Автоматически обрабатываем ошибки
    try {
        ContactController().start()
    } catch (e: Exception) {
        println("\n💥 Произошла ошибка: ${e.message}")
        println("Перезапустите программу.")
    }
}

// ================================
// ДОПОЛНИТЕЛЬНЫЕ УТИЛИТЫ
// ================================

// Функция для быстрого теста
fun testPhoneBook() {
    println("\n🧪 ТЕСТИРОВАНИЕ СИСТЕМЫ")
    println("=".repeat(30))

    val model = ContactModel()

    // Тест добавления
    println("1. Тест добавления контакта...")
    model.addContact("Тест", "1234567890")
    println("   ✓ Контакт добавлен")

    // Тест поиска
    println("2. Тест поиска...")
    val found = model.searchContacts("Тест")
    println("   ✓ Найдено: ${found.size} контактов")

    // Тест удаления
    println("3. Тест удаления...")
    val deleted = model.deleteContact(1)
    println("   ✓ Удаление: ${if (deleted) "успешно" else "не удалось"}")

    println("=".repeat(30))
    println("✅ Тестирование завершено!")
}

// Чтобы запустить тест, раскомментируйте следующую строку:
// fun main() = testPhoneBook()