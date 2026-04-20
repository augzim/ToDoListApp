package ui

import database.DeadlineFilter
import database.TimeFilter
import database.ToDoDataAccessObject
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel


// todo refactor
// todo remove color from constructor
// todo replace dao with ItemService!
class Sidebar(
    val color: Color,
    val dao: ToDoDataAccessObject,
    val mediator: Mediator
) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = color


        // todo this here are  buttons, not panels
        // categories
        val categoriesButton = JButton("categories").apply {
            addActionListener {
                mediator.notify(
                    this,
                    Event.ListItemsButtonClicked(ViewMode.Categories)
                )
            }
        }

        // tasks
        val tasksByPanel = createSubPanel(color, indent = 20)
        // todo repeats 4 times
        val tasksButton = JButton("tasks").apply {
            addActionListener {
                tasksByPanel.isVisible = !tasksByPanel.isVisible
                revalidate()
                repaint()
            }
        }

        // todo add everything in the end
        //  via apply?

        add(categoriesButton)
        add(tasksButton)
        add(tasksByPanel)

        // by time
        val tasksByTimePanel = createSubPanel(color, indent = 20)
        val tasksByTimeButton = JButton("by time")


        tasksByPanel.add(tasksByTimeButton)
        tasksByPanel.add(tasksByTimePanel)

        TimeFilter.entries.forEach { timeFilter ->
            var button = JButton(timeFilter.toString().lowercase())
            tasksByTimePanel.add(button)
            button.addActionListener {
                mediator.notify(
                    this,
                    Event.ListItemsButtonClicked(ViewMode.TasksByTime(timeFilter))
                )
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
                mediator.notify(
                    this,
                    Event.ListItemsButtonClicked(ViewMode.TasksByDeadline(deadlineFilter))
                )
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
                mediator.notify(
                    this,
                    Event.ListItemsButtonClicked(ViewMode.TasksByCategory(category.id))
                )
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