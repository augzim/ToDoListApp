import database.getConnection
import database.ToDoDataAccessObject
import service.ItemService
import ui.ContentPanel
import ui.ItemCreationPanel
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
            setSize(500, 400)
            isVisible = true
            setLocationRelativeTo(null)
            minimumSize = Dimension(200, 100)
        }

        val rootPanel = JPanel(BorderLayout())
        frame.add(rootPanel, BorderLayout.CENTER)

        val contentPanel = ContentPanel(itemService)
        rootPanel.add(contentPanel, BorderLayout.CENTER)

        val sidebar = Sidebar(
            Color.PINK,
            dao,
            contentPanel
        )
        rootPanel.add(sidebar, BorderLayout.WEST)

        val createEntityPanel = ItemCreationPanel(contentPanel, itemService)
        createEntityPanel.background = Color.CYAN
        rootPanel.add(createEntityPanel, BorderLayout.EAST)

    }
}
