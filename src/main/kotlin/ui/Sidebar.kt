package ui

import database.DeadlineFilter
import database.TimeFilter
import database.ToDoDataAccessObject
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel


class Sidebar(
    val color: Color,
    val dao: ToDoDataAccessObject,
    val contentPanel: ContentPanel
) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        this.background = color


        // categories
        val categoriesButton = JButton("categories")
        add(categoriesButton)

        categoriesButton.addActionListener {
            contentPanel.showCategories()
        }


        // tasks
        val tasksButton = JButton("tasks")
        add(tasksButton)
        val tasksByPanel = createSubPanel(color, indent = 20)
        add(tasksByPanel)
        // todo repeats 4 times
        tasksButton.addActionListener {
            tasksByPanel.isVisible = !tasksByPanel.isVisible
            revalidate()
            repaint()
        }

        // by time
        val tasksByTimeButton = JButton("by time")
        tasksByPanel.add(tasksByTimeButton)
        val tasksByTimePanel = createSubPanel(color, indent = 20)
        tasksByPanel.add(tasksByTimePanel)

        TimeFilter.entries.forEach { timeFilter ->
            var button = JButton(timeFilter.toString().lowercase())
            tasksByTimePanel.add(button)
            button.addActionListener {
                contentPanel.showTasksByTime(timeFilter)
            }
        }

        tasksByTimeButton.addActionListener {
            tasksByTimePanel.isVisible = !tasksByTimePanel.isVisible
            revalidate()
            repaint()
        }


        // by deadline
        val tasksByDeadlineButton = JButton("by deadline")
        tasksByPanel.add(tasksByDeadlineButton)
        val tasksByDeadlinePanel = createSubPanel(color, indent = 20)
        tasksByPanel.add(tasksByDeadlinePanel)

        DeadlineFilter.entries.forEach { deadlineFilter ->
            var button = JButton(deadlineFilter.toString().lowercase().replace('_', ' '))
            tasksByDeadlinePanel.add(button)
            button.addActionListener {
                contentPanel.showTasksByDeadline(deadlineFilter)
            }
        }

        tasksByDeadlineButton.addActionListener {
            tasksByDeadlinePanel.isVisible = !tasksByDeadlinePanel.isVisible
            revalidate()
            repaint()
        }


        // by category
        val tasksByCategoryButton = JButton("by category")
        tasksByPanel.add(tasksByCategoryButton)
        val tasksByCategoryPanel = createSubPanel(color, indent = 20)
        tasksByPanel.add(tasksByCategoryPanel)

        val categories = dao.getAllCategories()

        for (category in categories) {
            var button = JButton(category.title)
            tasksByCategoryPanel.add(button)
            button.addActionListener {
                contentPanel.showTasksByCategory(category.id)
            }
        }

        tasksByCategoryButton.addActionListener {
            tasksByCategoryPanel.isVisible = !tasksByCategoryPanel.isVisible
            revalidate()
            repaint()
        }

    }

    fun createSubPanel(color: Color, indent: Int = 20): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(0, indent, 0, 0)
            background = color
            isVisible = false
        }
    }
}