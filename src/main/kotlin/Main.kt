import database.getConnection
import database.ToDoDataAccessObject
import service.ItemService
import ui.ContentPanel
import ui.ItemCreationPanel
import ui.MainMediator
import ui.Sidebar

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities



fun main() {
    SwingUtilities.invokeLater {
        val conn = getConnection()
        val dao = ToDoDataAccessObject(conn)
        val itemService = ItemService(dao)

        val frame = JFrame("To Do List App").apply {
            defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            minimumSize = Dimension(200, 100)
            isVisible = true
            setSize(500, 400)
            setLocationRelativeTo(null)
        }

        val rootPanel = JPanel(BorderLayout())
        val mediator = MainMediator()
        val contentPanelView = ContentPanel(itemService, mediator)
        val sidebarView = Sidebar(
            Color.PINK,
            itemService,
            mediator
        )
        val itemCreationPanelView =
            ItemCreationPanel(itemService, mediator).apply {
                background = Color.CYAN
            }

        mediator.apply {
            sidebar = sidebarView
            contentPanel = contentPanelView
            itemCreationPanel = itemCreationPanelView
        }
        rootPanel.apply {
            add(sidebarView, BorderLayout.WEST)
            add(contentPanelView, BorderLayout.CENTER)
            add(itemCreationPanelView, BorderLayout.EAST)
        }
        frame.apply {
            add(rootPanel, BorderLayout.CENTER)
        }
    }
}
