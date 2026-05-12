package database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.Properties


// todo transfer to a separate file Item.kt?
sealed interface Item {
    val id: Int
    val title: String
    val createdAt: Timestamp
}


data class Category(
    override val id: Int,
    override val title: String,
    override val createdAt: Timestamp,
) : Item


data class Task(
    override val id: Int,
    override val title: String,
    val description: String?,
    val categories: List<Category>,
    override val createdAt: Timestamp,
    val deadline: Timestamp?,
) : Item


enum class TimeFilter {
    TODAY,
    WEEK,
    MONTH,
    YEAR
}


enum class DeadlineFilter {
    WITH_DEADLINE,
    WITHOUT_DEADLINE
}


enum class WithDeadlineFilter {
    CURRENT,
    OVERDUE
}


fun getConnection(): Connection {
    val port = System.getenv("DB_PORT")
    val dbName = System.getenv("DB_NAME")
    val url = "jdbc:postgresql://localhost:$port/$dbName"
    val user = System.getenv("DB_USER")
    val password = System.getenv("DB_PASSWORD")
    val schema = System.getenv("DB_SCHEMA")

    val props = Properties().apply {
        put("user", user)
        put("password", password)
        put("currentSchema", schema)
    }
    return DriverManager.getConnection(url, props)
}

open class ToDoDataAccessObject(private val conn: Connection) {
    protected fun createCategoryInstance(rs: ResultSet): Category {
        return Category(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getTimestamp("created_at"),
        )
    }

    fun createCategory(title: String) {
        val stmt = conn.prepareStatement("INSERT INTO categories(title) VALUES (?)")
        stmt.setString(1, title)
        stmt.executeUpdate()
    }

    fun updateCategory(categoryId: Int, title: String) {
        val stmt = conn.prepareStatement(
            """
            UPDATE categories 
            SET title = ? 
            WHERE id = ?
            """.trimIndent()
        )
        stmt.setString(1, title)
        stmt.setInt(2, categoryId)
        stmt.executeUpdate()
    }

    fun deleteCategories(categoryIds: Set<Int>) {
        val stmt = conn.prepareStatement(
            """
            DELETE  
            FROM categories
            WHERE id = ANY (?)
            """.trimIndent()
        )
        stmt.setArray(1, conn.createArrayOf("INTEGER", categoryIds.toTypedArray()))
        stmt.executeUpdate()
    }

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        // safe: escape chars, protect from sql-injection
        val stmt = conn.prepareStatement("SELECT * FROM categories")
        val rs = stmt.executeQuery()

        while (rs.next()) {
            categories.add(createCategoryInstance(rs))
        }
        return categories
    }

    protected fun createTaskInstance(rs: ResultSet): Task {
        return Task(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            getTaskCategories(rs.getInt("id")),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("deadline"),
        )
    }

    protected fun getTaskCategories(taskId: Int): List<Category> {
        val taskCategories = mutableListOf<Category>()
        val stmt = conn.prepareStatement(
            """
            SELECT c.*
            FROM categories c
            JOIN tasks_categories tc ON c.id = tc.category_id
            WHERE tc.task_id = ?
            """.trimIndent()
        )
        stmt.setInt(1, taskId)
        val rs = stmt.executeQuery()

        while (rs.next()) {
            taskCategories.add(
                Category(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getTimestamp("created_at")
                )
            )
        }
        return taskCategories
    }

    protected fun createTaskCategoryLinks(taskId: Int, categoryIds: Set<Int>) {
        val stmt = conn.prepareStatement(
            """
            INSERT INTO tasks_categories(task_id, category_id)
            SELECT ?, unnest(?)
            """.trimIndent()
        )

        stmt.setInt(1, taskId)
        stmt.setArray(2, conn.createArrayOf("INTEGER", categoryIds.toTypedArray()))

        stmt.executeUpdate()
    }

    protected fun createTaskItself(title: String, description: String?, deadline: Timestamp?): Int {
        // todo how this solve sql-injection risk
        val stmt = conn.prepareStatement(
            """
            INSERT INTO tasks(title, description, deadline) 
            VALUES (?, ?, ?)
            RETURNING id
            """.trimIndent()
        )
        // todo duplicate code (see below)
        stmt.setString(1, title)
        if (description != null) {
            stmt.setString(2, description)
        } else {
            // todo understand why do i need to pass java.sql.Types.VARCHAR even though type of column in db has type
            //  why do i need to specify it explicitly. Read in some google source, not gpt.
            stmt.setNull(2, java.sql.Types.VARCHAR)
        }

        if (deadline != null) {
            stmt.setTimestamp(3, deadline)
        } else {
            stmt.setNull(3, java.sql.Types.TIMESTAMP)
        }

        val rs = stmt.executeQuery()

        // get taskId
        rs.next()
        val taskId = rs.getInt("id")
        return taskId
    }

    fun createTask(title: String, description: String?, deadline: Timestamp?, categoryIds: Set<Int>) {
        conn.autoCommit = false

        try {
            val taskId = createTaskItself(title, description, deadline)
            createTaskCategoryLinks(taskId, categoryIds)
            conn.commit()
        } catch (e: Exception) {
            conn.rollback()
            throw e
        }
    }

    fun deleteTasks(taskIds: Set<Int>) {
        conn.autoCommit = false
        val stmt = conn.prepareStatement(
            "DELETE FROM tasks WHERE id = ANY (?)"
        )
        // ids.toTypedArray converts kotlin List to java array
        // conn.createArrayOf converts java array to SQL array
        stmt.setArray(1, conn.createArrayOf("INTEGER", taskIds.toTypedArray()))

        try {
            stmt.executeUpdate()
            conn.commit()
        } catch (e: Exception) {
            conn.rollback()
            throw e
        }
    }

    protected fun updateTaskCategoryLinks(taskId: Int, categoryIds: Set<Int>) {
        // 1. Delete old relations
        val deleteStmt = conn.prepareStatement(
            "DELETE FROM tasks_categories WHERE task_id = ?"
        )
        deleteStmt.setInt(1, taskId)
        deleteStmt.executeUpdate()

        // 2. Insert new ones
        if (categoryIds.isNotEmpty()) {
            val insertStmt = conn.prepareStatement(
                """
                INSERT INTO tasks_categories(task_id, category_id)
                SELECT ?, unnest(?)
                """.trimIndent()
            )

            insertStmt.setInt(1, taskId)
            insertStmt.setArray(2,conn.createArrayOf("INTEGER", categoryIds.toTypedArray()))
            insertStmt.executeUpdate()
        }
    }

    protected fun updateTaskItself(taskId: Int, title: String, description: String?, deadline: Timestamp?) {
        val stmt = conn.prepareStatement(
            """
            UPDATE tasks
            SET title = ?, description = ?, deadline = ?
            WHERE id = ?
            """.trimIndent()
        )
        // todo duplicate code (same as in createTask)
        stmt.setString(1, title)

        if (description != null) {
            stmt.setString(2, description)
        } else {
            stmt.setNull(2, java.sql.Types.VARCHAR)
        }

        if (deadline != null) {
            stmt.setTimestamp(3, deadline)
        } else {
            stmt.setNull(3, java.sql.Types.TIMESTAMP)
        }

        stmt.setInt(4, taskId)
        stmt.executeUpdate()
    }

    fun updateTask(taskId: Int, title: String, description: String?, deadline: Timestamp?, categoryIds: Set<Int>) {
        conn.autoCommit = false

        try {
            // delete rows in junction table first
            // to avoid DB throwing a foreign key constraint violation (possible) todo check violation
            updateTaskCategoryLinks(taskId, categoryIds)
            updateTaskItself(taskId, title, description, deadline)
            conn.commit()
        } catch (e: Exception) {
            conn.rollback()
            throw e
        }
    }

    fun getAllTasks(): List<Task> {
        val result = mutableListOf<Task>()
        // safe: escape chars, protect from sql-injection
        val stmt = conn.prepareStatement("SELECT * FROM tasks")
        val rs = stmt.executeQuery()

        while (rs.next()) {
            result.add(createTaskInstance(rs))
        }
        return result
    }

    fun getTasksByCategoryId(categoryId: Int): List<Task> {
        val result = mutableListOf<Task>()
        val stmt = conn.prepareStatement(
            """
            SELECT t.*
            FROM tasks t
            JOIN tasks_categories tc ON t.id = tc.task_id
            WHERE tc.category_id = ?
            """.trimIndent()
        )
        stmt.setInt(1, categoryId)
        val rs = stmt.executeQuery()

        while (rs.next()) {
            result.add(createTaskInstance(rs))
        }

        return result
    }

    fun getTasksByTime(time: TimeFilter): List<Task> {
        val result = mutableListOf<Task>()

        val condition = when (time) {
            TimeFilter.TODAY -> "DATE(deadline) = CURRENT_DATE"
            TimeFilter.WEEK -> "deadline >= date_trunc('week', CURRENT_DATE) AND deadline < date_trunc('week', CURRENT_DATE) + interval '1 week'"
            TimeFilter.MONTH -> "date_trunc('month', deadline) = date_trunc('month', CURRENT_DATE)"
            TimeFilter.YEAR -> "date_trunc('year', deadline) = date_trunc('year', CURRENT_DATE)"
        }

        val stmt = conn.prepareStatement(
            """
            SELECT *
            FROM tasks
            WHERE deadline IS NOT NULL 
                AND $condition
            """.trimIndent()
        )
        val rs = stmt.executeQuery()

        while (rs.next()) {
            result.add(createTaskInstance(rs))
        }
        return result
    }

    fun getTasksByDeadline(deadlineFilter: DeadlineFilter): List<Task> {
        val result = mutableListOf<Task>()
        val condition = when (deadlineFilter) {
            DeadlineFilter.WITH_DEADLINE -> "deadline IS NOT NULL"
            DeadlineFilter.WITHOUT_DEADLINE -> "deadline IS NULL"
        }
        val stmt = conn.prepareStatement(
            """
            SELECT *
            FROM tasks
            WHERE $condition
            """.trimIndent()
        )
        val rs = stmt.executeQuery()
        while (rs.next()) {
            result.add(createTaskInstance(rs))
        }
        return result
    }

    fun getTasksWithDeadline(withDeadlineFilter: WithDeadlineFilter): List<Task> {
        val result = mutableListOf<Task>()
        val condition = when (withDeadlineFilter) {
            WithDeadlineFilter.CURRENT -> "deadline > CURRENT_TIMESTAMP"
            WithDeadlineFilter.OVERDUE -> "deadline <= CURRENT_TIMESTAMP"
        }
        val stmt = conn.prepareStatement(
            """
            SELECT *
            FROM tasks
            WHERE $condition
            """.trimIndent()
        )
        val rs = stmt.executeQuery()
        while (rs.next()) {
            result.add(createTaskInstance(rs))
        }
        return result
    }

    fun completeTask(taskId: Int) {
        deleteTasks(setOf(taskId))
    }
}
