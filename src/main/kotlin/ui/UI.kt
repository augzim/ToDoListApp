package ui

import database.DeadlineFilter
import database.TimeFilter
import database.ToDoDataAccessObject
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.SpinnerDateModel


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

class ContentPanel(val dao: ToDoDataAccessObject) : JPanel() {
    init {
        background = Color.GREEN
    }

    // todo duplicate code in functions
    fun showCategories() {
        removeAll()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val categories = dao.getAllCategories()
        for (category in categories) {
            add(JButton(category.title))
        }
        revalidate()
        repaint()
    }

    fun showTasksByTime(timeFilter: TimeFilter) {
        removeAll()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val tasks = dao.getTasksByTime(timeFilter)
        for (task in tasks) {
            add(JLabel(task.title))
        }
        revalidate()
        repaint()
    }

    fun showTasksByCategory(categoryId: Int) {
        removeAll()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val tasks = dao.getTasksByCategoryId(categoryId)
        for (task in tasks) {
            add(JLabel(task.title))
        }
        revalidate()
        repaint()
    }

    fun showTasksByDeadline(deadlineFilter: DeadlineFilter) {
        removeAll()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val tasks = dao.getTasksByDeadline(deadlineFilter)
        for (task in tasks) {
            add(JLabel(task.title))
        }
        revalidate()
        repaint()
    }

}


class CreateEntityPanel(val dao: ToDoDataAccessObject) : JPanel() {
    init {
        showCreateTask()
    }

    fun showCreateCategory() {
        removeAll()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        // enter title

        // show done button
        // when pressed show empty fields for creating task again
        revalidate()
        repaint()
    }

    fun showCreateTask() {
        removeAll()
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // enter title
        add(JLabel("Title"))
        val titleField = JTextField(2)
        add(titleField)

        // enter description
        val descriptionArea = JTextArea(5, 20)
        val scroll = JScrollPane(descriptionArea)
        add(scroll)

        // choose categories
        val categories = dao.getAllCategories().sortedBy { it.title }
        val list = JList(categories.toTypedArray())
        list.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        val listScroll = JScrollPane(list)
        add(listScroll)

        // enter date
        // todo look for 3rd party libs
        val spinner = JSpinner(SpinnerDateModel())
        spinner.editor = JSpinner.DateEditor(spinner, "yyyy-MM-dd")
        add(spinner)

        // time
        add(JLabel("Time"))
        val timeSpinner = JSpinner(SpinnerDateModel())
        timeSpinner.editor = JSpinner.DateEditor(timeSpinner, "HH:mm")
        add(timeSpinner)

        // todo collect user input, prepare, send to db
        val doneButton = JButton("Done")
        add(doneButton)
        // show done button
        // when pressed show empty fields for creating task again
        revalidate()
        repaint()
    }
}