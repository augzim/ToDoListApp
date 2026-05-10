package service

import database.Category
import database.DeadlineFilter
import database.Item
import database.Task
import database.TimeFilter
import database.ToDoDataAccessObject
import java.sql.Timestamp


// todo entire class seems a bit redundant (use dao in UI directly)?
class ItemService(val dao: ToDoDataAccessObject) {
    // ---------- CREATE ----------
    fun createTask(
        title: String,
        description: String?,
        deadline: Timestamp,
        categoryIds: Set<Int>
    ) {
        dao.createTask(title, description, deadline, categoryIds)
    }

    fun createCategory(title: String) {
        dao.createCategory(title)
    }

    // ---------- READ ----------
    fun getAllCategories(): List<Category> =
        dao.getAllCategories()

    fun getAllTasks(): List<Task> =
        dao.getAllTasks()

    fun getTasksByTime(filter: TimeFilter): List<Task> =
        dao.getTasksByTime(filter)

    fun getTasksByCategory(categoryId: Int): List<Task> =
        dao.getTasksByCategoryId(categoryId)

    fun getTasksByDeadline(filter: DeadlineFilter): List<Task> =
        dao.getTasksByDeadline(filter)

    // ---------- DELETE ----------
    fun delete(item: Item) {
        when (item) {
            is Task -> dao.deleteTasks(setOf(item.id))
            is Category -> dao.deleteCategories(setOf(item.id))
        }
    }

    // ---------- UPDATE ----------
    fun updateCategory(categoryId: Int, title: String) {
        dao.updateCategory(categoryId, title)
    }

    fun updateTask(taskId: Int, title: String, description: String?, deadline: Timestamp?, categoryIds: Set<Int>) {
        dao.updateTask(taskId, title, description, deadline, categoryIds)
    }
}